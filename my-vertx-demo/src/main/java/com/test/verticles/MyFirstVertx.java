package com.test.verticles;

import io.vertx.core.AbstractVerticle;

public class MyFirstVertx extends AbstractVerticle {

    @Override
    public void start() {
        vertx.createHttpServer().requestHandler(req -> {
            req.response().putHeader("content-type", "text/plain").end("hello");
        }).listen(8080);
    }
}
