package messages.sub;

import com.pengrad.telegrambot.TelegramBot;

import messages.MessageParent;
import messages.MessageUtil;
import telegramBridge.User;

public class StatsResponse extends MessageParent {
	
	// Object encapsulates the response of the /stats command
	// The /stats command returns the recent tangle stats.
	
	private MessageUtil msgUtil;

	public StatsResponse(User inUser, TelegramBot inBot, MessageUtil inMsgUtil) {
		
		// build via super constructor
		super(inUser, inBot, null);
		this.msgUtil = inMsgUtil;
	}

	public String resonseToIncomingMessage(String inCurrentPrice, String inCurrentPriceEuro) {

		// Prepare response
		String returner = "";
		
		// Check if the price was parsed correctly, if the price was not
		// parsed correctly -> return only TPS and CTPS
		if (Double.parseDouble(inCurrentPrice) < 0) {
			returner = this.msgUtil.getTPS();
		
		// if the price was parsed correctly, return price and TPS/CTPS
		} else {
			returner = this.msgUtil.getTPS() + "\nCurrent Price: *$" + inCurrentPrice + " * or * " + inCurrentPriceEuro + "€*";
			returner = returner + "\nPrice data: Powered by CoinGecko API";
		}
		return returner;
	}

}

