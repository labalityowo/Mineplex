package nautilus.game.arcade.game.games.sheep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTextTop;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.events.PlayerGameRespawnEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.TeamGame;
import nautilus.game.arcade.game.games.sheep.kits.KitArcher;
import nautilus.game.arcade.game.games.sheep.kits.KitBeserker;
import nautilus.game.arcade.game.games.sheep.kits.KitBrute;
import nautilus.game.arcade.game.modules.TeamArmorModule;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.managers.chat.ChatStatData;
import nautilus.game.arcade.stats.SheepDropStatTracker;
import nautilus.game.arcade.stats.SheepThiefStatTracker;
import nautilus.game.arcade.stats.WinWithSheepStatTracker;

public class SheepGame extends TeamGame
{
	public static class SheepStolenEvent extends PlayerEvent
	{
		private static final HandlerList handlers = new HandlerList();

		public static HandlerList getHandlerList()
		{
			return handlers;
		}

		@Override
		public HandlerList getHandlers()
		{
			return getHandlerList();
		}

		public SheepStolenEvent(Player who)
		{
			super(who);
		}
	}

	public static class DropEnemySheepEvent extends PlayerEvent
	{
		private static final HandlerList handlers = new HandlerList();

		public static HandlerList getHandlerList()
		{
			return handlers;
		}

		@Override
		public HandlerList getHandlers()
		{
			return getHandlerList();
		}

		private final Player _enemy;

		public DropEnemySheepEvent(Player who, Player enemy)
		{
			super(who);

			_enemy = enemy;
		}

		public Player getEnemy()
		{
			return _enemy;
		}
	}

	private HashMap<GameTeam, Integer> _teamScore = new HashMap<GameTeam, Integer>();

	private HashMap<GameTeam, ArrayList<Block>> _sheepPens = new HashMap<GameTeam, ArrayList<Block>>();

	private ArrayList<Location> _sheepSpawns;
	private HashMap<Sheep, SheepData> _sheep = new HashMap<Sheep, SheepData>();
	private long _sheepTimer = System.currentTimeMillis();
	private long _sheepDelay = 20000;

	private long _gameTime = 300000;
	private long _gameEndAnnounce = 0;

	public SheepGame(ArcadeManager manager) 
	{
		this(manager, new Kit[]
				{
		new KitBeserker(manager),
		new KitArcher(manager),
		new KitBrute(manager)
				}, GameType.Sheep);

		registerStatTrackers(
				new SheepThiefStatTracker(this),
				new SheepDropStatTracker(this),
				new WinWithSheepStatTracker(this)
		);

		registerChatStats(
				new ChatStatData("AnimalRescue", "Captures", true),
				new ChatStatData("Thief", "Stolen", true),
				BlankLine,
				Kills,
				Deaths,
				KDRatio
		);
	}
	
	public SheepGame(ArcadeManager manager, GameType type) 
	{
		this(manager, new Kit[]
				{
		new KitBeserker(manager),
		new KitArcher(manager),
		new KitBrute(manager)
				}, type);
	}
	
	public SheepGame(ArcadeManager manager, Kit[] kits, GameType type) 
	{
		super(manager, type, kits,

						new String[]
								{
				C.cYellow + "Hold Saddle" + C.cGray + " to " + C.cGreen + "Grab/Hold Sheep",
				"Return Sheep to your Team Pen!",
				"Most sheep at 5 minutes wins!"
								});

		this.DeathOut = false;
		this.DeathSpectateSecs = 6;

		this.HungerSet = 20;

		this.WorldTimeSet = 2000;

		this.PlayerGameMode = GameMode.ADVENTURE;

		new CompassModule()
				.setGiveCompass(true)
				.setGiveCompassToSpecs(true)
				.setGiveCompassToAlive(false)
				.register(this);
		new TeamArmorModule()
				.giveTeamArmor()
				.giveHotbarItem()
				.register(this);
	}

