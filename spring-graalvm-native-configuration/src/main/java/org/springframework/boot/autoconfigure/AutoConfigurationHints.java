/*
 * Copyright 2020 the original author or authors.
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
package org.springframework.boot.autoconfigure;

import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.AccessBits;


@NativeImageHint(trigger=ImportAutoConfigurationImportSelector.class, typeInfos={
		@TypeInfo(types= { 
				ImportAutoConfiguration.class }, typeNames = {
				"org.springframework.boot.autoconfigure.test.ImportAutoConfiguration" 
			})},applyToFunctional=false
)
@NativeImageHint(trigger=AutoConfigurationImportSelector.class, applyToFunctional=false)
@NativeImageHint(typeInfos = {
	@TypeInfo(types = { AutoConfigureBefore.class, AutoConfigureAfter.class, AutoConfigureOrder.class, AutoConfigurationPackage.class },
			  access = AccessBits.CLASS | AccessBits.DECLARED_METHODS) },applyToFunctional=false)
// TODO why isn't this one pulled in via @EnableAutoConfiguration handling?
@NativeImageHint(typeInfos = { 
	@TypeInfo(types = { 
		AutoConfigurationImportSelector.class,
		AutoConfigurationPackages.class, AutoConfigurationPackages.Registrar.class,
		AutoConfigurationPackages.BasePackages.class,
		EnableAutoConfiguration.class,SpringBootApplication.class
	},access=AccessBits.LOAD_AND_CONSTRUCT|AccessBits.PUBLIC_METHODS)
},applyToFunctional=false)
@NativeImageHint(typeInfos = { 
	@TypeInfo(typeNames = {
		"org.springframework.boot.autoconfigure.AutoConfigurationImportSelector$AutoConfigurationGroup" 
	},access=AccessBits.CLASS|AccessBits.DECLARED_CONSTRUCTORS)
},applyToFunctional=false)
public class AutoConfigurationHints implements NativeImageConfiguration {
}