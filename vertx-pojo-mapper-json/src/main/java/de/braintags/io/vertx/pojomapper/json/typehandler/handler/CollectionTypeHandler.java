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

import io.vertx.core.json.JsonArray;

import java.util.Collection;
import java.util.Iterator;

import de.braintags.io.vertx.pojomapper.mapping.IField;
import de.braintags.io.vertx.pojomapper.mapping.impl.Mapper;
import de.braintags.io.vertx.pojomapper.typehandler.AbstractTypeHandler;
import de.braintags.io.vertx.pojomapper.typehandler.ITypeHandler;
import de.braintags.io.vertx.pojomapper.typehandler.ITypeHandlerResult;
import de.braintags.io.vertx.pojomapper.typehandler.impl.DefaultTypeHandlerResult;

/**
 * Deals all fields, which contain {@link Collection} content, in spite of maps
 * 
 * @author Michael Remme
 * 
 */

public class CollectionTypeHandler extends AbstractTypeHandler {

  /**
   * @param classesToDeal
   */
  public CollectionTypeHandler() {
    super(Collection.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.pojomapper.typehandler.ITypeHandler#fromStore(java.lang.Object,
   * de.braintags.io.vertx.pojomapper.mapping.IField)
   */
  @SuppressWarnings("unchecked")
  @Override
  public void fromStore(Object source, IField field, Class<?> cls, ITypeHandlerResult typeHandlerResult) {
    if (source != null) {
      @SuppressWarnings("rawtypes")
      Collection coll = field.getMapper().getObjectFactory().createCollection(field);
      Iterator<?> ji = ((JsonArray) source).iterator();
      ITypeHandler subHandler = field.getSubTypeHandler();
      while (ji.hasNext()) {
        Object o = ji.next();
        if (subHandler != null) {
          DefaultTypeHandlerResult tmpResult = new DefaultTypeHandlerResult();
          subHandler.fromStore(o, null, field.getSubClass(), tmpResult);
          tmpResult.validate();
          Object dest = tmpResult.getResult();
          coll.add(dest);
        } else {
          coll.add(o);
        }
      }
      typeHandlerResult.setResult(coll);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.pojomapper.typehandler.ITypeHandler#intoStore(java.lang.Object,
   * de.braintags.io.vertx.pojomapper.mapping.IField)
   */
  @Override
  public void intoStore(Object source, IField field, ITypeHandlerResult typeHandlerResult) {
    JsonArray jsonArray = null;
    if (source != null) {
      jsonArray = new JsonArray();
      if (!((Collection<?>) source).isEmpty()) {
        Iterator<?> sourceIt = ((Collection<?>) source).iterator();
        ITypeHandler subHandler = field.getSubTypeHandler();
        boolean determineSubhandler = subHandler == null;
        Class<?> valueClass = null;
        while (sourceIt.hasNext()) {
          Object value = sourceIt.next();
          if (determineSubhandler) {
            boolean valueClassChanged = valueClass != null && value.getClass() != valueClass;
            valueClass = value.getClass();
            if (subHandler == null || valueClassChanged) {
              subHandler = ((Mapper) field.getMapper()).getMapperFactory().getDataStore().getTypeHandlerFactory()
                  .getTypeHandler(value.getClass());
              // TODO could it be useful to write the class of the value into the field, to restore it proper from
              // datastore?
            }
          }
          DefaultTypeHandlerResult tmpResult = new DefaultTypeHandlerResult();
          subHandler.intoStore(value, null, tmpResult);
          tmpResult.validate();
          jsonArray.add(tmpResult.getResult());
        }

      }
    }

    typeHandlerResult.setResult(jsonArray);
  }
}
