package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;
import nautilus.game.arcade.game.games.mineware.challenge.LogicTracker;
import nautilus.game.arcade.game.games.mineware.challenge.TeamChallenge;

/**
 * A team based challenge based on tagging and untagging players.
 */
public class ChallengeReverseTag extends TeamChallenge implements LogicTracker
{
	private static final int CHALLENGE_DURATION_RANDOMIZER = 5;
	private static final int CHALLENGE_DURATION_MIN = 20;
	private static final int CHALLENGE_DURATION_MULTIPLIER = 1000;

	private static final int MAP_SIZE = 9;
	private static final int MAP_SPAWN_SHIFT = 1;
	private static final int MAP_HEIGHT = 1;
	private static final int SPAWN_COORDINATE_MULTIPLIER = 2;
	private static final int WOOL_DATA_RANGE = 16;

	private static final int PLAYER_COUNT_HIDE_FIREWORKS = 24;
	private static final int INVENTORY_HOTBAR_SLOTS = 8;
	private static final Material TAG_MATERIAL = Material.WOOL;
	private static final byte TAG_DATA = 5;
	private static final int UNTAG_COOLDOWN = 2; // seconds

	private Set<Player> _cooldowns = new HashSet<>();
	private Map<Player, Boolean> _tagTracker = new HashMap<>();

	public ChallengeReverseTag(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.FirstComplete,
			"Tagged",
			"Non Tagged",
			"Reverse Tag",
			"Punch a sparkling person to become one.",
			"Stay sparkling until the end.");

		Settings.setUseMapHeight();
		Settings.setTeamBased();
		Settings.setDuration((UtilMath.r(CHALLENGE_DURATION_RANDOMIZER) + CHALLENGE_DURATION_MIN) * CHALLENGE_DURATION_MULTIPLIER);
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();
		int size = getArenaSize(MAP_SIZE) - MAP_SPAWN_SHIFT;

		for (int x = -size; x <= size; x++)
		{
			for (int z = -size; z <= size; z++)
			{
				if (x % SPAWN_COORDINATE_MULTIPLIER == 0 && z % SPAWN_COORDINATE_MULTIPLIER == 0)
				{
					spawns.add(getCenter().add(x, MAP_HEIGHT, z));
				}
			}
		}

		return spawns;
	}

	@Override
	public void createMap()
	{
		int size = getArenaSize(MAP_SIZE);

		for (int x = -size; x <= size; x++)
		{
			for (int z = -size; z <= size; z++)
			{
				Block block = getCenter().getBlock().getRelative(x, 0, z);
				setBlock(block, Material.WOOL, (byte) UtilMath.r(WOOL_DATA_RANGE));
				addBlock(block);
			}
		}
	}

	@Override
	public void onStart()
	{
		Host.DamagePvP = true;

		autoSelectTeams();

		for (Player tagged : getFirstTeam().getPlayers())
		{
			tagEffect(tagged);
			_tagTracker.put(tagged, true);
		}
	}

	@Override
	public void onEnd()
	{
		Host.DamagePvP = false;

		_cooldowns.clear();
		_tagTracker.clear();
	}

	@Override
	public void onTimerFinish()
	{
		for (Player tagged : getFirstTeam().getPlayers())
		{
			setCompleted(tagged);
		}
	}

	@EventHandler
	public void onUpdateFireworks(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		if (!isChallengeValid())
			return;

		if (getPlayersAlive().size() > PLAYER_COUNT_HIDE_FIREWORKS)
			return;

		for (Player player : getFirstTeam().getPlayers())
		{
			UtilFirework.playFirework(player.getEyeLocation(), Type.BURST, Color.GREEN, false, false);
		}
	}

	@EventHandler
	public void onCustomDamage(CustomDamageEvent event)
	{
		if (!isChallengeValid())
			return;

		Player damager = event.GetDamagerPlayer(false);
		Player damagee = event.GetDamageePlayer();

		if (damager == null || damagee == null)
			return;

		if (!isPlayerValid(damager))
		{
			event.SetCancelled("Invalid Damager");
			return;
		}

		if (!isPlayerValid(damagee))
		{
			event.SetCancelled("Invalid Damagee");
			return;
		}

		if (getFirstTeam().isMember(damagee) && getSecondTeam().isMember(damager) && !_cooldowns.contains(damagee))
		{
			clear(damagee);
			tag(damager);
			event.SetCancelled("Successful Tag");
		}
		else
		{
			event.SetCancelled("Invalid Tag Attempt");
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		if (!isChallengeValid())
			return;

		Player player = event.getEntity();

		if (!isPlayerValid(player))
			return;

		_tagTracker.put(player, false);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if (!isChallengeValid())
			return;

		Player player = event.getPlayer();

		if (!isPlayerValid(player))
			return;

		_cooldowns.remove(player);
		_tagTracker.remove(player);
	}

	private void clear(Player player)
	{
		if (getFirstTeam().isMember(player))
		{
			trackClear(player);
			clearEffect(player);

			getFirstTeam().remove(player);
			getSecondTeam().add(player);
		}
	}

	private void trackClear(Player player)
	{
		if (_tagTracker.containsKey(player))
		{
			if (_tagTracker.get(player))
			{
				_tagTracker.put(player, false);
			}
		}
	}

	private void clearEffect(Player player)
	{
		alert(player, C.cRed + "You are no longer tagged.");
		player.getInventory().setHelmet(new ItemStack(Material.AIR));

		for (int i = 0; i <= INVENTORY_HOTBAR_SLOTS; i++)
		{
			player.getInventory().clear(i);
		}
	}

	private void tag(Player player)
	{
		if (getSecondTeam().isMember(player))
		{
			trackTag(player);
			tagEffect(player);

			getSecondTeam().remove(player);
			getFirstTeam().add(player);
			_cooldowns.add(player);
			removeCooldown(player);
		}
	}

	private void trackTag(Player player)
	{
		if (!_tagTracker.containsKey(player))
		{
			_tagTracker.put(player, true);
		}
	}

	private void tagEffect(Player player)
	{
		alert(player, C.cGreen + "You are now tagged, keep it up.");
		player.getInventory().setHelmet(ItemStackFactory.Instance.CreateStack(TAG_MATERIAL, TAG_DATA));

		for (int i = 0; i <= INVENTORY_HOTBAR_SLOTS; i++)
		{
			player.getInventory().setItem(i, ItemStackFactory.Instance.CreateStack(TAG_MATERIAL, TAG_DATA));
		}
	}

	private void removeCooldown(Player player)
	{
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				_cooldowns.remove(player);
			}
		}.runTaskLater(Host.Manager.getPlugin(), UNTAG_COOLDOWN * TICK_MULTIPLIER);
	}

	@Override
	public boolean hasData(Player player)
	{
		if (_tagTracker.containsKey(player))
		{
			return _tagTracker.get(player);
		}
		else
		{
			return false;
		}
	}
}
