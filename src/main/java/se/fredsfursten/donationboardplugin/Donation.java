package se.fredsfursten.donationboardplugin;

import org.bukkit.entity.Player;

class Donation {
	private String playerName;

	public Donation()
	{
		this.playerName = null;
	}
	
	public void setEmpty()
	{
		this.playerName = null;
	}

	public void setDonation(Player player)
	{
		this.playerName = player.getName();

	}

	public void setDonation(String playerName)
	{
		this.playerName = playerName;

	}

	boolean isEmpty() {
		return this.playerName == null;
	}

	boolean isDonation() {
		return !isEmpty();
	}

	String getPlayerName() {
		return this.playerName;
	}
	
	public String toString() {
		return getPlayerName();
	}

	public boolean isSame(Donation donationInfo) {
		return this.playerName == donationInfo.playerName;
	}

	public void copy(Donation from) {
		this.playerName = from.playerName;
	}
}
