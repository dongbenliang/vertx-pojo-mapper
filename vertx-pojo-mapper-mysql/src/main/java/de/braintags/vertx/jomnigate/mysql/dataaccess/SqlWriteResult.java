/*
 * #%L
 * vertx-pojo-mapper-mysql
 * %%
 * Copyright (C) 2017 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package de.braintags.vertx.jomnigate.mysql.dataaccess;

import java.util.List;

import de.braintags.vertx.jomnigate.dataaccess.write.IWriteEntry;
import de.braintags.vertx.jomnigate.dataaccess.write.impl.WriteResult;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
public class SqlWriteResult extends WriteResult {

  /**
   * 
   */
  public SqlWriteResult() {
    super();
  }

  /**
   * @param resultList
   */
  public SqlWriteResult(List<IWriteEntry> resultList) {
    super(resultList);
  }

}
