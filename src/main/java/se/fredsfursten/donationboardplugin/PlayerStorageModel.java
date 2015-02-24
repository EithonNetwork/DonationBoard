package se.fredsfursten.donationboardplugin;

import java.io.Serializable;
import java.util.UUID;

public class PlayerStorageModel implements Serializable {
	private static final long serialVersionUID = 1L;
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
