/*
 * #%L
 * vertx-pojo-mapper-common
 * %%
 * Copyright (C) 2015 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

package de.braintags.io.vertx.pojomapper.testdatastore;

import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.runner.RunWith;

import de.braintags.io.vertx.pojomapper.IDataStore;
import de.braintags.io.vertx.pojomapper.dataaccess.delete.IDelete;
import de.braintags.io.vertx.pojomapper.dataaccess.delete.IDeleteResult;
import de.braintags.io.vertx.pojomapper.dataaccess.query.IQuery;
import de.braintags.io.vertx.pojomapper.dataaccess.query.IQueryCountResult;
import de.braintags.io.vertx.pojomapper.dataaccess.query.IQueryResult;
import de.braintags.io.vertx.pojomapper.dataaccess.write.IWrite;
import de.braintags.io.vertx.pojomapper.dataaccess.write.IWriteEntry;
import de.braintags.io.vertx.pojomapper.dataaccess.write.IWriteResult;
import de.braintags.io.vertx.util.ErrorObject;
import de.braintags.io.vertx.util.ExceptionUtil;
import de.braintags.io.vertx.util.IteratorAsync;
import io.vertx.core.AsyncResult;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */

@RunWith(VertxUnitRunner.class)
public abstract class DatastoreBaseTest {
  private static final Logger logger = LoggerFactory.getLogger(DatastoreBaseTest.class);

  @Rule
  public Timeout rule = Timeout.seconds(Integer.parseInt(System.getProperty("testTimeout", "5")));

  public static IDataStore getDataStore() {
    return TestHelper.getDatastoreContainer().getDataStore();
  }

  public static ResultContainer saveRecords(TestContext context, List<?> records) {
    return saveRecords(context, records, 0);
  }

  /**
   * save the list of records
   * 
   * @param records
   *          the records to be saved
   * @param waittime
   *          the time to wait for saving
   * @return the result
   */
  @SuppressWarnings("unchecked")
  public static ResultContainer saveRecords(TestContext context, List<?> records, int waittime) {
    Async async = context.async();
    ResultContainer resultContainer = new ResultContainer();
    IWrite<Object> write = (IWrite<Object>) getDataStore().createWrite(records.get(0).getClass());
    for (Object record : records) {
      write.add(record);
    }
    write.save(result -> {
      try {
        resultContainer.writeResult = result.result();
        checkWriteResult(context, result, records.size());
      } catch (AssertionError e) {
        resultContainer.assertionError = e;
      } catch (Throwable e) {
        resultContainer.assertionError = new AssertionError(e);
      } finally {
        async.complete();
      }
    });

    if (waittime == 0) {
      async.await();
    } else {
      async.await(waittime);
    }
    return resultContainer;
  }

  public static ResultContainer saveRecord(TestContext context, Object sm) {
    return saveRecords(context, Arrays.asList(sm));
  }

  public static void checkWriteResult(TestContext context, AsyncResult<IWriteResult> result,
      int expectedNumberOfRecords) {
    resultFine(result);
    context.assertNotNull(result.result());
    IWriteEntry entry = result.result().iterator().next();
    context.assertNotNull(entry);
    context.assertNotNull(entry.getStoreObject());
    context.assertNotNull(entry.getId(), "the id of an IWriteEntry must be defined");
    if (expectedNumberOfRecords >= 0) {
      context.assertEquals(expectedNumberOfRecords, result.result().size());
    }
  }

  public static void resultFine(AsyncResult<?> result) {
    if (result.failed()) {
      logger.error("", result.cause());
      throw new AssertionError("result failed", result.cause());
    }
  }

  /**
   * Executes a query and checks for the expected result
   * 
   * @param query
   *          the query to be executed
   * @param expectedResult
   *          the expected number of records
   * @return ResultContainer with certain informations
   */
  public static ResultContainer find(TestContext context, IQuery<?> query, int expectedResult) {
    Async async = context.async();
    ResultContainer resultContainer = new ResultContainer();
    query.execute(result -> {
      try {
        resultFine(result);
        resultContainer.queryResult = result.result();
        logger.info("performed find with: " + resultContainer.queryResult.getOriginalQuery());
      } catch (AssertionError e) {
        resultContainer.assertionError = e;
      } catch (Throwable e) {
        resultContainer.assertionError = new AssertionError(e);
      } finally {
        async.complete();
      }
    });

    async.await();

    checkQueryResult(context, resultContainer.queryResult, expectedResult);
    return resultContainer;
  }

