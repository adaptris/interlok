package com.adaptris.core.services.jdbc;

import java.util.List;

public interface StatementParameterCollection extends List<StatementParameter>{
  
  StatementParameter getParameterByName(String name);

}
