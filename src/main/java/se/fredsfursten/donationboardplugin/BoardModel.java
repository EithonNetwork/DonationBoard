package se.fredsfursten.donationboardplugin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

class BoardModel implements Serializable {
	private static final long serialVersionUID = 1L;
	private UUID worldId;
	private int blockX;
	private int blockY;
	private int blockZ;
	int stepX;
	int stepZ;
	ArrayList<DonationModel> donations;
	
	public BoardModel(Block block, int stepX, int stepZ, DonationInfo[][] donations)
	{
		this.worldId = block.getWorld().getUID();
		this.blockX = block.getX();
		this.blockY = block.getY();
		this.blockZ = block.getZ();
		this.stepX = stepX;
		this.stepZ = stepZ;
		this.donations = new ArrayList<DonationModel>();
		for (int day = 0; day < donations.length; day++) {
			for (int level = 0; level < donations[day].length; level++) {
				DonationInfo donation = donations[day][level];
				this.donations.add(new DonationModel(day, level, donation.isEmpty(), donation.getCreatorId(), donation.getCreatorName()));
			}
		}
	}
	
	public Block getBlock()
	{
		World world = Bukkit.getWorld(worldId);
		if (world == null) return null;
		return world.getBlockAt(this.blockX, this.blockY, this.blockZ);
	}
	
	public int getStepX()
	{
		return this.stepX;
	}
	
	public int getStepZ()
	{
		return this.stepZ;
	}
	
	public ArrayList<DonationModel> getDonations() {
		return this.donations;
	}

	}
