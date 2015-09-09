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
    private String connString;
    private Jdbc jdbcHelper = new Jdbc(this);

    private static final String EVENTS_QUERY = "SELECT e.id, me.thread_id, m.name AS method, m.class_id, c.name AS clazz, e.lineNumber, me.object_id FROM executions e LEFT JOIN method_entries me ON e.methodentry_id = me.id LEFT JOIN methods m ON me.method_id=m.id LEFT JOIN classes c ON m.class_id = c.id ";

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
    public Map<String,CapturedLocalVariableValue> locals(Long time) {
        Long methodEntryTime = (Long) jdbcHelper.first("SELECT MAX(id) AS t FROM method_entries WHERE id <= ?", time).get("T");
        Map<String,Object> methodEntrypoint = jdbcHelper.first("SELECT id, lineNumber FROM executions WHERE id = ?", methodEntryTime);
        Map<String,Object> currentLine = jdbcHelper.first("SELECT id, lineNumber FROM executions WHERE id = ?", time);
        if (currentLine == null)
            return Collections.emptyMap();
        Integer lineNumberStart = (Integer) methodEntrypoint.get("LINENUMBER");
        Integer lineNumberCurrent = (Integer) currentLine.get("LINENUMBER");
        List<Map<String, Object>> variables = jdbcHelper.query("SELECT * FROM variables WHERE rangeStart <= ? AND rangeEnd >= ?", lineNumberCurrent, lineNumberCurrent);
        Map<String,CapturedLocalVariableValue> locals = Maps.newHashMap();
        for (Map<String,Object> var : variables) {
            String name = (String)var.get("VARIABLE_NAME");
            Long id = (Long)var.get("ID");
            String uuid = (String)var.get("UUID");
            Integer rangeStart = (Integer)var.get("RANGESTART");
            Integer rangeEnd = (Integer)var.get("RANGEEND");
            TrackedVariable variable = new TrackedVariable(uuid, name, new Pair<Integer,Integer>(rangeStart, rangeEnd));
            Long mostRecentUpdate = (Long)jdbcHelper.first("SELECT MAX(event_id) AS EVENT_ID FROM variable_updates WHERE variable_id = ? AND event_id <= ?", id, time).get("EVENT_ID");
            if (mostRecentUpdate == null) {
                locals.put(name, null);
            } else {
                Map<String,Object> update = jdbcHelper.first("SELECT vu.value, vu.ref, o.clazz FROM variable_updates vu LEFT JOIN objects o ON vu.ref = o.id WHERE vu.event_id = ? AND vu.variable_id=?", mostRecentUpdate, id);
                String val = (String) update.get("VALUE");
                Long ref = (Long) update.get("REF");
                String className = (String) update.get("CLAZZ");
                TrackedObject obj = new TrackedObject(className, val, ref);
                CapturedLocalVariableValue value = new CapturedLocalVariableValue(time, variable, obj);
                locals.put(name, value);
            }
        }
        return locals;
    }
    
    @Override
    public Map<String,CapturedFieldValue> eval(Long time, Long objectID) {
        List<Map<String, Object>> fields = jdbcHelper.query("SELECT * FROM fields WHERE object_id = ?", objectID);
        Map<String,CapturedFieldValue> values = Maps.newHashMap();
        for (Map<String,Object> var : fields) {
            String fieldName = (String)var.get("FIELD_NAME");
            Map<String, Object> update = jdbcHelper.first("SELECT fu.*, o.clazz FROM field_updates fu LEFT JOIN objects o ON fu.ref = o.id WHERE fu.field_id = ? AND fu.event_id <= ? ORDER BY event_id DESC", objectID, time);
            if (update != null) {
                String value = (String) update.get("VALUE");
                Long ref = (Long) update.get("REF");
                String className = (String) update.get("CLAZZ");
                TrackedObject obj = new TrackedObject(className, value, ref);
                CapturedFieldValue fieldValue = new CapturedFieldValue(time, objectID, obj);
                values.put(fieldName, fieldValue);
            }
        }
        return values;
    }
    
    @Override
    public List<Pair<Long,TrackedObject>> fieldHistory(Long fieldID) {
        List<Map<String, Object>> updates = jdbcHelper.find("SELECT fu.*, o.clazz FROM field_updates fu LEFT OUTER JOIN objects o ON fu.ref = o.id WHERE fu.field_id = ? ORDER BY event_id DESC", fieldID);
        List<Pair<Long, TrackedObject>> changes = Lists.newArrayList();
        for (Map<String,Object> update : updates) {
            Long time = (Long) update.get("EVENT_ID");
            String value = (String) update.get("VALUE");
            Long ref = (Long) update.get("REF");
            String className = (String) update.get("CLAZZ");
            TrackedObject obj = new TrackedObject(className, value, ref);
            Pair<Long,TrackedObject> timeAndValue = new Pair<Long, TrackedObject>(time, obj);
            changes.add(timeAndValue);
        }
        return changes;
    }
    
    @Override
    public List<Pair<Long,TrackedObject>> localVariableHistory(String localVariableID) {
        List<Map<String, Object>> updates = jdbcHelper.find("SELECT vu.*, v.clazz FROM variable_updates vu LEFT OUTER JOIN variables v ON vu.variable_id = v.id WHERE v.uuid = ? ORDER BY event_id DESC", localVariableID);
        List<Pair<Long, TrackedObject>> changes = Lists.newArrayList();
        for (Map<String,Object> update : updates) {
            Long time = (Long) update.get("EVENT_ID");
            String value = (String) update.get("VALUE");
            Long ref = (Long) update.get("REF");
            String className = (String) update.get("CLAZZ");
            TrackedObject obj = new TrackedObject(className, value, ref);
            Pair<Long,TrackedObject> timeAndValue = new Pair<Long, TrackedObject>(time, obj);
            changes.add(timeAndValue);
        }
        return changes;
    }
    
    @Override
    public List<SidratExecutionEvent> executions(String className, String method, int lineNumber) {
        List<Map<String, Object>> rows = jdbcHelper.find(EVENTS_QUERY + "WHERE clazz=? AND method=? AND lineNumber=?", className, method, lineNumber);
        List<SidratExecutionEvent> events = Lists.newArrayList();
        for (Map<String,Object> row : rows) {
            events.add(getEvent(row));
        }
        return events;
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
        Long methodEntryTime = (Long) row.get("METHODENTRY_ID");
        Long objectInstanceID = (Long) row.get("OBJECT_ID");
        Long threadID = (Long) row.get("THREAD_ID");
        String threadName = findThread(threadID);
        String className = (String) row.get("CLAZZ");
        String methodName = (String) row.get("METHOD");
        TrackedObject trackedObject = new TrackedObject(className, objectInstanceID);
        ExecutionLocation executionLocation = new ExecutionLocation(trackedObject, className, methodName);
        Map<String,Object> arguments = Collections.emptyMap(); // TODO: fill this map!
        SidratMethodEntryEvent methodEntryEvent = new SidratMethodEntryEvent(methodEntryTime, executionLocation, threadID, threadName, arguments);
        SidratExecutionEvent event = new SidratExecutionEvent(time, methodEntryEvent, lineNumber);
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
