package se.fredsfursten.donationboardplugin;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

class DonationInfo {
	private enum DonationType {NONE, BUTTON, DONATION};
	private DonationType donationType;
	private UUID creatorId;
	private String creatorName;

	public DonationInfo(Player creator)
	{
		this.donationType = DonationType.DONATION;
		this.creatorId = creator.getUniqueId();
		this.creatorName = creator.getName();

	}

	public DonationInfo(boolean isEmpty)
	{
		this.donationType = isEmpty? DonationType.NONE:DonationType.BUTTON;
		this.creatorId = null;
		this.creatorName = null;
	}

	public static DonationInfo createDonationBoardInfo(StorageModel storageModel)
	{
		Player player = storageModel.getCreator();
		if (player == null) {
			return new DonationInfo(storageModel.getIsEmpty());
		}
		return new DonationInfo(storageModel.getCreator());
	}

	boolean isEmpty() {
		return this.donationType == DonationType.NONE;
	}

	boolean isButton() {
		return this.donationType == DonationType.BUTTON;
	}

	boolean isDonation() {
		return this.donationType == DonationType.DONATION;
	}

	Player getCreator()
	{
		return Bukkit.getServer().getPlayer(this.creatorId);
	}

	String getCreatorName() {
		return this.creatorName;
	}

	UUID getCreatorId() {
		return this.creatorId;
	}

	StorageModel getStorageModel() {
		return new StorageModel(this.donationType == DonationType.NONE, getCreatorId(), getCreatorName());
	}

	public String toString() {
		return String.format("%s: block %s", getCreatorName());
	}
}
