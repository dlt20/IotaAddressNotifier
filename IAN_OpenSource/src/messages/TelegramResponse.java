package messages;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;

import com.pengrad.telegrambot.TelegramBot;
import jota.IotaAPI;
import messages.sub.AboutResponse;
import messages.sub.DonateResponse;
import messages.sub.GetUserResponse;
import messages.sub.HelpResponse;
import messages.sub.MessageAllResponse;
import messages.sub.MessageOneResponse;
import messages.sub.NoSeedResponse;
import messages.sub.SetAutofollowResponse;
import messages.sub.SetReattachResponse;
import messages.sub.ShowBalanceResponse;
import messages.sub.ShowWatchResponse;
import messages.sub.StartWatchResponse;
import messages.sub.StatsResponse;
import messages.sub.StopWatchResponse;
import readWrite.BotConfigReader;
import readWrite.EncryptionObject;
import telegramBridge.User;

public class TelegramResponse {

	// Class bundles the responses to the commands that users send to the Telegram
	// Bot, individual responses can be found in the messages.sub package

	private long userID;
	private LinkedList<User> userList;
	private User user;
	private TelegramBot telegramBot;
	private IotaAPI api;
	private String text, path, currentPriceIotaInDollar, currentPriceIotaInEuro, masterID;
	private String[] priceArray;
	private BotConfigReader readWrite;
	private MessageUtil utility;
	private EncryptionObject encObj;
	private HashMap<String, LinkedList<User>> mapAddressToUser;
	private HashMap<String, User> mapIdToUserObject;

	public TelegramResponse(String inText, long inUserID, int inDate, LinkedList<User> inUserList, String inPath, TelegramBot inBot, IotaAPI inApi,
			String[] inPrice, BotConfigReader inReadWrite, String inMasterID, EncryptionObject inEncObj, HashMap<String, LinkedList<User>> inMapAddressToUser,
			HashMap<String, User> inMapIdToUserObject) throws IOException {

		this.priceArray = inPrice;
		this.readWrite = inReadWrite;
		this.text = inText;
		this.userID = inUserID;
		this.userList = inUserList;
		this.path = inPath;
		this.telegramBot = inBot;
		this.api = inApi;
		this.masterID = inMasterID;
		this.utility = new MessageUtil(this.priceArray, this.api);
		this.encObj = inEncObj;
		this.mapAddressToUser = inMapAddressToUser;
		this.mapIdToUserObject = inMapIdToUserObject;

		// Set IOTA Dollar + EURO prices
		this.currentPriceIotaInDollar = inPrice[0];
		this.currentPriceIotaInEuro = String.format(Locale.US, "%1$,.4f", (Double.parseDouble(inPrice[0]) / Double.parseDouble(inPrice[1])));

		// if the user that started the communication didn't communicate earlier, create
		// user instance and write file
		if (!inMapIdToUserObject.containsKey("" + this.userID)) {
			this.user = new User(String.valueOf(this.userID), this.path, this.encObj, this.mapIdToUserObject);
			this.userList.add(this.user);
			this.user.print();
			System.out.println("New user started communication, User ID: " + this.userID);
		} else {
			this.user = inMapIdToUserObject.get("" + this.userID);
		}
	}

