package se.fredsfursten.donationboardplugin;

import java.io.Serializable;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

class DonationStorageModel implements Serializable {
	private static final long serialVersionUID = 1L;
	private int day;
	private int level;
	private boolean isEmpty;
	private UUID creatorId;
	private String creatorName;
	
	public DonationStorageModel(int day, int level, boolean isEmpty, UUID creatorId, String creatorName)
	{
		this.day = day;
		this.level = level;
		this.isEmpty = isEmpty;
		this.creatorId = creatorId;
		this.creatorName = creatorName;
	}
	
	public int getDay()
	{
		return this.day;
	}
	
	public int getLevel()
	{
		return this.level;
	}
	
	public boolean getIsEmpty()
	{
		return this.isEmpty;
	}
	
	@SuppressWarnings("deprecation")
	public Player getPlayer()
	{
		if (this.creatorId == null) return null;
		Player player = Bukkit.getServer().getPlayer(this.creatorId);
		if (player == null) {
			if (this.creatorName == null) return null;
			player = Bukkit.getServer().getPlayer(creatorName);
		}
		return player;
	}
	
	public String getCreatorName()
	{
		Player creator = getPlayer();
		if (creator != null){
			this.creatorName = creator.getName();
		}
		return this.creatorName;
	}
	
	public String toString()
	{
		return String.format("(%d,%d): %s (%s)", this.day, this.level, this.creatorName, this.creatorId);
	}
}
