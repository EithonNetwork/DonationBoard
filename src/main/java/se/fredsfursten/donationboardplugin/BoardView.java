package se.fredsfursten.donationboardplugin;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;

class BoardView {
	private Block _startBlock;
	int _stepX;
	int _stepZ;
	private BoardModel _lastBoard;

	BoardView(Block startBlock) {
		this._startBlock = startBlock;
		this._lastBoard = null;
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
		for (int day = 0; day < board.getNumberOfDays(); day++) {
			for (int level = 0; level < board.getNumberOfLevels(); level++) {
				if (!board.isSame(this._lastBoard, day, level)) {
					Block block = getBlock(day, level);
					update(block, board.getDonationInfo(day, level));
				}
			}
			int newDonationLevel = board.donationLevel(day);
			int lastDonationLevel = -2;
			if (this._lastBoard != null) {
				lastDonationLevel = this._lastBoard.donationLevel(day);
			}
			if (newDonationLevel != lastDonationLevel) {
				int level = newDonationLevel + 1;
				if (board.isInsideBoard(day, level)) {
					createDonationButton(getBlock(day, level));
				}
				if (newDonationLevel < lastDonationLevel) {
					level = lastDonationLevel + 1;
					if (board.isInsideBoard(day, level)) {
						createEmpty(getBlock(day, level));
					}
				}
			}
		}
		this._lastBoard = board.clone();
	}

	int calculateDay(Block block) {
		if (this._stepX != 0) {
			return Math.abs(block.getX() - this._startBlock.getX());
		} else {
			return Math.abs(block.getZ() - this._startBlock.getZ());
		}
	}

	int calculateLevel(Block block) {
		return (block.getY() - this._startBlock.getY());
	}

	Block getBlock(int day, int level) {
		Block block = this._startBlock.getWorld().getBlockAt(
				this._startBlock.getX()+this._stepX*day, 
				this._startBlock.getY()+level, 
				this._startBlock.getZ()+this._stepZ*day);
		return block;
	}

	private void update(Block block, Donation donationInfo) {
		if (donationInfo.isEmpty()) {
			createEmpty(block);
		} else {
			createPlayerSkull(donationInfo.getPlayer(), block);
		}
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
	private void createPlayerSkull(Player player, Block block) {
		block.setType(Material.SKULL);
		block.setData((byte) 4);
		Skull skull = (Skull)block.getState();
		skull.setOwner(player.getName());
		skull.update();
	}
}
