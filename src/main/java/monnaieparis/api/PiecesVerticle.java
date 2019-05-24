package monnaieparis.api;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.List;

import com.google.gson.Gson;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class PiecesVerticle extends AbstractVerticle {

	private Gson gson = new Gson();

	@Override
	public void start(final Future<Void> startFuture) throws Exception {

		final Router router = Router.router(vertx);

		router.get("/pieces").handler(this::getAllPieces);
		router.get("/pieces/my").handler(this::getMyPieces);
		router.get("/pieces/my/:id").handler(this::setPiecePossession);
		router.get("/pieces/missing").handler(this::getPiecesManquantes);
		router.get("/pieces/missing/:lattitude/:longitude").handler(this::getPiecesManquantesPlusProche);
		router.get("/pieces/my/top").handler(this::getPiecesRentable);

		router.get("/test/:id").handler(this::handleTest);

		vertx.createHttpServer().requestHandler(router::accept) // ici
				.listen(8181, res -> {
					if (res.succeeded()) {
						startFuture.complete();
					} else {
						startFuture.fail(res.cause());
					}
				});

	}

	private void handleTest(RoutingContext ctx) {
		String object = ctx.request().getParam("id");
		JsonObject response = new JsonObject().put("returnid", object);
		generateJson(ctx).end(response.encode());
	}

	private void getAllPieces(RoutingContext ctx) {
		JsonObject query = new JsonObject();
		getClient().find("PiecesMonnaieParis", query, res -> {
			if (res.succeeded()) {
				generateJson(ctx).end(gson.toJson(res.result(), List.class));
			} else {
				res.cause().printStackTrace();
			}
		});

	}

	private void getMyPieces(RoutingContext ctx) {

		JsonObject query = new JsonObject().put("isPossede", true);
		getClient().find("PiecesMonnaieParis", query, res -> {
			if (res.succeeded()) {
				generateJson(ctx).end(gson.toJson(res.result(), List.class));
			} else {
				res.cause().printStackTrace();
			}
		});

	}

	private void getPiecesManquantes(RoutingContext ctx) {

		JsonObject query = new JsonObject().put("isPossede", false);
		getClient().find("PiecesMonnaieParis", query, res -> {
			if (res.succeeded()) {
				generateJson(ctx).end(gson.toJson(res.result(), List.class));

			} else {
				res.cause().printStackTrace();
			}
		});
	}

	private void setPiecePossession(RoutingContext ctx) {
		String nom=new String(Base64.getDecoder().decode(ctx.request().getParam("id")),Charset.forName("ISO-8859-1"));
		System.out.println(nom);
		JsonObject query = new JsonObject().put("nom", nom);
				// Set the author field
		JsonObject update = new JsonObject().put("$set", new JsonObject().put("isPossede", true));
				getClient().updateCollection("PiecesMonnaieParis", query, update, res -> {
				  if (res.succeeded()) {
					  generateJson(ctx).end(query.encode());
				  } else {
				    res.cause().printStackTrace();
				  }
				});
	}

	private void getPiecesManquantesPlusProche(RoutingContext ctx) {
		// TODO
	}

	private void getPiecesRentable(RoutingContext ctx) {
		// TODO
	}

	private HttpServerResponse generateJson(RoutingContext ctx) {
		return ctx.response().putHeader("Content-Type", "application/json");
	}

	private MongoClient getClient() {
		JsonObject config = new JsonObject().put("host", "localhost");
		config.put("port", 27017).put("db_name", "monnaieparis");
		return MongoClient.createShared(vertx, config);
	}

}
