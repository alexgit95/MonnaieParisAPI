package monnaieparis.api;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class PiecesVerticle extends AbstractVerticle {

	private Gson gson = new Gson();
	
	private String contentClient;

	@Override
	public void start(final Future<Void> startFuture) throws Exception {

		final Router router = Router.router(vertx);
		contentClient=FileUtils.readFileToString(
				new File("/var/www/html/monnaie.html"),
				Charset.forName("UTF-8"));
		System.setProperty("org.mongodb.async.type", "netty");
		router.get("/monnaie.html").handler(this::getClientHtml);
		router.get("/pieces").handler(this::getAllPieces);
		router.get("/pieces/backup").handler(this::backup);
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
		try {
		JsonObject query = new JsonObject().put("isPossede", false);
		getClient().find("PiecesMonnaieParis", query, res -> {
			if (res.succeeded()) {
				generateJson(ctx).end(gson.toJson(res.result(), List.class));

			} else {
				res.cause().printStackTrace();
			}
		});
		}catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void setPiecePossession(RoutingContext ctx) {
		String id=new String(ctx.request().getParam("id"));
		System.out.println(id);
		JsonObject query = new JsonObject().put("_id", id);
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
		
		
		return MongoClient.createShared(vertx, new JsonObject());
	}
	
	private void getClientHtml(RoutingContext ctx) {
		
			ctx.response().putHeader("content-type", "text/html").end(contentClient);
		
	}
	
	private void backup(RoutingContext ctx) {
		JsonObject query = new JsonObject();
		getClient().find("PiecesMonnaieParis", query, res -> {
			if (res.succeeded()) {
				String json = gson.toJson(res.result(), List.class);
				try {
					FileUtils.write(new File("../backup/backupMonnaie.json"), json, Charset.forName("UTF-8"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				generateJson(ctx).end(gson.toJson(res.result(), List.class));
			} else {
				res.cause().printStackTrace();
			}
		});
	}

}
