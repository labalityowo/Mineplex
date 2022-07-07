package nautilus.game.arcade.game.games.wither;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mineplex.core.common.block.BlockData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.GameTeam.PlayerState;
import nautilus.game.arcade.game.TeamGame;
import nautilus.game.arcade.game.games.wither.events.HumanReviveEvent;
import nautilus.game.arcade.game.games.wither.kit.KitHumanArcher;
import nautilus.game.arcade.game.games.wither.kit.KitHumanEditor;
import nautilus.game.arcade.game.games.wither.kit.KitHumanMedic;
import nautilus.game.arcade.game.games.wither.kit.KitWitherMinion;
import nautilus.game.arcade.game.modules.TeamArmorModule;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.game.team.selectors.RatioSelector;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.NullKit;
import nautilus.game.arcade.kit.perks.data.IBlockRestorer;
import nautilus.game.arcade.managers.chat.ChatStatData;
import nautilus.game.arcade.stats.TeamDeathsStatTracker;
import nautilus.game.arcade.stats.TeamKillsStatTracker;

public class WitherGame extends TeamGame implements IBlockRestorer
{

	private static final long GAME_TIMEOUT = TimeUnit.MINUTES.toMillis(5);

	private GameTeam _runners;
	private GameTeam _withers;
	
	private int _yLimit = 0;
	private int _maxY;
	
	private final Map<Player, PlayerCopyWither> _doubles = new HashMap<>();
	private final Set<BlockData> _blocks = new HashSet<>();
	private final List<Location> _locationsOfBlocks = new ArrayList<>();

	public WitherGame(ArcadeManager manager)
	{
		super(manager, GameType.WitherAssault,

		new Kit[]
		{
				new KitHumanArcher(manager),
				new KitHumanMedic(manager),
				new KitHumanEditor(manager),
				new NullKit(manager),
				new KitWitherMinion(manager),
		},

		new String[]
		{

				C.cGreen + "Humans" + C.cWhite + "  Run and hide from the Withers",
				C.cGreen + "Humans" + C.cWhite + "  Revive your dead allies!",
				C.cGreen + "Humans" + C.cWhite + "  Win by surviving for 5 minutes",
				" ",
				C.cRed + "Withers" + C.cWhite + "  Moves very slowly when near ground",
				C.cRed + "Withers" + C.cWhite + "  Kill all the Humans within 5 Minutes",
		});

		DeathOut = true;
		DamageTeamSelf = false;
		DamageSelf = false;
		DeathSpectateSecs = 4;
		HungerSet = 20;
		WorldBoundaryKill = false;
		
		//Customizing for the Editor kit
		BlockBreak = true;
		BlockPlace = true;
		ItemPickup = true;

		KitRegisterState = GameState.Prepare;
		
		InventoryClick = false;
		InventoryOpenBlock = false;

		_help = new String[]
		{
			"Blocks placed by an Editor can be passed by other humans by " + C.cDGreen + "Right-clicking the block",
			"Withers are too powerful to be killed. Hiding is the only option!",
			"Medics are a valuable asset. Stick with them and keep them safe!",
		};
		
		
		registerStatTrackers(
				new TeamDeathsStatTracker(this),
				new TeamKillsStatTracker(this)
		);

		registerChatStats(
				Deaths,
				DamageTaken,
				DamageDealt,
				BlankLine,
				new ChatStatData("kit", "Kit", true)
		);

		new CompassModule()
				.setGiveCompass(true)
				.setGiveCompassToSpecs(true)
				.setGiveCompassToAlive(false)
				.register(this);
		new TeamArmorModule()
				.giveTeamArmor()
				.register(this);
	}

	@Override
	public void ParseData()
	{
		_yLimit = WorldData.GetDataLocs("RED").get(0).getBlockY();
	}

	@EventHandler
	public void teamSetup(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Recruit)
			return;

