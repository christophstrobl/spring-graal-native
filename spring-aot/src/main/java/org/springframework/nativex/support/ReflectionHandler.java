/*
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.nativex.support;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.nativex.AotOptions;
import org.springframework.nativex.domain.reflect.ClassDescriptor;
import org.springframework.nativex.domain.reflect.FieldDescriptor;
import org.springframework.nativex.domain.reflect.MethodDescriptor;
import org.springframework.nativex.domain.reflect.ReflectionDescriptor;
import org.springframework.nativex.type.AccessChecker;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.hint.Flag;
import org.springframework.nativex.type.AccessDescriptor;
import org.springframework.util.ObjectUtils;


/**
 * Loads up the constant data defined in resource file and registers reflective
 * access being necessary with the image build. Also provides an method
 * ({@code addAccess(String typename, Flag... flags)} usable from elsewhere
 * when needing to register reflective access to a type (e.g. used when resource
 * processing).
 * 
 * @author Andy Clement
 */
public class ReflectionHandler extends Handler {
	
	private static Log logger = LogFactory.getLog(ReflectionHandler.class);

	private ReflectionDescriptor constantReflectionDescriptor;

	private final AotOptions aotOptions;

	public ReflectionHandler(ConfigurationCollector collector, AotOptions aotOptions) {
		super(collector);
		this.aotOptions = aotOptions;
	}

	private void registerWebApplicationTypeClasses() {
		if (ts.resolveDotted("org.springframework.web.reactive.DispatcherHandler", true) !=null && 
			ts.resolveDotted("org.springframework.web.servlet.DispatcherServlet", true) == null && 
			ts.resolveDotted("org.glassfish.jersey.servlet.ServletContainer", true) == null) {
			addAccess("org.springframework.web.reactive.DispatcherHandler");
		} else if (ts.resolveDotted("javax.servlet.Servlet", true) !=null && 
			ts.resolveDotted("org.springframework.web.context.ConfigurableWebApplicationContext", true) != null) {
			addAccess("javax.servlet.Servlet");
			addAccess("org.springframework.web.context.ConfigurableWebApplicationContext");
		}
	}

