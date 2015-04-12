package se.fredsfursten.donationboardplugin;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import se.fredsfursten.plugintools.AlarmTrigger;
import se.fredsfursten.plugintools.PluginConfig;

public final class DonationBoardPlugin extends JavaPlugin implements Listener {

	private static File donationsStorageFile;
	private static String mandatoryWorld;

	@Override
	public void onEnable() {
		PluginConfig.enable(this);
		mandatoryWorld = PluginConfig.get().getString("MandatoryWorld", "");
		donationsStorageFile = new File(getDataFolder(), "donations.bin");
		getServer().getPluginManager().registerEvents(this, this);		
		BoardController.get().enable(this);
		Commands.get().enable(this);
		AlarmTrigger.get().enable(this);
		setShiftTimer();	
	}

	private void setShiftTimer() {
		LocalDateTime alarmTime = null;
		LocalDateTime alarmToday = LocalDateTime.of(LocalDate.now(), LocalTime.of(7,0,0));
		LocalDateTime alarmTomorrow = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(7,0,0));
		if (LocalDateTime.now().isBefore(alarmToday)) alarmTime = alarmToday;
		else alarmTime = alarmTomorrow;
		AlarmTrigger.get().setAlarm("Donation board daily shift",
				alarmTime, 
				new Runnable() {
			public void run() {
				keepOnShifting();
			}
		});
	}

	protected void keepOnShifting() {
		BoardController.get().shiftLeft();
		setShiftTimer();
	}

	@Override
	public void onDisable() {
		BoardController.get().disable();
		Commands.get().disable();
		AlarmTrigger.get().disable();
	}

	public static File getDonationsStorageFile()
	{
		return donationsStorageFile;
	}

	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (!isInMandatoryWorld(player.getWorld())) return;
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		switch (event.getClickedBlock().getType()) {
		case STONE_BUTTON:
			BoardController.get().initialize(player, event.getClickedBlock());
			break;
		case WOOD_BUTTON:
			BoardController.get().increasePerkLevel(player, event.getClickedBlock());
			break;
		default:
			break;
		}
	}

	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		BoardController.get().playerJoined(player);
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (!isInMandatoryWorld(event.getTo().getWorld())) return;
		Player player = event.getPlayer();
		BoardController.get().playerTeleportedToBoard(player, event.getFrom());
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1) {
			sender.sendMessage("Incomplete command...");
			return false;
		}
		String command = args[0].toLowerCase();
		if (command.equals("donate")) {
			Commands.get().donateCommand(sender, args);
		} else {

			if (!(sender instanceof Player)) {
				sender.sendMessage("You must be a player!");
				return false;
			}

			Player player = (Player) sender;

			if (command.equals("shift")) {
				Commands.get().shiftCommand(player, args);
			} else if (command.equals("print")) {
				Commands.get().printCommand(player, args);
			} else if (command.equals("load")) {
				Commands.get().loadCommand(player, args);
			} else if (command.equals("save")) {
				Commands.get().saveCommand(player, args);
			} else if (command.equals("register")) {
				Commands.get().saveCommand(player, args);
			} else if (command.equals("goto")) {
				Commands.get().gotoCommand(player, args);
			} else if (command.equals("stats")) {
				Commands.get().statsCommand(player, args);
			} else {
				sender.sendMessage("Could not understand command.");
				return false;
			}
		}
		return true;
	}

	public static boolean isInMandatoryWorld(World world) 
	{
		if (mandatoryWorld == null) return true;
		return world.getName().equalsIgnoreCase(mandatoryWorld);
	}
}
