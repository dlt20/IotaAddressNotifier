package startBot;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedList;
import com.pengrad.telegrambot.TelegramBot;

import jota.IotaAPI;
import readWrite.UserDataReader;
import readWrite.PriceThread;
import readWrite.BotConfigReader;
import tangleCommunication.NodeHealthThread;
import tangleCommunication.TangleListenerThread;
import telegramBridge.TelegramThread;
import telegramBridge.User;

public class starter {

	private static String iotaApiLink, iotaZMQLink, masterTelegramID, telegramSecret, workingDirectory;
	
	private static String staticAesKeyFragment = "yxrEjX+QqkbG"; 		// internal half of the AES key
	private static long priceQueryHeartBeat = 10; 						// every 5 minutes
	private static long nodeHealtCheckHeartBeat = 15; 					// every 15 minutes

	public static void main(String[] args) throws NoSuchAlgorithmException {

		// Set Reader and further Bot Configuration
		BotConfigReader readWrite = new BotConfigReader(staticAesKeyFragment);
		iotaApiLink = readWrite.getIotaApiLink();
		iotaZMQLink = readWrite.getIotaZMQLink();
		masterTelegramID = readWrite.getMasterTelegramID();
		telegramSecret = readWrite.getTelegramSecret();
		workingDirectory = readWrite.getWorkingDir();

		//Build IOTA Api, Telegram Bot, and  Build WebData Reader
		IotaAPI iotaApi = new IotaAPI.Builder().protocol("http").host(iotaApiLink.split(":")[0]).port(iotaApiLink.split(":")[1]).build();
		iotaApi.getNodeInfo().getLatestSolidSubtangleMilestoneIndex();		// If this line throws an error, check your IOTA Node configuration
		final TelegramBot telBot = new TelegramBot(telegramSecret);
		
		// Start Price Query Thread
		PriceThread priceThread = new PriceThread(priceQueryHeartBeat);
		priceThread.start();

		// Read User Data Base
		HashMap<String, User> mapIdToUserObject = new HashMap<String, User>();
		UserDataReader inReader = new UserDataReader(workingDirectory, readWrite.getEncryObject(), mapIdToUserObject);
		
		LinkedList<User> userList = inReader.getUserList();
		HashMap<String, LinkedList<User>> mapAddressToUser = inReader.getAddressUserMap();

		// Telegram and Tangle Monitor Threads
		TelegramThread telThread = new TelegramThread(iotaApi, telBot, priceThread, workingDirectory, userList, masterTelegramID, readWrite.getEncryObject(), mapAddressToUser, readWrite, mapIdToUserObject);
		telThread.start();

		new TangleListenerThread(iotaZMQLink, mapAddressToUser, telThread, iotaApi, priceThread.getPriceArray()).start();

		// Start Node Health Checker Thread
		new NodeHealthThread(iotaApi, telBot, masterTelegramID, nodeHealtCheckHeartBeat).start();	
	}
}