  /**
   * Executes a query and checks for the expected result
   * 
   * @param query
   *          the query to be executed
   * @param expectedResult
   *          the expected number of records
   * @return ResultContainer with certain informations
   */
  public static ResultContainer findCount(TestContext context, IQuery<?> query, int expectedResult) {
    Async async = context.async();
    ResultContainer resultContainer = new ResultContainer();
    query.executeCount(result -> {
      try {
        resultContainer.queryResultCount = result.result();
        checkQueryResultCount(context, result, expectedResult);
        logger.info(resultContainer.queryResultCount.getOriginalQuery());
      } catch (AssertionError e) {
        resultContainer.assertionError = e;
      } catch (Throwable e) {
        resultContainer.assertionError = new AssertionError(e);
      } finally {
        async.complete();
        ;
      }
    });

    async.await();
    return resultContainer;
  }

  /**
   * Performs the delete action and processes the checkQuery to improve the correct result
   * 
   * @param delete
   *          the {@link IDelete} to be executed
   * @param checkQuery
   *          the query to improve the correct result
   * @param expectedResult
   *          the expected result of checkQuery
   * @return {@link ResultContainer} with deleteResult and queryResult
   */
  public static ResultContainer delete(TestContext context, IDelete<?> delete, IQuery<?> checkQuery,
      int expectedResult) {
    Async async = context.async();
    ResultContainer resultContainer = new ResultContainer();
    delete.delete(result -> {
      try {
        resultContainer.deleteResult = result.result();
        checkDeleteResult(context, result);
      } catch (AssertionError e) {
        resultContainer.assertionError = e;
      } catch (Throwable e) {
        resultContainer.assertionError = new AssertionError(e);
      } finally {
        async.complete();
        ;
      }
    });

    async.await();

    // Now perform the query check
    ResultContainer queryResult = find(context, checkQuery, expectedResult);
    resultContainer.assertionError = queryResult.assertionError;
    resultContainer.queryResult = queryResult.queryResult;
    return resultContainer;
  }

  public static void checkDeleteResult(TestContext context, AsyncResult<? extends IDeleteResult> dResult) {
    resultFine(dResult);
    IDeleteResult dr = dResult.result();
    context.assertNotNull(dr);
    context.assertNotNull(dr.getOriginalCommand());
    logger.info(dr.getOriginalCommand());
  }

  public static void checkQueryResultCount(TestContext context, AsyncResult<? extends IQueryCountResult> qResult,
      int expectedResult) {
    resultFine(qResult);
    IQueryCountResult qr = qResult.result();
    context.assertNotNull(qr);
    context.assertEquals(new Long(expectedResult), new Long(qr.getCount()));
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static void checkQueryResult(TestContext context, IQueryResult qr, int expectedResult) {
    Async async = context.async();
    try {
      context.assertNotNull(qr);
    } catch (Exception e) {
      logger.error("", e);
      async.complete();
      throw ExceptionUtil.createRuntimeException(e);
    }

    if (expectedResult < 0) {
      async.complete();
      return;
    }
    if (expectedResult == 0) {
      try {
        context.assertFalse(qr.iterator().hasNext());
      } finally {
        async.complete();
      }
    } else {
      IteratorAsync<?> itr = qr.iterator();
      try {
        context.assertTrue(itr.hasNext());
      } catch (Exception e) {
        logger.error("", e);
        async.complete();
        throw ExceptionUtil.createRuntimeException(e);
      }

      itr.next(nitr -> {
        try {
          if (nitr.failed()) {
            logger.error("", nitr.cause());
            throw ExceptionUtil.createRuntimeException(nitr.cause());
          }
          context.assertNotNull(nitr.result());
        } finally {
          async.complete();
        }
      });
    }

    async.await();
  }

  /**
   * Calls {@link IDatastoreContainer#clearTable(String, io.vertx.core.Handler)} and waits for it.
   * 
   * @param context
   * @param tableName
   */
  public static void clearTable(TestContext context, String tableName) {
    Async async = context.async();
    ErrorObject<Void> err = new ErrorObject<Void>(null);
    TestHelper.getDatastoreContainer().clearTable(tableName, result -> {
      if (result.failed()) {
        err.setThrowable(result.cause());
      }
      async.complete();
    });

    async.await();
    if (err.isError())
      throw err.getRuntimeException();
  }

  public static void dropTable(TestContext context, String tableName) {
    Async async = context.async();
    ErrorObject<Void> err = new ErrorObject<Void>(null);
    TestHelper.getDatastoreContainer().dropTable(tableName, result -> {
      if (result.failed()) {
        err.setThrowable(result.cause());
      }
      async.complete();
    });

    async.await();
    if (err.isError())
      throw err.getRuntimeException();
  }
}
