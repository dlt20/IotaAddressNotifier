package messages.sub;

import java.io.IOException;

import com.pengrad.telegrambot.TelegramBot;

import messages.MessageParent;
import telegramBridge.User;

public class SetReattachResponse extends MessageParent {

	// Object encapsulates the response of the /setReattach command
	// The /setReattach starts/stops the bot's observation of reattaches
	// in regards to an address added by a user
	
	public SetReattachResponse(User inUser, TelegramBot inBot) {
		super(inUser, inBot, null);
	}

	public String resonseToIncomingMessage() {

		String returner = "";
		
		// Reverse the current isWatchReattach Setting
		// On -> Off, and Off -> On
		try {
			if (this.user.isWatchReattach()) {
				this.user.setWatchReattach(false);
				returner = "Watching reattaches: *off*";
			} else {
				this.user.setWatchReattach(true);
				returner = "Watching reattaches: *on*";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return returner;
	}

}
