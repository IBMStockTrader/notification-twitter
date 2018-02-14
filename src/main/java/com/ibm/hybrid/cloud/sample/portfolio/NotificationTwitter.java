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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

//Logging (JSR 47)
import java.util.logging.Level;
import java.util.logging.Logger;

//JSON-P (JSR 353).  The replaces my old usage of IBM's JSON4J (com.ibm.json.java.JSONObject)
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

//JAX-RS 2.0 (JSR 339)
import javax.ws.rs.core.Application;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;

//Twitter for Java (Twitter4J)
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;


@ApplicationPath("/")
@Path("/")
/** Determine loyalty status based on total portfolio value.
 *  Also send a notification when status changes for a given user.
 */
public class NotificationTwitter extends Application {
	private static Logger logger = Logger.getLogger(NotificationTwitter.class.getName());

	private boolean initialized = false;
	private SimpleDateFormat format = null;
	private Twitter twitter = null;

    @POST
    @Path("/")
	@Consumes("application/json")
	@Produces("application/json")
//	@RolesAllowed({"StockTrader", "StockViewer"}) //Couldn't get this to work; had to do it through the web.xml instead :(
	public JsonObject notifyLoyaltyLevelChange(JsonObject input) {
		JsonObjectBuilder result = Json.createObjectBuilder();
		String message = null;

		if (input != null) try {
			logger.fine("Notifying about change in loyalty level");
			message = tweet(input.getString("owner"), input.getString("old"), input.getString("new"));
		} catch (TwitterException te) { //in case Twitter credentials are not configured, just log the exception and continue
			logger.warning("Unable to send tweet.  Continuing without notification of change in loyalty level.");
			logException(te);
			message = te.getMessage();
		} catch (Throwable t) { //in case Twitter credentials are not configured, just log the exception and continue
			logger.warning("An unexpected error occurred.  Continuing without notification of change in loyalty level.");
			logException(t);
			message = t.getMessage();
		} else {
			message = "No http body provided in call to Notification microservice!";
		}

		result.add("message", message);
		result.add("location", "Twitter");

		return result.build();
	}

	/** Get our Twitter object, and a date formatter 
	 */
	private void initialize() {
		logger.fine("Initializing Twitter API");

		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setDebugEnabled(true);
		builder.setOAuthConsumerKey(System.getenv("TWITTER_CONSUMER_KEY"));
		builder.setOAuthConsumerSecret(System.getenv("TWITTER_CONSUMER_SECRET"));
		builder.setOAuthAccessToken(System.getenv("TWITTER_ACCESS_TOKEN"));
		builder.setOAuthAccessTokenSecret(System.getenv("TWITTER_ACCESS_TOKEN_SECRET"));

		TwitterFactory factory = new TwitterFactory(builder.build());
		twitter = factory.getInstance(); //initialize twitter4j

		//Example: Monday, October 16, 2017 at 3:45 PM UTC
		format = new SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:mm a 'UTC'");

		logger.fine("Initialization completed successfully!");
		initialized = true;
	}

	/** Tweet a message to our @IBMStockTrader account.
	 * @throws TwitterException 
	 */
	private String tweet(String owner, String oldLoyalty, String loyalty) throws TwitterException {
		if (!initialized) initialize();

		Date now = new Date();
		String message = "On "+format.format(now)+", "+owner+" changed status from "+oldLoyalty+" to "+loyalty+". #IBMStockTrader";

		logger.fine("Sending following tweet: "+message);
		twitter.updateStatus(message);

		logger.info("Message tweeted successfully!");
		return message;
	}

	private static void logException(Throwable t) {
		logger.warning(t.getClass().getName()+": "+t.getMessage());

		//only log the stack trace if the level has been set to at least FINE
		if (logger.isLoggable(Level.FINE)) {
			StringWriter writer = new StringWriter();
			t.printStackTrace(new PrintWriter(writer));
			logger.fine(writer.toString());
		}
	}
}
