package nautilus.game.arcade.game.games.dragonescape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import mineplex.core.common.Pair;
import mineplex.core.common.block.BlockData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTextTop;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.events.PlayerKitGiveEvent;
import nautilus.game.arcade.game.SoloGame;
import nautilus.game.arcade.game.games.dragonescape.kits.KitDigger;
import nautilus.game.arcade.game.games.dragonescape.kits.KitDisruptor;
import nautilus.game.arcade.game.games.dragonescape.kits.KitLeaper;
import nautilus.game.arcade.game.games.dragonescape.kits.KitWarper;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.managers.chat.ChatStatData;
import nautilus.game.arcade.stats.DistanceTraveledStatTracker;
import nautilus.game.arcade.stats.ParalympicsStatTracker;
import nautilus.game.arcade.stats.WinMapStatTracker;

public class DragonEscape extends SoloGame
{
	public static class PlayerFinishEvent extends PlayerEvent
	{
		private static final HandlerList HANDLER_LIST = new HandlerList();

		public static HandlerList getHandlerList()
		{
			return HANDLER_LIST;
		}

		PlayerFinishEvent(Player who)
		{
			super(who);
		}

		@Override
		public HandlerList getHandlers()
		{
			return getHandlerList();
		}
	}

	private static final String[] DESCRIPTION =
			{
					C.cYellowB + "Douglas the Dragon" + C.Reset + " is after you!",
					C.cRedB + "RUN!!!!!!!!!!",
					C.cYellow + "Last Player" + C.Reset + " alive wins!"
			};

	private final List<DragonScore> _ranks = new ArrayList<>();
	private final Map<Player, Long> _warpTime = new HashMap<>();

	private Location _dragon;
	private List<Location> _waypoints;

	private DragonEscapeData _dragonData;
	private Player _winner = null;
	private double _speedMult = 1;
	private final Map<BlockData, Player> _tunneled = new HashMap<>();
	private final Map<Player, Location> _safeLocation = new HashMap<>();

	private long _started;
	private long _ended;

	public DragonEscape(ArcadeManager manager)
	{
		super(manager, GameType.DragonEscape, new Kit[]
				{
						new KitLeaper(manager),
						new KitDisruptor(manager),
						new KitWarper(manager),
						new KitDigger(manager),
				}, DESCRIPTION);

		StrictAntiHack = true;
		DamagePvP = false;
		HungerSet = 20;
		BlockPlace = true;
		AllowParticles = false;

		registerStatTrackers(
				new ParalympicsStatTracker(this),
				new WinMapStatTracker(this),
				new DistanceTraveledStatTracker(this, "MarathonRunner")
		);

		registerChatStats(
				new ChatStatData("MarathonRunner", "Distance ran", true),
				BlankLine,
				new ChatStatData("kit", "Kit", true)
		);

		new CompassModule()
				.setGiveCompass(true)
				.setGiveCompassToSpecs(true)
				.setGiveCompassToAlive(false)
				.register(this);
	}

	@Override
	public void ParseData()
	{
		_dragon = WorldData.GetDataLocs("RED").get(0);
		_waypoints = new ArrayList<>();

		//Order Waypoints
		Location last = _dragon;

		while (!WorldData.GetDataLocs("BLACK").isEmpty())
		{
			Location best = null;
			double bestDist = 0;

			//Get Best
			for (Location loc : WorldData.GetDataLocs("BLACK"))
			{
				double dist = UtilMath.offset(loc, last);

				if (best == null || dist < bestDist)
				{
					best = loc;
					bestDist = dist;
				}
			}

			//Ignore Close
			if (bestDist < 3 && WorldData.GetDataLocs("BLACK").size() > 1)
			{
				System.out.println("Ignoring Node");
				WorldData.GetDataLocs("BLACK").remove(best);
				continue;
			}

			_waypoints.add(best);
			WorldData.GetDataLocs("BLACK").remove(best);
			best.subtract(new Vector(0, 1, 0));

			last = best;
		}

		if (!WorldData.GetDataLocs("GREEN").isEmpty())
			_speedMult = WorldData.GetDataLocs("GREEN").get(0).getX() / 100d;

		if (WorldData.MapName.contains("Hell"))
			WorldTimeSet = 16000;

		if (WorldData.MapName.contains("Pirate"))
			WorldWaterDamage = 2;
	}

