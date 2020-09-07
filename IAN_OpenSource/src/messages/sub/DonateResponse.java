package messages.sub;

import java.util.LinkedList;

import com.pengrad.telegrambot.TelegramBot;

import messages.MessageParent;
import readWrite.BotConfigReader;
import telegramBridge.User;

public class DonateResponse extends MessageParent {
	
	// Object encapsulates the response of the /donate command
	// The /donate command returns a donation address to support the bot.
	
	private BotConfigReader inRead;

	public DonateResponse(User inUser, TelegramBot inBot, LinkedList<User> inUserList,  BotConfigReader inRead) {
		// Construct via super-constructor
		super(inUser, inBot, inUserList);
		this.inRead = inRead; 
	}

	public String resonseToIncomingMessage() {

		// Prepare and return response String to /donation command
		String returner = "If you like the bot and want to help me cover a part of the server cost, consider donating ";
		
		// Read and parse donation address from external online source: this.onRead.getDonationAddress()
		// Aim: dynamically change the /donate address -> no new jar compile if donation address changes
		returner = returner + "[via address](http://thetangle.org/address/" + this.inRead.getDonationAddress() + ").";

		return returner;
	}

}
