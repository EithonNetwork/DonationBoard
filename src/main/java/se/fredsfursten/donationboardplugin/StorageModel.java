package se.fredsfursten.donationboardplugin;

import java.io.Serializable;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

class StorageModel implements Serializable {
	private static final long serialVersionUID = 1L;
	private boolean isEmpty;
	private UUID creatorId;
	private String creatorName;
	
	public StorageModel(boolean isEmpty, UUID creatorId, String creatorName)
	{
		this.isEmpty = isEmpty;
		this.creatorId = creatorId;
		this.creatorName = creatorName;
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
