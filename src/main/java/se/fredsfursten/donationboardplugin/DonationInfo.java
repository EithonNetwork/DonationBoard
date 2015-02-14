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

	DonationInfo(boolean isEmpty, Player creator)
	{
		if (creator != null)
		{
			this.creatorId = creator.getUniqueId();
			this.creatorName = creator.getName();
			this.donationType = DonationType.DONATION;
		} else {
			this.creatorId = null;
			this.creatorName = null;
			this.donationType = isEmpty? DonationType.NONE:DonationType.BUTTON;
		}
	}

	public static DonationInfo createDonationBoardInfo(StorageModel storageModel)
	{
		return new DonationInfo(storageModel.getIsEmpty(), storageModel.getCreator());
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
