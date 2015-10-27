package com.sidrat.event.store.hsqldb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sidrat.SidratProcessingException;
import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.SidratMethodEntryEvent;
import com.sidrat.event.store.EventReader;
import com.sidrat.event.tracking.CapturedFieldValue;
import com.sidrat.event.tracking.CapturedLocalVariableValue;
import com.sidrat.event.tracking.ExecutionLocation;
import com.sidrat.event.tracking.TrackedObject;
import com.sidrat.event.tracking.TrackedVariable;
import com.sidrat.replay.SystemState;
import com.sidrat.util.Jdbc;
import com.sidrat.util.JdbcConnectionProvider;
import com.sidrat.util.Pair;

/**
 * Reads an HSQLDB database Sidrat recording of a program execution.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class HsqldbEventReader implements EventReader, JdbcConnectionProvider {
    private static final String EVENTS_QUERY = "SELECT e.id, me.thread_id, m.name AS method, m.class_id, c.name AS clazz, e.lineNumber, me.object_id FROM executions e LEFT JOIN method_entries me ON e.methodentry_id = me.id LEFT JOIN methods m ON me.method_id=m.id LEFT JOIN classes c ON m.class_id = c.id ";

    static {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (Exception e) {
            throw new SidratProcessingException("Could not load hsqldb driver", e);
        }
    }

    private String connString;

    private Jdbc jdbcHelper = new Jdbc(this);

    public HsqldbEventReader(String filename) {
        this.connString = "jdbc:hsqldb:file:" + filename + "/sidrat";
    }

    @Override
    public Map<String, CapturedFieldValue> eval(Long time, String objectID) {
        List<Map<String, Object>> fields = jdbcHelper.query("SELECT * FROM fields WHERE object_id = ?", objectID);
        Map<String, CapturedFieldValue> values = Maps.newHashMap();
        for (Map<String, Object> field : fields) {
            String fieldName = (String) field.get("FIELD_NAME");
            Map<String, Object> update = jdbcHelper.first("SELECT fu.*, o.clazz FROM field_updates fu LEFT JOIN objects o ON fu.ref = o.id WHERE fu.field_id = ? AND fu.event_id <= ? ORDER BY event_id DESC", objectID, time);
            if (update != null) {
                String value = (String) update.get("VALUE");
                String ref = (String) update.get("REF");
                String className = (String) update.get("CLAZZ");
                TrackedObject obj = new TrackedObject(className, value, ref);
                CapturedFieldValue fieldValue = new CapturedFieldValue(time, objectID, obj);
                values.put(fieldName, fieldValue);
            }
        }
        return values;
    }

    @Override
    public List<SidratExecutionEvent> executions(String className, String method, int lineNumber) {
        List<Map<String, Object>> rows = jdbcHelper.find(EVENTS_QUERY + "WHERE clazz=? AND method=? AND lineNumber=?", className, method, lineNumber);
        List<SidratExecutionEvent> events = Lists.newArrayList();
        for (Map<String, Object> row : rows) {
            events.add(getEvent(row));
        }
        return events;
    }

    @Override
    public List<Pair<Long, TrackedObject>> fieldHistory(String fieldID) {
        List<Map<String, Object>> updates = jdbcHelper.find("SELECT fu.*, o.clazz FROM field_updates fu LEFT OUTER JOIN objects o ON fu.ref = o.id WHERE fu.field_id = ? ORDER BY event_id DESC", fieldID);
        List<Pair<Long, TrackedObject>> changes = Lists.newArrayList();
        for (Map<String, Object> update : updates) {
            Long time = (Long) update.get("EVENT_ID");
            String value = (String) update.get("VALUE");
            String ref = (String) update.get("REF");
            String className = (String) update.get("CLAZZ");
            TrackedObject obj = new TrackedObject(className, value, ref);
            Pair<Long, TrackedObject> timeAndValue = new Pair<Long, TrackedObject>(time, obj);
            changes.add(timeAndValue);
        }
        return changes;
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

    @Override
    public Connection getConnection() {
        Connection c;
        try {
            c = DriverManager.getConnection(connString, "sa", "");
        } catch (SQLException e) {
            throw new SidratProcessingException("Could not obtain connection to hsqldb store", e);
        }
        return c;
    }

    private SidratExecutionEvent getEvent(Map<String, Object> row) {
        if (row == null)
            return null;
        Integer lineNumber = (Integer) row.get("LINENUMBER");
        Long time = (Long) row.get("ID");
        Long methodEntryTime = (Long) row.get("METHODENTRY_ID");
        String objectInstanceID = (String) row.get("OBJECT_ID");
        Long threadID = (Long) row.get("THREAD_ID");
        String threadName = findThread(threadID);
        String className = (String) row.get("CLAZZ");
        String methodName = (String) row.get("METHOD");
        TrackedObject trackedObject = new TrackedObject(className, null, objectInstanceID);
        ExecutionLocation executionLocation = new ExecutionLocation(trackedObject, threadName, className, methodName);
        SidratMethodEntryEvent methodEntryEvent = new SidratMethodEntryEvent(methodEntryTime, executionLocation, threadID, threadName);
        SidratExecutionEvent event = new SidratExecutionEvent(time, methodEntryEvent, lineNumber);
        return event;
    }

    @Override
    public Map<String, CapturedLocalVariableValue> locals(Long time) {
        Map<String, Object> currentLine = jdbcHelper.first("SELECT id, methodentry_id, lineNumber FROM executions WHERE id = ?", time);
        if (currentLine == null)
            return Collections.emptyMap();
        Integer lineNumberCurrent = (Integer) currentLine.get("LINENUMBER");
        Long methodEntryId = (Long) currentLine.get("METHODENTRY_ID");
        Map<String, Object> currentMethod = jdbcHelper.first("SELECT method_id FROM method_entries WHERE id = ?", methodEntryId);
        Long methodId = (Long) currentMethod.get("METHOD_ID");
        Map<String, Object> methodDefinition = jdbcHelper.first("SELECT c.name AS class_name, name as method_name FROM methods LEFT OUTER JOIN classes c ON methods.class_id = c.id WHERE id = ?", methodId);
        String className = (String) methodDefinition.get("CLASS_NAME");
        String methodName = (String) methodDefinition.get("METHOD_NAME");
        List<Map<String, Object>> variables = jdbcHelper.query("SELECT * FROM variables WHERE rangeStart <= ? AND rangeEnd >= ? AND method_id = ?", lineNumberCurrent, lineNumberCurrent, methodId);
        Map<String, CapturedLocalVariableValue> locals = Maps.newHashMap();
        for (Map<String, Object> var : variables) {
            String name = (String) var.get("VARIABLE_NAME");
            Long id = (Long) var.get("ID");
            Integer rangeStart = (Integer) var.get("RANGESTART");
            Integer rangeEnd = (Integer) var.get("RANGEEND");
            TrackedVariable variable = new TrackedVariable(className, methodName, name, new Pair<Integer, Integer>(rangeStart, rangeEnd));
            Long mostRecentUpdate = (Long) jdbcHelper.first("SELECT MAX(event_id) AS EVENT_ID FROM variable_updates WHERE variable_id = ? AND event_id <= ?", id, time).get("EVENT_ID");
            if (mostRecentUpdate == null) {
                locals.put(name, null);
            } else {
                Map<String, Object> update = jdbcHelper.first("SELECT vu.value, vu.ref, o.clazz FROM variable_updates vu LEFT JOIN objects o ON vu.ref = o.id WHERE vu.event_id = ? AND vu.variable_id=?", mostRecentUpdate, id);
                String val = (String) update.get("VALUE");
                String ref = (String) update.get("REF");
                String varClass = (String) update.get("CLAZZ");
                TrackedObject obj = new TrackedObject(varClass, val, ref);
                CapturedLocalVariableValue value = new CapturedLocalVariableValue(time, variable, obj);
                locals.put(name, value);
            }
        }
        return locals;
    }

    @Override
    public List<Pair<Long, TrackedObject>> localVariableHistory(String localVariableID) {
        List<Map<String, Object>> updates = jdbcHelper.find("SELECT vu.*, v.clazz FROM variable_updates vu LEFT OUTER JOIN variables v ON vu.variable_id = v.id WHERE v.internal_name = ? ORDER BY event_id DESC", localVariableID);
        List<Pair<Long, TrackedObject>> changes = Lists.newArrayList();
        for (Map<String, Object> update : updates) {
            Long time = (Long) update.get("EVENT_ID");
            String value = (String) update.get("VALUE");
            String ref = (String) update.get("REF");
            String className = (String) update.get("CLAZZ");
            TrackedObject obj = new TrackedObject(className, value, ref);
            Pair<Long, TrackedObject> timeAndValue = new Pair<Long, TrackedObject>(time, obj);
            changes.add(timeAndValue);
        }
        return changes;
    }
}
