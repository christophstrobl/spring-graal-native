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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.jpa.entities.Order;
import org.springframework.nativex.util.NativeTestContext;
import org.springframework.nativex.util.TestTypeSystem;

/**
 * @author Christoph Strobl
 */
public class DomainTypeProcessorTests {

	static TestTypeSystem typeSystem = new TestTypeSystem();

	private NativeTestContext nativeContext;
	private DomainTypeProcessor processor;

	private List<String> capturedTypes;
	private List<String> capturedAnnotations;


	@Before
	public void setUp() {

		typeSystem.clearExclusions();
		nativeContext = new NativeTestContext(typeSystem);

		capturedTypes = new ArrayList<>();
		capturedAnnotations = new ArrayList<>();

		processor = new DomainTypeProcessor(
				(type, context) -> true, // do not filter out types
				(type, context) -> capturedTypes.add(type.getDottedName()), // types we discovered
				(type, context) -> capturedAnnotations.add(type.getDottedName()) // annotations we discovered
		);
	}

	@Test
	public void detectsUsedTypesAndAnnotations() {

		processor.process(typeSystem.resolve(Order.class), nativeContext);

		assertThat(capturedTypes).containsExactlyInAnyOrder("org.springframework.data.jpa.entities.Order", "java.lang.Long", "java.lang.String", "java.util.List", "org.springframework.data.jpa.entities.LineItem", "java.util.Date");
		assertThat(capturedAnnotations).contains("javax.persistence.Entity", "javax.persistence.ManyToOne", "javax.persistence.OneToMany", "javax.persistence.GeneratedValue", "org.springframework.data.jpa.entities.SomeAnnotation", "javax.annotation.Nullable", "org.springframework.beans.factory.annotation.Value");
	}

	@Test
	public void ignoresAnnotatiosNotReachable() {

		typeSystem.excludePackages("javax.annotation"); //  @Nullable should not be there

		processor.process(typeSystem.resolve(Order.class), nativeContext);

		assertThat(capturedTypes).containsExactlyInAnyOrder("org.springframework.data.jpa.entities.Order", "java.lang.Long", "java.lang.String", "java.util.List", "org.springframework.data.jpa.entities.LineItem", "java.util.Date");
		assertThat(capturedAnnotations).contains("org.springframework.data.jpa.entities.SomeAnnotation").doesNotContain("javax.annotation.Nullable");
	}
}
