package messages;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.LinkedList;
import java.util.Locale;
import java.util.regex.Pattern;

import jota.IotaAPI;

import java.net.*;
import java.io.*;

public class MessageUtil {

	// Class that bundles certain functions that are used in various other places.
	
	private IotaAPI api;
	private double priceEuro, priceBtc;

	public MessageUtil(IotaAPI inApi) {
		this.api = inApi;
	}

	public MessageUtil(String[] price, IotaAPI inApi) {
		
		this.api = inApi;	
		this.priceBtc = Double.parseDouble(price[2].substring(0, price[2].length() - 5).replace(",", ""));
		this.priceEuro = Double.parseDouble(price[1]);
	}

	
	public String getBalance(String inStringAddress) {
		
		// Input is either an IOTA address or an IOTA balance in form of a String
		// If input is an IOTA address -> query balance and return balance as formatted String
		// If input is an balance -> return balance as formatted String
		
		// Check if input is IOTA address or IOTA balance
		// if input is address -> get balance from address
		long iotaBalance = 0;
		try {
			iotaBalance = Long.parseLong(inStringAddress);
		} catch (Exception e) {
			iotaBalance = Long.parseLong(api.getBalances(1, new String[] {inStringAddress}).getBalances()[0]);
		}

		// check if balance is negative (might occur in the IOTA Bundle format for bundle inputs)
		// set Negative-marker, and opposite the negative number
		boolean negativ = false;
		if (iotaBalance < 0) {
			iotaBalance = iotaBalance * (-1);
			negativ = true;
		}

		// Initialize decimal formatter
		DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
		dfs.setDecimalSeparator('.');
		DecimalFormat df = new DecimalFormat("#.00", dfs);

		// build response String, String format for IOTA currency values, 
		// possible are IOTA, KI, MI, GI, TI
		
		String responseString = "";
		
		// Filter for TI and GI
		if (iotaBalance >= 1000000000) {
			double RESPP = (double) iotaBalance / 1000000000;
			if (RESPP >= 1000) {
				responseString = df.format(RESPP / 1000) + " TI";
			} else {
				responseString = df.format(RESPP) + " GI";
			}
		}
		
		// Filter for MI
		if (iotaBalance >= 1000000 && iotaBalance < 1000000000) {
			double RESPP = (double) iotaBalance / 1000000;
			responseString = df.format(RESPP) + " MI";
		}
		
		// Filter for KI
		if (iotaBalance >= 1000 && iotaBalance < 1000000) {
			double RESPP = (double) iotaBalance / 1000;
			responseString = df.format(RESPP) + " KI";
		}
		
		// Filter for IOTA
		if (iotaBalance < 1000) {
			responseString = iotaBalance + " IOTA";
		}
		
		// return String and opposite balance again, if original value was negative
		if (!negativ) {
			return responseString;
		} else {
			return "-" + responseString;
		}
	}

	public String getStringFromTrytes(String inMessage) {

		// Inital clear and cutoff character 9
		boolean messageValid = true;
		inMessage = inMessage.replace("99999", "");
		int countNines = 0;
		
		// Search for Character 9 in Message
		for (int h = 0; h < inMessage.length(); h++) {
			String tempGL = "" + inMessage.charAt(h);
			if (tempGL.equals("9")) {
				countNines = countNines + 1;
			}
		}
		
		// if input message is build by 9 -> invalid message
		if (inMessage.length() == countNines) {
			messageValid = false;
		}
		
		// Prepare output String
		String outputString = "";
		
		// Convert Trytes to ASCII
		if (messageValid) {
			String TRYTE_VALUES = "9ABCDEFGHIJKLMNOPQRSTUVWXYZ";
			String[] inputTrytes = new String[inMessage.length()];

			for (int k = 0; k < inMessage.length(); k++) {
				inputTrytes[k] = "" + inMessage.charAt(k);
			}

			for (int i = 0; i < inputTrytes.length; i += 2) {
				try {
					String[] trytes = new String[2];
					trytes[0] = inputTrytes[i];
					trytes[1] = inputTrytes[i + 1];
					int firstValue = TRYTE_VALUES.indexOf(trytes[0]);
					int secondValue = TRYTE_VALUES.indexOf(trytes[1]);
					int decimalValue = firstValue + secondValue * 27;
					char character = (char) (decimalValue);
					outputString += character;
				} catch (Exception e) {
				}
			}
		}
		
		// Clear and prepare output, return output as single Telegram-ready String
		outputString = outputString.replace("\n", "");
		outputString = outputString.replace("   ", "");
		return outputString;
	}

