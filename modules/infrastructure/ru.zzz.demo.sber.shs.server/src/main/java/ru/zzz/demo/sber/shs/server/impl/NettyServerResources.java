package ru.zzz.demo.sber.shs.server.impl;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import org.springframework.lang.NonNull;
import ru.zzz.demo.sber.shs.config.ServerConfig;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.SECONDS;

public class NettyServerResources {
    private static final AtomicInteger THREAD_NAMES_COUNTER = new AtomicInteger();
    private final NioEventLoopGroup acceptorGroup;
    private final NioEventLoopGroup selectorGroup;

    private NettyServerResources(NioEventLoopGroup acceptorGroup, NioEventLoopGroup selectorGroup) {
        this.acceptorGroup = acceptorGroup;
        this.selectorGroup = selectorGroup;
    }

    @NonNull
    public static NettyServerResources create(ServerConfig serverConfig) {
        NioEventLoopGroup acceptorGroup = new NioEventLoopGroup(serverConfig.serverAcceptorThreads(),
                createThreadFactory("acceptor"));
        NioEventLoopGroup selectorGroup = new NioEventLoopGroup(serverConfig.serverSelectorThreads(),
                createThreadFactory("selector"));
        return new NettyServerResources(acceptorGroup, selectorGroup);
    }

    @NonNull
    public NioEventLoopGroup getAcceptorGroup() {
        return acceptorGroup;
    }

    @NonNull
    public NioEventLoopGroup getSelectorGroup() {
        return selectorGroup;
    }

    public void shutdown() {
        Future<?> acc = null;
        Future<?> sel = null;
        if (acceptorGroup != null)
            acc = acceptorGroup.shutdownGracefully(1, 1, SECONDS);
        if (acceptorGroup != null)
            sel = selectorGroup.shutdownGracefully(1, 1, SECONDS);
        CompletableFuture.allOf(makeCompletableFuture(acc), makeCompletableFuture(sel))
                .handle((aVoid, throwable) -> {
                    if (throwable != null)
                        throwable.printStackTrace();
                    return CompletableFuture.completedFuture(null);
                })
                .join();
    }

    private static <T> CompletableFuture<T> makeCompletableFuture(Future<T> future) {
        return (future == null) ? CompletableFuture.completedFuture(null) : CompletableFuture.supplyAsync(() -> {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @NonNull
    private static ThreadFactory createThreadFactory(String type) {
        return runnable -> new Thread(runnable, "webflux-" + type + "-" + THREAD_NAMES_COUNTER.incrementAndGet());
    }
}
