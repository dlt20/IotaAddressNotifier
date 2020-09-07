package messages.sub;

import java.util.LinkedList;

import com.pengrad.telegrambot.TelegramBot;

import messages.MessageParent;
import readWrite.BotConfigReader;
import telegramBridge.User;

public class AboutResponse extends MessageParent {

	// Object encapsulates the response of the /about query
	// The /about command returns more information about the bot and sources
	
	private BotConfigReader inRead;
	
	public AboutResponse(User inUser, TelegramBot inBot, LinkedList<User> inUserList, BotConfigReader inRead) {
		// Construct via super-constructor
		super(inUser, inBot, inUserList);
		this.inRead = inRead;
	}

	public String resonseToIncomingMessage() {

		// returns more information about the bot
	
		// Read and add external information that is added to the /about response
		// Aim: dynamically change/add parts of the /about response during bot operation
		// this.onRead.getAboutString() returns empty String, if reading source fails
		String returner = this.inRead.getAboutString();
		returner = returner.replace("/n", "\n");
		
		// add sources for the IOTA/BTC price query
		returner = returner + "\nPrice data: Powered by CoinGecko API";
		
		// add disclaimer and return String
		returner = returner + "\n\nUse at your own risk. No guarantee for accurate, up-to-date or complete information.\n";
		return returner;
	}

}