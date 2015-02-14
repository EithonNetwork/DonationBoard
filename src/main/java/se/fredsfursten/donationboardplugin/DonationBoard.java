package se.fredsfursten.donationboardplugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class DonationBoard {
	private static DonationBoard singleton = null;

	public static int TOTAL_DAYS = 3;
	public static int TOTAL_LEVELS = 3;
	
	private DonationInfo[][] _donations;
	private Block _startBlock;
	int _stepX;
	int _stepZ;
	private JavaPlugin plugin = null;

	private DonationBoard() {
		this._donations = new DonationInfo[TOTAL_DAYS][TOTAL_LEVELS];
		for (int day = 0; day < TOTAL_DAYS; day++) {
			for (int level = 0; level < TOTAL_LEVELS; level++) {
				this._donations[day][level] = null;
			}
		}
	}

	static DonationBoard get()
	{
		if (singleton == null) {
			singleton = new DonationBoard();
		}
		return singleton;
	}

	void enable(JavaPlugin plugin){
		this.plugin = plugin;
	}

	void disable() {
	}

	void donate(Player player, Block block) {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this.plugin, new Runnable() {
			public void run() {
				rememberNewDonation(block, player);
			}
		});
	}
	
	public void initialize(Player player, Block clickedBlock) {
		this._startBlock = clickedBlock;
		this._stepX = 0;
		this._stepZ = 1;
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this.plugin, new Runnable() {
			public void run() {
				firstLineOfButtons(player, clickedBlock);
			}
		});
	}

	void firstLineOfButtons(Player player, Block clickedBlock) {
		int blockX = clickedBlock.getX();
		int blockY = clickedBlock.getY();
		int blockZ = clickedBlock.getZ();
		int x = blockX;
		int z = blockZ;
		player.sendMessage(String.format("Start x,z %d, %d", x, z));
		for (int day = 0; day < TOTAL_DAYS; day++) {
			Block nextBlock = clickedBlock.getWorld().getBlockAt(x, blockY, z);
			createDonationButton(nextBlock);
			x = x + this._stepX;
			z = z + this._stepZ;
		}
		player.sendMessage(String.format("Stop x,z %d, %d", x-this._stepX, z-this._stepZ));
	}
	
	void rememberNewDonation(Block block, Player player) {
		createPlayerSkull(player, block);
		markAsDonated(block, player);
		Block blockAbove = block.getWorld().getBlockAt(block.getX(), block.getY()+1, block.getZ());
		createDonationButton(blockAbove);
		markAsPossibleToDonate(block);
	}

	@SuppressWarnings("deprecation")
	void createPlayerSkull(Player player, Block block) {
		if (!isBlockInsideBoard(block)) return;
		block.setType(Material.SKULL);
		block.setData((byte) 4);
		Skull skull = (Skull)block.getState();
		skull.setOwner(player.getName());
		skull.update();
	}
	
	void createDonationButton(Block block) {
		if (!isBlockInsideBoard(block)) return;
		block.setType(Material.WOOD_BUTTON);
		block.setData((byte) 2);
	}
	
	private boolean isBlockInsideBoard(Block block) {
		int day = calculateDay(block);
		int level = calculateLevel(block);
		if (day < 0) return false;
		if (day >= TOTAL_DAYS) return false;
		if (level < 0) return false;
		if (level >= TOTAL_LEVELS) return false;
		return true;
	}

	private void markAsPossibleToDonate(Block block) {
		if (!isBlockInsideBoard(block)) return;
		int day = calculateDay(block);
		int level = calculateLevel(block);
		_donations[day][level] = new DonationInfo(false, null);
		for (int i = level+1; i < TOTAL_LEVELS; i++) {
			_donations[day][i] = new DonationInfo(true, null);
		}
	}

	private void markAsDonated(Block block, Player player) {
		if (!isBlockInsideBoard(block)) return;
		int day = calculateDay(block);
		int level = calculateLevel(block);
		this._donations[day][level] = new DonationInfo(false, player);
	}
	
	private int calculateDay(Block block) {
		if (this._stepX != 0) {
			return Math.abs(block.getX() - this._startBlock.getX());
		} else {
			return Math.abs(block.getZ() - this._startBlock.getZ());
		}
	}
	
	private int calculateLevel(Block block) {
		return (block.getY() - this._startBlock.getY());
	}
}
