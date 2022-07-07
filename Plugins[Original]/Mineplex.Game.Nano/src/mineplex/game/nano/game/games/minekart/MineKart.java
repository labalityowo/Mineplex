package mineplex.game.nano.game.games.minekart;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import net.minecraft.server.v1_8_R3.PacketPlayInSteerVehicle;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.noteblock.LoopedNotePlayer;
import mineplex.core.noteblock.NBSReader;
import mineplex.core.noteblock.NoteSong;
import mineplex.core.packethandler.IPacketHandler;
import mineplex.core.packethandler.PacketInfo;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.GamePlacements;
import mineplex.game.nano.game.GameType;
import mineplex.game.nano.game.SoloGame;
import mineplex.game.nano.game.event.PlayerGameApplyEvent;
import mineplex.game.nano.game.event.PlayerGameRespawnEvent;
import mineplex.game.nano.game.event.PlayerStateChangeEvent;
import mineplex.game.nano.game.games.minekart.KartController.DriftDirection;

public class MineKart extends SoloGame implements IPacketHandler
{

	private static final int LAPS = 3;
	private static final long RESET_KART_COOLDOWN = TimeUnit.SECONDS.toMillis(10);

	private int _spawnIndex;

	private final List<KartCheckpoint> _checkpoints, _keyCheckpoints;
	private Location _faceLocation;

	private final Map<Player, Kart> _karts;
	private final List<Kart> _positions;

	private final Comparator<Kart> _positionSorter = (o1, o2) ->
	{
		boolean o1Complete = o1.getCompletedAt() != 0, o2Complete = o2.getCompletedAt() != 0;

		if (o1Complete && o2Complete)
		{
			return Long.compare(o1.getCompletedAt(), o2.getCompletedAt());
		}
		else if (o1Complete)
		{
			return -1;
		}
		else if (o2Complete)
		{
			return 1;
		}

		if (o1.getLap() != o2.getLap())
		{
			return Integer.compare(o2.getLap(), o1.getLap());
		}

		if (o1.getLapKeyCheckpoint() != o2.getLapKeyCheckpoint())
		{
			return Integer.compare(o2.getLapKeyCheckpoint(), o1.getLapKeyCheckpoint());
		}

		return Integer.compare(o2.getLapCheckpoint(), o1.getLapCheckpoint());
	};

	private final ItemStack _resetItem = new ItemBuilder(Material.BARRIER)
			.setTitle(C.cRedB + "Reset Kart")
			.addLore("Click to reset your kart", "back on the track.")
			.build();

	private final LoopedNotePlayer _notePlayer;

	public MineKart(NanoManager manager)
	{
		super(manager, GameType.MINEKART, new String[]
				{
						"Control your Kart like a " + C.cYellow + "Boat" + C.Reset + "!",
						C.cGreen + "Hold Jump" + C.Reset + " to charge a " + C.cYellow + "Drift" + C.Reset + ".",
						C.cYellow + "Release Jump" + C.Reset + " to discharge your " + C.cYellow + "Drift" + C.Reset + ".",
						C.cYellow + "3 Laps" + C.Reset + " to win!"
				});

		_checkpoints = new ArrayList<>();
		_keyCheckpoints = new ArrayList<>();
		_karts = new HashMap<>();
		_positions = new ArrayList<>();

		NoteSong song;

		try
		{
			song = NBSReader.loadSong(".." + File.separator + ".." + File.separator + "update" + File.separator + "songs" + File.separator + "minekart.nbs");
		}
		catch (FileNotFoundException e)
		{
			song = null;
			e.printStackTrace();
		}

		if (song != null)
		{
			_notePlayer = new LoopedNotePlayer(getLifetime(), song, player ->
			{
				Kart kart = _karts.get(player);

				return kart != null && kart.getLap() < LAPS;
			});
			getLifetime().register(_notePlayer, Collections.singleton(GameState.Live));
		}
		else
		{
			_notePlayer = null;
		}

		_teamComponent.setAdjustSpawnYaw(false);

		_damageComponent.setDamage(false);

		_playerComponent.setHideParticles(true);

		manager.getPacketHandler().addPacketHandler(this, PacketPlayInSteerVehicle.class);

		_scoreboardComponent.setSidebar((player, scoreboard) ->
		{
			scoreboard.writeNewLine();

			scoreboard.write(C.cYellowB + "Racers");

			if (getState() == GameState.Prepare || _positions.isEmpty())
			{
				scoreboard.write(getAllPlayers().size() + " Players");
			}
			else
			{
				scoreboard.writeNewLine();

				boolean hasShownPlayer = false;

				for (int i = 0; i < Math.min(_positions.size(), hasShownPlayer ? 11 : 9); i++)
				{
					Kart kart = _positions.get(i);
					Player other = kart.getDriver();

					if (player.equals(other))
					{
						hasShownPlayer = true;
					}

					scoreboard.write(kart.getLap() + " " + (player.equals(other) ? C.cGreen : (UtilPlayer.isSpectator(other) ? C.cGray + C.Strike : C.cYellow)) + other.getName());
				}

				if (!hasShownPlayer)
				{
					Kart kart = null;

					for (Kart other : _positions)
					{
						if (player.equals(other.getDriver()))
						{
							kart = other;
							break;
						}
					}

					if (kart != null)
					{
						scoreboard.writeNewLine();

						scoreboard.write(kart.getLap() + " " + C.cGreen + player.getName());
					}
				}
			}

			scoreboard.writeNewLine();

			scoreboard.draw();
		});
	}

