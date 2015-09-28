package com.sidrat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

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
    
    private static final Set<String> BLACKLISTED_PACKAGES = Sets.newHashSet("jdk.internal.",
            "com.sidrat.",
            "java.",
            "org.junit.",
            "sun.");

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
            String packageRootDot = packageRoot.endsWith(".") ? packageRoot : packageRoot + ".";
            if (classname.startsWith(packageRootDot)) {
                return !onPackageBlacklist(classname);
            }
        }
        return false;
    }

    private boolean onPackageBlacklist(String classname) {
        for (String blacklistedPackage : BLACKLISTED_PACKAGES) {
            if (classname.startsWith(blacklistedPackage))
                return true;
        }
        for (String packageRoot : packageBlacklist) {
            if (classname.startsWith(packageRoot + ".")) {
                return true;
            }
        }
        return false;
    }
}
