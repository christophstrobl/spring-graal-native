/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.graalvm.support;

import static org.springframework.graalvm.domain.reflect.Flag.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.Feature.DuringSetupAccess;
import org.graalvm.nativeimage.impl.RuntimeReflectionSupport;
import org.graalvm.util.GuardedAnnotationAccess;
import org.springframework.graalvm.domain.reflect.ClassDescriptor;
import org.springframework.graalvm.domain.reflect.FieldDescriptor;
import org.springframework.graalvm.domain.reflect.Flag;
import org.springframework.graalvm.domain.reflect.JsonMarshaller;
import org.springframework.graalvm.domain.reflect.MethodDescriptor;
import org.springframework.graalvm.domain.reflect.ReflectionDescriptor;
import org.springframework.graalvm.type.AccessBits;
import org.springframework.graalvm.type.AccessDescriptor;
import org.springframework.graalvm.type.Type;

import com.oracle.svm.core.hub.ClassForNameSupport;
import com.oracle.svm.hosted.FeatureImpl.DuringSetupAccessImpl;
import com.oracle.svm.hosted.ImageClassLoader;
import com.oracle.svm.hosted.config.ReflectionRegistryAdapter;

/**
 * Loads up the constant data defined in resource file and registers reflective
 * access being necessary with the image build. Also provides an method
 * (<tt>addAccess(String typename, Flag... flags)</tt>} usable from elsewhere
 * when needing to register reflective access to a type (e.g. used when resource
 * processing).
 * 
 * @author Andy Clement
 */
public class ReflectionHandler {

	private final static String RESOURCE_FILE = "/reflect.json";

	private ReflectionRegistryAdapter rra;

	private ReflectionDescriptor constantReflectionDescriptor;

	private ImageClassLoader cl;

	private int typesRegisteredForReflectiveAccessCount = 0;

	public ReflectionDescriptor getConstantData() {
		if (constantReflectionDescriptor == null) {
			try {
				InputStream s = this.getClass().getResourceAsStream(RESOURCE_FILE);
				constantReflectionDescriptor = JsonMarshaller.read(s);
			} catch (Exception e) {
				throw new IllegalStateException("Unexpectedly can't load " + RESOURCE_FILE, e);
			}
		}
		return constantReflectionDescriptor;
	}
	
	private List<ClassDescriptor> activeClassDescriptors = new ArrayList<>();

	public void includeInDump(String typename, String[][] methodsAndConstructors, Flag[] flags) {
		if (!ConfigOptions.shouldDumpConfig()) {
			return;
		}
		ClassDescriptor currentCD = null;
		for (ClassDescriptor cd: activeClassDescriptors) {
			if (cd.getName().equals(typename)) {
				currentCD = cd;
				break;
			}
		}
		if (currentCD == null) {
			currentCD  = ClassDescriptor.of(typename);
			activeClassDescriptors.add(currentCD);
		}
		// Update flags...
		for (Flag f : flags) {
			currentCD.setFlag(f);
		}
		if (methodsAndConstructors != null) {
			for (String[] mc: methodsAndConstructors) {
				MethodDescriptor md = MethodDescriptor.of(mc[0], subarray(mc));
				if (!currentCD.contains(md)) {
					currentCD.addMethodDescriptor(md);	
				}
			}
		}
	}
	
