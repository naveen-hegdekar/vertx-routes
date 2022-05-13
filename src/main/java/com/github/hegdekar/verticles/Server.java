package com.github.hegdekar.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Server extends AbstractVerticle {
  private static final Logger LOGGER = LogManager.getLogger(Server.class);
  private static Router router;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    router = Router.router(vertx);

    Set<String> allowedHeadersSet = new HashSet<>(List.of("GET", "PUT", "POST", "DELETE", "OPTIONS", "PATCH"));
    Set<HttpMethod> allowedMethodsSet = Stream
      .of("Origin", "Access-Control-Allow-Origin", "Content-Type", "Accept")
      .map(HttpMethod::valueOf)
      .collect(Collectors.toSet());

    router.route("/*")
      .handler(BodyHandler.create(false))
      .handler(CorsHandler.create("*")
        .allowedHeaders(allowedHeadersSet)
        .allowedMethods(allowedMethodsSet)
      );

    router.errorHandler(404, routingContext -> {
      LOGGER.info("HTTP-404 Error Handler invoked");
      LOGGER.info("Router :" + router.toString() + " Routes Size: " + router.getRoutes().size());
      JsonObject error = new JsonObject()
        .put("error", "Resource not found");
      routingContext.response()
        .setStatusCode(404)
        .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        .end(error.toBuffer());
    });

    vertx.eventBus().consumer("REGISTER_ROUTE", handler -> {
      LOGGER.debug("==Before== Router:{}, Number of routes:{}", router, router.getRoutes().size());
      JsonObject message = (JsonObject) handler.body();
      HttpMethod method = HttpMethod.valueOf(message.getString("method"));
      router.route(method, message.getString("url"))
        .order(1)
        .handler(cxt -> cxt.json(message.getJsonObject("response")));
      LOGGER.debug("==After== Router:{}, Number of routes:{}", router, router.getRoutes().size());
    });

    vertx.createHttpServer().requestHandler(router).listen(handler -> {
      if (handler.succeeded()) {
        LOGGER.info("server started on http://0.0.0.0:{}", handler.result().actualPort());
        startPromise.complete();
      } else {
        LOGGER.catching(handler.cause());
        startPromise.tryFail(handler.cause());
      }

    });
  }
}
