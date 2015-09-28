package com.sidrat.instrument;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import com.sun.tools.attach.VirtualMachine;

import org.apache.commons.io.IOUtils;

/**
 * A Java premain agent that initializes the ClassFileTransformer that instruments classes so that Sidrat can
 * record program execution.
 * 
 * @author Lawrence McAlpin (admin@lmcalpin.com)
 */
public class SidratAgent {
    private static boolean initialized = false;
    private static SidratAgentTransformer transformer = new SidratAgentTransformer();

    public static boolean isInstrumentationAvailable() {
        return initialized;
    }

    public static void agentmain(String agentArguments, Instrumentation instrumentation) throws Exception {
        premain(agentArguments, instrumentation);
    }

    public static void premain(String agentArguments, Instrumentation instrumentation) {
        initialized = true;
        instrumentation.addTransformer(transformer);
    }
    
    public static void loadAgent(String agentPath) {
        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        int p = nameOfRunningVM.indexOf('@');
        String pid = nameOfRunningVM.substring(0, p);
        try {
            VirtualMachine vm = VirtualMachine.attach(pid);
            vm.loadAgent(agentPath, "");
            vm.detach();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void createAndLoadAgent() {
        try {
            if (initialized)
                return;
            File tempAgentJar = createTempAgentJar();
            loadAgent(tempAgentJar.getAbsolutePath());
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
               tempAgentJar.delete(); 
            }));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static File createTempAgentJar() throws Exception {
        String agentClassName = SidratAgent.class.getName();
        String tempDir = System.getProperty("java.io.tmpdir");
        File tempAgentJar = new File(tempDir, agentClassName + "-" + System.currentTimeMillis() + ".jar");
        addToClassLoader(tempAgentJar);
        Manifest manifest = new Manifest(SidratAgent.class.getResourceAsStream("/META-INF/MANIFEST.MF"));
        manifest.getMainAttributes().putValue("Agent-Class", agentClassName);
        JarOutputStream tempAgentJarOut = new JarOutputStream(new FileOutputStream(tempAgentJar), manifest);
        ZipEntry entry = new ZipEntry(agentClassName.replace(".", "/") + ".class");
        tempAgentJarOut.putNextEntry(entry);
        IOUtils.copy(SidratAgent.class.getProtectionDomain().getCodeSource().getLocation().openStream(), tempAgentJarOut);
        tempAgentJarOut.closeEntry();
        tempAgentJarOut.close();
        return tempAgentJar;
    }

    // TODO: ugly hack... is there a better way?
    private static void addToClassLoader(File jar) throws MalformedURLException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        URL url = new File(jar.getAbsolutePath()).toURI().toURL();
        Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
        method.setAccessible(true);
        method.invoke(ClassLoader.getSystemClassLoader(), new Object[] { url });
    }
}
