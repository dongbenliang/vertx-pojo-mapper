/*-
 * #%L
 * vertx-pojo-mapper-common
 * %%
 * Copyright (C) 2017 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package examples.mapper;

import java.util.List;

import de.braintags.vertx.jomnigate.annotation.Entity;
import de.braintags.vertx.jomnigate.annotation.field.Embedded;
import de.braintags.vertx.jomnigate.annotation.field.Id;
import io.vertx.docgen.Source;

@Source(translate = false)
@Entity
public class PersonEmbedList {
  @Id
  public String id;
  public String name;
  @Embedded
  public List<Animal> animals;

  public PersonEmbedList() {
  }

}