	public static String[] subarray(String[] array) {
		if (array.length == 1) {
			return null;
		} else {
			return Arrays.copyOfRange(array, 1, array.length);
		}
	}
		
		
	public void dump() {
		if (!ConfigOptions.shouldDumpConfig()) {
			return;
		}
		activeClassDescriptors.sort((c1,c2) -> c1.getName().compareTo(c2.getName()));
		ReflectionDescriptor rd = new ReflectionDescriptor();
		for (ClassDescriptor cd: activeClassDescriptors) {
			rd.add(cd);
		}
		try (FileOutputStream fos = new FileOutputStream(new File(ConfigOptions.getDumpConfigLocation()))) {
			JsonMarshaller.write(rd,fos);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public void registerHybrid(DuringSetupAccess a) {
		DuringSetupAccessImpl access = (DuringSetupAccessImpl) a;
		RuntimeReflectionSupport rrs = ImageSingletons.lookup(RuntimeReflectionSupport.class);
		cl = access.getImageClassLoader();
		rra = new ReflectionRegistryAdapter(rrs, cl);
		getConstantData();

		if (rra.resolveType("org.springframework.web.servlet.DispatcherServlet") != null) {
			addAccess("org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedWebappClassLoader", Flag.allDeclaredConstructors, Flag.allDeclaredMethods);
		}
	}

	public void registerAgent(DuringSetupAccess a) {
		DuringSetupAccessImpl access = (DuringSetupAccessImpl) a;
		RuntimeReflectionSupport rrs = ImageSingletons.lookup(RuntimeReflectionSupport.class);
		cl = access.getImageClassLoader();
		rra = new ReflectionRegistryAdapter(rrs, cl);
		getConstantData();
	}

	public void registerFunctional(DuringSetupAccess a) {
		DuringSetupAccessImpl access = (DuringSetupAccessImpl) a;
		RuntimeReflectionSupport rrs = ImageSingletons.lookup(RuntimeReflectionSupport.class);
		cl = access.getImageClassLoader();
		rra = new ReflectionRegistryAdapter(rrs, cl);
		getConstantData();

		if (rra.resolveType("org.springframework.web.servlet.DispatcherServlet") != null) {
			addAccess("org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedWebappClassLoader", Flag.allDeclaredConstructors, Flag.allDeclaredMethods);
		}
		registerWebApplicationTypeClasses();
	}

	private void registerWebApplicationTypeClasses() {
		if (rra.resolveType("org.springframework.web.reactive.DispatcherHandler") !=null && rra.resolveType("org.springframework.web.servlet.DispatcherServlet") == null && rra.resolveType("org.glassfish.jersey.servlet.ServletContainer") == null) {
			addAccess("org.springframework.web.reactive.DispatcherHandler");
		} else
		if (rra.resolveType("javax.servlet.Servlet") !=null && rra.resolveType("org.springframework.web.context.ConfigurableWebApplicationContext") != null) {
			addAccess("javax.servlet.Servlet");
			addAccess("org.springframework.web.context.ConfigurableWebApplicationContext");
		}
	}

	public void register(DuringSetupAccess a) {
		DuringSetupAccessImpl access = (DuringSetupAccessImpl) a;
		RuntimeReflectionSupport rrs = ImageSingletons.lookup(RuntimeReflectionSupport.class);
		cl = access.getImageClassLoader();
		rra = new ReflectionRegistryAdapter(rrs, cl);
		ReflectionDescriptor reflectionDescriptor = getConstantData();

		System.out.println("Found #" + reflectionDescriptor.getClassDescriptors().size()
				+ " types in static reflection list to register");
		int missingFromClasspathCount = 0;
		int flagHandlingCount = 0;
		for (ClassDescriptor classDescriptor : reflectionDescriptor.getClassDescriptors()) {
			Class<?> type = null;
			String n2 = classDescriptor.getName();
			if (n2.endsWith("[]")) {
				type = rra.resolveType(n2.substring(0, n2.length() - 2));
				if (type != null) {
					Object o = Array.newInstance(type, 1);
					type = o.getClass();
				}
			} else {
				type = rra.resolveType(classDescriptor.getName());
			}
			if (type == null) {
				missingFromClasspathCount++;
				SpringFeature.log(RESOURCE_FILE + " included " + classDescriptor.getName()
						+ " but it doesn't exist on the classpath, skipping...");
				continue;
			}
			if (checkType(type)) {
				activeClassDescriptors.add(classDescriptor);
				rra.registerType(type);
				Set<Flag> flags = classDescriptor.getFlags();
				if (flags != null) {
					for (Flag flag : flags) {
						try {
							switch (flag) {
							case allDeclaredClasses:
								rra.registerDeclaredClasses(type);
								break;
							case allDeclaredFields:
								rra.registerDeclaredFields(type);
								break;
							case allPublicFields:
								rra.registerPublicFields(type);
								break;
							case allDeclaredConstructors:
								rra.registerDeclaredConstructors(type);
								break;
							case allPublicConstructors:
								rra.registerPublicConstructors(type);
								break;
							case allDeclaredMethods:
								rra.registerDeclaredMethods(type);
								break;
							case allPublicMethods:
								rra.registerPublicMethods(type);
								break;
							case allPublicClasses:
								rra.registerPublicClasses(type);
								break;
							}
						} catch (NoClassDefFoundError ncdfe) {
							flagHandlingCount++;
							SpringFeature.log(RESOURCE_FILE + " problem handling flag: " + flag + " for "
									+ type.getName() + " because of missing " + ncdfe.getMessage());
						}
					}
				}
				typesRegisteredForReflectiveAccessCount++;
			}

			// Process all specific methods defined in the input class descriptor (including
			// constructors)
			List<MethodDescriptor> methods = classDescriptor.getMethods();
			if (methods != null) {
				for (MethodDescriptor methodDescriptor : methods) {
					String n = methodDescriptor.getName();
					List<String> parameterTypes = methodDescriptor.getParameterTypes();
					if (parameterTypes == null) {
						if (n.equals("<init>")) {
							rra.registerAllConstructors(type);
						} else {
							rra.registerAllMethodsWithName(type, n);
						}
					} else {
						List<Class<?>> collect = parameterTypes.stream().map(pname -> rra.resolveType(pname))
								.collect(Collectors.toList());
						try {
							if (n.equals("<init>")) {
								rra.registerConstructor(type, collect);
							} else {
								rra.registerMethod(type, n, collect);
							}
						} catch (NoSuchMethodException nsme) {
							throw new IllegalStateException("Couldn't find: " + methodDescriptor.toString(), nsme);
						}
					}
				}
			}

			// Process all specific fields defined in the input class descriptor
			List<FieldDescriptor> fields = classDescriptor.getFields();
			if (fields != null) {
				for (FieldDescriptor fieldDescriptor : fields) {
					try {
						rra.registerField(type, fieldDescriptor.getName(), fieldDescriptor.isAllowWrite(),
								fieldDescriptor.isAllowUnsafeAccess());
					} catch (NoSuchFieldException nsfe) {
						throw new IllegalStateException(
								"Couldn't find field: " + type.getName() + "." + fieldDescriptor.getName(), nsfe);
//						System.out.println("SBG: WARNING: skipping reflection registration of field "+type.getName()+"."+fieldDescriptor.getName()+": field not found");
					}
				}
			}
		}
		if (missingFromClasspathCount != 0) {
			System.out.println("Skipping #" + missingFromClasspathCount + " types not on the classpath");
		}
		if (flagHandlingCount != 0) {
			System.out.println(
					"Number of problems processing field/method/constructor access requests: #" + flagHandlingCount);
		}

		registerWebApplicationTypeClasses();

		if (!ConfigOptions.shouldRemoveYamlSupport()) {
			addAccess("org.yaml.snakeyaml.Yaml", Flag.allDeclaredConstructors, Flag.allDeclaredMethods);
		}
	}

	private boolean checkType(Class clazz) {
		try {
			clazz.getDeclaredFields();
			clazz.getFields();
			clazz.getDeclaredMethods();
			clazz.getMethods();
			clazz.getDeclaredConstructors();
			clazz.getConstructors();
			clazz.getDeclaredClasses();
			clazz.getClasses();
		} catch (NoClassDefFoundError e) {
			return false;
		}
		return true;
	}

	/**
	 * Record that reflective access to a type (and a selection of its members based
	 * on the flags) should be possible at runtime. This method will pre-emptively
	 * check all type references to ensure later native-image processing will not
	 * fail if, for example, it trips up over a type reference in a generic type
	 * that isn't on the image building classpath. NOTE: it is assumed that if
	 * elements are not accessible that the runtime doesn't need them (this is done
	 * under the spring model where conditional checks on auto configuration would
	 * cause no attempts to be made to types/members that aren't added here).
	 * 
	 * @param typename the dotted type name for which to add reflective access
	 * @param flags    any members that should be accessible via reflection
	 * @return the class, if the type was successfully registered for reflective
	 *         access, otherwise null
	 */
	public Class<?> addAccess(String typename, Flag...flags) {
		return addAccess(typename, null, null, false, flags);
	}

	public Class<?> addAccess(String typename, boolean silent, AccessDescriptor ad) {
		if (ad.noMembersSpecified()) {
			return addAccess(typename, null, null, silent, AccessBits.getFlags(ad.getAccessBits()));
		} else {
			List<org.springframework.graalvm.type.MethodDescriptor> mds = ad.getMethodDescriptors();
			String[][] methodsAndConstructors = new String[mds.size()][];
			for (int m=0;m<mds.size();m++) {
				org.springframework.graalvm.type.MethodDescriptor methodDescriptor = mds.get(m);
				methodsAndConstructors[m] = new String[methodDescriptor.getParameterTypes().size()+1];
				methodsAndConstructors[m][0] = methodDescriptor.getName();
				List<String> ps = methodDescriptor.getParameterTypes();
				for (int p=0;p<ps.size();p++) {
					methodsAndConstructors[m][p+1]=ps.get(p);
				}
			}
			List<org.springframework.graalvm.type.FieldDescriptor> fds = ad.getFieldDescriptors();
			String[][] fields = new String[fds.size()][];
			for (int m=0;m<mds.size();m++) {
				org.springframework.graalvm.type.FieldDescriptor fieldDescriptor = fds.get(m);
				if (fieldDescriptor.isAllowUnsafeAccess()) {
					fields[m]=new String[] {fieldDescriptor.getName(),Boolean.toString(fieldDescriptor.isAllowUnsafeAccess())};
				} else {
					fields[m]=new String[] {fieldDescriptor.getName()};
				}
			}
			return addAccess(typename, methodsAndConstructors, fields, silent, AccessBits.getFlags(ad.getAccessBits()));
		}
	}

	public Class<?> addAccess(String typename, String[][] methodsAndConstructors, String[][] fields, boolean silent, Flag... flags) {
		if (!silent) {
			SpringFeature.log("Registering reflective access to " + typename+": "+(flags==null?"":Arrays.asList(flags)));
		}
		includeInDump(typename, methodsAndConstructors, flags);
		// This can return null if, for example, the supertype of the specified type is
		// not on the classpath. In a simple app there may be a number of types coming in
		// from spring-boot-autoconfigure but they extend types not on the classpath.
		Class<?> type = rra.resolveType(typename);
		if (type == null) {
			SpringFeature.log("WARNING: Possible problem, cannot resolve " + typename);
			return null;
		}
		if (constantReflectionDescriptor.hasClassDescriptor(typename)) {
			SpringFeature.log("WARNING: type " + typename + " being added dynamically whilst " + RESOURCE_FILE
					+ " already contains it - does it need to be in the file? ");
		}
		// The call on this next line and the need to guard with checkType on the
		// register call feel dirty
		// They are here because otherwise we start getting warnings to system.out -
		// need graal bug to tidy this up
		ClassForNameSupport.registerClass(type);
		// TODO need a checkType() kinda guard on here? (to avoid rogue printouts from graal)
		boolean specificConstructorsSpecified = false;
		boolean specificMethodsSpecified = false;
		boolean specificFieldsSpecified = false;
		if (methodsAndConstructors != null) {
			for (String[] methodOrCtor : methodsAndConstructors) {
				String name = methodOrCtor[0];
				List<Class<?>> params = new ArrayList<>();
				for (int p = 1; p < methodOrCtor.length; p++) {
					// TODO should use type system and catch problems?
					params.add(rra.resolveType(methodOrCtor[p]));
				}
				try {
					if (name.equals("<init>")) {
						specificConstructorsSpecified=true;
						rra.registerConstructor(type, params);
					} else {
						specificMethodsSpecified=true;
						try {
							rra.registerMethod(type, name, params);
						} catch (NoClassDefFoundError ncdfe) {
							SpringFeature.log("skipping problematic registration of method: "+name+" missing class: "+ncdfe.getMessage());
						}
					}
				} catch (NoSuchMethodException nsme) {
					throw new IllegalStateException(
							"Problem registering member " + name + " for reflective access on type " + type, nsme);
				}
			}
		}
		if (fields != null) {
			for (String[] field : fields) {
				String name = field[0];
				boolean allowUnsafeAccess = Boolean.valueOf(field.length>1?field[1]:"false");
				try {
					rra.registerField(type, name, false, allowUnsafeAccess);
				} catch (NoSuchFieldException nsfe) {
					throw new IllegalStateException(
							"Problem registering field " + name + " for reflective access on type " + type, nsfe);
				}
			}
		}
		if (checkType(type)) {
			rra.registerType(type);

			boolean allowFinalWrite = Arrays.asList(flags).contains(Flag.allowFinalWrite);

			for (Flag flag : flags) {
				try {
					switch (flag) {
					case allDeclaredClasses:
						if (verify(type.getDeclaredClasses())) {
							rra.registerDeclaredClasses(type);
						}
						break;
					case allDeclaredFields:
						if (specificFieldsSpecified) {
							throw new IllegalStateException();
						}
						if (verify(type.getDeclaredFields())) {
							if(!allowFinalWrite) {
								rra.registerDeclaredFields(type);
							} else {
								for(Field field : type.getDeclaredFields()) {

									System.out.println("Registering final write for : " + type +  field.getName());
									try {
										rra.registerField(type, field.getName(), true, false);
									} catch (NoSuchFieldException e) {
										e.printStackTrace();
									}
								}
							}
						}
						break;
					case allPublicFields:
						if (specificFieldsSpecified) {
							throw new IllegalStateException();
						}
						if (verify(type.getFields())) {
							rra.registerPublicFields(type);
						}
						break;
					case allDeclaredConstructors:
						if (specificConstructorsSpecified) {
							throw new IllegalStateException();
						}
						if (verify(type.getDeclaredConstructors())) {
							rra.registerDeclaredConstructors(type);
						}
						break;
					case allPublicConstructors:
						if (specificConstructorsSpecified) {
							throw new IllegalStateException();
						}
						if (verify(type.getConstructors())) {
							rra.registerPublicConstructors(type);
						}
						break;
					case allDeclaredMethods:
						if (specificMethodsSpecified) {
							throw new IllegalStateException();
						}
						if (verify(type.getDeclaredMethods())) {
							rra.registerDeclaredMethods(type);
						}
						break;
					case allPublicMethods:
						if (specificMethodsSpecified) {
							throw new IllegalStateException();
						}
						if (verify(type.getMethods())) {
							rra.registerPublicMethods(type);
						}
						break;
					case allPublicClasses:
						if (verify(type.getClasses())) {
							rra.registerPublicClasses(type);
						}
						break;
					}
				} catch (NoClassDefFoundError ncdfe) {
					SpringFeature.log("WARNING: problem handling flag: " + flag + " for " + type.getName()
							+ " because of missing " + ncdfe.getMessage());
				}
			}
		}
		typesRegisteredForReflectiveAccessCount++;
		return type;
	}

	public int getTypesRegisteredForReflectiveAccessCount() {
		return typesRegisteredForReflectiveAccessCount;
	}

	private boolean verify(Object[] things) {
		for (Object o : things) {
			try {
				if (o instanceof Method) {
					((Method) o).getGenericReturnType();
				}
				if (o instanceof Field) {
					((Field) o).getGenericType();
				}
				if (o instanceof AccessibleObject) {
					AccessibleObject accessibleObject = (AccessibleObject) o;
					GuardedAnnotationAccess.getDeclaredAnnotations(accessibleObject);
				}

				if (o instanceof Parameter) {
					Parameter parameter = (Parameter) o;
					parameter.getType();
				}
				if (o instanceof Executable) {
					Executable e = (Executable) o;
					e.getGenericParameterTypes();
					e.getGenericExceptionTypes();
					e.getParameters();
				}
			} catch (Exception e) {
				SpringFeature.log("WARNING: Possible reflection problem later due to (generics related) reference from "
						+ o + " to " + e.getMessage());
				return false;
			}
		}
		return true;
	}

	private static boolean isPresent(String className) {
		try {
			Class.forName(className);
			return true;
		}
		catch (ClassNotFoundException ex) {
			return false;
		}
	}

}
