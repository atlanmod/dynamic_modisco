package org.example;
/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        new org.atlanmod.trace.TestTracer().before("App.java","138", "169");
        System.out.println("Hello, ");
        new org.atlanmod.trace.TestTracer().before("App.java","180", "208");
        System.out.println("World!");
        new org.atlanmod.trace.TestTracer().after("App.java","org.example.App#main","main(java.lang.String[])");
    }
}