	@Override
	protected void parseData()
	{
		_mineplexWorld.getSpongeLocations().forEach((key, locations) ->
		{
			if (!key.startsWith("CP") || locations.size() < 2)
			{
				return;
			}

			String[] args = key.split(" ");
			int index;

			try
			{
				index = Integer.parseInt(args[1]);
			}
			catch (NumberFormatException ex)
			{
				ex.printStackTrace();
				return;
			}

			Location a = locations.get(0), b = locations.get(1);

			if (a.getY() == b.getY())
			{
				a.subtract(0, 1, 0);
				b.add(0, 1, 0);
			}

			KartCheckpoint checkpoint = new KartCheckpoint(a, b, index, args.length > 2);

			_checkpoints.add(checkpoint);

			if (checkpoint.isKey())
			{
				_keyCheckpoints.add(checkpoint);
			}
		});

		_faceLocation = getCheckpoint(0).getCenter();

		_playersTeam.getSpawns().sort(Comparator.comparingDouble(o -> UtilMath.offsetSquared(o, _faceLocation)));
		_keyCheckpoints.sort(Comparator.comparingInt(KartCheckpoint::getIndex));
	}

	@Override
	public boolean endGame()
	{
		return getAlivePlayers().isEmpty();
	}

	@Override
	public void disable()
	{
		_checkpoints.clear();
		_manager.getPacketHandler().removePacketHandler(this);
	}

	@Override
	public void handle(PacketInfo packetInfo)
	{
		Kart kart = _karts.get(packetInfo.getPlayer());

		if (kart != null)
		{
			PacketPlayInSteerVehicle packet = (PacketPlayInSteerVehicle) packetInfo.getPacket();

			// Dismounting
			if (packet.d())
			{
				packetInfo.setCancelled(true);
				return;
			}

			// a() - Right-Left, b() - Backwards-Forwards
			kart.setInput(packet.b(), packet.a());

			// Holding Space
			if (packet.c())
			{
				if (packet.a() != 0 && kart.getDriftDirection() == null)
				{
					kart.setDriftDirection(packet.a() > 0 ? DriftDirection.LEFT : DriftDirection.RIGHT);
				}
			}
			else
			{
				kart.setDriftDirection(null);
			}
		}
	}

