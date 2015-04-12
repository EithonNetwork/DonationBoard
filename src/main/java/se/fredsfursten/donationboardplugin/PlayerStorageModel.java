package se.fredsfursten.donationboardplugin;

import java.io.Serializable;
import java.util.UUID;

@Deprecated
public class PlayerStorageModel implements Serializable {
	private static final long serialVersionUID = 1L;
	private UUID _id;
	private double _totalMoneyDonated;
	private long _totalTokensDonated;
	private int _remainingDonationTokens;
	
	public PlayerStorageModel(UUID playerId, int remainingDonationTokens, long totalTokensDonated, double totalMoneyDonated)
	{
		this._id = playerId;
		this._remainingDonationTokens = remainingDonationTokens;
		this._totalTokensDonated = totalTokensDonated;
		this._totalMoneyDonated = totalMoneyDonated;
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

	public UUID getUniqueId() {
		return this._id;
	}
}
