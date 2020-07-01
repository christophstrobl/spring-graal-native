package app.main;

import static org.springframework.web.servlet.function.RequestPredicates.*;
import static org.springframework.web.servlet.function.RouterFunctions.*;
import static org.springframework.web.servlet.function.ServerResponse.*;

import app.main.model.Address;
import app.main.model.Customer;
import app.main.model.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.function.RouterFunction;

@SpringBootApplication(proxyBeanMethods = false, exclude = SpringDataWebAutoConfiguration.class)
public class SampleApplication {

	private CustomerRepository repository;

	public SampleApplication(CustomerRepository repository) {
		this.repository = repository;
	}

	@Bean
	public CommandLineRunner runner() {
		return args -> {

			Customer dave = repository.save(new Customer("Dave", "Matthews",
					new Address("4711 Some Place", "54321", "Charlottesville", "VA")));
		};
	}

	@Bean
	public RouterFunction<?> userEndpoints() {
		return route(GET("/api/customers/1"), request -> ok().body(findOne()));
	}

	private Customer findOne() {
		return repository.findById(1L).get();
	}

//	@EnableHypermediaSupport(
//			type = {HypermediaType.HAL}
//	)
//	@ComponentScan(
//			basePackageClasses = {RepositoryRestController.class},
//			includeFilters = {@Filter({BasePathAwareController.class})},
//			useDefaultFilters = false
//	)
//	@ImportResource({"classpath*:META-INF/spring-data-rest/**/*.xml"})
//	@Import({SpringDataJacksonConfiguration.class, QuerydslActivator.class})
//	@Configuration(proxyBeanMethods = false)
//	static class X extends RepositoryRestMvcConfiguration {
//
//		public X(ApplicationContext context, ObjectFactory<ConversionService> conversionService) {
//			super(context, conversionService);
//		}
//	}

	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
	}
}
