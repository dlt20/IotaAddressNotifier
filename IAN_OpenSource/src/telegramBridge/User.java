package telegramBridge;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import org.json.JSONObject;
import readWrite.EncryptionObject;

public class User {

	// this calls describes the users of this bot
	// the class also bundles the print functions, that store user data
	// (configuration, addresses, etc.) encrypted on the hard-drive
	
	private long lastGetBalanceTime, timeNoSeedAgreement, lastInterationUnixStamp, muteTime;
	private String path, telegramID, fileAndFolderName;

	private HashMap<String, HashMap<String, Boolean>> mapAddressAndTransactions;
	private HashMap<String, User> mapIdToUserObject;

	private boolean watchReattach, agreedNotUseSeed, isAdmin;
	private int counter, msgNumberWithoutAnswers;
	private boolean allowed, autoFollow;
	private EncryptionObject encObj;


	public User(String inTelegramID, String inPath, EncryptionObject inEncObj, HashMap<String, User> inMapIdToUserObject) throws IOException {

		// Constructor to build a new user during operations (not parse from data storage)
		this.telegramID = inTelegramID;
		this.path = inPath;
		this.encObj = inEncObj;
		this.mapIdToUserObject = inMapIdToUserObject;
		this.mapAddressAndTransactions = new HashMap<String, HashMap<String, Boolean>>();

		this.watchReattach = true;
		this.agreedNotUseSeed = this.autoFollow = this.allowed = false;
		this.timeNoSeedAgreement = this.lastGetBalanceTime = 0;
		this.counter = this.msgNumberWithoutAnswers = 0;
		this.muteTime = 0;
		this.isAdmin = false;

		this.lastInterationUnixStamp = System.currentTimeMillis() / 1000;				// Short unix stamp, of last user interaction
		this.fileAndFolderName = this.encryptAndDeleteNonAlphanums(this.telegramID);	// Encrypted and cleaned user's telegram ID

		// Create folder for the user (folder Name -> AES encrypted and cleaned TelegramID)
		File ordner = new File(this.path + "/userdata/" + this.fileAndFolderName);
		if (!ordner.exists() || !ordner.isDirectory()) {
			ordner.mkdir();
		}

		// Check if chatlog file exists, if not create that file
		File f = new File(this.path + "/userdata/" + this.fileAndFolderName + "/Chatlog.txt");
		if (!f.exists()) {
			f.createNewFile();
		}

		// Add user to map: ID -> User Object
		this.mapIdToUserObject.put(this.telegramID, this);
	}
	
	public User(String inTelegramID, String inPath, EncryptionObject inEncObj, HashMap<String, User> inMapIdToUserObject,
			HashMap<String, HashMap<String, Boolean>> inHashMapToAdd, boolean inAllowed, boolean inAutoFollow, long inTimeNoSeedAgreement, boolean inNoSeed,
			boolean inWatchReattach, long inLastInterationUnixStamp) throws IOException {
		
		// Constructor to build a new user during read/parse users from data storage
		
		// Call other constructor and overwrite default values
		this(inTelegramID, inPath, inEncObj, inMapIdToUserObject); 
		this.mapAddressAndTransactions = inHashMapToAdd;
		this.allowed = inAllowed;
		this.autoFollow = inAutoFollow;
		this.timeNoSeedAgreement = inTimeNoSeedAgreement;
		this.agreedNotUseSeed = inNoSeed;
		this.watchReattach = inWatchReattach;
		this.lastInterationUnixStamp = inLastInterationUnixStamp;

	}

	public void setBundleHashAsConfirmed(String inAddress, String inPureBundleHash) {

		// Method input IOTA Address Hash and Bundle Hash
		// Method marks the bundle hash as confirmed to let the bot detect future bundle reattaches
		
		for (String keys : this.mapAddressAndTransactions.get(inAddress).keySet()) {
			if (keys.contains(inPureBundleHash)) {
				this.mapAddressAndTransactions.get(inAddress).put(keys, true);
			}
		}
	}

