package ru.zzz.demo.sber.shs.server;

import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import ru.zzz.demo.sber.shs.config.ServerConfig;
import ru.zzz.demo.sber.shs.server.impl.NettyServerResources;

/**
 * Implements a simple and manageable server which theoretically may be stopped gracefully.
 * @implNote
 * Spring Boot can start WebFlux server automatically has manage it with a set of customizers but I didn't
 * find a simple way to make it work after my customizations and I have much doubts about its ability to
 * shutdown gracefully.
 */
@Component("SHS.Infrastructure.HttpServer.HttpServerStarter")
public class HttpServerStarter implements InitializingBean, DisposableBean {
    private final ApplicationContext ctx;
    private final ServerConfig config;
    private DisposableServer server;
    private NettyServerResources resources;

    @Autowired
    HttpServerStarter(ApplicationContext ctx, ServerConfig config) {
        this.ctx = ctx;
        this.config = config;
    }

    @Override
    public void afterPropertiesSet() {
        server = createServer(config);
    }

    @Override
    public void destroy() {
        if (server != null) server.disposeNow();
        if (resources != null) resources.shutdown();
    }

    @NonNull
    private DisposableServer createServer(ServerConfig serverConfig) {
        resources = NettyServerResources.create(serverConfig);
        HttpHandler handler = WebHttpHandlerBuilder.applicationContext(ctx).build();
        return HttpServer.create()
                //.host(serverConfig.hostName())
                .port(serverConfig.port())
                .tcpConfiguration(tcpServer -> tcpServer.bootstrap(serverBootstrap -> {
                    return serverBootstrap.group(resources.getAcceptorGroup(), resources.getSelectorGroup())
                            .channel(NioServerSocketChannel.class);
                }))
                .handle(new ReactorHttpHandlerAdapter(handler))
                .bind()
                .blockOptional()
                .orElseThrow(() -> {
                    resources.shutdown();
                    return new RuntimeException("Cannot start WebFlux server");
                });
    }
}
