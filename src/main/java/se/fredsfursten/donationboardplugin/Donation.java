package se.fredsfursten.donationboardplugin;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.hamcrest.core.IsEqual;

class Donation {
	private enum DonationType {NONE, EMPTY, BUTTON, DONATION};
	private DonationType donationType;
	private UUID playerId;
	private String playerName;

	public Donation()
	{
		this.donationType = DonationType.NONE;
		this.playerId = null;
		this.playerName = null;
	}
	
	public void setEmpty()
	{
		this.donationType = DonationType.EMPTY;
		this.playerId = null;
		this.playerName = null;
	}
	
	public void setButton()
	{
		this.donationType = DonationType.BUTTON;
		this.playerId = null;
		this.playerName = null;
	}

	public void setDonation(Player player)
	{
		this.donationType = DonationType.DONATION;
		this.playerId = player.getUniqueId();
		this.playerName = player.getName();

	}

	public static Donation createDonationBoardInfo(DonationStorageModel storageModel)
	{
		Donation donation = new Donation();
		if (storageModel.getIsEmpty())
		{
			donation.setEmpty();
		} else if (storageModel.getPlayer() == null) {
			donation.setButton();
		} else {
			donation.setDonation(storageModel.getPlayer());
		}
		return donation;
	}

	boolean isEmpty() {
		return (this.donationType == DonationType.EMPTY) || (this.donationType == DonationType.NONE);
	}

	boolean isButton() {
		return this.donationType == DonationType.BUTTON;
	}

	boolean isDonation() {
		return this.donationType == DonationType.DONATION;
	}

	Player getPlayer()
	{
		if (this.donationType != DonationType.DONATION) return null;
		return Bukkit.getServer().getPlayer(this.playerId);
	}

	String getCreatorName() {
		return this.playerName;
	}

	UUID getCreatorId() {
		return this.playerId;
	}

	public String toString() {
		return String.format("%s: %s", this.donationType.toString(), getCreatorName());
	}

	public boolean isSame(Donation donationInfo) {
		if (this.donationType != donationInfo.donationType) return false;
		if (this.donationType != DonationType.DONATION) return true;
		return this.playerId == donationInfo.playerId;
	}

	public void copy(Donation from) {
		this.donationType = from.donationType;
		this.playerId = from.playerId;
		this.playerName = from.playerName;
	}
}
