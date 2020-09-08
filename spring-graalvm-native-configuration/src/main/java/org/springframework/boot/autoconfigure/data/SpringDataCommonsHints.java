/*
 * Copyright 2020 the original author or authors.
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
package org.springframework.boot.autoconfigure.data;

import java.util.Properties;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.io.InputStreamSource;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.core.support.PropertiesBasedNamedQueries;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFragmentsFactoryBean;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.ProxyInfo;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.AccessBits;

@NativeImageHint(trigger = AbstractRepositoryConfigurationSourceSupport.class, //
		typeInfos = {
				@TypeInfo(types = {
						RepositoryFactoryBeanSupport.class,
						RepositoryFragmentsFactoryBean.class,
						TransactionalRepositoryFactoryBeanSupport.class,
						QueryByExampleExecutor.class,
						MappingContext.class,
						PropertiesBasedNamedQueries.class,
				}),
				@TypeInfo(types = {Properties.class, BeanFactory.class, InputStreamSource[].class}, access = AccessBits.CLASS),
				@TypeInfo(types = Throwable.class, access = AccessBits.LOAD_AND_CONSTRUCT | AccessBits.DECLARED_FIELDS)
		},
		proxyInfos = {
				@ProxyInfo(
						typeNames = {"org.springframework.data.annotation.QueryAnnotation", "org.springframework.core.annotation.SynthesizedAnnotation" }
				)
		}
)
public class SpringDataCommonsHints implements NativeImageConfiguration {

}
