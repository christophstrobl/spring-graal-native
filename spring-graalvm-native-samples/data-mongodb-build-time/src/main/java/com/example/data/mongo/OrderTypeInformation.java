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
package com.example.data.mongo;

import java.lang.annotation.Annotation;
import java.util.Date;
import java.util.List;

import org.springframework.data.mapping.model.DomainTypeConstructor;
import org.springframework.data.mapping.model.DomainTypeInformation;
import org.springframework.data.mapping.model.Field;
import org.springframework.data.mapping.model.ListTypeInformation;
import org.springframework.data.mongodb.core.mapping.FieldType;

/**
 * @author Christoph Strobl
 * @since 2020/10
 */
public class OrderTypeInformation extends DomainTypeInformation<Order> {

	private static final OrderTypeInformation INSTANCE = new OrderTypeInformation();

	private OrderTypeInformation() {

		super(Order.class);

		setConstructor(DomainTypeConstructor.<Order>builder()
				.args("id", "customerId", "orderDate", "items")
				.newInstanceFunction(args -> new Order((String) args[0], (String) args[1], (Date) args[2], (List) args[3])));

		addField(Field.<Order>string("id")
				.setter(Order::setId)
				.getter(Order::getId));

		addField(Field.<Order>string("customerId")
				.setter(Order::setCustomerId)
				.getter(Order::getCustomerId));

		addField(new Field<Date, Order>("orderDate", new DomainTypeInformation(Date.class))
				.setter(Order::setOrderDate)
				.getter(Order::getOrderDate)
				.annotation(atField("order-date")));

		addField(new Field<List<LineItem>, Order>("items", ListTypeInformation.listOf(LineItemTypeInformation.instance()))
				.setter(Order::setItems)
				.getter(Order::getItems));
	}

	public static OrderTypeInformation instance() {
		return INSTANCE;
	}

	org.springframework.data.mongodb.core.mapping.Field atField(String fieldname) {
		return new org.springframework.data.mongodb.core.mapping.Field() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return org.springframework.data.mongodb.core.mapping.Field.class;
			}

			@Override
			public String value() {
				return fieldname;
			}

			@Override
			public String name() {
				return value();
			}

			@Override
			public int order() {
				return 0;
			}

			@Override
			public FieldType targetType() {
				return FieldType.IMPLICIT;
			}
		};
	}
}
