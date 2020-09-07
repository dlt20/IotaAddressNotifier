package messages.sub;

import java.util.Collections;
import java.util.LinkedList;

import com.pengrad.telegrambot.TelegramBot;

import messages.MessageParent;
import telegramBridge.SortUser;
import telegramBridge.User;

public class GetUserResponse extends MessageParent {

	// Object encapsulates the response of the /getUser query
	// The /getUser command returns a list of all active and inactive users, 
	// their telegramId and #watched addresses. Can only be called by operator.
	
	public GetUserResponse(User inUser, TelegramBot inBot, LinkedList<User> inUserList) {
		// Construct via super-constructor
		super(inUser, inBot, inUserList);
	}

	
	@SuppressWarnings("unchecked")
	public String resonseToIncomingMessage() {

		// returns a list of all active and inactive users.
		// inactive: agreed to not use a seed, but not currently watching addresses
		// active: agreed to not use a seed, min. 1 watched address
		
		LinkedList<SortUser> activeUser = new LinkedList<>();
		LinkedList<SortUser> passiveUser = new LinkedList<>();

		// iterate through all users that are registered with the bot
		for (int n = 0; n < this.userList.size(); n++) {
			
			// Check if users have agreed to not use a seed
			if (this.userList.get(n).isNoSeed()) {
				
				// Get number of watched addresses
				int numWatchedAddresses = this.userList.get(n).getAddList().size();

				// Get Time in Days since last active
				long currentTimeUnix = System.currentTimeMillis();
				long timeDifference = currentTimeUnix - (this.userList.get(n).getLastInterationUnixStamp());
				int timeDiffInDays = (int) (timeDifference / (1000 * 60 * 60 * 24));

				// Passive User
				if (numWatchedAddresses == 0) {
					passiveUser.add(new SortUser(this.userList.get(n).getTelegramID(), timeDiffInDays, numWatchedAddresses));
					Collections.sort(passiveUser);
				}

				// Active User
				if (numWatchedAddresses != 0) {
					activeUser.add(new SortUser(this.userList.get(n).getTelegramID(), timeDiffInDays, numWatchedAddresses));
					Collections.sort(activeUser);
				}

			}
		}

		// prepare prints/returns
		String print = "*" + passiveUser.size() + " inactive Users*\n" + passiveUser.toString();
		print = print + "\n*" + activeUser.size() + " active Users*\n" + activeUser.toString();
		return print.replace(", ", "");
	}

}
