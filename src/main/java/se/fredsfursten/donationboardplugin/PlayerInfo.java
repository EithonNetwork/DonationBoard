package se.fredsfursten.donationboardplugin;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import se.fredsfursten.plugintools.Misc;

public class PlayerInfo {
	private static String addGroupCommand;
	private static String removeGroupCommand;

	private String _name;
	private UUID _id;
	private Player _player;
	private int _donationTokens;
	private int _perkLevel;
	private boolean _isDonatorOnTheBoard;
	private boolean _hasBeenToBoard;

	static
	{
		addGroupCommand = DonationBoardPlugin.getPluginConfig().getString("AddGroupCommand");
		removeGroupCommand = DonationBoardPlugin.getPluginConfig().getString("RemoveGroupCommand");
	}

	public PlayerInfo(Player player)
	{
		this._player = player;
		this._name = player.getName();
		this._id = player.getUniqueId();
		this._donationTokens = 0;
		this._perkLevel = 0;
		this._hasBeenToBoard = false;
	}

	public PlayerInfo(UUID uniqueId, int donationTokens)
	{
		this._player = null;
		this._name = null;
		this._id = uniqueId;
		this._donationTokens = donationTokens;
		this._perkLevel = 0;
		this._hasBeenToBoard = false;
	}

	public int getDonationTokens() {
		return this._donationTokens;
	}

	public boolean shouldGetPerks() {
		return (this._donationTokens > 0) || this._isDonatorOnTheBoard || this._hasBeenToBoard;
	}

	public boolean shouldBeAutomaticallyPromoted() {
		return (this._donationTokens > 0) || this._isDonatorOnTheBoard;
	}

	public void addDonationTokens(int tokens) {
		this._donationTokens+=tokens;
		sendMessage(String.format("You now have %d E-tokens.", getDonationTokens()));
	}

	public void usedOneToken() {
		if (this._donationTokens < 0) {
			this._donationTokens = 0;
			return;
		}			
		this._donationTokens--;
		if (this._donationTokens < 0) this._donationTokens = 0;
		if (this._donationTokens == 0) {
			sendMessage("You have no E-tokens left.");
		} else {
			sendMessage(String.format("You have %d remaining E-tokens.", this._donationTokens));
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
			sendMessage(String.format("If you visit the donationboard, you can raise your perk level to %d.", toLevel));
			return;
		}
		for (int level = this._perkLevel + 1; level <= toLevel; level++) {
			addGroup(level);
		}
		this._perkLevel = toLevel;
		if (toLevel > currentLevel) {
			sendMessage(String.format("Your perk level has been raised to %d.", toLevel));
		}
	}

	private void demote(int toLevel, int currentLevel) {
		for (int level = this._perkLevel; level > toLevel; level--) {
			removeGroup(level);
		}
		this._perkLevel = toLevel;
		if (toLevel < currentLevel) {
			sendMessage(String.format("Your perk level has been lowered to %d.", toLevel));
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
		String command = String.format(addGroupCommand, this.getName(), level);
		Misc.executeCommand(command);
	}

	private void removeGroup(int level) {
		String command = String.format(removeGroupCommand, this.getName(), level);
		Misc.executeCommand(command);
	}

	public String toString()
	{
		return String.format("%s (%d tokens): perklevel %d", this.getName(), this._donationTokens, this._perkLevel);
	}
}
