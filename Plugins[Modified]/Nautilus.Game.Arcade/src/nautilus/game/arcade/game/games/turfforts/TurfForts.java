package nautilus.game.arcade.game.games.turfforts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.mission.MissionTrackerType;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.events.PlayerKitGiveEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.TeamGame;
import nautilus.game.arcade.game.games.turfforts.kits.KitInfiltrator;
import nautilus.game.arcade.game.games.turfforts.kits.KitMarksman;
import nautilus.game.arcade.game.games.turfforts.kits.KitShredder;
import nautilus.game.arcade.game.games.turfforts.mission.KillMidAirMissionTracker;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.game.modules.rejoin.RejoinModule;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.managers.chat.ChatStatData;
import nautilus.game.arcade.stats.BehindEnemyLinesStatTracker;
import nautilus.game.arcade.stats.BlockBreakStatTracker;
import nautilus.game.arcade.stats.BlockPlaceStatTracker;
import nautilus.game.arcade.stats.BlockShreadStatTracker;
import nautilus.game.arcade.stats.TheComebackStatTracker;

public class TurfForts extends TeamGame
{

	public static class ShredBlockEvent extends BlockEvent
	{
		private static final HandlerList HANDLER_LIST = new HandlerList();

		public static HandlerList getHandlerList()
		{
			return HANDLER_LIST;
		}

		@Override
		public HandlerList getHandlers()
		{
			return getHandlerList();
		}

		private final Arrow _arrow;

		ShredBlockEvent(Block theBlock, Arrow arrow)
		{
			super(theBlock);

			_arrow = arrow;
		}

		public Arrow getArrow()
		{
			return _arrow;
		}
	}

	private List<Location> _turf;

	private Location _red;
	private Location _redBase;

	private Location _blue;
	private Location _blueBase;

	private int _xRed = 0;
	private int _zRed = 0;

	private long _phaseTime = 0;
	private long _buildTime = TimeUnit.SECONDS.toMillis(20);
	private long _fightTime = TimeUnit.SECONDS.toMillis(90);
	private boolean _fight = false;
	private int _lines = 0;

	private final BlockBreakStatTracker _breakStatTracker;
	private final Map<Player, InfiltrateData> _enemyTurf = new HashMap<>();
	private final Set<UUID> playersThatNeedBlocks = new HashSet<>();

	public TurfForts(ArcadeManager manager)
	{
		super(manager, GameType.TurfWars,

				new Kit[]
						{
								new KitMarksman(manager),
								new KitInfiltrator(manager),
								new KitShredder(manager),
						},

				new String[]
						{
								"You have " + C.cGreen + "40 Seconds" + C.Reset + " to build your " + C.cAqua + "Fort" + C.Reset + "!",
								"Each " + C.cRed + "Kill" + C.Reset + " advances your turf forwards.",
								"Take over " + C.cYellow + "All The Turf" + C.Reset + " to win!"
						});

		StrictAntiHack = true;
		HungerSet = 20;
		DeathOut = false;
		BlockPlaceAllow.add(Material.WOOL.getId());
		BlockBreakAllow.add(Material.WOOL.getId());
		ItemDrop = false;
		ItemPickup = false;
		DamageSelf = false;
		DamageFall = false;
		DeathSpectateSecs = 4;
		GameTimeout = TimeUnit.MINUTES.toMillis(15);
		InventoryClick = true;

		_breakStatTracker = new BlockBreakStatTracker(this, false);

		registerStatTrackers(
				new BlockShreadStatTracker(this),
				new BehindEnemyLinesStatTracker(this),
				new TheComebackStatTracker(this),
				new BlockPlaceStatTracker(this, new Material[]{}),
				_breakStatTracker
		);

		registerMissions(new KillMidAirMissionTracker(this));

		registerChatStats(
				Kills,
				Deaths,
				KDRatio,
				BlankLine,
				new ChatStatData("BlocksPlaced", "Blocks Placed", true),
				new ChatStatData("BlocksBroken", "Blocks Broken", true)
		);

		new RejoinModule(manager)
				.register(this);

		new CompassModule()
				.setGiveCompass(true)
				.setGiveCompassToSpecs(true)
				.setGiveCompassToAlive(false)
				.register(this);
	}

