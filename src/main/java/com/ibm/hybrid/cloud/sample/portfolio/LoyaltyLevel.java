/*
       Copyright 2017 IBM Corp All Rights Reserved

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.ibm.hybrid.cloud.sample.portfolio;

//Standard HTTP request classes.  Maybe replace these with use of JAX-RS 2.0 client package instead...
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

//JMS 2.0
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

//JNDI
import javax.naming.InitialContext;
import javax.naming.NamingException;

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
/** Determine loyalty status based on total portfolio value.
 *  Also send a notification when status changes for a given user.
 */
public class LoyaltyLevel extends Application {
	private static final String OPENWHISK_ACTION   = "https://openwhisk.ng.bluemix.net/api/v1/namespaces/jalcorn%40us.ibm.com_dev/actions/PostLoyaltyLevelToSlack";
	private static final String OPENWHISK_USER     = "bc2b0a37-0554-4658-9ebe-ae068eb1aa22";
	private static final String OPENWHISK_PASSWORD = "45t2FZC1q1bv6OYUztZUjkYFaVNs5klaviHoE6gFvgEedu9akiE1YW6lChOxUgJb";

	private static final String NOTIFICATION_Q   = "jms/Portfolio/NotificationQueue";
	private static final String NOTIFICATION_QCF = "jms/Portfolio/NotificationQueueConnectionFactory";

	private Queue queue = null;
	private QueueConnectionFactory queueCF = null;
	private boolean initialized = false;

    @GET
    @Path("/")
	@Produces("application/json")
	public JsonObject getLoyalty(@QueryParam("owner") String owner, @QueryParam("loyalty") String oldLoyalty, @QueryParam("total") double total) {
		JsonObjectBuilder loyaltyLevel = Json.createObjectBuilder();

		String loyalty = "Basic";
		if (total > 1000000.00) {
			loyalty = "Platinum";
		} else if (total > 100000.00) {
			loyalty = "Gold";
		} else if (total > 50000.00) {
			loyalty = "Silver";
		} else if (total > 10000.00) {
			loyalty = "Bronze";
		}

		if (!loyalty.equals(oldLoyalty)) try {
			JsonObjectBuilder builder = Json.createObjectBuilder();
			builder.add("owner", owner);
			builder.add("old", oldLoyalty);
			builder.add("new", loyalty);

			JsonObject message = builder.build();

//			invokeJMS(message);
			invokeREST("POST", OPENWHISK_ACTION, message.toString(), OPENWHISK_USER, OPENWHISK_PASSWORD);
		} catch (Throwable t) { //in case MQ is not configured, just log the exception and continue
			t.printStackTrace();
		}

		loyaltyLevel.add("owner", owner);
		loyaltyLevel.add("loyalty", loyalty);
		return loyaltyLevel.build();
	}

	/** Connect to the server, and lookup the managed resources. */
	public void initialize() throws NamingException {
		System.out.println("Getting the InitialContext");
		InitialContext context = new InitialContext();

		//lookup our JMS objects
		System.out.println("Looking up our JMS resources");
		queueCF = (QueueConnectionFactory) context.lookup(NOTIFICATION_QCF);
		queue = (Queue) context.lookup(NOTIFICATION_Q);

		initialized = true;
		System.out.println("Initialization completed");
	}

	/** Send a JSON message to our notification queue.
	 */
	public void invokeJMS(JsonObject json) throws JMSException, NamingException {
		if (!initialized) initialize(); //gets our JMS managed resources (Q and QCF)

		QueueConnection connection = queueCF.createQueueConnection();
		QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

		TextMessage message = session.createTextMessage(json.toString());
		QueueSender sender = session.createSender(queue);
		sender.send(message);

		sender.close();
		session.close();
		connection.close();
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