		for (Kit kit : GetKits())
		{
			for (GameTeam team : GetTeamList())
			{
				if (team.GetColor() == ChatColor.RED)
				{
					_withers = team;
					_withers.SetName("Withers");
					_withers.SetColor(ChatColor.RED);

					if (!kit.GetName().contains("Wither"))
						team.GetRestrictedKits().add(kit);
				}
				else
				{
					_runners = team;
					_runners.SetName("Humans");
					_runners.SetColor(ChatColor.GREEN);

					if (kit.GetName().contains("Wither"))
						team.GetRestrictedKits().add(kit);
				}
			}
		}

		_teamSelector = new RatioSelector(_withers, 0.25);
	}

	// Cancel wither shooting in waiting lobby
	@EventHandler
	public void onWitherSkullFire(ProjectileLaunchEvent event)
	{
		if (GetState() == GameState.Recruit || GetState() == GameState.Prepare)
		{
			Projectile proj = event.getEntity();

			if (proj instanceof WitherSkull)
			{
				WitherSkull ws = (WitherSkull) proj;

				if (ws.getShooter() instanceof Wither)
				{
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void handleCustomBlockPlace(BlockPlaceEvent event)
	{
		if(!IsLive())
		{
			return;
		}
		if(!GetKit(event.getPlayer()).GetName().contentEquals("Human Editor"))
		{
			event.setCancelled(true);
			return;
		}
		
		_locationsOfBlocks.add(event.getBlock().getLocation());
	}
	
	@EventHandler
	public void handleCustomBlockbreak(BlockBreakEvent event)
	{
		if(!IsLive())
		{
			return;
		}
		if(!GetKit(event.getPlayer()).GetName().contentEquals("Human Editor"))
		{
			event.setCancelled(true);
			return;
		}
		
		Location blockLocation = event.getBlock().getLocation();
		if(blockLocation.add(0,1,0).getBlock().getType() == Material.AIR)
		{
			for(Player humans: _runners.GetPlayers(true))
			{
				if(IsAlive(humans))
				{
					if(humans.getLocation().add(0,-1,0).getBlock().equals(event.getBlock()))
					{
						if(humans.getName() != event.getPlayer().getName())
						{
							event.setCancelled(true);
							return;
						}
					}
				}
			}
		}
		
		if(blockLocation.getBlockY() < _maxY - 3)
		{
			event.getPlayer().sendMessage(F.main("BlockChecker", "You may not build under this height!"));
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler
	public void handleCustomItemPickup(PlayerPickupItemEvent event)
	{
		if(!IsLive())
		{
			return;
		}
		if(GetKit(event.getPlayer()) != null && !GetKit(event.getPlayer()).GetName().contentEquals("Human Editor"))
		{
			event.setCancelled(true);
			return;
		}
	}
	
	//On Player interact with a placed block by Editor
	@EventHandler
	public void onPlayerInteract(final PlayerInteractEvent event)
	{
		if(!IsLive())
		{
			return;
		}
		if(!IsAlive(event.getPlayer()))
		{
			return;
		}
		if(GetTeam(event.getPlayer()).GetColor() == ChatColor.RED)
		{
			return;
		}
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			Block clickedBlock = event.getClickedBlock();
			
			if(_locationsOfBlocks.contains(clickedBlock.getLocation()))
			{
				new BukkitRunnable()
				{
					
					@Override
					public void run()
					{
						if(!(event.getPlayer().getItemInHand().getType().isBlock()) || event.getPlayer().getItemInHand().getType() == Material.AIR)
						{
							UtilParticle.PlayParticle(ParticleType.FLAME, event.getClickedBlock().getLocation().add(0.5, 0.5, 0.5), 0, 0, 0, 0, 1, ViewDist.LONG, UtilServer.getPlayers());
							event.getClickedBlock().getWorld().playSound(event.getClickedBlock().getLocation(), Sound.NOTE_STICKS, 2f, 1f);
							Manager.GetBlockRestore().add(event.getClickedBlock(), 0, event.getClickedBlock().getData(), 2000);
						}
					}
				}.runTaskLater(Manager.getPlugin(), 5);
			}
		}
	}

	@EventHandler
	public void updateFairTeams(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !IsLive())
		{
			return;
		}

		if (_withers.GetPlayers(true).isEmpty() && !_runners.GetPlayers(true).isEmpty())
		{
			Player player = UtilAlg.Random(_runners.GetPlayers(true));
			setWither(player);
		}
	}

	private void setWither(Player player)
	{
		SetPlayerTeam(player, _withers, true);
		player.teleport(_withers.GetSpawn());

		AddGems(player, 10, "Forced Wither", false, false);

		Announce(F.main(
				"Game",
				F.elem(_withers.GetColor() + player.getName())
						+ " has become a "
						+ F.elem(_withers.GetColor() + GetKit(player).GetName())
						+ "."));

		player.getWorld().strikeLightningEffect(player.getLocation());
	}

	@EventHandler
	public void gameStart(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
			return;

		_maxY = GetTeamList().get(1).GetSpawn().getBlockY();
		UtilTextMiddle.display(C.cGreen + "Humans Hiding",
				"15 Seconds until Assault", 10, 80, 10);

		for (Player player : _withers.GetPlayers(true))
		{
			Manager.GetCondition()
					.Factory()
					.Blind("Game Start", player, null, 15, 0, false, false,
							false);
		}
	}
	
	@EventHandler
	public void removeUselessPlayerCopies(UpdateEvent event)
	{
		if(event.getType() != UpdateType.TWOSEC)
		return;
		
		for(Player players: _doubles.keySet())
		{
			if(!players.isOnline())
			{
				PlayerCopyWither pc = _doubles.get(players);
				pc.GetEntity().remove();
				_doubles.remove(players);
			}
		}
		
	}

	@Override
	public void EndCheck()
	{
		if (!IsLive())
			return;

		// Players Quit
		if (GetPlayers(true).size() < 1)
		{
			SetState(GameState.End);
			_locationsOfBlocks.clear();
		}

		GameTeam winner = null;

		// Wither Win
		if (UtilTime.elapsed(this.GetStateTime(), GAME_TIMEOUT))
			winner = _runners;

		// Runner Win
		if (_runners.GetPlayers(true).isEmpty())
			winner = _withers;

		// Set Win
		if (winner != null)
		{
			AnnounceEnd(winner);

			for (GameTeam team : GetTeamList())
			{
				if (WinnerTeam != null && team.equals(WinnerTeam))
				{
					for (Player player : team.GetPlayers(false))
						AddGems(player, 10, "Winning Team", false, false);
				}

				for (Player player : team.GetPlayers(false))
					if (player.isOnline())
						AddGems(player, 10, "Participation", false, false);
			}

			// End
			SetState(GameState.End);
			_locationsOfBlocks.clear();
		}
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (!InProgress())
			return;

		if (event.getType() != UpdateType.FAST)
			return;
		// Wipe Last
		Scoreboard.reset();

		Scoreboard.writeNewLine();
		Scoreboard.write(_runners.GetColor() + C.Bold + _runners.GetName());
		Scoreboard.write(_runners.GetColor() + "" + _runners.GetPlayers(true).size() + " Players");

		Scoreboard.writeNewLine();
		Scoreboard.write(_withers.GetColor() + C.Bold + _withers.GetName());
		Scoreboard.write(_withers.GetColor() + "" + _withers.GetPlayers(true).size() + " Players");

		Scoreboard.writeNewLine();
		Scoreboard.write(C.cYellow + C.Bold + "Time Left");
		Scoreboard.write(UtilTime.MakeStr(Math.max(0, GAME_TIMEOUT - (System.currentTimeMillis() - GetStateTime())), 1));

		Scoreboard.draw();
	}

	@EventHandler
	public void witherMovement(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.FASTER)
			return;

		Location _spawn = GetTeamList().get(1).GetSpawn();
		for (Player player : _withers.GetPlayers(true))
		{
			ArrayList<Location> collisions = new ArrayList<Location>();

			// Fly Speed
			
			double distanceToGround = player.getLocation().distance(new Location(_spawn.getWorld(), player.getLocation().getX(), _spawn.getY(), player.getLocation().getZ()));
			double speed;

			if (distanceToGround < 4)
			{
				speed = 0.016;
			}
			else
			{
				speed = 0.09 - (_yLimit - player.getLocation().getY()) * 0.006;
			}
			
			if (speed < 0.01) //This is to stop players having negative speed when they go under the map
			{
				speed = 0.01;
			}
			
			player.setFlySpeed((float) Math.min(1, speed));

			// Bump
			for (Block block : UtilBlock.getInRadius(
					player.getLocation().add(0, 0.5, 0), 1.5d).keySet())
			{
				if (!UtilBlock.airFoliage(block))
				{
					collisions.add(block.getLocation().add(0.5, 0.5, 0.5));
				}
			}

			Vector vec = UtilAlg.getAverageBump(player.getLocation(),
					collisions);

			if (vec == null)
				continue;

			UtilAction.velocity(player, vec, 0.6, false, 0, 0.4, 10, true);
		}
	}

	@Override
	public void addBlocks(Set<Block> blocks)
	{
		Iterator<Block> blockIter = blocks.iterator();

		while (blockIter.hasNext())
		{
			Block block = blockIter.next();

			if (block.getType() == Material.BEDROCK
					|| block.getType() == Material.IRON_BLOCK)
				blockIter.remove();

			else if (!isInsideMap(block.getLocation()))
				blockIter.remove();
		}

		for (Block block : blocks)
			_blocks.add(new BlockData(block));
	}

	@Override
	public void restoreBlock(Location loc, double radius)
	{
		Iterator<BlockData> dataIt = _blocks.iterator();

		while (dataIt.hasNext())
		{
			BlockData data = dataIt.next();

			double dist = UtilMath.offset(loc,
					data.Block.getLocation().add(0.5, 0.5, 0.5));

			if (dist < radius)
			{
				Manager.GetBlockRestore().add(data.Block, 0, (byte) 0,
						data.Material.getId(), data.Data,
						(long) (6000 * (dist / radius)));
				dataIt.remove();
			}
		}
	}

	@EventHandler
	public void arrowDamage(CustomDamageEvent event)
	{
		if (event.GetProjectile() == null)
			return;

		event.AddMult(GetName(), "Arrow Mod", 0.75, false);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void damageOut(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetDamageePlayer() == null)
			return;

		if (event.GetDamage() < event.GetDamageePlayer().getHealth())
			return;

		event.SetCancelled("Fake Death");

		event.GetDamageePlayer().playEffect(EntityEffect.HURT);

		playerOut(event.GetDamageePlayer());

		if (event.GetDamagerPlayer(true) != null)
		{
			AddGems(event.GetDamagerPlayer(true), 2, "Humans Stunned", true,
					true);

			Bukkit.broadcastMessage(C.cBlue + "Death> " + C.cGreen
					+ event.GetDamageePlayer().getName() + C.cGray
					+ " was killed by " + C.cRed
					+ event.GetDamagerPlayer(true).getName() + C.cGray + ".");
		}
		else
		{
			Bukkit.broadcastMessage(C.cBlue + "Death> " + C.cGreen
					+ event.GetDamageePlayer().getName() + C.cGray
					+ " was killed.");
		}
	}

	public void playerOut(Player player)
	{
		// State
		SetPlayerState(player, PlayerState.OUT);
		player.setHealth(20);

		player.setFlySpeed(0.1f);

		// Conditions
		Manager.GetCondition().Factory()
				.Blind("Hit", player, player, 1.5, 0, false, false, false);
		Manager.GetCondition().Factory()
				.Cloak("Hit", player, player, 9999, false, false);

		// Settings
		player.setAllowFlight(true);
		player.setFlying(true);
		((CraftPlayer) player).getHandle().spectating = true;
		((CraftPlayer) player).getHandle().k = false;

		player.setVelocity(new Vector(0, 1.2, 0));

		_doubles.put(player, new PlayerCopyWither(this, player, ChatColor.YELLOW));
	}
	
	public void playerIn(final Player player, final LivingEntity copy, Player revivedBy)
	{
		// State
		SetPlayerState(player, PlayerState.IN);
		player.setHealth(20);

		// Teleport
		if (copy != null)
		{
			Location loc = player.getLocation();
			loc.setX(copy.getLocation().getX());
			loc.setY(copy.getLocation().getY());
			loc.setZ(copy.getLocation().getZ());
			player.teleport(loc);
		}

		// Settings
		if (player.getGameMode() == GameMode.SPECTATOR)
			player.setSpectatorTarget(null);
			
		player.setGameMode(GameMode.SURVIVAL);
		player.setAllowFlight(false);
		player.setFlying(false);
		((CraftPlayer) player).getHandle().spectating = false;
		((CraftPlayer) player).getHandle().k = true;
		Manager.GetCondition().Factory().Invulnerable("Revival", player, player, 5, false, false);

		// Items
		player.getInventory().remove(Material.WATCH);
		player.getInventory().remove(Material.COMPASS);

		// Inform
		if(revivedBy != null)
		{
			UtilPlayer.message(player, F.main("Game", "You have been revived by " + C.cGold + revivedBy.getName()));
		}
		else
		{
			UtilPlayer.message(player, F.main("Game", "You have been revived!"));
		}

		// Delayed Visibility
		if (copy != null)
		{
			Manager.runSyncLater(() ->
			{
				// Remove Invis
				if (IsAlive(player))
				{
					Manager.GetCondition().EndCondition(player, ConditionType.CLOAK, null);
				}

				// Remove Copy
				copy.remove();
			}, 4);

		}
	}

	@EventHandler
	public void revive(ProjectileHitEvent event)
	{
		if (!IsLive())
			return;

		if (!(event.getEntity() instanceof ThrownPotion))
			return;

		if (event.getEntity().getShooter() == null)
			return;

		if (!(event.getEntity().getShooter() instanceof Player))
			return;

		Player thrower = (Player) event.getEntity().getShooter();
		if (!IsAlive(thrower))
			return;

		GameTeam throwerTeam = GetTeam(thrower);
		if (throwerTeam == null)
			return;

		// Revive a copy
		Iterator<PlayerCopyWither> copyIterator = _doubles.values().iterator();
		while (copyIterator.hasNext())
		{
			PlayerCopyWither copy = copyIterator.next();

			GameTeam otherTeam = GetTeam(copy.GetPlayer());
			if (otherTeam == null || !otherTeam.equals(throwerTeam))
				continue;

			if (UtilMath.offset(copy.GetEntity().getLocation().add(0, 1, 0),
					event.getEntity().getLocation()) > 3)
				continue;

			playerIn(copy.GetPlayer(), copy.GetEntity(), thrower);
			copyIterator.remove();

			AddGems(thrower, 3, "Revived Ally", true, true);
			UtilServer.CallEvent(new HumanReviveEvent(thrower, copy.GetPlayer()));
		}
	}

	@EventHandler
	public void removePotionEffect(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : GetPlayers(true))
			player.removePotionEffect(PotionEffectType.WATER_BREATHING);
	}

	@EventHandler
	public void skeletonDamage(CustomDamageEvent event)
	{
		for (PlayerCopyWither copy : _doubles.values())
		{
			if (copy.GetEntity().equals(event.GetDamageeEntity()))
			{
				event.SetCancelled("Runner Copy Cancel");
				break;
			}
		}
	}

	@EventHandler
	public void skeletonCombust(EntityCombustEvent event)
	{
		for (PlayerCopyWither copy : _doubles.values())
		{
			if (copy.GetEntity().equals(event.getEntity()))
			{
				event.setCancelled(true);
				break;
			}
		}
	}
}
