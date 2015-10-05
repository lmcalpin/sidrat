package com.sidrat.cli;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.sidrat.SidratReplay;
import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.store.EventReader;
import com.sidrat.event.tracking.CapturedFieldValue;
import com.sidrat.event.tracking.CapturedLocalVariableValue;
import com.sidrat.event.tracking.ExecutionLocation;
import com.sidrat.event.tracking.TrackedObject;
import com.sidrat.event.tracking.ValueTracker;
import com.sidrat.util.Logger;
import com.sidrat.util.Pair;
import com.sidrat.util.Tuple3;

/**
 * Debugger for replaying Sidrat recordings from the command line.
 * 
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class SidratReplayCli extends SidratReplay {
    private static final Logger logger = new Logger();

    private boolean continueProcessing = true;
    private List<String> breakpoints = Lists.newArrayList();
    private Multimap<ExecutionLocation, String> variableWatches = ArrayListMultimap.create();
    private Multimap<String, String> fieldWatches = ArrayListMultimap.create();
    private BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
    private PrintStream out = System.out;

    public static interface Condition {
        public boolean execute();
    }

    public SidratReplayCli(EventReader reader) {
        super(reader);
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
                                    if (SidratReplayCli.this.event == null)
                                        return false;
                                    String breakpointName = SidratReplayCli.this.event.asBreakpointID();
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

    private void print(String key, TrackedObject value) {
        if (value == null) {
            out.println(key + " = null");
        } else {
            out.println(key + ":" + value.getClassName() + " = " + value.getValueAsString());
        }
    }

    private void print(String key, ValueTracker value) {
        if (value == null) {
            out.println(key + " is undefined");
        } else {
            print(key, value.getCurrentValue());
        }
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

    public void print(List<Pair<Long, TrackedObject>> valueChanges) {
        for (Pair<Long, TrackedObject> valueChange : valueChanges) {
            out.println(" " + valueChange.getValue1() + ": " + valueChange.getValue2().getValueAsString());
        }
    }

    @Override
    public SidratExecutionEvent readNext() {
        SidratExecutionEvent event = super.readNext();
        if (event == null) {
            continueProcessing = false;
        }
        print(event);
        return event;
    }

    @Override
    public SidratExecutionEvent gotoEvent(int id) {
        SidratExecutionEvent event = super.gotoEvent(id);
        print(event);
        return event;
    }

    @Override
    public SidratExecutionEvent gotoEvent(Long id) {
        SidratExecutionEvent event = super.gotoEvent(id);
        print(event);
        return event;
    }

    @Override
    public SidratExecutionEvent prevEvent() {
        SidratExecutionEvent prevEvent = super.prevEvent();
        print(prevEvent);
        return prevEvent;
    }

}
