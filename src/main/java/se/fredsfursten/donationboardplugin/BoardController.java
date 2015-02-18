package se.fredsfursten.donationboardplugin;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import se.fredsfursten.plugintools.SavingAndLoadingBinary;

public class BoardController {
	private static String addGroupCommand;
	private static String removeGroupCommand;

	private static BoardController singleton = null;

	private static int numberOfDays;
	private static int numberOfLevels;

	private BoardModel _model;
	private BoardView _view;
	private JavaPlugin plugin = null;

	private BoardController() {
	}

	static BoardController get()
	{
		if (singleton == null) {
			singleton = new BoardController();
		}
		return singleton;
	}

	void enable(JavaPlugin plugin){
		this.plugin = plugin;
		numberOfDays = DonationBoardPlugin.getPluginConfig().getInt("Days");
		numberOfLevels = DonationBoardPlugin.getPluginConfig().getInt("Levels");
		addGroupCommand = DonationBoardPlugin.getPluginConfig().getString("AddGroupCommand");
		removeGroupCommand = DonationBoardPlugin.getPluginConfig().getString("RemoveGroupCommand");
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this.plugin, new Runnable() {
			public void run() {
				load(DonationBoardPlugin.getDonationsStorageFile());
			}
		}, 100L);
	}

	void disable() {
		this._model = null;
		this._view = null;
	}

	void donate(Player player, Block block) {
		int day = this._view.calculateDay(block);
		int level = this._view.calculateLevel(block);
		this._model.markOnlyThis(day, level, player);
		delayedRefresh();
	}

	public void initialize(Player player, Block clickedBlock) {
		this._model = new BoardModel(numberOfDays, numberOfLevels);
		this._view = new BoardView(clickedBlock);
		this._model.createFirstLineOfButtons();
		delayedRefresh();
	}

	private void delayedRefresh() {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this.plugin, new Runnable() {
			public void run() {
				refreshNow();
			}
		});
	}

	void refreshNow() {
		if (this._model == null) return;
		this._view.refresh(this._model);
		//save(DonationBoardPlugin.getDonationsStorageFile());
	}

	public void shiftLeft(Player player) {
		this._model.shiftLeft();
		delayedRefresh();
	}

	public void save(File file)
	{
		BoardStorageModel storageModel = new BoardStorageModel(this._view, this._model);
		try {
			SavingAndLoadingBinary.save(storageModel, file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Save");
		print(null);
		load(file);
	}

	public void load(File file)
	{
		if(!file.exists()) return;
		BoardStorageModel storageModel;
		try {
			storageModel = SavingAndLoadingBinary.load(file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		this._model = storageModel.getModel(numberOfDays, numberOfLevels);
		this._view = storageModel.getView();
		System.out.println("Load");
		print(null);
		delayedRefresh();
	}

	public void print(Player player) {
		this._model.print(player);
	}

	public void promote(Player player) {
		int currentDonationlevel = this._model.donationLevel(0);
		for (int level = 0; level <= currentDonationlevel; level++) {
			addGroup(player, level);
		}
	}

	public void demote(Player player) {
		for (int level = 0; level < numberOfLevels; level++) {
			removeGroup(player, level);
		}
	}

	private void addGroup(Player player, int level) {
		String command = String.format(addGroupCommand, player.getName(), level+1);
		player.sendMessage(command);
		executeCommand(command);
	}

	private void removeGroup(Player player, int level) {
		String command = String.format(removeGroupCommand, player.getName(), level+1);
		player.sendMessage(command);
		executeCommand(command);
	}

	private void executeCommand(String command)
	{
		this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), command);
	}
}
