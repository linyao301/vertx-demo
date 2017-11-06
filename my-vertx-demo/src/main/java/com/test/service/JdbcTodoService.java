package com.test.service;

import com.test.entity.Todo;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

import java.util.List;
import java.util.Optional;

public class JdbcTodoService implements TodoService {

    private final Vertx vertx;

    private final JsonObject config;

    private final JDBCClient client;

    private static final String SQL_CREATE = "";

    private static final String SQL_INSERT = "";

    private static final String SQL_QUERY = "";

    private static final String SQL_QUERY_ALL = "";

    private static final String SQL_UPDATE = "";

    private static final String SQL_DELETE = "";

    private static final String SQL_DELETE_ALL = "";
    private boolean r;
    private boolean r1;

    public JdbcTodoService(JsonObject config) {
        this(Vertx.vertx(), config);
    }

    public JdbcTodoService(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.config = config;
        this.client = JDBCClient.createShared(vertx, config);
    }

    private Handler<AsyncResult<SQLConnection>> connHandler(Future future, Handler<SQLConnection> handler) {
        return conn -> {
            if (conn.succeeded()) {
                final SQLConnection connection = conn.result();
                handler.handle(connection);
            } else {
                future.fail(conn.cause());
            }
        };
    }

    @Override
    public Future<Boolean> initData() {
        Future<Boolean> result = Future.future();
        client.getConnection(connHandler(result, connection -> {
            connection.execute(SQL_CREATE, create -> {
                if (create.succeeded()) {
                    result.complete(true);
                } else {
                    result.fail(create.cause());
                }
            });
        }));
        return null;
    }

    @Override
    public Future<Boolean> insert(Todo todo) {
        Future<Boolean> result = Future.future();
        client.getConnection(connHandler(result, connection -> {
            connection.updateWithParams(SQL_INSERT, new JsonArray().add(todo.getId())
                    .add(todo.getTitle())
                    .add(todo.getCompleted())
                    .add(todo.getOrder())
                    .add(todo.getUrl()), r -> {
                if (r.failed()) {
                    result.fail(r.cause());
                } else {
                    result.complete(true);
                }
                connection.close();
            });
        }));
        return null;
    }

    @Override
    public Future<List<Todo>> getAll() {
        return null;
    }

    @Override
    public Future<Optional<Todo>> getCertain(String todoID) {
        return null;
    }

    @Override
    public Future<Todo> update(String todoId, Todo newTodo) {
        return null;
    }

    @Override
    public Future<Boolean> delete(String todoId) {
        return null;
    }

    @Override
    public Future<Boolean> deleteAll() {
        return null;
    }
}
