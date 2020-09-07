package messages.sub;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import com.pengrad.telegrambot.TelegramBot;
import messages.MessageParent;
import messages.MessageUtil;
import telegramBridge.User;

public class StartWatchResponse extends MessageParent {

	// Object encapsulates the response of the /StartWatch command
	// The /StartWatch command tries to add a new address to the user's set
	// of addresses that are observed by the bot

	private MessageUtil utility;
	private HashMap<String, LinkedList<User>> mapAddressToUser;

	public StartWatchResponse(User inUser, TelegramBot inBot, MessageUtil inUtil, HashMap<String, LinkedList<User>> inMapAddressToUser) {
		super(inUser, inBot, null);
		this.utility = inUtil;
		this.mapAddressToUser = inMapAddressToUser;
	}

	public String responseToIncomingMessage(String inMSG) {

		String returner = "";
		boolean valid = true;

		// Clean incoming MSG String, ideally only the IOTA address remains
		String cleanedStartAddressHash = inMSG.toLowerCase().replace("/startwatch", "").replace("\n", "").replace(" ", "");
		String validityAddressResponse = utility.getAddressFromTxt(cleanedStartAddressHash.toUpperCase());
		returner = validityAddressResponse;

		// Check if the cleaned String is more that 90 characters (either 81/90 chars
		// are the valid IOTA Address Hash Format)
		if (cleanedStartAddressHash.length() > 90) {
			valid = false;
			returner = "The address you entered has " + cleanedStartAddressHash.length()
					+ " characters. That is no a valid IOTA format. (81 or 90 characters lenght).";
		}

		// Check if the user already exceeded the maximum of 25 observable addresses
		int beobachteteAddressen = this.user.getAddList().size();
		if (beobachteteAddressen >= 25) {
			valid = false;
			returner = "To maintain fairness, IAN only watch 25 addresses per user. You reached that limit. IAN can show you all addresses, if you use the /showWatch command."
					+ "\nPlease use the /stopWatch command to free up some space and then add a new address.";
		}

		// Coordinator can't be watched, this String has to be altered if the Coo
		// address changes
		if (validityAddressResponse.contains("UDYXTZBE9GZGPM9SSQV9LTZNDLJIZM")) {
			valid = false;
			returner = "You can't watch milestones yet for performance reasons. If you want to get an overview what the coordinator is doing, check out: \n"
					+ "[The Iota Coordinator Monitor](http://coordinator.iotawatch.it)";
		}

		// Check if the validity response was successful (contains "!" if not
		// successful)
		if (!validityAddressResponse.contains("!") && valid) {
			returner = "You entered a valid IOTA address!";

			// Check if that address is already observed
			if (!returner.contains("already watching that")) {

				try {
					returner = returner + "\n" + this.user.addAddress(validityAddressResponse);

					//check if that addresses is already linked, if not update object that maps addresses to users
					if (this.mapAddressToUser.containsKey(validityAddressResponse)) {
						this.mapAddressToUser.get(validityAddressResponse).add(this.user);
					} else {
						// if addresses is not linked, create new <Address String, LinkedList<User>()> entry
						LinkedList<User> tempUser = new LinkedList<User>();
						tempUser.add(user);
						this.mapAddressToUser.put(validityAddressResponse, tempUser);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

				// Add balance reply and disclaimer
				returner = returner + "\nThe Balance on this address is: " + utility.getBalance(validityAddressResponse);
				returner = returner + "\n\nUse at your own risk. No guarantee for accurate, up-to-date or complete information.\n";
			}
		}
		return returner;
	}

	// Getters/Setters
	public HashMap<String, LinkedList<User>> getMapAddressToUser() {
		return mapAddressToUser;
	}

}
