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
package org.springframework.boot.autoconfigure.data.r2dbc;

import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.r2dbc.dialect.DialectResolver.R2dbcDialectProvider;
import org.springframework.data.r2dbc.repository.config.R2dbcRepositoryConfigurationExtension;
import org.springframework.data.r2dbc.repository.support.R2dbcRepositoryFactoryBean;
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.PropertiesBasedNamedQueries;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFragmentsFactoryBean;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.AccessBits;

@NativeImageHint(trigger = R2dbcRepositoriesAutoConfiguration.class, typeInfos = {
		@TypeInfo(types = {
				R2dbcRepositoryFactoryBean.class, RepositoryFactoryBeanSupport.class, R2dbcRepositoryConfigurationExtension.class,
				MappingContext.class, PropertiesBasedNamedQueries.class, RepositoryFragmentsFactoryBean.class,
				R2dbcRepositoriesAutoConfigureRegistrar.class,
				SimpleR2dbcRepository.class,
				RepositoryMetadata.class,
				R2dbcDialectProvider.class
		}, typeNames = {"org.springframework.data.r2dbc.dialect.DialectResolver.R2dbcDialectProvider.BuiltInDialectProvider"}
				, access = AccessBits.DECLARED_FIELDS | AccessBits.DECLARED_METHODS | AccessBits.DECLARED_CONSTRUCTORS | AccessBits.RESOURCE
		)
})
public class Hints implements NativeImageConfiguration {

}
