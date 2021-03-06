package com.sidrat.event.store.hsqldb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sidrat.SidratProcessingException;
import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.SidratFieldChangedEvent;
import com.sidrat.event.SidratLocalVariableEvent;
import com.sidrat.event.SidratMethodEntryEvent;
import com.sidrat.event.SidratMethodExitEvent;
import com.sidrat.event.store.EventStore;
import com.sidrat.event.tracking.TrackedObject;
import com.sidrat.util.Jdbc;
import com.sidrat.util.JdbcConnectionProvider;

/**
 * This implementation of EventStore uses HSQLDB to store events and run state information (changes to variables and fields).
 * It is highly unlikely that this will work even remotely well for any "real world" project. It is only intended to support
 * debugging small projects as a proof of concept.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class HsqldbEventStore implements EventStore, JdbcConnectionProvider {
    static {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (Exception e) {
            throw new SidratProcessingException("Could not load hsqldb driver", e);
        }
    }

    private String connString;

    private Jdbc jdbcHelper = new Jdbc(this);

    private Map<String, String> persistedObjects = new HashMap<>();
    private Map<String, Long> persistedFields = new HashMap<>();
    private Map<String, Long> persistedVariables = new HashMap<>();
    private List<Long> persistedThreads = new ArrayList<>();
    private Map<String, Long> classes = new HashMap<>();
    private Map<String, Long> methods = new HashMap<>();

    public HsqldbEventStore(String filename) {
        this.connString = "jdbc:hsqldb:file:" + filename + "/sidrat";
        init();
    }

    @Override
    public void close() {
        jdbcHelper.update("SHUTDOWN");
    }

    private String foundObject(TrackedObject obj) {
        if (obj == null)
            return null;
        String ownerClassName = obj.getClassName();
        String objectID = obj.getUniqueID();
        if (!persistedObjects.keySet().contains(objectID)) {
            jdbcHelper.insert("INSERT INTO objects VALUES(?,?)", objectID, ownerClassName);
            persistedObjects.put(objectID, ownerClassName);
        }
        return objectID;
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

    // empty the database
    private void init() {
        jdbcHelper.update("DROP TABLE IF EXISTS threads");
        jdbcHelper.update("DROP TABLE IF EXISTS classes");
        jdbcHelper.update("DROP TABLE IF EXISTS methods");
        jdbcHelper.update("DROP TABLE IF EXISTS method_entries");
        jdbcHelper.update("DROP TABLE IF EXISTS method_args");
        jdbcHelper.update("DROP TABLE IF EXISTS method_exits");
        jdbcHelper.update("DROP TABLE IF EXISTS executions");
        jdbcHelper.update("DROP TABLE IF EXISTS variables");
        jdbcHelper.update("DROP TABLE IF EXISTS variable_updates");
        jdbcHelper.update("DROP TABLE IF EXISTS fields");
        jdbcHelper.update("DROP TABLE IF EXISTS field_updates");
        jdbcHelper.update("DROP TABLE IF EXISTS objects");

        jdbcHelper.update("CREATE TABLE threads(id BIGINT, name VARCHAR(255))");
        jdbcHelper.update("CREATE TABLE classes(id BIGINT IDENTITY, name VARCHAR(255))");
        jdbcHelper.update("CREATE TABLE methods(id BIGINT IDENTITY, class_id BIGINT, name VARCHAR(255))");
        jdbcHelper.update("CREATE TABLE method_entries(id BIGINT, object_id VARCHAR(255), thread_id BIGINT, method_id BIGINT)");
        jdbcHelper.update("CREATE TABLE method_args(id BIGINT, arg_name LONGVARCHAR, arg_value LONGVARCHAR)");
        jdbcHelper.update("CREATE TABLE method_exits(id BIGINT, methodentry_id BIGINT, ref VARCHAR(255), value LONGVARCHAR)");
        jdbcHelper.update("CREATE TABLE executions(id BIGINT, methodentry_id BIGINT, lineNumber INTEGER)");
        jdbcHelper.update("CREATE TABLE variables(id BIGINT IDENTITY, internal_name VARCHAR(255), variable_name VARCHAR(255), method_id BIGINT, rangeStart INTEGER, rangeEnd INTEGER, clazz VARCHAR(255))");
        jdbcHelper.update("CREATE TABLE variable_updates(event_id BIGINT, variable_id BIGINT, value LONGVARCHAR, ref VARCHAR(255))");
        jdbcHelper.update("CREATE TABLE objects(id VARCHAR(255), clazz VARCHAR(255))");
        jdbcHelper.update("CREATE TABLE fields(id BIGINT IDENTITY, object_id VARCHAR(255), field_name VARCHAR(255))");
        jdbcHelper.update("CREATE TABLE field_updates(event_id BIGINT, field_id VARCHAR(255), value LONGVARCHAR, ref VARCHAR(255))");
    }

    @Override
    public void store(SidratExecutionEvent event) {
        jdbcHelper.insert("INSERT INTO executions VALUES(?, ?, ?)", event.getTime(), event.getMethodEntryTime(), event.getLineNumber());
    }

    @Override
    public void store(SidratFieldChangedEvent event) {
        String ownerID = foundObject(event.getOwner());
        String objectID = foundObject(event.getTrackedValue());
        String variableUuid = String.valueOf(objectID);
        if (!persistedFields.keySet().contains(variableUuid)) {
            Long id = jdbcHelper.insert("INSERT INTO fields(object_id,field_name) VALUES(?,?)", ownerID, event.getVariableName());
            persistedFields.put(variableUuid, id);
        }
        // Long fieldID = persistedFields.get(variableUuid);
        Long eventID = event.getTime();
        jdbcHelper.insert("INSERT INTO field_updates VALUES(?, ?, ?, ?)", eventID, ownerID, event.getTrackedValue().getValueAsString(), objectID);
    }

    @Override
    public void store(SidratLocalVariableEvent event) {
        if (!persistedVariables.keySet().contains(event.getUniqueID())) {
            Long methodId = (Long) jdbcHelper.first("SELECT id FROM methods WHERE class_id = (SELECT id FROM classes WHERE name=?) AND name = ?", event.getClassName(), event.getMethodName()).get("ID");
            Long id = jdbcHelper.insert("INSERT INTO variables(internal_name, variable_name, method_id, rangeStart, rangeEnd, clazz) VALUES(?,?,?,?,?,?)", event.getUniqueID(), event.getVariableName(), methodId,
                    event.getVariableValidityRange().getValue1(), event.getVariableValidityRange().getValue2(), event.getTrackedValue().getClassName());
            persistedVariables.put(event.getUniqueID(), id);
        }
        Long variableID = persistedVariables.get(event.getUniqueID());
        Long eventID = event.getTime();
        String objectID = foundObject(event.getTrackedValue());
        String value = event.getTrackedValue() != null ? event.getTrackedValue().getValueAsString() : null;
        jdbcHelper.insert("INSERT INTO variable_updates VALUES(?, ?, ?, ?)", eventID, variableID, value, objectID);
    }

    @Override
    public void store(SidratMethodEntryEvent event) {
        // store the object that owns the method in which we are executing
        // TODO: currently this value is null if the method is static, but we probably want to capture the class anyway?
        String objectInstanceID = null;
        TrackedObject executionContext = event.getExecutionContext().getObject();
        if (executionContext != null) {
            objectInstanceID = foundObject(executionContext);
        }
        if (!persistedThreads.contains(event.getThreadID())) {
            jdbcHelper.update("INSERT INTO threads VALUES(?,?)", event.getThreadID(), event.getThreadName());
            persistedThreads.add(event.getThreadID());
        }
        int classHash = event.getClassName().hashCode();
        if (!classes.containsKey(event.getClassName())) {
            Long id = jdbcHelper.insert("INSERT INTO classes(name) VALUES(?)", event.getClassName());
            classes.put(event.getClassName(), id);
        }
        Long classID = classes.get(event.getClassName());
        String combinedMethodName = event.getClassName() + "." + event.getMethodName();
        if (!methods.containsKey(classHash)) {
            Long id = jdbcHelper.insert("INSERT INTO methods(class_id,name) VALUES(?,?)", classID, event.getMethodName());
            methods.put(combinedMethodName, id);
        }
        Long methodID = methods.get(combinedMethodName);
        jdbcHelper.insert("INSERT INTO method_entries VALUES(?, ?, ?, ?)", event.getTime(), objectInstanceID, event.getThreadID(), methodID);
    }

    @Override
    public void store(SidratMethodExitEvent event) {
        String objectID = foundObject(event.getReturns());
        String value = event.getReturns() != null ? event.getReturns().getValueAsString() : null;
        jdbcHelper.insert("INSERT INTO method_exits VALUES(?, ?, ?, ?)", event.getTime(), event.getMethodEntryTime(), objectID, value);
    }

}
