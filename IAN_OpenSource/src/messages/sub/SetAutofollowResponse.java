package messages.sub;

import com.pengrad.telegrambot.TelegramBot;
import messages.MessageParent;
import telegramBridge.User;

public class SetAutofollowResponse extends MessageParent {

	// Object encapsulates the response of the /setAutoFollow command
	// The /setAutoFollow enables the bot to follow funds through the IOTA ledger,
	// if sent from one address to the next one

	public SetAutofollowResponse(User inUser, TelegramBot inBot) {
		// Construct via super constructor
		super(inUser, inBot, null);
	}

	public String resonseToIncomingMessage() {

		String returner = "";

		// Reverse the current setAutofollow Setting
		// On -> Off, and Off -> On
		try {
			if (this.user.isAutoFollow() == false) {
				this.user.setAutoFollow(true);
				returner = "Currently Autofollow is: *on*\n\n";
			} else {
				this.user.setAutoFollow(false);
				returner = "Currently Autofollow is: *off*\n\n";
			}

			// Add AutoFollow explaination
			returner = returner
					+ "IAN can automatically follow your funds. If IOTAs are send from an address that you are observing, IAN will start watching the address where your IOTAs were sent to. The old address will no longer be observed, when the transaction has been confirmed. \n\nIn the current beta, IAN will automatically start watching the address were the most IOTAs were sent to, if IOTAs were sent to multiple addresses.";
		} catch (Exception e) {
			e.printStackTrace();
		}

		return returner;
	}

}
