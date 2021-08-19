package nautilus.game.arcade.game.games.christmas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.PacketPlayOutAttachEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHorse;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.packethandler.IPacketHandler;
import mineplex.core.packethandler.PacketInfo;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.SoloGame;
import nautilus.game.arcade.game.games.christmasnew.ChristmasNewAudio;
import nautilus.game.arcade.game.games.christmasnew.section.Section;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;

public class ChristmasCommon extends SoloGame
{

	public enum Perm implements Permission
	{
		DEBUG_COMMANDS
	}

	private static final int BARRIER_BREAK_SQUARED = 1000;
	private static final int MIN_Y = 40;

	protected final List<Section> _sections;
	private Section _currentSection;

	private boolean _forceSkip;
	private boolean _forceSkipTeleport;

	private GameTeam _badGuys;

	private List<Location> _barrier;

	private Sleigh _sleigh;
	protected Location _sleighSpawn;
	private final IPacketHandler _reindeerPackets = new IPacketHandler()
	{

		@Override
		public void handle(PacketInfo packetInfo)
		{
			if (_sleigh == null)
			{
				return;
			}

			if (packetInfo.getPacket() instanceof PacketPlayOutSpawnEntityLiving)
			{
				PacketPlayOutSpawnEntityLiving spawnPacket = (PacketPlayOutSpawnEntityLiving) packetInfo.getPacket();

				for (SleighHorse horse : _sleigh.getHorses())
				{
					if (horse.horseId == spawnPacket.a)
					{
						horse.spawnHorns(packetInfo.getPlayer());
						break;
					}
				}
			}
			else if (packetInfo.getPacket() instanceof PacketPlayOutEntityDestroy)
			{
				try
				{
					PacketPlayOutEntityDestroy destroyPacket = (PacketPlayOutEntityDestroy) packetInfo.getPacket();
					int[] entityIds = destroyPacket.a;
					int origLength = entityIds.length;
					for (int a = 0; a < entityIds.length; a++)
					{
						for (SleighHorse horse : _sleigh.getHorses())
						{
							if (horse.horseId == a)
							{
								int p = entityIds.length;
								entityIds = Arrays.copyOf(entityIds, entityIds.length + 3);
								System.arraycopy(horse.hornsAndNose, 0, entityIds, p, 3);
								break;
							}
						}
					}
					if (entityIds.length != origLength)
					{
						destroyPacket.a = entityIds;
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
	};

	public ChristmasCommon(ArcadeManager manager, GameType gameType, Kit[] kits, String[] gameDesc)
	{
		super(manager, gameType, kits, gameDesc);

		_sections = new ArrayList<>(6);

		DeathOut = false;
		DeathTeleport = false;
		HungerSet = 20;
		PrepareFreeze = false;
		InventoryClick = true;
		WorldTimeSet = 4000;
		FixSpawnFacing = false;
		GameTimeout = TimeUnit.MINUTES.toMillis(30);

		manager.GetCreature().SetDisableCustomDrops(true);

		registerChatStats(
				DamageDealt,
				DamageTaken
		);

		new CompassModule()
				.register(this);

		registerDebugCommand("skip", Perm.DEBUG_COMMANDS, PermissionGroup.ADMIN, (player, args) ->
		{
			if (args.length > 0)
			{
				_forceSkipTeleport = true;
			}

			_forceSkip = true;
			player.sendMessage(F.main("Debug", "Skipping current challenge." + (_forceSkipTeleport ? " Teleporting..." : "")));
		});
		registerDebugCommand("forceend", Perm.DEBUG_COMMANDS, PermissionGroup.ADMIN, (player, args) ->
		{
			player.sendMessage(F.main("Debug", "Force ending the game."));
			endGame(true, "You saved Christmas!");
		});
	}

	@Override
	public void ParseData()
	{
		_barrier = WorldData.GetCustomLocs(String.valueOf(Material.EMERALD_ORE.getId()));
		_barrier.forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.BARRIER));

		_sleighSpawn = WorldData.GetDataLocs("RED").get(0);
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		Scoreboard.writeNewLine();

		Scoreboard.write(C.cYellowB + "Players");
		List<Player> alive = GetPlayers(true);

		if (alive.size() > 6)
		{
			Scoreboard.write(alive.size() + " Alive");
		}
		else
		{
			alive.forEach(player -> Scoreboard.write(player.getName()));
		}

		if (IsLive() && _currentSection != null)
		{
			Scoreboard.writeNewLine();

			Scoreboard.write(C.cYellowB + "Objective");
			Scoreboard.write(_currentSection.getObjectiveText());

			Scoreboard.writeNewLine();

			Scoreboard.write(C.cYellowB + "Time Left");
			Scoreboard.write(UtilTime.MakeStr(GetStateTime() + GameTimeout - System.currentTimeMillis()));
		}

		Scoreboard.writeNewLine();

		Scoreboard.draw();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void generateTeams(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		for (Player player : GetPlayers(true))
		{
			player.setGameMode(GameMode.ADVENTURE);
		}

		_badGuys = new GameTeam(this, "Christmas Thieves", ChatColor.RED, new ArrayList<>());
		AddTeam(_badGuys);
	}

	@EventHandler
	public void updateSection(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !IsLive())
		{
			return;
		}

		if (_currentSection == null || _currentSection.isComplete() || _forceSkip)
		{
			if (_currentSection != null)
			{
				UtilServer.Unregister(_currentSection);
				_currentSection.end();
				UtilTextMiddle.display("❄" + C.cGreen + " Well Done " + C.cWhite + "❄", "Follow Santa", 0, 80, 10, GetPlayers(true).toArray(new Player[0]));
			}

			_forceSkip = false;
			_currentSection = getNext();

			if (_currentSection == null)
			{
				return;
			}

			UtilServer.RegisterEvents(_currentSection);
			_currentSection.start();
			_currentSection.setObjective("Follow Santa");

			for (Player player : GetPlayers(true))
			{

			}

			Location target = _currentSection.getSleighTarget();

			if (_forceSkipTeleport)
			{
				_forceSkipTeleport = false;

				for (Player player : GetPlayers(true))
				{
					player.teleport(target);
				}
			}

			getSleigh().SetTarget(target);
		}
	}

	private Section getNext()
	{
		return _sections.isEmpty() ? null : _sections.remove(0);
	}

	@EventHandler
	public void sleighSpawn(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		List<Location> elfSpawns = getPlayersTeam().GetSpawns();

		Manager.runSyncLater(() ->
		{
			Manager.getPacketHandler().addPacketHandler(
					_reindeerPackets,
					PacketPlayOutEntityDestroy.class,
					PacketPlayOutSpawnEntityLiving.class);

			getSleigh();

			CreatureAllowOverride = true;

			for (Location location : elfSpawns)
			{
				if (Math.random() < 0.6)
				{
					continue;
				}

				Villager elf = location.getWorld().spawn(UtilAlg.getRandomLocation(location, 3, 0, 3), Villager.class);

				elf.setBaby();
				elf.setAgeLock(true);

				elf.setCustomName("Elf");
				elf.setCustomNameVisible(true);
			}

			CreatureAllowOverride = false;
		}, GetPlayers(true).size() * TickPerTeleport + 10);
	}

	@EventHandler
	public void sleighUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || _sleigh == null)
		{
			return;
		}

		for (SleighHorse horse : _sleigh.getHorses())
		{
			horse.onTick();
		}

		if (!IsLive())
		{
			return;
		}

		getSleigh().Update();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void damageCancel(CustomDamageEvent event)
	{
		if (_sleigh != null)
		{
			_sleigh.onDamage(event);
		}

		if (event.GetDamageeEntity() instanceof ArmorStand)
		{
			event.SetCancelled("Armour Stand");
			return;
		}

		if (InProgress() && event.GetDamageePlayer() != null)
		{
			switch (event.GetCause())
			{
				case FALL:
					event.SetCancelled("Fall Damage");
					break;
				case SUFFOCATION:
					event.GetDamageeEntity().teleport(getSleigh().GetLocation());
					break;
			}
		}
	}

	@EventHandler
	public void updateDeath(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !IsLive())
		{
			return;
		}

		for (Player player : GetPlayers(true))
		{
			if (player.getLocation().getY() < MIN_Y)
			{
				Manager.GetDamage().NewDamageEvent(player, null, null, DamageCause.CUSTOM, 100, false, true, true, GetName(), "Falling");
			}
		}
	}