	public String clearMSG(String stringToClear) {
		
		// Method input: text String that should be cleared
		// Output: cleared String (only a-z, A-Z and 0-1)
		
		stringToClear = stringToClear.replaceAll("\n", "").replaceAll("_", "").replaceAll("-", "").replaceAll("   ", "");
		String returnString = "";
		
		// Iterate through String's chars and drop invalid characters
		for (int h = 0; h < stringToClear.length(); h++) {
			String singleChar = "" + stringToClear.charAt(h);
			if (Pattern.matches("\\w*", singleChar) || Pattern.matches("[?!,.<> \"$%&§/()=#+-€]*", singleChar)) {
				returnString = returnString + stringToClear.charAt(h);
			}
		}
		
		// Return cleared String
		return returnString;
	}


	public String getAddressFromTxt(String inText) {
		
		// Method input: text that is received from Telegram user (should include IOTA address)
		// Output either a valid IOTA address, or error message
		
		// Array of valid chars that build an IOTA address
		String[] validChar = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
				"S", "T", "U", "V", "W", "X", "Y", "Z", "9" };

		String parsedIotaAddress = "";
		boolean addressLengthEquals90 = false;

		// Iterate through input text and check every char for validity
		for (int h = 0; h < inText.length(); h++) {
			for (int j = 0; j < validChar.length; j++) {
				String equal = "" + inText.charAt(h);
				if (equal.equals(validChar[j]) && !addressLengthEquals90) {
					parsedIotaAddress = parsedIotaAddress + equal;
					if (parsedIotaAddress.length() == 90) {
						addressLengthEquals90 = true;
					}
				}
			}
		}

		// Check if address has the valid IOTA address length of 81 or 90 (incl. Checksum)
		boolean validAddress = false;
		
		// Check if address length is 90 -> remove checksum
		if (addressLengthEquals90) {
			parsedIotaAddress = parsedIotaAddress.substring(0, 81);
		} 
		
		// Check if address length is 81 -> set validity
		if (parsedIotaAddress.length() == 81) {
			validAddress = true;
		}
		
