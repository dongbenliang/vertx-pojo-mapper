/*
 * #%L
 * vertx-pojo-mapper-common-test
 * %%
 * Copyright (C) 2017 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package de.braintags.vertx.jomnigate.dataaccess.datatypetests;

import java.util.List;

import org.junit.Test;

import de.braintags.vertx.jomnigate.dataaccess.query.IQuery;
import de.braintags.vertx.jomnigate.testdatastore.mapper.SimpleMapper;
import de.braintags.vertx.jomnigate.testdatastore.mapper.typehandler.BaseRecord;
import de.braintags.vertx.jomnigate.testdatastore.mapper.typehandler.ReferenceMapper_Single;
import de.braintags.vertx.jomnigate.testdatastore.mapper.typehandler.ReferenceMapper_SingleInSingle;
import io.vertx.ext.unit.TestContext;

/**
 * Tests for testing embedded Arrays
 * 
 * @author Michael Remme
 * 
 */
public class ReferencedSingleTest extends AbstractDatatypeTest {
  private static final io.vertx.core.logging.Logger LOGGER = io.vertx.core.logging.LoggerFactory
      .getLogger(ReferencedSingleTest.class);

  public ReferencedSingleTest() {
    super("simpleMapper");
  }

  @Test
  public void testReferenceMapper_SingleInSingle(TestContext context) {
    clearTable(context, ReferenceMapper_SingleInSingle.class.getSimpleName());
    clearTable(context, ReferenceMapper_Single.class.getSimpleName());
    clearTable(context, SimpleMapper.class.getSimpleName());
    ReferenceMapper_SingleInSingle record = new ReferenceMapper_SingleInSingle();
    saveRecord(context, record);

    ReferenceMapper_SingleInSingle rec2 = (ReferenceMapper_SingleInSingle) findRecordByID(context,
        ReferenceMapper_SingleInSingle.class, record.id);
    context.assertNotNull(rec2);
    context.assertTrue(rec2.equals(record));

  }

  @Test
  public void extreme(TestContext context) {
    clearTable(context, ReferenceMapper_Single.class.getSimpleName());
    ReferenceMapper_Single record = new ReferenceMapper_Single();
    record.simpleMapper = null;
    saveRecord(context, record);
    IQuery<ReferenceMapper_Single> query = getDataStore(context).createQuery(ReferenceMapper_Single.class);
    List list = findAll(context, query);
    context.assertEquals(1, list.size());
    ReferenceMapper_Single loaded = (ReferenceMapper_Single) list.get(0);
    context.assertNull(loaded.simpleMapper);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.vertx.jomnigate.testdatastore.typehandler.AbstractTypeHandlerTest#createInstance()
   */
  @Override
  public BaseRecord createInstance(TestContext context) {
    BaseRecord mapper = new ReferenceMapper_Single();
    return mapper;
  }

}
