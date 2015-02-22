package se.fredsfursten.donationboardplugin;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerInfo {
	private static String addGroupCommand;
	private static String removeGroupCommand;

	private String name;
	private UUID id;
	private Player player;
	private int donationTokens;
	private int perkLevel;

	static
	{
		addGroupCommand = DonationBoardPlugin.getPluginConfig().getString("AddGroupCommand");
		removeGroupCommand = DonationBoardPlugin.getPluginConfig().getString("RemoveGroupCommand");
	}

	public PlayerInfo(Player player)
	{
		this.player = player;
		this.name = player.getName();
		this.id = player.getUniqueId();
		this.setDonationTokens(0);
		this.perkLevel = -1;
	}

	public PlayerInfo(UUID uniqueId, int donationTokens)
	{
		this.player = null;
		this.name = null;
		this.id = player.getUniqueId();
		this.setDonationTokens(donationTokens);
		this.perkLevel = -1;
	}

	public int getDonationTokens() {
		return this.donationTokens;
	}

	public void setDonationTokens(int donationTokens) {
		this.donationTokens = donationTokens;
	}

	public String getName() {
		if (this.name == null)
		{
			Player player = this.getPlayer();
			if (player == null) return null;
		}
		return this.name;
	}

	public Player getPlayer() {
		if (this.player == null)
		{
			this.player = Bukkit.getPlayer(getUniqueId());
		}
		return this.player;
	}

	public UUID getUniqueId() {
		return this.id;
	}

	public void demoteOrPromote(int toLevel) {
		if (toLevel == this.perkLevel) return;
		if (toLevel < this.perkLevel) {
			for (int level = this.perkLevel; level > toLevel; level--) {
				removeGroup(this.player, level);
				this.perkLevel = level;
			}
		} else {
			for (int level = this.perkLevel+1; level <= toLevel; level++) {
				addGroup(this.player, level);
				this.perkLevel = level;
			}			
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
		Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
	}
	
	public String toString()
	{
		return String.format("%s (%d tokens): %d perks", this.name, this.donationTokens, this.perkLevel);
	}
}
