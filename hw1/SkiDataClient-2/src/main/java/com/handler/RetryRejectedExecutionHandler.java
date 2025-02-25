package com.handler;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class RetryRejectedExecutionHandler implements RejectedExecutionHandler {

    private final long retryDelay;

    public RetryRejectedExecutionHandler(long retryDelay) {
        this.retryDelay = retryDelay;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        try {
            Thread.sleep(retryDelay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        executor.execute(r);
    }
}
