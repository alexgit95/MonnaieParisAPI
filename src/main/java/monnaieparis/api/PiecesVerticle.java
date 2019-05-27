package monnaieparis.api;

import java.util.List;

import com.google.gson.Gson;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
/**
 * 
 * @author AB
 *
 */
public class PiecesVerticle extends AbstractVerticle {

	private Gson gson = new Gson();

	/**
	 * Methode principale Vertx
	 */
	@Override
	public void start(final Future<Void> startFuture) throws Exception {

		final Router router = Router.router(vertx);

		router.get("/pieces").handler(this::getAllPieces);
		router.get("/pieces/my").handler(this::getMyPieces);
		router.get("/pieces/my/:id").handler(this::setPiecePossession);
		router.get("/pieces/missing").handler(this::getPiecesManquantes);
		router.get("/pieces/missing/:lattitude/:longitude").handler(this::getPiecesManquantesPlusProche);
		router.get("/pieces/mytop").handler(this::getPiecesRentable);

		vertx.createHttpServer().requestHandler(router) 
				.listen(8181, res -> {
					if (res.succeeded()) {
						startFuture.complete();
					} else {
						startFuture.fail(res.cause());
					}
				});

	}

	/**
	 * Permet de recuperer toutes les pieces de la collection
	 */
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

	/**
	 * Permet de recuperer toutes les pieces que l'on possede
	 * 
	 */
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

	/**
	 * Permet de recuperer toutes les pieces qu'on ne possede pas
	 */
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

	/**
	 * Permet de marquer une piece comme possedee
	 */
	private void setPiecePossession(RoutingContext ctx) {

		String nom = new String(ctx.request().getParam("id"));
		JsonObject query = new JsonObject().put("_id", nom);
		JsonObject update = new JsonObject().put("$set", new JsonObject().put("isPossede", true));
		getClient().updateCollection("PiecesMonnaieParis", query, update, res -> {
			if (res.succeeded()) {
				generateJson(ctx).end(query.encode());
			} else {
				res.cause().printStackTrace();
			}
		});

	}

	
	/**
	 * Permet de recuperer la liste des pieces non possedees les plus proches
	 * @param ctx
	 */
	private void getPiecesManquantesPlusProche(RoutingContext ctx) {
		JsonObject query = new JsonObject().put("isPossede", false);
		JsonObject lattitude = new JsonObject()
				.put("$gt", Double.parseDouble(ctx.request().getParam("lattitude")) - 0.5)
				.put("$lt", Double.parseDouble(ctx.request().getParam("lattitude")) + 0.5);
		JsonObject longitude = new JsonObject()
				.put("$gt", Double.parseDouble(ctx.request().getParam("longitude")) - 0.5)
				.put("$lt", Double.parseDouble(ctx.request().getParam("longitude")) + 0.5);
		query.put("lattitude", lattitude);
		query.put("longitude", longitude);

		getClient().find("PiecesMonnaieParis", query, res -> {
			if (res.succeeded()) {
				generateJson(ctx).end(gson.toJson(res.result(), List.class));
			} else {
				res.cause().printStackTrace();
			}
		});
	}

	
	/**
	 * Tri par ordre decroissant les pieces que l'on possede par leur valeur
	 * @param ctx
	 */
	private void getPiecesRentable(RoutingContext ctx) {
		JsonObject query = new JsonObject().put("isPossede", true);
		FindOptions fo = new FindOptions();
		fo.setSort(new JsonObject().put("valeur", -1));
		getClient().findWithOptions("PiecesMonnaieParis", query, fo, res -> {
			if (res.succeeded()) {
				generateJson(ctx).end(gson.toJson(res.result(), List.class));
			} else {
				res.cause().printStackTrace();
			}
		});
	}

	/**
	 * Permet de generer de maniere generique le header
	 * @return le header qui indique un format JSON
	 */
	private HttpServerResponse generateJson(RoutingContext ctx) {
		return ctx.response().putHeader("Content-Type", "application/json");
	}

	/**
	 * Permet de parametre le client mongodb
	 * 
	 */
	private MongoClient getClient() {
		JsonObject config = new JsonObject().put("host", "localhost");
		config.put("port", 27017).put("db_name", "monnaieparis");
		return MongoClient.createShared(vertx, config);
	}

}
