package se.fredsfursten.donationboardplugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import se.fredsfursten.plugintools.Misc;

public class Commands {
	private static Commands singleton = null;
	private static final String ADD_COMMAND = "/donationboard add <name> <up speed> [<forward speed>]";
	private static final String GOTO_COMMAND = "/donationboard goto <name>";
	private static final String REMOVE_COMMAND = "/donationboard remove <name>";
	private static final String EDIT_COMMAND = "/donationboard edit <up speed> [<forward speed>]";
	private static final String RULES_COMMAND_BEGINNING = "/rules";

	private JavaPlugin plugin = null;
	private AllDonations allDonationBoards = null;

	private Commands() {
		this.allDonationBoards = AllDonations.get();
	}

	static Commands get()
	{
		if (singleton == null) {
			singleton = new Commands();
		}
		return singleton;
	}

	void enable(JavaPlugin plugin){
		this.plugin = plugin;
		this.allDonationBoards.load(plugin);
	}

	void disable() {
		this.allDonationBoards.save();
	}

	void addCommand(Player player, String[] args)
	{
		if (!verifyPermission(player, "donationboard.add")) return;
		if (!arrayLengthIsWithinInterval(args, 3, 4)) {
			player.sendMessage(ADD_COMMAND);
			return;
		}

		String name = args[1];
		if (!verifyNameIsNew(player, name)) return;	

		double upSpeed = 0.0;
		double forwardSpeed = 0.0;
		try 
		{
			upSpeed = Double.parseDouble(args[2]);
			if (args.length > 3)
			{
				forwardSpeed = Double.parseDouble(args[3]);
			}		
		} catch (Exception e) {
			player.sendMessage(ADD_COMMAND);
			return;
		}

		createOrUpdateDonationBoard(player, name, upSpeed, forwardSpeed);
	}

	private boolean verifyNameIsNew(Player player, String name) {
		DonationBoardInfo info = this.allDonationBoards.getByName(name);
		if (info != null)
		{
			player.sendMessage("donationBoard already exists: " + name);
			return true;		
		}
		return true;
	}

	private void createOrUpdateDonationBoard(Player player, String name, double upSpeed, double forwardSpeed) {
		Block pressurePlate = Misc.getFirstBlockOfMaterial(Material.STONE_PLATE, player.getLocation(), 3);
		if (pressurePlate == null) {
			player.sendMessage("No stone plate within 3 blocks");
			return;
		}
		
		Location location;
		Vector velocityVector;
		location = pressurePlate.getLocation();
		velocityVector = convertToVelocityVector(location, upSpeed, forwardSpeed);
		try {
			DonationBoardInfo newInfo = new DonationBoardInfo(name, location, velocityVector, player);
			this.allDonationBoards.add(newInfo);
			if (player != null) {
				Jumper.get().playerCanJump(player, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	private Vector convertToVelocityVector(Location location, double upSpeed, double forwardSpeed) {
		double yaw = location.getYaw();
		double rad = yaw*Math.PI/180.0;
		double vectorX = -Math.sin(rad)*forwardSpeed;
		double vectorY = upSpeed;
		double vectorZ = Math.cos(rad)*forwardSpeed;
		Vector jumpVector = new Vector(vectorX, vectorY, vectorZ);
		return jumpVector;
	}

	void editCommand(Player player, String[] args)
	{
		if (!verifyPermission(player, "donationboard.edit")) return;
		DonationBoardInfo info = this.allDonationBoards.getByLocation(player.getLocation());
		if (info == null) {
			player.sendMessage("You must go to a donationboard before you edit the donationboard. Use /donationboard goto <name>.");	
			return;
		}
		if (!arrayLengthIsWithinInterval(args, 2, 3)) {
			player.sendMessage(EDIT_COMMAND);
			return;
		}

		double upSpeed = 0.0;
		double forwardSpeed = 0.0;
		try 
		{
			upSpeed = Double.parseDouble(args[1]);

			if (args.length > 2)
			{
				forwardSpeed = Double.parseDouble(args[2]);
			}		
		} catch (Exception e) {
			player.sendMessage(EDIT_COMMAND);
			return;
		}

		createOrUpdateDonationBoard(player, info.getName(), upSpeed, forwardSpeed);
	}

	void removeCommand(Player player, String[] args)
	{
		if (!verifyPermission(player, "donationboard.remove")) return;
		if (!arrayLengthIsWithinInterval(args, 2, 2)) {
			player.sendMessage(REMOVE_COMMAND);
			return;
		}
		String name = args[1];
		DonationBoardInfo info = this.allDonationBoards.getByName(name);
		if (info == null)
		{
			player.sendMessage("Unknown donationboard: " + name);
			return;	
		}
		this.allDonationBoards.remove(info);
	}

	void gotoCommand(Player player, String[] args)
	{
		if (!verifyPermission(player, "donationboard.goto")) return;
		if (args.length < 2) {
			player.sendMessage(GOTO_COMMAND);
			return;
		}
		String name = args[1];
		DonationBoardInfo info = this.allDonationBoards.getByName(name);
		if (info == null)
		{
			player.sendMessage("Unknown donationboard: " + name);
			return;			
		}
		player.teleport(info.getLocation());
		// Temporarily disable jump for this player to avoid an immediate jump at the jump pad
		Jumper.get().playerCanJump(player, false);
	}

	void listCommand(Player player)
	{
		if (!verifyPermission(player, "donationboard.list")) return;

		player.sendMessage("Jump pads:");
		for (DonationBoardInfo info : this.allDonationBoards.getAll()) {
			player.sendMessage(info.toString());
		}
	}

	private boolean verifyPermission(Player player, String permission)
	{
		if (player.hasPermission(permission)) return true;
		player.sendMessage("You must have permission " + permission);
		return false;
	}

	static Vector calculateVelocity(Vector from, Vector to, int heightGain)
	{
		// Gravity of a potion
		double gravity = 0.115;

		// Block locations
		int endGain = to.getBlockY() - from.getBlockY();
		double horizDist = Math.sqrt(distanceSquared(from, to));

		// Height gain
		int gain = heightGain;

		double maxGain = gain > (endGain + gain) ? gain : (endGain + gain);

		// Solve quadratic equation for velocity
		double a = -horizDist * horizDist / (4 * maxGain);
		double b = horizDist;
		double c = -endGain;

		double slope = -b / (2 * a) - Math.sqrt(b * b - 4 * a * c) / (2 * a);

		// Vertical velocity
		double vy = Math.sqrt(maxGain * gravity);

		// Horizontal velocity
		double vh = vy / slope;

		// Calculate horizontal direction
		int dx = to.getBlockX() - from.getBlockX();
		int dz = to.getBlockZ() - from.getBlockZ();
		double mag = Math.sqrt(dx * dx + dz * dz);
		double dirx = dx / mag;
		double dirz = dz / mag;

		// Horizontal velocity components
		double vx = vh * dirx;
		double vz = vh * dirz;

		return new Vector(vx, vy, vz);
	}

	private static double distanceSquared(Vector from, Vector to)
	{
		double dx = to.getBlockX() - from.getBlockX();
		double dz = to.getBlockZ() - from.getBlockZ();

		return dx * dx + dz * dz;
	}

	void listenToCommands(Player player, String message) {
		if (message.toLowerCase().startsWith(RULES_COMMAND_BEGINNING))
		{
			player.sendMessage("Getting permission");
			player.addAttachment(this.plugin, "donationboard.jump", true);
		}
	}

	private boolean arrayLengthIsWithinInterval(Object[] args, int min, int max) {
		return (args.length >= min) && (args.length <= max);
	}
}
