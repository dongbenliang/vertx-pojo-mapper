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

package de.braintags.io.vertx.pojomapper.mysql.mapping;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.braintags.io.vertx.pojomapper.exception.MappingException;
import de.braintags.io.vertx.pojomapper.mapping.IDataStoreSynchronizer;
import de.braintags.io.vertx.pojomapper.mapping.IField;
import de.braintags.io.vertx.pojomapper.mapping.IMapper;
import de.braintags.io.vertx.pojomapper.mapping.ISyncResult;
import de.braintags.io.vertx.pojomapper.mapping.SyncAction;
import de.braintags.io.vertx.pojomapper.mapping.datastore.IColumnHandler;
import de.braintags.io.vertx.pojomapper.mapping.datastore.IColumnInfo;
import de.braintags.io.vertx.pojomapper.mapping.datastore.ITableInfo;
import de.braintags.io.vertx.pojomapper.mapping.impl.DefaultSyncResult;
import de.braintags.io.vertx.pojomapper.mapping.impl.Mapper;
import de.braintags.io.vertx.pojomapper.mysql.MySqlDataStore;
import de.braintags.io.vertx.pojomapper.mysql.mapping.datastore.SqlTableInfo;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;

/**
 * 
 * @author Michael Remme
 * 
 */

public class SqlDataStoreSynchronizer implements IDataStoreSynchronizer<String> {
  private static Logger LOGGER = LoggerFactory.getLogger(SqlDataStoreSynchronizer.class);

  private MySqlDataStore datastore;

  private static final String TABLE_QUERY = "SELECT * FROM INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA='%s' AND TABLE_NAME='%s'";
  private static final String COLUMN_QUERY = "SELECT * FROM INFORMATION_SCHEMA.COLUMNS where TABLE_SCHEMA='%s' AND TABLE_NAME='%s'";
  private static final String CREATE_TABLE = "CREATE TABLE %s.%s ( %s )";

  /**
   * 
   */
  public SqlDataStoreSynchronizer(MySqlDataStore ds) {
    this.datastore = ds;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.pojomapper.mapping.IDataStoreSynchronizer#synchronize(de.braintags.io.vertx.pojomapper.
   * mapping .IMapper, io.vertx.core.Handler)
   */
  @Override
  public void synchronize(IMapper mapper, Handler<AsyncResult<ISyncResult<String>>> resultHandler) {
    datastore.getSqlClient().getConnection(cr -> {
      if (cr.failed()) {
        resultHandler.handle(Future.failedFuture(cr.cause()));
      } else {
        SQLConnection connection = cr.result();
        try {
          readTableFromDatabase(connection, mapper, res -> checkTable(connection, (Mapper) mapper, res, resultHandler));
        } finally {
          LOGGER.debug("closing connection - sync finished");
          connection.close();
        }
      }
    });
  }

  private void checkTable(SQLConnection connection, Mapper mapper, AsyncResult<SqlTableInfo> tableResult,
      Handler<AsyncResult<ISyncResult<String>>> resultHandler) {
    if (tableResult.failed()) {
      resultHandler.handle(Future.failedFuture(tableResult.cause()));
    } else {
      try {
        SqlTableInfo dbTable = tableResult.result();
        if (dbTable == null) {
          generateNewTable(connection, mapper, resultHandler);
        } else {
          compareTables(mapper, dbTable, resultHandler);
        }
      } catch (Exception e) {
        resultHandler.handle(Future.failedFuture(e));
      }
    }

  }

  private void compareTables(IMapper mapper, SqlTableInfo currentDbTable,
      Handler<AsyncResult<ISyncResult<String>>> resultHandler) {
    Map<String, SyncAction> syncMap = currentDbTable.compareColumns(mapper);
    if (!syncMap.isEmpty()) {
      throw new UnsupportedOperationException("Implement update of table structure");
    } else {
      resultHandler.handle(Future.succeededFuture(new DefaultSyncResult(SyncAction.NO_ACTION)));
    }

  }

  /*
   * CREATE TABLE test.test2 (id INT NOT NULL AUTO_INCREMENT, name LONGTEXT, wahr BOOL, PRIMARY KEY (id))
   * 
   * CREATE TABLE test.TestTable ( id int(10) NOT NULL auto_increment, name varchar(25), PRIMARY KEY (id) )
   * ENGINE=InnoDB DEFAULT CHARSET=utf8;
   * 
   */
  private void generateNewTable(SQLConnection connection, Mapper mapper,
      Handler<AsyncResult<ISyncResult<String>>> resultHandler) {
    DefaultSyncResult syncResult = createSyncResult(mapper, SyncAction.CREATE);
    connection.execute(syncResult.getSyncCommand(), exec -> {
      if (exec.failed()) {
        LOGGER.error("error in executing command: " + syncResult.getSyncCommand());
        resultHandler.handle(Future.failedFuture(exec.cause()));
      } else {
        readTableFromDatabase(connection, mapper, tableResult -> {
          if (tableResult.failed()) {
            resultHandler.handle(Future.failedFuture(tableResult.cause()));
          } else {
            tableResult.result().copyInto(mapper);
            resultHandler.handle(Future.succeededFuture(syncResult));
          }
        });

      }
    });
  }