	public int checkBundleHash(String inAddress, String inBundleHash) {

		// Method input: IOTA address hash and bundle hash
		// Method returns if the bundle was 
		// 1: new and not known to the bot
		// 2: not already confirmed and reattached
		// 3: already confirmed and reattached
		
		if (this.mapAddressAndTransactions.get(inAddress).containsKey(inBundleHash)) {
			// Transaction already known -> means rettach, confirmed or unconfirmed?

			if (this.mapAddressAndTransactions.get(inAddress).get(inBundleHash)) {
				// this means already confirmed, ignore reattach
				return 3;
			} else {
				// unconfirmed reattach
				return 2;
			}
		} else {
			// new transaction on this address
			this.mapAddressAndTransactions.get(inAddress).put(inBundleHash, false);
			return 1;
		}
	}
	
	public void deleteUser() {

		// this method delets the user object from maps and lists, 
		// and deletes the user's data on the hard-drive
		
		// Remove user from map: ID -> User Object
		this.mapIdToUserObject.remove(this.telegramID);

		// Iterate through folder structure and delete data that is related to the user
		try {
			File file = new File(this.path + "/userdata/" +  this.fileAndFolderName);
			File ordner = new File(this.path + "/userdata/" +  this.fileAndFolderName);

			if (file.isDirectory()) {
				File[] listFiles = file.listFiles();

				for (int i = 0; i < listFiles.length; i++) {
					file = (listFiles[i]);
					if (file.isDirectory()) {
						File[] listFiles2 = file.listFiles();
						for (int k = 0; k < listFiles2.length; k++) {
							File file2 = (listFiles2[k]);
							file2.delete();
						}
					}
					file.delete();
				}
			}
			ordner.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public String addAddress(String inAddress) throws IOException {

		// This method adds an IOTA address to the user's list of observed addresses
		
		// Remove Checksum
		if (inAddress.length() == 90) {
			inAddress = inAddress.substring(0, 81);
		}

		// Check if the address is already in the HashMap
		if (this.mapAddressAndTransactions.containsKey(inAddress)) {
			return "*I'm already watching that Address.*";

		} else {
			// make new HashMap entry
			HashMap<String, Boolean> transactionStorage = new HashMap<String, Boolean>();
			this.mapAddressAndTransactions.put(inAddress, transactionStorage);
			
			// Update stored instance representation
			this.print();
			return "Address is now beeing watched.";
		}
	}

	public String stopWatchAddress(String inAddress) throws IOException {

		// This method removes an address from the list/map of 
		// addresses that are observed for this user
		
		// Remove Checksum
		if (inAddress.length() == 90) {
			inAddress = inAddress.substring(0, 81);
		}

		if (this.mapAddressAndTransactions.containsKey(inAddress)) {
			this.mapAddressAndTransactions.remove(inAddress);
			
			// Update stored instance representation
			try {
				this.print();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return "I am no longer watching that address.";
		} else {
			return "I can't find the address you gave me.";
		}
	}

	public void print() throws IOException {

		// this method (over)writes the user's configuration file
		// called if there are changes to the user's configuration
		// e.g., new addresses, setReattach, etc.
		
		File f = new File(path + "/userdata/" + this.fileAndFolderName + "/" + this.fileAndFolderName + ".txt");
		if (!f.exists()) {
			f.createNewFile();
		}

		// Build JSON objects
		JSONObject overAllPrintObject = new JSONObject();
		JSONObject addressObject = new JSONObject();

		// Iterate through addresses and transactions observed for the user
		for (String keyHigh : this.mapAddressAndTransactions.keySet()) {

			JSONObject transactionObject = new JSONObject();
			for (String keyLow : this.mapAddressAndTransactions.get(keyHigh).keySet()) {

				transactionObject.put(keyLow, this.mapAddressAndTransactions.get(keyHigh).get(keyLow).booleanValue());
			}
			addressObject.put(keyHigh, transactionObject);
		}

		// store user's configuration
		overAllPrintObject.put("addressMap", addressObject);
		overAllPrintObject.put("telegramID", this.telegramID);
		overAllPrintObject.put("isWatchReattach()", this.isWatchReattach());
		overAllPrintObject.put("isNoSeed()", this.isNoSeed());
		overAllPrintObject.put("timeNoSeedAgreement", this.timeNoSeedAgreement);
		overAllPrintObject.put("allowed", this.allowed);
		overAllPrintObject.put("autoFollow", this.autoFollow);
		overAllPrintObject.put("lastInterationUnixStamp", this.lastInterationUnixStamp);

		// Encrypting and writing user data
		FileWriter writer = new FileWriter(path + "/userdata/" + this.fileAndFolderName + "/" + this.fileAndFolderName + ".txt", false);
		writer.write(this.encObj.encodeAES(overAllPrintObject.toString()));
		writer.close();
	}

	public void printChat(String inChatLog) throws IOException {

		// This method writes the user's chatlog for debugging purpose
		
		// set up the file (might be newly created)
		File f = new File(this.path + "/userdata/" + this.fileAndFolderName + "/Chatlog.txt");
		if (!f.exists()) {
			f.createNewFile();
		}

		// Currently deactivated
		/*
		 * Calendar cal = Calendar.getInstance(); Date time = cal.getTime(); DateFormat
		 * formatter = new SimpleDateFormat();
		 * 
		 * //String output = formatter.format(time) + " - " + inChatLog + "\n";
		 * //FileWriter writer = new FileWriter(f, true);
		 */
		
		// Write data to hard-drive.
		FileWriter writer = new FileWriter(f, false);
		writer.write("Chatlog disabled.");
		writer.close();

	}

	// encode TelegramID via AES and remove non alphanumeric characters (folder structure)
	public String encryptAndDeleteNonAlphanums(String inText) {
		return this.encObj.encodeAES(inText).replaceAll("[^a-zA-Z0-9]", "");
	}

	public void setMapAddressAndTransactions(HashMap<String, HashMap<String, Boolean>> mapAddressAndTransactions) {
		this.mapAddressAndTransactions = mapAddressAndTransactions;

		// Update instance representation on hard-drive
		try {
			this.print();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
	// Getter and Setter
	public LinkedList<String> getAddList() {
		return new LinkedList<>(this.mapAddressAndTransactions.keySet());
	}
	
	public long getLastGetBalance() {
		return lastGetBalanceTime;
	}

	public void setLastGetBalance(long lastGetBalance) {
		this.lastGetBalanceTime = lastGetBalance;
	}

	public boolean isAutoFollow() {
		return autoFollow;
	}

	public void setAutoFollow(boolean AutoFollowValue) throws IOException {
		this.autoFollow = AutoFollowValue;

		// Update instance representation on hard-drive
		try {
			this.print();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isAllowed() {
		return allowed;
	}

	public void setAllowed(boolean allowed) throws IOException {
		this.allowed = allowed;
		this.print();
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

	public boolean isWatchReattach() {
		return watchReattach;
	}

	public void setWatchReattach(boolean watchReattach) throws IOException {
		this.watchReattach = watchReattach;
		this.print();
	}

	public boolean isNoSeed() {
		return agreedNotUseSeed;
	}

	public void setNoSeed(boolean noSeed) throws IOException {
		this.timeNoSeedAgreement = System.currentTimeMillis() / 1000;
		agreedNotUseSeed = noSeed;
		this.print();
	}

	public void settimeNoSeed(long INtimeNoSeed) {
		this.timeNoSeedAgreement = INtimeNoSeed;

		// Update instance representation on hard-drive
		try {
			this.print();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public long gettimeNoSeed() throws IOException {
		return this.timeNoSeedAgreement;
	}

	public String getTelegramID() {
		return telegramID;
	}

	public void setTelegramID(String telegramID) throws IOException {
		this.telegramID = telegramID;
		this.print();
	}

	public int getMsgNumberWithoutAnswers() {
		return msgNumberWithoutAnswers;
	}

	public void setMsgNumberWithoutAnswers(int msgNumberWithoutAnswers) {
		this.msgNumberWithoutAnswers = msgNumberWithoutAnswers;
	}


	public long getLastInterationUnixStamp() {
		return lastInterationUnixStamp;
	}

	public void setLastInterationUnixStamp(long lastInterationUnixStamp) {
		this.lastInterationUnixStamp = lastInterationUnixStamp;
	}

	public long getMuteTime() {
		return muteTime;
	}

	public void setMuteTime(long muteTime) {
		this.muteTime = muteTime;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}
	
}
