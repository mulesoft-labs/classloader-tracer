package com.mulesoft.support.classloadertracer;

import java.io.FileOutputStream;
import java.lang.instrument.*;
import java.net.URL;
import java.security.*;
import java.io.PrintWriter;

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
        StringBuilder buf= new StringBuilder();

        while (cl != null) {
            buf.append(cl.toString());
            buf.append("->");
            cl = cl.getParent();
        }
        buf.append("(null)");
        return buf.toString();
    }

    public static void premain(String agentArguments, Instrumentation inst) throws Exception {
        PrintWriter outWriter=null;

        if(agentArguments!=null) {
            for (String t : agentArguments.split(",")) {
                if (t.equals("help")) {
                    printUsage();
                    System.exit(-1); // abort program execution!
                } else if (t.startsWith("output=")) {
                    outWriter = new PrintWriter(new FileOutputStream(t.substring(7)));
                } else if (t.startsWith("stdout")) {
                    outWriter = new PrintWriter(System.out);
                } else {
                    System.err.println("Unknown option: "+t);
                    printUsage();
                    System.exit(-1);
                }
            }
        }

        if (outWriter==null) {
            outWriter = new PrintWriter(System.err);
        }

        final PrintWriter out=outWriter;

        inst.addTransformer(new ClassFileTransformer() {
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer)
                    throws IllegalClassFormatException {

                out.print(new java.util.Date() + " [" + Thread.currentThread().getName() + "] Class: " + className + " loaded by " + printClassloadersHierarchy(loader) );
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


    private static void printUsage() {
        System.err.println("  help          - show the help screen");
        System.err.println("  stdout        - prints output to stdout (default is stderr)");
        System.err.println("  output=FILE   - prints output to a file");
    }
}