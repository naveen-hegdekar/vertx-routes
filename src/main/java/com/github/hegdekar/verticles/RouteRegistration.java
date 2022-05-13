package com.github.hegdekar.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;

public class RouteRegistration extends AbstractVerticle {
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.setTimer(5000, handler -> {
      vertx.eventBus().send("REGISTER_ROUTE",
        new JsonObject()
          .put("url", "/test-router")
          .put("method", "GET")
          .put("response", new JsonObject().put("message", "This works..!!"))
      );
      startPromise.complete();
    });
  }
}
