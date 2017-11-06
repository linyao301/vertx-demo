package com.test.verticles;

import com.test.Constants;
import com.test.service.JdbcTodoService;
import com.test.service.RedisTodoService;
import com.test.service.TodoService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.redis.RedisOptions;

import java.util.HashSet;
import java.util.Set;

public class TodoVerticle extends AbstractVerticle {

    private static final String HOST = "0.0.0.0";
    private static final int PORT = 8080;

    private TodoService service;

    private void initData() {
        final String serviceType = config().getString("service.type", "redis");
        switch (serviceType) {
            case "jdbc":
                service = new JdbcTodoService(vertx, config());
                break;
            case "redis":
            default:
                RedisOptions config = new RedisOptions()
                        .setHost(config().getString("redis.host", "127.0.0.1"))
                        .setPort(config().getInteger("redis.port", 6379));
                service = new RedisTodoService(vertx, config);
        }
        service.initData();
    }

    @Override
    public void start(Future<Void> future) throws Exception {
        Router router = Router.router(vertx);
        //CORS support
        Set<String> allowHeaders = new HashSet<>();
        allowHeaders.add("x-requested-with");
        allowHeaders.add("Access-Control-Allow-Origin");
        allowHeaders.add("origin");
        allowHeaders.add("Content-Type");
        allowHeaders.add("accept");
        Set<HttpMethod> allowMethods = new HashSet<>();
        allowMethods.add(HttpMethod.GET);
        allowMethods.add(HttpMethod.POST);
        allowMethods.add(HttpMethod.DELETE);
        allowMethods.add(HttpMethod.PATCH);

        router.route().handler(BodyHandler.create());
        router.route().handler(CorsHandler.create("*")
                .allowedHeaders(allowHeaders)
                .allowedMethods(allowMethods));

        //routes
        //router.get(Constants.API_GET).handler();

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(PORT, HOST, result -> {
                    if (result.succeeded()) {
                        future.complete();
                    } else {
                        future.fail(result.cause());
                    }
                });

        initData();
    }
}
