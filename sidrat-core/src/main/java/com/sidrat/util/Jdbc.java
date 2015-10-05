package com.sidrat.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sidrat.SidratProcessingException;

public class Jdbc {
    private JdbcConnectionProvider provider;

    public Jdbc(JdbcConnectionProvider provider) {
        this.provider = provider;
    }

    public Map<String, Object> first(String sql, Object... params) {
        List<Map<String, Object>> rows = query(sql, params);
        if (rows == null || rows.size() == 0)
            return null;
        return rows.get(0);
    }

    public List<Map<String, Object>> find(String sql, Object... params) {
        return query(sql, params);
    }

    public List<Map<String, Object>> query(String sql, Object... params) {
        try (Connection conn = provider.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int parameterIndex = 1;
                for (Object param : params) {
                    ps.setObject(parameterIndex++, param);
                }
                ResultSet rs = ps.executeQuery();
                ResultSetMetaData rsmd = rs.getMetaData();
                int colCount = rsmd.getColumnCount();
                List<Map<String, Object>> response = new ArrayList<Map<String, Object>>();
                Map<Integer, String> columnNames = new HashMap<Integer, String>();
                Map<Integer, SqlDataType> columnTypes = new HashMap<Integer, SqlDataType>();
                for (int i = 1; i <= colCount; i++) {
                    String colName = rsmd.getColumnLabel(i);
                    columnNames.put(i, colName);
                    int colType = rsmd.getColumnType(i);
                    SqlDataType sdt = SqlDataType.fromColumnType(colType);
                    columnTypes.put(i, sdt);
                }
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<String, Object>();
                    for (int i = 1; i <= colCount; i++) {
                        String columnName = columnNames.get(i);
                        Object columnValue = rs.getObject(i);
                        row.put(columnName, columnValue);
                    }
                    response.add(row);
                }
                return response;
            }
        } catch (SQLException e) {
            throw new SidratProcessingException("Error executing query", e);
        }
    }

    public Long insert(String sql, Object... params) {
        try (Connection conn = provider.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                int parameterIndex = 1;
                for (Object param : params) {
                    ps.setObject(parameterIndex++, param);
                }
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected == 0)
                    return null;
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new SidratProcessingException("Error executing update", e);
        }
    }

    public int update(String sql, Object... params) {
        try (Connection conn = provider.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int parameterIndex = 1;
                for (Object param : params) {
                    ps.setObject(parameterIndex++, param);
                }
                int rowsAffected = ps.executeUpdate();
                return rowsAffected;
            }
        } catch (SQLException e) {
            throw new SidratProcessingException("Error executing update", e);
        }
    }
}
