package com.sidrat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.store.EventReader;
import com.sidrat.event.store.hsqldb.HsqldbEventReader;
import com.sidrat.util.Logger;

import org.apache.commons.io.FileUtils;

public class SidratReplay {
    private static final Logger logger = new Logger();

    private EventReader eventReader;
    private File sourceDir;
    private BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
    private PrintStream out = System.out;

    private boolean continueProcessing = true;
    private SidratExecutionEvent event;
    private List<String> breakpoints = Lists.newArrayList();

    private List<String> invalidSourceFiles = Lists.newArrayList();

    public static interface Condition {
        public boolean execute();
    }

    public SidratReplay(String fileName) {
        this.eventReader = new HsqldbEventReader(fileName);
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
                                if (eventReader.executions(breakpoint).size() > 0) {
                                    breakpoints.add(parsedLine[1]);
                                }
                            }
                        }
                        break;
                    case "del": // delete a breakpoint
                        {
                            if (parsedLine.length != 2) {
                                out.println("Breakpoint # is required.");
                            } else {
                                int index = Integer.parseInt(parsedLine[1]);
                                breakpoints.remove(index);
                            }
                        }
                        break;
                    case "c": // continue
                    case "cont": // continue
                        {
                            automaticExecution = new Condition() {
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
                    case "l": // locals
                        {
                            Map<String,Object> localVariables = locals();
                            for (String key : localVariables.keySet()) {
                                Object value = localVariables.get(key);
                                out.println(key + ": " + value);
                            }
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
        return event;
    }

    public SidratExecutionEvent gotoEvent(Long id) {
        event = eventReader.find(id);
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

    public Map<String, Object> locals() {
        if (event == null) {
            return Maps.newHashMap();
        }
        return eventReader.locals(event.getTime());
    }

    public void print() {
        print(event);
    }

    public void print(SidratExecutionEvent event) {
        event.print(System.out);
        String sourceCode = lookupSourceCode(event);
        if (sourceCode != null)
            out.println(sourceCode);
    }

    private String lastFile;
    private List<String> lines;

    @SuppressWarnings("unchecked")
    public String lookupSourceCode(SidratExecutionEvent event) {
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
                lines = (List<String>) FileUtils.readLines(new File(fullPath));
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
