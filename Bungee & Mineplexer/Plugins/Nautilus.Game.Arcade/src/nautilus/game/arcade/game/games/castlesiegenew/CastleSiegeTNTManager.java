package nautilus.game.arcade.game.games.castlesiegenew;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTime;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseLiving;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;

public class CastleSiegeTNTManager implements Listener
{

	private static final long SPAWN_TIME = TimeUnit.SECONDS.toMillis(25);
	private static final ItemStack TNT_HELMET = new ItemStack(Material.TNT);
	private static final int TOO_FAR_FROM_CASTLE_SQUARED = 256;
	private static final int TNT_WEAKNESS_DISTANCE_SQUARED = 16;
	private static final long TNT_MAX_TIME = TimeUnit.SECONDS.toMillis(30);
	private static final long REGENERATION_TIME = TimeUnit.SECONDS.toMillis(15);

	private final CastleSiegeNew _host;

	private final Map<Player, Long> _tntCarrier;
	private List<Location> _tntSpawns;
	private List<Location> _tntWeaknesses;
	private long _lastTNT;

	CastleSiegeTNTManager(CastleSiegeNew host)
	{
		_host = host;
		_tntCarrier = new HashMap<>();
	}

	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		_lastTNT = System.currentTimeMillis();
		_tntSpawns = _host.WorldData.GetDataLocs("RED");
		_tntWeaknesses = _host.WorldData.GetDataLocs("BLACK");
	}

	@EventHandler
	public void spawnTnt(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !_host.IsLive() || !UtilTime.elapsed(_lastTNT, SPAWN_TIME))
		{
			return;
		}

		_lastTNT = System.currentTimeMillis();

		Location location = UtilAlg.Random(_tntSpawns);

		if (location == null || location.getBlock().getType() == Material.TNT)
		{
			return;
		}

		MapUtil.QuickChangeBlockAt(location, Material.TNT);
		location.getWorld().playEffect(location.clone().add(0, 0.5, 0), Effect.STEP_SOUND, Material.TNT);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void tntPickup(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R_BLOCK))
		{
			return;
		}

		Player player = event.getPlayer();
		Block block = event.getClickedBlock();

		if (block == null || block.getType() != Material.TNT || UtilPlayer.isSpectator(player) || !_host.getUndead().HasPlayer(player) || _tntCarrier.containsKey(player))
		{
			return;
		}

		event.setCancelled(true);

		DisguiseBase disguise = _host.getArcadeManager().GetDisguise().getActiveDisguise(player);

		if (disguise != null && disguise instanceof DisguiseLiving)
		{
			DisguiseLiving disguiseLiving = (DisguiseLiving) disguise;
			player.getInventory().setHelmet(TNT_HELMET);
			disguiseLiving.setHelmet(TNT_HELMET);
			_host.getArcadeManager().GetDisguise().updateDisguise(disguiseLiving);
		}

		MapUtil.QuickChangeBlockAt(block.getLocation(), Material.AIR);

		player.sendMessage(F.main("Game", "You picked up " + F.skill("TNT") + "."));
		player.sendMessage(F.main("Game", F.elem("Click") + " to " + F.skill("Detonate") + " yourself."));
		_tntCarrier.put(player, System.currentTimeMillis());
	}

	@EventHandler
	public void tntDetonate(PlayerInteractEvent event)
	{
		if (event.isCancelled() || event.getAction() == Action.PHYSICAL)
		{
			return;
		}

		Player player = event.getPlayer();

		if (!_tntCarrier.containsKey(player))
		{
			return;
		}

		event.setCancelled(true);
		detonate(player, true);
	}

	@EventHandler
	public void updateExpire(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !_host.IsLive())
		{
			return;
		}

		for (Entry<Player, Long> entry : _tntCarrier.entrySet())
		{
			long timeLeft = entry.getValue() + TNT_MAX_TIME - System.currentTimeMillis();
			double percentage = (double) (System.currentTimeMillis() - entry.getValue()) / (double) TNT_MAX_TIME;

			if (timeLeft < 0)
			{
				detonate(entry.getKey(), false);
				continue;
			}

			UtilTextBottom.displayProgress(C.Bold + "TNT Detonation", percentage, UtilTime.MakeStr(timeLeft), entry.getKey());
		}
	}

	@EventHandler
	public void updateFireworks(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		for (Player player : _tntCarrier.keySet())
		{
			UtilFirework.playFirework(player.getEyeLocation(), Type.BURST, Color.RED, true, false);
		}
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		Player killed = event.getEntity();
		Player killer = killed.getKiller();

		if (killer != null && _tntCarrier.containsKey(killed))
		{
			_host.AddStat(killer, "TNTKiller", 1, false, false);
		}

		detonate(killed, false);
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		detonate(event.getPlayer(), false);
	}

	private void detonate(Player player, boolean triggered)
	{
		if (!_tntCarrier.containsKey(player) || !Recharge.Instance.use(player, "Prevent Double Detonation", 1000, false, false))
		{
			return;
		}

		Location playerLocation = player.getLocation();

		for (Location location : _tntSpawns)
		{
			if (UtilMath.offsetSquared(location, playerLocation) < TOO_FAR_FROM_CASTLE_SQUARED)
			{
				if (triggered)
				{
					player.sendMessage(F.main("Game", "You cannot " + F.skill("Detonate") + " so far from the Castle."));
				}
				else
				{
					_tntCarrier.remove(player);
					player.getInventory().setHelmet(null);
				}

				return;
			}
		}

		// Handle Weaknesses
		for (Location location : _tntWeaknesses)
		{
			if (UtilMath.offsetSquared(playerLocation, location) < TNT_WEAKNESS_DISTANCE_SQUARED)
			{
				for (int i = 0; i < 10; i++)
				{
					UtilServer.runSyncLater(() ->
					{

						TNTPrimed primed = player.getWorld().spawn(UtilAlg.getRandomLocation(location, 2, 2, 2), TNTPrimed.class);
						primed.setFuseTicks(0);
						primed.setIsIncendiary(true);

					}, i * 3);
				}
				break;
			}
		}

		_tntCarrier.remove(player);

		TNTPrimed primed = player.getWorld().spawn(player.getEyeLocation(), TNTPrimed.class);
		primed.setFuseTicks(0);

		player.sendMessage(F.main("Game", "You used " + F.skill("Detonate") + "."));
		_host.getArcadeManager().GetDamage().NewDamageEvent(player, null, null, DamageCause.BLOCK_EXPLOSION, 5000, false, true, true, player.getName(), "Explosion");
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void entityExplode(EntityExplodeEvent event)
	{
		BlockRestore restore = _host.getArcadeManager().GetBlockRestore();

		int lowestY = Integer.MAX_VALUE;

		for (Block block : event.blockList())
		{
			int y = block.getLocation().getBlockY();

			if (y < lowestY)
			{
				lowestY = y;
			}
		}

		for (Block block : event.blockList())
		{
			Material material = block.getType();
			byte materialData = block.getData();

			if (
					material == Material.SMOOTH_BRICK && materialData == 2 ||
					material == Material.IRON_FENCE ||
					material == Material.FENCE
					)
			{
				continue;
			}

			restore.add(block, Material.AIR.getId(), (byte) 0, (long) (REGENERATION_TIME + (block.getLocation().getBlockY() - lowestY) * 2000 + Math.random() * 1750));
		}
	}

	@EventHandler
	public void tntClick(InventoryClickEvent event)
	{
		Inventory inventory = event.getClickedInventory();
		ItemStack itemStack = event.getCurrentItem();

		if (inventory != null && itemStack != null && itemStack.getType() == Material.TNT)
		{
			event.setCancelled(true);
		}
	}
}
