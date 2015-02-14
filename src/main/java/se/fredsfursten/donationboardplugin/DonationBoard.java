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
		for (int day = 0; day < TOTAL_DAYS; day++) {
			Block block = clickedBlock.getWorld().getBlockAt(x, blockY, z);
			createDonationButton(block);
			markAsPossibleToDonate(block);
			x = x + this._stepX;
			z = z + this._stepZ;
		}
	}

	public void shiftLeft(Player player) {
		for (int day = 0; day < TOTAL_DAYS-1; day++) {
			for (int level = 0; level < TOTAL_LEVELS; level++) {
				Block block = calculateBlock(day, level);
				DonationInfo donation = this._donations[day+1][level];
				this._donations[day][level] = donation;
				player.sendMessage(String.format("%d,%d: %s", day, level, donation.toString()));
				if (donation.isEmpty()) {
					setEmpty(block);
				} else if (donation.isButton()) {
					createDonationButton(block);
				} else {
					createPlayerSkull(donation.getCreator(), block);
				}
			}
		}			
		int day = TOTAL_DAYS-1;
		for (int level = 0; level < TOTAL_LEVELS; level++) {
			Block block = calculateBlock(day, level);
			this._donations[day][level] = new DonationInfo(level > 0);
			if (level > 0) setEmpty(block);
			else createDonationButton(block);
		}
	}

	void rememberNewDonation(Block block, Player player) {
		createPlayerSkull(player, block);
		markAsDonated(block, player);
		Block blockAbove = block.getWorld().getBlockAt(block.getX(), block.getY()+1, block.getZ());
		createDonationButton(blockAbove);
		markAsPossibleToDonate(blockAbove);
	}

	@SuppressWarnings("deprecation")
	private void createPlayerSkull(Player player, Block block) {
		if (!isBlockInsideBoard(block)) return;
		block.setType(Material.SKULL);
		block.setData((byte) 4);
		Skull skull = (Skull)block.getState();
		skull.setOwner(player.getName());
		skull.update();
	}

	@SuppressWarnings("deprecation")
	private void createDonationButton(Block block) {
		if (!isBlockInsideBoard(block)) return;
		block.setType(Material.WOOD_BUTTON);
		block.setData((byte) 2);
	}

	private void setEmpty(Block block) {
		if (!isBlockInsideBoard(block)) return;
		block.setType(Material.AIR);
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
		this._donations[day][level] = new DonationInfo(false);
		for (int i = level+1; i < TOTAL_LEVELS; i++) {
			this._donations[day][i] = new DonationInfo(true);
		}
	}

	private void markAsDonated(Block block, Player player) {
		if (!isBlockInsideBoard(block)) return;
		int day = calculateDay(block);
		int level = calculateLevel(block);
		this._donations[day][level] = new DonationInfo(player);
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
	
	private Block calculateBlock(int day, int level) {
		Block block = this._startBlock.getWorld().getBlockAt(
				this._startBlock.getX()+this._stepX*day, 
				this._startBlock.getY()+level, 
				this._startBlock.getZ()+this._stepZ*day);
		if (!isBlockInsideBoard(block)) return null;
		return block;
	}
}
