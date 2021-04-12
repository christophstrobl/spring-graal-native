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

import static org.assertj.core.api.Assertions.*;

import javax.persistence.Entity;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.jpa.entities.NotAnEntity;
import org.springframework.data.jpa.entities.Order;
import org.springframework.nativex.domain.reflect.FieldDescriptor;
import org.springframework.nativex.util.NativeTestContext;
import org.springframework.nativex.util.TestTypeSystem;

/**
 * @author Christoph Strobl
 * @since 2021/04
 */
public class JpaComponentProcessorTests {

	static TestTypeSystem typeSystem = new TestTypeSystem();

	private NativeTestContext nativeContext;
	private JpaComponentProcessor processor;

	@Before
	public void setUp() {

		typeSystem.clearExclusions();
		nativeContext = new NativeTestContext(typeSystem);

		processor = new JpaComponentProcessor();
	}


	@Test
	public void shouldOnlyHandleJavaxPersistenceEntity() {

		// via detected classifier
		assertThat(processor.handle(nativeContext, typeSystem.resolve(NotAnEntity.class).getDottedName(), Collections.singletonList(typeSystem.resolve(Entity.class).getDottedName()))).isTrue();

		// @EntityOnType
		assertThat(processor.handle(nativeContext, typeSystem.resolve(Order.class).getDottedName(), Collections.emptyList())).isTrue();

		// not at all
		assertThat(processor.handle(nativeContext, typeSystem.resolve(NotAnEntity.class).getDottedName(), Collections.emptyList())).isFalse();
	}

	@Test
	public void shouldRegisterReflectionAllowWriteForFinalFields() {

		processor.process(nativeContext, typeSystem.resolve(Order.class).getDottedName(), Collections.emptyList());

		assertThat(nativeContext.getReflectionEntry(Order.class).getFieldDescriptors())
				.hasSize(1)
				.satisfies(it -> {

					FieldDescriptor fieldDescriptor = it.iterator().next();
					assertThat(fieldDescriptor.getName()).isEqualTo("id");
					assertThat(fieldDescriptor.isAllowWrite()).isTrue();
				});
	}
}
