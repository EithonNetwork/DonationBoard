package se.fredsfursten.donationboardplugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import se.fredsfursten.plugintools.Misc;

public class Commands {
	private static Commands singleton = null;
	private static final String SHIFT_COMMAND = "/donationboard shift";

	private JavaPlugin plugin = null;
	
	private Commands() {
	}

	static Commands get()
	{
		if (singleton == null) {
			singleton = new Commands();
		}
		return singleton;
	}

	void enable(JavaPlugin plugin){
		this.plugin = plugin;
	}

	void disable() {
	}

	void shiftCommand(Player player, String[] args)
	{
		if (!verifyPermission(player, "donationboard.shift")) return;
		if (!arrayLengthIsWithinInterval(args, 1, 1)) {
			player.sendMessage(SHIFT_COMMAND);
			return;
		}

		DonationBoard.get().shiftLeft();
	}


	private boolean verifyPermission(Player player, String permission)
	{
		if (player.hasPermission(permission)) return true;
		player.sendMessage("You must have permission " + permission);
		return false;
	}





	private boolean arrayLengthIsWithinInterval(Object[] args, int min, int max) {
		return (args.length >= min) && (args.length <= max);
	}
}
