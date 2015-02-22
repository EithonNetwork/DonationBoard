package se.fredsfursten.donationboardplugin;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import se.fredsfursten.plugintools.PlayerCollection;
import se.fredsfursten.plugintools.SavingAndLoadingBinary;

public class BoardController {

	private static BoardController singleton = null;

	private static int numberOfDays;
	private static int numberOfLevels;

	private PlayerCollection<PlayerInfo> _knownPlayers;

	private BoardModel _model;
	private BoardView _view;
	private JavaPlugin _plugin = null;

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
		this._plugin = plugin;
		numberOfDays = DonationBoardPlugin.getPluginConfig().getInt("Days");
		numberOfLevels = DonationBoardPlugin.getPluginConfig().getInt("Levels");
		this._model = new BoardModel(numberOfDays, numberOfLevels);
		load(DonationBoardPlugin.getDonationsStorageFile());
		this._knownPlayers = new PlayerCollection<PlayerInfo>();
	}

	void disable() {
		changePerkLevel(0);
		this._model = null;
		this._view = null;
		this._knownPlayers = null;
	}

	void donate(Player player, Block block) {
		int day = this._view.calculateDay(block);
		int level = this._view.calculateLevel(block);
		this._model.markOnlyThis(day, level, player.getName());
		delayedRefresh();
	}

	public void initialize(Player player, Block clickedBlock) {
		this._model = new BoardModel(numberOfDays, numberOfLevels);
		this._view = new BoardView(clickedBlock);
		this._model.createFirstLineOfButtons();
		save(DonationBoardPlugin.getDonationsStorageFile());
		delayedRefresh();
	}

	private void delayedRefresh() {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._plugin, new Runnable() {
			public void run() {
				refreshNow();
			}
		});
	}

	void refreshNow() {
		if (this._model == null) return;
		this._view.refresh(this._model);
	}

	public void shiftLeft(Player player) {
		this._model.shiftLeft();
		changePerkLevel();
		delayedRefresh();
	}

	public void save(File file)
	{
		BoardStorageModel storageModel = new BoardStorageModel(this._view, this._model, this._knownPlayers);
		try {
			SavingAndLoadingBinary.save(storageModel, file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		this._view = storageModel.getView();
		this._view.updateBoardModel(this._model);
		this._knownPlayers = storageModel.getKnownPlayers();
		changePerkLevel();
		delayedRefresh();
	}

	public void print(Player player) {
		//this._model.print(player);
		for (PlayerInfo playerInfo : this._knownPlayers) {
			player.sendMessage(playerInfo.toString());
		}
	}

	public void changePerkLevel() 
	{
		int toLevel = this._model.getDonationLevel(0);
		changePerkLevel(toLevel);	
	}

	private void changePerkLevel(int toLevel) 
	{
		if (this._knownPlayers == null) return;
		for (PlayerInfo playerInfo : this._knownPlayers) {
			playerInfo.demoteOrPromote(toLevel);
		}	
	}

	public void register(Player player) {
		int toLevel = this._model.getDonationLevel(0);
		PlayerInfo playerInfo = new PlayerInfo(player);
		playerInfo.demoteOrPromote(toLevel);
		this._knownPlayers.put(player, playerInfo);
	}
}