	@Override
	public void ParseData() 
	{
		for (GameTeam team : this.GetTeamList())
		{
			ArrayList<Location> locs = null;

			if (team.GetColor() == ChatColor.RED)
				locs = this.WorldData.GetDataLocs("RED");
			else if (team.GetColor() == ChatColor.AQUA)
				locs =  this.WorldData.GetDataLocs("BLUE");
			else if (team.GetColor() == ChatColor.YELLOW)
				locs =  this.WorldData.GetDataLocs("YELLOW");
			else if (team.GetColor() == ChatColor.GREEN)
				locs =  this.WorldData.GetDataLocs("GREEN");

			if (locs == null)
			{
				System.out.println("ERROR! Could not find Sheep Pen for Team " + team.GetColor().toString());
				return;
			}

			ArrayList<Block> blocks = new ArrayList<Block>();
			for (Location loc : locs)
				blocks.add(loc.getBlock());

			_sheepPens.put(team, blocks);
		}

		_sheepSpawns = this.WorldData.GetDataLocs("WHITE");
	}

	@EventHandler
	public void SheepSpawnStart(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Live)
			for (int i=0 ; i<4 ; i++)
				SheepSpawn();
	}

	@EventHandler
	public void SheepSpawnUpdate(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.FAST)
			return;

		if (!UtilTime.elapsed(_sheepTimer, _sheepDelay))
			return;

		SheepSpawn();
	}

	public void SheepSpawn()
	{
		//Spawn Sheep
		this.CreatureAllowOverride = true;
		Sheep sheep = _sheepSpawns.get(0).getWorld().spawn(UtilAlg.Random(_sheepSpawns), Sheep.class);
		sheep.setAdult();
		sheep.setMaxHealth(2048);
		sheep.setHealth(2048);
		this.CreatureAllowOverride = false;

		_sheep.put(sheep, new SheepData(this, sheep));

		_sheepTimer = System.currentTimeMillis();

		//Effect
		sheep.getWorld().playSound(sheep.getLocation(), Sound.SHEEP_IDLE, 2f, 1.5f);
		UtilFirework.playFirework(sheep.getLocation().add(0, 0.5, 0), FireworkEffect.builder().flicker(false).withColor(Color.WHITE).with(Type.BALL).trail(false).build());
	}

