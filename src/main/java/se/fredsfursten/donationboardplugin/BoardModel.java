package se.fredsfursten.donationboardplugin;

import org.bukkit.entity.Player;

public class BoardModel {
	public int _numberOfDays;
	public int _numberOfLevels;

	private Donation[][] _donations;

	BoardModel(int numberOfDays, int numberOfLevels) {
		this._numberOfDays = numberOfDays;
		this._numberOfLevels = numberOfLevels;
		this._donations = new Donation[this._numberOfDays][this._numberOfLevels];
		resetBoard();
	}

	public int getNumberOfDays()
	{
		return this._numberOfDays;
	}

	public int getNumberOfLevels()
	{
		return this._numberOfLevels;
	}

	public void createFirstLineOfButtons() {
		for (int day = 0; day < this._numberOfDays; day++) {
			initializeNewDay(day);
		}
	}

	private void initializeNewDay(int day) {
		for (int level = 0; level < this._numberOfLevels; level++) {
			this._donations[day][level].setEmpty();
		}
	}

	public void shiftLeft() {
		// Copy values from right
		for (int day = 0; day < this._numberOfDays-1; day++) {
			for (int level = 0; level < this._numberOfLevels; level++) {
				this._donations[day][level].copy(this._donations[day+1][level]);
			}
		}			
		// Initialize the last day
		initializeNewDay(this._numberOfDays-1);
	}

	public BoardModel clone() {
		BoardModel clone = new BoardModel(this._numberOfDays, this._numberOfLevels);
		for (int day = 0; day < this._numberOfDays ; day++) {
			for (int level = 0; level < this._numberOfLevels; level++) {
				clone._donations[day][level].copy(this._donations[day][level]);
			}
		}
		return clone;
	}

	public void markOnlyThis(int day, int level, Player player) {
		if (!isInsideBoard(day, level)) return;
		if (player == null)
		{
			this._donations[day][level].setEmpty();		
		} else {
			this._donations[day][level].setDonation(player);			
		}
	}

	public boolean isInsideBoard(int day, int level) {
		if (day < 0) return false;
		if (day >= this._numberOfDays) return false;
		if (level < 0) return false;
		if (level >= this._numberOfLevels) return false;
		return true;
	}

	public boolean isSame(BoardModel board, int day, int level) {
		if (!isInsideBoard(day, level)) {
			if ((board == null) || !board.isInsideBoard(day, level)) return true;
		} else if ((board == null) || !board.isInsideBoard(day, level)) return false;
		return this._donations[day][level].isSame(board._donations[day][level]);
	}

	public Donation getDonationInfo(int day, int level) {
		if (!isInsideBoard(day, level)) return null;
		return this._donations[day][level];
	}

	private void resetBoard() {
		for (int day = 0; day < this._numberOfDays; day++) {
			for (int level = 0; level < this._numberOfLevels; level++) {
				this._donations[day][level] = new Donation();
			}
		}
	}

	public void print(Player player) {
		for (int day = 0; day < this._numberOfDays; day++) {
			for (int level = 0; level < this._numberOfLevels; level++) {
				String message = String.format("%d,%d: %s", day, level, this._donations[day][level].toString());
				if (player != null) player.sendMessage(message);
				else System.out.println(message);
			}
		}
	}

	public int donationLevel(int day) {
		int donationLevel = -1;
		for (int level = 0; level < this._numberOfLevels; level++) {
			if (!this._donations[day][level].isDonation()) break;
			donationLevel = level;
		}
		return donationLevel;
	}
}
