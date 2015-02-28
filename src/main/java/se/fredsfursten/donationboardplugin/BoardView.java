package se.fredsfursten.donationboardplugin;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;

import se.fredsfursten.plugintools.PlayerCollection;

class BoardView {
	private Block _startBlock;
	int _stepX;
	int _stepZ;

	BoardView(Block startBlock) {
		this._startBlock = startBlock;
		this._stepX = 0;
		this._stepZ = 1;	
	}

	public World getWorld()
	{
		return this._startBlock.getWorld();
	}

	public int getStepX()
	{
		return this._stepX;
	}

	public int getStepZ()
	{
		return this._stepZ;
	}

	public void refresh(BoardModel board)
	{		
		for (int day = 1; day <= board.getNumberOfDays(); day++) {
			int newDonationLevel = board.getDonationLevel(day);
			for (int level = 1; level <= board.getNumberOfLevels(); level++) {
				Block block = getBlock(day, level);
				String blockPlayerName = getSkullOwner(block);
				String modelPlayerName = board.getDonationInfo(day, level).getPlayerName();
				if (modelPlayerName != null) {
					// A skull
					if (blockPlayerName != modelPlayerName) createPlayerSkull(modelPlayerName, block);
				} else if (level == newDonationLevel+1) {
					// A button
					if (!isButton(block)) createDonationButton(block);
				} else {
					if (!isAir(block)) createEmpty(block);
				}
			}
		}
	}

	public void updateBoardModel(BoardModel board) {
		for (int day = 1; day <= board.getNumberOfDays(); day++) {
			for (int level = 1; level <= board.getNumberOfLevels(); level++) {
				Block block = getBlock(day, level);
				String playerName = getSkullOwner(block);
				if (playerName != null) board.markOnlyThis(day, level, playerName);
			}
		}
	}

	private String getSkullOwner(Block block)
	{
		if (block.getType() != Material.SKULL) return null;

		Skull skull = (Skull)block.getState();
		String playerName = skull.getOwner();
		return playerName;
	}

	private boolean isButton(Block block)
	{
		return (block.getType() == Material.WOOD_BUTTON);
	}

	private boolean isAir(Block block)
	{
		return (block.getType() == Material.AIR);
	}

	int calculateDay(Block block) {
		if (this._stepX != 0) {
			return Math.abs(block.getX() - this._startBlock.getX() + 1);
		} else {
			return Math.abs(block.getZ() - this._startBlock.getZ() + 1);
		}
	}

	int calculateLevel(Block block) {
		return (block.getY() - this._startBlock.getY() + 1);
	}

	Block getBlock(int day, int level) {
		return getBlockInternal(day-1, level-1);
	}

	private Block getBlockInternal(int dayIndex, int levelIndex) {
		Block block = this._startBlock.getWorld().getBlockAt(
				this._startBlock.getX()+this._stepX*dayIndex, 
				this._startBlock.getY()+levelIndex, 
				this._startBlock.getZ()+this._stepZ*dayIndex);
		return block;
	}

	private void createEmpty(Block block) {
		block.setType(Material.AIR);
	}

	@SuppressWarnings("deprecation")
	private void createDonationButton(Block block) {
		block.setType(Material.WOOD_BUTTON);
		block.setData((byte) 2);
	}

	@SuppressWarnings("deprecation")
	private void createPlayerSkull(String playerName, Block block) {
		block.setType(Material.SKULL);
		block.setData((byte) 4);
		Skull skull = (Skull)block.getState();
		skull.setOwner(playerName);
		skull.update();
	}
}