//	@EventHandler
//	public void Stack(PlayerInteractEntityEvent event)
//	{
//		if (!IsLive())
//			return;
//
//		if (!(event.getRightClicked() instanceof Sheep))
//			return;
//
//		if (event.getRightClicked().getVehicle() != null)
//			return;
// 
//		if (!Recharge.Instance.usable(event.getPlayer(), "Sheep Stack", true))
//			return;
//		
//		SheepStack(event.getPlayer(), (Sheep)event.getRightClicked());
//	}
	
	@EventHandler
	public void Stack(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		for (Player player : GetPlayers(true))
		{ 
			if (!Recharge.Instance.usable(player, "Sheep Stack"))
				continue;
			
			for (Entity ent : player.getWorld().getEntities())
			{
				if (!(ent instanceof Sheep))
					continue;
				
				if (ent.getVehicle() != null)
					continue;
				
				if (UtilMath.offset(player, ent) > 2.5)
					continue;
				
				if (SheepStack(player, (Sheep)ent))
					break;
			}
		}
	}
	
	public boolean SheepStack(Player player, Sheep sheep)
	{
		if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.SADDLE)
			return true;

		if (Manager.isSpectator(player))
			return true;

		if (!IsAlive(player))
			return true;

		if (player.getPassenger() != null && player.getPassenger() instanceof Player)
		{
			DropSheep(player, 500, false);
		}

		//Holding too many
		int count = 0;
		Entity top = player;
		while (top.getPassenger() != null)
		{
			top = top.getPassenger();
			count++;
		}

		if (count >= 3)
		{
			//UtilPlayer.message(player, F.main("Game", "You cannot hold more than 3 Sheep!"));
			return true;
		}

		for (SheepData data : _sheep.values())
		{
			if (data.Sheep.equals(sheep))
			{
				if (data.Owner != null && data.Owner.equals(GetTeam(player)) && data.IsInsideOwnPen())
				{
					//UtilPlayer.message(player, F.main("Game", "You have already captured this Sheep!"));
					return false;
				}

				data.SetHolder(player);
			}
		}

		//Put Wool in Inventory
		inventoryWool(player, count, sheep);
		
		//Effect
		sheep.getWorld().playEffect(sheep.getLocation(), Effect.STEP_SOUND, 35);

		//Stack
		top.setPassenger(sheep);

		//Audio
		player.playSound(player.getLocation(), Sound.SHEEP_IDLE, 2f, 3f);
		
		//Cooldown
		Recharge.Instance.useForce(player, "Sheep Stack", 500);
		
		return true;
	}
	
	public void inventoryWool(Player player, int count, Entity sheep)
	{
		player.getInventory().setItem(4 + count, ItemStackFactory.Instance.CreateStack(35, ((Sheep)sheep).getColor().getWoolData()));
		UtilInv.Update(player);
	}

	@EventHandler
	public void StackPlayer(PlayerInteractEntityEvent event)
	{
		if (!IsLive())
			return;

		if (!(event.getRightClicked() instanceof Player))
			return;

		if (event.getRightClicked().getVehicle() != null)
			return;

		Player player = event.getPlayer();
		Player other = (Player)event.getRightClicked();

		if (!(GetKit(player) instanceof KitBrute))
			return;

		if (!GetTeam(player).HasPlayer(other))
			return;

		if (player.getPassenger() != null)
			DropSheep(player, 500, false);

		if (!Recharge.Instance.usable(player, "Sheep Stack"))
			return;

		if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.SADDLE)
			return;

		if (Manager.isSpectator(event.getPlayer()))
			return;

		if (!IsAlive(event.getPlayer()))
			return;

		//Effect
		event.getRightClicked().getWorld().playEffect(event.getRightClicked().getLocation(), Effect.STEP_SOUND, 35);

		//Stack
		player.setPassenger(other);

		//Audio
		player.playSound(player.getLocation(), Sound.HORSE_ARMOR, 1f, 1f);

		//Inform
		UtilPlayer.message(other, F.main("Skill", F.elem(GetTeam(player).GetColor() + player.getName()) + " picked you up."));
		UtilPlayer.message(player, F.main("Skill", "You picked up " + F.elem(GetTeam(other).GetColor() + other.getName()) + "."));
	}

	@EventHandler
	public void DeathDrop(PlayerDeathEvent event)
	{
		DropSheep(event.getEntity(), 0, false);
		
		event.getEntity().eject();
	}
	
	@EventHandler
	public void DeathDrop(PlayerItemHeldEvent event)
	{
		DropSheep(event.getPlayer(), 0, false);
	}

	public void DropSheep(Player player, long cooldown, boolean calledByCarrier)
	{
		//Brute can only throw
		if (calledByCarrier && GetKit(player) instanceof KitBrute)
			return;
		
		boolean hadSheep = false;

		Entity top = player;
		while (top.getPassenger() != null)
		{
			top = top.getPassenger();
			top.leaveVehicle();

			hadSheep = true;
		}

		if (hadSheep)
			UtilTextTop.display(C.cRed + C.Bold + "You dropped your Sheep!", player);

		player.setExp(0f);

		player.getInventory().remove(Material.WOOL);

		Manager.GetCondition().EndCondition(player, ConditionType.SLOW, null);

		//Audio
		player.playSound(player.getLocation(), Sound.SHEEP_IDLE, 2f, 1f);
		
		//Recharge
		Recharge.Instance.useForce(player, "Sheep Stack", cooldown);
	}

	@EventHandler
	public void Drop(PlayerDropItemEvent event)
	{
		DropSheep(event.getPlayer(), 500, true);
	}

	@EventHandler
	public void SheepUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		Iterator<Sheep> sheepIterator = _sheep.keySet().iterator();

		while (sheepIterator.hasNext())
		{
			Sheep sheep = sheepIterator.next();
			SheepData data = _sheep.get(sheep);

			if (data.Update())
			{
				sheep.remove();
				sheepIterator.remove();
			}
		}
	}

	@EventHandler
	public void CarryingEffect(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		for (Player player : GetPlayers(true))
		{
			int count = 0;

			Entity top = player;
			while (top.getPassenger() != null)
			{
				top = top.getPassenger();
				count++;
			}

			player.setExp(0.33f * (float)count);

			//Inventory Wool Clean
			if (count <= 0 && UtilGear.isMat(player.getInventory().getItem(4), Material.WOOL))
				player.getInventory().setItem(4, null);
			if (count <= 1 && UtilGear.isMat(player.getInventory().getItem(5), Material.WOOL))
				player.getInventory().setItem(5, null);
			if (count <= 2 && UtilGear.isMat(player.getInventory().getItem(6), Material.WOOL))
				player.getInventory().setItem(6, null);

			if (count == 0)
				continue;

			Manager.GetCondition().Factory().Slow("Sheep Slow", player, player, 3, count-1, false, false, false, true);

			UtilTextTop.displayProgress(C.Bold + "Return the Sheep to your Team Pen!", (float)count/3f, player);
		}
	}
	
