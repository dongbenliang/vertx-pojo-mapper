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

package de.braintags.io.vertx.pojomapper.json.typehandler.handler;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import de.braintags.io.vertx.pojomapper.exception.ClassAccessException;
import de.braintags.io.vertx.pojomapper.mapping.IField;
import de.braintags.io.vertx.pojomapper.typehandler.AbstractTypeHandler;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */

public class CharSequenceTypeHandler extends AbstractTypeHandler {

  /**
   * @param classesToDeal
   */
  public CharSequenceTypeHandler() {
    super(CharSequence.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.pojomapper.typehandler.ITypeHandler#fromStore(java.lang.Object,
   * de.braintags.io.vertx.pojomapper.mapping.IField)
   */
  @Override
  public Object fromStore(Object source, IField field, Class<?> cls) {
    if (source == null)
      return null;
    Constructor<?> constr = getConstructor(field, cls, String.class);
    try {
      return constr.newInstance(source);
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new ClassAccessException("", e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.pojomapper.typehandler.ITypeHandler#intoStore(java.lang.Object,
   * de.braintags.io.vertx.pojomapper.mapping.IField)
   */
  @Override
  public Object intoStore(Object source, IField field) {
    return source == null ? source : ((CharSequence) source).toString();
  }

}