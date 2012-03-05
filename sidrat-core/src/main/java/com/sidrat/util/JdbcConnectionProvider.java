package com.sidrat.util;

import java.sql.Connection;

public interface JdbcConnectionProvider {
    public Connection getConnection();
}