	public void register() {
		registerWebApplicationTypeClasses();
		if (!aotOptions.isRemoveYamlSupport()) {
			addAccess("org.yaml.snakeyaml.Yaml", Flag.allDeclaredConstructors, Flag.allDeclaredMethods);
		}
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
	 */
	public void addAccess(String typename, Flag...flags) {
		addAccess(typename, null, null, false, flags);
	}
	
	public void addAccess(String typename, boolean silent, AccessDescriptor ad) {
		if (ad.noMembersSpecified()) {
			addAccess(typename, null, null, silent, AccessBits.getFlags(ad.getAccessBits()));
		} else {
			List<org.springframework.nativex.type.MethodDescriptor> mds = ad.getMethodDescriptors();
			String[][] methodsAndConstructors = new String[mds.size()][];
			for (int m=0;m<mds.size();m++) {
				org.springframework.nativex.type.MethodDescriptor methodDescriptor = mds.get(m);
				methodsAndConstructors[m] = new String[methodDescriptor.getParameterTypes().size()+1];
				methodsAndConstructors[m][0] = methodDescriptor.getName();
				List<String> ps = methodDescriptor.getParameterTypes();
				for (int p=0;p<ps.size();p++) {
					methodsAndConstructors[m][p+1]=ps.get(p);
				}
			}
			List<org.springframework.nativex.type.FieldDescriptor> fds = ad.getFieldDescriptors();
			String[][] fields = new String[fds.size()][];
			for (int m=0;m<mds.size();m++) {
				org.springframework.nativex.type.FieldDescriptor fieldDescriptor = fds.get(m);
				if (fieldDescriptor.isAllowUnsafeAccess()) {
					if(fieldDescriptor.isAllowWrite()) {
						fields[m]=new String[] {fieldDescriptor.getName(),Boolean.toString(fieldDescriptor.isAllowUnsafeAccess()), Boolean.toString(fieldDescriptor.isAllowWrite())};
					} else {
						fields[m] = new String[]{fieldDescriptor.getName(), Boolean.toString(fieldDescriptor.isAllowUnsafeAccess())};
					}
				} else {
					if(fieldDescriptor.isAllowWrite()) {
						fields[m]=new String[] {fieldDescriptor.getName(),"false", Boolean.toString(fieldDescriptor.isAllowWrite())};
					} else {
						fields[m] = new String[]{fieldDescriptor.getName()};
					}
				}
			}
			addAccess(typename, methodsAndConstructors, fields, silent, AccessBits.getFlags(ad.getAccessBits()));
		}
	}
	
	public static String[] subarray(String[] array) {
		if (array.length == 1) {
			return null;
		} else {
			return Arrays.copyOfRange(array, 1, array.length);
		}
	}

	public void addAccess(String typename, String[][] methodsAndConstructors, String[][] fields, boolean silent, Flag... flags) {
		if (!silent) {
			logger.debug("Registering reflective access to " + typename+": "+(flags==null?"":Arrays.asList(flags)));
		}
		List<AccessChecker> accessCheckers = ts.getAccessCheckers();
		for (AccessChecker accessChecker: accessCheckers) {
			boolean isOK = accessChecker.check(ts, typename);
			if (!isOK) {
				logger.debug(typename+" discarded due to access check by "+accessChecker.getClass().getName());
				return;
			}
		}
		// This can return null if, for example, the supertype of the specified type is
		// not on the classpath. In a simple app there may be a number of types coming in
		// from spring-boot-autoconfigure but they extend types not on the classpath.
//		Class<?> type = rra.resolveType(typename);
//		if (type == null) {
//			logger.info("WARNING: Possible problem, cannot resolve " + typename);
//			return null;
//		}
		
		ClassDescriptor cd = ClassDescriptor.of(typename);
		if (cd == null) {
			cd  = ClassDescriptor.of(typename);
		}
		// Update flags...
		for (Flag f : flags) {
			cd.setFlag(f);
		}
		if (methodsAndConstructors != null) {
			for (String[] mc: methodsAndConstructors) {
				MethodDescriptor md = MethodDescriptor.of(mc[0], subarray(mc));
				if (!cd.contains(md)) {
					cd.addMethodDescriptor(md);	
				}
			}
		}
		if (fields != null) {
			for (String[] fs: fields) {

				boolean allowUnsafeAccess = Boolean.valueOf(fs.length>1?fs[1]:"false");
				boolean allowWrite = Boolean.valueOf(fs.length>2?fs[2]:"false");

				FieldDescriptor fd = FieldDescriptor.of(fs[0],allowWrite,allowUnsafeAccess);
				FieldDescriptor existingFd = cd.getFieldDescriptorNamed(fd.getName());
				if (existingFd != null) {
					/*
					 * TODO: if the check below is removed then we can end up with the very same FieldDescriptor added more than once.
					 *  Problem is the reflect-config in the generated test sources will have the field set once and work but the one in main will fail with the duplicate
					 *  and I don't know where this is coming from.
					 *  You may try the data-rest sample and have a look at the following type hint in DataRestHints:
					 *  	@TypeHint(typeNames = "org.springframework.hateoas.CollectionModel", fields = @FieldHint(name = "content", allowUnsafeAccess = true, allowWrite = true))
					 */
					if(!ObjectUtils.nullSafeEquals(fd, existingFd)) {
						throw new IllegalStateException(String.format("nyi - need to merge field description %s with existing %s for type %s.", fd, existingFd, typename)); // merge of configuration necessary
					}
				} else {
					cd.addFieldDescriptor(fd);
				}
			}
		}
		collector.addClassDescriptor(cd);
	}
	
	public ClassDescriptor getClassDescriptor(String typename) {
		return collector.getClassDescriptorFor(typename);
	}

}
