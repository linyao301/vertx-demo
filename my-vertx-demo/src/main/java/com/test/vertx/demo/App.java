package com.test;

import com.test.verticles.MyFirstVertx;
import com.test.verticles.SingleApplicationVerticle;
import io.vertx.core.Vertx;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        //vertx.deployVerticle(MyFirstVertx.class.getName());
        vertx.deployVerticle(SingleApplicationVerticle.class.getName());
    }
}
