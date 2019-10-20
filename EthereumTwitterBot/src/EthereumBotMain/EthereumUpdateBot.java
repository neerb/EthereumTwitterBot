package EthereumBotMain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class EthereumUpdateBot 
{
	double lastPrice = 0;
	int totalPosted = 0;
	
	/*
	 * Method is called to begin the process
	 */
	void start()
	{
		// Scheduled execution every hour
		ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
		ses.scheduleAtFixedRate(new Runnable() {
		    @Override
		    public void run() {
		        TweetCurrentPriceOfEthereum();
		    }
		}, 0, 1, TimeUnit.HOURS);
	}
	
	/*
	 * This method tweets the current price of Ethereum along with
	 * the price difference from the previous tweet.
	 */
	void TweetCurrentPriceOfEthereum()
	{
		try
		{
			ConfigurationBuilder cf = new ConfigurationBuilder();

			cf.setDebugEnabled(true).setOAuthConsumerKey("Your OAuthConsumerKey here")
					.setOAuthConsumerSecret("Your OAuthConsumerSecret here")
					.setOAuthAccessToken("Your OAuthAccessToken here")
					.setOAuthAccessTokenSecret("Your OAuthAccessTokenSecret here");

			TwitterFactory tf = new TwitterFactory(cf.build());
			Twitter twitterInstance = tf.getInstance();
			
			double ethPrice = getCryptoPrice("ETH");
			double difference = ethPrice - lastPrice;
			
			twitterInstance.updateStatus("The current price of Ethereum is $" + ethPrice + ".  " + 
					"That is a $" + difference + " difference from an hour ago.");
			
			System.out.println(">> TWEET POSTED #" + (++totalPosted) + " - Price: $" + ethPrice + " Difference - $" + difference);
			
			lastPrice = ethPrice;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	/*
	 * This method gets the cryptocurrency data
	 * 
	 * Uses cryptonator API
	 */
	double getCryptoPrice(String symbol) throws Exception
	{
		final String urlHalf1 = "https://api.cryptonator.com/api/ticker/";
		final String urlHalf2 = "-usd";

		String url = urlHalf1 + symbol + urlHalf2;

		double price = -1;

		JsonParser parser = new JsonParser();

		String hitUrl = url;
		String jsonData = getJsonData(hitUrl);

		JsonElement jsonTree = parser.parse(jsonData);

		if (jsonTree.isJsonObject())
		{
			JsonObject jsonObject = jsonTree.getAsJsonObject();

			JsonElement ticker = jsonObject.get("ticker");

			if (ticker.isJsonObject())
			{
				JsonObject tickObject = ticker.getAsJsonObject();

				JsonElement priceData = tickObject.get("price");

				price = priceData.getAsDouble();
			}
		}

		return price;
	}
	
	/*
	 * This method opens a url stream and requests the data from a specified URL.
	 * The data that is returned from this is ideally Json objects, but it will also
	 * return whatever the server sends back from the request.
	 * 
	 * Returns a string containing(ideally) Json object
	 */
	String getJsonData(String urlString) throws IOException
	{
		BufferedReader reader = null;
		try
		{
			// Create URL
			URL url = new URL(urlString);
			// Create URL stream
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuffer buffer = new StringBuffer();

			int read;

			char[] chars = new char[1024];

			// Copies individual characters into buffer until there aren't anymore to read.
			while ((read = reader.read(chars)) != -1)
				buffer.append(chars, 0, read);

			return buffer.toString();
		}
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
		}
		finally
		{
			// Close the stream
			if (reader != null)
				reader.close();
		}

		return null;
	}
	
	/*
	 * Entry point main
	 */
	public static void main(String[] args)
	{
		EthereumUpdateBot bot = new EthereumUpdateBot();
		bot.start();
	}
}