	@Override
	public List<Player> getWinners()
	{
		if (GetState().ordinal() >= GameState.End.ordinal())
		{
			if (_winner == null)
			{
				return Collections.emptyList();
			}

			return Collections.singletonList(_winner);
		}

		return null;
	}

	@EventHandler
	public void SpawnDragon(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
			return;

		for (Team team : GetScoreboard().getScoreboard().getTeams())
			team.setCanSeeFriendlyInvisibles(true);

		CreatureAllowOverride = true;
		EnderDragon dragon = _dragon.getWorld().spawn(_dragon, EnderDragon.class);
		CreatureAllowOverride = false;

		dragon.setCustomName(ChatColor.YELLOW + C.Bold + "Douglas the Dragon");

		_dragonData = new DragonEscapeData(this, dragon, _waypoints.get(0));
	}

	@EventHandler
	public void invisibility(PlayerKitGiveEvent event)
	{
		event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
	}

	@EventHandler
	public void MoveDragon(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		if (_dragonData == null)
			return;

		_dragonData.Target = _waypoints.get(Math.min(_waypoints.size() - 1, (GetWaypointIndex(_dragonData.Location) + 1)));

		_dragonData.Move();

		Set<Block> blocks = UtilBlock.getInRadius(_dragonData.Location, 10d).keySet();

		Iterator<Block> blockIterator = blocks.iterator();
		while (blockIterator.hasNext())
		{
			Block block = blockIterator.next();

			if (block.isLiquid())
				blockIterator.remove();

			else if (block.getRelative(BlockFace.UP).isLiquid())
				blockIterator.remove();

			else if (WorldData.MapName.contains("Hell") && block.getY() < 30)
				blockIterator.remove();

			else if (WorldData.MapName.contains("Pirate") && (block.getY() < 6))
				blockIterator.remove();
		}

		Manager.GetExplosion().BlockExplosion(blocks, _dragonData.Location, false);
	}

	@EventHandler
	public void UpdateScores(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (!IsLive())
			return;

		if (_dragonData == null)
			return;

		double dragonScore = GetScore(_dragonData.Dragon);

		for (Player player : GetPlayers(true))
		{
			double playerScore = GetScore(player);

			if (SetScore(player, playerScore))
				return;

			if (dragonScore > playerScore)
				player.damage(50);
		}
	}

	public boolean SetScore(Player player, double playerScore)
	{
		//Rank
		for (DragonScore score : _ranks)
		{
			if (score.Player.equals(player))
			{
				//debug
				int preNode = (int) (score.Score / 10000);
				int postNode = (int) (playerScore / 10000);

				//Backwards
				if (preNode - postNode >= 3)
				{
					return false;
				}

				//Shortcut
				if (postNode - preNode >= 3)
				{
					if (!_warpTime.containsKey(score.Player) || UtilTime.elapsed(_warpTime.get(score.Player), 1000))
					{
						Manager.GetDamage().NewDamageEvent(player, null, null, DamageCause.CUSTOM, 500, false, true, true, GetName(), "Cheating");
						return false;
					}
				}

				//Finish
				if (GetWaypointIndex(player.getLocation()) == _waypoints.size() - 1)
				{
					//Only if NEAR end.
					if (UtilMath.offset(player.getLocation(), _waypoints.get(_waypoints.size() - 1)) < 3)
					{
						_winner = player;
						SetCustomWinLine(player.getName() + " reached the end of the course!");

						Bukkit.getPluginManager().callEvent(new PlayerFinishEvent(player));

						return true;
					}
				}

				score.Score = playerScore;
				return false;
			}
		}

		_ranks.add(new DragonScore(player, playerScore));

		return false;
	}

	public double GetScore(Entity ent)
	{
		int index = GetWaypointIndex(ent.getLocation());

		double score = 10000 * index;

		score -= UtilMath.offset(ent.getLocation(), _waypoints.get(Math.min(_waypoints.size() - 1, index + 1)));

		return score;
	}

	public int GetWaypointIndex(Location loc)
	{
		int best = -1;
		double bestDist = 0;

		for (int i = 0; i < _waypoints.size(); i++)
		{
			Location waypoint = _waypoints.get(i);

			double dist = UtilMath.offset(waypoint, loc);

			if (best == -1 || dist < bestDist)
			{
				best = i;
				bestDist = dist;
			}
		}

		return best;
	}

