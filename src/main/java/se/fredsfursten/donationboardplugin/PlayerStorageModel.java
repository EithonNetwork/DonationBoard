package se.fredsfursten.donationboardplugin;

import java.util.UUID;

public class PlayerStorageModel {
	private UUID id;
	private int donationTokens;
	
	public PlayerStorageModel(UUID playerId, int donationTokens)
	{
		this.id = playerId;
		this.donationTokens = donationTokens;
	}

	public int getDonationTokens() {
		return this.donationTokens;
	}

	public UUID getUniqueId() {
		return this.id;
	}
}