  private DefaultSyncResult createSyncResult(IMapper mapper, SyncAction action) {
    String columnPart = generateColumnPart(mapper);
    String tableName = mapper.getTableInfo().getName();
    String database = datastore.getDatabase();
    String sqlCommand = String.format(CREATE_TABLE, database, tableName, columnPart);
    DefaultSyncResult sr = new DefaultSyncResult(action, sqlCommand);
    return sr;
  }

  /**
   * Generates the part of the sequence, which is creating the columns id int(10) NOT NULL auto_increment, name
   * varchar(25), PRIMARY KEY (id)
   * 
   * @param mapper
   * @return
   */
  private String generateColumnPart(IMapper mapper) {
    StringBuilder buffer = new StringBuilder();
    IField idField = mapper.getIdField();
    ITableInfo ti = mapper.getTableInfo();
    Set<String> fieldNames = mapper.getFieldNames();

    for (String fieldName : fieldNames) {
      String colString = generateColumn(mapper, ti, fieldName);
      buffer.append(colString).append(", ");
    }
    buffer.append(String.format("PRIMARY KEY ( %s )", idField.getColumnInfo().getName()));
    return buffer.toString();
  }

  private String generateColumn(IMapper mapper, ITableInfo ti, String fieldName) {
    IField field = mapper.getField(fieldName);
    IColumnInfo ci = ti.getColumnInfo(field);
    IColumnHandler ch = ci.getColumnHandler();
    if (ch == null)
      throw new MappingException("Undefined column handler for field  " + field.getFullName());
    String colString = (String) ch.generate(field);
    if (colString == null || colString.isEmpty())
      throw new UnsupportedOperationException(
          String.format(" Did not generate column creation string for column '%s'", fieldName));
    return colString;
  }

  private void readTableFromDatabase(SQLConnection connection, IMapper mapper,
      Handler<AsyncResult<SqlTableInfo>> resultHandler) {
    String tableQuery = String.format(TABLE_QUERY, datastore.getDatabase(), mapper.getTableInfo().getName());
    executeCommand(connection, tableQuery, result -> {
      if (result.failed()) {
        resultHandler.handle(Future.failedFuture(result.cause()));
      } else {
        ResultSet rs = result.result();
        SqlTableInfo tInfo = createTableInfo(mapper, rs);
        if (tInfo == null) { // signal that table doesn't exist
          resultHandler.handle(Future.succeededFuture(null));
          return;
        }
        String columnQuery = String.format(COLUMN_QUERY, datastore.getDatabase(), mapper.getTableInfo().getName());
        executeCommand(connection, columnQuery, colResult -> readColumns(mapper, tInfo, colResult, resultHandler));
      }
    });

  }

  /**
   * Reads the columns from the datastore and updates the infos into the {@link ITableInfo}
   * 
   * @param mapper
   *          the mapper
   * @param tInfo
   *          the instance of {@link ITableInfo}
   * @param result
   *          the {@link ResultSet}
   * @param resultHandler
   *          the handler to be called
   */
  private void readColumns(IMapper mapper, SqlTableInfo tInfo, AsyncResult<ResultSet> result,
      Handler<AsyncResult<SqlTableInfo>> resultHandler) {
    if (result.failed()) {
      resultHandler.handle(Future.failedFuture(result.cause()));
    } else {
      ResultSet rs = result.result();
      if (rs.getNumRows() == 0) {
        String message = String.format("No column definitions found for '%s'", tInfo.getName());
        resultHandler.handle(Future.failedFuture(new MappingException(message)));
        return;
      }

      try {
        List<JsonObject> rows = rs.getRows();
        for (JsonObject row : rows) {
          tInfo.createColumnInfo(row);
        }
        resultHandler.handle(Future.succeededFuture(tInfo));
      } catch (Exception e) {
        resultHandler.handle(Future.failedFuture(e));
        return;
      }
    }
  }

  private SqlTableInfo createTableInfo(IMapper mapper, ResultSet resultSet) {
    if (resultSet.getNumRows() == 0)
      return null;
    return new SqlTableInfo(mapper);
  }

  /**
   * Executed the given command and returns the {@link ResultSet} to the {@link Handler}
   * 
   * @param command
   * @param resultHandler
   */
  private void executeCommand(SQLConnection connection, String command, Handler<AsyncResult<ResultSet>> resultHandler) {
    connection.query(command, qr -> {
      if (qr.failed()) {
        resultHandler.handle(Future.failedFuture(qr.cause()));
      } else {
        ResultSet res = qr.result();
        resultHandler.handle(Future.succeededFuture(res));
      }
    });
  }
}