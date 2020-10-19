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

import org.springframework.data.mapping.model.DomainTypeConstructor;
import org.springframework.data.mapping.model.DomainTypeInformation;
import org.springframework.data.mapping.model.Field;

/**
 * @author Christoph Strobl
 * @since 2020/10
 */
public class LineItemTypeInformation extends DomainTypeInformation<LineItem> {

	private static final LineItemTypeInformation INSTANCE = new LineItemTypeInformation();

	private LineItemTypeInformation() {

		super(LineItem.class);

		setConstructor(DomainTypeConstructor.<LineItem>builder()
				.args("caption", "price", "quantity")
				.newInstanceFunction((args) -> {

					System.out.println("new LineItem via function ctor invocation");
					return new LineItem((String) args[0], (Double) args[1], (Integer) args[2]);
				}));

		addField(Field.<LineItem>string("caption").getter(LineItem::getCaption));
		addField(new Field<Double, LineItem>("price", new DomainTypeInformation(Double.class)).getter(LineItem::getPrice));
		addField(Field.<LineItem>int32("quantity").getter(LineItem::getQuantity));
	}

	public static LineItemTypeInformation instance() {
		return INSTANCE;
	}
}
