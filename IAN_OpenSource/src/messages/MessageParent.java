package messages;

import java.util.LinkedList;

import com.pengrad.telegrambot.TelegramBot;

import telegramBridge.User;

	// Parental class for all message response types

public class MessageParent {

	protected String keyWord, responseMsg;
	protected boolean validCommand;
	protected User user;
	protected TelegramBot telBot;
	protected LinkedList<User> userList;
	
	public MessageParent(User inUser, TelegramBot inBot, LinkedList<User> inUserList) {
		
		// constructor to be used by child-classes
		this.user = inUser;
		this.telBot = inBot;
		this.userList = inUserList;
		
	}
}
