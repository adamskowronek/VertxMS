package VertxMS.VertxMS;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new MainVerticle());
	}
	
	@Override
	public void start(Future<Void> fut) {
		Router router = Router.router(vertx);

		router.route().handler(BodyHandler.create());
		router.post("/save").handler(this::saveChart);
		router.get("/load/:userId").handler(this::getCharts);
		
		vertx.createHttpServer().requestHandler(router::accept).listen(
				// Retrieve the port from the configuration,
				// default to 8080.
				config().getInteger("http.port", 8888), result -> {
					if (result.succeeded()) {
						fut.complete();
					} else {
						fut.fail(result.cause());
					}
				});
	}

	private void saveChart(RoutingContext routingContext) {
		
		JDBCClient client = JDBCClient.createShared(vertx, new JsonObject()
				.put("url", "jdbc:mysql://localhost:3306/chart?serverTimezone=UTC")
				.put("driver_class", "com.mysql.jdbc.Driver")
				.put("max_pool_size", 30)
				.put("user", "root")
				.put("password", "root"));
		
		int uId = routingContext.getBodyAsJson().getInteger("userId");
		String chartData = routingContext.getBodyAsJson().getString("chart");
		
		String sql = "INSERT INTO charts VALUES (NULL, ?, ?)";
		
		JsonArray params = new JsonArray();
		params.add(chartData).add(uId);

		client.getConnection(car -> {
			if (car.succeeded()) {
				SQLConnection connection = car.result();
				connection.updateWithParams(sql, params, res -> {
					connection.close();
					if (res.succeeded()) {
						System.out.println("Insert succeeded.");
					} else {
						System.out.println("Insert failed.");
						System.out.println(res.cause());
					}
				});
			} else {
				System.out.println("Connection failed.");
				car.cause();
			}
		});
	}
	
	private void getCharts(RoutingContext routingContext) {
		
		JDBCClient client = JDBCClient.createShared(vertx, new JsonObject()
				.put("url", "jdbc:mysql://localhost:3306/chart?serverTimezone=UTC")
				.put("driver_class", "com.mysql.jdbc.Driver")
				.put("max_pool_size", 30)
				.put("user", "root")
				.put("password", "root"));
		
		String sql = "SELECT chart FROM charts WHERE userId = ?";
		int uId = Integer.parseInt(routingContext.request().getParam("userId"));
		JsonArray params = new JsonArray();
		params.add(uId);
		
		client.getConnection(car -> {
			if (car.succeeded()) {
				SQLConnection connection = car.result();
				connection.queryWithParams(sql, params, res -> {
					connection.close();
					if (res.succeeded()) {
						System.out.println("Fetch succeeded.");
						List<String> charts = new ArrayList<>();
						for (int i=0; i<res.result().getResults().size(); i++) {
							charts.add(res.result().getResults().get(i).getString(0));
						}
		
						Gson gson = new Gson();
						
						  routingContext.response()
					      .putHeader("content-type", "application/json")
					      .end(gson.toJson(charts));
						  
						
					} else {
						System.out.println("Fetch failed.");
						System.out.println(res.cause());
					}
				});
			} else {
				System.out.println("Connection failed.");
				car.cause();
			}
		});
	}
}