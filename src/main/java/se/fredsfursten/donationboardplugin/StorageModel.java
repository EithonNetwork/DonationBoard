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
	private double blockX;
	private double blockY;
	private double blockZ;
	private UUID worldId;
	private UUID creatorId;
	private String creatorName;
	
	public StorageModel(Block block, UUID creatorId, String creatorName)
	{
		this.blockX =block.getX();
		this.blockY = block.getY();
		this.blockZ = block.getZ();
		this.worldId = block.getWorld().getUID();
		this.creatorId = creatorId;
		this.creatorName = creatorName;
	}
	
	public World getWorld()
	{
		return Bukkit.getServer().getWorld(this.worldId);
	}
	
	public Block getBlock()
	{
		return getLocation().getBlock();
	}
	
	public Location getLocation()
	{
		return new Location(getWorld(), this.blockX, this.blockY, this.blockZ);
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
