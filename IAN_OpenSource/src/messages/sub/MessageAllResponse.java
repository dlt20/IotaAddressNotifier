package messages.sub;

import java.util.LinkedList;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;

import messages.MessageParent;
import telegramBridge.User;

public class MessageAllResponse extends MessageParent{

	// Object encapsulates /messageAll command/function.
	// The /messageAll command lets the bot message all users, that accepted to not use a seed
	// with a String that is handed over by the /messageAll command caller.
	// The /messageAll command can only be called by the bot-operator
	
	public MessageAllResponse(User inUser, TelegramBot inBot, LinkedList<User> inUserList) {
		// The /help command returns the help-String
		super(inUser, inBot, inUserList);
	}
	
	public String resonseToIncomingMessage(String inText) {
		
		// parse message that is to be send.
		String message = inText.replace("/messageAll", "");
		int countSentToUsers = 0;
		
		// iterate through all active and allowed users and send message.
		for (int iterateUser = 0; iterateUser < this.userList.size(); iterateUser++) {
			
			if (this.userList.get(iterateUser).isAllowed() && this.userList.get(iterateUser).isNoSeed()) {
				this.telBot.execute(new SendMessage(this.userList.get(iterateUser).getTelegramID(), message));
				countSentToUsers = countSentToUsers + 1;
			}
		}
		
		return "Message sent to " + countSentToUsers + " Users.";
	}

}
