package com.sidrat.util;


public enum SqlDataType {
    BIT(-7), TINYINT(-6), BIGINT(-5), LONGVARBINARY(-4), VARBINARY(-3), BINARY(-2), LONGVARCHAR(-1), NULL(0), CHAR(1), NUMERIC(
            2), DECIMAL(3), INTEGER(4), SMALLINT(5), FLOAT(6), REAL(7), DOUBLE(8), VARCHAR(12), DATE(91), TIME(92), TIMESTAMP(
            93), OTHER(1111);

    private int type;

    private SqlDataType(int type) {
        this.type = type;
    }

    public static SqlDataType fromColumnType(int val) {
        for (SqlDataType dataType : values()) {
            if (dataType.type == val)
                return dataType;
        }
        return null;
    }
}
