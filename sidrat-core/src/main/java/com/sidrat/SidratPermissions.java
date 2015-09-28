package com.sidrat;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps track of what we should and should not record.
 *
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class SidratPermissions {
    private List<String> packageWhitelist = new ArrayList<>();
    private List<String> packageBlacklist = new ArrayList<>();
    private List<String> classWhitelist = new ArrayList<>();
    private boolean whitelistEverything = false;

    public SidratPermissions() {
        allowAll();
    }
    
    /**
     * Do not use a whitelist; attempt to instrument all classes.
     * 
     * @return
     */
    public SidratPermissions allowAll() {
        whitelistEverything = true;
        return this;
    }

    public SidratPermissions whitelistPackage(Class<?> clazz) {
        whitelistPackage(clazz.getPackage().getName());
        return this;
    }

    public SidratPermissions whitelistPackage(String packageRoot) {
        whitelistEverything = false;
        this.packageWhitelist.add(packageRoot);
        return this;
    }

    public SidratPermissions whitelistClass(String className) {
        whitelistEverything = false;
        this.classWhitelist.add(className);
        return this;
    }

    public SidratPermissions blacklistPackage(String packageRoot) {
        this.packageBlacklist.add(packageRoot);
        return this;
    }

    public boolean isAllowed(Class<?> javaClass) {
        String classname = javaClass.getName();
        return isAllowed(classname);
    }

    public boolean isAllowed(String classname) {
        if (whitelistEverything) {
            if (classWhitelist.contains(classname))
                return true;
            return !onPackageBlacklist(classname);
        }
        if (classWhitelist.contains(classname))
            return true;
        for (String packageRoot : packageWhitelist) {
            if (classname.startsWith(packageRoot + ".")) {
                return !onPackageBlacklist(classname);
            }
        }
        return false;
    }

    private boolean onPackageBlacklist(String classname) {
        if (classname.startsWith("com.sidrat."))
            return true;
        if (classname.startsWith("java."))
            return true;
        if (classname.startsWith("org.junit."))
            return true;
        if (classname.startsWith("sun."))
            return true;
        for (String packageRoot : packageBlacklist) {
            if (classname.startsWith(packageRoot + ".")) {
                return true;
            }
        }
        return false;
    }
}
