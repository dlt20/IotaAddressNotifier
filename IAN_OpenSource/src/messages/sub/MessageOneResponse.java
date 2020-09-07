package messages.sub;

import java.util.LinkedList;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;

import messages.MessageParent;
import telegramBridge.User;

public class MessageOneResponse extends MessageParent {

	// Object encapsulates /messageOne command/function.
	// The /messageOne command lets the bot message one user, that accepted to not use a seed
	// with a String that is handed over by the /messageOne command caller.
	// The /messageOne command can only be called by the bot-operator
	
	public MessageOneResponse(User inUser, TelegramBot inBot, LinkedList<User> inUserList) {
		super(inUser, inBot, inUserList);
	}

	public String resonseToIncomingMessage(String inText) {

		// Parse and prepare message
		String message = inText.replace("/messageone", "");
		
		// The message that is to be send is handed over by the operator and 
		// follows a **TARGET TELEGRAM ID++ pattern.
		// contruct targetUserTelegramId and message from command-String
		
		String targetUserTelegramId = message.substring(message.indexOf("**") + 2, message.indexOf("++"));
		message = message.replace(targetUserTelegramId, "");
		message = message.replace("**", "");
		message = message.replace("++", "");
		
		// Send message to the user
		this.telBot.execute(new SendMessage(targetUserTelegramId, message));
		return "Message sent to " + targetUserTelegramId + ".";

	}

}
