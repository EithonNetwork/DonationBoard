package se.fredsfursten.donationboardplugin;

import org.bukkit.Bukkit;
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
	private static final String PRINT_COMMAND = "/donationboard print";
	private static final String PROMOTE_COMMAND = "/donationboard promote [username]";
	private static final String DEMOTE_COMMAND = "/donationboard demote [username]";

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

		BoardController.get().shiftLeft(player);
	}

	void printCommand(Player player, String[] args)
	{
		if (!verifyPermission(player, "donationboard.print")) return;
		if (!arrayLengthIsWithinInterval(args, 1, 1)) {
			player.sendMessage(PRINT_COMMAND);
			return;
		}

		BoardController.get().print(player);
	}

	@SuppressWarnings("deprecation")
	void promoteCommand(Player player, String[] args)
	{
		if (!verifyPermission(player, "donationboard.load")) return;
		if (!arrayLengthIsWithinInterval(args, 1, 2)) {
			player.sendMessage(PROMOTE_COMMAND);
			return;
		}

		Player affectedPlayer = player;
		if (args.length > 1) {
			affectedPlayer = Bukkit.getPlayer(args[1]);
			if (affectedPlayer == null)
			{
				player.sendMessage("Unknown player: " + args[1]);
			}
		}
		BoardController.get().promote(player);
	}

	@SuppressWarnings("deprecation")
	void demoteCommand(Player player, String[] args)
	{
		if (!verifyPermission(player, "donationboard.load")) return;
		if (!arrayLengthIsWithinInterval(args, 1, 2)) {
			player.sendMessage(DEMOTE_COMMAND);
			return;
		}

		Player affectedPlayer = player;
		if (args.length > 1) {
			affectedPlayer = Bukkit.getPlayer(args[1]);
			if (affectedPlayer == null)
			{
				player.sendMessage("Unknown player: " + args[1]);
			}
		}
		BoardController.get().demote(player);
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
