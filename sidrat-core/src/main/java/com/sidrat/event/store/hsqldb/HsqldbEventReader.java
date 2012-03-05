package com.sidrat.event.store.hsqldb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sidrat.SidratProcessingException;
import com.sidrat.event.SidratEvent;
import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.SidratFieldChangedEvent;
import com.sidrat.event.SidratLocalVariableEvent;
import com.sidrat.event.store.EventReader;
import com.sidrat.event.tracking.StackFrame;
import com.sidrat.replay.SystemState;
import com.sidrat.util.Jdbc;
import com.sidrat.util.JdbcConnectionProvider;

public class HsqldbEventReader implements EventReader, JdbcConnectionProvider {
    private String connString;
    private Jdbc jdbcHelper = new Jdbc(this);

    private static final String EVENTS_QUERY = "SELECT e.id, e.thread_id, m.name AS method, m.class_id, c.name AS clazz, e.lineNumber, e.entering FROM executions e LEFT JOIN methods m ON e.method_id=m.id LEFT JOIN classes c ON m.class_id = c.id ";

    static {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (Exception e) {
            throw new SidratProcessingException("Could not load hsqldb driver", e);
        }
    }

    public HsqldbEventReader(String filename) {
        this.connString = "jdbc:hsqldb:file:" + filename + "/sidrat";
    }

    public Connection getConnection() {
        Connection c;
        try {
            c = DriverManager.getConnection(connString, "sa", "");
        } catch (SQLException e) {
            throw new SidratProcessingException("Could not obtain connection to hsqldb store", e);
        }
        return c;
    }

    @Override
    public SidratExecutionEvent find(Long time) {
        Map<String, Object> row = jdbcHelper.first(EVENTS_QUERY + "WHERE id=?", time);
        return getEvent(row);
    }

    @Override
    public SidratExecutionEvent findFirst() {
        Map<String, Object> row = jdbcHelper.first(EVENTS_QUERY + "ORDER BY id ASC LIMIT 1");
        return getEvent(row);
    }

    @Override
    public SidratExecutionEvent findNext(SidratExecutionEvent lastEvent) {
        Map<String, Object> row = jdbcHelper.first(EVENTS_QUERY + "WHERE id > ? ORDER BY id ASC LIMIT 1", lastEvent.getTime());
        return getEvent(row);
    }

    @Override
    public SidratExecutionEvent findPrev(SidratExecutionEvent lastEvent) {
        Map<String, Object> row = jdbcHelper.first(EVENTS_QUERY + "WHERE id < ? ORDER BY id DESC LIMIT 1", lastEvent.getTime());
        return getEvent(row);
    }

    @Override
    public Map<String,Object> locals(Long time) {
        Long methodEntryTime = (Long) jdbcHelper.first("SELECT MAX(id) AS t FROM executions WHERE id <= ? AND entering=1", time).get("T");
        Map<String,Object> methodEntrypoint = jdbcHelper.first("SELECT id, lineNumber FROM executions WHERE id = ? AND entering=1", methodEntryTime);
        Map<String,Object> currentLine = jdbcHelper.first("SELECT id, lineNumber FROM executions WHERE id = ?", time);
        Integer lineNumberStart = (Integer) methodEntrypoint.get("LINENUMBER");
        Integer lineNumberCurrent = (Integer) currentLine.get("LINENUMBER");
        List<Map<String, Object>> variables = jdbcHelper.query("SELECT * FROM variables WHERE rangeStart <= ? AND rangeEnd >= ?", lineNumberCurrent, lineNumberCurrent);
        Map<String,Object> locals = Maps.newHashMap();
        for (Map<String,Object> var : variables) {
            String name = (String)var.get("VARIABLE_NAME");
            Long id = (Long)var.get("ID");
            Long mostRecentUpdate = (Long)jdbcHelper.first("SELECT MAX(event_id) AS EVENT_ID FROM variable_updates WHERE variable_id = ? AND event_id <= ?", id, time).get("EVENT_ID");
            if (mostRecentUpdate == null) {
                locals.put(name, "<null>");
            } else {
                Object val = jdbcHelper.first("SELECT value FROM variable_updates WHERE event_id = ?", mostRecentUpdate).get("VALUE");
                locals.put(name, val);
            }
        }
        return locals;
    }
    
    public List<SidratEvent> executions(String loc) {
        // TODO: return a list of SidratEvents where we execute the line of code described by 'loc'
        // loc should be in the form 'com.pkg.ClassName.method:##'
        return Lists.newArrayList();
    }
        
    private String findThread(Long threadID) {
        String strThreadID = String.valueOf(threadID);
        String threadName = SystemState.current().getThreadName(strThreadID);
        if (threadName != null)
            return threadName;
        Map<String, Object> row = jdbcHelper.first("SELECT name FROM threads WHERE id = ?", threadID);
        threadName = (String) row.get("NAME");
        SystemState.current().addNewThread(strThreadID, threadName);
        return threadName;
    }

    private SidratExecutionEvent getEvent(Map<String, Object> row) {
        if (row == null)
            return null;
        Integer lineNumber = (Integer) row.get("LINENUMBER");
        Long time = (Long) row.get("ID");
        Long objectInstanceID = (Long) row.get("OBJECT_ID");
        Long threadID = (Long) row.get("THREAD_ID");
        String threadName = findThread(threadID);
        String className = (String) row.get("CLAZZ");
        String methodName = (String) row.get("METHOD");
        Boolean entering = (Boolean) row.get("ENTERING");
        StackFrame stackFrame = new StackFrame(className, methodName);
        SidratExecutionEvent event = new SidratExecutionEvent(time, objectInstanceID, stackFrame, threadID, threadName, lineNumber, entering);
        return event;
    }

    /*
    private void addFieldUpdateDetails(Long time, SidratFieldChangedEvent event) throws ClassNotFoundException {
        Map<String, Object> fieldUpdateDetails = jdbcHelper.first(
                "SELECT fu.field_id, fu.value, f.field_name, f.object_id, o.clazz FROM field_updates fu LEFT JOIN fields f ON fu.field_id = f.id LEFT JOIN objects o ON f.object_id = o.id WHERE fu.event_id = ?", time);
        String value = (String) fieldUpdateDetails.get("VALUE");
        String fieldName = (String) fieldUpdateDetails.get("FIELD_NAME");
        Long objectInstanceID = (Long) fieldUpdateDetails.get("OBJECT_ID");
        String clazz = (String) fieldUpdateDetails.get("CLAZZ");
        event.setObjectInstanceID(objectInstanceID);
        event.setOwnerClass(Class.forName(clazz));
        event.setVariableName(fieldName);
        event.setValue(value);
    }

    private void addLocalVariableDetails(Long time, SidratLocalVariableEvent event) {
        Map<String, Object> varUpdateDetails = jdbcHelper.first(
                "SELECT v.variable_name, vu.value, v.rangeStart, v.rangeEnd FROM variable_updates vu LEFT JOIN variables v ON vu.variable_id = v.id WHERE vu.event_id = ?", time);
        String value = (String) varUpdateDetails.get("VALUE");
        String variableName = (String) varUpdateDetails.get("VARIABLE_NAME");
        Integer rangeStart = (Integer) varUpdateDetails.get("RANGE_START");
        Integer rangeEnd = (Integer) varUpdateDetails.get("RANGE_END");
        event.setVariableName(variableName);
        event.setValue(value);
        event.setVariableValidityRange(rangeStart, rangeEnd);
    }
    */
}
