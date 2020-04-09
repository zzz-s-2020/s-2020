/*
 * Copyright (C) 2019, 1C
 */
package ru.zzz.demo.sber.shs.server.impl;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import ru.zzz.demo.sber.shs.config.ServerConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Component("SHS.Infrastructure.HttpServer.WebFluxScheduler")
public class WebFluxScheduler implements Scheduler, InitializingBean, DisposableBean {
    private final AtomicInteger threadsNamePostfix = new AtomicInteger();
    private final ServerConfig config;
    private Scheduler scheduler;

    @Autowired
    public WebFluxScheduler(ServerConfig config) {
        this.config = config;
    }

    @Override
    public void afterPropertiesSet() {
        ThreadFactory threadFactory = runnable -> {
            Thread t = new Thread(runnable, "webflux-worker-" + threadsNamePostfix.incrementAndGet());
            t.setDaemon(true);
            return t;
        };
        ExecutorService executorService =
                Executors.newFixedThreadPool(config.serverWorkerThreads(), threadFactory);
        scheduler = Schedulers.fromExecutorService(executorService);
    }

    @Override
    public void destroy() {
        scheduler.dispose();
    }

    @NonNull
    @Override
    public Disposable schedule(Runnable task) {
        return scheduler.schedule(task);
    }

    @NonNull
    @Override
    public Scheduler.Worker createWorker() {
        return scheduler.createWorker();
    }
}
