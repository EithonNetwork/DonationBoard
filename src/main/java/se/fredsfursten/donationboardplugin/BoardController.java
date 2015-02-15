package se.fredsfursten.donationboardplugin;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import se.fredsfursten.plugintools.SavingAndLoadingBinary;

public class BoardController {
	private static BoardController singleton = null;

	public static final int TOTAL_DAYS = 31;
	public static final int TOTAL_LEVELS = 5;
	private static final String FILE_PATH = "plugins/DonationBoard/donations.bin";

	private BoardModel _model;
	private BoardView _view;
	private JavaPlugin plugin = null;

	private BoardController() {
		this._model = new BoardModel(TOTAL_DAYS, TOTAL_LEVELS);
	}

	static BoardController get()
	{
		if (singleton == null) {
			singleton = new BoardController();
		}
		return singleton;
	}

	void enable(JavaPlugin plugin){
		this.plugin = plugin;
	}

	void disable() {
	}

	void donate(Player player, Block block) {
		int day = this._view.calculateDay(block);
		int level = this._view.calculateLevel(block);
		this._model.markOnlyThis(day, level, player);
		delayedRefresh();
	}

	public void initialize(Player player, Block clickedBlock) {
		this._view = new BoardView(clickedBlock);
		this._model.createFirstLineOfButtons();
		delayedRefresh();
	}

	private void delayedRefresh() {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this.plugin, new Runnable() {
			public void run() {
				refreshNow();
			}
		});
	}

	void refreshNow() {
		this._view.refresh(this._model);
	}

	public void shiftLeft(Player player) {
		this._model.shiftLeft();
		delayedRefresh();
	}

	public void save()
	{
		BoardStorageModel storageModel = new BoardStorageModel(this._view, this._model);
		try {
			SavingAndLoadingBinary.save(storageModel, FILE_PATH);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void load()
	{
		BoardStorageModel storageModel;
		try {
			storageModel = SavingAndLoadingBinary.load(FILE_PATH);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		this._model = storageModel.getModel(TOTAL_DAYS, TOTAL_LEVELS);
		this._view = storageModel.getView();
		delayedRefresh();
	}

	public void print(Player player) {
		this._model.print(player);
	}
}
