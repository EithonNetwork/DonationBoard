package se.fredsfursten.donationboardplugin;

import java.io.Serializable;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

class DonationModel implements Serializable {
	private static final long serialVersionUID = 1L;
	private int day;
	private int level;
	private boolean isEmpty;
	private UUID creatorId;
	private String creatorName;
	
	public DonationModel(int day, int level, boolean isEmpty, UUID creatorId, String creatorName)
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
	
	public Player getCreator()
	{
		Player creator = Bukkit.getServer().getPlayer(this.creatorId);
		return creator;
	}
	
	public String getCreatorName()
	{
		Player creator = getCreator();
		if (creator != null){
			this.creatorName = creator.getName();
		}
		return this.creatorName;
	}
}
