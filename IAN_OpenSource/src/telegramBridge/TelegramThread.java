package telegramBridge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import jota.IotaAPI;
import messages.TelegramResponse;
import readWrite.BotConfigReader;
import readWrite.EncryptionObject;
import readWrite.PriceThread;

public class TelegramThread extends Thread {

	// This thread queries the Telegram servers for user messages in real-time,
	// the messages are then processed. The thread responds accordingly to the users.
	
	private IotaAPI api;
	private TelegramBot telegramBot;
	private String path, masterID;
	private LinkedList<User> userList;
	private PriceThread priceThread;
	private EncryptionObject encObj;
	private HashMap<String, LinkedList<User>> mapAddressToUser;
	private HashMap<String, User> mapIdToUserObject;
	private BotConfigReader readWrite;

	private boolean runningFlag = true;

	public TelegramThread(IotaAPI inApi, TelegramBot inTelegramBot, PriceThread inPriceThread, String inPath, LinkedList<User> inUserList,
			String inMasterID, EncryptionObject inEncObj, HashMap<String, LinkedList<User>> inMapAddressToUser,
			BotConfigReader inReadWrite, HashMap<String, User> inMapIdToUserObject) {

		this.api = inApi;
		this.telegramBot = inTelegramBot;
		this.path = inPath;
		this.userList = inUserList;
		this.priceThread = inPriceThread;
		this.masterID = inMasterID;
		this.encObj = inEncObj;
		this.mapAddressToUser = inMapAddressToUser;
		this.mapIdToUserObject = inMapIdToUserObject;
		this.readWrite = inReadWrite;
	}

	public void run() {

		synchronized (this) {
			System.out.println("Telegram bot is running.");
			int lastOffset = 0; 	// Store telegram offset

			while (runningFlag) {

				// Fetch the current prices of BTC and IOTA
				String[] priceString = this.priceThread.getPriceArray();

				// Work Message Queue
				try {
					int initialOffset = this.readStatus();
					try {
						
						// query 100 updates starting from the last offset
						GetUpdates getUpdates = new GetUpdates().limit(100).offset(initialOffset).timeout(0);
						GetUpdatesResponse updatesResponse = telegramBot.execute(getUpdates);
						List<Update> updates = updatesResponse.updates();

						// processing the Telegram updates
						for (int j = 0; j < updates.size(); j++) {
							if (updates.get(j).message().text() != null) {
								
								// TelegramResponse returns the answer that is sent back to the user, based on user's message
								TelegramResponse responseToMessage = new TelegramResponse(updates.get(j).message().text(), updates.get(j).message().from().id(),
										updates.get(j).message().date(), this.userList, this.path, this.telegramBot, this.api, priceString, this.readWrite,
										this.masterID, this.encObj, this.mapAddressToUser, this.mapIdToUserObject);
								System.out.println(
										"New message by: " + updates.get(j).message().from().id() + ": " + updates.get(j).message().text().replace("\n", " "));
								LinkedList<String> response = responseToMessage.getResponse();

								// User List might have changed during processing of message
								this.userList = responseToMessage.getUserList();
								
								// Try to set user's interaction timestamp
								try {
									String telegramID = updates.get(j).message().from().id().toString();
									this.mapIdToUserObject.get(telegramID).setLastInterationUnixStamp(System.currentTimeMillis());
								} catch (Exception e) {
									System.out.println("Can't update user's interaction timestamp.");
								}

								// Reply to the user -> send message to the user
								for (int u = 0; u < response.size(); u++) {
									this.sentMessage(response.get(u), updates.get(j).message().from().id());
								}
							}
						}
						int currentOffset = updates.get(0).updateId() + 1;
						lastOffset = currentOffset;
						this.printStatus(currentOffset);
					} catch (Exception u) {
						if (!u.toString().contains("IndexOutOfBoundsException") && !u.toString().contains("SocketTimeoutException")
								&& !u.toString().contains("NullPointerException")) {
							u.printStackTrace();
						}
						
						// Problems occur, if user's edit older messages, the following should fix that.
						if (u.toString().contains("NullPointerException")) {
							u.printStackTrace();
							this.printStatus(lastOffset + 1);
							System.out.println("Current Offset: " + lastOffset + 1);
							System.out.println("Nutzer has edited an older Message!");
						}
						
						// Brief sleep, if an error occurred, to prevent handling an error message multiple times
						Thread.sleep(500);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void sentMessage(String inMsg, long inUserId) {
		
		// Method encapsulates sending a message (String MSG) to a user (long ID)
		telegramBot.execute(new SendMessage(inUserId, inMsg).parseMode(ParseMode.Markdown).disableWebPagePreview(true));
	}

	public int readStatus() throws IOException {

		// set up file representing status object
		File f = new File(this.path + "/Status.csv");
		boolean newFile = false;
		
		// check if status file exists, if not -> create File
		// ToDo add another initialization method if status file does not exist
		
		String returnStatusString = "0";
		if (!f.exists()) {
			f.createNewFile();
			newFile = true;
		}

		// if file did exist, read and parse data from file
		if (!newFile) {
			BufferedReader in = null;
			try {
				in = new BufferedReader(new FileReader(this.path + "/Status.csv"));
				String zeile = null;
				while ((zeile = in.readLine()) != null) {
					returnStatusString = zeile;
				}
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			in.close();
		}
		return Integer.parseInt(returnStatusString);
	}

	public void printStatus(int offset) throws IOException {

		// this methods writes the TelegramThreads status to the hardware
		// status is mainly the Telegram offset, a parameters that describes 
		// which Telegram updates are "new" and unseen
		
		String offsetString = "" + offset;

		// Write new Status File
		FileWriter writer = new FileWriter(path + "/Status.csv", false);
		writer.write(offsetString);
		writer.close();
	}
	
	// - - - - - - - - - - - - - - - - - - - - - 
	// Setters and Getters

	public LinkedList<User> getUserListe() {
		return this.userList;
	}

	public void setUserListe(LinkedList<User> e) {
		this.userList = e;
	}

	public boolean isRunningFlag() {
		return runningFlag;
	}

	public void setRunningFlag(boolean runningFlag) {
		this.runningFlag = runningFlag;
	}
}
