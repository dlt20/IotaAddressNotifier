package readWrite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Locale;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PriceThread extends Thread {

	// This thread reads the IOTA price in USD and BTC price in USD and EURO 
	// every predetermined interval from CoinGeckos free API
	
	private boolean flagRuns = true;
	private long readerHeartbeat;
	private double price, BTC, EURO = -1;

	public PriceThread(long readerHeartbeat) {
		this.readerHeartbeat = (readerHeartbeat * 60 * 1000);
	}

	public void run() {

		while (flagRuns) {
			
			// Read Price from CoinGecko
			this.price = this.BTC = this.EURO = -1;
			
			try {
				String url = "https://api.coingecko.com/api/v3/simple/price?ids=iota%2Cbitcoin&vs_currencies=usd%2Ceur";
				double btcUSD, btcEUR, iotUSD = 0;

				// build json object based on query
				JsonObject jsonObject = readJsonFromUrl(url);
				
				// parse prices
				iotUSD = Double.parseDouble(jsonObject.get("iota").getAsJsonObject().get("usd").toString());
				btcUSD = Double.parseDouble(jsonObject.get("bitcoin").getAsJsonObject().get("usd").toString());
				btcEUR = Double.parseDouble(jsonObject.get("bitcoin").getAsJsonObject().get("eur").toString());

				// calculate relevant prices in USD and EURO
				this.price = iotUSD;
				this.BTC = btcUSD;
				this.EURO = (btcUSD / btcEUR);

				//System.out.println("Price is: " + this.price + " IOTA USD, " + this.BTC + " BTC USD, " + this.EURO + " USD/EUR");
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Can't fetch price!");
			}
			
			// Wait for next reading interval
			try {
				Thread.sleep(this.readerHeartbeat);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}

	}

	private static String appendAll(Reader rd) throws IOException {
		
		// Method appends all read Information and returns it as single String				
		StringBuilder strBuild = new StringBuilder();
		int readerData;
		while ((readerData = rd.read()) != -1) {
			strBuild.append((char) readerData);
		}
		return strBuild.toString();
	}

	private static JsonObject readJsonFromUrl(String url) throws IOException {
		
		// Method gets URL String handed over and reads all price related data from URL
		
		// Build Input Stream
		InputStream is = new URL(url).openStream();
		
		// Read data, transform data into JSON, return JSON
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = appendAll(rd);
			JsonObject json = new JsonParser().parse(jsonText).getAsJsonObject();
			return json;
		} finally {
			is.close();
		}
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	// Getters and Setters
	
	public String[] getPriceArray() {
		
		String[] priceString = { "", "", "" };

		priceString[2] = String.format(Locale.US, "%1$,.4f", this.getBTCPrice());
		priceString[0] = String.format(Locale.US, "%1$,.4f", this.getPrice());
		priceString[1] = String.format(Locale.US, "%1$,.4f", this.getEUROPrice());
		
		return priceString; 
	}
	
	public double getPrice() {
		return this.price;
	}

	public double getEUROPrice() {
		return this.EURO;
	}

	public double getBTCPrice() {
		return this.BTC;
	}

	public void setFlagRuns(boolean flagRuns) {
		this.flagRuns = flagRuns;
	}
}
