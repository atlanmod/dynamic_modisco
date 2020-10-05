package org.example;
/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        new org.atlanmod.SimpleTraceTracer().before("App.java","9", "40");
        System.out.println("Hello, ");
        new org.atlanmod.SimpleTraceTracer().before("App.java","9", "36");
        System.out.println("World!");
        new org.atlanmod.SimpleTraceTracer().after("main(java.lang.String[])");
    }
}