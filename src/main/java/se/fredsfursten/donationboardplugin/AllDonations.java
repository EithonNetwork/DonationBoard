package se.fredsfursten.donationboardplugin;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public class AllDonations {
	private static final String FILE_PATH = "plugins/DonationBoard/donationboard_locations.bin";
	private static AllDonations singleton = null;

	private HashMap<String, DonationBoardInfo> donationBoardsByBlock = null;
	private HashMap<String, DonationBoardInfo> donationBoardsByName = null;
	private JavaPlugin _plugin = null;

	private AllDonations() {
	}

	static AllDonations get() {
		if (singleton == null) {
			singleton = new AllDonations();
		}
		return singleton;
	}

	void add(DonationBoardInfo info) {
		this.donationBoardsByBlock.put(info.getBlockHash(), info);
		this.donationBoardsByName.put(info.getName(), info);
	}

	void remove(DonationBoardInfo info) {
		this.donationBoardsByName.remove(info.getName());
		this.donationBoardsByBlock.remove(info.getBlockHash());
	}

	Collection<DonationBoardInfo> getAll() {
		return this.donationBoardsByName.values();
	}

	DonationBoardInfo getByLocation(Location location) {
		if (this.donationBoardsByBlock == null) return null;
		String position = DonationBoardInfo.toBlockHash(location);
		if (!this.donationBoardsByBlock.containsKey(position)) return null;
		return this.donationBoardsByBlock.get(position);
	}

	DonationBoardInfo getByName(String name) {
		if (!this.donationBoardsByName.containsKey(name)) return null;
		return this.donationBoardsByName.get(name);
	}

	void load(JavaPlugin plugin) {
		this._plugin = plugin;

		this.donationBoardsByBlock = new HashMap<String, DonationBoardInfo>();
		this.donationBoardsByName = new HashMap<String, DonationBoardInfo>();

		ArrayList<StorageModel> donationBoardStorageList = loadData(plugin);
		if (donationBoardStorageList == null) return;
		rememberAllData(donationBoardStorageList);
		this._plugin.getLogger().info(String.format("Loaded %d DonationBoards", donationBoardStorageList.size()));
	}

	private ArrayList<StorageModel> loadData(JavaPlugin plugin) {
		ArrayList<StorageModel> donationBoardStorageList = null;
		try {
			donationBoardStorageList = SavingAndLoadingBinary.load(FILE_PATH);
		} catch (FileNotFoundException e) {
			plugin.getLogger().info("No jump pad data file found.");
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			plugin.getLogger().info("Failed to load data.");
			return null;
		}
		return donationBoardStorageList;
	}

	private void rememberAllData(ArrayList<StorageModel> storageModelList) {
		for (StorageModel storageModel : storageModelList) {
			this.add(DonationBoardInfo.createDonationBoardInfo(storageModel));
		}
	}

	void save() {
		ArrayList<StorageModel> donationBoardStorageList = getAllData();
		boolean success = saveData(donationBoardStorageList);
		if (success) {
			this._plugin.getLogger().info(String.format("Saved %d DonationBoards", donationBoardStorageList.size()));
		} else {
			this._plugin.getLogger().info("Failed to save data.");			
		}
	}

	private ArrayList<StorageModel> getAllData() {
		ArrayList<StorageModel> donationBoardStorageList = new ArrayList<StorageModel>();
		for (DonationBoardInfo donationBoardInfo : getAll()) {
			donationBoardStorageList.add(donationBoardInfo.getStorageModel());
		}
		return donationBoardStorageList;
	}

	private boolean saveData(ArrayList<StorageModel> donationBoardStorageList) {
		try {
			SavingAndLoadingBinary.save(donationBoardStorageList, FILE_PATH);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
