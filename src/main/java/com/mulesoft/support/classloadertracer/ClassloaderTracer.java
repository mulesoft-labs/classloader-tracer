package com.mulesoft.support.classloadertracer;

import java.lang.instrument.*;
import java.net.URL;
import java.security.*;


/** trace class loading and prints the chain of classloaders that loads each class.
 * Works as a replacement of the Java -verbose:gc which doesn't show the name of the classloaders.
 *
 * Based on this blog post: https://blogs.oracle.com/sundararajan/entry/tracing_class_loading_1_5
 *
 * Usage:
 *
 * add this argument to your application Java command line: -javaagent:classloader-tracer.jar
 */


public class ClassloaderTracer {


    public static String printClassloadersHierarchy(ClassLoader cl) {
        StringBuffer buf=new StringBuffer();

        while (cl != null) {
            buf.append(cl.toString());
            buf.append("->");
            cl = cl.getParent();
        }
        buf.append("(null)");
        return buf.toString();
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        final java.io.PrintStream out = System.out;
        inst.addTransformer(new ClassFileTransformer() {
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer)
                    throws IllegalClassFormatException {

                // TODO can I print classloader hierarchy here?? loader.getParent() loop and repeat

                out.print("[" + Thread.currentThread().getName() + "] Class: " + className + " loaded by " + printClassloadersHierarchy(loader) + " at " +
                        new java.util.Date());
                CodeSource cs = protectionDomain.getCodeSource();
                URL url = cs.getLocation();
                out.println(" in " + url.getFile());

                // dump stack trace of the thread loading class
                // out.println(" stacktrace ");
                // Thread.dumpStack();

                // we just want the original .class bytes to be loaded!
                // we are not instrumenting it...
                return null;
            }
        });
    }
}