package se.fredsfursten.donationboardplugin;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerInfo {
	private static String addGroupCommand;
	private static String removeGroupCommand;

	private String _name;
	private UUID _id;
	private Player _player;
	private int _donationTokens;
	private int _perkLevel;
	private boolean _shouldGetPerks;

	static
	{
		addGroupCommand = DonationBoardPlugin.getPluginConfig().getString("AddGroupCommand");
		removeGroupCommand = DonationBoardPlugin.getPluginConfig().getString("RemoveGroupCommand");
	}

	public PlayerInfo(Player player, boolean shouldGetPerks)
	{
		this._player = player;
		this._name = player.getName();
		this._id = player.getUniqueId();
		this._donationTokens = 0;
		this._perkLevel = 0;
		this._shouldGetPerks = shouldGetPerks;
	}

	public PlayerInfo(UUID uniqueId, int donationTokens)
	{
		this._player = null;
		this._name = null;
		this._id = uniqueId;
		this._donationTokens = donationTokens;
		this._perkLevel = 0;
		this._shouldGetPerks = donationTokens > 0;
	}

	public int getDonationTokens() {
		return this._donationTokens;
	}

	public void addDonationTokens(int tokens) {
		this._donationTokens+=tokens;
		sendMessage(String.format("You now have %d E-tokens.", getDonationTokens()));
		if (this._donationTokens > 0) this._shouldGetPerks = true;
	}

	public void subtractDonationTokens(int tokens) {
		this._donationTokens-=tokens;
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

	public void demoteOrPromote(int toLevel, boolean force) {
		int currentPerkLevel = this._perkLevel;
		if (force) resetPerkLevel(true);
		if (toLevel < currentPerkLevel) {
			demote(toLevel);
			sendMessage(String.format("Your perk level has been lowered to %d.", toLevel));
		} else if ((toLevel > currentPerkLevel) && this._shouldGetPerks) {
			promote(toLevel);
			sendMessage(String.format("Your perk level has been raised to %d.", toLevel));
		}
	}

	private void promote(int toLevel) {
		if (!this._shouldGetPerks) return;
		for (int level = this._perkLevel + 1; level <= toLevel; level++) {
			addGroup(level);
		}
		this._perkLevel = toLevel;
	}

	private void demote(int toLevel) {
		for (int level = this._perkLevel; level > toLevel; level--) {
			removeGroup(level);
		}
		this._perkLevel = toLevel;
	}

	private void resetPerkLevel(boolean force) {
		if (force) this._perkLevel = BoardController.get().getMaxPerkLevel();
		demote(0);
	}

	private void sendMessage(String message) {
		Player player = this.getPlayer();
		if (player != null) {
			player.sendMessage(message);
		}
	}

	private void addGroup(int level) {
		String command = String.format(addGroupCommand, this.getName(), level);
		executeCommand(command);
	}

	private void removeGroup(int level) {
		String command = String.format(removeGroupCommand, this.getName(), level);
		executeCommand(command);
	}

	private void executeCommand(String command)
	{
		Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
	}

	public String toString()
	{
		return String.format("%s (%d tokens): %d perks", this.getName(), this._donationTokens, this._perkLevel);
	}
}
