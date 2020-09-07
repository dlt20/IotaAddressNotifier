package messages.sub;

import java.io.File;
import java.io.IOException;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendPhoto;

import messages.MessageParent;
import telegramBridge.User;

public class NoSeedResponse extends MessageParent{

	// Object encapsulates the response of the /iwillnotuseaseed command
	// The /iwillnotuseaseed command enables the user to interact with the bot
	// + the startWatch command is explained via an telegram response to the user
	
	private String path;
	
	public NoSeedResponse(User inUser, TelegramBot inBot, String inPath) {
		super(inUser, inBot, null);
		this.path = inPath;
	}
	
	public String resonseToIncomingMessage() {
		
		// Prepare Telegram resonse
		String returner = "You can get an overview about all commands by typing: /help \n";
		
		// Mark user as having agreed to not use a seed
		try {
			this.user.setNoSeed(true);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Send user a picture, that helps explaining the /startWatch function
		File picture = new File(this.path + "/" + "tutorial.gif");
		SendPhoto sendPhoto = new SendPhoto(this.user.getTelegramID(), picture);
		sendPhoto.caption(
				"Great! Lets start now! \nIf you want notifications about an IOTA address, use the command \n/startWatch and add your address after the command in the same message (see photo).");
		
		// Add disclaimer to return String
		returner = returner + "Please note, that you will use the bot on your own risk.";
		this.telBot.execute(sendPhoto);
		
		return returner;
	}

}
