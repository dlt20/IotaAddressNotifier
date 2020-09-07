package tangleCommunication;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.zeromq.ZMQ;

import jota.IotaAPI;
import jota.model.Transaction;
import jota.utils.Checksum;
import messages.MessageUtil;
import telegramBridge.TelegramThread;
import telegramBridge.User;

public class TangleListenerThread extends Thread {

	// This thread monitors an IOTA node (via the node's ZMQ stream) and evaluates
	// the transactions that the node receives. The thread matches the transaction with the transactions
	// that the bot's user are observing.

	private String iotaZMQLink;
	private HashMap<String, LinkedList<User>> mapAddressToUser;
	private TelegramThread telBot;
	private IotaAPI api;
	private MessageUtil utility;
	private boolean runningFlag = true;

	public TangleListenerThread(String iotaApiLink, HashMap<String, LinkedList<User>> inMapAddressToUser, TelegramThread inTelBot, IotaAPI inApi, String[] priceString) {
		this.iotaZMQLink = iotaApiLink;
		this.mapAddressToUser = inMapAddressToUser;
		this.telBot = inTelBot;
		this.api = inApi;
		this.utility = new MessageUtil(priceString, this.api);
	}

	public void run() {

		// Subscribe to the IOTA node's ZMQ stream (tx -> transactions, sn -> confirmed transactions)
		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket requester = context.socket(ZMQ.SUB);
		requester.connect("tcp://" + this.iotaZMQLink);
		requester.subscribe("tx");
		requester.subscribe("sn");

		while (runningFlag) {

			// Receive the ZMQ stream subscription data
			byte[] reply = requester.recv(0);
			String output = new String(reply);

			// parse the incoming data for further processing
			String[] replySplit = output.split(" ");

			// Go through the broadcasted transactions
			if (replySplit[0].equals("tx")) {

				// Only look for transactions, that have value
				if (Long.parseLong(replySplit[3]) != 0) {

					// If the transaction is related to an observed address -> make notification
					if (this.mapAddressToUser.containsKey(replySplit[2])) {

						// Get user that are related to the address
						for (int u = 0; u < this.mapAddressToUser.get(replySplit[2]).size(); u++) {

							// build index and bundle has String (combining bundle hash with index of tx in bundle)
							String indexAndBundleHash = replySplit[6] + "_" + replySplit[8];

							// look for the address and bundle confirmation in the user's stored data
							// the received transaction/bundle might be new or a reattach
							int indicatorReattachOrNew = this.mapAddressToUser.get(replySplit[2]).get(u).checkBundleHash(replySplit[2], indexAndBundleHash);
							boolean doesUserWatchReattach = this.mapAddressToUser.get(replySplit[2]).get(u).isWatchReattach();
							String telegramID = this.mapAddressToUser.get(replySplit[2]).get(u).getTelegramID();

							// build Strings that are used in the reply to the user
							String printTxLong = replySplit[1];
							String printTx = printTxLong.substring(0, 7) + "...";

							// three options possible:
							switch (indicatorReattachOrNew) {
							case 1:

								// 1: new transaction -> new message
								String[] temp = { replySplit[8] };
								List<Transaction> tempList = this.api.findTransactionObjectsByBundle(temp);
								
								// prepare output message
								String messageOut = "Hi! You just got a new \nTransaction ([" + replySplit[1].substring(0, 10)
										+ "...](http://thetangle.org/transaction/" + replySplit[1] + ")) on: \n[" + replySplit[2].substring(0, 20)
										+ "...](http://thetangle.org/address/" + replySplit[2] + ")\n";

								// add value and msg String to the output message
								messageOut = messageOut + this.getValueTagAndMsg(tempList, replySplit[3], replySplit[4]);

								// send output message to the user, that observes the address
								this.telBot.sentMessage(messageOut, Long.parseLong(telegramID));
								break;
							case 2:

								// 2: not confirmed and reattach -> nothing
								if (doesUserWatchReattach) {
									String SendMSG = "Your unconfirmed Transaction ([" + printTx + "](http://thetangle.org/transaction/" + printTxLong
											+ ")) was reattached!";
									// message user that an reattach of an unconfirmed tx occured
									this.telBot.sentMessage(SendMSG, Long.parseLong(telegramID));
								}
								break;
							case 3:
								// 3: already confirmed and reattach -> nothing
								if (doesUserWatchReattach) {
									String SendMSG = "Your already confirmed Transaction ([" + printTx + "](http://thetangle.org/transaction/" + printTxLong
											+ ")) was reattached!";
									// message user that an reattach of a confirmed tx occured
									this.telBot.sentMessage(SendMSG, Long.parseLong(telegramID));
								}
								break;
							}
						}
					}
				}
			}

			// Go through the confirmed transactions that were received by the node
			if (replySplit[0].equals("sn")) {
				
				// parse the address that was confirmed from node data
				String addressConfirmed = replySplit[3];

				// If the transaction is related to an observed address -> make notification
				if (this.mapAddressToUser.containsKey(addressConfirmed)) {

					// parse further data from node's ZMQ confirmation event
					String bundleHashConfirmed = replySplit[6];
					String milestoneIndex = replySplit[1];
					String txHashConfirmed = replySplit[2];
					boolean addressWasRemovedFromUser = false;

					// Get user that are related to the address
					for (int u = 0; u < this.mapAddressToUser.get(addressConfirmed).size(); u++) {

						// Build message response
						Long userTelegramID = Long.parseLong(this.mapAddressToUser.get(addressConfirmed).get(u).getTelegramID());
						String confMessage = "Confirmed transaction: [" + txHashConfirmed.substring(0, 10) + "...](http://thetangle.org/transaction/"
								+ txHashConfirmed + ") ";
						confMessage = confMessage + "on address: [" + addressConfirmed.substring(0, 10) + "...](http://thetangle.org/address/"
								+ addressConfirmed + ") by Milestone: " + milestoneIndex + ".";

						// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
						// Following section implements the AutoFollow feature
						User tempUser = this.mapAddressToUser.get(addressConfirmed).get(u);
						if (tempUser.isAutoFollow()) {

							// Get whole bundle of confirmed tx
							String[] wrapperArrayBundleHash = { bundleHashConfirmed };
							List<Transaction> tempList = this.api.findTransactionObjectsByBundle(wrapperArrayBundleHash);

							String addressHashToRemove = "";
							String addressHashToFollow = "";
							long previousMaxValue = 0;

							// Find Input and Max Output Address
							for (Transaction iterateTx : tempList) {
								if (iterateTx.getValue() < 0) {
									addressHashToRemove = iterateTx.getAddress();
								} else {
									if (iterateTx.getValue() > previousMaxValue) {
										addressHashToFollow = iterateTx.getAddress();
										previousMaxValue = iterateTx.getValue();
									}
								}
							}

							// Set new address to follow, and delete old address
							if (tempUser.getAddList().contains(addressHashToRemove)) {
								try {
									// remove link old address to user
									tempUser.stopWatchAddress(Checksum.removeChecksum(addressHashToRemove));
									this.mapAddressToUser.get(addressHashToRemove).remove(tempUser);
									
									//check if that addresses is already linked, if not update object that maps addresses to users
									if (this.mapAddressToUser.containsKey(addressHashToFollow)) {
										this.mapAddressToUser.get(addressHashToFollow).add(tempUser);
									} else {
										// if addresses is not linked, create new <Address String, LinkedList<User>()> entry
										LinkedList<User> tempUserArray = new LinkedList<User>();
										tempUserArray.add(tempUser);
										this.mapAddressToUser.put(addressHashToFollow, tempUserArray);
									}
									tempUser.addAddress(Checksum.removeChecksum(addressHashToFollow));

									// Prepare output string
									addressWasRemovedFromUser = true;
									confMessage = confMessage + "\n\nYou enabled the /setAutofollow feature, therefore the new address ["
											+ addressHashToFollow.substring(0, 5) + "...](http://thetangle.org/address/" + addressHashToFollow
											+ ") is now watched (instead of [" + addressHashToRemove.substring(0, 5) + "...](http://thetangle.org/address/" + addressHashToRemove + ")).";
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
						// End of AutoFollow feature
						// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

						// Mark all transactions of the bundle as confirmed
						if (!addressWasRemovedFromUser) {
							this.mapAddressToUser.get(addressConfirmed).get(u).setBundleHashAsConfirmed(addressConfirmed, bundleHashConfirmed);
						}

						// Send final response to user
						this.telBot.sentMessage(confMessage, userTelegramID);
					}
				}
			}
		}
	}


	private String getValueTagAndMsg(List<Transaction> inList, String inValue, String inTag) {

		// Method returns a String that contains a transactions value, tag, and Message
		// To do so, a list of IOTA transaction and value String are handed to the method


		// parse value and tag from inputs -> add to return String
		String messageOut = "";
		try {
			messageOut += "Value:          " + utility.getBalance(inValue) + "\n";
		} catch (Exception e) {
			messageOut += "Value can be parsed.\n";
		}

		try {	
			messageOut = messageOut + "Tag:             " + inTag.replaceAll("99999", "") + "\n";
		} catch (Exception e) {
			e.printStackTrace();
			messageOut += "Tag can be parsed.\n";
		}


		try {
			// Colleact the singature framents from all transactions (of the bundle)
			String msgTemp = "";
			for (Transaction i : inList) {
				if (i.getValue() > 0) {
					msgTemp = utility.getStringFromTrytes(i.getSignatureFragments());
				}
			}

			// hand the parsed message to the return string, if length is between 0..50
			// Idea: long Strings can't be displayed properly in a telegram message
			if (msgTemp.length() > 0 && msgTemp.length() < 50) {
				// Clear String
				msgTemp = utility.clearMSG(msgTemp);
				messageOut = messageOut + "Message:    " + msgTemp;
			} else if (msgTemp.length() >= 50) {
				// if message is too long, respond with a substitute
				messageOut = messageOut + "Your message (or signature) is quite long and can't be displayed here!";
			} else if (msgTemp.length() == 0) {
				// if message is too long, respond with a substitute
				messageOut = messageOut + "No message was attached to the transaction.";
			}
		} catch (Exception e) {
			messageOut = messageOut + "Your Message (or signature) can't be parsed!";
		}

		return messageOut;
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - 
	// Getters and Setters

	public boolean isRunningFlag() {
		return runningFlag;
	}

	public void setRunningFlag(boolean runningFlag) {
		this.runningFlag = runningFlag;
	}

}
