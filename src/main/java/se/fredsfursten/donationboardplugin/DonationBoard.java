package se.fredsfursten.donationboardplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import se.fredsfursten.plugintools.PlayerInfo;

public class DonationBoard {
	private static DonationBoard singleton = null;

	private PlayerInfo<Object> playersWithTemporaryJumpPause = null;
	private AllDonations allDonationBoards = null;
	private JavaPlugin plugin = null;

	private DonationBoard() {
		this.allDonationBoards = AllDonations.get();
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
		this.playersWithTemporaryJumpPause = new PlayerInfo<Object>();
	}

	void disable() {
	}

	@SuppressWarnings("deprecation")
	void donate(Player player, Block block) {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this.plugin, new Runnable() {
			public void run() {
				block.setType(Material.SKULL);
				block.setData((byte) 1);
				Skull skull = (Skull)block.getState();
				skull.setOwner(player.getName());
				skull.update();
			}
		});

	}

	public void initialize(Player player, Block clickedBlock) {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this.plugin, new Runnable() {
			@SuppressWarnings("deprecation")
			public void run() {
				int blockX = clickedBlock.getX();
				int blockY = clickedBlock.getY();
				int blockZ = clickedBlock.getZ();
				int stepX = 0;
				int stepZ = 1;
				int x = blockX;
				int z = blockZ;
				player.sendMessage(String.format("Start x,z %d, %d", x, z));
				for (int i = 0; i < 31; i++) {
					Block nextBlock = clickedBlock.getWorld().getBlockAt(x, blockY, z);
					nextBlock.setType(Material.WOOD_BUTTON);
					nextBlock.setData((byte) 2);
					x = x + stepX;
					z = z + stepZ;
				}
				player.sendMessage(String.format("Stop x,z %d, %d", x-stepX, z-stepZ));
			}
		});
	}
}
