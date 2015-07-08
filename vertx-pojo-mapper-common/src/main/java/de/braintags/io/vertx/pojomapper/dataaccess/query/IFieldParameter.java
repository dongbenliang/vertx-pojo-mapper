/*
 * Copyright 2014 Red Hat, Inc.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * 
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 * 
 * You may elect to redistribute this code under either of these licenses.
 */

package de.braintags.io.vertx.pojomapper.dataaccess.query;

/**
 * Defines the logic of the field arguments of a query
 * 
 * @author Michael Remme
 * 
 */

public interface IFieldParameter<T extends IQueryContainer> {

  /**
   * Defines a query argument which fits exact the given argumentT
   * 
   * @param value
   *          the value to search for
   * @return the parent {@link IQueryContainer} to enable chaining of commands
   */
  T is(Object value);

  T contains(Object value);

}
