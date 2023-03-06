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

package com.adaptris.core.services.jdbc.types;

import java.io.IOException;
import java.sql.SQLException;
import com.adaptris.jdbc.JdbcResultRow;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
* Column Translator implementation for handling integer types
*
* @config jdbc-type-integer-column-translator
*
*
* @author lchan
*
*/
@JacksonXmlRootElement(localName = "jdbc-type-integer-column-translator")
@XStreamAlias("jdbc-type-integer-column-translator")
public class IntegerColumnTranslator extends FormattableColumnTranslator {

public IntegerColumnTranslator() {
}

@Override
public String translate(JdbcResultRow rs, int column) throws SQLException, IOException {
Object integerObject = rs.getFieldValue(column);
if(integerObject instanceof Integer)
return toString(rs.getFieldValue(column));
else
return toString(Integer.valueOf(rs.getFieldValue(column).toString()));
}

@Override
public String translate(JdbcResultRow rs, String column) throws SQLException, IOException {
Object integerObject = rs.getFieldValue(column);
if(integerObject instanceof Integer)
return toString(rs.getFieldValue(column));
else
return toString(Integer.valueOf(rs.getFieldValue(column).toString()));
}

}
