package nautilus.game.arcade.game.games.evolution;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.combat.CombatComponent;
import mineplex.minecraft.game.core.combat.CombatManager.AttackReason;
import mineplex.minecraft.game.core.combat.DeathMessageType;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.minecraft.game.core.condition.ConditionActive;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.events.PlayerPrepareTeleportEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.SoloGame;
import nautilus.game.arcade.game.games.evolution.events.EvolutionAbilityUseEvent;
import nautilus.game.arcade.game.games.evolution.events.EvolutionAttemptingTickEvent;
import nautilus.game.arcade.game.games.evolution.events.EvolutionBeginEvent;
import nautilus.game.arcade.game.games.evolution.events.EvolutionEndEvent;
import nautilus.game.arcade.game.games.evolution.events.EvolutionPostEvolveEvent;
import nautilus.game.arcade.game.games.evolution.evolve.EvolveManager;
import nautilus.game.arcade.game.games.evolution.kits.KitAbility;
import nautilus.game.arcade.game.games.evolution.kits.KitEvolveSpeed;
import nautilus.game.arcade.game.games.evolution.kits.KitHealth;
import nautilus.game.arcade.game.games.evolution.mobs.KitBlaze;
import nautilus.game.arcade.game.games.evolution.mobs.KitChicken;
import nautilus.game.arcade.game.games.evolution.mobs.KitCreeper;
import nautilus.game.arcade.game.games.evolution.mobs.KitGolem;
import nautilus.game.arcade.game.games.evolution.mobs.KitSlime;
import nautilus.game.arcade.game.games.evolution.mobs.KitSpider;
import nautilus.game.arcade.game.games.evolution.trackers.EvoWinWithoutDyingTracker;
import nautilus.game.arcade.game.games.evolution.trackers.KillsWhileEvolvingTracker;
import nautilus.game.arcade.game.games.evolution.trackers.NoAbilityTracker;
import nautilus.game.arcade.game.games.evolution.trackers.NoDamageWhileEvolvingTracker;
import nautilus.game.arcade.game.games.evolution.trackers.NoMeleeTracker;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.stats.KillFastStatTracker;

public class Evolution extends SoloGame
{
	/**
	 * @author Mysticate
	 * Reworked from Original Code.
	 */
	
	private EvolveManager _evolve;
	
	private ArrayList<EvoKit> _mobKits = new ArrayList<EvoKit>();
	private ArrayList<Kit> _kits = new ArrayList<Kit>();

	private NautHashMap<String, EvoToken> _tokens = new NautHashMap<String, EvoToken>();
	
	private NautHashMap<Location, SimpleEntry<Location, Location>> _evoPlatforms = new NautHashMap<Location, SimpleEntry<Location, Location>>();
	private ArrayList<Location> _spawns = new ArrayList<Location>();
	
	private ArrayList<Player> _evolutionsAvailable = new ArrayList<Player>();
	private NautHashMap<String, Float> _chargingExp = new NautHashMap<String, Float>();
		
