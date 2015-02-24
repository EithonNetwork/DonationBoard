package se.fredsfursten.plugintools;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Timer {
	private static final long TICK_LENGTH = 100L;
	private static Timer singleton = null;
	private JavaPlugin _plugin = null;
	private ArrayList<Alarm> _alarms = new ArrayList<Alarm>();
	private boolean _isLocked = false;

	private Timer() {
	}

	public static Timer get()
	{
		if (singleton == null) {
			singleton = new Timer();
		}
		return singleton;
	}

	public void enable(JavaPlugin plugin){
		this._plugin = plugin;
		LocalDateTime date = LocalDateTime.now();
		setAlarm(date, new Runnable() {
			public void run() {
				Bukkit.getServer().getConsoleSender().sendMessage("!!! Alarm 1 !!!");
			}
		});
		date = date.plusSeconds(30);
		setAlarm(date, new Runnable() {
			public void run() {
				Bukkit.getServer().getConsoleSender().sendMessage("!!! Alarm 2 !!!");
			}
		});
		date = date.plusSeconds(30);
		setAlarm(date, new Runnable() {
			public void run() {
				Bukkit.getServer().getConsoleSender().sendMessage("!!! Alarm 3 !!!");
			}
		});
		tick();
	}

	public void disable() {
		this._plugin = null;
	}
	
	public void setAlarm(LocalDateTime time, Runnable task)
	{
		Alarm alarm = new Alarm(time, task);
		this._alarms.add(alarm);
	}
	
	private boolean isEnabled()
	{
		return (this._plugin != null);
	}

	void tick() {
		if (!isEnabled()) return;
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._plugin, new Runnable() {
			public void run() {
				tick();
			}
		}, TICK_LENGTH);
		scheduler.scheduleSyncDelayedTask(this._plugin, new Runnable() {
			public void run() {
				checkAlarms();
			}
		});
	}

	void checkAlarms() {
		if (this._isLocked) return;
		synchronized(this)
		{
			this._isLocked = true;
			for (int i = 0; i < this._alarms.size(); i++) {
				Alarm alarm = this._alarms.get(i);
				if (alarm.maybeSetOff()) {
					this._alarms.remove(i);
					i--;
				}			
			}
			this._isLocked = false;
		}
	}
}
