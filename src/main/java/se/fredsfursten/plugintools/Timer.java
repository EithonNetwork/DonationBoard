package se.fredsfursten.plugintools;

import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Timer {
	private static final long TICK_LENGTH = 100L;
	private static Timer singleton = null;
	private JavaPlugin _plugin = null;

	private Timer() {
	}

	static Timer get()
	{
		if (singleton == null) {
			singleton = new Timer();
		}
		return singleton;
	}

	void enable(JavaPlugin plugin){
		this._plugin = plugin;
	}

	void disable() {
		this._plugin = null;
	}

	void tick() {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._plugin, new Runnable() {
			public void run() {
				tick();
			}
		}, TICK_LENGTH);
	}	
}
