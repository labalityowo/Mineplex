package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;
import nautilus.game.arcade.game.games.mineware.challenge.TeamChallenge;

/**
 * A challenge based on volley.
 * 
 * @deprecated
 */
public class ChallengeVolleyPig extends TeamChallenge
{
	private static final int CHALLENGE_PLAYERS_MAX = 25;
	private static final int LOCKED_INVENTORY_SLOT = 4;
	private static final int CHALLENGE_DURATION = 30000;
	private static final int MAP_SPAWN_SHIFT = 1;
	private static final int MAP_X = 6;
	private static final int MAP_SPAWN_X = MAP_X - 1;
	private static final int MAP_HEIGHT = 3;
	private static final int MAP_SPAWN_HEIGHT = MAP_HEIGHT - 2;

	private static final byte BLUE_STAINED_GLASS = 11;
	private static final byte RED_STAINED_GLASS = 14;

	// Relative to map center.
	private static final int BLUE_CENTER_X = 0;
	private static final int BLUE_CENTER_Y = 3;
	private static final int BLUE_CENTER_Z = 5;
	private static final int RED_CENTER_X = 0;
	private static final int RED_CENTER_Y = 3;
	private static final int RED_CENTER_Z = -5;

	private static final double PIG_PUSH_DAMAGE = 0.001;

	// Relative to map center.
	private static final double PIG_CENTER_X = 7.5;
	private static final double PIG_CENTER_Y = 1;
	private static final double PIG_CENTER_Z = 0.5;

	private static final int KNOCKBACK_MESSAGE_COOLDOWN = 1000;
	private static final double MAX_BLOCK_SHIFT = 0.3;
	private static final int SCORE_GAIN = 50;
	private static final int SCORE_GOAL = 10000;
	private static final int BAR_AMOUNT = 24;

	private Location _blueCenter, _redCenter;
	private Pig _pig;
	private long _blueSide, _redSide;

	public ChallengeVolleyPig(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.FirstComplete,
			"Blue",
			"Red",
			true,
			(byte) 11,
			(byte) 14,
			"Volley Pig",
			"Punch the pig on the enemy side.");

		Settings.setUseMapHeight();
		Settings.setMaxPlayers(CHALLENGE_PLAYERS_MAX);
		Settings.setTeamBased();
		Settings.setLockInventory(LOCKED_INVENTORY_SLOT);
		Settings.setDuration(CHALLENGE_DURATION);
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();
		int size = getArenaSize() - MAP_SPAWN_SHIFT;

		for (int x = -size; x <= MAP_SPAWN_X; x++)
		{
			for (int z = -size; z <= size; z++)
			{
				if (z != 0)
				{
					spawns.add(getCenter().add(x, MAP_SPAWN_HEIGHT, z));
				}
			}
		}

