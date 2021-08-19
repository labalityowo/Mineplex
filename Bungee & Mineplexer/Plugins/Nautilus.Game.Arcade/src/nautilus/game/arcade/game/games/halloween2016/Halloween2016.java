package nautilus.game.arcade.game.games.halloween2016;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.Managers;
import mineplex.core.common.Pair;
import mineplex.core.common.animation.AnimationPoint;
import mineplex.core.common.animation.Animator;
import mineplex.core.common.animation.AnimatorFactory;
import mineplex.core.common.block.schematic.Schematic;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.pet.PetType;
import mineplex.core.reward.rewards.PetReward;
import mineplex.core.treasure.reward.RewardRarity;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.visibility.VisibilityManager;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.halloween.Halloween;
import nautilus.game.arcade.game.games.halloween.creatures.CreatureBase;
import nautilus.game.arcade.game.games.halloween.kits.KitFinn;
import nautilus.game.arcade.game.games.halloween.kits.KitRobinHood;
import nautilus.game.arcade.game.games.halloween.kits.KitThor;
import nautilus.game.arcade.game.games.halloween2016.creatures.MobPumpling;
import nautilus.game.arcade.game.games.halloween2016.tutorial.TutorialHalloween2016;
import nautilus.game.arcade.game.games.halloween2016.wave.Wave1;
import nautilus.game.arcade.game.games.halloween2016.wave.Wave2;
import nautilus.game.arcade.game.games.halloween2016.wave.Wave3;
import nautilus.game.arcade.game.games.halloween2016.wave.Wave4;
import nautilus.game.arcade.game.games.halloween2016.wave.Wave5;
import nautilus.game.arcade.game.games.halloween2016.wave.WaveBoss;
import nautilus.game.arcade.game.games.halloween2016.wave.WaveVictory;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;

public class Halloween2016 extends Halloween
{
	
	private List<PumpkinPlant> _pumpkins = new ArrayList<>();
	private Crypt _crypt;
	
	private Animator _introAnimator;
	
	private int _maxPumplings = 30;
	private int _maxNonPumplings = 65;
	
	private double _mobCapMultiplier = 1;
	private int _defaultMaxPlayerCount = 16;
	
	private List<MobPumpling> _pumplings = new ArrayList<>();
	private List<CreatureBase<?>> _nonPumplings = new ArrayList<>();
	
	private List<List<Location>> _lanes = new ArrayList<>();
	
	private Location _cryptView;
	
	private Location _lockAllPlayers = null;
	private Map<Player, Pair<Location, GameMode>> _playerPreLockData = new HashMap<>();
	
	private static boolean DO_TUTORIALS = true;

	public Halloween2016(ArcadeManager manager)
	{
		super(manager, GameType.Halloween2016,
				new Kit[]
						{
								new KitFinn(manager),
								new KitRobinHood(manager),
								new KitThor(manager)
						},
						new String[]
								{
				"Do not die.",
				"Work as a team!",
				"Defeat the waves of monsters",
				"Kill the Pumpkin Prince",
				"Destroy pumpkins by hitting them 3 times",
				"Protect the crypt!"
								}
		);
		
		_help = new String[] 
				{
				C.cGreen + "Giants one hit kill you! Stay away!!!",
				C.cAqua + "Work together with your teammates.",
				C.cGreen + "Each kit gives a buff to nearby allies.",
				C.cAqua + "Kill monsters to keep their numbers down.",
				C.cGreen + "Giants instantly destroy the crypt! Kill them quickly!",
				C.cAqua + "Defend your teammates from monsters.",
				C.cGreen + "Zombies, Giants and Spiders get faster over time.",
				C.cAqua + "Stick together to survive.",
				C.cGreen + "The Pumpkin Prince gets harder over time!",
				C.cAqua + "Protect the crypt to not lose the game!",
				C.cGreen + "Pumplings spawn from pumpkins. Hit the pumpkins 3 times to prevent it from spawning.",
				};
		
		_updateCreatureMoveRate = UpdateType.TICK;
		EnableTutorials = DO_TUTORIALS;
		
		doVoices = false;
	}
	
