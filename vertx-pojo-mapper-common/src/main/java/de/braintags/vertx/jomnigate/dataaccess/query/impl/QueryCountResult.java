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

package de.braintags.vertx.jomnigate.dataaccess.query.impl;

import de.braintags.vertx.jomnigate.IDataStore;
import de.braintags.vertx.jomnigate.dataaccess.query.IQueryCountResult;
import de.braintags.vertx.jomnigate.mapping.IMapper;

/**
 * The default implementation of {@link IQueryCountResult}
 * 
 * @author Michael Remme
 * 
 */

public class QueryCountResult implements IQueryCountResult {
  private IMapper mapper;
  private IDataStore dataStore;
  private long count;
  private IQueryExpression originalQuery;

  /**
   * Constructor based on various information
   * 
   * @param mapper
   *          the mapper which was used
   * @param dataStore
   *          the datastore which was used
   * @param count
   *          the number of instances found
   * @param originalQuery
   *          the object which was used to process native the query
   */
  public QueryCountResult(IMapper mapper, IDataStore dataStore, long count, IQueryExpression originalQuery) {
    this.mapper = mapper;
    this.dataStore = dataStore;
    this.count = count;
    this.originalQuery = originalQuery;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.vertx.jomnigate.dataaccess.query.IQueryCountResult#getDataStore()
   */
  @Override
  public IDataStore getDataStore() {
    return dataStore;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.vertx.jomnigate.dataaccess.query.IQueryCountResult#getMapper()
   */
  @Override
  public IMapper getMapper() {
    return mapper;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.vertx.jomnigate.dataaccess.query.IQueryCountResult#getOriginalQuery()
   */
  @Override
  public IQueryExpression getOriginalQuery() {
    return originalQuery;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.vertx.jomnigate.dataaccess.query.IQueryCountResult#getCount()
   */
  @Override
  public long getCount() {
    return count;
  }

}
