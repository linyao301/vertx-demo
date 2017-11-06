package com.test.service;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.test.Constants;
import com.test.entity.Todo;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Redis Todo Service
 */
public class RedisTodoService implements TodoService {

    private final Vertx vertx;
    private final RedisOptions config;
    private final RedisClient redis;

    public RedisTodoService(RedisOptions config) {
        this(Vertx.vertx(), config);
    }

    public RedisTodoService(Vertx vertx, RedisOptions config) {
        this.vertx = vertx;
        this.config = config;
        this.redis = RedisClient.create(vertx, config);
    }

    @Override
    public Future<Boolean> initData() {
        return insert(new Todo(Math.abs(new Random().nextInt()), "Something to do ...", false, 1, "todo/ex"));
    }

    @Override
    public Future<Boolean> insert(Todo todo) {
        Future<Boolean> result = Future.future();
        final String encoded = Json.encodePrettily(todo);
        redis.hset(Constants.RESIT_TODO_KEY, String.valueOf(todo.getId()),
                encoded, res -> {
                    if (res.succeeded()) {
                        result.complete(true);
                    } else {
                        result.fail(res.cause());
                    }
                });
        return result;
    }

    @Override
    public Future<List<Todo>> getAll() {
        Future<List<Todo>> result = Future.future();
        redis.hvals(Constants.RESIT_TODO_KEY, res -> {
            if (res.succeeded()) {
                result.complete(res.result().stream().map(x -> new Todo()).collect(Collectors.toList()));
                Collectors.toList();
            } else {
                result.fail(res.cause());
            }
        });
        return result;
    }

    @Override
    public Future<Optional<Todo>> getCertain(String todoID) {
        Future<Optional<Todo>> result = Future.future();
        redis.hget(Constants.RESIT_TODO_KEY, todoID, res -> {
            if (res.succeeded()) {
                result.complete(Optional.ofNullable(res.result() == null ? null : new Todo()));
            } else {
                result.fail(res.cause());
            }
        });
        return null;
    }

    @Override
    public Future<Todo> update(String todoId, Todo newTodo) {
        return this.getCertain(todoId).compose(old -> {
            if (old.isPresent()) {
                Todo fnTodo = newTodo;
                return this.insert(fnTodo).map(r -> r ? fnTodo : null);
            } else {
                return Future.succeededFuture();
            }
        });
    }

    @Override
    public Future<Boolean> delete(String todoId) {
        Future<Boolean> result = Future.future();
        redis.hdel(Constants.RESIT_TODO_KEY, todoId, res -> {
            if (res.succeeded()) {
                result.complete(true);
            } else {
                result.complete(false);
            }
        });
        return null;
    }

    @Override
    public Future<Boolean> deleteAll() {
        Future<Boolean> result = Future.future();
        redis.del(Constants.RESIT_TODO_KEY, res -> {
            if (res.succeeded()) {
                result.complete(true);
            } else {
                result.complete(false);
            }
        });
        return null;
    }
}