	@SuppressWarnings("unchecked")
	public Evolution(ArcadeManager manager) 
	{
		super(manager, GameType.Evolution, new EvoKit[0], new String[0]);
//		 
//		//Custom kit stuff to make other things easiet
		_mobKits.add(new KitGolem(manager));
		_mobKits.add(new KitBlaze(manager));
		_mobKits.add(new KitSpider(manager));
		_mobKits.add(new KitCreeper(manager));
		_mobKits.add(new KitSlime(manager));
//		_mobKits.add(new KitEnderman(manager));
//		_mobKits.add(new KitSnowman(manager));
//		_mobKits.add(new KitWolf(manager));
		_mobKits.add(new KitChicken(manager));
//		_mobKits.add(new KitSkeleton(manager));
		
		_kits.add(new KitAbility(Manager));
		_kits.add(new KitEvolveSpeed(Manager));
		_kits.add(new KitHealth(Manager));
		
		ArrayList<Kit> allKits = new ArrayList<Kit>();
		allKits.addAll(_mobKits);
		allKits.addAll(_kits);
		
		setKits(allKits.toArray(new Kit[0]));

		_gameDesc = new String[]
				{
				"You can evolve every kill.",
				"Each evolution has unique skills.",
				"First to get through " + _mobKits.size() + " evolutions wins!"
				};
		
		DamageTeamSelf = true;
		
		HungerSet = 20;
		
		DeathOut = false;
		
		PrepareFreeze = false;
		
		GemKillDeathRespawn = 2;
		GemAssistDeathRespawn = .5;

		new CompassModule()
				.setGiveCompassToAlive(true)
				.setGiveCompass(false)
				.register(this);
		
		DeathSpectateSecs = 4.0;

		CreatureAllow = false;
		InventoryClick = false;
		
		InventoryOpenBlock = false;

		SplitKitXP = true;
		
		Manager.GetDamage().GetCombatManager().setUseWeaponName(AttackReason.Attack);
		
		registerStatTrackers(
				new EvoWinWithoutDyingTracker(this),
				new NoDamageWhileEvolvingTracker(this),
				new KillFastStatTracker(this, 3, 5, "Rampage"),
				new NoAbilityTracker(this),
				new NoMeleeTracker(this),
				new KillsWhileEvolvingTracker(this)
		);

		registerChatStats(
				Kills,
				Deaths,
				KDRatio,
				BlankLine,
				Assists,
				DamageDealt,
				DamageTaken
		);

		StrictAntiHack = true;
	}

	public EvolveManager getEvolve()
	{
		return _evolve;
	}
	
	@Override
	public void ParseData()
	{
		for (Location platform : WorldData.GetDataLocs("RED"))
		{
			platform.getBlock().setType(Material.AIR);
			
			for (Location viewing : WorldData.GetDataLocs("GREEN"))
			{				
				viewing.getBlock().setType(Material.AIR);
				
				Iterator<Location> iS = WorldData.GetDataLocs("YELLOW").iterator();
				while (iS.hasNext())
				{
					Location store = iS.next();
					
					store.getBlock().setType(Material.AIR);
					
					if (UtilMath.offset(store, platform) > 8 || UtilMath.offset(platform, viewing) > 8)
						continue;
					
					iS.remove();
					_evoPlatforms.put(platform, new SimpleEntry<Location, Location>(loadAngle(platform, viewing), store));
				}
			}
		}
		
		for (GameTeam team : GetTeamList())
		{
			_spawns.addAll(team.GetSpawns());
		}
	}
	
	private Location loadAngle(Location platform, Location viewing)
	{
		Vector b = UtilAlg.getTrajectory(viewing, platform.clone().subtract(0, 1.1, 0)).normalize();
		
		viewing.setPitch(UtilAlg.GetPitch(b));
		viewing.setYaw(UtilAlg.GetYaw(b));
		
		return viewing;
	}
	
	//Double kit
	@EventHandler(priority = EventPriority.MONITOR)
	public void storeTokens(PlayerPrepareTeleportEvent event)
	{
		_tokens.put(event.GetPlayer().getName(), new EvoToken(event.GetPlayer(), GetKit(event.GetPlayer()) == null ? _kits.get(0) : GetKit(event.GetPlayer())));
		
		upgradeKit(event.GetPlayer(), false);
	}
	
	@EventHandler
	public void removeToken(PlayerQuitEvent event)
	{
		_tokens.remove(event.getPlayer().getName());
	}
	
//	//Double Kit
//	@EventHandler(priority = EventPriority.MONITOR)
//	public void storeTokens(GameStateChangeEvent event)
//	{
//		if (event.GetState() != GameState.Prepare)
//			return;
//
//		for (Player player : GetPlayers(true))
//		{
//			_tokens.put(player.getName(), new EvoToken(player, GetKit(player) == null ? _kits.get(0) : GetKit(player)));
//			
//			upgradeKit(player, false);
//		}
//	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void showKit(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
			return;
		
