/*
 * Copyright 2012-2018 the original author or authors.
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

package com.example.data.mongo;

import java.util.Date;
import java.util.List;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings.Builder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;

//@SpringBootApplication(proxyBeanMethods = false)
public class SDMongoApplication {

	private final static LineItem product1 = new LineItem("p1", 1.23);
	private final static LineItem product2 = new LineItem("p2", 0.87, 2);
	private final static LineItem product3 = new LineItem("p3", 5.33);

	public static void main(String[] args) throws Exception {



//		ConfigurableApplicationContext ctx = SpringApplication.run(SDMongoApplication.class, args);

		ConfigurableApplicationContext ctx = new AnnotationConfigApplicationContext(Config.class);
//		ctx.refresh();

		MongoTemplate template = ctx.getBean("mongoTemplate", MongoTemplate.class);

		MongoRepositoryFactory factory = new MongoRepositoryFactory(template);
		factory.getRepository(OrderRepository.class);

//		OrderRepository repository = ctx.getBean(OrderRepository.class);

		OrderRepository repository = factory.getRepository(OrderRepository.class);


		{
			repository.deleteAll();

			Order order = new Order("c42", new Date()).//
					addItem(product1).addItem(product2).addItem(product3);
			order = repository.save(order);

//			Invoice invoice = repository.getInvoiceFor(order);
//			System.out.println("invoice: " + invoice);

//			assertThat(invoice).isNotNull();
//			assertThat(invoice.getOrderId()).isEqualTo(order.getId());
//			assertThat(invoice.getNetAmount()).isCloseTo(8.3D, offset(0.00001));
//			assertThat(invoice.getTaxAmount()).isCloseTo(1.577D, offset(0.00001));
//			assertThat(invoice.getTotalAmount()).isCloseTo(9.877, offset(0.00001));

		}

		{
			repository.deleteAll();

			repository.save(new Order("c42", new Date()).addItem(product1));
			repository.save(new Order("c42", new Date()).addItem(product2));
			repository.save(new Order("c42", new Date()).addItem(product3));

			repository.save(new Order("b12", new Date()).addItem(product1));
			repository.save(new Order("b12", new Date()).addItem(product1));

			List<OrdersPerCustomer> result = repository.totalOrdersPerCustomer(Sort.by(Sort.Order.desc("total")));
			System.out.println("result: " + result);

//			assertThat(result).containsExactly(new OrdersPerCustomer("c42", 3L), new OrdersPerCustomer("b12", 2L));
		}
	}

	@Configuration(proxyBeanMethods = false)
	static class Config extends AbstractMongoClientConfiguration {

		@Override
		protected String getDatabaseName() {
			return "test";
		}

		@Override
		protected void configureClientSettings(Builder builder) {
			builder.applyConnectionString(new ConnectionString("mongodb://host.docker.internal:27017"));
		}
	}
}
