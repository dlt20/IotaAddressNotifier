package messages.sub;

import messages.MessageParent;
import telegramBridge.User;

// Object encapsulates the response of the /ShowWatch command
// The /ShowWatch command returns all addresses that are observed for the user

public class ShowWatchResponse extends MessageParent{

	public ShowWatchResponse(User inUser) {
		// build via super constructor
		super(inUser, null, null);
	}
	
	public String resonseToIncomingMessage() {
		
		// prepare response String
		String returner = "";
		
		// respond with the total nubmer of watched addresses
		if (this.user.getAddList().size() > 0) {
			returner = "I am watching " + this.user.getAddList().size() + " address(es).";
		} else {
			returner = "I'm currently not watching an address for you!";
		}
		
		// if addresses are observed for the user, respond with addresses and links to theTangle.com
		for (int h = 0; h < this.user.getAddList().size(); h++) {
			returner = returner + "\n" + "[" + this.user.getAddList().get(h) + "]" + "(http://thetangle.org/address/" + this.user.getAddList().get(h) + ")\n";
		}
		
		// Add Disclaimer and return String
		returner = returner + "\nUse at your own risk. No guarantee for accurate, up-to-date or complete information.\n";
		return returner;
	}

}