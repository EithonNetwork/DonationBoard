package se.fredsfursten.donationboardplugin;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import se.fredsfursten.plugintools.PluginConfig;
import se.fredsfursten.plugintools.AlarmTrigger;

public final class DonationBoardPlugin extends JavaPlugin implements Listener {

	private static File donationsStorageFile;
	private static PluginConfig configuration;
	private static String mandatoryWorld;

	@Override
	public void onEnable() {
		if (configuration == null) {
			configuration = new PluginConfig(this, "config.yml");
		} else {
			configuration.load();
		}
		mandatoryWorld = DonationBoardPlugin.getPluginConfig().getString("MandatoryWorld");
		donationsStorageFile = new File(getDataFolder(), "donations.bin");
		getServer().getPluginManager().registerEvents(this, this);		
		BoardController.get().enable(this);
		Commands.get().enable(this);
		AlarmTrigger.get().enable(this);
		setShiftTimer();
	}

	private void setShiftTimer() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime alarmTime = null;
		LocalDateTime alarmToday = LocalDateTime.of(LocalDate.now(), LocalTime.of(7,0,0));
		LocalDateTime alarmTomorrow = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(7,0,0));
		if (LocalDateTime.now().isBefore(alarmToday)) alarmTime = alarmToday;
		else alarmTime = alarmTomorrow;
		AlarmTrigger.get().setAlarm(alarmTime, new Runnable() {
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

	public static FileConfiguration getPluginConfig()
	{
		return configuration.getFileConfiguration();
	}

	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (mandatoryWorld != null) {
			if(!player.getWorld().getName().equalsIgnoreCase(mandatoryWorld)) return;
		}
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		switch (event.getClickedBlock().getType()) {
		case STONE_BUTTON:
			BoardController.get().initialize(player, event.getClickedBlock());
			break;
		case WOOD_BUTTON:
			BoardController.get().increaseLevel(player, event.getClickedBlock());
			break;
		default:
			break;
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
		if (command.equals("shift")) {
			Commands.get().shiftCommand(player, args);
		} else if (command.equals("print")) {
			Commands.get().printCommand(player, args);
		} else if (command.equals("load")) {
			Commands.get().loadCommand(player, args);
		} else if (command.equals("save")) {
			Commands.get().saveCommand(player, args);
		} else if (command.equals("register")) {
			Commands.get().registerCommand(player, args);
		} else if (command.equals("donate")) {
			Commands.get().donateCommand(player, args);
		} else {
			sender.sendMessage("Could not understand command.");
			return false;
		}
		return true;
	}
}