	public void setObjective(String objective)
	{
		Objective = objective;
		Announce(F.main("Objective", C.cYellow + C.Bold + objective));
	}
	
	public void unlockAllPlayers()
	{
		if (_lockAllPlayers == null) return;
		
		_lockAllPlayers = null;
		VisibilityManager vm = Managers.require(VisibilityManager.class);
		for (Entry<Player, Pair<Location, GameMode>> e : _playerPreLockData.entrySet())
		{
			e.getKey().teleport(e.getValue().getLeft());
			e.getKey().setGameMode(e.getValue().getRight());
			if (IsAlive(e.getKey()))
			{
				Bukkit.getOnlinePlayers().forEach(p -> vm.showPlayer(p, e.getKey(), "Halloween 2016 Lock"));
			}
		}
		_playerPreLockData.clear();
		
		Manager.getCosmeticManager().setHideParticles(false);
	}
	
	public void lockAllPlayers(Location loc)
	{
		unlockAllPlayers();
		
		_lockAllPlayers = loc;
		
		Manager.getCosmeticManager().setHideParticles(true);
	}
	
	@EventHandler
	public void lockPlayerTask(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK) return;
		
		if (_lockAllPlayers != null)
		{
			VisibilityManager vm = Managers.require(VisibilityManager.class);
			for (Player p : Bukkit.getOnlinePlayers())
			{
				if (!_playerPreLockData.containsKey(p))
				{
					_playerPreLockData.put(p, Pair.create(p.getLocation(), p.getGameMode()));
					p.setGameMode(GameMode.SPECTATOR);
				}
				p.teleport(_lockAllPlayers);
				if (IsAlive(p))
				{
					Bukkit.getOnlinePlayers().forEach(pl -> vm.hidePlayer(pl, p, "Halloween 2016 Lock"));
				}
			}
		}
	}
	
	@EventHandler
	@Override
	public void onGameStart(GameStateChangeEvent event)
	{
		super.onGameStart(event);
		
		if(event.GetState() != GameState.Live) return;
		
		_mobCapMultiplier = (double) GetPlayers(true).size() / (double) _defaultMaxPlayerCount;
		if(_mobCapMultiplier < 0.5)
		{
			_mobCapMultiplier = 0.5;
		}
		
		_maxNonPumplings *= _mobCapMultiplier;
		_maxPumplings *= _mobCapMultiplier;
		_maxMobs *= _mobCapMultiplier;
	}
	
	public int getMaxPumplings()
	{
		return _maxPumplings;
	}
	
	public void setMaxPumplings(int maxPumplings)
	{
		_maxPumplings = maxPumplings;
	}
	
	@Override
	public void ParseData()
	{
		List<Schematic> schematics = new ArrayList<>();
		Location doorSchematicLocation = getDoorSchematicLocation();
		_crypt = new Crypt(this, getDoorSchematicLocation(), schematics);
		
		for(Location loc : getPumkinSpawns())
		{
			_pumpkins.add(new PumpkinPlant(this, loc));
		}
		
		_lanes.add(getBackLane());
		_lanes.add(getLeftLane());
		_lanes.add(getRightLane());
		_lanes.add(getMainLane());
		_lanes.add(getGraveLane());
		
		_waves = new ArrayList<>();
		_waves.add(new Wave1(this));
		_waves.add(new Wave2(this));
		_waves.add(new Wave3(this));
		_waves.add(new Wave4(this));
		_waves.add(new Wave5(this));
		_waves.add(new WaveBoss(this));
		_waves.add(new WaveVictory(this, getMobSpawns()));
		
		_spawns = new ArrayList<>();
		
		
		CreatureAllowOverride = true;
		ArmorStand bat = doorSchematicLocation.getWorld().spawn(doorSchematicLocation, ArmorStand.class);
		CreatureAllowOverride = false;
		UtilEnt.vegetate(bat, true);
		UtilEnt.setAI(bat, false);
		UtilEnt.setTickWhenFarAway(bat, true);
		bat.setRemoveWhenFarAway(false);
		bat.setVisible(false);
		
		_introAnimator = new Animator(Manager.getPlugin())
		{
			private int _tick = 0;
			@Override
			protected void tick(Location loc)
			{
				if(loc == null) return;

				for(Player p : Halloween2016.this.GetPlayers(false))
				{
					if(p.getGameMode() != GameMode.SPECTATOR) p.setGameMode(GameMode.SPECTATOR);
					if(!p.getAllowFlight()) p.setAllowFlight(true);
					if(!p.isFlying()) p.setFlying(true);
					
					if(_tick%3 == 0) p.teleport(loc);
				}
				
				_tick++;
			}
			
			@Override
			protected void finish(Location loc)
			{}
		};
		
		_cryptView = WorldData.GetCustomLocs("CryptView").get(0);
		_cryptView.setDirection(getClosest(_cryptView, "PINK").subtract(_cryptView).toVector());
		
		AnimatorFactory factory = new AnimatorFactory();
		
		double lastEntry = 0;
		double firstEntry = Double.MAX_VALUE;
		Location first = null;
		
		for(Entry<String, List<Location>> point : WorldData.GetAllCustomLocs().entrySet())
		{
			String[] args = point.getKey().split(" ");
			if(args.length < 2) continue;
			if(args[0].equals("Intro"))
			{
				try
				{
					double sec = Double.parseDouble(args[1]);
					double delay = 0;
					try
					{
						delay = Double.parseDouble(args[2]);
					} 
					catch(Exception e1) {}
					int tick = (int) (sec*20);
					int tickdelay = (int) (delay*20);
					
					Location loc = point.getValue().get(0);
					Location lookingAt = getClosest(loc, "PINK");
					
					loc.setDirection(lookingAt.subtract(loc).toVector());
					
					factory.addLocation(loc, tick);
					if(tickdelay > 0) factory.addLocation(loc, tickdelay);
					
					if(sec > lastEntry) lastEntry = sec;
					if(delay > lastEntry) lastEntry = delay;
					
					if(sec < firstEntry)
					{
						firstEntry = sec;
						first = loc.clone();
					}
				}
				catch(Exception e2) {}
			}
		}
		
		System.out.print("Scanned " + WorldData.GetAllCustomLocs().entrySet().size() + " data points");
		
		List<AnimationPoint> animation = factory.getBuildList(first);
		_introAnimator.addPoints(animation);
		
		System.out.println("Loaded intro animation with " + _introAnimator.getSet().size() + " | " + animation.size() + " points and duration of " + lastEntry + "s");
		
		GameTeam team = GetTeamList().get(0);
		team.setTutorial(new TutorialHalloween2016(this, _introAnimator, team, first, (int) (lastEntry * 20)));
	}
	
	public Location getInfrontOfCrypt()
	{
		return WorldData.GetCustomLocs("PumpkinKing Win").get(0).clone();
	}
	
	public Location getPrinceTargetInfrontOfCrypt()
	{
		return WorldData.GetCustomLocs("PrinceTarget").get(0).clone();
	}
	
	public Location getPrinceSpawn()
	{
		return WorldData.GetCustomLocs("PrinceSpawn").get(0).clone();
	}
	
	
	public Location getClosest(Location loc, String dataSet)
	{
		Location c = null;
		double dist = 0;
		for(Location l : WorldData.GetDataLocs(dataSet))
		{
			double ldist = loc.distanceSquared(l);
			if(c == null || ldist <= dist)
			{
				c = l;
				dist = ldist;
			}
		}
		if(c == null) return null;
		return c.clone();
	}
	
	public Crypt getCrypt()
	{
		return _crypt;
	}
	
	public Location getGiantSpawn()
	{
		return getMainLane().get(UtilMath.r(getMainLane().size()));
	}
	
	public List<Location> getMainLane()
	{
		return WorldData.GetDataLocs("RED");
	}
	
	public List<Location> getLeftLane()
	{
		return WorldData.GetDataLocs("MAGENTA");
	}
	
	public List<Location> getBackLane()
	{
		return WorldData.GetDataLocs("LIGHT_BLUE");
	}
	
	public List<Location> getRightLane()
	{
		return WorldData.GetDataLocs("YELLOW");
	}
	
	public List<Location> getGraveLane()
	{
		return WorldData.GetDataLocs("MAGENTA");
	}
	
	public List<Location> getRandomLane()
	{
		List<Location> lane = new ArrayList<>();
		lane.addAll(_lanes.get(UtilMath.r(_lanes.size())));
		return lane;
	}
	
	public List<Location> getMobSpawns()
	{
		List<Location> list = new ArrayList<>();
		for(List<Location> lane : _lanes)
		{
			list.addAll(lane);
		}
		return list;
	}
	
	public List<Location> getPumkinSpawns()
	{
		return new ArrayList<>(WorldData.GetDataLocs("ORANGE"));
	}
	
	public List<Location> getInfrontOfDoorTargets()
	{
		return new ArrayList<>(WorldData.GetDataLocs("BLUE"));
	}
	
	public Location getDoorSchematicLocation()
	{
		return WorldData.GetCustomLocs("Door Schematic Paste").get(0).clone();
	}
	
	@EventHandler
	public void updatePumpkinPlants(UpdateEvent event)
	{
		if(!IsLive()) return;
		
		if(event.getType() != UpdateType.TICK) return;
		
		List<PumpkinPlant> notGrowing = new ArrayList<>();
		int growing = 0;
		for(PumpkinPlant plant : _pumpkins)
		{
			if(plant.isGrowing())
			{
				growing++;
			}
			else
			{
				notGrowing.add(plant);
			}
			plant.tick();
		}
		
		if(!notGrowing.isEmpty() && growing + getPumplings().size() < getMaxPumplings())
		{
			notGrowing.get(UtilMath.r(notGrowing.size())).startGrow();
		}
	}
	
	public Location getCryptView()
	{
		return _cryptView.clone();
	}
	
	
	@Override
	@EventHandler
	public void WaveUpdate(UpdateEvent event)
	{	
		if (event.getType() != UpdateType.TICK)
			return;

		if (!IsLive())
			return;
		
		if(_crypt.isDestroyed() && !(_waves.get(_wave) instanceof WaveBoss) && !(_waves.get(_wave) instanceof WaveVictory))
		{
			for(CreatureBase<?> c : _mobs)
			{
				c.remove();
			}
		
			Announce(F.main("Objective", C.cRed + C.Bold + "Objective failed!"));
			endGame(GetTeam(ChatColor.RED));
			
			return;
		}
		
		super.WaveUpdate(event);
	}
	
	
	@EventHandler
	public void onUpdateCrypt(UpdateEvent event)
	{
		if(event.getType() != UpdateType.TICK) return;
		if(!IsLive()) return;
		if(_crypt.isDestroyed()) return;
		_crypt.updateHealthDisplay();
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event)
	{
		if(event.getAction() != Action.LEFT_CLICK_BLOCK) return;
		if(event.getClickedBlock() == null) return;
		
		Block block = event.getClickedBlock();
		
		for(PumpkinPlant plant : _pumpkins)
		{
			plant.hit(block);
		}
	}
	
	@Override
	public void AddCreature(CreatureBase<?> mob)
	{
		AddCreature(mob, true);
	}
	
	public void AddCreature(CreatureBase<?> mob, boolean cap)
	{
		super.AddCreature(mob);
		if(!cap) return;
		
		if(mob instanceof MobPumpling)
		{
			_pumplings.add((MobPumpling) mob);
		}
		else
		{
			_nonPumplings.add(mob);
		}
	}
	
	public List<MobPumpling> getPumplings()
	{
		return _pumplings;
	}
	
	public List<CreatureBase<?>> getNonPumplings()
	{
		return _nonPumplings;
	}
	
	
	public int getMaxNonPumplings()
	{
		return _maxNonPumplings;
	}
	
	@Override
	public void onRemove(CreatureBase<?> mob)
	{
		_pumplings.remove(mob);
		_nonPumplings.remove(mob);
	}
	
	
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event)
	{
		if(!UtilServer.isTestServer()) return;
		
		
		String[] args = event.getMessage().split(" ");
		boolean orig = event.isCancelled();
		event.setCancelled(true);
		if(args[0].equalsIgnoreCase("/setwave"))
		{
			if(event.getMessage().matches("/setwave [0-9]+"))
			{
				Announce(event.getPlayer().getName() + " set wave to " + args[1], true);
				_wave = Integer.parseInt(args[1]);
			}
			else
			{
				event.getPlayer().sendMessage("Use /setwave #Wave");
			}
		}
		else if(args[0].equalsIgnoreCase("/god"))
		{
			Announce(event.getPlayer().getName() + " made everyone 'gods'", true);
			for(Player p : GetPlayers(false))
			{
				p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 99999, 200, true, false), true);
				p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 99999, 200, true, false), true);
				p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 99999, 200, true, false), true);
			}
		}
		else if(args[0].equalsIgnoreCase("/degod"))
		{
			Announce(event.getPlayer().getName() + " made everyone no longer 'gods'", true);
			for(Player p : GetPlayers(false))
			{
				p.removePotionEffect(PotionEffectType.REGENERATION);
				p.removePotionEffect(PotionEffectType.SATURATION);
				p.removePotionEffect(PotionEffectType.NIGHT_VISION);
			}
		}
		else if(args[0].equals("/tutorial"))
		{
			if(args.length != 2)
			{
				event.getPlayer().sendMessage("Use /tutorial <true/false> - true = tutorial enabled before game");
				return;
			}
			if(args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("on"))
			{
				DO_TUTORIALS = true;
				Announce(event.getPlayer().getName() + " enabled tutorials before games starts");
			}
			else if(args[1].equalsIgnoreCase("false") || args[1].equalsIgnoreCase("off"))
			{
				DO_TUTORIALS = false;
				Announce(event.getPlayer().getName() + " disabled tutorials before games starts");
			}
			else
			{
				event.getPlayer().sendMessage("Use /tutorial <true/false> - true = tutorial enabled before game");
			}
			
			EnableTutorials = DO_TUTORIALS;
		}
		else
		{
			event.setCancelled(orig);
		}
	}
	
	@EventHandler
	public void onDeath(EntityDeathEvent event)
	{
		if(event.getEntity().getKiller() != null)
		{
			AddGems(event.getEntity().getKiller(), 0.2, "Mobs Killed", true, true);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void Clean(GameStateChangeEvent event) 
	{}
		
	@Override
	@EventHandler(priority = EventPriority.MONITOR)
	public void TeamGen(GameStateChangeEvent event) 
	{
		if (event.GetState() != GameState.Live)
			return;

		GetTeamList().add(new GameTeam(this, "Pumpkin Prince", ChatColor.RED, WorldData.GetDataLocs("RED")));
	}
	
//	@Override
//	public void EndCheck()
//	{
//		if (!IsLive())
//			return;
//
//		if (_wave >= _waves.size())
//		{
//			for (Player player : GetPlayers(false))
//			{
//				Manager.GetGame().AddGems(player, 30, "Killing the Pumpkin Prince", false, false);
//				Manager.GetGame().AddGems(player, 10, "Participation", false, false);
//			}
//
//			if (Manager.IsRewardItems())
//			{
//				SetCustomWinLine("You earned the Grim Reaper Pet!");
//
//				for (Player player : GetPlayers(false))
//				{
//					//Prevent game hopping
//					if (!player.isOnline())
//						continue;
//
////					PetReward pr = new PetReward(Manager.getCosmeticManager().getPetManager(), Manager.getInventoryManager(), Manager.GetDonation(), "Grim Reaper", "Grim Reaper", PetType.BLAZE, RewardRarity.OTHER, 0, 0);
////
////					if (pr.canGiveReward(player))
////					{
////						pr.giveReward(null, player, data -> {});
////					}
//				}
//			}
//
//			AnnounceEnd(this.GetTeamList().get(0));
//
//			SetState(GameState.End);
//		}
//
//		else if (GetPlayers(true).size() == 0)
//		{
//			for (Player player : GetPlayers(false))
//			{
//				Manager.GetGame().AddGems(player, 10, "Participation", false, false);
//			}
//
//			AnnounceEnd(this.GetTeamList().get(1));
//
//			SetState(GameState.End);
//		}
//	}

	
	

}
