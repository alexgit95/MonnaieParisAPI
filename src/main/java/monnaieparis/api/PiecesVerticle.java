package monnaieparis.api;


import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;
/**
 * 
 * @author AB
 *
 */
public class PiecesVerticle extends AbstractVerticle {

	private Gson gson = new Gson();
	
	private String contentClient;
	
	private JsonObject config;

	/**
	 * Methode principale Vertx
	 */
	@Override
	public void start(final Future<Void> startFuture) throws Exception {

		final Router router = Router.router(vertx);

		contentClient=FileUtils.readFileToString(
				new File("/apps/monnaie.html"),
				Charset.forName("UTF-8"));
		
		router.get("/monnaie.html").handler(this::getClientHtml);

		System.setProperty("org.mongodb.async.type", "netty");
		
		router.route().handler(CorsHandler.create("*")
				.allowedMethod(io.vertx.core.http.HttpMethod.GET)
				.allowedMethod(io.vertx.core.http.HttpMethod.POST)
				.allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
				.allowedHeader("Access-Control-Request-Method")
				.allowedHeader("Access-Control-Allow-Credentials")
				.allowedHeader("Access-Control-Allow-Origin")
				.allowedHeader("Access-Control-Allow-Headers")
				.allowedHeader("Content-Type"));
		
		
		

		router.get("/pieces").handler(this::getAllPieces);
		router.get("/pieces/backup").handler(this::backup);
		router.get("/pieces/my").handler(this::getMyPieces);
		router.get("/pieces/my/:id").handler(this::setPiecePossession);
		router.get("/pieces/missing").handler(this::getPiecesManquantes);
		router.get("/pieces/missing/:lattitude/:longitude").handler(this::getPiecesManquantesPlusProche);
		router.get("/pieces/mytop").handler(this::getPiecesRentable);
		
		
		ConfigStoreOptions fileStore = new ConfigStoreOptions()
				  .setType("file")
				  .setFormat("properties")
				  .setConfig(new JsonObject().put("path", "props.properties"));
		
		ConfigRetrieverOptions options = new ConfigRetrieverOptions()
				  .addStore(fileStore);
		
		ConfigRetriever retriever = ConfigRetriever.create(vertx, options);
		
		retriever.getConfig(ar -> {
			  if (ar.failed()) {
			   System.out.println("Erreur lors du chargement du fichier config");
			  } else {
			    config = ar.result();
			    
			  }
			});
		
		
		

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

	/**
	 * Permet de marquer une piece comme possedee
	 */
	private void setPiecePossession(RoutingContext ctx) {

		LocalDateTime newdate = LocalDateTime.now();
		try {
			String nom = new String(ctx.request().getParam("id"));
			JsonObject query = new JsonObject().put("_id", nom);
			JsonObject update = new JsonObject().put("$set", new JsonObject().put("isPossede", true).put("dateAcquisition", newdate.toString()));
			getClient().updateCollection("PiecesMonnaieParis", query, update, res -> {
				if (res.succeeded()) {
					generateJson(ctx).end(query.encode());
				} else {
					res.cause().printStackTrace();
				}
			});
		
		}catch(Exception e) {
			e.printStackTrace();
			throw e;
		}

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
		
		
		try {
		JsonArray hostTable= new JsonArray()
				.add(new JsonObject().put("host", config.getString("host1")).put("port", 27017));
				hostTable.add(new JsonObject().put("host", config.getString("host2")).put("port", 27017));
				hostTable.add(new JsonObject().put("host", config.getString("host3")).put("port", 27017));


				JsonObject config2 = new JsonObject().put("hosts",hostTable);
				config2.put("db_name", config.getString("db_name"));
				config2.put("username", config.getString("username"));
				config2.put("password", config.getString("password"));
				config2.put("ssl", true);
				config2.put("authSource","admin");

				return MongoClient.createShared(vertx, config2);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
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
