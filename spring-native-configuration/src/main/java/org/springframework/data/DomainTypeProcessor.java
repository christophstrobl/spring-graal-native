/*
 * Copyright 2021 the original author or authors.
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
package org.springframework.data;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.nativex.type.Method;
import org.springframework.nativex.type.NativeContext;
import org.springframework.nativex.type.Type;

/**
 * @author Christoph Strobl
 */
public class DomainTypeProcessor {

	private static Log logger = LogFactory.getLog(DomainTypeProcessor.class);

	private final BiPredicate<Type, NativeContext> typeFilter;
	private final BiConsumer<Type, NativeContext> typeRegistrar;
	private final BiConsumer<Type, NativeContext> annotationRegistrar;

	private Predicate<Method> methodFilter = (method) -> method.getName().startsWith("get");
	private Predicate<Method> ctorFilter = (method) -> method.getName().equals("<init>");


	public DomainTypeProcessor(BiPredicate<Type, NativeContext> typeFilter, BiConsumer<Type, NativeContext> typeRegistrar, BiConsumer<Type, NativeContext> annotationRegistrar) {

		this.typeFilter = typeFilter;
		this.typeRegistrar = typeRegistrar;
		this.annotationRegistrar = annotationRegistrar;
	}

	/**
	 * Process a given {@link Type} by iterating of its direct annotations, fields and the annotations on
	 * them as well as matching methods, invoking the type and annotation registrar when needed.
	 *
	 * @param domainType
	 * @param imageContext
	 */
	public void process(Type domainType, NativeContext imageContext) {
		process(domainType, imageContext, new LinkedHashSet<>());
	}

	private void process(Type domainType, NativeContext imageContext, Set<Type> seen) {

		if (!typeFilter.test(domainType, imageContext) || seen.contains(domainType)) {
			return;
		}

		// cycle guard
		seen.add(domainType);

		// call method that will add a domain type if necessary.
		typeRegistrar.accept(domainType, imageContext);

		if (domainType.isPartOfDomain("java.") || domainType.isPartOfDomain("sun.") || domainType.isPartOfDomain("jdk.") || domainType.isPartOfDomain("reactor.")) {
			return;
		}

		// inspect annotations on the type itself and register those if necessary.
		processAnnotationsOnType(domainType, imageContext, seen);

		// inspect the constructor and its parameters
		processConstructorsOfType(domainType, imageContext, seen);

		// inspect fields and register the types if necessary.
		processFieldsOfType(domainType, imageContext, seen);

		// inspect methods and register return types if necessary.
		processMethodsOfType(domainType, imageContext, seen);
	}


	private void processMethodsOfType(Type domainType, NativeContext imageContext, Set<Type> seen) {

		domainType.getMethods(methodFilter)
				.forEach(it -> processMethod(it, imageContext, seen));
	}

	private void processConstructorsOfType(Type domainType, NativeContext imageContext, Set<Type> seen) {

		domainType.getMethods(ctorFilter)
				.forEach(it -> processMethod(it, imageContext, seen));
	}

	private void processMethod(Method method, NativeContext imageContext, Set<Type> seen) {

		method.getSignatureTypes(true).forEach(returnType -> process(returnType, imageContext, seen));
		method.getAnnotationTypes().forEach(annotation -> processAnnotation(annotation, imageContext, seen));

		for (int parameterIndex = 0; parameterIndex < method.getParameterCount(); parameterIndex++) {
			method.getParameterAnnotationTypes(parameterIndex)
					.forEach(it -> processAnnotation(it, imageContext, seen));
		}
	}

	private void processFieldsOfType(Type domainType, NativeContext imageContext, Set<Type> seen) {

		domainType.getFields().forEach(field -> {

			field.getTypesInSignature().stream().map(imageContext.getTypeSystem()::resolve)
					.forEach(signatureType -> process(signatureType, imageContext, seen));

			field.getAnnotationTypes().forEach(annotation -> {
				processAnnotation(annotation, imageContext, seen);
			});
		});
	}

	private void processAnnotationsOnType(Type type, NativeContext imageContext, Set<Type> seen) {
		type.getAnnotations().forEach(it -> processAnnotation(it, imageContext, seen));
	}

	private void processAnnotation(Type annotation, NativeContext imageContext, Set<Type> seen) {

		if (annotation.isPartOfDomain("java.lang.annotation") || seen.contains(annotation) || !imageContext.getTypeSystem().canResolve(annotation.getName())) {
			return;
		}

		// cycle guard
		seen.add(annotation);

		annotationRegistrar.accept(annotation, imageContext);

		// TODO: do we need to check methods on annotations?

		// meta annotations
		processAnnotationsOnType(annotation, imageContext, seen);
	}
}