//	@EventHandler(priority = EventPriority.LOW)
//	public void DamageSheepCancel(CustomDamageEvent event)
//	{
//		if (event.IsCancelled())
//			return;
//		
//		if (event.GetDamagerPlayer(true) != null)
//		{
//			if (event.GetDamagerPlayer(true).getPassenger() != null)
//			{
//				event.SetCancelled("SQ: Has Passenger");
//			}
//		}
//	}

	@EventHandler(priority = EventPriority.LOW)
	public void DamagePassOn(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetDamageeEntity().getVehicle() == null)
			return;

		LivingEntity bottom = event.GetDamageeEntity();
		while (bottom.getVehicle() != null && bottom.getVehicle() instanceof LivingEntity)
			bottom = (LivingEntity) bottom.getVehicle();

		event.SetCancelled("Damage Passdown");

		//Damage Event
		Manager.GetDamage().NewDamageEvent(bottom, event.GetDamagerEntity(true), event.GetProjectile(), 
				event.GetCause(), event.GetDamageInitial(), true, false, false,
				UtilEnt.getName(event.GetDamagerEntity(true)), event.GetReason());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void DamageSuffocate(CustomDamageEvent event)
	{
		if (event.GetCause() == DamageCause.SUFFOCATION)
			event.SetCancelled("Sheep Game");
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void DamageUnstack(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetCause() != DamageCause.ENTITY_ATTACK && event.GetCause() != DamageCause.PROJECTILE && event.GetCause() != DamageCause.CUSTOM)
			return;

		Player player = event.GetDamageePlayer();
		if (player == null)		return;

		DropSheep(player, 1000, false);

		Bukkit.getPluginManager().callEvent(new DropEnemySheepEvent(event.GetDamagerPlayer(true), player));
	}

	@EventHandler
	public void RespawnInvul(PlayerGameRespawnEvent event)
	{
		Manager.GetCondition().Factory().Regen("Respawn", event.GetPlayer(), event.GetPlayer(), 5, 3, false, false, true);
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (!InProgress())
			return;

		//Wipe Last
		Scoreboard.reset();
		Scoreboard.writeNewLine();

		Scoreboard.writeGroup(_sheepPens.keySet(), team ->
		{
			_teamScore.put(team, getSheepCount(team));
			return Pair.create(team.GetColor() + team.GetName(), getSheepCount(team));
		}, true);

		if (!IsLive())
			return;

		Scoreboard.writeNewLine();
		Scoreboard.write(C.cYellow + C.Bold + "Next Sheep");
		Scoreboard.write(UtilTime.MakeStr(_sheepDelay - (System.currentTimeMillis() - _sheepTimer), 0));

		long time = _gameTime
				- (System.currentTimeMillis() - this.GetStateTime());

		Scoreboard.writeNewLine();
		Scoreboard.write(C.cYellow + C.Bold + "Time Left");
		Scoreboard.write(UtilTime.MakeStr(Math.max(0, time), 0));

		Scoreboard.draw();
	}

	public void GetTeamPen(SheepData data) 
	{
		for (GameTeam team : _sheepPens.keySet())
		{
			if (_sheepPens.get(team).contains(data.SheepBlock())) 
			{
				data.SetOwner(team, _sheepPens.get(team));	
			}
		}
	}

	public Location GetSheepSpawn() 
	{
		return UtilAlg.Random(_sheepSpawns);
	}

	@Override
	public void EndCheck()
	{
		if (!IsLive())
			return;

		ArrayList<GameTeam> teamsAlive = new ArrayList<GameTeam>();

		for (GameTeam team : this.GetTeamList())
			if (team.GetPlayers(true).size() > 0)
				teamsAlive.add(team);

		if (teamsAlive.size() <= 1)
		{
			//Announce
			if (teamsAlive.size() > 0)
				AnnounceEnd(teamsAlive.get(0));

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

			//End
			SetState(GameState.End);	
		}

		//Actual Game End 
		if (_gameTime - (System.currentTimeMillis() - this.GetStateTime()) <= 0)
		{
			GameTeam bestTeam = null;
			int bestScore = -1;
			int duplicate = 0;

			//Get Highest
			for (GameTeam team : _teamScore.keySet())
			{
				if (bestTeam == null || _teamScore.get(team) > bestScore)
				{
					bestTeam = team;
					bestScore = _teamScore.get(team);
					duplicate = 0;
				}
				else if (_teamScore.get(team) == bestScore)
				{
					duplicate++;
				}
			}

			if (duplicate > 0)
			{
				if (UtilTime.elapsed(_gameEndAnnounce, 10000))
				{
					this.Announce(C.cGold + C.Bold + "First team to take the lead will win the game!");

					_gameEndAnnounce = System.currentTimeMillis();
				}
			}
			else
			{
				AnnounceEnd(bestTeam);

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

				//End
				SetState(GameState.End);	
			}
		}
	}
	/*
	@EventHandler
	public void Giant(PlayerCommandPreprocessEvent event)
	{
		if (event.getMessage().contains("/giant"))
		{
			Giant giant = event.getPlayer().getWorld().spawn(event.getPlayer().getLocation(), Giant.class);

			Entity top = giant;
			for (int i=0 ; i < 10 ; i++)
			{
				Entity buffer = event.getPlayer().getWorld().spawn(event.getPlayer().getLocation(), Chicken.class);
				top.setPassenger(buffer);
				top = buffer;
			}

			top.setPassenger(event.getPlayer());
		}
	}

	@EventHandler
	public void Invul(CustomDamageEvent event)
	{
		event.SetCancelled("True");
	}*/
	
	public void setSheepDelay(long delay)
	{
		_sheepDelay = delay;
	}

	public int getSheepCount(GameTeam team)
	{
		int score = 0;

		for (Sheep sheep : _sheep.keySet())
		{
			if (_sheepPens.get(team).contains(sheep.getLocation().getBlock()))
				score++;
		}

		return score;
	}
	
	public HashMap<GameTeam, ArrayList<Block>> getSheepPens()
	{
		return _sheepPens;
	}
	
	public long getGameTime()
	{
		return _gameTime;
	}
	
	public HashMap<GameTeam, Integer> getTeamScores()
	{
		return _teamScore;
	}
}
