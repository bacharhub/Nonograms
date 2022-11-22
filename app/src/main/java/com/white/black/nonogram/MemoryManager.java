package com.white.black.nonogram;

public class MemoryManager {

    private static final int LOW_MEMORY_THRESHOLD_IN_MB = 50;
    private static final int CRITICAL_MEMORY_THRESHOLD_IN_MB = 30;
    private static final int NO_MEMORY_THRESHOLD_IN_MB = 10;

    private static long getMaxMemoryInMB() {
        Runtime rt = Runtime.getRuntime();
        long maxMemory = rt.maxMemory() / 1024 / 1024;
        return maxMemory;
    }

    private static long getUsedMemoryInMB() {
        Runtime rt = Runtime.getRuntime();
        long freeVMMemory = rt.freeMemory();
        long totalVMMemory = rt.totalMemory();
        long usedMemory = totalVMMemory - freeVMMemory;
        return usedMemory / 1024 / 1024;
    }

    private static long getAvailableMemoryInMb() {
        return getMaxMemoryInMB() - getUsedMemoryInMB();
    }

    public static boolean isLowMemory() {
        return getAvailableMemoryInMb() < LOW_MEMORY_THRESHOLD_IN_MB;
    }

    public static boolean isCriticalMemory() {
        return getAvailableMemoryInMb() < CRITICAL_MEMORY_THRESHOLD_IN_MB;
    }

    public static boolean isNoMemory() {
        return getAvailableMemoryInMb() < NO_MEMORY_THRESHOLD_IN_MB;
    }
}
