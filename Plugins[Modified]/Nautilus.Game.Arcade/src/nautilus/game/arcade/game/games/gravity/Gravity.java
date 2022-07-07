package nautilus.game.arcade.game.games.gravity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftFallingSand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.events.PlayerPrepareTeleportEvent;
import nautilus.game.arcade.game.SoloGame;
import nautilus.game.arcade.game.games.gravity.kits.*;
import nautilus.game.arcade.game.games.gravity.objects.GravityBomb;
import nautilus.game.arcade.game.games.gravity.objects.GravityDebris;
import nautilus.game.arcade.game.games.gravity.objects.GravityHook;
import nautilus.game.arcade.game.games.gravity.objects.GravityPlayer;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;

public class Gravity extends SoloGame
{
	//Contains ALL Objects
	private ArrayList<GravityObject> _objects = new ArrayList<GravityObject>();

	//Player Hooks
	private HashMap<Player, GravityHook> _hooks = new HashMap<Player, GravityHook>();

	private HashMap<Arrow, Vector> _arrows = new HashMap<Arrow, Vector>();

	private ArrayList<Location> _powerups = new ArrayList<Location>();
	private Location _powerup = null;
	private long _lastPowerup = 0;

	public Gravity(ArcadeManager manager) 
	{
		super(manager, GameType.Gravity,

				new Kit[]
						{
				new KitJetpack(manager)
						},

						new String[]
								{
				C.cGreen + "Push Drop" + C.cGray + " to boost off blocks",
				C.cGreen + "Left-Click" + C.cGray + " to use " + F.skill("Sonic Blast"),
				C.cGreen + "Right-Click" + C.cGray + " to use " + F.skill("Jetpack"),
				"Food is Oxygen. Restore it at Emerald Blocks.",
				"Last player alive wins!"
								});

		_help = new String[]
				{
				"Drop Item to launch yourself off platforms.",
				"You automatically grab onto nearby platforms.",
				"Hold Block to use your Jetpack",
				"Your Experience Bar is your Jetpack Fuel",
				"Restore Jetpack Fuel by collecting Powerups",
				"Powerups are flashing green fireworks",
				"Your Hunger is your Oxygen Level",
				"Restore Oxygen at the Emerald Blocks",
				};

		this.DamagePvP = false;
		this.HungerSet = 10;

		this.WorldTimeSet = 18000;
 
		new CompassModule()
				.setGiveCompassToAlive(true)
				.register(this);
		
		this.WorldBoundaryKill = false;

		registerChatStats(
				Kills,
				Assists
		);
	}

	@Override
	public void ParseData() 
	{
		_powerups = this.WorldData.GetDataLocs("LIME");
	}

	@EventHandler
	public void CreatePlayerObjects(PlayerPrepareTeleportEvent event)
	{
		Player player = event.GetPlayer();

		GravityPlayer obj = new GravityPlayer(this, player, 60, null);
		_objects.add(obj);

		player.setExp(0.9999f);
	}

