package info.cameronlund.gradescrape.api.v1.server;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class ScrapeProvider implements HttpHandler {
	public void handle(HttpExchange exchange) throws IOException
	{
		boolean success;
		long start = System.currentTimeMillis();
		JsonObject response = new JsonObject();

		try
		{
			success = handleRequest(exchange, response);
		} catch (Exception e)
		{
			e.printStackTrace();
			success = false;
		}

		// Write default values
		response.addProperty("success", success ? "1" : "0");
		// TODO Write why we failed if we did
		response.addProperty("time", (System.currentTimeMillis()-start) / 1000D);

		// Response setup
		exchange.getResponseHeaders().add("Content-Type", "application/json");
		exchange.getResponseHeaders().add("Content-Length", response.toString().getBytes().length+"");
		exchange.sendResponseHeaders(200, response.toString().getBytes().length);

		// Send response
		OutputStream os = exchange.getResponseBody();
		os.write(response.toString().getBytes());
		os.close();

		// Tell if failure
		if (!success)
			System.out.println(" - Failed a request!"); // TODO Write why we failed
	}

	public boolean handleRequest(HttpExchange httpExchange, JsonObject response)
	{
		return false;
	}
}