	@EventHandler
	public void updateReigns(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER || !InProgress() || _sleigh == null || _sleigh.getSanta() == null)
		{
			return;
		}

		Entity santa = ((CraftEntity) _sleigh.getSanta()).getHandle();

		for (SleighHorse horse : _sleigh.getHorses())
		{
			if (horse.Ent == null || !horse.Ent.isValid())
			{
				continue;
			}

			PacketPlayOutAttachEntity packet = new PacketPlayOutAttachEntity(1, ((CraftHorse) horse.Ent).getHandle(), santa);

			for (Player player : UtilServer.getPlayersCollection())
			{
				UtilPlayer.sendPacket(player, packet);
			}
		}
	}

	@EventHandler
	public void barrierDecay(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !IsLive())
		{
			return;
		}

		_barrier.removeIf(location ->
		{
			if (UtilMath.offsetSquared(getSleigh().GetLocation(), location) > BARRIER_BREAK_SQUARED)
			{
				return false;
			}

			MapUtil.QuickChangeBlockAt(location, Material.AIR);
			return true;
		});
	}

	@EventHandler
	public void timeUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !IsLive() || _currentSection == null)
		{
			return;
		}

		int timeSet = _currentSection.getTimeSet();

		if (timeSet > 0 && WorldTimeSet < timeSet)
		{
			WorldTimeSet += 5;
		}

		if (UtilTime.elapsed(GetStateTime(), GameTimeout))
		{
			endGame(false, "You couldn't save Christmas in time!");
		}
	}

	@EventHandler
	public void combust(EntityCombustEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void itemSpawn(ItemSpawnEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void blockFade(BlockFadeEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void entityDeath(EntityDeathEvent event)
	{
		event.setDroppedExp(0);
	}

	@EventHandler
	public void armourStandManipulate(PlayerArmorStandManipulateEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void projectileHit(ProjectileHitEvent event)
	{
		if (event.getEntity() instanceof Arrow && InProgress())
		{
			event.getEntity().remove();
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void entityExplode(EntityExplodeEvent event)
	{
		event.blockList().clear();
	}

	@Override
	public void RespawnPlayerTeleport(Player player)
	{
		player.teleport(_sleigh.GetLocation());
	}

	@Override
	public Location GetSpectatorLocation()
	{
		if (!IsLive())
		{
			return super.GetSpectatorLocation();
		}
		else
		{
			List<Player> alive = GetPlayers(true);
			List<Location> locations = new ArrayList<>(alive.size());
			alive.forEach(player -> locations.add(player.getLocation()));
			return UtilAlg.getAverageLocation(locations);
		}
	}

	@Override
	public void EndCheck()
	{
		if (!IsLive())
		{
			return;
		}

		List<Player> alive = GetPlayers(true);

		if (alive.isEmpty())
		{
			endGame(false, "Everyone died!");
		}
	}

	public void endGame(boolean victory, String customLine)
	{
		if (victory)
		{
			for (Player player : GetPlayers(true))
			{
				AddGems(player, 10, "Participation", false, false);
			}

			WorldTimeSet = 18000;
			Manager.runSyncLater(() -> sendSantaMessage("You did it! I can’t believe it! You saved Christmas!", ChristmasNewAudio.SANTA_YOU_DID_IT), 80);
		}

		for (Player player : GetPlayers(true))
		{
			AddGems(player, 20, "Defeating the Pumpkin King", false, false);
		}

		SetCustomWinLine(customLine);
		AnnounceEnd(victory ? getPlayersTeam() : _badGuys);
		SetState(GameState.End);
	}

	@Override
	public void disable()
	{
		super.disable();

		if (_currentSection != null)
		{
			UtilServer.Unregister(_currentSection);
			_currentSection.end();
		}
	}

	public void sendSantaMessage(String message, ChristmasNewAudio audio)
	{
		if (GetState() == GameState.Dead)
		{
			return;
		}

		GetPlayers(false).forEach(player -> sendSantaMessage(player, message, audio));
	}

	public void sendSantaMessage(Player player, String message, ChristmasNewAudio audio)
	{
		if (GetState() == GameState.Dead)
		{
			return;
		}

		UtilPlayer.playCustomSound(player, audio);
		player.sendMessage(C.cRedB + "Santa" + C.cWhiteB + ": " + C.cYellow + message);
	}

	public void sendBossMessage(String message, ChristmasNewAudio audio)
	{
		if (GetState() == GameState.Dead)
		{
			return;
		}

		GetPlayers(false).forEach(player ->
		{
			UtilPlayer.playCustomSound(player, audio);
			player.sendMessage(C.cGoldB + "Pumpkin King" + C.cWhiteB + ": " + C.cYellow + message);
		});
	}

	public Sleigh getSleigh()
	{
		if (_sleigh == null)
		{
			_sleigh = new Sleigh();
			_sleigh.setupSleigh(this, _sleighSpawn);
		}

		return _sleigh;
	}
}
