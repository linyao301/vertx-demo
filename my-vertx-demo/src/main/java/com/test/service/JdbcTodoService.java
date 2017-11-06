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

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        Future<List<Todo>> result = Future.future();
        client.getConnection(connHandler(result, connection -> {
            connection.query(SQL_QUERY_ALL, r -> {
                if (r.failed()) {
                    result.fail(r.cause());
                } else {
                    List<Todo> todos = r.result().getRows().stream()
                            .map(x -> new Todo(String.valueOf(x)))
                            .collect(Collectors.toList());
                    result.complete(todos);
                }
                connection.close();
            });
        }));
        return null;
    }

    @Override
    public Future<Optional<Todo>> getCertain(String todoID) {
        Future<Optional<Todo>> result = Future.future();
        client.getConnection(connHandler(result, connection -> {
            connection.queryWithParams(SQL_QUERY, new JsonArray().add(todoID), r -> {
                if (r.failed()) {
                    result.fail(r.cause());
                } else {
                    List<JsonObject> list = r.result().getRows();
                    if (list == null || list.isEmpty()) {
                        result.complete(Optional.empty());
                    } else {
                        result.complete(Optional.of(new Todo(list.get(0))));
                    }
                }
                connection.close();
            });
        }));
        return null;
    }

    @Override
    public Future<Todo> update(String todoId, Todo newTodo) {
        Future<Todo> result = Future.future();
        client.getConnection(connHandler(result, connection -> {
            this.getCertain(todoId).setHandler(r -> {
                if (r.failed()) {
                    result.fail(r.cause());
                } else {
                    Optional<Todo> oldTodo = r.result();
                    if (!oldTodo.isPresent()) {
                        result.complete(null);
                        return;
                    }
                    // TODO: 2017\11\6 0006  此处需要merge
                    Todo fnTodo = oldTodo.get();
                    int updateId = oldTodo.get().getId();
                    connection.updateWithParams(SQL_UPDATE, new JsonArray().add(updateId)
                            .add(fnTodo.getTitle())
                            .add(fnTodo.getCompleted())
                            .add(fnTodo.getOrder())
                            .add(fnTodo.getUrl())
                            .add(updateId), x -> {
                        if (x.failed()) {
                            result.fail(x.cause());
                        } else {
                            result.complete(fnTodo);
                        }
                        connection.close();
                    });
                }
            });
        }));
        return result;
    }

    private Future<Boolean> deleteProcess(String sql, JsonArray params) {
        Future<Boolean> result = Future.future();
        client.getConnection(connHandler(result, connection -> {
            connection.updateWithParams(sql, params, r -> {
                if (r.failed()) {
                    result.complete(false);
                } else {
                    result.complete(true);
                }
                connection.close();
            });
        }));
        return result;
    }

    @Override
    public Future<Boolean> delete(String todoId) {
        return deleteProcess(SQL_DELETE, new JsonArray().add(todoId));
    }

    @Override
    public Future<Boolean> deleteAll() {
        return deleteProcess(SQL_DELETE_ALL, new JsonArray());
    }
}
