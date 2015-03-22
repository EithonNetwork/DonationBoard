package se.fredsfursten.plugintools;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class AlarmTrigger {
	private static final long TICK_LENGTH = 100L;
	private static AlarmTrigger singleton = null;
	private JavaPlugin _plugin = null;
	private ArrayList<Alarm> _alarms = new ArrayList<Alarm>();
	private int _enableCounter = 0;
	private int _firstAlarmIndex= -1;

	private AlarmTrigger() {
	}

	public static AlarmTrigger get()
	{
		if (singleton == null) {
			singleton = new AlarmTrigger();
		}
		return singleton;
	}

	public void enable(JavaPlugin plugin){
		this._plugin = plugin;
		this._enableCounter++;
		this._firstAlarmIndex = getFirstAlarmIndex();
		tick(this._enableCounter);
	}

	public void disable() {
		this._plugin = null;
	}

	public void setAlarm(LocalDateTime time, Runnable task, String name)
	{
		synchronized(this) {
			Alarm alarm = new Alarm(time, task, name);
			this._alarms.add(alarm);
			Alarm firstAlarm = getFirstAlarm();
			if ((firstAlarm == null) || firstAlarm.getTime().isAfter(time)) {
				this._firstAlarmIndex = this._alarms.size()-1;	
			}
		}
	}

	private boolean isEnabled()
	{
		return (this._plugin != null);
	}

	void tick(int enableCounter) {
		final int currentCounter = enableCounter;
		if (!isEnabled()) return;
		if (enableCounter < this._enableCounter) return;
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._plugin, new Runnable() {
			public void run() {
				tick(currentCounter);
			}
		}, TICK_LENGTH);
		scheduler.scheduleSyncDelayedTask(this._plugin, new Runnable() {
			public void run() {
				checkAlarms();
			}
		});
	}

	void checkAlarms() 
	{
		synchronized(this)
		{
			Alarm firstAlarm = getFirstAlarm();
			while ((firstAlarm != null) && firstAlarm.maybeSetOff()) {
				this._plugin.getLogger().info(String.format("The \"%s\" alarm set for %s was set off now (%s)", 
						firstAlarm.getName(), firstAlarm.getTime().toString(), LocalDateTime.now().toString()));
				this._alarms.remove(this._firstAlarmIndex);
				this._firstAlarmIndex = getFirstAlarmIndex();
				firstAlarm = this._alarms.get(this._firstAlarmIndex);				
			}
		}
	}

	private int getFirstAlarmIndex()
	{
		if (this._alarms.size() < 1) return -1;
		Alarm alarm = this._alarms.get(0);
		LocalDateTime firstAlarmTime = alarm.getTime();
		int firstAlarmIndex = 0;
		for (int i = 1; i < this._alarms.size(); i++) {
			alarm = this._alarms.get(i);
			if (alarm.getTime().isBefore(firstAlarmTime)) {
				firstAlarmIndex = i;
				firstAlarmTime = alarm.getTime();
			}		
		}

		return firstAlarmIndex;
	}

	private Alarm getFirstAlarm()
	{
		if (hasNoAlarms()) return null;
		return this._alarms.get(this._firstAlarmIndex);
	}

	private boolean hasNoAlarms() {
		return this._firstAlarmIndex == -1;
	}
}
