package com.github.hegdekar;

import com.github.hegdekar.verticles.RouteRegistration;
import com.github.hegdekar.verticles.Server;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App {
  public static final Logger LOGGER = LogManager.getLogger(App.class);

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(Server.class.getName())
      .compose(server -> {
        return vertx.deployVerticle(RouteRegistration.class.getName())
          .onSuccess(id -> LOGGER.info("Application started Successfully"))
          .onFailure(LOGGER::catching);
      });
  }
}
