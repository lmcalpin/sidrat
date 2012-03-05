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
import com.sidrat.event.store.EventStore;
import com.sidrat.util.Jdbc;
import com.sidrat.util.JdbcConnectionProvider;

// This implementation of EventStore uses HSQLDB to store events and run state information (changes to variables and fields).  It
// is highly unlikely that this will work even remotely well for any "real world" project.  It is only intended to support debugging 
// small projects as a proof of concept.
public class HsqldbEventStore implements EventStore, JdbcConnectionProvider {
    private String connString;
    private Jdbc jdbcHelper = new Jdbc(this);

    static {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (Exception e) {
            throw new SidratProcessingException("Could not load hsqldb driver", e);
        }
    }

    public HsqldbEventStore(String filename) {
        this.connString = "jdbc:hsqldb:file:" + filename + "/sidrat";
        init();
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

    // empty the database
    private void init() {
        jdbcHelper.update("DROP TABLE IF EXISTS threads");
        jdbcHelper.update("DROP TABLE IF EXISTS classes");
        jdbcHelper.update("DROP TABLE IF EXISTS methods");
        jdbcHelper.update("DROP TABLE IF EXISTS executions");
        jdbcHelper.update("DROP TABLE IF EXISTS variables");
        jdbcHelper.update("DROP TABLE IF EXISTS variable_updates");
        jdbcHelper.update("DROP TABLE IF EXISTS fields");
        jdbcHelper.update("DROP TABLE IF EXISTS field_updates");
        jdbcHelper.update("DROP TABLE IF EXISTS objects");

        jdbcHelper.update("CREATE TABLE threads(id BIGINT, name VARCHAR(255))");
        jdbcHelper.update("CREATE TABLE classes(id BIGINT IDENTITY, name VARCHAR(255))");
        jdbcHelper.update("CREATE TABLE methods(id BIGINT IDENTITY, class_id BIGINT, name VARCHAR(255))");
        jdbcHelper.update("CREATE TABLE executions(id BIGINT, object_id BIGINT, thread_id BIGINT, method_id BIGINT, entering BIT, lineNumber INTEGER)");
        jdbcHelper.update("CREATE TABLE variables(id BIGINT IDENTITY, variable_name VARCHAR(255), rangeStart INTEGER, rangeEnd INTEGER)");
        jdbcHelper.update("CREATE TABLE variable_updates(event_id BIGINT, variable_id BIGINT, value LONGVARCHAR, ref BIT)");
        jdbcHelper.update("CREATE TABLE objects(id BIGINT, clazz VARCHAR(255))");
        jdbcHelper.update("CREATE TABLE fields(id BIGINT IDENTITY, object_id BIGINT, field_name VARCHAR(255))");
        jdbcHelper.update("CREATE TABLE field_updates(event_id BIGINT, field_id BIGINT, value LONGVARCHAR, ref BIT)");
    }

    private Map<String, Long> persistedObjects = new HashMap<String, Long>();
    private Map<String, Long> persistedFields = new HashMap<String, Long>();
    private Map<String, Long> persistedVariables = new HashMap<String, Long>();
    private List<Long> persistedThreads = new ArrayList<Long>();
    private Map<String, Long> classes = new HashMap<String, Long>();
    private Map<String, Long> methods = new HashMap<String, Long>();

    @Override
    public void store(SidratLocalVariableEvent event) {
        if (!persistedVariables.keySet().contains(event.getUniqueID())) {
            Long id = jdbcHelper.insert("INSERT INTO variables(variable_name,rangeStart,rangeEnd) VALUES(?,?,?)", event.getVariableName(), event.getVariableValidityRange().getValue1(), event.getVariableValidityRange().getValue2());
            persistedVariables.put(event.getUniqueID(), id);
        }
        Long variableID = persistedVariables.get(event.getUniqueID());
        Long eventID = event.getTime();
        jdbcHelper.insert("INSERT INTO variable_updates VALUES(?, ?, ?, ?)", eventID, variableID, String.valueOf(event.getValue()), 0);
    }

    @Override
    public void store(SidratFieldChangedEvent event) {
        String ownerClassName = event.getOwnerClass().getName();
        Long ownerID = event.getObjectInstanceID();
        String variableUuid = ownerClassName + ":" + ownerID;
        if (!persistedObjects.keySet().contains(ownerID)) {
            jdbcHelper.insert("INSERT INTO objects VALUES(?,?)", ownerID, ownerClassName);
        }
        if (!persistedFields.keySet().contains(variableUuid)) {
            Long id = jdbcHelper.insert("INSERT INTO fields(object_id,field_name) VALUES(?,?)", ownerID, event.getVariableName());
            persistedFields.put(variableUuid, id);
        }
        Long fieldID = persistedFields.get(variableUuid);
        Long eventID = event.getTime();
        jdbcHelper.insert("INSERT INTO field_updates VALUES(?, ?, ?, ?)", eventID, fieldID, event.getValue(), 0);
    }

    @Override
    public void store(SidratExecutionEvent event) {
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
        jdbcHelper.insert("INSERT INTO executions VALUES(?, ?, ?, ?, ?, ?)", event.getTime(), event.getObjectInstanceID(), event.getThreadID(), methodID, event.isEntering(), event.getLineNumber());
        event.print(System.out);
    }

    @Override
    public void close() {
        jdbcHelper.update("SHUTDOWN");
    }

}
