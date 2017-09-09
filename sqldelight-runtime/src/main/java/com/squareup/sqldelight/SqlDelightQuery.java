/*
 * Copyright (C) 2016 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.sqldelight;

import android.arch.persistence.db.SupportSQLiteQuery;
import java.util.Set;

public abstract class SqlDelightQuery implements SupportSQLiteQuery {
  /** A set of the tables this statement observes. */
  public final Set<String> tables;
  private final String sql;

  public SqlDelightQuery(Set<String> tables, String sql) {
    this.tables = tables;
    this.sql = sql;
  }

  @Override public final String getSql() {
    return sql;
  }
}
