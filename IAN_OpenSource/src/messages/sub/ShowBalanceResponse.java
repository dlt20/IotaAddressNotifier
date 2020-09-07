package messages.sub;

import com.pengrad.telegrambot.TelegramBot;

import messages.MessageParent;
import messages.MessageUtil;
import telegramBridge.User;

public class ShowBalanceResponse extends MessageParent {
	
	// Object encapsulates the response of the /showBalance command
	// The /showBalance command returns the balances of the user's watched addresses to the user
		
	private MessageUtil msgUtil;

	public ShowBalanceResponse(User inUser, TelegramBot inBot, MessageUtil inMsgUtil) {
		super(inUser, inBot, null);
		this.msgUtil = inMsgUtil;
	}

	public String resonseToIncomingMessage(String inCurrentPrice) {

		String returner = "";

		// Check if the user has not used the command in the last 60 seconds, otherwise the user has to wait
		// Aim: prevent user's calling the command to often, since the command results in a Node API query, that needs ressources
		long currentTime = System.currentTimeMillis() / 1000;
		long diffTime = currentTime - this.user.getLastGetBalance();
		
		// if the user has not used the bot in the last 60 seconds:
		if (diffTime >= 60) {
			
			// Check if the current IOTA price was queried successfully
			// IOTA price is "-1", if query went wrong
			if (Double.parseDouble(inCurrentPrice) < 0) {
				returner = "Apologies, this command does not work at the moment!";
			
			// If price was queried correctly, get balances and calculate response String
			} else {
				if (this.user.getAddList().size() > 0) {
					returner = this.msgUtil.getAllBalances(this.user.getAddList(), inCurrentPrice);
					returner = returner + "\n\nUse at your own risk. No guarantee for accurate, up-to-date or complete information.";
				} else {
					returner = "Apologies, but you have not entered an address yet!";
				}
			}
			// Change user's last getBalance timestamp
			this.user.setLastGetBalance(currentTime);
		} else {
			returner = "Getting the balances of your addresses takes some time! \nTherefore you can only use this command every 60 seconds.";
			returner = returner + "\n\nPlease wait for " + (60 - diffTime) + " more seconds.";
		}
		
		return returner;
	}

}

