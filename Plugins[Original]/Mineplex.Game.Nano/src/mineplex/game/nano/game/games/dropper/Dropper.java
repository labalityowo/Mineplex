package mineplex.game.nano.game.games.dropper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.mineplex.anticheat.checks.move.Glide;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.GamePlacements;
import mineplex.game.nano.game.GameType;
import mineplex.game.nano.game.SoloGame;
import mineplex.game.nano.game.components.player.NightVisionComponent;
import mineplex.game.nano.game.event.GameStateChangeEvent;
import mineplex.game.nano.game.event.PlayerGameRespawnEvent;
import mineplex.minecraft.game.core.combat.DeathMessageType;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;

public class Dropper extends SoloGame
{

	private List<Location> _floor;
	private int _goalY, _goalDelay;

	private final List<Player> _completed, _delayed;

	public Dropper(NanoManager manager)
	{
		super(manager, GameType.DROPPER, new String[]
				{
						"Get to the " + C.cYellow + "Bottom" + C.Reset + "!",
						C.cYellow + "First player" + C.Reset + " down wins!"
				});

		_completed = new ArrayList<>();
		_delayed = new ArrayList<>();

		_prepareComponent.setPrepareFreeze(false);

		_teamComponent.setRespawnRechargeTime(0);

		_damageComponent.setPvp(false);

		_spectatorComponent.setDeathOut(false);

		_endComponent.setTimeout(TimeUnit.MINUTES.toMillis(1));

		new NightVisionComponent(this);

		_scoreboardComponent.setSidebar((player, scoreboard) ->
		{
			scoreboard.writeNewLine();

			List<Player> players = getAllPlayers();
			scoreboard.write(C.cYellowB + "Players");

			if (players.size() > 11)
			{
				scoreboard.write(players.size() + " Playing");
			}
			else
			{
				players.forEach(other -> scoreboard.write((other.equals(player) ? C.cGreen : (_completed.contains(other) ? C.cYellow : "")) + other.getName()));
			}

			scoreboard.writeNewLine();

			scoreboard.draw();
		});

		// Temporary fix to fix rubber banding
		_manager.getAntiHack().addIgnoredCheck(Glide.class);
	}

	@Override
	protected void parseData()
	{
		_mineplexWorld.getSpongeLocations().forEach((key, locations) ->
		{
			if (!key.startsWith("END"))
			{
				return;
			}

			_goalY = locations.get(0).getBlockY();
			_goalDelay = Integer.parseInt(key.split(" ")[1]);
		});

		_floor = _mineplexWorld.getSpongeLocations(String.valueOf(Material.GLASS.getId()));
		_floor.forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.STAINED_GLASS, (byte) 5));
	}

	@Override
	public boolean endGame()
	{
		return getAlivePlayers().isEmpty();
	}

	@Override
	public void disable()
	{

	}

	@EventHandler
	public void combatDeath(CombatDeathEvent event)
	{
		event.SetBroadcastType(DeathMessageType.None);
	}

	@EventHandler
	public void respawn(PlayerGameRespawnEvent event)
	{
		Player player = event.getPlayer();

		_delayed.remove(player);
		player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
	}

	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.getState() != GameState.Live)
		{
			return;
		}

		_floor.forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.AIR));
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (Player player : getAlivePlayers())
		{
			if (_delayed.contains(player) || player.getLocation().getY() > _goalY)
			{
				continue;
			}

			_delayed.add(player);

			getManager().runSyncLater(() ->
			{
				if (!isLive() || !isAlive(player))
				{
					return;
				}

				announce(F.main(getManager().getName(), F.name(player.getName()) + " completed the fall in " + F.time(UtilTime.MakeStr(System.currentTimeMillis() - getStateTime())) + "!"), null);
				UtilTextMiddle.display(C.cGreen + "End!", "You survived the drop!", 0, 50, 20, player);
				_completed.add(player);
				addSpectator(player, false, true);
			}, _goalDelay);
		}
	}

	@Override
	public GamePlacements getGamePlacements()
	{
		return GamePlacements.fromTeamPlacements(_completed);
	}
}