		return spawns;
	}

	@Override
	public void createMap()
	{
		for (int x = -getArenaSize(); x <= MAP_X; x++)
		{
			for (int z = -getArenaSize(); z <= getArenaSize(); z++)
			{
				for (int y = 0; y <= MAP_HEIGHT; y++)
				{
					Block block = getCenter().getBlock().getRelative(x, y, z);

					if (x == MAP_X || x == -getArenaSize() || y == 0 || Math.abs(z) == getArenaSize())
					{
						setBlock(block, z == 0 ? Material.STAINED_GLASS : Material.STAINED_CLAY, (z < 0 ? BLUE_STAINED_GLASS : z > 0 ? RED_STAINED_GLASS : 0));
						addBlock(block);
					}
				}
			}
		}
	}

	@Override
	public void onStart()
	{
		Host.DamagePvE = true;

		_blueCenter = getCenter().add(BLUE_CENTER_X, BLUE_CENTER_Y, BLUE_CENTER_Z);
		_redCenter = getCenter().add(RED_CENTER_X, RED_CENTER_Y, RED_CENTER_Z);

		spawnPig();
		equipKnockbackStick();
		equipTeamHelmets();
		startMainTask();
	}

	@Override
	public void onEnd()
	{
		Host.DamagePvE = false;

		if (_pig != null)
		{
			_pig.remove();
		}

		_pig = null;
		_blueSide = 0;
		_redSide = 0;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerMove(PlayerMoveEvent event)
	{
		if (!isChallengeValid())
			return;

		Player player = event.getPlayer();

		if (!isPlayerValid(player))
			return;

		Location from = event.getFrom();
		Location to = event.getTo();

		Block fromBlock = from.getBlock().getRelative(BlockFace.DOWN);
		Block toBlock = to.getBlock().getRelative(BlockFace.DOWN);

		if (!fromBlock.isEmpty() && !toBlock.isEmpty())
		{
			boolean crossedBlue = getSecondTeam().isMember(player) && (fromBlock.getData() == BLUE_STAINED_GLASS || toBlock.getData() == BLUE_STAINED_GLASS);
			boolean crossedRed = getFirstTeam().isMember(player) && (fromBlock.getData() == RED_STAINED_GLASS || toBlock.getData() == RED_STAINED_GLASS);
			boolean fromStainedGlass = fromBlock.getType() == Material.STAINED_GLASS;
			boolean toStainedGlass = toBlock.getType() == Material.STAINED_GLASS;

			if (crossedBlue || crossedRed || fromStainedGlass || toStainedGlass)
			{
				knockback(player);
			}
		}
	}

	@EventHandler
	public void onCustomDamage(CustomDamageEvent event)
	{
		if (!isChallengeValid())
			return;

		if (!event.GetDamageeEntity().equals(_pig))
			return;

		if (event.GetCause() == DamageCause.FALL)
			return;

		event.AddMult("Push", null, PIG_PUSH_DAMAGE, false);
		_pig.setHealth(_pig.getMaxHealth());
	}

	private void startMainTask()
	{
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (!isChallengeValid())
				{
					cancel();
					return;
				}

				increaseTeamScore();
				displayProgress();
				selectWinners();
			}
		}.runTaskTimer(Host.getArcadeManager().getPlugin(), 0L, 1L);
	}

	private void spawnPig()
	{
		Host.CreatureAllow = true;

		_pig = (Pig) getCenter().getWorld().spawn(
			getCenter().add(PIG_CENTER_X, PIG_CENTER_Y, PIG_CENTER_Z).subtract(getArenaSize(), 0, 0),
			Pig.class);

		UtilEnt.vegetate(_pig);

		Host.CreatureAllow = false;
	}

	private void equipTeamHelmets()
	{
		ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
		LeatherArmorMeta meta = (LeatherArmorMeta) helmet.getItemMeta();

		meta.setColor(Color.BLUE);
		helmet.setItemMeta(meta);

		for (Player player : getFirstTeam().getPlayers())
		{
			player.getInventory().setHelmet(helmet);
		}

		meta.setColor(Color.RED);
		helmet.setItemMeta(meta);

		for (Player player : getSecondTeam().getPlayers())
		{
			player.getInventory().setHelmet(helmet);
		}

	}

	private void equipKnockbackStick()
	{
		ItemStack stick = new ItemBuilder(Material.STICK)
			.addEnchantment(Enchantment.KNOCKBACK, 1)
			.addItemFlags(ItemFlag.HIDE_ENCHANTS)
			.build();

		setItem(Settings.getLockedSlot(), stick);
	}

	private void knockback(Player player)
	{
		UtilAction.velocity(
			player,
			UtilAlg.getTrajectory2d(player.getLocation(), getTeamCenter(player)),
			-UtilAlg.calculateVelocity(player.getLocation().toVector(), getTeamCenter(player).toVector(), 0).length() + 0.2,
			false,
			0,
			0,
			0,
			false);

		if (Recharge.Instance.use(player, "Knockback Message", KNOCKBACK_MESSAGE_COOLDOWN, false, false))
		{
			alert(player, C.cRed + "You cannot cross to the enemy side.");
		}
	}

	private Location getTeamCenter(Player player)
	{
		if (getFirstTeam().isMember(player))
		{
			return _blueCenter;
		}
		else
		{
			return _redCenter;
		}
	}

	private void increaseTeamScore()
	{
		if (_pig.isValid())
		{
			Location loc = _pig.getLocation();
			Block feetBlock = loc.getBlock().getRelative(BlockFace.DOWN);
			Location feet = feetBlock.getLocation();

			if (feetBlock.isEmpty()) // Retrieve the correct block if the pig is near a block edge.
			{
				double x = loc.getX();
				double z = loc.getZ();

				if ((x + MAX_BLOCK_SHIFT) >= Math.ceil(x))
				{
					feetBlock = feet.subtract(MAX_BLOCK_SHIFT, 0, 0).getBlock();
				}
				else if ((x - MAX_BLOCK_SHIFT) <= Math.floor(x))
				{
					feetBlock = feet.add(MAX_BLOCK_SHIFT, 0, 0).getBlock();
				}
				else if ((z + MAX_BLOCK_SHIFT) >= Math.ceil(z))
				{
					feetBlock = feet.subtract(0, 0, MAX_BLOCK_SHIFT).getBlock();
				}
				else if ((z - MAX_BLOCK_SHIFT) <= Math.floor(z))
				{
					feetBlock = feet.add(0, 0, MAX_BLOCK_SHIFT).getBlock();
				}
			}

			if (feetBlock.getType() == Material.STAINED_CLAY)
			{
				byte data = feetBlock.getData();

				if (data == BLUE_STAINED_GLASS)
				{
					_blueSide += SCORE_GAIN;
				}
				else
				{
					_redSide += SCORE_GAIN;
				}
			}
		}
	}

	private void selectWinners()
	{
		if (_pig.isValid())
		{
			if (_redSide > SCORE_GOAL)
			{
				for (Player bluePlayer : getFirstTeam().getPlayers())
				{
					setCompleted(bluePlayer);
				}
			}
			else if (_blueSide > SCORE_GOAL)
			{
				for (Player redPlayer : getSecondTeam().getPlayers())
				{
					setCompleted(redPlayer);
				}
			}
		}
	}

	private void displayProgress()
	{
		double red = _redSide / (double) SCORE_GOAL;
		double blue = _blueSide / (double) SCORE_GOAL;
		boolean redFirst = red < blue;
		String progressBar = (redFirst ? C.cRed : C.cBlue) + "";
		int colorChange = 0;

		for (int i = 0; i < BAR_AMOUNT; i++)
		{
			float d = (float) i / (float) BAR_AMOUNT;

			if (colorChange == 0 && d >= (redFirst ? red : blue))
			{
				progressBar += (redFirst ? C.cBlue : C.cRed);
				colorChange = 1;
			}

			if (colorChange != 2 && d >= Math.max(red, blue))
			{
				progressBar += C.cWhite;
				colorChange = 2;
			}

			progressBar += "▌";
		}

		UtilTextBottom.display(progressBar, UtilServer.getPlayers());
	}

}
