/*
 * #%L
 * vertx-pojongo
 * %%
 * Copyright (C) 2015 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package de.braintags.io.vertx.pojomapper.mongo.dataaccess;

import java.util.List;

import de.braintags.io.vertx.pojomapper.IDataStore;
import de.braintags.io.vertx.pojomapper.dataaccess.query.IQuery;
import de.braintags.io.vertx.pojomapper.dataaccess.query.IQueryCountResult;
import de.braintags.io.vertx.pojomapper.dataaccess.query.IQueryResult;
import de.braintags.io.vertx.pojomapper.dataaccess.query.impl.Query;
import de.braintags.io.vertx.pojomapper.dataaccess.query.impl.QueryCountResult;
import de.braintags.io.vertx.pojomapper.mongo.MongoDataStore;
import de.braintags.io.vertx.pojomapper.mongo.mapper.MongoMapper;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;

/**
 * An implementation of {@link IQuery} for Mongo
 * 
 * @author Michael Remme
 * @param <T>
 *          the type of the underlaying mapper
 */
public class MongoQuery<T> extends Query<T> {

  /**
   * Constructor
   * 
   * @param mapperClass
   *          the mapper class
   * @param datastore
   *          the datastore to be used
   */
  public MongoQuery(Class<T> mapperClass, IDataStore datastore) {
    super(mapperClass, datastore);
  }

  @Override
  public void execute(Handler<AsyncResult<IQueryResult<T>>> resultHandler) {
    try {
      createQueryDefinition(result -> {
        if (result.failed()) {
          resultHandler.handle(Future.failedFuture(result.cause()));
        } else {
          doFind(result.result(), resultHandler);
        }
      });
    } catch (Exception e) {
      Future<IQueryResult<T>> future = Future.failedFuture(e);
      resultHandler.handle(future);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.pojomapper.dataaccess.query.IQuery#executeExplain(io.vertx.core.Handler)
   */
  @Override
  public void executeExplain(Handler<AsyncResult<IQueryResult<T>>> resultHandler) {
    resultHandler.handle(Future.failedFuture(new UnsupportedOperationException("Not implemented yet")));
  }

  @Override
  public void executeCount(Handler<AsyncResult<IQueryCountResult>> resultHandler) {
    try {
      createQueryDefinition(result -> {
        if (result.failed()) {
          resultHandler.handle(Future.failedFuture(result.cause()));
        } else {
          doFindCount(result.result(), resultHandler);
        }
      });
    } catch (Exception e) {
      Future<IQueryCountResult> future = Future.failedFuture(e);
      resultHandler.handle(future);
    }
  }

  private void doFindCount(MongoQueryRambler rambler, Handler<AsyncResult<IQueryCountResult>> resultHandler) {
    MongoClient mongoClient = ((MongoDataStore) getDataStore()).getMongoClient();
    String column = getMapper().getTableInfo().getName();
    mongoClient.count(column, ((MongoQueryExpression) rambler.getQueryExpression()).getQueryDefinition(), qResult -> {
      if (qResult.failed()) {
        Future<IQueryCountResult> future = Future.failedFuture(qResult.cause());
        resultHandler.handle(future);
      } else {
        QueryCountResult qcr = new QueryCountResult(getMapper(), getDataStore(), qResult.result(),
            rambler.getQueryExpression());
        Future<IQueryCountResult> future = Future.succeededFuture(qcr);
        resultHandler.handle(future);
      }
    });
  }

  private void doFind(MongoQueryRambler rambler, Handler<AsyncResult<IQueryResult<T>>> resultHandler) {
    MongoClient mongoClient = ((MongoDataStore) getDataStore()).getMongoClient();
    String column = getMapper().getTableInfo().getName();
    FindOptions fo = new FindOptions();
    fo.setSkip(getStart());
    fo.setLimit(getLimit());
    mongoClient.findWithOptions(column, ((MongoQueryExpression) rambler.getQueryExpression()).getQueryDefinition(), fo,
        qResult -> {
          if (qResult.failed()) {
            Future<IQueryResult<T>> future = Future.failedFuture(qResult.cause());
            resultHandler.handle(future);
          } else {
            createQueryResult(qResult.result(), rambler, resultHandler);
          }
        });
  }

  private void createQueryResult(List<JsonObject> findList, MongoQueryRambler rambler,
      Handler<AsyncResult<IQueryResult<T>>> resultHandler) {
    MongoQueryResult<T> qR = new MongoQueryResult<T>(findList, (MongoDataStore) getDataStore(),
        (MongoMapper) getMapper(), rambler);
    if (getLimit() > 0 && isReturnCompleteCount() && qR.size() == getLimit()) {
      fetchCompleteCount(qR, resultHandler);
    } else {
      qR.setCompleteResult(qR.size());
      resultHandler.handle(Future.succeededFuture(qR));
    }
  }

  private void fetchCompleteCount(MongoQueryResult<T> qR, Handler<AsyncResult<IQueryResult<T>>> resultHandler) {
    int bLimit = getLimit();
    int bStart = getStart();
    setLimit(0);
    setStart(0);
    executeCount(cr -> {
      if (cr.failed()) {
        resultHandler.handle(Future.failedFuture(cr.cause()));
      } else {
        long count = cr.result().getCount();
        qR.setCompleteResult(count);
        setLimit(bLimit);
        setStart(bStart);
        resultHandler.handle(Future.succeededFuture(qR));
      }
    });
  }

  /**
   * Create the {@link JsonObject} which then contains the definition for the query, which will be executed by
   * {@link MongoClient}
   * 
   * @param resultHandler
   *          the resulthandler which will receive notification
   */
  void createQueryDefinition(Handler<AsyncResult<MongoQueryRambler>> resultHandler) {
    MongoQueryRambler rambler = new MongoQueryRambler();
    executeQueryRambler(rambler, result -> {
      if (result.failed()) {
        resultHandler.handle(Future.failedFuture(result.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(rambler));
      }
    });
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.pojomapper.dataaccess.query.IQueryContainer#parent()
   */
  @Override
  public Object parent() {
    return null;
  }

}
