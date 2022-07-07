package mineplex.game.nano.game.games.redgreenlight;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import mineplex.core.common.util.C;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.GamePlacements;
import mineplex.game.nano.game.GameType;
import mineplex.game.nano.game.SoloGame;
import mineplex.game.nano.game.event.GameStateChangeEvent;
import mineplex.game.nano.game.event.PlayerStateChangeEvent;

public class RedGreenLight extends SoloGame
{

	enum LightState
	{

		GREEN("GO", ChatColor.GREEN, (byte) 5, Sound.FIREWORK_LAUNCH),
		YELLOW("SLOW", ChatColor.YELLOW, (byte) 4, Sound.NOTE_STICKS),
		RED("STOP", ChatColor.RED, (byte) 14, Sound.NOTE_BASS_DRUM);

		final String Title;
		final byte BlockData;
		final ItemStack WoolItem;
		final Sound SoundToPlay;

		LightState(String name, ChatColor colour, byte blockData, Sound soundToPlay)
		{
			Title = colour + C.Bold + name;
			BlockData = blockData;
			WoolItem = new ItemBuilder(Material.WOOL, blockData)
					.setTitle(Title)
					.build();
			SoundToPlay = soundToPlay;
		}
	}

	private final List<Player> _order;

	private List<Location> _wall, _lights;
	private Location _villager;
	private int _zGoal;

	private boolean _scheduled;
	private LightState _state;

	public RedGreenLight(NanoManager manager)
	{
		super(manager, GameType.RED_GREEN_LIGHT, new String[]
				{
						"Two Rules",
						C.cGreenB + "Run" + C.Reset + " on " + C.cGreenB + "Green",
						C.cRedB + "STOP" + C.Reset + " on " + C.cRedB + "RED",
						C.cYellow + "First Player" + C.Reset + " to the villager wins!"
				});

		_order = new ArrayList<>();

		_teamComponent.setAdjustSpawnYaw(false);

		_prepareComponent.setPrepareFreeze(false);

		_damageComponent.setDamage(false);

		_endComponent.setTimeout(TimeUnit.MINUTES.toMillis(3));

		_scoreboardComponent.setSidebar((player, scoreboard) ->
		{
			scoreboard.writeNewLine();

			scoreboard.write(C.cYellowB + "Players");

			for (Player other : _order.subList(0, Math.min(12, _order.size())))
			{
				scoreboard.write((other.equals(player) ? C.cGreen : "") + other.getName());
			}

			scoreboard.writeNewLine();

			scoreboard.draw();
		});
	}

	@Override
	protected void parseData()
	{
		_wall = _mineplexWorld.getSpongeLocations(String.valueOf(Material.EMERALD_ORE.getId()));
		_lights = _mineplexWorld.getSpongeLocations(String.valueOf(Material.DIAMOND_ORE.getId()));
		_villager = _mineplexWorld.getIronLocation("LIME");
		_zGoal = _villager.getBlockZ() - 3;

		_wall.forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.STAINED_GLASS_PANE, (byte) 15));
		setLights(LightState.GREEN);
	}

	@Override
	public boolean endGame()
	{
		if (super.endGame() || _order.isEmpty())
		{
			return true;
		}

		_order.sort((o1, o2) -> Double.compare(o2.getLocation().getZ(), o1.getLocation().getZ()));

		if (!_order.isEmpty())
		{
			Player top = _order.get(0);

			return top.getLocation().getZ() > _zGoal;
		}

		return false;
	}

	@Override
	protected GamePlacements createPlacements()
	{
		return GamePlacements.fromTeamPlacements(_order);
	}

	@Override
	public void disable()
	{
		_wall.clear();
		_lights.clear();
	}

	@EventHandler
	public void prepare(GameStateChangeEvent event)
	{
		if (event.getState() != GameState.Prepare)
		{
			return;
		}

		_order.addAll(getAlivePlayers());
	}

	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.getState() != GameState.Live)
		{
			return;
		}

		_wall.forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.AIR));

		_worldComponent.setCreatureAllowOverride(true);

		_villager.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(_villager, getSpectatorLocation())));

		Villager villager = _villager.getWorld().spawn(_villager, Villager.class);

		UtilEnt.vegetate(villager, true);
		UtilEnt.ghost(villager, true, false);
		UtilEnt.setFakeHead(villager, true);

		villager.setCustomName(C.cGreen + "Danger Dan");
		villager.setCustomNameVisible(true);

		_worldComponent.setCreatureAllowOverride(false);
	}

	@EventHandler
	public void updateLights(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER || !isLive())
		{
			return;
		}

		if (endGame())
		{
			setState(GameState.End);
		}

		if (_scheduled)
		{
			return;
		}

		_scheduled = true;

		// 2 - 4 seconds
		int ticksToRun = UtilMath.rRange(40, 80);
		// 1 - 1.5 seconds
		int ticksToSlow = UtilMath.rRange(20, 30);
		// 1 - 2 seconds
		int ticksToStop = UtilMath.rRange(20, 40);

		setLights(LightState.GREEN);

		getManager().runSyncLater(() ->
		{
			setLights(LightState.YELLOW);

			getManager().runSyncLater(() ->
			{
				setLights(LightState.RED);

				getManager().runSyncLater(() -> _scheduled = false, ticksToStop);
			}, ticksToSlow);
		}, ticksToRun);
	}

	@EventHandler
	public void playerMove(PlayerMoveEvent event)
	{
		if (!isLive() || _state != LightState.RED)
		{
			return;
		}

		Player player = event.getPlayer();

		if (UtilPlayer.isSpectator(player))
		{
			return;
		}

		Location from = event.getFrom(), to = event.getTo();

		if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ() || !Recharge.Instance.use(player, "Caught", 1000, false, false))
		{
			return;
		}

		player.teleport(_playersTeam.getSpawn());
		player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
		UtilTextMiddle.display(null, C.cRed + "You Moved!", 0, 30, 10, player);
	}

	private void setLights(LightState state)
	{
		if (state == LightState.RED)
		{
			getManager().runSyncLater(() -> _state = state, 7);
		}
		else
		{
			_state = state;
		}

		_lights.forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.WOOL, state.BlockData));

		if (!isLive())
		{
			return;
		}

		Player[] players = getAlivePlayers().toArray(new Player[0]);

		for (Player player : players)
		{
			PlayerInventory inventory = player.getInventory();

			for (int i = 0; i < 9; i++)
			{
				inventory.setItem(i, state.WoolItem);
			}

			player.playSound(player.getLocation(), state.SoundToPlay, 1, 0.8F);
		}

		UtilTextMiddle.display(null, state.Title, 5, 10, 5, players);
	}

	@EventHandler
	public void playerOut(PlayerStateChangeEvent event)
	{
		if (!event.isAlive())
		{
			_order.remove(event.getPlayer());
		}
	}

	@EventHandler
	public void playerInteractEntity(PlayerInteractEntityEvent event)
	{
		if (event.getRightClicked() instanceof Villager)
		{
			event.setCancelled(true);
		}
	}
}
