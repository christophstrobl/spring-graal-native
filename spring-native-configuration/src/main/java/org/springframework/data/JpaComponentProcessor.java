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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.nativex.domain.reflect.FieldDescriptor;
import org.springframework.nativex.hint.AccessBits;
import org.springframework.nativex.type.AccessDescriptor;
import org.springframework.nativex.type.ComponentProcessor;
import org.springframework.nativex.type.Field;
import org.springframework.nativex.type.NativeContext;
import org.springframework.nativex.type.Type;

/**
 * @author Christoph Strobl
 */
public class JpaComponentProcessor implements ComponentProcessor {

	private final DomainTypeProcessor domainTypeProcessor = new DomainTypeProcessor(

			(type, nativeContext) -> {
//				if (nativeContext.hasReflectionConfigFor(type)) {
//					return false;
//				}
				return true;
			},
			this::registerTypeInConfiguration,
			this::registerAnnotationInConfiguration
	);


	@Override
	public boolean handle(NativeContext imageContext, String componentType, List<String> classifiers) {

		if (classifiers.contains("javax.persistence.Entity")) {
			return true;
		}

		Type type = imageContext.getTypeSystem().resolveName(componentType);

		return type.getAnnotations() //
				.stream() //
				.anyMatch(it -> it.getDottedName().contains("javax.persistence"));
	}

	@Override
	public void process(NativeContext imageContext, String componentType, List<String> classifiers) {

		Type domainType = imageContext.getTypeSystem().resolveName(componentType);
		domainTypeProcessor.process(domainType, imageContext);
	}

	private void registerAnnotationInConfiguration(Type annotation, NativeContext context) {
		context.addReflectiveAccess(annotation.getDottedName(), AccessBits.ANNOTATION);
	}

	private void registerTypeInConfiguration(Type type, NativeContext context) {

		AccessDescriptor accessDescriptor = new AccessDescriptor(AccessBits.FULL_REFLECTION, Collections.emptyList(), fieldDescriptorsForType(type));
		context.addReflectiveAccess(type.getDottedName(), accessDescriptor);
	}

	private List<FieldDescriptor> fieldDescriptorsForType(Type type) {

		if (type.isPartOfDomain("java.")) { // other well known domains ?
			return Collections.emptyList();
		}

		return type.getFields()
				.stream()
				.filter(Field::isFinal)
				.map(field -> new FieldDescriptor(field.getName(), true, true))
				.collect(Collectors.toList());
	}
}
