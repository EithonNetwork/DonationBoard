package se.fredsfursten.donationboardplugin;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;

import se.fredsfursten.plugintools.ConfigurableFormat;
import se.fredsfursten.plugintools.IJson;
import se.fredsfursten.plugintools.IUuidAndName;
import se.fredsfursten.plugintools.Json;
import se.fredsfursten.plugintools.Misc;
import se.fredsfursten.plugintools.PluginConfig;

public class PlayerInfo implements IJson<PlayerInfo>, IUuidAndName  {
	private static ConfigurableFormat addGroupCommandMessage;
	private static ConfigurableFormat removeGroupCommandMessage;
	private static ConfigurableFormat visitBoardMessage;
	private static ConfigurableFormat levelRaisedMessage;
	private static ConfigurableFormat levelLoweredMessage;
	private static ConfigurableFormat noTokensLeftMessage;
	private static ConfigurableFormat tokensLeftMessage;

	private String _name;
	private UUID _id;
	private Player _player;
	private int _remainingDonationTokens;
	private double _totalMoneyDonated;
	private long _totalTokensDonated;
	private int _perkLevel;
	private boolean _isDonatorOnTheBoard;
	private boolean _hasBeenToBoard;

	static void initialize(JavaPlugin plugin)
	{
		PluginConfig config = PluginConfig.get(plugin);
		addGroupCommandMessage = new ConfigurableFormat(config, "AddGroupCommand", 2,
				"perm player %s addgroup PerkLevel%d");
		removeGroupCommandMessage = new ConfigurableFormat(config, "RemoveGroupCommand", 2,
				"perm player %s removegroup PerkLevel%d");
		visitBoardMessage = new ConfigurableFormat(config, "VisitBoardMessage", 1,
				"If you visit the donationboard, you can raise your perk level to %d.");
		levelRaisedMessage = new ConfigurableFormat(config, "PerkLevelRaisedMessage", 1,
				"Your perk level has been raised to %d.");
		levelLoweredMessage = new ConfigurableFormat(config, "PerkLevelLoweredMessage", 1,
				"Your perk level has been lowered to %d.");
		noTokensLeftMessage = new ConfigurableFormat(config, "NoTokensLeftMessage", 0,
				"You have no E-tokens left.");
		tokensLeftMessage = new ConfigurableFormat(config, "TokensLeftMessage", 1,
				"You have %d remaining E-tokens.");
	}

	public PlayerInfo(Player player)
	{
		this._player = player;
		this._name = player.getName();
		this._id = player.getUniqueId();
		this._remainingDonationTokens = 0;
		this._perkLevel = 0;
		this._hasBeenToBoard = false;
	}

	public PlayerInfo(UUID uniqueId, int remainingDonationTokens, long totalTokensDonated, double totalAmountDonated)
	{
		this._player = null;
		this._name = null;
		this._id = uniqueId;
		this._remainingDonationTokens = remainingDonationTokens;
		this._totalTokensDonated = totalTokensDonated;
		this._totalMoneyDonated = totalAmountDonated;
		this._perkLevel = 0;
		this._hasBeenToBoard = false;
	}

	public PlayerInfo(UUID uniqueId, String name, int remainingDonationTokens, long totalTokensDonated, double totalAmountDonated)
	{
		this._player = null;
		this._name = name;
		this._id = uniqueId;
		this._remainingDonationTokens = remainingDonationTokens;
		this._totalTokensDonated = totalTokensDonated;
		this._totalMoneyDonated = totalAmountDonated;
		this._perkLevel = 0;
		this._hasBeenToBoard = false;
	}

	PlayerInfo() {
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("player", Json.fromPlayer(this._id, this._name));
		json.put("remainingDonationTokens", this._remainingDonationTokens);
		json.put("totalTokensDonated", this._totalTokensDonated);
		json.put("totalMoneyDonated", this._totalMoneyDonated);
		return json;
	}

	public int getRemainingDonationTokens() {
		return this._remainingDonationTokens;
	}

	public long getTotalTokensDonated() {
		return this._totalTokensDonated;
	}

	public double getTotalMoneyDonated() {
		return this._totalMoneyDonated;
	}

	public boolean shouldGetPerks() {
		return (this._remainingDonationTokens > 0) || this._isDonatorOnTheBoard || this._hasBeenToBoard;
	}