	@Override
	public void ParseData()
	{
		_turf = WorldData.GetDataLocs("YELLOW");

		_red = WorldData.GetDataLocs("RED").get(0);
		_redBase = WorldData.GetDataLocs("PINK").get(0);

		_blue = WorldData.GetDataLocs("BLUE").get(0);
		_blueBase = WorldData.GetDataLocs("LIGHT_BLUE").get(0);

		if (_red.getBlockX() > _blue.getBlockX())
		{
			_xRed = 1;
		}
		else if (_red.getBlockX() < _blue.getBlockX())
		{
			_xRed = -1;
		}

		if (_red.getBlockZ() > _blue.getBlockZ())
		{
			_zRed = 1;
		}
		else if (_red.getBlockZ() < _blue.getBlockZ())
		{
			_zRed = -1;
		}

		//Color Turf
		for (Location location : _turf)
		{
			if (UtilMath.offsetSquared(location, _red) < UtilMath.offsetSquared(location, _blue))
			{
				MapUtil.QuickChangeBlockAt(location, Material.STAINED_CLAY, (byte) 14);
			}
			else
			{
				MapUtil.QuickChangeBlockAt(location, Material.STAINED_CLAY, (byte) 3);
			}
		}
	}

	@EventHandler
	public void prepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		CreatureAllowOverride = true;
		spawnKitNPCs(GetTeam(ChatColor.RED), "ORANGE");
		spawnKitNPCs(GetTeam(ChatColor.AQUA), "CYAN");
		CreatureAllowOverride = false;
	}

	private void spawnKitNPCs(GameTeam team, String colour)
	{
		List<Location> spawns = WorldData.GetDataLocs(colour);
		int i = 0;

		if (spawns.size() < GetKits().length)
		{
			return;
		}

		for (Kit kit : GetKits())
		{
			Location location = spawns.get(i++);
			location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, GetSpectatorLocation())));
			kit.getGameKit().createNPC(location);
		}
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		Player killed = event.getEntity(), killer = killed.getKiller();
		_enemyTurf.remove(killed);

		if (killer == null)
		{
			return;
		}

		InfiltrateData data = _enemyTurf.get(killer);

		if (data != null)
		{
			data.HasKilled = true;
		}

		GameTeam killedTeam = GetTeam(killed), killerTeam = GetTeam(killer);

		if (killedTeam.equals(killerTeam))
		{
			return;
		}

		turfMove(killerTeam.GetColor() == ChatColor.RED);
	}

	private void turfMove(boolean red)
	{
		for (int line = 0; line < getLinesPerKill(); line++)
		{
			if (red)
			{
				if (_xRed != 0)
				{
					for (Location location : _turf)
					{
						if (location.getBlockX() == _blue.getBlockX())
						{
							MapUtil.QuickChangeBlockAt(location, Material.STAINED_CLAY, (byte) 14);

							for (int i = 1; i < 6; i++)
							{
								if (location.getBlock().getRelative(BlockFace.UP, i).getType() == Material.WOOL)
								{
									MapUtil.QuickChangeBlockAt(location.clone().add(0, i, 0), 0, (byte) 0);
								}
							}
						}
					}
				}

				if (_zRed != 0)
				{
					for (Location location : _turf)
					{
						if (location.getBlockZ() == _blue.getBlockZ())
						{
							MapUtil.QuickChangeBlockAt(location, Material.STAINED_CLAY, (byte) 14);

							for (int i = 1; i < 6; i++)
							{
								if (location.getBlock().getRelative(BlockFace.UP, i).getType() == Material.WOOL)
								{
									MapUtil.QuickChangeBlockAt(location.clone().add(0, i, 0), 0, (byte) 0);
								}
							}
						}
					}
				}

				_red.subtract(_xRed, 0, _zRed);
				_blue.subtract(_xRed, 0, _zRed);
			}
			else
			{
				if (_xRed != 0)
					for (Location location : _turf)
						if (location.getBlockX() == _red.getBlockX())
						{
							MapUtil.QuickChangeBlockAt(location, Material.STAINED_CLAY, (byte) 3);

							for (int i = 1; i < 6; i++)
								if (location.getBlock().getRelative(BlockFace.UP, i).getType() == Material.WOOL)
									MapUtil.QuickChangeBlockAt(location.clone().add(0, i, 0), 0, (byte) 0);
						}

				if (_zRed != 0)
					for (Location location : _turf)
						if (location.getBlockZ() == _red.getBlockZ())
						{
							MapUtil.QuickChangeBlockAt(location, Material.STAINED_CLAY, (byte) 3);

							for (int i = 1; i < 6; i++)
								if (location.getBlock().getRelative(BlockFace.UP, i).getType() == Material.WOOL)
									MapUtil.QuickChangeBlockAt(location.clone().add(0, i, 0), 0, (byte) 0);
						}

				_red.add(_xRed, 0, _zRed);
				_blue.add(_xRed, 0, _zRed);
			}

			EndCheck();
		}
	}

	@EventHandler
	public void shootBow(EntityShootBowEvent event)
	{
		if (!_fight)
		{
			UtilPlayer.message(event.getEntity(), F.main("Game", "You cannot attack during Build Time!"));
			event.setCancelled(true);

			((Player) event.getEntity()).updateInventory();
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void blockBreak(BlockBreakEvent event)
	{
		Player player = event.getPlayer();

		if (UtilPlayer.isSpectator(player))
		{
			event.setCancelled(true);
			return;
		}

		Block block = event.getBlock();

		GameTeam team = GetTeam(event.getPlayer());
		GameTeam otherTeam = getOtherTeam(team);

		if (block.getType().equals(Material.WOOL) && (block.getData() == 14 && team.GetColor() != ChatColor.RED) || (block.getData() == 3 && team.GetColor() != ChatColor.AQUA))
		{
			player.sendMessage(F.main("Game", "You cannot break the " + F.elem(otherTeam.GetFormattedName()) + " team's blocks!"));
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void blockPlace(BlockPlaceEvent event)
	{
		Player player = event.getPlayer();
		GameTeam team = GetTeam(event.getPlayer());
		Block block = event.getBlock().getRelative(BlockFace.DOWN);

		if (team == null || UtilPlayer.isSpectator(player))
		{
			event.setCancelled(true);
			return;
		}

		//On Own
		while (block.getType() == Material.AIR && block.getY() > 0)
		{
			block = block.getRelative(BlockFace.DOWN);
		}

		if ((block.getType() != Material.STAINED_CLAY && block.getType() != Material.WOOL) || block.getData() != team.GetColorData())
		{
			player.sendMessage(F.main("Game", "You can only build above " + F.elem(team.GetFormattedName()) + "."));
			event.setCancelled(true);
			return;
		}

		//Height
		boolean aboveTurf = false;
		for (int i = 1; i <= 5; i++)
		{
			if (event.getBlock().getRelative(BlockFace.DOWN, i).getType() != Material.STAINED_CLAY)
			{
				continue;
			}

			aboveTurf = true;
			break;
		}

		if (!aboveTurf)
		{
			player.sendMessage(F.main("Game", "You cannot build this high above Turf."));
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void blockHit(ProjectileHitEvent event)
	{
		Projectile entity = event.getEntity();

		if (!(entity instanceof Arrow) || entity.getShooter() == null || !(entity.getShooter() instanceof Player))
		{
			return;
		}

		Player shooter = (Player) event.getEntity().getShooter();
		GameTeam team = GetTeam(shooter);

		if (team == null)
		{
			return;
		}

		Arrow arrow = (Arrow) event.getEntity();

		Manager.runSyncLater(() ->
		{
			Block block = UtilEnt.getHitBlock(arrow);
			byte data = block.getData();

			if (block.getType() == Material.WOOL)
			{
				if (data == 3 || data == 14)
				{
					block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, data == 3 ? Material.LAPIS_BLOCK : Material.REDSTONE_BLOCK);
					_breakStatTracker.addStat(shooter);
					getArcadeManager().getMissionsManager().incrementProgress(shooter, 1, MissionTrackerType.TURF_WARS_BOW_BREAK, GetType().getDisplay(), null);
					AddGems(shooter, 0.25, "Blocks Broken", true, true);
				}

				UtilServer.CallEvent(new ShredBlockEvent(block, arrow));
				block.setType(Material.AIR);
			}

			arrow.remove();
		}, 0);
	}

	@EventHandler
	public void damage(CustomDamageEvent event)
	{
		if (!_fight && (event.GetCause() == DamageCause.PROJECTILE || event.GetCause() == DamageCause.ENTITY_ATTACK))
		{
			event.SetCancelled("Build Time");
			return;
		}

		Player damager = event.GetDamagerPlayer(true);

		if (damager == null)
		{
			return;
		}

		if (event.GetCause() == DamageCause.PROJECTILE)
		{
			if (GetKit(damager) instanceof KitShredder)
			{
				event.AddMod("Shredder", "Barrage", -event.GetDamage() + 9, true);
			}
			else
			{
				event.AddMod(GetName(), "One Hit Kill", 500, false);
			}
		}
		else if (event.GetCause() == DamageCause.ENTITY_ATTACK)
		{
			ItemStack itemStack = damager.getItemInHand();

			event.AddMod(GetName(), "Sword", -event.GetDamage() + (itemStack != null && itemStack.getType() == Material.IRON_SWORD ? 12 : 6), false);
		}
	}

	@EventHandler
	public void ScoreboardTitle(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER || !IsLive())
		{
			return;
		}

		//2x Initial Build
		if (_phaseTime == 0)
		{
			_phaseTime = System.currentTimeMillis() + _buildTime;
		}

		long time;
		if (!_fight)
		{
			time = _buildTime - (System.currentTimeMillis() - _phaseTime);

			if (time <= 0)
			{
				_fight = true;
				_lines++;

				Announce(" ", true);
				Announce(C.cWhiteB + "1 Kill = " + getLinesPerKill() + " Turf Lines", false);
				Announce(C.cWhiteB + "90 Seconds of " + C.cYellowB + "Combat Time" + C.cWhiteB + " has begun!", false);
				Announce(" ", false);

				_phaseTime = System.currentTimeMillis();
			}
		}

		else
		{
			time = _fightTime - (System.currentTimeMillis() - _phaseTime);

			if (time <= 0)
			{
				_fight = false;

				Announce(" ", true);
				Announce(C.cWhiteB + "20 Seconds of " + C.cGreenB + "Build Time" + C.cWhiteB + " has begun!", false);
				Announce(" ", false);

				_phaseTime = System.currentTimeMillis();

				for (GameTeam team : GetTeamList())
				{
					for (Player player : team.GetPlayers(true))
					{
						if (UtilPlayer.isSpectator(player))
						{
							playersThatNeedBlocks.add(player.getUniqueId());
						}
						else
						{
							addBlocks(team, player);
						}
					}
				}
			}
		}
	}

	private void addBlocks(GameTeam team, Player player)
	{
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.WOOL, team.GetColorData(), 24));
	}

	@EventHandler(ignoreCancelled = true)
	public void onQuit(PlayerQuitEvent event)
	{
		playersThatNeedBlocks.remove(event.getPlayer().getUniqueId());
	}

	@EventHandler(ignoreCancelled = true)
	public void onSpawn(PlayerKitGiveEvent event)
	{
		Player player = event.getPlayer();

		if (playersThatNeedBlocks.remove(player.getUniqueId()))
		{
			addBlocks(GetTeam(player), player);
		}
	}

	@EventHandler
	public void blockGlitchFix(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (event.getAction() != Action.RIGHT_CLICK_BLOCK || UtilEnt.isGrounded(player) || !IsLive())
		{
			return;
		}

		Block block = event.getClickedBlock();

		if (block.getType() == Material.STAINED_CLAY || block.getType() == Material.WOOL)
		{
			return;
		}

		Block ground = player.getLocation().getBlock();

		while (ground.getType() == Material.AIR && ground.getLocation().getBlockY() >= 0)
		{
			ground = ground.getRelative(BlockFace.DOWN);
		}

		if (ground.isLiquid())
		{
			Manager.GetDamage().NewDamageEvent(player, null, null, DamageCause.VOID, 500, false, true, true, GetName(), "Border Damage");
			player.playSound(player.getLocation(), Sound.NOTE_BASS, 2f, 1f);
			return;
		}

		if (UtilMath.offsetSquared(player.getLocation(), ground.getLocation()) < 16)
		{
			return;
		}

		Manager.GetDamage().NewDamageEvent(player, null, null, DamageCause.VOID, 500, false, true, true, GetName(), "Border Damage");
		player.playSound(player.getLocation(), Sound.NOTE_BASS, 2, 1);
	}

	// Keep ladders placed on ice when block updates occur.
	@EventHandler
	public void ladderDestroyFix(BlockPhysicsEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		Block block = event.getBlock();

		if (block.getType() == Material.LADDER)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void itemSpawn(ItemSpawnEvent event)
	{
		ItemStack itemStack = event.getEntity().getItemStack();

		if (itemStack.getType() == Material.WOOL)
		{
			event.setCancelled(true);
		}
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		for (GameTeam team : GetTeamList())
		{
			int lines = getLines(team);

			Scoreboard.writeNewLine();
			Scoreboard.write(lines + " " + team.GetColor() + team.GetName());
		}

		if (!_fight)
		{
			long time = _buildTime - (System.currentTimeMillis() - _phaseTime);

			Scoreboard.writeNewLine();
			Scoreboard.write(C.cYellowB + "Build Time");
			Scoreboard.write(UtilTime.MakeStr(Math.max(0, time), 0));
		}
		else
		{
			long time = _fightTime - (System.currentTimeMillis() - _phaseTime);

			Scoreboard.writeNewLine();
			Scoreboard.write(C.cYellowB + "Combat Time");
			Scoreboard.write(UtilTime.MakeStr(Math.max(0, time), 0));
		}

		Scoreboard.draw();
	}

	private int getRedLines()
	{
		if (!InProgress())
		{
			return 0;
		}

		if (_xRed != 0)
		{
			return Math.abs(_redBase.getBlockX() - _red.getBlockX());
		}

		return Math.abs(_redBase.getBlockZ() - _red.getBlockZ());
	}

	private int getBlueLines()
	{
		if (!InProgress())
		{
			return 0;
		}

		if (_xRed != 0)
		{
			return Math.abs(_blueBase.getBlockX() - _blue.getBlockX());
		}

		return Math.abs(_blueBase.getBlockZ() - _blue.getBlockZ());
	}

	public int getLines(GameTeam team)
	{
		return team.GetColor() == ChatColor.RED ? getRedLines() : getBlueLines();
	}

	private int getLinesPerKill()
	{
		return _lines;
	}

	@EventHandler
	public void Territory(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST || !IsLive())
		{
			return;
		}

		for (GameTeam team : GetTeamList())
		{
			for (Player player : team.GetPlayers(true))
			{
				if (UtilPlayer.isSpectator(player))
				{
					continue;
				}

				Location location = player.getLocation();

				//Slow
				if (_enemyTurf.containsKey(player))
				{
					InfiltrateData data = getInfiltrateData(player);

					if (data.SlownessLevel > -1)
					{
						Manager.GetCondition().Factory().Slow("Infiltrator Slow", player, player, 0.9, data.SlownessLevel, false, false, false, false);
					}
				}

				Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);

				// Apply velocity even if the player is sneaking near turf edges.
				if (block.isEmpty() && UtilEnt.isGrounded(player))
				{
					for (Block near : UtilBlock.getSurrounding(block, true))
					{
						if (near.getType() == Material.STAINED_CLAY || near.getType() == Material.WOOL)
						{
							block = near;
							break;
						}
					}
				}

				while (block.getType() != Material.STAINED_CLAY && block.getY() > 0)
				{
					block = block.getRelative(BlockFace.DOWN);
				}

				if (block.getType() == Material.AIR)
				{
					continue;
				}

				byte data = block.getData();

				//On Enemy Turf
				if ((team.GetColor() == ChatColor.RED && data == 3) || (team.GetColor() == ChatColor.AQUA && data == 14))
				{
					//Infiltrate
					if (_fight && GetKit(player) != null && GetKit(player) instanceof KitInfiltrator)
					{
						_enemyTurf.putIfAbsent(player, new InfiltrateData());
						continue;
					}

					knockback(player, team);
				}
				//On Own Turf
				else if ((team.GetColor() == ChatColor.RED && data == 14) || (team.GetColor() == ChatColor.AQUA && data == 3))
				{
					_enemyTurf.remove(player);
				}
				else
				{
					Map<Location, GameTeam> averages = new HashMap<>(GetTeamList().size());

					for (GameTeam other : GetTeamList())
					{
						averages.put(UtilAlg.getAverageLocation(other.GetSpawns()), other);
					}

					Location nearest = UtilAlg.findClosest(location, averages.keySet());

					if (!averages.get(nearest).equals(team) && Recharge.Instance.use(player, "Spawn Damage", 2000, false, false))
					{
						Manager.GetDamage().NewDamageEvent(player, null, null, DamageCause.CUSTOM, _fight ? 2 : 500, false, true, true, GetName(), "Spawn");
					}
				}
			}
		}
	}

	@EventHandler
	public void updateInfiltrate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		_enemyTurf.forEach((player, data) ->
		{
			data.Seconds++;
			getArcadeManager().getMissionsManager().incrementProgress(player, 1, MissionTrackerType.TURF_WARS_ON_ENEMY, GetType().getDisplay(), null);

			if (UtilTime.elapsed(data.LastTick, data.HasKilled ? 2500 : 5000))
			{
				data.SlownessLevel++;
				data.LastTick = System.currentTimeMillis();
			}
		});
	}

	private void knockback(Player player, GameTeam team)
	{
		if (Recharge.Instance.use(player, "Territory Knockback", 2000, false, false))
		{
			Location location = player.getLocation();
			UtilAction.velocity(player, UtilAlg.getTrajectory2d(location, team.GetSpawn()), 2, false, 0, 0.8, 1, true);
			player.playSound(location, Sound.NOTE_BASS, 2, 1);
			player.sendMessage(F.main("Game", "You cannot walk on the enemy's turf!"));
		}
	}

	private GameTeam getOtherTeam(GameTeam team)
	{
		return team.GetColor() == ChatColor.RED ? GetTeam(ChatColor.AQUA) : GetTeam(ChatColor.RED);
	}

	@Override
	public void EndCheck()
	{
		if (!IsLive())
		{
			return;
		}

		if (getRedLines() == 0 || GetTeam(ChatColor.RED).GetPlayers(true).isEmpty())
		{
			AnnounceEnd(GetTeam(ChatColor.AQUA));
		}
		else if (getBlueLines() == 0 || GetTeam(ChatColor.AQUA).GetPlayers(true).isEmpty())
		{
			AnnounceEnd(GetTeam(ChatColor.RED));
		}
		else
		{
			return;
		}

		for (GameTeam team : GetTeamList())
		{
			if (WinnerTeam != null && team.equals(WinnerTeam))
			{
				for (Player player : team.GetPlayers(false))
				{
					AddGems(player, 10, "Winning Team", false, false);
				}
			}

			for (Player player : team.GetPlayers(false))
			{
				if (player.isOnline())
				{
					AddGems(player, 10, "Participation", false, false);
				}
			}
		}

		SetState(GameState.End);
	}

	@Override
	public double GetKillsGems(Player killer, Player killed, boolean assist)
	{
		return assist ? 1 : 2;
	}

	public InfiltrateData getInfiltrateData(Player player)
	{
		return _enemyTurf.get(player);
	}

	public class InfiltrateData
	{

		public int Seconds, SlownessLevel = -1;
		public boolean HasKilled;
		public long LastTick = System.currentTimeMillis();

	}
}