package com.sidrat;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sidrat.event.SidratExecutionEvent;
import com.sidrat.event.store.EventReader;
import com.sidrat.event.store.hsqldb.HsqldbEventReader;
import com.sidrat.event.tracking.CapturedFieldValue;
import com.sidrat.event.tracking.CapturedLocalVariableValue;
import com.sidrat.event.tracking.TrackedObject;
import com.sidrat.util.Logger;
import com.sidrat.util.Tuple3;

import org.apache.commons.io.FileUtils;

/**
 * Debugger for replaying Sidrat recordings.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class SidratReplay {
    private static final Logger logger = new Logger();

    protected EventReader eventReader;
    protected SidratExecutionEvent event;
    private File sourceDir;
    private List<String> invalidSourceFiles = Lists.newArrayList();

    private String lastFile;

    private List<String> lines;

    public SidratReplay(EventReader reader) {
        this.eventReader = reader;
    }

    public SidratReplay(String fileName) {
        this.eventReader = new HsqldbEventReader(fileName);
    }

    public Map<String, CapturedFieldValue> eval(TrackedObject obj) {
        if (event == null || obj == null) {
            return Maps.newHashMap();
        }
        return eventReader.eval(event.getTime(), obj.getUniqueID());
    }

    public SidratExecutionEvent gotoEvent(int id) {
        event = eventReader.find(new Long(id));
        return event;
    }

    public SidratExecutionEvent gotoEvent(Long id) {
        event = eventReader.find(id);
        return event;
    }

    public Map<String, CapturedLocalVariableValue> locals() {
        if (event == null) {
            return Maps.newHashMap();
        }
        return eventReader.locals(event.getTime());
    }

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

    public SidratExecutionEvent prevEvent() {
        SidratExecutionEvent prevEvent = eventReader.findPrev(event);
        if (prevEvent != null)
            event = prevEvent;
        else
            return null;
        return event;
    }

    public SidratExecutionEvent readNext() {
        if (event == null) {
            event = eventReader.findFirst();
        } else {
            SidratExecutionEvent nextEvent = eventReader.findNext(event);
            if (nextEvent == null) {
                return null;
            } else {
                event = nextEvent;
            }
        }
        return event;
    }

    public Tuple3<String, String, Integer> split(String breakpoint) {
        String[] split = breakpoint.split(":");
        Integer lineNumber = Integer.parseInt(split[1]);
        int lastDot = split[0].lastIndexOf('.');
        String method = split[0].substring(lastDot + 1);
        String className = split[0].substring(0, lastDot);
        return new Tuple3<String, String, Integer>(className, method, lineNumber);
    }

    private void validateDir(File file) {
        validateFile(file);
        if (!file.isDirectory())
            throw new SidratProcessingException("Replay file path: " + file.getName());
    }

    private void validateFile(File file) {
        if (!file.exists())
            throw new SidratProcessingException("Replay file not found: " + file.getName());
    }

    public SidratReplay withSource(String source) {
        this.sourceDir = new File(source);
        validateDir(this.sourceDir);
        return this;
    }
}
