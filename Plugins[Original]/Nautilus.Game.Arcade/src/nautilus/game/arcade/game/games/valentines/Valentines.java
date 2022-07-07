package nautilus.game.arcade.game.games.valentines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.F;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.SoloGame;
import nautilus.game.arcade.game.games.valentines.kit.KitMasterOfLove;
import nautilus.game.arcade.game.games.valentines.tutorial.TutorialValentines;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;

public class Valentines extends SoloGame
{
	private String _cowName = "Calvin the Cow";
	
	private Cow _cow;
	
	private int _playersOutPerRound = 1;
	
	private HashSet<Block> _blocks = new HashSet<Block>();
	private int _blockMapHeight = 0;
	
	private int _round = 0;
	private long _roundTime = 0;
	private long _roundTimeLimit = 60000;
	
	private int _roundState = 0;
	//1 = Playing
	//2 = Between Rounds
	
	private boolean _finalRound = false;
	
	private ValItem _item = null;
	
	private ArrayList<Location> _pigSpawns = null;
	private HashMap<Pig, Location> _pigs = new HashMap<Pig, Location>();
	private HashSet<Pig> _pigsDead = new HashSet<Pig>();
	
	private ArrayList<Player> _hasItem = new ArrayList<Player>();
	private ArrayList<Player> _completedRound = new ArrayList<Player>();
	
	private HashSet<ValItem> _unusedGifts = new HashSet<ValItem>();
	
	public Valentines(ArcadeManager manager) 
	{
		super(manager, GameType.Valentines,

				new Kit[]
						{
				new KitMasterOfLove(manager),
						},

						new String[]
								{
				"Calvin the Cow has a Valentines Date,",
				"but he was robbed by the nasty pigs!",
				"Recover his items, and save the day!",
				"",
				"Slowest players are eliminated!"
								});
	
		this.DamageTeamSelf = true;
		this.HungerSet = 20;
		this.HealthSet = 20;
		
		EnableTutorials = true;

		new CompassModule()
				.setGiveCompass(true)
				.setGiveCompassToSpecs(true)
				.setGiveCompassToAlive(false)
				.register(this);
	}
	
	@Override
	public void ParseData() 
	{
		_pigSpawns = WorldData.GetDataLocs("YELLOW");
		
		//Scan Map
		for (Block block : UtilBlock.getInBoundingBox(WorldData.GetDataLocs("GRAY").get(0), WorldData.GetDataLocs("GRAY").get(1)))
		{
			if (block.getType() != Material.AIR)
				_blocks.add(block);
			
			if (UtilBlock.solid(block))
			{
				if (block.getY() > _blockMapHeight)
					_blockMapHeight = block.getY();
			}
		}
		
		System.out.println("Scanned " + _blocks.size() + " Arena Blocks");
	}
	