	public LinkedList<String> getResponse() throws IOException {

		// marker, whether the bot understood the command
		boolean botUnderstoodCommand = false;

		// Prepare text input
		String inputTextLowerCase = this.text.toLowerCase();

		// Prepare returns
		String returner = "";
		LinkedList<String> responseList = new LinkedList<String>();

		// Print user's chatlog for debugging - though method is currently disabled
		this.user.printChat(this.text);

		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		// Start of the input processing:

		// Only proceed if the user accepted to not use the bot with a seed
		if (this.user.isAllowed()) {
			
			// Check if the user accepts to not use a seed
			if (!this.user.isNoSeed() && inputTextLowerCase.contains("/iwillnotuseaseed")) {
				botUnderstoodCommand = true;
				responseList.add(new NoSeedResponse(this.user, this.telegramBot, this.path).resonseToIncomingMessage());
			}
			
			if (this.user.isNoSeed()) {

				// Check if the user is spamming (more than 50 MSGs per 5 minutes)
				int timeDiff = (int) (((System.currentTimeMillis() - this.user.getLastInterationUnixStamp()) / 1000));
				int timeDiffSinceMute = (int) (((System.currentTimeMillis() - this.user.getMuteTime()) / 1000));

				// un-mute the user after 5 minutes
				if (timeDiff > 300) {
					this.user.setCounter(0);
					if (this.user.getCounter() >= 50) {
						responseList.add("You have been unmuted.");
					}
				}

				// if the user is not spamming, handle message
				if (this.user.getCounter() < 50) {

					// Print first warning, that the user is approaching the spam-mute
					if (this.user.getCounter() == 40) {
						responseList.add("Please do not spam! Otherwise the bot will start to ignore you for 5 minutes!");
					}

					// Respond to a "hi" message
					if (this.text.contains("hi") || this.text.contains("Hi")) {
						responseList.add("Hi,\nyou have some work for me?\n");
					}

					// Respond to a message using the command /help
					if (inputTextLowerCase.contains("/help") || inputTextLowerCase.equals("/start") || this.user.getMsgNumberWithoutAnswers() == 2) {
						botUnderstoodCommand = true;
						responseList.add(new HelpResponse().resonseToIncomingMessage());
					}

					// Respond to a message using the command /help
					if (inputTextLowerCase.contains("/userid")) {
						botUnderstoodCommand = true;
						responseList.add("Your user ID is: " + this.userID + "\n");
					}

					// Respond to a message asking to start observing an address
					if (inputTextLowerCase.contains("/startwatch")) {
						botUnderstoodCommand = true;
						responseList.add(new StartWatchResponse(this.user, this.telegramBot, this.utility, this.mapAddressToUser)
								.responseToIncomingMessage(inputTextLowerCase));
					}

					// Respond to a message asking to stop observing an address
					if (inputTextLowerCase.contains("/stopwatch")) {
						botUnderstoodCommand = true;
						responseList.add(new StopWatchResponse(this.user, this.telegramBot, this.utility, this.mapAddressToUser)
								.responseToIncomingMessage(inputTextLowerCase));
					}

					// Respond to a message asking to show all observed addresses
					if (inputTextLowerCase.contains("/showwatch")) {
						botUnderstoodCommand = true;
						responseList.add(new ShowWatchResponse(this.user).resonseToIncomingMessage());
					}

					// Respond to the request to delete all user's files.
					if (inputTextLowerCase.contains("/deleteuser")) {
						botUnderstoodCommand = true;
						responseList.add("Sad to see you go! If you're sure that you want to stop the bot and delete all of your data type: /YESdeleteUser");
					}

					// Respond to the confirmation of the request to delete all user's files.
					if (inputTextLowerCase.contains("/yesdeleteuser")) {
						botUnderstoodCommand = true;
						this.userList.remove(this.user);
						this.user.deleteUser();
						responseList.add("Goodby. All your data has been removed.");
					}

					// Respond to the request to start/stop watching reattaches
					if (inputTextLowerCase.contains("/setreattach")) {
						botUnderstoodCommand = true;
						responseList.add(new SetReattachResponse(this.user, this.telegramBot).resonseToIncomingMessage());
					}

					// Respond to the request to enable/disable the autofollow feature
					if (inputTextLowerCase.contains("/setautofollow")) {
						botUnderstoodCommand = true;
						responseList.add(new SetAutofollowResponse(this.user, this.telegramBot).resonseToIncomingMessage());
					}

					// Respond to the an /about request
					if (inputTextLowerCase.contains("/about")) {
						botUnderstoodCommand = true;
						responseList.add(new AboutResponse(this.user, this.telegramBot, this.userList, this.readWrite).resonseToIncomingMessage());
					}

					// Respond to the a /donate request
					if (inputTextLowerCase.contains("/donate")) {
						botUnderstoodCommand = true;
						responseList.add(new DonateResponse(this.user, this.telegramBot, this.userList, this.readWrite).resonseToIncomingMessage());
					}

					// Respond to the a /showStats request
					if (inputTextLowerCase.contains("/showstats")) {
						botUnderstoodCommand = true;
						responseList.add(new StatsResponse(this.user, this.telegramBot, this.utility).resonseToIncomingMessage(this.currentPriceIotaInDollar,
								this.currentPriceIotaInEuro));
					}

					// Respond to the a /showBalance request -> returns all user's observed addresses + IOTA balance
					if (inputTextLowerCase.contains("/showbalance")) {
						botUnderstoodCommand = true;
						responseList
						.add(new ShowBalanceResponse(this.user, this.telegramBot, this.utility).resonseToIncomingMessage(this.currentPriceIotaInDollar));
					}

					// Respond to the a /showBalance request -> returns all user's observed addresses + IOTA balance
					if (this.text.contains(this.readWrite.getAdminSecret())) {

						botUnderstoodCommand = true;
						this.user.setAdmin(true);
						responseList.add("You are now set as a bot admin.");
					}


					// Operator Commands -> can only be requested by operator (specified by Telegram ID in config)
					// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

					// Response to the request to return all users' telegram IDs and date of last contact
					if (inputTextLowerCase.contains("/getuser") && this.user.isAdmin() && this.user.getTelegramID().equals(this.masterID)) {
						botUnderstoodCommand = true;
						responseList.add(new GetUserResponse(this.user, this.telegramBot, this.userList).resonseToIncomingMessage());
					}

					// Response to the request of asking the bot to message a single use (identified by Telegram ID)
					if (inputTextLowerCase.contains("/messageall") && this.user.isAdmin() && this.user.getTelegramID().equals(this.masterID)) {
						botUnderstoodCommand = true;
						responseList.add(new MessageAllResponse(this.user, this.telegramBot, this.userList).resonseToIncomingMessage(inputTextLowerCase));
					}

					// Response to the request of asking the bot to message all users with String handed over
					if (inputTextLowerCase.contains("/messageone") && this.user.isAdmin() && this.user.getTelegramID().equals(this.masterID)) {
						botUnderstoodCommand = true;
						responseList.add(new MessageOneResponse(this.user, this.telegramBot, this.userList).resonseToIncomingMessage(inputTextLowerCase));
					}

					// if request contains "/" but no valid command -> offer help
					if (this.text.contains("/") && !botUnderstoodCommand) {
						responseList.add("Command not recognized. Type /help to see all valid commands.");
					}

					// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
					// Update MSG for the user -> used to identify spam
					this.user.setCounter(this.user.getCounter() + 1);

					// If commands were not understood, increase user's counter for unrecognized messages
					// Aim: send user a help response, if multiple unrecognized commands were received by the bot
					if (responseList.size() == 0) {
						this.user.setMsgNumberWithoutAnswers(this.user.getMsgNumberWithoutAnswers() + 1);
					} else {
						this.user.setMsgNumberWithoutAnswers(0);
					}

				} else {
					// AREA for SPAM detection:

					// user sent more than 50 msg, if that was in the last 5 minutes -> mute user
					if (timeDiff < 300) {

						boolean alreadyIsMuted = !(this.user.getMuteTime() == 0);

						if (alreadyIsMuted) {

							// un-mute the user after 5 minutes
							if (timeDiffSinceMute > 300) {
								this.user.setCounter(0);
								this.user.setMuteTime(0);
								responseList.add("You have been unmuted.");
							} else {
								// prepare String showing minutes and seconds, when next response is allowed
								int timeDiffOUT = 300 - (timeDiffSinceMute);
								responseList.add("Please do not spam! You have been muted and must wait\nfor *" + String.format("%d:%02d", (timeDiffOUT / (60)), ((timeDiffOUT) % 60))
								+ "* more minutes.");
							}
						} else {
							responseList.add("Please do not spam! You have been muted for 5 minutes.");
							this.user.setMuteTime(System.currentTimeMillis());
						}
					}
				}
			} else {
				// AREA where (new) users, that have not agreed to not use a seed are welocmed
				returner = returner + "Welcome to the IOTA Address Notifier Bot, or short IAN.";
				returner = returner
						+ " This bot will keep you informed about what's going on with you IOTA address(es). *Therefore you will NEVER have to enter your seed!*\n";
				returner = returner + "\nClick or type /IWillNotUseASeed to show that you understand that and will *NEVER* use your seed with this TelegramBot.";
				responseList.add(returner);
			} 
		} else {
			returner = returner + "IAN is currently in a closed beta. Currently, all spots are take. \nPlease check back later.";
			responseList.add(returner);
		}
		return responseList;
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	// Getters and Setters
	public LinkedList<User> getUserList() {
		return userList;
	}

	public void setUserList(LinkedList<User> userList) {
		this.userList = userList;
	}
}
