package se.fredsfursten.donationboardplugin;

import java.io.File;
import java.time.LocalDateTime;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import se.fredsfursten.plugintools.AlarmTrigger;
import se.fredsfursten.plugintools.ConfigurableFormat;
import se.fredsfursten.plugintools.PlayerCollection;
import se.fredsfursten.plugintools.SavingAndLoadingBinary;

public class BoardController {

	private static BoardController singleton = null;

	private static int numberOfDays;
	private static int numberOfLevels;
	private static long perkClaimAfterSeconds;
	private static ConfigurableFormat needTokensMessage;
	private static ConfigurableFormat howToGetTokensMessage;
	private static ConfigurableFormat playerHasDonatedMessage;

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
		perkClaimAfterSeconds = DonationBoardPlugin.getPluginConfig().getInt("PerkClaimAfterSeconds");
		needTokensMessage = new ConfigurableFormat("NeedTokensMessage", 0,
				"You must have E-tokens to raise the perk level.");
		howToGetTokensMessage = new ConfigurableFormat("HowToGetTokens", 0,
				"You get E-tokens by donating money at http://eithon.org/donate.");
		playerHasDonatedMessage = new ConfigurableFormat("PlayerHasDonatedMessage", 1,
				"Player %s has made a donation for today!");
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
			needTokensMessage.sendMessage(player);
			howToGetTokensMessage.sendMessage(player);
			return;
		}
		decreasePlayerDonationTokens(player);
		int day = markAsDonated(player, block);
		if (day == 1) broadCastDonation(player);
		playersNeedToRevisitBoard();
		delayedSave();
		delayedRefresh();
	}

	private void broadCastDonation(Player player) {
		this._plugin.getServer().broadcastMessage(playerHasDonatedMessage.getMessage(player.getDisplayName()));
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
		playersNeedToRevisitBoard();
		delayedRefresh();
	}

	public void saveNow()
	{
		if (this._view == null) return;
		File file = DonationBoardPlugin.getDonationsStorageFile();
		BoardStorageModel storageModel = new BoardStorageModel(this._view, this._model, this._knownPlayers);
		try {
			SavingAndLoadingBinary.save(storageModel, file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void delayedLoad() {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._plugin, new Runnable() {
			public void run() {
				loadNow();
			}
		}, 200L);
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
		if (this._view == null) {
			Bukkit.getLogger().warning("Could not load donation board.");
			delayedLoad();
			return;
		}
		this._view.updateBoardModel(this._model);
		this._knownPlayers = storageModel.getKnownPlayers();
		Bukkit.getLogger().info("Loaded donation board.");
		delayedRefresh();
	}

	private void FindDonators() {
		for (PlayerInfo playerInfo : this._knownPlayers) {
			playerInfo.setIsDonatorOnTheBoard(false);
		}
		for (int day = 0; day <= BoardController.numberOfDays; day++) {
			for (int level = 0; level <= BoardController.numberOfLevels; level++) {
				Donation donation = this._model.getDonationInfo(day, level);
				if (donation == null) continue;
				Player player = donation.getPlayer();
				if (player == null) continue;
				PlayerInfo playerInfo = getOrAddPlayerInfo(player);
				playerInfo.setIsDonatorOnTheBoard(true);
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
		PlayerInfo playerInfo = getOrAddPlayerInfo(player);
		playerInfo.markAsHasBeenToBoard();
		maybePromotePlayer(player, true);
	}

	public void donate(Player player, int tokens, double amount) {
		PlayerInfo playerInfo = getOrAddPlayerInfo(player);
		playerInfo.addDonationTokens(tokens, amount);
		maybePromotePlayer(player, false);
		delayedSave();
	}

	private void playersNeedToRevisitBoard() {
		for (PlayerInfo playerInfo : this._knownPlayers) {
			if (!playerInfo.shouldBeAutomaticallyPromoted())  {
				playerInfo.resetHasBeenToBoard();
			}
		}	
	}

	private void maybePromotePlayer(Player player, boolean forceReset) {
		PlayerInfo playerInfo = getOrAddPlayerInfo(player);
		int toLevel = this._model.getDonationLevel(1);
		playerInfo.demoteOrPromote(toLevel, forceReset);
	}

	public void playerJoined(Player player) {
		PlayerInfo playerInfo = getOrAddPlayerInfo(player);
		if (isDonator(player)) {
			playerInfo.setIsDonatorOnTheBoard(true);
		}
		maybePromotePlayer(player, true);
	}

	public void playerTeleportedToBoard(Player player, Location from) 
	{
		if (!DonationBoardPlugin.isInMandatoryWorld(player.getWorld())) return;
		PlayerInfo playerInfo = this._knownPlayers.get(player);
		if (playerInfo.shouldGetPerks()) return;
		LocalDateTime alarm = LocalDateTime.now().plusSeconds(perkClaimAfterSeconds);
		AlarmTrigger.get().setAlarm(String.format("%s can claim perk", player.getName()),
				alarm,
				new Runnable() {
			public void run() {
				if (DonationBoardPlugin.isInMandatoryWorld(player.getWorld())) {
					register(player);
				}
			}
		});	
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
			if (playerInfo.getRemainingDonationTokens() > 0) return true;
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

	public Location getBoardLocation() {
		return this._view.getLocation();
	}

	public void stats(CommandSender sender) {
		for (PlayerInfo playerInfo : this._knownPlayers) {
			stats(sender, playerInfo);
		}
	}

	public void stats(CommandSender sender, Player player)
	{
		PlayerInfo playerInfo = this._knownPlayers.get(player);
		if (playerInfo == null) {
			sender.sendMessage(String.format("%s has no donation information.", player.getName()));
			return;
		}
		stats(sender, playerInfo);
	}

	public void stats(CommandSender sender, PlayerInfo playerInfo)
	{
		sender.sendMessage(String.format("%s has %d E-tokens, of %d (%.2f€) in total.", 
				playerInfo.getName(),
				playerInfo.getRemainingDonationTokens(),
				playerInfo.getTotalTokensDonated(),
				playerInfo.getTotalMoneyDonated()));	
	}
}