	private void SortScores()
	{
		for (int i = 0; i < _ranks.size(); i++)
		{
			for (int j = _ranks.size() - 1; j > 0; j--)
			{
				if (_ranks.get(j).Score > _ranks.get(j - 1).Score)
				{
					DragonScore temp = _ranks.get(j);
					_ranks.set(j, _ranks.get(j - 1));
					_ranks.set(j - 1, temp);
				}
			}
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

		SortScores();

		Scoreboard.writeNewLine();

		AtomicInteger index = new AtomicInteger(0);

		Scoreboard.writeGroup(_ranks.subList(0, Math.min(_ranks.size(), 14)), score ->
		{
			ChatColor col = ChatColor.GREEN;
			if (!IsAlive(score.Player))
				col = ChatColor.RED;
			return Pair.create(col + score.Player.getName(), index.incrementAndGet());
		}, true);

		Scoreboard.draw();
	}

	@Override
	public Location GetSpectatorLocation()
	{
		if (SpectatorSpawn == null)
		{
			SpectatorSpawn = new Location(WorldData.World, 0, 0, 0);
		}

		Vector vec = new Vector(0, 0, 0);
		double count = 0;

		for (Player player : GetPlayers(true))
		{
			count++;
			vec.add(player.getLocation().toVector());
		}

		if (count == 0)
		{
			count++;
		}

		vec.multiply(1d / count);

		SpectatorSpawn.setX(vec.getX() + 0.5);
		SpectatorSpawn.setY(vec.getY() + 10);
		SpectatorSpawn.setZ(vec.getZ() + 0.5);

		return SpectatorSpawn;
	}

	@Override
	public void EndCheck()
	{
		if (!IsLive())
		{
			return;
		}

		if (GetPlayers(true).isEmpty() || _winner != null)
		{
			SortScores();

			List<Player> places = new ArrayList<>();

			for (DragonScore score : _ranks)
			{
				places.add(score.Player);
			}

			//Announce
			AnnounceEnd(places);

			//Gems
			if (_winner != null)
			{
				AddGems(_winner, 10, "Course Complete", false, false);
			}

			if (places.size() >= 1)
			{
				AddGems(places.get(0), 20, "1st Place", false, false);
			}

			if (places.size() >= 2)
			{
				AddGems(places.get(1), 15, "2nd Place", false, false);
			}

			if (places.size() >= 3)
			{
				AddGems(places.get(2), 10, "3rd Place", false, false);
			}

			for (Player player : GetPlayers(false))
			{
				if (player.isOnline())
				{
					AddGems(player, 10, "Participation", false, false);
				}
			}

			_safeLocation.clear();
			SetState(GameState.End);
		}
	}

	public double GetSpeedMult()
	{
		return _speedMult;
	}

	@EventHandler
	public void Warp(PlayerInteractEvent event)
	{
		if (event.getAction() == Action.PHYSICAL || !IsLive())
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (itemStack == null || itemStack.getType() != Material.ENDER_PEARL)
		{
			return;
		}

		event.setCancelled(true);

		SortScores();

		Player target = null;
		boolean self = false;

		for (int i = _ranks.size() - 1; i >= 0; i--)
		{
			DragonScore score = _ranks.get(i);

			if (score.Player.equals(player))
			{
				self = true;
			}
			else if (self)
			{
				if (IsAlive(score.Player))
				{
					target = score.Player;
					break;
				}
			}
		}

		if (target != null)
		{
			UtilInv.remove(player, Material.ENDER_PEARL, (byte) 0, 1);
			UtilInv.Update(player);

			//Firework
			UtilFirework.playFirework(player.getEyeLocation(), Type.BALL, Color.BLACK, false, false);
			player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_UNFECT, 2f, 2f);

			//Teleport
			player.teleport(_safeLocation.getOrDefault(target, target.getLocation()).add(0, 0.5, 0));
			UtilAction.zeroVelocity(player);

			//Record
			_warpTime.put(player, System.currentTimeMillis());

			//Inform
			UtilPlayer.message(player, F.main("Game", "You warped to " + F.name(target.getName()) + "!"));

			//Effect
			player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_UNFECT, 1f, 1f);

			//Firework
			UtilFirework.playFirework(player.getEyeLocation(), Type.BALL, Color.BLACK, false, false);
			player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_UNFECT, 2f, 2f);
		}
		else
		{
			UtilPlayer.message(player, F.main("Game", "There is no one infront of you!"));
		}
	}

	@EventHandler
	public void Tunneler(BlockDamageEvent event)
	{
		Player player = event.getPlayer();

		if (!IsAlive(player))
			return;

		if (!player.getInventory().contains(Material.DIAMOND_PICKAXE))
			return;

		if (!UtilTime.elapsed(GetStateTime(), 10000))
		{
			UtilPlayer.message(player, F.main("Game", "You cannot dig for " + F.elem(UtilTime.MakeStr(10000 - (System.currentTimeMillis() - GetStateTime())) + ".")));
			return;
		}

		for (Player other : GetPlayers(true))
		{
			if (player.equals(other))
			{
				continue;
			}

			if (UtilMath.offset(event.getBlock().getLocation().add(0.5, 0.5, 0.5), other.getLocation()) < 1.5 || UtilMath.offset(event.getBlock().getLocation().add(0.5, 1, 0.5), other.getLocation()) < 1.5)
			{
				UtilPlayer.message(player, F.main("Game", "You cannot dig near other players."));
				return;
			}
		}

		if (!Recharge.Instance.use(player, "Tunneler", 100, false, false))
			return;

		event.getBlock().getWorld().playEffect(event.getBlock().getLocation(), Effect.STEP_SOUND, event.getBlock().getType());

		player.getInventory().addItem(new ItemStack(event.getBlock().getType()));

		_tunneled.put(new BlockData(event.getBlock()), player);

		Manager.GetBlockRestore().add(event.getBlock(), 0, (byte) 0, 2400);

		UtilInv.remove(player, Material.DIAMOND_PICKAXE, (byte) 0, 1);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void Tunneler(BlockPlaceEvent event)
	{
		if (event.isCancelled())
			return;

		Manager.GetBlockRestore().add(event.getBlock(),
				event.getPlayer().getItemInHand().getType().getId(), event.getPlayer().getItemInHand().getData().getData(),
				event.getBlockReplacedState().getTypeId(), event.getBlockReplacedState().getRawData(), 2400);
	}

	@EventHandler
	public void TunnelerUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Iterator<BlockData> tunnelIterator = _tunneled.keySet().iterator();

		while (tunnelIterator.hasNext())
		{
			BlockData data = tunnelIterator.next();

			if (data.Block.getType() != Material.AIR || UtilTime.elapsed(data.Time, 2400))
			{
				tunnelIterator.remove();
			}
			else
			{
				for (Player other : UtilServer.getPlayers())
				{
					if (!other.equals(_tunneled.get(data)))
					{
						other.sendBlockChange(data.Block.getLocation(), data.Material, data.Data);
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		Block block = event.getClickedBlock();

		if (UtilBlock.usable(block))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void setTimes(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Live)
		{
			_started = System.currentTimeMillis();
		}
		else if (event.GetState() == GameState.End)
		{
			_ended = System.currentTimeMillis();
		}
	}

	@EventHandler
	public void updateTimer(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
		{
			return;
		}

		if (GetState() == GameState.Prepare)
		{
			UtilTextTop.display(F.game("Game time: ") + F.time("0.0 Seconds"), UtilServer.getPlayers());
		}
		else if (GetState() == GameState.Live)
		{
			if (_started == 0)
			{
				return;
			}

			UtilTextTop.display(F.game("Game time: ") + F.time(UtilTime.convertString(System.currentTimeMillis() - _started, 6, TimeUnit.SECONDS)), UtilServer.getPlayers());
		}
		else if (GetState() == GameState.End)
		{
			if (_started == 0 || _ended == 0)
			{
				return;
			}

			UtilTextBottom.display(F.game("Game time: ") + F.time(UtilTime.convertString(_ended - _started, 6, TimeUnit.SECONDS)), UtilServer.getPlayers());
		}
	}

	@EventHandler
	public void updateSafe(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER || !InProgress())
		{
			return;
		}

		for (Player player : GetPlayers(true))
		{
			if (UtilEnt.isGrounded(player))
			{
				_safeLocation.put(player, player.getLocation());
			}
		}
	}
}
