package messages.sub;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import com.pengrad.telegrambot.TelegramBot;

import messages.MessageParent;
import messages.MessageUtil;
import telegramBridge.User;

public class StopWatchResponse extends MessageParent{

	// Object encapsulates the response of the /StopWatch command
	// The /StopWatch command tries to remove an address from the user's set
	// of addresses that are observed by the bot
	
	private MessageUtil utility;
	private HashMap<String, LinkedList<User>> mapAddressToUser;
	
	public StopWatchResponse(User inUser, TelegramBot inBot, MessageUtil inUtil, HashMap<String, LinkedList<User>> inMapAddressToUser) {
		super(inUser, inBot, null);
		this.utility = inUtil;
		this.mapAddressToUser = inMapAddressToUser;
	}
	
	public String responseToIncomingMessage(String inMSG) {
		
		// Clean the input String, so that only the IOTA Address remains
		String cleanedString = inMSG.toLowerCase().replace("/stopwatch", "").replace("\n", "").replace(" ", "");
		
		// Check the Address for validity
		String returner = utility.getAddressFromTxt(cleanedString.toUpperCase());
		
		// Check if the address-validity request was successfull
		// if not, answer contains a "!"
		
		if (!returner.contains("!")) {
			try {
				// remove address from the map that maps addresses to the users
				this.mapAddressToUser.get(returner).remove(this.user);
				
				// remove the address from the user
				returner = this.user.stopWatchAddress(returner);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return returner;
	}
	
	// Getter for mapAddressToUser
	public HashMap<String, LinkedList<User>> getMapAddressToUser() {
		return mapAddressToUser;
	}
}
