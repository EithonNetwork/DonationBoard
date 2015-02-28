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
		this._knownPlayers = new PlayerCollection<PlayerInfo>();
		loadNow();
	}

	int getMaxPerkLevel()
	{
		return numberOfLevels;
	}

	void disable() {
		updatePerkLevel(0);
		this._model = null;
		this._view = null;
		this._knownPlayers = new PlayerCollection<PlayerInfo>();
		this._plugin = null;
	}

	void increasePerkLevel(Player player, Block block) {
		if (!playerHasTokens(player)) {
			player.sendMessage("You must have E-tokens to raise the perk level.");
			player.sendMessage("You get E-tokens by donating money at http://eithon.org/donate.");
			return;
		}
		int day = markAsDonated(player, block);
		decreasePlayerDonationTokens(player);
		delayedSave();
		delayedRefresh();
	}

	public void initialize(Player player, Block clickedBlock) {
		this._model = new BoardModel(numberOfDays, numberOfLevels);
		this._view = new BoardView(clickedBlock);
		this._model.createFirstLineOfButtons();
		delayedSave();
		delayedRefresh();
	}

	public void shiftLeft() {
		this._model.shiftLeft();
		delayedRefresh();
	}

	public void saveNow()
	{
		File file = DonationBoardPlugin.getDonationsStorageFile();
		BoardStorageModel storageModel = new BoardStorageModel(this._view, this._model, this._knownPlayers);
		try {
			SavingAndLoadingBinary.save(storageModel, file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadNow()
	{
		File file = DonationBoardPlugin.getDonationsStorageFile();
		if(!file.exists()) return;
		BoardStorageModel storageModel;
		try {
			storageModel = SavingAndLoadingBinary.load(file);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		this._view = storageModel.getView();
		this._view.updateBoardModel(this._model);
		this._knownPlayers = storageModel.getKnownPlayers();
		delayedRefresh();
	}

	private void FindDonators() {
		for (PlayerInfo playerInfo : this._knownPlayers) {
			playerInfo.setIsOnTheBoard(false);
		}
		for (int day = 0; day <= BoardController.numberOfDays; day++) {
			for (int level = 0; level <= BoardController.numberOfLevels; level++) {
				Donation donation = this._model.getDonationInfo(day, level);
				if (donation == null) continue;
				Player player = donation.getPlayer();
				if (player == null) continue;
				PlayerInfo playerInfo = getOrAddPlayerInfo(player);
				playerInfo.setIsOnTheBoard(true);
			}
		}
	}	

	private boolean isDonator(Player player) {
		for (int day = 0; day <= BoardController.numberOfDays; day++) {
			for (int level = 0; level <= BoardController.numberOfLevels; level++) {
				Donation donation = this._model.getDonationInfo(day, level);
				if (donation == null) continue;
				if (donation.getPlayer() == player) return true;
			}
		}
		return false;
	}	

	public void print(Player player) {
		//this._model.print(player);
		for (PlayerInfo playerInfo : this._knownPlayers) {
			player.sendMessage(playerInfo.toString());
		}
	}

	public void register(Player player) {
		getOrAddPlayerInfo(player);
		maybePromotePlayer(player);
	}

	public void donate(Player player, int tokens) {
		PlayerInfo playerInfo = getOrAddPlayerInfo(player);
		playerInfo.addDonationTokens(tokens);
		maybePromotePlayer(player);
		delayedSave();
	}

	private void maybePromotePlayer(Player player) {
		PlayerInfo playerInfo = getOrAddPlayerInfo(player);
		if (playerInfo.shouldGetPerks()) {
			int toLevel = this._model.getDonationLevel(1);
			playerInfo.demoteOrPromote(toLevel, true);
		}
	}

	public void playerJoined(Player player) {
		PlayerInfo playerInfo = getOrAddPlayerInfo(player);
		if (isDonator(player)) {
			playerInfo.setIsOnTheBoard(true);
		}
		maybePromotePlayer(player);
	}

	void refreshNow() {
		if (this._model == null) return;
		this._view.refresh(this._model);
		FindDonators();
		updatePerkLevel();
	}

	private void updatePerkLevel() 
	{
		int toLevel = this._model.getDonationLevel(1);
		updatePerkLevel(toLevel);	
	}

	private boolean playerHasTokens(Player player)
	{
		PlayerInfo playerInfo = this._knownPlayers.get(player);
		if (playerInfo != null) {
			if (playerInfo.getDonationTokens() > 0) return true;
		}
		return false;
	}

	private void delayedRefresh() {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._plugin, new Runnable() {
			public void run() {
				refreshNow();
			}
		});
	}

	private int markAsDonated(Player player, Block block) {
		int day = this._view.calculateDay(block);
		int level = this._view.calculateLevel(block);
		this._model.markOnlyThis(day, level, player.getName());
		return day;
	}

	private void decreasePlayerDonationTokens(Player player) {
		PlayerInfo playerInfo = this._knownPlayers.get(player);
		playerInfo.usedOneToken();
	}

	private void delayedSave() {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._plugin, new Runnable() {
			public void run() {
				saveNow();
			}
		});
	}

	private void updatePerkLevel(int toLevel) 
	{
		for (PlayerInfo playerInfo : this._knownPlayers) {
			playerInfo.demoteOrPromote(toLevel, false);
		}	
	}

	private PlayerInfo getOrAddPlayerInfo(Player player) {
		PlayerInfo playerInfo = this._knownPlayers.get(player);
		if (playerInfo == null) {
			playerInfo = new PlayerInfo(player);
			this._knownPlayers.put(player, playerInfo);
		}
		return playerInfo;
	}
}
