package io.vertx.redis.client.impl;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.redis.client.RedisConnection;
import io.vertx.redis.client.Request;
import io.vertx.redis.client.Response;

import java.util.List;

public class RedisSentinelConnection implements RedisConnection {

  private final RedisConnection connection;
  private final RedisConnection sentinel;

  public RedisSentinelConnection(RedisConnection connection, RedisConnection sentinel) {
    this.connection = connection;
    this.sentinel = sentinel;
  }

  @Override
  public RedisConnection exceptionHandler(Handler<Throwable> handler) {
    connection.exceptionHandler(handler);
    return this;
  }

  @Override
  public RedisConnection handler(Handler<Response> handler) {
    connection.handler(handler);
    return this;
  }

  @Override
  public RedisConnection pause() {
    connection.pause();
    return this;
  }

  @Override
  public RedisConnection resume() {
    connection.resume();
    return this;
  }

  @Override
  public RedisConnection fetch(long amount) {
    connection.fetch(amount);
    return this;
  }

  @Override
  public RedisConnection endHandler(@Nullable Handler<Void> endHandler) {
    connection.endHandler(endHandler);
    return this;
  }

  @Override
  public Future<@Nullable Response> send(Request command) {
    return connection.send(command);
  }

  @Override
  public Future<List<@Nullable Response>> batch(List<Request> commands) {
    return connection.batch(commands);
  }

  @Override
  public Future<Void> close() {
    final Promise<Void> promise = Promise.promise();

    sentinel.close()
      .onSuccess(done -> connection.close(promise))
      .onFailure(promise::fail);

    return promise.future();
  }

  @Override
  public boolean pendingQueueFull() {
    return connection.pendingQueueFull();
  }
}