	@EventHandler
	public void AnnounceBoost(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Prepare)
		{
			UtilTextMiddle.display(null, C.cGreen + "Drop Weapon" + C.cWhite + " to boost off Platforms", 0, 120, 0);
			
			Bukkit.getScheduler().runTaskLater(Manager.getPlugin(), new Runnable()
			{
				@Override
				public void run() 
				{
					UtilTextMiddle.display(null, C.cGreen + "Hold Block" + C.cWhite + " to use Jetpack", 0, 80, 5);
				}
			}, 120);
		}
			
	}

	@EventHandler
	public void cleanObjects(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		//Objects
		Iterator<GravityObject> oIter = _objects.iterator();
		
		while (oIter.hasNext())
		{
			GravityObject o = oIter.next();
			
			if (o instanceof GravityPlayer)
				continue;
			
			if (!isInsideMap(o.Base.getLocation()))
			{
				o.remove();
			}
		}
		
		//Projectiles
		Iterator<Arrow> arrowIter = _arrows.keySet().iterator();
		
		while (arrowIter.hasNext())
		{
			Arrow arrow = arrowIter.next();
			
			if (!isInsideMap(arrow.getLocation()))
			{
				arrow.remove();
				arrowIter.remove();
			}
		}
	}
	
	@EventHandler
	public void ClearObjects(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End)
			return;

		for (GravityObject obj : _objects)
			obj.Clean();

		_objects.clear();
	}

	@EventHandler
	public void KickOff(PlayerDropItemEvent event)
	{
		event.setCancelled(true);
		
		if (!IsLive())
			return;
		
		for (GravityObject object : _objects)
			if (object instanceof GravityPlayer)
				((GravityPlayer)object).KickOff(event.getPlayer());
	}

	@EventHandler
	public void Jetpack(UpdateEvent event)
	{
		if (!InProgress())
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		for (GravityObject object : _objects)
			if (object instanceof GravityPlayer)
				((GravityPlayer)object).Jetpack();
	}

	@EventHandler
	public void Shoot(PlayerInteractEvent event)
	{
		if (!IsLive())
			return;

		if (event.isCancelled())
			return;

		if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (event.getPlayer().getItemInHand() == null)
			return;

		if (!event.getPlayer().getItemInHand().getType().toString().contains("IRON_SWORD"))
			return;

		Player player = event.getPlayer();

		if (!Recharge.Instance.use(player, "Sonic Blast", 1200, true, false))
			return;

		//Projectile
		Vector vel = player.getLocation().getDirection().multiply(1.2);

		Arrow arrow = player.getWorld().spawnArrow(
				player.getEyeLocation().add(player.getLocation().getDirection().multiply(2.5)).subtract(new Vector(0,0.8,0)), 
				player.getLocation().getDirection(), (float) vel.length(), 0f);
		arrow.setShooter(player);

		UtilEnt.ghost(arrow, true, true);

		_arrows.put(arrow, vel);

		event.setCancelled(true);

		/* OLD
		Vector velocity = player.getLocation().getDirection().multiply(1.2);

		FallingBlock projectile = player.getWorld().spawnFallingBlock(player.getEyeLocation().subtract(0, 1.2, 0).add(player.getLocation().getDirection().multiply(2)), Material.SKULL, (byte)0);

		_objects.add(new GravityBomb(this, projectile, 12, velocity, player));

		//Sound
		player.getWorld().playSound(player.getLocation(), Sound.EXPLODE, 0.5f, 2f);
		 */
	}

	@EventHandler
	public void HookFire(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (event.getPlayer().getItemInHand() == null)
			return;

		if (event.getPlayer().getItemInHand().getType() != Material.DIAMOND_SWORD)
			return;

		Player player = event.getPlayer();

		GravityObject old = _hooks.remove(player);
		if (old != null)
		{
			old.Clean();
			_objects.remove(old);

			//Inform
			UtilPlayer.message(player, F.main("Skill", "You detatched from your " + F.skill("Grappling Hook") + "."));

			//Sound
			player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_METAL, 0.75f, 2f);

			return;
		}

		if (!Recharge.Instance.use(player, "Hookshot", 12000, true, false))
			return;	

		//Projectile
		Vector velocity = player.getLocation().getDirection().multiply(0.4);

		this.CreatureAllowOverride = true;
		Slime slime = player.getWorld().spawn(player.getEyeLocation().add(player.getLocation().getDirection().multiply(2)), Slime.class);
		this.CreatureAllowOverride = false;

		slime.setSize(1);
		UtilEnt.vegetate(slime, true);
		UtilEnt.ghost(slime, true, false);

		GravityHook hook = new GravityHook(this, slime, 4, velocity);

		UtilEnt.leash(hook.Base, player, false, false);

		_hooks.put(player, hook);

		_objects.add(hook);

		//Inform
		UtilPlayer.message(player, F.main("Skill", "You launched a " + F.skill("Grappling Hook") + "."));

		//Sound
		player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_METAL, 0.75f, 1.5f);
	}

	@EventHandler
	public void HookUpdate(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		Iterator<Player> hookIterator = _hooks.keySet().iterator();

		while (hookIterator.hasNext())
		{
			Player player = hookIterator.next();
			GravityObject obj = _hooks.get(player);

			if (!player.isValid() || !obj.Ent.isValid())
			{
				obj.Clean();
				hookIterator.remove();
				continue;
			}

			//Dont Disappear
			if (obj.Ent instanceof FallingBlock)
				((CraftFallingSand)obj.Ent).getHandle().ticksLived = 1;

			HashMap<Block, Double> blocks = UtilBlock.getInRadius(obj.Ent.getLocation(), 1.2);

			double bestDist = 0;
			Block bestBlock = null;

			for (Block block : blocks.keySet())
			{
				if (UtilBlock.airFoliage(block))
					continue;

				double dist = blocks.get(block);

				if (bestBlock == null || dist > bestDist)
				{
					bestBlock = block;
					bestDist = dist;
				}
			}

			if (bestBlock == null)
			{
				//Too Far
				if (UtilMath.offset(player, obj.Ent) > 16)
				{
					obj.Clean();
					hookIterator.remove();

					//Inform
					UtilPlayer.message(player, F.main("Skill", "Your " + F.skill("Grappling Hook") + " missed."));
				}

				continue;
			}


			//Reel In
			if (player.isBlocking() && player.getItemInHand().getType() == Material.DIAMOND_SWORD)
			{
				for (GravityObject object : _objects)
					if (object instanceof GravityPlayer)
					{
						GravityPlayer pObj = (GravityPlayer)object; 

						if (pObj.Ent.equals(player))
						{
							pObj.AddVelocity(UtilAlg.getTrajectory(player, obj.Ent).multiply(0.03), 0.6);

							//Sound
							player.getWorld().playSound(player.getLocation(), Sound.NOTE_STICKS, 0.3f, 2f);
						}
					}
			}
			else if (UtilMath.offset(player, obj.Ent) > 16)
			{
				double power = (double)(UtilMath.offset(player, obj.Ent) - 16) / 200d;

				for (GravityObject object : _objects)
					if (object instanceof GravityPlayer)
					{
						GravityPlayer pObj = (GravityPlayer)object; 

						if (pObj.Ent.equals(player))
						{
							pObj.AddVelocity(UtilAlg.getTrajectory(player, obj.Ent).multiply(power), 0.6);

							//Sound
							player.getWorld().playSound(player.getLocation(), Sound.NOTE_STICKS, 0.3f, 0.6f - (float)power);
						}
					}
			}

			_objects.remove(obj);
			obj.Base.setVelocity(new Vector(0,0,0));
		}
	}

	@EventHandler
	public void ObjectUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		if (!InProgress() && GetState() != GameState.End)
			return;

		//Movement + Invalid
		Iterator<GravityObject> objectIterator = _objects.iterator();

		while (objectIterator.hasNext())
		{
			GravityObject obj = objectIterator.next();

			obj.Update();

			if (!obj.Update())
			{
				obj.Clean();
				objectIterator.remove();
			}
		}

		//Player AutoGrab
		for (GravityObject object : _objects)
			if (object instanceof GravityPlayer)
				((GravityPlayer)object).AutoGrab();

		//Collision
		for (GravityObject a : _objects)
			for (GravityObject b : _objects)
				a.Collide(b);

		//Bomb Detonate
		HashSet<GravityDebris> newDebris = new HashSet<GravityDebris>();
		objectIterator = _objects.iterator();
		while (objectIterator.hasNext())
		{
			GravityObject obj = objectIterator.next();

			if (!(obj instanceof GravityBomb))
				continue;

			HashSet<GravityDebris> debris = ((GravityBomb)obj).BombDetonate();

			if (debris != null && !debris.isEmpty())
			{
				newDebris.addAll(debris);
				objectIterator.remove();
				obj.CustomCollide(null);
				obj.Clean();
			}
		}
		_objects.addAll(newDebris);
	}

	@EventHandler
	public void Damage(CustomDamageEvent event)
	{
		if (event.GetCause() != DamageCause.CUSTOM && event.GetCause() != DamageCause.VOID)
			event.SetCancelled("No Damage");
	}

	@EventHandler
	public void FallingBlockLand(EntityChangeBlockEvent event)
	{
		if (event.getEntity() instanceof FallingBlock)
		{
			event.setCancelled(true);
			event.getEntity().remove();
		}
	}

	@EventHandler
	public void OxygenSuffocate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		if (!IsLive())
			return;

		for (GravityObject object : _objects)
			if (object instanceof GravityPlayer)
				((GravityPlayer)object).Oxygen();
	}

	public ArrayList<GravityObject> GetObjects() 
	{
		return _objects;
	}

	@EventHandler
	public void BowShoot(EntityShootBowEvent event)
	{
		Player shooter = (Player)event.getEntity();

		Vector vel = event.getProjectile().getVelocity();
		vel.multiply(0.6);

		Arrow arrow = shooter.getWorld().spawnArrow(
				shooter.getEyeLocation().add(shooter.getLocation().getDirection().multiply(1.5)).subtract(new Vector(0,0.8,0)), 
				shooter.getLocation().getDirection(), (float) vel.length(), 0f);
		arrow.setShooter(shooter);

		UtilEnt.ghost(arrow, true, true);

		_arrows.put(arrow, vel);

		event.setCancelled(true);
	}

	@EventHandler
	public void BowUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		Iterator<Arrow> arrowIterator = _arrows.keySet().iterator();

		while (arrowIterator.hasNext())
		{
			Arrow arrow = arrowIterator.next();

			for (GravityObject obj : _objects)
			{
				if (UtilMath.offset(obj.Base.getLocation().add(0, 0.5, 0), arrow.getLocation()) > obj.Size)
					continue;

				if (obj instanceof GravityPlayer)
				{
					if (obj.Ent.equals(arrow.getShooter()))
						continue;
				}

				BowExplode(arrow);
				break;
			}

			if (!arrow.isValid() || arrow.getTicksLived() > 200)
			{
				arrow.remove();
				arrowIterator.remove();
			}
			else
			{
				arrow.setVelocity(_arrows.get(arrow));
				UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, arrow.getLocation(), 0, 0, 0, 0, 1,
						ViewDist.MAX, UtilServer.getPlayers());
				arrow.getWorld().playSound(arrow.getLocation(), Sound.FIZZ, 0.3f, 0.5f);
			}
		}
	}

	@EventHandler
	public void BowHit(ProjectileHitEvent event)
	{
		if (!IsLive())
			return;
		
		BowExplode(event.getEntity());
	}

	public void BowExplode(Projectile proj)
	{
		//Blast Objs
		for (GravityObject obj : _objects)
		{
			if (UtilMath.offset(proj, obj.Base) > 3)
				continue;

			obj.AddVelocity(UtilAlg.getTrajectory(proj, obj.Base).multiply(0.4), 10);

			//obj.CustomCollide(null);

			if (obj.Ent instanceof Player)
			{
				//Damage Event
				Manager.GetDamage().NewDamageEvent((Player)obj.Ent, (LivingEntity)proj.getShooter(), null, 
						DamageCause.CUSTOM, 1, false, true, true,
						UtilEnt.getName((Entity)proj.getShooter()), "Sonic Blast");	

			}

			obj.GrabDelay = System.currentTimeMillis();
			obj.SetMovingBat(true);
		}

		//Blast Debris
		for (Block block : UtilBlock.getInRadius(proj.getLocation().add(0, 0.5, 0), 3d).keySet())
		{
			if (UtilBlock.airFoliage(block))
				continue;

			if (block.getType() == Material.EMERALD_BLOCK || block.getType() == Material.GOLD_BLOCK)
				continue;

			//Projectile
			Vector velocity = UtilAlg.getTrajectory(proj.getLocation(), block.getLocation().add(0.5, 0.5, 0.5));
			velocity.add(proj.getVelocity().normalize());
			velocity.add((new Vector(Math.random()-0.5,Math.random()-0.5,Math.random()-0.5)).multiply(0.5));
			velocity.multiply(0.3);

			//Block
			Material type = block.getType();
			byte data = block.getData();
			block.setType(Material.AIR);

			//Projectile
			FallingBlock projectile = block.getWorld().spawnFallingBlock(block.getLocation().add(0.5, 0.6, 0.5), type,data);
			GravityDebris newDebris = new GravityDebris(this, projectile, 12, velocity);

			//Add
			_objects.add(newDebris);
		}

		//Effect
		UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, proj.getLocation(), 0, 0, 0, 0, 1,
				ViewDist.MAX, UtilServer.getPlayers());
		proj.getWorld().playSound(proj.getLocation(), Sound.EXPLODE, 0.6f, 1.5f);

		//Remove
		proj.remove();	
	}

	@EventHandler
	public void PowerupUpdate(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.SEC)
			return;

		if (!UtilTime.elapsed(_lastPowerup, 15000))
			return;

		if (_powerup == null)
			_powerup = UtilAlg.Random(_powerups);

		else
		{
			FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.GREEN).with(Type.BALL).trail(false).build();
			UtilFirework.playFirework(_powerup, effect);
		}
	}

	@EventHandler
	public void PowerupCollect(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		if (_powerup == null)
			return;

		for (Player player : GetPlayers(true))
		{
			if (UtilMath.offset(player.getLocation(), _powerup) < 3)
			{
				UtilTextMiddle.display(null, C.cGreen + "Collected Jetpack Fuel", 0, 80, 5, player);
				
				player.setExp(Math.min(0.9999f, player.getExp() + 0.25f));

				player.getWorld().playSound(player.getLocation(), Sound.DRINK, 1f, 0.5f);

				_powerup = null;
				_lastPowerup = System.currentTimeMillis();

				break;
			}
		}
	}	
}
