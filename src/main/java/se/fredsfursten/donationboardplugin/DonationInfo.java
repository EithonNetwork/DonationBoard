package se.fredsfursten.donationboardplugin;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

class DonationInfo {
	private Block block;
	private UUID creatorId;
	private String creatorName;

	DonationInfo(Block block, Player creator)
	{
		this.block = block;
		if (creator != null)
		{
			this.creatorId = creator.getUniqueId();
			this.creatorName = creator.getName();
		} else {
			this.creatorId = null;
			this.creatorName = null;
		}
	}

	public static DonationInfo createDonationBoardInfo(StorageModel storageModel)
	{
		return new DonationInfo(storageModel.getBlock(), storageModel.getCreator());
	}
	
	Block getBlock() {
		return this.block;
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
		return new StorageModel(getBlock(), getCreatorId(), getCreatorName());
	}

	public String toString() {
		return String.format("%s: block %s", getCreatorName(), getBlock().toString());
	}
}
