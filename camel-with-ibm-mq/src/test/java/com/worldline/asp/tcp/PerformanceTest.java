package com.worldline.asp.tcp;


public class PerformanceTest {

    private static final long MEGABYTE = 1024L * 1024L;

    public static long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }

    public static void main(String[] args) {
        // Get the Java runtime
        Runtime runtime = Runtime.getRuntime();
        // Run the garbage collector
        runtime.gc();

        // Calculate the used memory
        long memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Used memory is bytes: " + memory);
        System.out.println("Used memory is megabytes: "+ bytesToMegabytes(memory));

        // I assume you will know how to create a object Person yourself...
        /*        List<Person> list = new ArrayList<Person>();
        for (int i = 0; i <= 1; i++) {
            list.add(new  Person("Jim","ASC"));
        }*/

        // Calculate the used memory
        memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Used memory is bytes: " + memory);
        System.out.println("Used memory is megabytes: "+ bytesToMegabytes(memory));
    }

}
