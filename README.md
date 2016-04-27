# Classloader Tracer
This is a simple tool for Java applications to trace which classloader loads each class. It is useful to troubleshoot problems classloading issues at application startup or on first usage of a class.

It will print to standard output information about the chain of classloaders used to load a class, up to the null classloader. 
Note that null means that there are no further information.

It will also print some useful information, similar to the -verbose:class argument to Java.

It is based on information provided on this blog post: https://blogs.oracle.com/sundararajan/entry/tracing_class_loading_1_5

# Usage
add this argument to your application Java command line: -javaagent:/path/to/classloader-tracer.jar

