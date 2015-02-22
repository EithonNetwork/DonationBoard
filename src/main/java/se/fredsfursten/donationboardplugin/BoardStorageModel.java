package se.fredsfursten.donationboardplugin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import se.fredsfursten.plugintools.PlayerCollection;

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
		this.worldId = block.getWorld().getUID();
		this.blockX = block.getX();
		this.blockY = block.getY();
		this.blockZ = block.getZ();
		this.stepX = stepX;
		this.stepZ = stepZ;
	}

	public BoardStorageModel(BoardView view, BoardModel model, PlayerCollection<PlayerInfo> knownPlayers) {
		initialize(view.getBlock(0,0), view.getStepX(), view.getStepZ(), model);
		this.worldId = view.getWorld().getUID();
		this.donators = new ArrayList<PlayerStorageModel>();
		for (PlayerInfo playerInfo : knownPlayers) {
			if (playerInfo.getDonationTokens() > 0) {
				this.donators.add(new PlayerStorageModel(playerInfo.getUniqueId(), playerInfo.getDonationTokens()));
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
		return new BoardView(getBlock());
	}

	public PlayerCollection<PlayerInfo> getKnownPlayers()
	{
		if (this.donators == null) return null;
		PlayerCollection<PlayerInfo> knownPlayers = new PlayerCollection<PlayerInfo>();
		for (PlayerStorageModel storageModel : this.donators) {
			knownPlayers.put(storageModel.getUniqueId(), new PlayerInfo(storageModel.getUniqueId(), storageModel.getDonationTokens()));
		}
		
		return knownPlayers;
	}
}