	@EventHandler
	public void stateChanges(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Prepare)
		{
			_playersOutPerRound = Math.max(1, (int)(GetPlayers(true).size()/8d));
			
			CreatureAllowOverride = true;
			_cow = GetSpectatorLocation().getWorld().spawn(GetSpectatorLocation(), Cow.class);
			_cow.setCustomName(C.cGreen + C.Bold + _cowName);
			_cow.setCustomNameVisible(true);
			
			UtilEnt.vegetate(_cow);
			UtilEnt.ghost(_cow, true, false);
			CreatureAllowOverride = false;
		}
	}
	
	@Override
	public void addTutorials()
	{
		GetTeamList().get(0).setTutorial(new TutorialValentines(this, Manager));
	}
			
	@EventHandler
	public void knockback(CustomDamageEvent event)
	{
		if (event.GetDamageePlayer() != null)
		{
			if (event.GetCause() == DamageCause.ENTITY_ATTACK)
				event.AddKnockback("Hit Reversal", -1);
		}
	}
	
	@EventHandler
	public void knockbackRod(EntityDamageEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;

		Player player = ((Player) event.getEntity());
		Projectile projectile = Manager.GetDamage().GetProjectile(event);

		if (!(projectile instanceof Fish))
			return;
		
//		LivingEntity damagee = Manager.GetDamage().GetDamageeEntity(event);
//		LivingEntity damager = UtilEvent.GetDamagerEntity(event, true);

		if (player.getVehicle() != null)
		{
			UtilTextMiddle.display("", C.cRed + "You were knocked off the pig!", player);
			player.getVehicle().eject();
		}
		
		event.setCancelled(true);
		
//		damagee.playEffect(EntityEffect.HURT);
//		
//		UtilAction.velocity(damagee, UtilAlg.getTrajectory(damagee, damager), 0.2, false, 0, 0.1, 1, true);
		
		projectile.remove();
	}
	
	@EventHandler
	public void grabDamage(CustomDamageEvent event)
	{
		if (_finalRound)
			return;
		
		if (event.GetDamagerPlayer(false) == null)
			return;
		
		if (_finalRound)
		{
			event.GetDamageeEntity().eject();
			event.GetDamageeEntity().leaveVehicle();
		}
		
		if (event.GetDamageeEntity() instanceof Pig)
		{
			grabItem(event.GetDamagerPlayer(false), (Pig)event.GetDamageeEntity());
		}
		
		if (!event.IsCancelled())
		{
			event.SetCancelled("Pig Cancel");

			event.GetDamageeEntity().playEffect(EntityEffect.HURT);
		}	
	}
	
	private void grabItem(Player player, Pig pig) 
	{
		if (!IsAlive(player))
			return;
		
		if (_hasItem.contains(player) || _completedRound.contains(player))
			return;
		
		if (!_pigs.containsKey(pig))
			return;
		
		_pigs.remove(pig);
		
		pig.damage(9999);
//		pig.playEffect(EntityEffect.DEATH);
//		_pigsDead.add(pig);
		
		//Remove Item from Pig
		if (pig.getPassenger() != null)
			pig.getPassenger().remove();
		
		//Give Item to Player
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(_item.getMaterial(), _item.getData()));
		
		_hasItem.add(player);
		
		//Effects
		player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1f, 1f);
		player.getWorld().playSound(pig.getLocation(), Sound.PIG_DEATH, 1f, 1f);
		
		//Inform
		UtilTextMiddle.display(C.cGreen + C.Bold + "Success", "Take " + _item.getTitle() + " back to Calvin!", 0, 80, 20, player);
		
		if (_pigs.size() > 0)
			UtilTextBottom.display(C.Bold + _pigs.size() + " Items Left", UtilServer.getPlayers());
	}
	
	@EventHandler
	public void returnDamage(CustomDamageEvent event)
	{
		if (event.GetDamagerPlayer(false) == null)
			return;
		
		if (event.GetDamageeEntity() instanceof Cow)
		{
			returnItem(event.GetDamagerPlayer(false), (Cow)event.GetDamageeEntity());
			
			event.SetCancelled("Cow Damage");
		}
	}
	
	@EventHandler
	public void returnInteract(PlayerInteractEntityEvent event)
	{
		if (event.getRightClicked() instanceof Cow)
		{
			returnItem(event.getPlayer(), (Cow)event.getRightClicked());
		}
	}

	private void returnItem(Player player, Cow cow) 
	{
		if (!IsAlive(player))
			return;
		
		if (!_hasItem.remove(player) && player.getVehicle() == null)
			return;
						
		
		//Remove Item to Player
		if (!_finalRound)
		{
			player.getInventory().remove(_item.getMaterial());
		}
		else
		{
			_pigs.clear();
		}

		_completedRound.add(player);
		
		//Effects
		player.playSound(player.getLocation(), Sound.LEVEL_UP, 1f, 1f);
		player.getWorld().playSound(cow.getLocation(), Sound.COW_IDLE, 2f, 1f);
		
		UtilParticle.PlayParticle(ParticleType.HEART, _cow.getLocation().add(0, 0.5, 0), 1f, 1f, 1f, 0, 10, ViewDist.NORMAL, UtilServer.getPlayers());
		
		//Inform
		UtilTextMiddle.display(C.cGreen + C.Bold + _cowName, _item.getEndText(), 0, 80, 20, player);
		
		if (_pigs.size() > 0)
			UtilTextBottom.display(C.Bold + _pigs.size() + " Items Left", UtilServer.getPlayers());

	}

	public double getRadius()
	{
		return 24 + (GetPlayers(true).size() * 0.5d);
	}
	
	//@EventHandler
	public void arenaShrinkUpdate(UpdateEvent event)
	{
		if (!IsLive())
			return;
		
		if (event.getType() != UpdateType.TICK)
			return;
		
		double radius = getRadius();
		
		Block bestBlock = null;
		double bestDist = 0;
		
		for (Block block : _blocks)
		{
			double dist = UtilMath.offset2d(GetSpectatorLocation(), block.getLocation().add(0.5, 0.5, 0.5));
			
			if (dist < radius)
				continue;
			
			if (bestBlock == null || dist > bestDist)
			{
				bestBlock = block;
				bestDist = dist;
			}
		}
		
		//Shuffle Down
		while (bestBlock.getRelative(BlockFace.DOWN).getType() != Material.AIR)
			bestBlock = bestBlock.getRelative(BlockFace.DOWN);				
		
		_blocks.remove(bestBlock);
		
		if (bestBlock.getType() != Material.AIR)
			MapUtil.QuickChangeBlockAt(bestBlock.getLocation(), Material.AIR);
	}
	
	@EventHandler
	public void pigFall(EntityDamageEvent event)
	{
		if (event.getCause() == DamageCause.FALL)
			event.setCancelled(true);
	}
	
	public void pigSpawn()
	{
		CreatureAllowOverride = true;
		
		int toSpawn = Math.max(1, GetPlayers(true).size()-_playersOutPerRound);
		
		if (toSpawn == 1)
			_finalRound = true;
		
		for (int i=0 ; i < toSpawn ; i++)
		{	
			Location loc = UtilAlg.Random(_pigSpawns);
			
			Pig pig = loc.getWorld().spawn(loc, Pig.class);
			_pigs.put(pig, pig.getLocation());
			
			UtilEnt.vegetate(pig);
			
			//Give Item
			if (toSpawn > 1)
			{
				Item item = pig.getWorld().dropItem(pig.getLocation(),ItemStackFactory.Instance.CreateStack(_item.getMaterial(), _item.getData()));
				
				if (pig.getPassenger() != null)
					pig.getPassenger().remove();
				
				pig.setPassenger(item);
			}
		}
		
		CreatureAllowOverride = false;
	}
	
	@EventHandler
	public void pigUpdate(UpdateEvent event)
	{
		for (Pig pig : _pigs.keySet())
		{		
			//Fallen Off Island?! JUMP BACK!
			Block block = pig.getLocation().getBlock();
			while (block.getType() == Material.AIR)
			{
				block = block.getRelative(BlockFace.DOWN);
			}
			if (block.isLiquid())
			{
				UtilAction.velocity(pig, 
						UtilAlg.getTrajectory(pig.getLocation(), 
						GetSpectatorLocation().clone().add(Math.random() * 30 - 15, 0, Math.random() * 30 - 15)), 
						2 + Math.random(), false, 0, 0.4, 10, true);
				
				pig.getWorld().playSound(pig.getLocation(), Sound.PIG_IDLE, 2f, 2f);
				
				continue;
			}
			
			//Player Rider
			if (_finalRound && pig.getPassenger() != null && pig.getPassenger() instanceof Player)
			{
				Location target = pig.getLocation();
				target.add(pig.getPassenger().getLocation().getDirection().multiply(5));
				
				UtilEnt.CreatureMoveFast(pig, target, 1.3f);
				
				continue;
			}
			
			Vector threat = new Vector(0,0,0);
			
			//waypoint
			Location loc = _pigs.get(pig);
			
			//find new waypoint
			if (UtilMath.offset2d(pig.getLocation(), loc) < 5 || targetAtEdge(loc))
			{
				Location newLoc = getNewWaypoint(pig);
				
				if (newLoc != null)
				{
					loc.setX(newLoc.getX());
					loc.setZ(newLoc.getZ());
				}
			}
			
			threat.add(UtilAlg.getTrajectory2d(pig.getLocation(), loc).multiply(0.4));
			
			//run from players
			double closestDist = 0;
			HashMap<LivingEntity, Double> ents = UtilEnt.getInRadius(pig.getLocation(), 12);
			if (ents.size() > 0)
			{
				for (LivingEntity ent : ents.keySet())
				{
					if (ent instanceof Player)
					{
						if (ent.equals(pig))
							continue;
						
						if (!IsAlive((Player)ent))
							continue;
						
						double score = ents.get(ent);
						
						//Add scaled threat!
						threat.add(UtilAlg.getTrajectory2d(ent, pig).multiply(score));
						
						if (score > closestDist)
							closestDist = score;
					}
					if (ent instanceof Pig)
					{
						if (ents.get(ent) < 0.3)
						{
							threat.add(UtilAlg.getTrajectory2d(ent, pig).multiply(ents.get(ent) * 0.3));
						}
					}
				}	
			}
			
			threat.normalize();

			//MOVE
			Location target = pig.getLocation().add(threat.multiply(4));
			
			//try to skate around edge
			int attempts = 0;
			while (targetAtEdge(target) && attempts < 10)
			{
				attempts++;
				
				target.add(UtilAlg.getTrajectory(target, GetSpectatorLocation()));
				
				//too cornered! jump back in
				if (UtilMath.offset(pig.getLocation(), target) < 2)
				{
					UtilEnt.CreatureMoveFast(pig, pig.getLocation().add(UtilAlg.getTrajectory(pig.getLocation(), GetSpectatorLocation())), 2f);
					
					UtilAction.velocity(pig, 
							UtilAlg.getTrajectory(pig.getLocation(), 
							GetSpectatorLocation().clone().add(Math.random() * 30 - 15, 0, Math.random() * 30 - 15)), 
							2 + Math.random(), false, 0, 0.4, 10, true);
					
					pig.getWorld().playSound(pig.getLocation(), Sound.PIG_IDLE, 2f, 2f);
				}
			}
			
			UtilEnt.CreatureMoveFast(pig, target, 1.3f + (float)(1f * closestDist));
		}
	}
	
	private Location getNewWaypoint(Pig pig) 
	{
		for (int i=0 ; i<50 ; i++)
		{
			Location loc = pig.getLocation();
			
			loc.add(Math.random() * 50 - 25, 0, Math.random() * 50 - 25);
			
			if (!targetAtEdge(loc))
				return loc;
		}
		
		return null;
	}

	private boolean targetAtEdge(Location target) 
	{
		Block block = target.getWorld().getBlockAt(target.getBlockX(), _blockMapHeight, target.getBlockZ());
		
		for (int x=-1 ; x <= 1 ; x++)
			for (int z=-1 ; z <= 1 ; z++)
			{
				if (block.getRelative(x, -1, z).getType() == Material.AIR)
					return true;
			}
		
		return false;
	}

	@EventHandler
	public void roundUpdate(UpdateEvent event)
	{
		if (!IsLive())
			return;
		
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (_roundState == 0)
		{
			nextRound();
		}
		
		else if (_roundState == 1)
		{
			if (UtilTime.elapsed(_roundTime, _roundTimeLimit) || (_pigs.isEmpty() && _hasItem.isEmpty()))
			{
				/*
				if (_completedRound.isEmpty())
				{
					System.out.println("RoundUpdate:604");
					//Announce
					AnnounceEnd(new ArrayList<Player>());

					//End
					SetState(GameState.End);
					
					return;
				}
				*/
				
				//Kill Failed Players
				for (Player player : GetPlayers(true))
				{
					if (!_completedRound.contains(player))
					{
						UtilTextMiddle.display(C.cRed + C.Bold + "Game Over", "You failed to help Calvin", 0, 80, 20, player);
						player.damage(9999);
					}
				}
				
				_roundState = 2;
				_roundTime = System.currentTimeMillis();
	
				
				//Cull Excess Pigs
				Iterator<Pig> pigIter = _pigs.keySet().iterator();
				while (pigIter.hasNext())
				{
					Pig pig = pigIter.next();
					
					if (pig.getPassenger() != null)
						pig.getPassenger().remove();
					
					_pigsDead.add(pig);
					pigIter.remove();
				};
			}
		}
		
		else if (_roundState == 2 && UtilTime.elapsed(_roundTime, 5000))
		{
			nextRound();
		}
	}

	private void nextRound() 
	{
		_round++;
		
		_roundState = 1;
		_roundTime = System.currentTimeMillis();
		
		_completedRound.clear();
		_hasItem.clear();
		
		//Restock Items (only repeat items if required)
		if (_unusedGifts.isEmpty())
			for (ValItem gift : ValItem.values())
				_unusedGifts.add(gift);
		
		//Set Item
		_item = UtilAlg.Random(_unusedGifts);
		_unusedGifts.remove(_item);
		
		//Delete Dead Pigs
		for (Pig pig : _pigsDead)
			pig.remove();
		_pigsDead.clear();
		
		//Clean
		for (Pig pig : _pigs.keySet())
		{
			if (pig.getPassenger() != null)
				pig.getPassenger().remove();
			
			pig.remove();
		}
		_pigs.clear();
		
		//Restock Pigs
		pigSpawn();

		//Announce
		if (_pigs.size() > 1)
		{
			UtilTextMiddle.display(C.cYellow + "Round " + _round, _item.getTitle(), 0, 80, 20);
		}
		else
		{
			UtilTextMiddle.display(C.cYellow + "Final Round", "Capture the Pig!", 0, 80, 20);

			for (Player player : GetPlayers(true))
				player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.SADDLE, (byte)0, 1, "Pig Saddle"));
		}
	}
	
	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (GetTeamList().isEmpty())
			return;

		Scoreboard.reset();

		Scoreboard.writeNewLine();
		Scoreboard.write(C.cGreen + "Players Alive");
		Scoreboard.write("" + GetPlayers(true).size());
		
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cGreen + "Items Left");
		
		if (_roundState == 1)
			Scoreboard.write("" + _pigs.size());
		else
			Scoreboard.write("-");
		
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cGreen + "Time Left");
		if (_roundState == 1)
			Scoreboard.write("" + UtilTime.MakeStr(_roundTimeLimit - (System.currentTimeMillis() - _roundTime), 0));
		else
			Scoreboard.write("-");

		Scoreboard.draw();
	}
	
	@Override
	public void EndCheck()
	{
		if (!IsLive())
			return;

		if (GetPlayers(true).size() == 1)
		{	
			List<Player> places = _players.GetPlacements(true);

			System.out.println("EndCheck:760");
			//Announce
			AnnounceEnd(places);

			//Gems
			if (places.size() >= 1)
				AddGems(places.get(0), 20, "1st Place", false, false);

			if (places.size() >= 2)
				AddGems(places.get(1), 15, "2nd Place", false, false);

			if (places.size() >= 3)
				AddGems(places.get(2), 10, "3rd Place", false, false);

			for (Player player : GetPlayers(false))
				if (player.isOnline())
					AddGems(player, 10, "Participation", false, false);

			
			if (GetPlayers(true).size() >= 1)
			{
				Player winner = places.get(0);

				if (Manager.GetTaskManager().hasCompletedTask(winner, "Valentines Reward 2016"))
				{
					
				}
				else
				{
					SetCustomWinMessage(winner, winner.getName() + " earned " + C.cYellow + "3x Valentines Gift");

					Manager.GetTaskManager().completedTask(new Callback<Boolean>()
					{
						@Override
						public void run(Boolean data)
						{
							if (data)
							{
								Manager.getInventoryManager().addItemToInventory(winner, "Valentines Gift", 3);
							}
							else
							{
								UtilPlayer.message(winner, F.main("Inventory", "An error occured while giving you " + C.cRed + "3x Valentines Gift" + C.cGray + "."));
							}
						}
					}, winner, "Valentines Reward 2016");
				}
			}
			
			//End
			SetState(GameState.End);
		}
		else if (GetPlayers(true).size() == 0)
		{
			for (Player player : GetPlayers(false))
			{
				Manager.GetGame().AddGems(player, 10, "Participation", false, false);
			}

			SetCustomWinLine("Moolanie broke up with Calvin...");

			SetState(GameState.End);
		}
	}

	@EventHandler
	public void displayCalvinMessage(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (!IsLive())
			return;

		if (_item != null)
		{
			String message = _finalRound ? "Bring the Pig to Calvin!" : "Return " + _item.getTitle() + " to Calvin!";

			Player[] players = GetPlayers(true).stream().filter(this::canReturnToCow).toArray(size -> new Player[size]);
			UtilTextMiddle.display("", C.cGreen + message, players);
		}
	}

	private boolean canReturnToCow(Player player)
	{
		return _hasItem.contains(player) || player.getVehicle() != null;
	}
}
