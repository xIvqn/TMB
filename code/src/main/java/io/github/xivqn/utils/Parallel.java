package io.github.xivqn.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Parallel {

    public static ExecutorService executor;
    private static AtomicInteger counter = new AtomicInteger(1);

    public static synchronized void initialize(int nWorkers){
        if(executor != null) return;
        executor = Executors.newFixedThreadPool(nWorkers, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("MonteCarlo-" + counter.getAndIncrement());
            return t;
        });
    }

    static{
        initialize(getAvailableThreads());
    }

    public static int getAvailableThreads() {
        return (int)(Runtime.getRuntime().availableProcessors()/1.5);
    }
}
