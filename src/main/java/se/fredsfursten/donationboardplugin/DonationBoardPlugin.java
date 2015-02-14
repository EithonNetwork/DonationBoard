package se.fredsfursten.donationboardplugin;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class DonationBoardPlugin extends JavaPlugin implements Listener {

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);		
		DonationBoard.get().enable(this);
		Commands.get().enable(this);
	}

	@Override
	public void onDisable() {
		DonationBoard.get().disable();
		Commands.get().disable();
	}

	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		player.sendMessage("Interact event");
		//if(!player.getWorld().getName().equalsIgnoreCase("w_donationworld")) return;
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		player.sendMessage("RIGHT_CLICK_BLOCK");
		switch (event.getClickedBlock().getType()) {
		case STONE_BUTTON:
			player.sendMessage("STONE_BUTTON");
			DonationBoard.get().initialize(player, event.getClickedBlock());
			player.sendMessage("Initialize done.");
			break;
		case WOOD_BUTTON:
			player.sendMessage("WOOD_BUTTON");
			DonationBoard.get().donate(player, event.getClickedBlock());
			player.sendMessage("Donate done.");
			break;

		default:
			break;
		}
		if(event.getClickedBlock().getType() == Material.WOOD_BUTTON) {
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be a player!");
			return false;
		}
		if (args.length < 1) {
			sender.sendMessage("Incomplete command...");
			return false;
		}

		Player player = (Player) sender;

		String command = args[0].toLowerCase();
		if (command.equals("add")) {
			Commands.get().addCommand(player, args);
		} else if (command.equals("remove")) {
			Commands.get().removeCommand(player, args);
		} else if (command.equals("edit")) {
			Commands.get().editCommand(player, args);
		} else if (command.equals("list")) {
			Commands.get().listCommand(player);
		} else if (command.equals("goto")) {
			Commands.get().gotoCommand(player, args);
		} else {
			sender.sendMessage("Could not understand command.");
			return false;
		}
		return true;
	}
}