	@EventHandler
	public void playerApply(PlayerGameApplyEvent event)
	{
		Location location = _playersTeam.getSpawns().get(_spawnIndex);
		location.setYaw((Math.round(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, _faceLocation)) / 90F) & 0x3) * 90F);
		event.setRespawnLocation(location);

		_spawnIndex = (_spawnIndex + 1) % _playersTeam.getSpawns().size();
	}

	@EventHandler
	public void playerRespawn(PlayerGameRespawnEvent event)
	{
		Player player = event.getPlayer();

		_manager.runSyncLater(() ->
		{
			_worldComponent.setCreatureAllowOverride(true);

			Kart kart = new Kart(player);
			_karts.put(player, kart);
			_positions.add(kart);

			player.getInventory().setItem(8, _resetItem);

			_worldComponent.setCreatureAllowOverride(false);
		}, 1);
	}

	@EventHandler
	public void updateKartControl(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !isLive())
		{
			return;
		}

		_karts.forEach((player, kart) ->
		{
			LivingEntity vehicle = kart.getVehicle();

			if (!vehicle.isValid())
			{
				return;
			}

			if (vehicle.getPassenger() == null || !vehicle.getPassenger().equals(player))
			{
				vehicle.setPassenger(player);
			}

			if (kart.isResetting())
			{
				return;
			}

			Location location = vehicle.getLocation();
			KartCheckpoint checkpoint = getCheckpoint(location);

			if (checkpoint != null)
			{
				kart.setLapCheckpoint(checkpoint.getIndex());

				if (checkpoint.isKey())
				{
					int keyIndex = _keyCheckpoints.indexOf(checkpoint);

					if (kart.getLapKeyCheckpoint() + 1 == keyIndex)
					{
						kart.setLapKeyCheckpoint(keyIndex);
					}
				}

				if (checkpoint.getIndex() == 0 && kart.getLapKeyCheckpoint() == _keyCheckpoints.size() - 1)
				{
					int lap = kart.getLap() + 1;

					kart.setLapKeyCheckpoint(0);

					if (lap == LAPS + 1)
					{
						kart.complete();
						UtilFirework.launchFirework(location, FireworkEffect.builder()
								.with(Type.BALL_LARGE)
								.withColor(Color.YELLOW)
								.build(), null, 1);
						announce(F.main(getManager().getName(), F.name(player.getName()) + " completed the race in " + F.time(UtilTime.MakeStr(kart.getCompletedAt() - getStateTime())) + "!"), Sound.FIREWORK_BLAST);
						addSpectator(player, false, true);
					}
					else
					{
						kart.setLap(lap);

						boolean finalLap = lap == LAPS;

						if (finalLap && _notePlayer != null)
						{
							_notePlayer.cloneForPlayer(player, 0.75F);
						}

						UtilTextMiddle.display(C.cYellowB + "Lap " + lap, finalLap ? C.cAquaB + "FINAL LAP" : "", 10, 40, 10, player);
						player.sendMessage(F.main(getManager().getName(), "Lap " + F.count(lap) + (finalLap ? F.color(" FINAL LAP", C.cAquaB) : "") + "!"));
					}
				}
			}

			boolean canControl = true;

			if (kart.isCrashed())
			{
				if (UtilTime.elapsed(kart.getCrashedAt(), 1000) && UtilEnt.isGrounded(kart.getVehicle()))
				{
					kart.setCrashed(false);
				}
				else
				{
					canControl = false;
				}
			}

			KartController.applyAirResistance(kart);

			if (canControl)
			{
				KartController.accelerate(kart);
				KartController.brake(kart);
				KartController.drift(kart);
				KartController.turn(kart);
				KartController.applyTopSpeed(kart, _positions.indexOf(kart));
			}

			KartController.collideBlock(kart);

			if (vehicle.getFallDistance() > 20 || KartController.collideOutOfBounds(kart, _mineplexWorld))
			{
				resetKart(kart);
				return;
			}

			double velocityLength = kart.getVelocity().length();

			location.add(kart.getVelocity()).add(kart.getOffset());
			location.setDirection(kart.getVelocity());
			UtilEnt.setPosition(vehicle, location);
			UtilEnt.CreatureLook(vehicle, kart.getYaw());

			kart.getOffset().multiply(0);

			location.getWorld().playSound(location, Sound.PIG_IDLE, (float) (.1 + velocityLength / 2), (float) (.5 + velocityLength));
			player.setLevel((int) (velocityLength * 100));
			player.setExp(kart.getDriftPower());
		});

		_positions.sort(_positionSorter);
	}

	private void resetKart(Kart kart)
	{
		if (kart == null || kart.isResetting())
		{
			return;
		}

		kart.setResetting(true);

		UtilTextMiddle.display(C.cRedB + "Resetting!", null, 0, 50, 10, kart.getDriver());

		LivingEntity vehicle = kart.getVehicle();
		Location location = vehicle.getLocation();
		List<Location> points = UtilShapes.getLinesDistancedPoints(location, getCheckpoint(kart.getLapCheckpoint()).getCenter().add(0, 3, 0), 0.5);

		getManager().runSyncTimer(new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (points.isEmpty())
				{
					kart.setVelocity(new Vector());
					kart.getVehicle().setFallDistance(0);
					kart.setResetting(false);
					cancel();
					return;
				}

				Location location = points.remove(0);
				location.setYaw(location.getYaw());
				UtilEnt.setPosition(vehicle, location);
				UtilParticle.PlayParticleToAll(ParticleType.CLOUD, location, 0.3F, 0.3F, 0.3F, 0, 5, ViewDist.NORMAL);
			}
		}, 0, 1);
	}

	@EventHandler
	public void playerInteract(PlayerInteractEvent event)
	{
		if (event.getAction() == Action.PHYSICAL || !isLive())
		{
			return;
		}

		Player player = event.getPlayer();

		if (!_resetItem.equals(player.getItemInHand()) || !Recharge.Instance.use(player, "Reset Kart", RESET_KART_COOLDOWN, true, true))
		{
			return;
		}

		resetKart(_karts.get(player));
	}

	@EventHandler
	public void playerOut(PlayerStateChangeEvent event)
	{
		if (!event.isAlive())
		{
			Kart kart = _karts.get(event.getPlayer());

			if (kart != null)
			{
				kart.remove();
			}
		}
	}

	@Override
	public GamePlacements getGamePlacements()
	{
		return GamePlacements.fromTeamPlacements(_positions.stream()
				.map(Kart::getDriver)
				.collect(Collectors.toList()));
	}

	public KartCheckpoint getCheckpoint(int index)
	{
		return _checkpoints.stream()
				.filter(checkpoint -> checkpoint.getIndex() == index)
				.findFirst()
				.orElse(null);
	}

	public KartCheckpoint getCheckpoint(Location location)
	{
		return _checkpoints.stream()
				.filter(checkpoint -> checkpoint.isInBox(location))
				.findFirst()
				.orElse(null);
	}

	@EventHandler
	public void playerChat(AsyncPlayerChatEvent event)
	{
		Player player = event.getPlayer();

		if (!player.isOp())
		{
			return;
		}

		Kart kart = _karts.get(player);

		if (kart == null)
		{
			return;
		}

		try
		{
			int index = Integer.parseInt(event.getMessage());
			player.leaveVehicle();

			UtilEnt.setPosition(kart.getVehicle(), getCheckpoint(index).getCenter());
			player.sendMessage("Moving kart to " + index);
		}
		catch (NumberFormatException ex)
		{

		}
	}
}
