package tangleCommunication;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;

import jota.IotaAPI;

public class NodeHealthThread extends Thread {

	// This thread monitors the IOTA Node's health and informs the bot's operator 
	// if the not falls behind in regards to Milestone synchronization.
	// The thread compares the bot's IOTA node with theTangle.com nodes
	
	private TelegramBot telBot;
	private IotaAPI api;
	private String masterTelegramID;
	private boolean runThread = true;
	private long currentIndex, currentSolidIndex, healtCheckHeartbeat;

	public NodeHealthThread(IotaAPI inApi, TelegramBot inTelBot, String inMasterTelegramID, long inHealtCheckHeartbeat) {
		this.telBot = inTelBot;
		this.api = inApi;
		this.masterTelegramID = inMasterTelegramID;
		this.healtCheckHeartbeat = inHealtCheckHeartbeat;
	}

	public void run() {

		// Marker if the operator has already been contacted with an error
		boolean sentErrorToOperator = false;
		
		while (runThread) {
			
			try {
				// read currentIndex from theTangle.com and the currentSolidIndex from own node
				currentIndex = new IotaAPI.Builder().protocol("https").host("nodes.thetangle.org").port("443").build().getNodeInfo().getLatestMilestoneIndex();
				currentSolidIndex = this.api.getNodeInfo().getLatestSolidSubtangleMilestoneIndex();

				// check if node is in synch
				long differenceInMilestones = currentIndex - currentSolidIndex;

				// if the difference is > 3 trigger an error message via Telegram to the bot's operator 
				// Idea: minor deviations are ok, since it takes a while for new milestone to move thorugh the network
				
				if (differenceInMilestones > 3) {
					String message = "Warning!/nThe node(s) queried by IAN is/are more than *3* milestones behind thetangle.com and therefore most likely no longer in synch.";
					message = message + "/n/nCorrect notifications can not be guaranteed at the moment.";
					message = message + "/n(" + currentSolidIndex + " vs. " + currentIndex + ")";
					this.sentMessage(message, Long.parseLong(this.masterTelegramID));
				} else {
					// noting happens, since own node is less than 3 milestones behind theTangle.com
				}

				// Print the synch status to the console
				//System.out.println("Synch status: " + currentSolidIndex + " vs. " + currentIndex + ".");
			
			} catch (Exception e) {
				
				// message operator also, if the currentMilestone can't be queried from theTangle.com
				// -> e.g., because of rateLimiting
				
				if (!sentErrorToOperator) {
					this.sentMessage("Error fetching current milestone from thetangle.com", Long.parseLong(this.masterTelegramID));
					sentErrorToOperator = true;
				}
			}

			// Pause the thread according to the healtCheckHeartbeat configuration
			try {
				Thread.sleep((this.healtCheckHeartbeat * 60 * 1000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	// Send a message to the operator via Telegram
	public void sentMessage(String inMsg, long inUserID) {
		this.telBot.execute(new SendMessage(inUserID, inMsg).parseMode(ParseMode.Markdown).disableWebPagePreview(true));
	}

	// Getters and Setters
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	public boolean isRunThread() {
		return runThread;
	}

	public void setRunThread(boolean runThread) {
		this.runThread = runThread;
	}

}
