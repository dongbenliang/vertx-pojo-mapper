/*
 * #%L
 * vertx-pojo-mapper-json
 * %%
 * Copyright (C) 2017 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package de.braintags.vertx.jomnigate.json.typehandler;

import java.lang.annotation.Annotation;

import de.braintags.vertx.jomnigate.annotation.field.Embedded;
import de.braintags.vertx.jomnigate.json.typehandler.handler.ArrayTypeHandlerReferenced;
import de.braintags.vertx.jomnigate.json.typehandler.handler.CollectionTypeHandlerReferenced;
import de.braintags.vertx.jomnigate.json.typehandler.handler.MapTypeHandlerReferenced;
import de.braintags.vertx.jomnigate.json.typehandler.handler.ObjectTypeHandler;
import de.braintags.vertx.jomnigate.json.typehandler.handler.ObjectTypeHandlerReferenced;
import de.braintags.vertx.jomnigate.typehandler.AbstractTypeHandlerFactory;
import de.braintags.vertx.jomnigate.typehandler.ITypeHandler;

/**
 * Creates {@link ITypeHandler} which are creating Json-usable formats from Objects and back
 *
 * @author Michael Remme
 *
 */

public class JsonTypeHandlerFactory extends AbstractTypeHandlerFactory {
  private final ITypeHandler defaultHandler = new ObjectTypeHandler(this);
  private final ITypeHandler defaultHandleEmbedded = new ObjectTypeHandler(this);
  private final ITypeHandler defaultHandlerReferenced = new ObjectTypeHandlerReferenced(this);

  /**
   * Constructor calls the init method
   */
  public JsonTypeHandlerFactory() {
    init();
  }

  /**
   * Initializes the {@link ITypeHandler} which are belonging to the current instance
   */
  protected void init() {
    // getDefinedTypeHandlers().add(0, new IdTypeHandler(this));

    // add(new FloatTypeHandler(this));
    // add(new DateTypeHandler(this));
    // add(new ShortTypeHandler(this));
    // add(new IntegerTypeHandler(this));
    // add(new LongTypeHandler(this));
    // add(new CalendarTypeHandler(this));
    // add(new PriceTypeHandler(this));
    // add(new BigDecimalTypeHandler(this));
    // add(new BigIntegerTypeHandler(this));
    // add(new CharSequenceTypeHandler(this));
    // add(new CharacterTypeHandler(this));
    // add(new ByteTypeHandler(this));
    // add(new URITypeHandler(this));
    // add(new URLTypeHandler(this));
    // add(new GeoPointTypeHandlerJson(this));

    // add(new CollectionTypeHandler(this));
    // add(new CollectionTypeHandlerEmbedded(this));
    add(new CollectionTypeHandlerReferenced(this));
    // add(new ClassTypeHandler(this));
    // add(new LocaleTypeHandler(this));
    // add(new EnumTypeHandler(this));
    // add(new MapTypeHandler(this));
    // add(new MapTypeHandlerEmbedded(this));
    add(new MapTypeHandlerReferenced(this));
    // add(new StringTypeHandler(this));
    // add(new ArrayTypeHandler(this));
    // add(new ArrayTypeHandlerEmbedded(this));
    add(new ArrayTypeHandlerReferenced(this));
  }

  @Override
  public ITypeHandler getDefaultTypeHandler(Annotation embedRef) {
    if (embedRef == null) {
      return defaultHandler;
    }
    return embedRef instanceof Embedded ? defaultHandleEmbedded : defaultHandlerReferenced;
  }

}
