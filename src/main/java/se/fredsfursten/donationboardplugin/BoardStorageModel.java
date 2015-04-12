package se.fredsfursten.donationboardplugin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

import se.fredsfursten.plugintools.PlayerCollection;

@Deprecated
class BoardStorageModel implements Serializable {
	private static final long serialVersionUID = 1L;
	private UUID worldId;
	private int blockX;
	private int blockY;
	private int blockZ;
	private int stepX;
	private int stepZ;
	private ArrayList<PlayerStorageModel> donators;
	

	public BoardStorageModel(Block block, int stepX, int stepZ, BoardModel model)
	{
		initialize(block, stepX, stepZ, model);
	}

	private void initialize(Block block, int stepX, int stepZ, BoardModel model) {
		this.worldId = block == null ? null : block.getWorld().getUID();
		this.blockX = block == null ? 0 : block.getX();
		this.blockY = block == null ? 0 : block.getY();
		this.blockZ = block == null ? 0 : block.getZ();
		this.stepX = stepX;
		this.stepZ = stepZ;
	}

	public BoardStorageModel(BoardView view, BoardModel model, PlayerCollection<PlayerInfo> knownPlayers) {
		initialize(view.getBlock(1,1), view.getStepX(), view.getStepZ(), model);
		this.worldId = view.getWorld().getUID();
		this.donators = new ArrayList<PlayerStorageModel>();
		for (PlayerInfo playerInfo : knownPlayers) {
			if (playerInfo.getRemainingDonationTokens() > 0) {
				this.donators.add(
						new PlayerStorageModel(
								playerInfo.getUniqueId(), 
								playerInfo.getRemainingDonationTokens(),
								playerInfo.getTotalTokensDonated(),
								playerInfo.getTotalMoneyDonated()));
			}
		}
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

	public BoardView getView()
	{
		Block block = getBlock();
		if (block == null) return null;
		return new BoardView(block);
	}

	public PlayerCollection<PlayerInfo> getKnownPlayers()
	{
		PlayerCollection<PlayerInfo> knownPlayers = new PlayerCollection<PlayerInfo>();
		if (this.donators == null) return knownPlayers;
		for (PlayerStorageModel storageModel : this.donators) {
			knownPlayers.put(storageModel.getUniqueId(), 
					new PlayerInfo(
							storageModel.getUniqueId(), 
							storageModel.getRemainingDonationTokens(),
							storageModel.getTotalTokensDonated(), 
							storageModel.getTotalMoneyDonated()));
		}
		
		return knownPlayers;
	}
}
