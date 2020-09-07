package telegramBridge;

@SuppressWarnings("rawtypes")
public class SortUser implements Comparable{
	
	// This class enables the sorting of users by daysSinceActive
	// by implementing the interface comparable
	
	public int daysSinceActive, addressStorageSize;
	public String telegramId;
	
	public SortUser(String inTelegramId, int inDaysSinceActive, int inAddressStorageSize) {
		
		this.telegramId = inTelegramId;
		this.daysSinceActive = inDaysSinceActive;
		this.addressStorageSize = inAddressStorageSize;		
	}

	@Override
	
	// Sort by days active.
	public int compareTo(Object o) {
		SortUser u = (SortUser) o;

		if (this.daysSinceActive == u.daysSinceActive) {
			return 0;
		}
		if (this.daysSinceActive > u.daysSinceActive) {
			return 1;
		}
		else {
			return -1;
		}
	}
	
	// To String for prints
	public String toString() { 
		return this.addressStorageSize + " (" + this.daysSinceActive + "): " + this.telegramId + "\n";
	}
}