		for (Player player : GetPlayers(true))
		{
			Kit kit = GetKit(player);
			
			if (!(kit instanceof EvoKit))
				continue;
			
			((EvoKit) kit).upgradeGive(player);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void endNoEvolve(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
			return;
		
		if (_evoPlatforms.size() <= 0)
		{
			Announce(C.cWhite + C.Bold + GetName() + " ended, map not set up properly!");
			SetState(GameState.Dead);
			return;
		}
		
		_evolve = new EvolveManager(this, _evoPlatforms);
	}
	
	public void upgradeKit(Player player, boolean give)
	{	
		if (!IsAlive(player))
			return;

		EvoKit newKit = _mobKits.get(getScore(player));

		_tokens.get(player.getName()).SupplementKit.ApplyKit(player);
		SetKit(player, newKit, false);

		if (give)
		{
			newKit.upgradeGive(player);
		}
	}
	
	@Override
	public boolean HasKit(Player player, Kit kit)
	{
		if (super.HasKit(player, kit))
			return true;
		
		if (!_tokens.containsKey(player.getName()))
			return false;
		
		return _tokens.get(player.getName()).SupplementKit.equals(kit);
	}
	
	@EventHandler
	public void startEvolve(EvolutionBeginEvent event)
	{
		Player player = event.getPlayer();
		
		Recharge.Instance.Get(player).clear();
				
		player.setSprinting(false);
		player.setSneaking(false);

		player.setHealth(player.getMaxHealth());

		player.setFireTicks(0);
		player.setFallDistance(0);
		
		player.eject();
		player.leaveVehicle();

		((CraftPlayer) player).getHandle().o(0);
				
		//Freeze
		Manager.GetCondition().Factory().Cloak("Evolving", player, null, 10, false, false);
		
		((Player) player).playSound(player.getLocation(), Sound.ORB_PICKUP, 1f, 1.25f);
	}

	@EventHandler
	public void stopEvolve(EvolutionEndEvent event)
	{
		if (Manager.GetCondition().GetActiveConditions().containsKey(event.getPlayer()))
		{
			for (ConditionActive cond : Manager.GetCondition().GetActiveConditions().get(event.getPlayer()))
			{
				if (cond.GetCondition().GetType() == ConditionType.CLOAK)
					continue;
				
				cond.GetCondition().Expire();
			}
		}
		
		if (increaseScore(event.getPlayer()) >= _mobKits.size())
		{
			ScoreboardUpdate(new UpdateEvent(UpdateType.FAST));

			event.setCancelled(true);
			
			End();
		}
		else
		{
			upgradeKit(event.getPlayer(), true);
			event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.LEVEL_UP, 1, 0.6F);
		}
	}
	
