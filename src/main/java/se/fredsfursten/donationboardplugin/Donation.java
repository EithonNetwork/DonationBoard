package se.fredsfursten.donationboardplugin;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

class Donation {
	private UUID playerId;
	private String playerName;

	public Donation()
	{
		this.playerId = null;
		this.playerName = null;
	}
	
	public void setEmpty()
	{
		this.playerId = null;
		this.playerName = null;
	}

	public void setDonation(Player player)
	{
		this.playerId = player.getUniqueId();
		this.playerName = player.getName();

	}

	public static Donation createDonationBoardInfo(DonationStorageModel storageModel)
	{
		Donation donation = new Donation();
		if (storageModel.getPlayer() != null) {
			donation.setDonation(storageModel.getPlayer());
		} else {
			donation.setEmpty();
		}
		return donation;
	}

	boolean isEmpty() {
		return this.playerId == null;
	}

	boolean isDonation() {
		return !isEmpty();
	}

	Player getPlayer()
	{
		if (isEmpty()) return null;
		return Bukkit.getServer().getPlayer(this.playerId);
	}

	String getCreatorName() {
		return this.playerName;
	}

	UUID getCreatorId() {
		return this.playerId;
	}

	public String toString() {
		return getCreatorName();
	}

	public boolean isSame(Donation donationInfo) {
		return this.playerId == donationInfo.playerId;
	}

	public void copy(Donation from) {
		this.playerId = from.playerId;
		this.playerName = from.playerName;
	}
}