	public boolean shouldBeAutomaticallyPromoted() {
		return (this._remainingDonationTokens > 0) || this._isDonatorOnTheBoard;
	}

	public void addDonationTokens(int tokens, double amount) {
		this._remainingDonationTokens+=tokens;
		this._totalTokensDonated += tokens;
		this._totalMoneyDonated += amount;
		sendMessage(String.format("You now have %d E-tokens to use on the donation board.", getRemainingDonationTokens()));
	}

	public void usedOneToken() {
		if (this._remainingDonationTokens < 0) {
			this._remainingDonationTokens = 0;
			return;
		}			
		this._remainingDonationTokens--;
		if (this._remainingDonationTokens < 0) this._remainingDonationTokens = 0;
		if (this._remainingDonationTokens == 0) {
			noTokensLeftMessage.sendMessage(getPlayer());
		} else {
			tokensLeftMessage.sendMessage(getPlayer(), this._remainingDonationTokens);
		}
	}

	public String getName() {
		if (this._name == null)
		{
			Player player = this.getPlayer();
			if (player == null) return null;
			this._name = player.getName();
		}
		return this._name;
	}

	public Player getPlayer() {
		if (this._player == null)
		{
			this._player = Bukkit.getPlayer(getUniqueId());
		}
		return this._player;
	}

	public UUID getUniqueId() {
		return this._id;
	}

	public void demoteOrPromote(int toLevel, boolean reset) {
		int perkLevelBeforeReset = this._perkLevel;
		if (reset) {
			resetPerkLevel(true);
			this._perkLevel = 0;
		}
		int currentPerkLevel = this._perkLevel;
		if (toLevel < currentPerkLevel) {
			demote(toLevel, perkLevelBeforeReset);
		} else if (toLevel > currentPerkLevel) {
			promote(toLevel, perkLevelBeforeReset);
		}
	}

	public void setIsDonatorOnTheBoard(boolean isDonatorOnTheBoard)
	{
		this._isDonatorOnTheBoard = isDonatorOnTheBoard;
	}

	public void markAsHasBeenToBoard()
	{
		this._hasBeenToBoard = true;
	}

	public void resetHasBeenToBoard()
	{
		this._hasBeenToBoard = false;
	}

	private void promote(int toLevel, int currentLevel) {
		if (!shouldGetPerks()) {
			visitBoardMessage.sendMessage(this.getPlayer(), toLevel);
			return;
		}
		for (int level = this._perkLevel + 1; level <= toLevel; level++) {
			addGroup(level);
		}
		this._perkLevel = toLevel;
		if (toLevel > currentLevel) {
			levelRaisedMessage.sendMessage(getPlayer(), toLevel);
		}
	}

	private void demote(int toLevel, int currentLevel) {
		for (int level = this._perkLevel; level > toLevel; level--) {
			removeGroup(level);
		}
		this._perkLevel = toLevel;
		if (toLevel < currentLevel) {
			levelLoweredMessage.sendMessage(getPlayer(), toLevel);
		}
	}

	private void resetPerkLevel(boolean force) {
		if (force) this._perkLevel = BoardController.get().getMaxPerkLevel();
		demote(0, 0);
	}

	private void sendMessage(String message) {
		Player player = this.getPlayer();
		if (player != null) {
			player.sendMessage(message);
		}
	}

	private void addGroup(int level) {
		String command = addGroupCommandMessage.getMessage(this.getName(), level);
		Misc.executeCommand(command);
	}

	private void removeGroup(int level) {
		String command = removeGroupCommandMessage.getMessage(this.getName(), level);
		Misc.executeCommand(command);
	}

	public String toString()
	{
		return String.format("%s (%d tokens): perklevel %d", this.getName(), this._remainingDonationTokens, this._perkLevel);
	}

	@Override
	public PlayerInfo factory() {
		return new PlayerInfo();
	}

	@Override
	public void fromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		this._id = Json.toPlayerId((JSONObject) jsonObject.get("player"));
		this._name = Json.toPlayerName((JSONObject) jsonObject.get("player"));
		this._remainingDonationTokens = (int) jsonObject.get("remainingDonationTokens");
		this._totalTokensDonated = (long) jsonObject.get("totalTokensDonated");
		this._totalMoneyDonated = (double) jsonObject.get("totalMoneyDonated");
	}
}
