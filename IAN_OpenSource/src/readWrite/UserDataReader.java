package readWrite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.json.JSONObject;

import telegramBridge.User;

public class UserDataReader {

	// File reads user data from hard drive when programm is carried out
	// Some data has to be stored, e.g. observed addresses, transactions, configurations, etc.
	
	private String LocalPath;
	private LinkedList<User> UserList;
	private EncryptionObject encObj;
	private HashMap<String, User> mapIdToUserObject;

	public UserDataReader(String path, EncryptionObject inEncObj, HashMap<String, User> inMapIdToUserObject) {
		
		this.LocalPath = path;
		this.encObj = inEncObj;
		this.mapIdToUserObject = inMapIdToUserObject;

		// Reading the users' data
		this.UserList = new LinkedList<User>();
		
		// All user data is stored in a folder and .txt file that relates to the users' telegram ID
		File dir = new File(this.LocalPath + "/userdata/"); // current directory
		try {
			File[] files = dir.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					
					// The relevantUserID is formed by encryption and cleaning the Telegram ID
					String relevantUserID = file.getName().toString();
					
					// The configuration can be found in this file:
					File userConfigFile = new File(file.getAbsolutePath() + "/" + relevantUserID + ".txt");
					
					if (userConfigFile.exists()) {
						String readFile = "";

						// Read data from the user's config file
						BufferedReader in = null;
						try {
							in = new BufferedReader(new FileReader(file.getAbsolutePath() + "/" + relevantUserID + ".txt"));
							String zeile = null;
							while ((zeile = in.readLine()) != null) {
								readFile = zeile;
							}
							in.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						in.close();

						// Read and Decrypte file
						JSONObject jReader = new JSONObject(this.encObj.decodeAES(readFile));

						// Build address map - later added to the user
						JSONObject jAddresses = jReader.getJSONObject("addressMap");
						HashMap<String, HashMap<String, Boolean>> hashMapToAdd = new HashMap<String, HashMap<String, Boolean>>();

						// Put the read addresses into the map
						Iterator<String> addressKeys = jAddresses.keys();
						while (addressKeys.hasNext()) {
							String addressKey = addressKeys.next();

							JSONObject jTransaction = jAddresses.getJSONObject(addressKey);
							HashMap<String, Boolean> transMap = new HashMap<String, Boolean>();

							// Get Transactions
							Iterator<String> transKeys = jTransaction.keys();
							while (transKeys.hasNext()) {
								String transKey = transKeys.next();
								transMap.put(transKey, jTransaction.getBoolean(transKey));
							}

							// Build final HashMap
							hashMapToAdd.put(addressKey, transMap);
						}
												
						// Set other user parameters
						String telegramID = jReader.getString("telegramID");
						Boolean isWatchReattach = jReader.getBoolean("isWatchReattach()");
						Boolean isNoSeed = jReader.getBoolean("isNoSeed()");
						Long timeNoSeedAgreement = jReader.getLong("timeNoSeedAgreement");
						Boolean allowed = jReader.getBoolean("allowed");
						Boolean autoFollow = jReader.getBoolean("autoFollow");

						// Parse the user's chatlog file to get the last interaction timestamp
						File userChatlog = new File(file.getAbsolutePath() + "/" + "Chatlog" + ".txt");
						long lastInteraction = (userChatlog.lastModified() / 1000);

						// build new user instance based on the data that was just read
						User newUser = new User(telegramID, this.LocalPath, this.encObj, this.mapIdToUserObject, hashMapToAdd, allowed, autoFollow,
								timeNoSeedAgreement, isNoSeed, isWatchReattach, lastInteraction);
						UserList.add(newUser);
	
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public HashMap<String, LinkedList<User>> getAddressUserMap() {

		// Return Map that links addresses with users
		HashMap<String, LinkedList<User>> returnMap = new HashMap<String, LinkedList<User>>();
		
		// for each user the map entries are generated
		for (int z = 0; z < this.UserList.size(); z++) {

			for (int i = 0; i < this.UserList.get(z).getAddList().size(); i++) {
				String tempAddress = this.UserList.get(z).getAddList().get(i);

				if (returnMap.containsKey(tempAddress)) {
					returnMap.get(tempAddress).add(this.UserList.get(z));
				} else {
					LinkedList<User> tempUserList = new LinkedList<User>();
					tempUserList.add(this.UserList.get(z));
					returnMap.put(tempAddress, tempUserList);
				}
			}
		}

		return returnMap;
	}

	public LinkedList<User> getUserList() {
		
		// Print status log for reader
		// Only printed to console to show if/and how many users were read from hard-drive
		for (int z = 0; z < this.UserList.size(); z++) {
			System.out.print("User: " + this.UserList.get(z).getTelegramID() + " (" + this.UserList.get(z).getAddList().size() + "), ");
			
			// Print addresses, that are observed for the user
			for (int u = 0; u < this.UserList.get(z).getAddList().size(); u++) {
				System.out.print(this.UserList.get(z).getAddList().get(u).substring(0, 5) + ".., ");
			}
			
			// Print some of the user's configurations
			System.out.print(" - Allowed: " + this.UserList.get(z).isAllowed());
			System.out.print(" - AutoFollow: " + this.UserList.get(z).isAutoFollow());
			System.out.print(" - SEED: " + this.UserList.get(z).isNoSeed());
			System.out.println();
		}
		return this.UserList;
	}

}
