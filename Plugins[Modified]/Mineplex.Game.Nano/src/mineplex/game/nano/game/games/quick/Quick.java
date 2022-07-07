package mineplex.game.nano.game.games.quick;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.command.CommandCenter;
import mineplex.core.common.block.schematic.Schematic;
import mineplex.core.common.block.schematic.UtilSchematic;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.NanoPlayer;
import mineplex.game.nano.game.GameType;
import mineplex.game.nano.game.ScoredSoloGame;
import mineplex.game.nano.game.components.player.NightVisionComponent;
import mineplex.game.nano.game.event.PlayerDeathOutEvent;
import mineplex.minecraft.game.core.combat.DeathMessageType;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;

public class Quick extends ScoredSoloGame
{

	public enum Perm implements Permission
	{
		CHALLENGE_SET_COMMAND
	}

	private static final int CHALLENGES = 8;

	private final List<ChallengeType> _challengeTypes;
	private final ChallengeSetCommand _command;

	private Location _center;
	private Location _winnersLocation, _losersLocation;
	private Location _arenaPaste;
	private Schematic _arena;
	private int _arenaSize;

	private Challenge _challenge;
	private boolean _creating, _delay;

	public Quick(NanoManager manager)
	{
		super(manager, GameType.QUICK, new String[]
				{
						"You will be given " + C.cYellow + "Challenges" + C.Reset + ".",
						"Complete them as " + C.cGreen + "Quickly" + C.Reset + " as you can.",
						C.cYellow + "Most challenges completed" + C.Reset + " wins!"
				});

		PermissionGroup.ADMIN.setPermission(Perm.CHALLENGE_SET_COMMAND, true, true);

		_challengeTypes = new ArrayList<>(Arrays.asList(ChallengeType.values()));
		_command = new ChallengeSetCommand(manager);
		CommandCenter.Instance.addCommand(_command);

		_prepareComponent.setPrepareFreeze(false);

		_playerComponent
				.setHunger(true)
				.setItemMovement(true);

		_worldComponent.setBlockInteract(true);

		_endComponent.setTimeout(-1);

		new NightVisionComponent(this);

		while (_challengeTypes.size() > CHALLENGES)
		{
			_challengeTypes.remove(UtilMath.r(_challengeTypes.size()));
		}
	}

	@Override
	public void disable()
	{
		super.disable();

		if (_challenge != null && _challenge.getLifetime().isActive())
		{
			_challenge.end();
		}

		CommandCenter.Instance.removeCommand(_command);
	}

	@Override
	protected void parseData()
	{
		_center = _mineplexWorld.getSpongeLocation("LOOK_AT");

		_winnersLocation = _mineplexWorld.getGoldLocation("Lime");
		_losersLocation = _mineplexWorld.getGoldLocation("Red");

		_winnersLocation.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(_winnersLocation, getSpectatorLocation())));
		_losersLocation.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(_losersLocation, getSpectatorLocation())));

		List<Location> corners = getRedPoints();
		_arenaPaste = corners.get(0);
		_arena = UtilSchematic.createSchematic(corners.get(0), corners.get(1));
		_arenaSize = _center.getBlockX() - _arenaPaste.getBlockX();

		getYellowSpawns().forEach(location -> location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, getSpectatorLocation()))));
	}

	@EventHandler
	public void updateChallenge(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !isLive() || _creating || (_challenge != null && _challenge.getLifetime().isActive()))
		{
			return;
		}

		if (_challengeTypes.isEmpty())
		{
			setState(GameState.End);
			return;
		}

		int index = UtilMath.r(_challengeTypes.size());
		ChallengeType challengeType = _challengeTypes.get(index);
		_challengeTypes.remove(index);

		if (challengeType == null)
		{
			setState(GameState.End);
			return;
		}

		_arena.paste(_arenaPaste, false);

		try
		{
			_challenge = challengeType.getChallengeClass().getConstructor(Quick.class).newInstance(this);
		}
		catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e)
		{
			e.printStackTrace();
			setState(GameState.End);
		}

		_damageComponent.setPvp(false);
		_creating = true;

		if (_delay)
		{
			getManager().runSyncTimer(new BukkitRunnable()
			{
				int note = 0;

				@Override
				public void run()
				{
					float pitch = 0;

					switch (note)
					{
						case 0:
							pitch = 1.059F;
							break;
						case 1:
							pitch = 1;
							break;
						case 2:
							pitch = 0.840F;
							break;
						case 3:
							pitch = 0.594F;
							break;
						case 4:
							pitch = 0.561F;
							break;
						case 5:
							pitch = 0.890F;
							break;
						case 6:
							pitch = 1.12F;
							break;
						case 7:
							pitch = 1.414F;
							break;
					}

					for (Player player : getAllPlayers())
					{
						player.getWorld().playSound(player.getLocation(), Sound.NOTE_PIANO, 1, pitch);
					}

					if (++note == 8)
					{
						cancel();
						getManager().runSyncLater(() -> start(), 20);
					}
				}
			}, 0, 4);
		}
		else
		{
			_delay = true;
			start();
		}
	}

	private void start()
	{
		_challenge.start();
		_damageComponent.setPvp(_challenge.isPvp());
		_creating = false;
	}

	public void onOut(Player player, boolean winner)
	{
		NanoPlayer.clear(getManager(), player);
		NanoPlayer.setSpectating(player, true);
		player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));

		if (winner)
		{
			player.teleport(_winnersLocation);
		}
		else
		{
			player.teleport(_losersLocation);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onOut(PlayerDeathOutEvent event)
	{
		event.setCancelled(true);
		event.setShouldRespawn(true);
	}

	@EventHandler
	public void combatDeath(CombatDeathEvent event)
	{
		event.SetBroadcastType(DeathMessageType.Simple);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();

		if (!_manager.isSpectator(player))
		{
			joinTeam(player, _playersTeam);
		}
	}

	public Location getCenter()
	{
		return _center;
	}

	public List<Location> getRedPoints()
	{
		return _mineplexWorld.getIronLocations("RED");
	}

	public List<Location> getGreenPoints()
	{
		return _mineplexWorld.getIronLocations("GREEN");
	}

	public List<Location> getOrangePoints()
	{
		return _mineplexWorld.getIronLocations("ORANGE");
	}

	public List<Location> getYellowPoints()
	{
		return _mineplexWorld.getIronLocations("YELLOW");
	}

	public List<Location> getYellowSpawns()
	{
		return _mineplexWorld.getGoldLocations("Yellow");
	}

	public int getArenaSize()
	{
		return _arenaSize;
	}

	public void setChallenges(List<ChallengeType> challenges)
	{
		_challengeTypes.clear();
		_challengeTypes.addAll(challenges);
	}
}
