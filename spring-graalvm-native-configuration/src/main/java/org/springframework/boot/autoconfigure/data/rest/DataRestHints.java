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

import org.springframework.graalvm.extension.NativeImageConfiguration;
import org.springframework.graalvm.extension.NativeImageHint;
import org.springframework.graalvm.extension.TypeInfo;
import org.springframework.graalvm.type.AccessBits;
import org.springframework.hateoas.mediatype.hal.HalMediaTypeConfiguration;
import reactor.core.publisher.Flux;

/**
 * @author Christoph Strobl
 */
@NativeImageHint(trigger = RepositoryRestMvcAutoConfiguration.class, typeInfos = {
		@TypeInfo(types = {
				Flux.class,
				org.reactivestreams.Publisher.class,
				HalMediaTypeConfiguration.class,
				org.springframework.stereotype.Controller.class,
				org.springframework.data.repository.core.support.RepositoryFactoryInformation.class,
				org.springframework.data.repository.support.Repositories.class,
				org.springframework.data.repository.support.RepositoryInvoker.class,
				org.springframework.data.repository.support.RepositoryInvokerFactory.class,
		},
				typeNames = {

//						"org.springframework.plugin.core.OrderAwarePluginRegistry",
//						"org.springframework.plugin.core.Plugin",
//						"org.springframework.plugin.core.PluginRegistry",
//						"org.springframework.plugin.core.PluginRegistrySupport",
//						"org.springframework.plugin.core.SimplePluginRegistry",
//						"org.springframework.plugin.core.config.EnablePluginRegistries",
//						"org.springframework.plugin.core.config.PluginRegistriesBeanDefinitionRegistrar",
//						"org.springframework.plugin.core.support.AbstractTypeAwareSupport",
//						"org.springframework.plugin.core.support.PluginRegistryFactoryBean",

						"org.springframework.hateoas.EntityModel",
						"org.springframework.hateoas.CollectionModel",
						"org.springframework.hateoas.AffordanceModel",

						"org.springframework.data.rest.webmvc.RepositoryRestController",
						"org.springframework.data.rest.webmvc.BasePathAwareController",
						"org.springframework.data.rest.core.annotation.Description",
						"org.springframework.data.rest.core.config.EnumTranslationConfiguration",

//						"org.springframework.data.rest.core.config.MetadataConfiguration",
//						"org.springframework.data.rest.core.config.Projection",
//						"org.springframework.data.rest.core.config.RepositoryRestConfiguration",
//						"org.springframework.data.rest.core.event.AbstractRepositoryEventListener",
//						"org.springframework.data.rest.core.event.AnnotatedEventHandlerInvoker",
//						"org.springframework.data.rest.core.event.ValidatingRepositoryEventListener",
//						"org.springframework.data.rest.core.mapping.PersistentEntitiesResourceMappings",
//						"org.springframework.data.rest.core.annotation.RepositoryRestResource",
//						"org.springframework.data.rest.core.mapping.RepositoryResourceMappings",
//						"org.springframework.data.rest.core.mapping.ResourceMapping",
//						"org.springframework.data.rest.core.mapping.ResourceMappings",
//						"org.springframework.data.rest.core.support.RepositoryRelProvider",
//						"org.springframework.data.rest.core.support.DefaultSelfLinkProvider",
//						"org.springframework.data.rest.core.support.SelfLinkProvider",
//						"org.springframework.data.rest.core.support.UnwrappingRepositoryInvokerFactory",
//						"org.springframework.data.rest.webmvc.AbstractRepositoryRestController",
//						"org.springframework.data.rest.webmvc.BasePathAwareController",
//						"org.springframework.data.rest.webmvc.BaseUri",
//						"org.springframework.data.rest.webmvc.HttpHeadersPreparer",
//						"org.springframework.data.rest.webmvc.PersistentEntityResource",
//						"org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler",
//						"org.springframework.data.rest.webmvc.ProfileController",
//						"org.springframework.data.rest.webmvc.ProfileResourceProcessor",
//						"org.springframework.data.rest.webmvc.RepositoryController",
//						"org.springframework.data.rest.webmvc.RepositoryEntityController",
//						"org.springframework.data.rest.webmvc.RepositoryPropertyReferenceController",
//						"org.springframework.data.rest.webmvc.RepositoryPropertyReferenceController$HttpRequestMethodNotSupportedException",
//						"org.springframework.data.rest.webmvc.RepositoryRestController",
//						"org.springframework.data.rest.webmvc.RepositoryRestExceptionHandler",
//						"org.springframework.data.rest.webmvc.RepositoryRestHandlerAdapter",
//						"org.springframework.data.rest.webmvc.RepositorySchemaController",
//						"org.springframework.data.rest.webmvc.RepositorySearchController",
//						"org.springframework.data.rest.webmvc.RootResourceInformation",
//						"org.springframework.data.rest.webmvc.ServerHttpRequestMethodArgumentResolver",
//						"org.springframework.data.rest.webmvc.alps.AlpsController",
//						"org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer",
//						"org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration",
//						"org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration$ResourceSupportHttpMessageConverter",
//						"org.springframework.data.rest.webmvc.config.ResourceMetadataHandlerMethodArgumentResolver",
//						"org.springframework.data.rest.webmvc.config.RootResourceInformationHandlerMethodArgumentResolver",
//						"org.springframework.data.rest.webmvc.convert.UriListHttpMessageConverter",
//
//
//						"org.springframework.web.accept.ContentNegotiationManager",
//						"org.springframework.web.accept.ContentNegotiationStrategy",
//						"org.springframework.web.accept.MediaTypeFileExtensionResolver",
//
//						"org.springframework.web.bind.annotation.ControllerAdvice",
//						"org.springframework.web.bind.annotation.ExceptionHandler",
//						"org.springframework.web.bind.annotation.Mapping",
//						"org.springframework.web.bind.annotation.RequestBody",
//						"org.springframework.web.bind.annotation.RequestMapping",
//						"org.springframework.web.bind.annotation.RequestMethod",
//						"org.springframework.web.bind.annotation.RequestMethod[]",
//						"org.springframework.web.bind.annotation.ResponseBody",
//						"org.springframework.web.bind.annotation.ResponseStatus",
//
//						"org.springframework.web.client.RestTemplate",
//
//						"org.springframework.web.context.ConfigurableWebApplicationContext",
//						"org.springframework.web.context.ServletContextAware",
//						"org.springframework.web.context.request.AsyncWebRequestInterceptor",
//						"org.springframework.web.context.request.RequestContextListener",
//						"org.springframework.web.context.request.WebRequestInterceptor",
//
//						"org.springframework.web.context.support.GenericWebApplicationContext",
//						"org.springframework.web.context.support.WebApplicationObjectSupport",
//
//						"org.springframework.web.filter.CharacterEncodingFilter",
//						"org.springframework.web.filter.FormContentFilter",
//						"org.springframework.web.filter.GenericFilterBean",
//						"org.springframework.web.filter.OncePerRequestFilter",
//						"org.springframework.web.filter.RequestContextFilter",
//
//						"org.springframework.web.method.support.CompositeUriComponentsContributor",
//						"org.springframework.web.method.support.HandlerMethodArgumentResolver",
//						"org.springframework.web.method.support.UriComponentsContributor",
//
//						"org.springframework.web.multipart.MultipartResolver",
//						"org.springframework.web.multipart.commons.CommonsMultipartResolver",
//						"org.springframework.web.multipart.support.StandardServletMultipartResolver",
//
//						"org.springframework.web.servlet.DispatcherServlet",
//						"org.springframework.web.servlet.FrameworkServlet",
//						"org.springframework.web.servlet.HandlerAdapter",
//						"org.springframework.web.servlet.HandlerExceptionResolver",
//						"org.springframework.web.servlet.HandlerMapping",
//						"org.springframework.web.servlet.HttpServletBean",
//
//						"org.springframework.web.servlet.View",
//						"org.springframework.web.servlet.ViewResolver",
//						"org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer",
//						"org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer",
//						"org.springframework.web.servlet.config.annotation.CorsRegistry",
//						"org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer",
//						"org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration",
//						"org.springframework.web.servlet.config.annotation.InterceptorRegistry",
//						"org.springframework.web.servlet.config.annotation.PathMatchConfigurer",
//						"org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry",
//						"org.springframework.web.servlet.config.annotation.ViewControllerRegistry",
//						"org.springframework.web.servlet.config.annotation.ViewResolverRegistry",
//						"org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport",
//						"org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport$NoOpValidator",
//						"org.springframework.web.servlet.config.annotation.WebMvcConfigurer",
//
//						"org.springframework.web.servlet.function.support.HandlerFunctionAdapter",
//						"org.springframework.web.servlet.function.support.RouterFunctionMapping",
//
//						"org.springframework.web.servlet.handler.AbstractDetectingUrlHandlerMapping",
//						"org.springframework.web.servlet.handler.AbstractHandlerMapping",
//						"org.springframework.web.servlet.handler.AbstractHandlerMethodMapping",
//						"org.springframework.web.servlet.handler.AbstractHandlerMethodMapping$EmptyHandler",
//						"org.springframework.web.servlet.handler.AbstractUrlHandlerMapping",
//						"org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping",
//						"org.springframework.web.servlet.handler.HandlerExceptionResolverComposite",
//						"org.springframework.web.servlet.handler.HandlerMappingIntrospector",
//						"org.springframework.web.servlet.handler.MatchableHandlerMapping",
//						"org.springframework.web.servlet.handler.SimpleUrlHandlerMapping",
//						"org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter",
//						"org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter",
//						"org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter",
//						"org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping",
//						"org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping$HttpOptionsHandler",
//						"org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter",
//						"org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping",
//						"org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice",
//
//						"org.springframework.web.servlet.resource.ResourceUrlProvider",
//						"org.springframework.web.servlet.support.WebContentGenerator",
//
//						"org.springframework.web.servlet.view.AbstractCachingViewResolver",
//						"org.springframework.web.servlet.view.BeanNameViewResolver",
//						"org.springframework.web.servlet.view.ContentNegotiatingViewResolver",
//						"org.springframework.web.servlet.view.InternalResourceViewResolver",
//						"org.springframework.web.servlet.view.UrlBasedViewResolver",
//						"org.springframework.web.servlet.view.ViewResolverComposite",
//						"org.springframework.web.util.UrlPathHelper",
//
//						"org.springframework.web.servlet.view.InternalResourceView"
				},

				access = AccessBits.DECLARED_FIELDS | AccessBits.DECLARED_METHODS | AccessBits.DECLARED_CONSTRUCTORS | AccessBits.RESOURCE)
})
public class DataRestHints implements NativeImageConfiguration {

}