	@EventHandler
	public void endEvolve(final EvolutionPostEvolveEvent event)
	{				
		//Make sure they're not invis when they're done evolving
		while (Manager.GetCondition().IsCloaked(event.getPlayer()))
		{
			Manager.GetCondition().GetActiveCondition(event.getPlayer(), ConditionType.CLOAK).Expire();
		}
		
		if (getScore(event.getPlayer()) >= _mobKits.size())
		{
			End();
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onDamage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;
		
		if (!IsLive())
			return;
		
		if (!IsAlive(event.GetDamageeEntity()))
			return;
		
		if (!_chargingExp.containsKey(event.GetDamageePlayer().getName()))
			return;
		
		_chargingExp.put(event.GetDamageePlayer().getName(), (float) Math.max(0, _chargingExp.get(event.GetDamageePlayer().getName()) * .92));
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent event)
	{
		if (!IsLive())
			return;
		
		if (!IsAlive(event.getEntity()))
			return;
		
		if (_evolutionsAvailable.contains(event.getEntity()))
		{
			_evolutionsAvailable.remove(event.getEntity());
		}
		
		if (_chargingExp.containsKey(event.getEntity().getName()))
		{
			_chargingExp.remove(event.getEntity().getName());
		}
		
		if (hasEvolvingEffects(event.getEntity()))
		{
			removeEvolvingEffects(event.getEntity());
		}
		
		if (_tokens.containsKey(event.getEntity().getName()))
		{
			_tokens.get(event.getEntity().getName()).LifeKills = 0;
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onKill(CombatDeathEvent event)
	{
		event.SetBroadcastType(DeathMessageType.Simple);
		
		if (!IsLive())
			return;

		CombatComponent damager = event.GetLog().GetKiller();
		if (damager == null)
			return;
			
		if (!damager.IsPlayer())
			return;

		Player player = UtilPlayer.searchExact(damager.GetName());

		if (player == null || !player.isOnline())
			return;

		if (!IsAlive(player))
			return;

		if (UtilPlayer.isSpectator(player))
			return;
		
		if (((Player) event.GetEvent().getEntity()) == player)
			return;

		if (!_evolutionsAvailable.contains(player))
		{
			_evolutionsAvailable.add(player);
			
			if (_chargingExp.containsKey(player.getName()))
				_chargingExp.remove(player.getName());
		}
		
		if (_tokens.containsKey(player.getName()))
		{
			EvoToken token = _tokens.get(player.getName());
			token.LifeKills++;
			
			UtilPlayer.message(player, "");
			UtilPlayer.message(player, F.main("Game", F.elem(token.LifeKills + " Kill" + (token.LifeKills == 1 ? "" : "s"))  + C.cWhite + " - " + C.cGreen + Math.max(1.0, 6.0 - (1.5 * (token.LifeKills - 1))) + "s evolve speed" + (token.SupplementKit instanceof KitEvolveSpeed ? C.cGreen + " - 30% (Quick Evolver)" : "")));
			UtilPlayer.message(player, "");
		}
		
		//Buffs
		Manager.GetCondition().Factory().Speed("Kill", player, null, 3, 0, true, false, false);
		UtilPlayer.health(player, player.getMaxHealth() / 2);
	}
	
	@Override
	public void RespawnPlayer(final Player player)
	{
		player.eject();
		player.teleport(getSpawn());

		Manager.Clear(player);

		//Re-Give Kit
		Manager.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(Manager.getPlugin(), new Runnable()
		{
			public void run()
			{				
				if (_tokens.containsKey(player.getName()))
					_tokens.get(player.getName()).SupplementKit.ApplyKit(player);
				
				GetKit(player).ApplyKit(player);
			}
		}, 0);
	}
	
	@EventHandler
	public void onEgg(PlayerInteractEvent event)
	{
		if (!IsLive())
			return;
		
		if (!UtilEvent.isAction(event, ActionType.R_BLOCK) && !UtilEvent.isAction(event, ActionType.L_BLOCK))
			return;
		
		if (event.getClickedBlock().getType() == Material.DRAGON_EGG)
		{
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler
	public void updateAvailableEvolutions(UpdateEvent event)
	{	
		if (!IsLive())
			return;
		
		if (event.getType() != UpdateType.TICK)
			return;
		
		Iterator<Player> iterator = _evolutionsAvailable.iterator(); 
		while (iterator.hasNext())
		{
			Player player = iterator.next();
			
			if (player == null || !player.isOnline())
			{
				iterator.remove();
				continue;
			}
			
			if (!IsAlive(player))
			{
				iterator.remove();
				continue;
			}

			if (player.isSneaking())
			{
				if (player.getLocation().getBlock().isLiquid())
				{
					if (Recharge.Instance.use(player, "No Evolve Water Message", 2000, false, false))
					{
						UtilPlayer.message(player, F.main("Game", "You cannot evolve in water!"));
					}
				}
				else
				{
					if (onEvolveAttempting(player))
					{
						iterator.remove();
					}
					continue;
				}
			}

			if (_chargingExp.containsKey(player.getName()))
				_chargingExp.remove(player.getName());
			
			if (hasEvolvingEffects(player))
				removeEvolvingEffects(player);
			
			if (Recharge.Instance.use(player, "Evolve Available", 2000, false, false))
			{
				UtilTextMiddle.display("", C.cGreen + C.Bold + "Hold Crouch to Evolve", 0, 60, 20, player);
			}
		}
	}
	
	public boolean isAttemptingEvolve(Player player)
	{
		return _chargingExp.containsKey(player.getName());
	}
	
	//Boolean remove
	private boolean onEvolveAttempting(Player player)
	{	
		if (!_chargingExp.containsKey(player.getName()))
			_chargingExp.put(player.getName(), 0F);
		
		float exp = _chargingExp.get(player.getName());
		
		if (exp >= .9999F)
		{			
			
			UtilTextMiddle.display("", "", player);
			
			_chargingExp.remove(player.getName());
			
			if (hasEvolvingEffects(player))
				removeEvolvingEffects(player);

			EvoToken token = _tokens.get(player.getName());

			token.LifeKills = 0;
			
			EvoKit from = (EvoKit) _mobKits.get(token.Level);
			EvoKit to = (EvoKit) _mobKits.get(token.Level + 1 >= _mobKits.size() ? token.Level : token.Level + 1); //Account for the score increase after evolve

			_evolve.addEvolve(Manager.getHologramManager(), player, from, to);
			
			player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 1);
			return true;
		}
		
		Manager.GetCondition().EndCondition(player, ConditionType.SPEED, "Kill");
		
		if (!hasEvolvingEffects(player))
			addEvolvingEffects(player);

		int kills = _tokens.get(player.getName()).LifeKills;
		
		if (UtilEnt.isGrounded(player) && player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.DRAGON_EGG)
		{
			EvolutionAttemptingTickEvent tickEvent = new EvolutionAttemptingTickEvent(player, (float) Math.min(.022, .012 + (kills <= 0 ? 0 : (.003 * (kills - 1)))));
			Bukkit.getPluginManager().callEvent(tickEvent);
						
			_chargingExp.put(player.getName(), (float) Math.min(exp + tickEvent.getProgress(), .9999F));
		}
		
		UtilTextMiddle.display("", UtilTextMiddle.progress(_chargingExp.get(player.getName())).trim(), player);
		player.playSound(player.getLocation(), Sound.NOTE_BASS, 1, _chargingExp.get(player.getName()) + 1);
		
		UtilParticle.PlayParticle(ParticleType.HAPPY_VILLAGER, player.getLocation().clone().add(0, 1, 0), 0.4F, 0.56F, 0.4F, 0, 3, ViewDist.NORMAL, UtilServer.getPlayers());	
		return false;
	}
	
	private boolean hasEvolvingEffects(Player player)
	{
		if (!Manager.GetCondition().GetActiveConditions().containsKey(player))
			return false;
		
		for (ConditionActive active : Manager.GetCondition().GetActiveConditions().get(player))
		{
			if (!active.GetCondition().GetReason().equalsIgnoreCase("Charging"))
				continue;
			
			return true;
		}
		
		return false;
	}
	
	private void removeEvolvingEffects(Player player)
	{
		if (!Manager.GetCondition().GetActiveConditions().containsKey(player))
			return;
		
		for (ConditionActive active : Manager.GetCondition().GetActiveConditions().get(player))
		{
			if (!active.GetCondition().GetReason().equalsIgnoreCase("Charging"))
				continue;
			
			active.GetCondition().Expire();
		}
	}
	
	private void addEvolvingEffects(Player player)
	{
		Manager.GetCondition().Factory().Jump("Charging", player, null, 20, 128, false, false, false);
		Manager.GetCondition().Factory().Slow("Charging", player, null, 20, 1, false, false, false, false);
	}
	
	@EventHandler
	public void onEvolveAttemptingAbility(EvolutionAbilityUseEvent event)
	{
		if (!IsLive())
			return;
		
		if (_chargingExp.containsKey(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	private int increaseScore(Player player)
	{
		if (!IsAlive(player))
			return 0;
		
		if (!_tokens.containsKey(player.getName()))
			return 0;
			
		_tokens.get(player.getName()).Level++;
		return getScore(player);
	}

	public int getScore(Player player)
	{
		if (!IsPlaying(player))
			return 0;

		try
		{
			return _tokens.get(player.getName()).Level;
		}
		catch (NullPointerException ex)
		{
			return 0;
		}
	}
	
	@EventHandler
	public void onSlot(PlayerItemHeldEvent event)
	{
		if (!Manager.GetGame().InProgress())
			return;
		
		if (!IsAlive(event.getPlayer()))
			return;
		
		if (UtilPlayer.isSpectator(event.getPlayer()))
			return;
		
		if (event.getNewSlot() != 0)
		{
			event.setCancelled(true);
		}
	}
	
	public Location getSpawn()
	{
		ArrayList<Location> allPlayers = new ArrayList<Location>();
		
		for (Player cur : GetPlayers(true))
		{
			if (UtilPlayer.isSpectator(cur))
				continue;
			
			if (_evolve.isEvolving(cur))
			{
				allPlayers.add(_evolve.getEvolve(cur).getEggLocation());
			}
			else
			{
				allPlayers.add(cur.getLocation());
			}
		}
		
		return UtilAlg.getLocationAwayFromOtherLocations(_spawns, allPlayers);		
	}
	
	@EventHandler
	public void onCombust(EntityCombustEvent event)
	{
		event.setCancelled(true);
	}
	
	private void End()
	{
		ArrayList<EvoToken> tokens = new ArrayList<EvoToken>(_tokens.values());
		Collections.sort(tokens);

		List<Player> players = new ArrayList<Player>();	

		for (int i = 0 ; i < tokens.size() ; i++)
		{
			Player cur = tokens.get(i).Player;
			if (!cur.isOnline())
				continue;
			
			players.add(cur);
		}
		
		//Award Gems
		if (tokens.size() >= 1)
			AddGems(players.get(0), 20, "1st Place", false, false);

		if (tokens.size() >= 2)
			AddGems(players.get(1), 15, "2nd Place", false, false);

		if (tokens.size() >= 3)
			AddGems(players.get(2), 10, "3rd Place", false, false);

		//Participation
		for (Player player : GetPlayers(false))
			if (player.isOnline())
				AddGems(player, 10, "Participation", false, false);
		
//		_tokens.clear();
		_evoPlatforms.clear();
		_evolutionsAvailable.clear();
		
		_evolve.end();
		
		AnnounceEnd(players);
		SetState(GameState.End);
	}
	
	@Override
	public List<Player> getWinners()
	{
		if (GetState() != GameState.End)
			return null;
		
		ArrayList<EvoToken> tokens = new ArrayList<EvoToken>(_tokens.values());
		Collections.sort(tokens);

		if (tokens.size() < 1)
			return null;
		
		return Arrays.asList(tokens.get(0).Player);
	}
	
	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (!InProgress())
			return;
		
		if (event.getType() != UpdateType.FAST)
			return;

		Scoreboard.reset();
		
		Scoreboard.writeNewLine();
		
		Scoreboard.write(C.cYellow + C.Bold + "First to " + _mobKits.size());
		
		Scoreboard.writeNewLine();

		Scoreboard.writeGroup(GetPlayers(true), player -> Pair.create(C.cGreen + player.getName(), getScore(player)), true);
						
		Scoreboard.draw();
	}
}
