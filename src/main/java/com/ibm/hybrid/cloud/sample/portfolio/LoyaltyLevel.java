package com.ibm.hybrid.cloud.sample.portfolio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

//JSON-P (JSR 353).  The replaces my old usage of IBM's JSON4J (com.ibm.json.java.JSONObject)
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

//JAX-RS 2.0 (JSR 339)
import javax.ws.rs.core.Application;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Path;


@ApplicationPath("/")
@Path("/")
public class LoyaltyLevel extends Application {
	private static final String OPENWHISK_ACTION   = "https://openwhisk.ng.bluemix.net/api/v1/namespaces/jalcorn%40us.ibm.com_dev/actions/PostLoyaltyLevelToSlack";
	private static final String OPENWHISK_USER     = "bc2b0a37-0554-4658-9ebe-ae068eb1aa22";
	private static final String OPENWHISK_PASSWORD = "45t2FZC1q1bv6OYUztZUjkYFaVNs5klaviHoE6gFvgEedu9akiE1YW6lChOxUgJb";

    @GET
    @Path("/")
	@Produces("application/json")
	public static JsonObject getLoyalty(@QueryParam("owner") String owner, @QueryParam("loyalty") String oldLoyalty, @QueryParam("total") double total) {
		JsonObjectBuilder loyaltyLevel = Json.createObjectBuilder();

		String loyalty = "Basic";
		if (total > 1000000.00) {
			loyalty = "Platinum";
		} else if (total > 50000.00) {
			loyalty = "Gold";
		} else if (total > 50000.00) {
			loyalty = "Silver";
		} else if (total > 10000.00) {
			loyalty = "Bronze";
		}

		if (!loyalty.equals(oldLoyalty)) try {
			System.out.println(owner+" has changed from "+oldLoyalty+" to "+loyalty+".");

			JsonObjectBuilder builder = Json.createObjectBuilder();
			builder.add("owner", owner);
			builder.add("old", oldLoyalty);
			builder.add("new", loyalty);

			JsonObject message = builder.build();
			String input = message.toString();

			invokeREST("POST", OPENWHISK_ACTION, input, OPENWHISK_USER, OPENWHISK_PASSWORD);
		} catch (Throwable t) {
			t.printStackTrace();
		}

		loyaltyLevel.add("owner", owner);
		loyaltyLevel.add("loyalty", loyalty);
		return loyaltyLevel.build();
	}

	private static JsonObject invokeREST(String verb, String uri, String input, String user, String password) throws IOException {
		URL url = new URL(uri);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod(verb);
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setDoOutput(true);

		if ((user != null) && (password != null)) {
			String credentials = user + ":" + password;
			String authorization = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
			conn.setRequestProperty("Authorization", authorization);
		}

		if (input != null) {
			OutputStream body = conn.getOutputStream();
			body.write(input.getBytes());
			body.flush();
			body.close();
		}

		InputStream stream = conn.getInputStream();

//		JSONObject json = JSONObject.parse(stream); //JSON4J
		JsonObject json = Json.createReader(stream).readObject();

		stream.close();

		return json;
	}
}
