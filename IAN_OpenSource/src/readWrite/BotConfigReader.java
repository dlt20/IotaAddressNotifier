package readWrite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.json.JSONObject;

public class BotConfigReader {

	// Class provides the object that reads the initial bot-configuration
	// Configuration file must be located in the folder /botStorage along the main
	// directory

	private static String userDir = System.getProperty("user.dir");
	private static String workingDir = userDir + "/botStorage";
	private String externalAesKeyFragment, iotaZMQLink, iotaApiLink, masterTelegramID, telegramSecret, initialDonationAddress, aboutString, adminSecret;
	private long lastTime, currentTime;
	private EncryptionObject encryObject;
	private static String configPath = "/BC.txt";			// or /BotConfiguration.txt
	private static String aboutPath = "/AB.txt";						// or /about.txt

	public BotConfigReader() {

		// Check if folder exists, if not make folder
		new File(workingDir).mkdirs();
		this.currentTime = System.currentTimeMillis();
		this.lastTime = this.currentTime;
		this.aboutString = this.queryAboutString();
	}

	public BotConfigReader(String staticAesKeyFragment) {

		// Constructor
		this.externalAesKeyFragment = "";
		this.readBotConfiguration();
		this.encryObject = new EncryptionObject(staticAesKeyFragment, this.externalAesKeyFragment);
		this.currentTime = System.currentTimeMillis();
		this.lastTime = this.currentTime;
		this.aboutString = this.queryAboutString();
		
		// Calculate adminSecret
		this.adminSecret = this.encryObject.encodeAES(masterTelegramID);
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	// Write bot configuration (Plain Text)
	public void writeBotConfiguration() {

		// Build JOSN object, based on objects parameters
		JSONObject jsonOutput = new JSONObject();
		jsonOutput.put("externalAesKeyFragment", this.externalAesKeyFragment);
		jsonOutput.put("iotaApiLink", this.iotaApiLink);
		jsonOutput.put("iotaZMQLink", this.iotaZMQLink);
		jsonOutput.put("telegramSecret", this.telegramSecret);
		jsonOutput.put("masterTelegramID", this.masterTelegramID);
		jsonOutput.put("initialDonationAddress", this.initialDonationAddress);

		// (Over) write configuration file

		try {
			FileWriter writer = new FileWriter(BotConfigReader.workingDir + configPath, true);
			writer.write(jsonOutput.toString() + "\n");
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Read Bot Configuration (Plain Text)
	public void readBotConfiguration() {

		// Build the file object
		String tempReader = "";
		File PwdFile = new File(BotConfigReader.workingDir + configPath);

		// read the file object
		try {
			BufferedReader in = null;
			in = new BufferedReader(new FileReader(PwdFile));
			String zeile = null;
			while ((zeile = in.readLine()) != null) {
				tempReader += zeile;
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Build JSON Object
		JSONObject jsonInput = new JSONObject(tempReader);

		// Set variables based on read JSON object
		this.externalAesKeyFragment = jsonInput.getString("externalAesKeyFragment");
		this.iotaApiLink = jsonInput.getString("iotaApiLink");
		this.iotaZMQLink = jsonInput.getString("iotaZMQLink");
		this.telegramSecret = jsonInput.getString("telegramSecret");
		this.masterTelegramID = jsonInput.getString("masterTelegramID");
		this.initialDonationAddress = jsonInput.getString("initialDonationAddress");
	}

	public String getDonationAddress() {

		// Providing the donation address
		// Method returns the result of a new query every 30 minutes,
		// otherwise results of the last query are returned

		// Get current Time
		this.currentTime = System.currentTimeMillis();
		long timeDiff = this.currentTime - this.lastTime;
		this.lastTime = this.currentTime;

		// read new donation address every 30 minutes
		if (timeDiff > 1000 * 60 * 30) {
			this.readBotConfiguration();
			return this.initialDonationAddress;
		} else {
			return this.initialDonationAddress;
		}
	}

	private String queryAboutString() {

		// Method reads additional information regarding the /about command.
		// About can be customized by locating an about.txt on a webserver
		// files's URL can be specified in the bot's configuration file

		// Read data from file & Build the file object
		String aboutReturnString = "";
		File PwdFile = new File(BotConfigReader.workingDir + aboutPath);

		try {

			BufferedReader in = null;
			in = new BufferedReader(new FileReader(PwdFile));
			String zeile = null;
			while ((zeile = in.readLine()) != null) {
				aboutReturnString = aboutReturnString + "\n" + zeile;
			}
			in.close();

		} catch (Exception e) {
			System.out.println("About section can't be parsed.");
			return "";
		}

		return aboutReturnString.replace("XXXX", this.initialDonationAddress);
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	// Getters and Setters
	public String getWorkingDir() {
		return workingDir;
	}

	public String getStaticAesKeyFragment() {
		return externalAesKeyFragment;
	}

	public String getIotaApiLink() {
		return iotaApiLink;
	}

	public String getIotaZMQLink() {
		return iotaZMQLink;
	}

	public EncryptionObject getEncryObject() {
		return encryObject;
	}

	public String getMasterTelegramID() {
		return masterTelegramID;
	}

	public String getTelegramSecret() {
		return telegramSecret;
	}

	public String getInitialDonationAddress() {
		return initialDonationAddress;
	}

	public String getAboutString() {
		return aboutString;
	}

	public void setAboutString(String aboutString) {
		this.aboutString = aboutString;
	}

	public String getAdminSecret() {
		return adminSecret;
	}

	public void setAdminSecret(String adminSecret) {
		this.adminSecret = adminSecret;
	}
	
}