		// return either valid address or Telegram error msg.
		if (validAddress) {
			return parsedIotaAddress;
		} else {
			return "Please insert a valid IOTA address! In order to do that, the IOTA address has to be inserted right after the command in one single message.";
		}
	}

	public String getAllBalances(LinkedList<String> tempList, String price) {
		
		// Method inputs: list of addresses and current IOTA price in USD and EURO
		// Method returns a String with the requested addresses, their balances and balance value in USD/EURO
		
		// Initialize variables
		double currentPrice = Double.parseDouble(price);
		double totalBalanceUSD = 0;
		long totalAmount = 0;
		String output = "";
		String totalBalanceUSDString = "";
		String totalAmountIOTA = "";
		
		// get Balance from Address
		String[] balances = api.getBalances(100, tempList).getBalances();

		// Iterate through all addresses and their balance
		for (int g = 0; g < tempList.size(); g++) {
			
			// Calculate total balance, current balance
			String currBalance = balances[g];
			totalAmount = totalAmount + Long.parseLong(currBalance);
			
			double doublCurrBalance = (Double.parseDouble(currBalance));
			doublCurrBalance = doublCurrBalance / 1000000;
			
			// Dollar equivalent
			double balanceUSD = doublCurrBalance * currentPrice;
			totalBalanceUSD = totalBalanceUSD + balanceUSD;
			
			// Start preparing the output String (Telegram-ready)
			String balanceUSDString = String.format(Locale.US, "%1$,.2f", balanceUSD);
			totalBalanceUSDString = String.format(Locale.US, "%1$,.2f", totalBalanceUSD);
			output = output + "> [" + tempList.get(g).substring(0, 5) + "..](http://thetangle.org/address/"
					+ tempList.get(g) + ") " + this.getBalance(currBalance) + " ~ $" + balanceUSDString + "\n";
			totalAmountIOTA = "" + totalAmount;
		}
		
		// Calculate the total balance (all addresses) and convert to USD, EURO, and BTC eqiv.
		double DollarBalance = Double.parseDouble(totalBalanceUSDString.replaceAll(",", ""));
		double EuroBalance = (DollarBalance / this.priceEuro);
		double BTCBalance = (DollarBalance / this.priceBtc);

		// Build and return the output String
		output = output + "\n" + "*Total amount: " + this.getBalance(totalAmountIOTA) + " ~ $" + totalBalanceUSDString + "*";
		output = output + "\n equals ~ " + String.format(Locale.US, "%1$,.2f", EuroBalance) + " EUR";
		output = output + "\n equals ~ " + String.format(Locale.US, "%1$,.4f", BTCBalance) + " Bitcoin";
		return output;
	}

	public String getTPS() {
		
		// Method reads and returns TPS and CTPS values from
		// http://coordinator.iotawatch.it/Korrelation.csv
		
		// Set Decimal Format for return
		DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
		dfs.setDecimalSeparator('.');
		DecimalFormat df = new DecimalFormat("#0.00", dfs);

		String inputLine = ""; 
		String ctps = ""; 
		String tps = "";
		double doubleCtps  = 0;
		double doubleTps = 0;
		
		// Read and parse data from http://coordinator.iotawatch.it/Korrelation.csv
		try {
			URL oracle = new URL("http://coordinator.iotawatch.it/Korrelation.csv");
			BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));
			while ((inputLine = in.readLine()) != null) {
				if (inputLine.contains("5 min. average") && inputLine.contains("\"TPS")) {
					tps = inputLine.replace("\"", "").replace("(5 min. average),", "").replace(" ", "").replace("TPS", "");
					doubleTps = Double.parseDouble(tps);
				}
				if (inputLine.contains("5 min. average") && inputLine.contains("\"CTPS")) {
					ctps = inputLine.replace("\"", "").replace("(5 min. average),", "").replace(" ", "").replace("CTPS", "");
					doubleCtps = Double.parseDouble(ctps);
				}
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Prepare output String that can be send by Telegram
		double averageCtpsPerTps = doubleCtps / doubleTps;
		String output = "In the past 5 minutes the tangle statistics were on average ([Source](http://coordinator.iotawatch.it)): \n\n"
				+ "TPS: *" + tps + "*, CTPS: *" + ctps + "*\nConfirmation Ratio: *" + df.format(averageCtpsPerTps) + "*";
		
		if (averageCtpsPerTps >= 0.4) {
			output = "Tangle looks healthy. " + output;
		} else {
			output = "Tangle is a bit slow right now. " + output;
		}
		return output;
	}

	public String getLastMSG(String pfad) {
		
		// Method returns the last 5 messages of the user's chatlog
		// by reading the user's chatlog file (currently disabled)
		
		int cutOffLengthPerMessage = 50;
		int amountOfMaxReturnedMessages = 5;
		
		// Generate object to store the data read-in
		LinkedList<String> DataSpeicher = new LinkedList<String>();
		
		// Read the chatlog from chatlog-file
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(pfad));
			String zeile = null;
			while ((zeile = in.readLine()) != null) {
				DataSpeicher.add(zeile);
			}
			in.close();
		} catch (Exception e) {
			return "No chatlog to return.";
		}
		
		// Reverse msg order and cut the msgs into form cutOffLength and #MaxMessages
		String outReturn = "";
		for (int i = 1; i <= amountOfMaxReturnedMessages; i++) {
			if (DataSpeicher.size() > i) {
				String temp = DataSpeicher.get(DataSpeicher.size() - i);
				if (temp.length() > cutOffLengthPerMessage) {
					temp = temp.substring(0, cutOffLengthPerMessage);
				}
				outReturn = outReturn + temp + "\n";
			}
		}
		
		// return messages as single string for telegram
		return outReturn;
	}
}
