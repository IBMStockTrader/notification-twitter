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
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
public class LoyaltyLevel extends Application {
	private static Logger logger = Logger.getLogger(LoyaltyLevel.class.getName());

	private boolean initialized = false;
	private SimpleDateFormat format = null;
	private Twitter twitter = null;

    @GET
    @Path("/")
	@Produces("application/json")
//	@RolesAllowed({"StockTrader", "StockViewer"}) //Couldn't get this to work; had to do it through the web.xml instead :(
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
		logger.fine("Loyalty level = "+loyalty);

		if (!loyalty.equals(oldLoyalty)) try {
			logger.fine("Change in loyalty level detected");
			tweet(owner, oldLoyalty, loyalty);
		} catch (TwitterException te) { //in case Twitter credentials are not configured, just log the exception and continue
			logger.warning("Unable to send tweet.  Continuing without notification of change in loyalty level.");
			logException(te);
		} catch (Throwable t) { //in case Twitter credentials are not configured, just log the exception and continue
			logger.warning("An unexpected error occurred.  Continuing without notification of change in loyalty level.");
			logException(t);
		}

		loyaltyLevel.add("owner", owner);
		loyaltyLevel.add("loyalty", loyalty);

		return loyaltyLevel.build();
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
	private void tweet(String owner, String oldLoyalty, String loyalty) throws TwitterException {
		if (!initialized) initialize();

		Date now = new Date();
		String message = "On "+format.format(now)+", "+owner+" changed status from "+oldLoyalty+" to "+loyalty+". #IBMStockTrader";

		logger.fine("Sending following tweet: "+message);
		twitter.updateStatus(message);

		logger.info("Message tweeted successfully!");
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
