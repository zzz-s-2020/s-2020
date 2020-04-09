package ru.zzz.demo.sber.shs.server.impl;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration("SHS.Infrastructure.HttpServer.ServerConfiguration")
@ComponentScan(basePackages = {"ru.zzz.demo.sber.shs"})
public class ServerConfiguration {
}
