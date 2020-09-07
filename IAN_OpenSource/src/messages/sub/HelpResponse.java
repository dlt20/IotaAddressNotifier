package messages.sub;

import messages.MessageParent;

public class HelpResponse extends MessageParent{
	
	// Object encapsulates the response of the /help command
	// The /help command returns the help-String
	
	public HelpResponse() {
		super(null, null, null);
	}
	
	public String resonseToIncomingMessage() {
				
		// Build the help response String.
		String returner = "You can use the following commands\n(substitute XYZ with an *address*):\n\n";
		returner = returner + "Start watching XYZ: /startWatch XYZ\n";
		returner = returner + "Stop watching XYZ: /stopWatch XYZ\n\n";
		returner = returner + "List all addresses: /showWatch\n";
		returner = returner + "List all balances: /showBalances\n\n";
		returner = returner + "Watch reattaches on/off: /setReattach\n";
		returner = returner + "Autofollow on/off: /setAutofollow\n\n";
		returner = returner + "Delete your data: /deleteUser\n\n";
		returner = returner + "Show tangle stats: /showStats\n";
		returner = returner + "Information about the bot: /about\n";
		returner = returner + "*Donate to help run the bot*: /donate\n";

		// Add notice to not use a seed with the bot.
		returner = returner
				+ "\n*Remeber to NEVER enter your seed!*\nIf you're not sure what seeds and addresses are, have a look [here](https://domschiener.gitbooks.io/iota-guide/content/chapter1/seeds-private-keys-and-addresses.html).";

		return returner;
	}

}
