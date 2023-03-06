/*
* Copyright 2015 Adaptris Ltd.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.adaptris.core.services.jdbc;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;

import javax.validation.Valid;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.jdbc.types.ColumnWriter;
import com.adaptris.core.services.jdbc.types.StringColumnTranslator;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.jdbc.JdbcResult;
import com.adaptris.jdbc.JdbcResultRow;
import com.adaptris.jdbc.JdbcResultSet;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
* Takes the first result set, and the specified column (optional) and makes that the payload.
* <p>
* Used as part of a {@link JdbcDataQueryService}
* </p>
*
* @config jdbc-simple-output
*/
@JacksonXmlRootElement(localName = "jdbc-simple-output")
@XStreamAlias("jdbc-simple-output")
public class SimplePayloadResultSetTranslator extends ResultSetTranslatorBase {

private static final ColumnWriter DEFAULT_COLUMN_WRITER = new StringColumnTranslator();

private String columnName;
@Valid
private ColumnWriter columnWriter;

public SimplePayloadResultSetTranslator() {
super();
}

@Override
public void translate(JdbcResult source, AdaptrisMessage target) throws SQLException, ServiceException {
try (OutputStream output = target.getOutputStream()) {
Iterator<JdbcResultRow> iter = firstResultSet(source).getRows().iterator();
if (iter.hasNext()) {
JdbcResultRow resultRow = iter.next();
if (isEmpty(columnName)) {
columnWriter().write(resultRow, 0, output);
}
else {
columnWriter().write(resultRow, columnName, output);
}
}
else {
log.debug("No Rows to process");
}
}
catch (IOException e) {
throw ExceptionHelper.wrapServiceException(e);
}
}

public String getColumnName() {
return columnName;
}

/**
* Set the column to be used as the payload.
*
* @param columnName the column name, if not specified then the first column is used).
*/
public void setColumnName(String columnName) {
this.columnName = columnName;
}

public ColumnWriter getColumnWriter() {
return columnWriter;
}

public void setColumnWriter(ColumnWriter cw) {
columnWriter = Args.notNull(cw, "columnWriter");
}

public ColumnWriter columnWriter() {
return getColumnWriter() != null ? getColumnWriter() : DEFAULT_COLUMN_WRITER;
}

JdbcResultSet firstResultSet(JdbcResult result) {
if (result.isHasResultSet()) {
return result.getResultSet(0);
}
return new JdbcResultSet() {
@Override
public Iterable<JdbcResultRow> getRows() {
return Collections.EMPTY_LIST;
}

@Override
public void close() {

}
};
}

}
