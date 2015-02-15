package se.fredsfursten.donationboardplugin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

class BoardStorageModel implements Serializable {
	private static final long serialVersionUID = 1L;
	private UUID worldId;
	private int blockX;
	private int blockY;
	private int blockZ;
	int stepX;
	int stepZ;
	ArrayList<DonationStorageModel> donations;

	public BoardStorageModel(Block block, int stepX, int stepZ, BoardModel model)
	{
		initialize(block, stepX, stepZ, model);
	}

	private void initialize(Block block, int stepX, int stepZ, BoardModel model) {
		this.worldId = block.getWorld().getUID();
		this.blockX = block.getX();
		this.blockY = block.getY();
		this.blockZ = block.getZ();
		this.stepX = stepX;
		this.stepZ = stepZ;
		this.donations = new ArrayList<DonationStorageModel>();
		for (int day = 0; day < model.getNumberOfDays(); day++) {
			for (int level = 0; level < model.getNumberOfLevels(); level++) {
				Donation donation = model.getDonationInfo(day, level);
				this.donations.add(new DonationStorageModel(day, level, donation.isEmpty(), donation.getCreatorId(), donation.getCreatorName()));
			}
		}
	}

	public BoardStorageModel(BoardView view, BoardModel model) {
		initialize(view.getBlock(0,0), view.getStepX(), view.getStepZ(), model);
		this.worldId = view.getWorld().getUID();

	}

	public Block getBlock()
	{
		World world = Bukkit.getWorld(this.worldId);
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

	public ArrayList<DonationStorageModel> getDonations()
	{
		return this.donations;
	}

	public BoardView getView()
	{
		return new BoardView(getBlock());
	}

	public BoardModel getModel(int numberOfDays, int numberOfLevels)
	{
		BoardModel model = new BoardModel(numberOfDays, numberOfLevels);
		for (DonationStorageModel storageDonation : getDonations()) {
			int day = storageDonation.getDay();
			int level = storageDonation.getLevel();
			if (storageDonation.getPlayer() != null) {
				model.markOnlyThis(day, level, storageDonation.getIsEmpty(), storageDonation.getPlayer());
			}
		}
		
		return model;
	}
}
