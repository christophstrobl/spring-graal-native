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
package org.springframework.boot.autoconfigure.data.rest;

import org.springframework.boot.autoconfigure.web.servlet.WebMvcHints;
import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.AccessBits;

/**
 * @author Christoph Strobl
 */
@NativeImageHint(trigger = RepositoryRestMvcAutoConfiguration.class, typeInfos = {
		@TypeInfo(types = {
				org.reactivestreams.Publisher.class,

				org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver.class,
				org.springframework.web.servlet.theme.FixedThemeResolver.class,
				org.springframework.web.servlet.view.DefaultRequestToViewNameTranslator.class,
				org.springframework.web.servlet.support.SessionFlashMapManager.class,

				org.springframework.data.repository.core.support.RepositoryFactoryInformation.class,
				org.springframework.data.repository.support.Repositories.class,
				org.springframework.data.repository.support.RepositoryInvoker.class,
				org.springframework.data.repository.support.RepositoryInvokerFactory.class,
		},
				typeNames = {
						"org.springframework.data.rest.webmvc.config.WebMvcRepositoryRestConfiguration",

						"org.springframework.hateoas.EntityModel",
						"org.springframework.hateoas.CollectionModel",
						"org.springframework.hateoas.AffordanceModel"
				},

				access = AccessBits.DECLARED_FIELDS | AccessBits.DECLARED_METHODS | AccessBits.DECLARED_CONSTRUCTORS | AccessBits.RESOURCE)
}, importInfos = WebMvcHints.class)
public class DataRestHints implements NativeImageConfiguration {

}
