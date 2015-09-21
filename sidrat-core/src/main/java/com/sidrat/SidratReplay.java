package com.sidrat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.store.EventReader;
import com.sidrat.event.store.hsqldb.HsqldbEventReader;
import com.sidrat.event.tracking.CapturedFieldValue;
import com.sidrat.event.tracking.CapturedLocalVariableValue;
import com.sidrat.event.tracking.ExecutionLocation;
import com.sidrat.event.tracking.TrackedObject;
import com.sidrat.event.tracking.ValueTracker;
import com.sidrat.util.Logger;
import com.sidrat.util.Pair;
import com.sidrat.util.Tuple3;

import org.apache.commons.io.FileUtils;

/**
 * Debugger for replaying Sidrat recordings.
 * 
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class SidratReplay {
    private static final Logger logger = new Logger();

    private EventReader eventReader;
    private File sourceDir;
    private BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
    private PrintStream out = System.out;

    private boolean continueProcessing = true;
    private SidratExecutionEvent event;
    private List<String> breakpoints = Lists.newArrayList();
    private Multimap<ExecutionLocation, String> variableWatches = ArrayListMultimap.create();
    private Multimap<String, String> fieldWatches = ArrayListMultimap.create();

    private List<String> invalidSourceFiles = Lists.newArrayList();

    public static interface Condition {
        public boolean execute();
    }

    public SidratReplay(String fileName) {
        this.eventReader = new HsqldbEventReader(fileName);
    }

    public SidratReplay(EventReader reader) {
        this.eventReader = reader;
    }

    public SidratReplay withSource(String source) {
        this.sourceDir = new File(source);
        validateDir(this.sourceDir);
        return this;
    }

    private void validateFile(File file) {
        if (!file.exists())
            throw new SidratProcessingException("File not found: " + file.getName());
    }

    private void validateDir(File file) {
        validateFile(file);
        if (!file.isDirectory())
            throw new SidratProcessingException("Invalid path: " + file.getName());
    }

    public void replay() {
        readNext();
        if (event == null) {
            out.println("Data file is empty");
            System.exit(-1);
        }
        Condition automaticExecution = null;
        while (continueProcessing) {
            try {
                if (automaticExecution != null) {
                    if (!automaticExecution.execute()) {
                        automaticExecution = null;
                    }
                } else {
                    System.out.print("> ");
                    String input = stdin.readLine();
                    String[] parsedLine = input.split("\\s");
                    String command = parsedLine[0];
                    switch (command) {
                    case "n":
                        readNext();
                        break;
                    case "p":
                        prevEvent();
                        break;
                    case "g":
                        {
                            String eventIDStr = parsedLine[1];
                            int eventID = Integer.parseInt(eventIDStr);
                            gotoEvent(eventID);
                        }
                        break;
                    case "b": // set a breakpoint
                    case "break": // set a breakpoint
                        {
                            if (parsedLine.length == 1) {
                                int i = 1;
                                for (String breakpoint : breakpoints) {
                                    out.println("" + i + ": " + breakpoint);
                                }
                            } else {
                                String breakpoint = parsedLine[1];
                                Tuple3<String, String, Integer> split = split(breakpoint);
                                if (eventReader.executions(split.getValue1(), split.getValue2(), split.getValue3()).size() > 0) {
                                    breakpoints.add(breakpoint);
                                }
                            }
                        }
                        break;
                    case "del": // delete a breakpoint
                    case "rem":
                        {
                            if (parsedLine.length != 2) {
                                out.println("Breakpoint # is required.");
                            } else if (parsedLine[1].indexOf(':') >= 0) {
                                // breakpoint?
                                String breakpoint = parsedLine[1];
                                int index = Integer.parseInt(breakpoint);
                                breakpoints.remove(index);
                            } else {
                                // must be a watch
                                removeWatch(parsedLine);
                            }
                        }
                        break;
                    case "c": // continue
                    case "cont": // continue
                        {
                            automaticExecution = new Condition() {
                                @Override
                                public boolean execute() {
                                    readNext();
                                    if (SidratReplay.this.event == null)
                                        return false;
                                    String breakpointName = SidratReplay.this.event.asBreakpointID();
                                    if (breakpointName != null) {
                                        if (breakpoints.contains(breakpointName)) {
                                            out.println("Reached breakpoint: " + breakpointName);
                                            return false;
                                        }
                                    }
                                    return true;
                                }
                            };
                        }
                        break;
                    case "v": // show variables
                    case "var":
                        {
                            out.println(" - instance variables: ");
                            Map<String, CapturedFieldValue> instanceVariables = eval(this.event.getExecutionContext().getObject());
                            printFields(instanceVariables);
                            out.println(" - local variables: ");
                            Map<String, CapturedLocalVariableValue> localVariables = locals();
                            printLocalVariables(localVariables);
                        }
                        break;
                    case "h":
                    case "history":
                        {
                            showHistory(parsedLine);
                        }
                        break;
                    case "w": // watch
                    case "watch":
                        {
                            watchVariable(parsedLine);
                        }
                        break;
                    case "q":
                    case "quit":
                        continueProcessing = false;
                        break;
                    default:
                        out.println("Invalid command: " + command);
                    }
                }
            } catch (Exception e) {
                logger.severe("Error processing Sidrat command", e);
            }
        }
        out.println("Done.");
    }

    private void watchVariable(String[] parsedLine) {
        if (parsedLine.length != 2) {
            out.println("Variable is required.");
        } else {
            String variable = parsedLine[1];
            Map<String, CapturedFieldValue> instanceVariables = eval(this.event.getExecutionContext().getObject());
            Map<String, CapturedLocalVariableValue> localVariables = locals();
            if (instanceVariables.get(variable) != null) {
                fieldWatches.put(this.event.getExecutionContext().getClassName(), variable);
            } else if (localVariables != null) {
                variableWatches.put(this.event.getExecutionContext(), variable);
            } else {
                out.println("Variable " + variable + " not found");
            }
        }
    }

    private void removeWatch(String[] parsedLine) {
        if (parsedLine.length != 2) {
            out.println("Variable is required.");
        } else {
            String variable = parsedLine[1];
            Map<String, CapturedFieldValue> instanceVariables = eval(this.event.getExecutionContext().getObject());
            Map<String, CapturedLocalVariableValue> localVariables = locals();
            if (instanceVariables.get(variable) != null) {
                fieldWatches.remove(this.event.getExecutionContext().getClassName(), variable);
            } else if (localVariables != null) {
                variableWatches.remove(this.event.getExecutionContext(), variable);
            } else {
                out.println("Variable " + variable + " not found");
            }
        }
    }

    private void showHistory(String[] parsedLine) {
        if (parsedLine.length != 2) {
            out.println("Variable is required.");
        } else {
            String variable = parsedLine[1];
            Map<String, CapturedFieldValue> instanceVariables = eval(this.event.getExecutionContext().getObject());
            Map<String, CapturedLocalVariableValue> localVariables = locals();
            boolean found = false;
            if (instanceVariables.get(variable) != null) {
                CapturedFieldValue cfv = instanceVariables.get(variable);
                if (cfv != null) {
                    found = true;
                    print(eventReader.fieldHistory(cfv.getOwnerID()));
                }
            } else if (localVariables != null) {
                CapturedLocalVariableValue clvv = localVariables.get(variable);
                if (clvv != null) {
                    found = true;
                    print(eventReader.localVariableHistory(clvv.getVariable().getId()));
                }
            } 
            if (!found) {
                out.println("Variable " + variable + " not found");
            }
        }
    }

    private void printFields(Map<String, CapturedFieldValue> vals) {
        for (String key : vals.keySet()) {
            print(key, vals.get(key));
        }
    }
    
    private void printLocalVariables(Map<String, CapturedLocalVariableValue> vals) {
        for (String key : vals.keySet()) {
            print(key, vals.get(key));
        }
    }
    
    private void print(String key, ValueTracker value) {
        if (value == null) {
            out.println(key + " is undefined");
        } else {
            print(key, value.getCurrentValue());
        }
    }
    
    private void print(String key, TrackedObject value) {
        if (value == null) {
            out.println(key + " = null");
        } else {
            out.println(key + ":" + value.getClassName() + " = " + value.getValueAsString());
        }
    }

    public void readNext() {
        if (event == null) {
            event = eventReader.findFirst();
        } else {
            SidratExecutionEvent nextEvent = eventReader.findNext(event);
            if (nextEvent == null) {
                out.println("Reached the end of the recording.");
                continueProcessing = false;
                return;
            } else {
                event = nextEvent;
            }
        }
        print(event);
    }

    public SidratExecutionEvent gotoEvent(int id) {
        event = eventReader.find(new Long(id));
        print(event);
        return event;
    }

    public SidratExecutionEvent gotoEvent(Long id) {
        event = eventReader.find(id);
        print(event);
        return event;
    }
    
    public void prevEvent() {
        SidratExecutionEvent prevEvent = eventReader.findPrev(event);
        if (prevEvent != null)
            event = prevEvent;
        else
            return;
        print(event);
    }

    public Map<String, CapturedLocalVariableValue> locals() {
        if (event == null) {
            return Maps.newHashMap();
        }
        return eventReader.locals(event.getTime());
    }

    public Map<String, CapturedFieldValue> eval(TrackedObject obj) {
        if (event == null || obj == null) {
            return Maps.newHashMap();
        }
        return eventReader.eval(event.getTime(), obj.getUniqueID());
    }

    public void print() {
        print(event);
    }

    public void print(SidratExecutionEvent event) {
        if (event == null)
            return;
        event.print(System.out);
        String sourceCode = lookupSourceCode(event);
        if (sourceCode != null)
            out.println(sourceCode);
        // if we are watching variables, print their current values
        if (fieldWatches.size() > 0 || variableWatches.size() > 0) {
            out.println("-- watched variables:");
        }
        if (fieldWatches.size() > 0) {
            Collection<String> variables = fieldWatches.get(this.event.getExecutionContext().getClassName());
            Map<String, CapturedFieldValue> instanceVariables = eval(this.event.getExecutionContext().getObject());
            for (String variable : variables) {
                print(variable, instanceVariables.get(variable));
            }
        }
        if (variableWatches.size() > 0) {
            Collection<String> variables = variableWatches.get(this.event.getExecutionContext());
            Map<String, CapturedLocalVariableValue> localVariables = locals();
            for (String variable : variables) {
                print(variable, localVariables.get(variable));
            }
        }
    }
    
    public void print(List<Pair<Long,TrackedObject>> valueChanges) {
        for (Pair<Long,TrackedObject> valueChange : valueChanges) {
            out.println(" " + valueChange.getValue1() + ": " + valueChange.getValue2().getValueAsString());
        }
    }

    public Tuple3<String, String, Integer> split(String breakpoint) {
        String[] split = breakpoint.split(":");
        Integer lineNumber = Integer.parseInt(split[1]);
        int lastDot = split[0].lastIndexOf('.');
        String method = split[0].substring(lastDot + 1);
        String className = split[0].substring(0, lastDot);
        return new Tuple3<String, String, Integer>(className, method, lineNumber);
    }

    private String lastFile;
    private List<String> lines;

    @SuppressWarnings("unchecked")
    public String lookupSourceCode(SidratExecutionEvent event) {
        if (sourceDir == null)
            return null;
        String rootPath = sourceDir.getAbsolutePath();
        if (rootPath.endsWith("/")) {
            rootPath = rootPath.substring(0, rootPath.length() - 1);
        }
        String eventClassNameAsPath = event.getClassName().replaceAll("\\.", "/");
        if (eventClassNameAsPath.contains("$")) {
            eventClassNameAsPath = eventClassNameAsPath.substring(0, eventClassNameAsPath.indexOf('$'));
        }
        String filePath = eventClassNameAsPath + ".java";
        String fullPath = rootPath + "/" + filePath;
        if (!fullPath.equalsIgnoreCase(lastFile)) {
            lastFile = fullPath;
            try {
                lines = FileUtils.readLines(new File(fullPath));
            } catch (IOException e) {
                if (invalidSourceFiles.contains(filePath))
                    return null;
                invalidSourceFiles.add(filePath);
                logger.warning("Could not read: " + filePath);
                return null;
            }
        }
        return lines.get(event.getLineNumber() - 1);
    }
}
