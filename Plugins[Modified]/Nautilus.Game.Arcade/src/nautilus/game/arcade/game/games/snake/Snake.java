package nautilus.game.arcade.game.games.snake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseSheep;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GamePrepareCountdownCommence;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.SoloGame;
import nautilus.game.arcade.game.games.snake.events.SlimeUpgradeEvent;
import nautilus.game.arcade.game.games.snake.events.TailGrowEvent;
import nautilus.game.arcade.game.games.snake.kits.KitInvulnerable;
import nautilus.game.arcade.game.games.snake.kits.KitReverser;
import nautilus.game.arcade.game.games.snake.kits.KitSpeed;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.managers.chat.ChatStatData;
import nautilus.game.arcade.stats.ChooChooStatTracker;
import nautilus.game.arcade.stats.KillsWithinGameStatTracker;
import nautilus.game.arcade.stats.SlimySheepStatTracker;

public class Snake extends SoloGame
{
	private double _maxSpeed = 180;

	private HashMap<Player, ArrayList<Creature>> _tail = new HashMap<Player, ArrayList<Creature>>();
	private HashSet<Entity> _food = new HashSet<Entity>();

	private HashMap<Player, DyeColor> _color = new HashMap<Player, DyeColor>();

	private HashMap<Player, Long> _invul = new HashMap<Player, Long>();
	private HashMap<Player, Long> _speed = new HashMap<Player, Long>();
	private HashMap<Player, Long> _reverse = new HashMap<Player, Long>();

	private HashMap<Player, Location> _move = new HashMap<Player, Location>();
	private HashMap<Player, Long> _moveTime = new HashMap<Player, Long>();

	public Snake(ArcadeManager manager)
	{
		super(manager, GameType.Snake,

				new Kit[]
						{
								new KitSpeed(manager),
								new KitInvulnerable(manager),
								new KitReverser(manager)
						},

				new String[]
						{
								"Avoid hitting snake tails",
								"You get faster as you grow longer",
								"Eat slimes to grow faster",
								"Last one alive wins!"
						});

		this.DamageTeamSelf = true;

		this.HungerSet = 2;

		this.GemMultiplier = 0.5;

		registerStatTrackers(
				new KillsWithinGameStatTracker(this, 6, "Cannibal"),
				new ChooChooStatTracker(this),
				new SlimySheepStatTracker(this)
		);

		registerChatStats(//slimes eaten, kills, length
				new ChatStatData("Cannibal", "Kills", true),
				BlankLine,
				new ChatStatData("ChooChoo", "Tail Length", true),
				new ChatStatData("SlimySheep", "Slimes Eaten", true),
				BlankLine,
				new ChatStatData("kit", "Kit", true)
		);

		new CompassModule()
				.setGiveCompass(true)
				.setGiveCompassToSpecs(true)
				.setGiveCompassToAlive(false)
				.register(this);
	}

	@EventHandler
	public void CreateSheep(GamePrepareCountdownCommence event)
	{
		for (int i = 0; i < GetPlayers(true).size(); i++)
		{
			Player player = GetPlayers(true).get(i);
			_color.put(player, DyeColor.getByDyeData((byte) (i % 16)));

			this.CreatureAllowOverride = true;
			Sheep sheep = player.getWorld().spawn(player.getLocation(), Sheep.class);
			UtilEnt.addFlag(sheep, UtilEnt.FLAG_ENTITY_COMPONENT);
			this.CreatureAllowOverride = false;

			sheep.setColor(DyeColor.getByDyeData((byte) (i % 16)));
			sheep.setPassenger(player);

			UtilEnt.vegetate(sheep);

			_tail.put(player, new ArrayList<Creature>());
			_tail.get(player).add(sheep);
		}

		for (Player player : GetPlayers(true))
		{
			player.playEffect(this.GetSpectatorLocation(), Effect.RECORD_PLAY, 2259);
		}
	}

	@EventHandler
	public void ThirdPerson(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
			return;

		this.Announce(C.cYellow + C.Scramble + "@@" + C.cAqua + C.Bold + " Snake is best played in 3rd Person! (Push F5) " + C.cYellow + C.Scramble + "@@");
	}

	@EventHandler
	public void AutoGrow(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.SEC)
			return;

		for (Player player : GetPlayers(true))
		{
			Grow(player, 1, false);
		}
	}

	@EventHandler
	public void ReSit(UpdateEvent event)
	{
		if (!InProgress())
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : GetPlayers(true))
		{
			if (player.getVehicle() != null)
				continue;

			if (!_tail.containsKey(player))
				continue;

			if (_tail.get(player).isEmpty())
				continue;

			_tail.get(player).get(0).setPassenger(player);
		}
	}

	@EventHandler
	public void Move(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : GetPlayers(true))
		{
			if (!_tail.containsKey(player))
				continue;
			
			if (_reverse.containsKey(player))
				continue;

			double mult = 0.4;

			if (_tail.containsKey(player))
				mult += Math.min(0.7, (double) _tail.get(player).size() / _maxSpeed);

			if (_speed.containsKey(player))
				mult = mult * 1.5;

			Vector vel = player.getLocation().getDirection().setY(0).normalize().multiply(4);

			Creature before = null;
			for (int i = 0; i < _tail.get(player).size(); i++)
			{
				Creature tail = _tail.get(player).get(i);

				Location loc = player.getLocation().add(vel);

				//First
				if (i == 0)
					loc = tail.getLocation().add(vel);

				//Others
				if (before != null)
					loc = before.getLocation();

				//Teleport
				if (before != null)
				{
					Location tp = before.getLocation().add(UtilAlg.getTrajectory2d(before, tail).multiply(1.4));
					tp.setPitch(tail.getLocation().getPitch());
					tp.setYaw(tail.getLocation().getYaw());
					tail.teleport(tp);
				}

				//Move
				UtilEnt.CreatureMoveFast(tail, loc, (float) (1.2 + 2f * mult));

				//Store
				before = tail;
			}
		}
	}

	@EventHandler
	public void Idle(UpdateEvent event)
	{
		if (!IsLive())
			return;

		for (Player player : GetPlayers(true))
		{
			if (!_tail.containsKey(player))
			{
				KillPlayer(player, null, "No Tail");
				continue;
			}

			if (!_move.containsKey(player) || UtilMath.offset(_tail.get(player).get(0).getLocation(), _move.get(player)) > 2)
			{
				_move.put(player, _tail.get(player).get(0).getLocation());
				_moveTime.put(player, System.currentTimeMillis());
				continue;
			}

			if (UtilTime.elapsed(_moveTime.get(player), 2000))
				KillPlayer(player, null, "Idle");
		}
	}

	@EventHandler
	public void Collide(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		//Hit Tail
		for (Player player : GetPlayers(true))
		{
			if (_invul.containsKey(player))
				continue;

			if (_reverse.containsKey(player))
				continue;
			
			boolean done = false;
			for (Player other : _tail.keySet())
			{
				int start = 0;
				if (other.equals(player))
					start = 3;

				for (int i = start; i < _tail.get(other).size(); i++)
				{
					Creature tail = _tail.get(other).get(i);

					if (UtilMath.offset(_tail.get(player).get(0), tail) < 1.2)
					{
						UtilParticle.PlayParticle(ParticleType.EXPLODE, tail.getLocation().add(0, 1, 0), 0, 0, 0, 0, 1,
								ViewDist.LONG, UtilServer.getPlayers());
						
						KillPlayer(player, other, null);

						done = true;
						break;
					}
				}

				if (done)
					break;
			}
		}
	}

	public void KillPlayer(Player player, Player killer, String type)
	{
		if (killer != null)
		{
			//Damage Event
			Manager.GetDamage().NewDamageEvent(player, killer, null,
					DamageCause.CUSTOM, 500, false, true, false,
					killer.getName(), "Snake Tail");
		}
		else
		{
			//Damage Event
			Manager.GetDamage().NewDamageEvent(player, null, null,
					DamageCause.CUSTOM, 500, false, true, false,
					type, type);
		}

		if (_tail.containsKey(player))
		{
			for (Creature cur : _tail.get(player))
			{
				cur.playEffect(EntityEffect.HURT);
				cur.remove();
			}

			_tail.get(player).clear();
		}
	}

	@EventHandler
	public void SpawnFood(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.SEC)
			return;

		Location loc = GetTeamList().get(0).GetSpawns().get(UtilMath.r(GetTeamList().get(0).GetSpawns().size()));

		loc.setX(-48 + UtilMath.r(97));
		loc.setZ(-48 + UtilMath.r(97));

		if (!UtilBlock.airFoliage(loc.getBlock()))
			return;

		if (UtilMath.offset(loc, this.GetSpectatorLocation()) > 48)
			return;

		//Spawn
		this.CreatureAllowOverride = true;
		Slime pig = loc.getWorld().spawn(loc, Slime.class);
		this.CreatureAllowOverride = false;
		pig.setSize(2);
		UtilEnt.vegetate(pig);

		_food.add(pig);
	}

	@EventHandler
	public void EatFood(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		Iterator<Entity> foodIterator = _food.iterator();

		while (foodIterator.hasNext())
		{
			Entity food = foodIterator.next();

			if (!food.isValid())
			{
				food.remove();
				foodIterator.remove();
				continue;
			}

			for (Player player : GetPlayers(true))
			{
				if (UtilMath.offset(food, player) < 2)
				{
					int amount = 2;

					Grow(player, amount, true);
					foodIterator.remove();
					food.remove();

					Bukkit.getPluginManager().callEvent(new SlimeUpgradeEvent(player));

					break;
				}
			}
		}
	}

	public void Grow(Player player, int amount, boolean sound)
	{
		while (amount > 0)
		{
			//Ensure
			Location loc = player.getLocation();
			if (!_tail.get(player).isEmpty())
				loc = _tail.get(player).get(_tail.get(player).size() - 1).getLocation();

			if (_tail.get(player).size() > 1)
				loc.add(UtilAlg.getTrajectory2d(_tail.get(player).get(_tail.get(player).size() - 2), _tail.get(player).get(_tail.get(player).size() - 1)));
			else
				loc.subtract(player.getLocation().getDirection().setY(0));

			//Spawn
			this.CreatureAllowOverride = true;
			Sheep tail = loc.getWorld().spawn(loc, Sheep.class);
			UtilEnt.addFlag(tail, UtilEnt.FLAG_ENTITY_COMPONENT);
			this.CreatureAllowOverride = false;

			tail.setRemoveWhenFarAway(false);
			tail.setColor(_color.get(player));

			//Sets yaw/pitch
			tail.teleport(loc);

			UtilEnt.vegetate(tail);
			UtilEnt.ghost(tail, true, false);

			_tail.get(player).add(tail);

			//Audio
			if (sound)
				player.getWorld().playSound(player.getLocation(), Sound.EAT, 2f, 1f);

			amount--;

			Bukkit.getPluginManager().callEvent(new TailGrowEvent(player, _tail.get(player).size()));
		}

		player.setExp((float) Math.min(0.9999f, (double) _tail.get(player).size() / _maxSpeed));
	}

	@EventHandler
	public void DamageCancel(CustomDamageEvent event)
	{
		if (event.GetCause() == DamageCause.LAVA)
		{
			if (event.GetDamageePlayer() != null)
			{
				KillPlayer(event.GetDamageePlayer(), null, "Lava");
				return;
			}
		}

		if (event.GetCause() != DamageCause.CUSTOM)
			event.SetCancelled("Snake Damage");
	}

	@EventHandler
	public void TargetCancel(EntityTargetEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void CombustCancel(EntityCombustEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void InvulnerabilityUse(PlayerInteractEvent event)
	{
		if (!IsLive())
			return;

		Player player = event.getPlayer();

		if (!IsAlive(player))
			return;

		if (!UtilGear.isMat(event.getPlayer().getItemInHand(), Material.NETHER_STAR))
			return;

		if (!Recharge.Instance.use(player, "Snake Item", 1000, false, false))
			return;

		UtilInv.remove(player, Material.NETHER_STAR, (byte) 0, 1);

		_invul.put(player, System.currentTimeMillis());
	}

	@EventHandler
	public void InvulnerabilityUpdate(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		Iterator<Player> invIterator = _invul.keySet().iterator();

		while (invIterator.hasNext())
		{
			Player player = invIterator.next();

			if (UtilTime.elapsed(_invul.get(player), 2000))
			{
				//Self Color
				DisguiseBase disguise = Manager.GetDisguise().getDisguise(player);
				if (disguise != null && disguise instanceof DisguiseSheep)
					((DisguiseSheep) disguise).setColor(_color.get(player));

				//Tail Color
				if (_tail.containsKey(player))
					for (Creature ent : _tail.get(player))
						if (ent instanceof Sheep)
							((Sheep) ent).setColor(_color.get(player));

				invIterator.remove();
				continue;
			}

			DyeColor col = GetColor();

			//Self Color
			DisguiseBase disguise = Manager.GetDisguise().getDisguise(player);
			if (disguise != null && disguise instanceof DisguiseSheep)
				((DisguiseSheep) disguise).setColor(col);

			//Tail Color
			if (_tail.containsKey(player))
				for (Creature ent : _tail.get(player))
					if (ent instanceof Sheep)
						((Sheep) ent).setColor(col);
		}
	}

	public DyeColor GetColor()
	{
		double r = Math.random();

		if (r > 0.75) return DyeColor.RED;
		else if (r > 0.5) return DyeColor.YELLOW;
		else if (r > 0.25) return DyeColor.GREEN;
		else return DyeColor.BLUE;
	}

	@EventHandler
	public void SpeedUse(PlayerInteractEvent event)
	{
		if (!IsLive())
			return;

		Player player = event.getPlayer();

		if (!IsAlive(player))
			return;

		if (!UtilGear.isMat(event.getPlayer().getItemInHand(), Material.FEATHER))
			return;

		if (!Recharge.Instance.use(player, "Snake Item", 1000, false, false))
			return;

		UtilInv.remove(player, Material.FEATHER, (byte) 0, 1);

		_speed.put(player, System.currentTimeMillis());
	}

	@EventHandler
	public void SpeedUpdate(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		Iterator<Player> speedIterator = _speed.keySet().iterator();

		while (speedIterator.hasNext())
		{
			Player player = speedIterator.next();

			if (UtilTime.elapsed(_speed.get(player), 2000))
			{
				speedIterator.remove();
				continue;
			}
		}
	}
	
	@EventHandler
	public void ReverseUse(PlayerInteractEvent event)
	{
		if (!IsLive())
			return;

		Player player = event.getPlayer();

		if (!IsAlive(player))
			return;

		if (!UtilGear.isMat(event.getPlayer().getItemInHand(), Material.COOKIE))
			return;

		if (!Recharge.Instance.use(player, "Snake Item", 1000, false, false))
			return;

		UtilInv.remove(player, Material.COOKIE, (byte) 0, 1);

		_reverse.put(player, System.currentTimeMillis());
		
		//Leave
		player.leaveVehicle();
		
		//Reverse Tail
		ArrayList<Creature> newTail = new ArrayList<Creature>();
		for (int i=_tail.get(player).size()-1 ; i>=0 ; i--)
			newTail.add(_tail.get(player).get(i));
		_tail.put(player, newTail);

		//Resit
		_tail.get(player).get(0).setPassenger(player);
	}

	@EventHandler
	public void ReverseUpdate(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		Iterator<Player> speedIterator = _reverse.keySet().iterator();

		while (speedIterator.hasNext())
		{
			Player player = speedIterator.next();

			//Dont idle kill
			_move.put(player, _tail.get(player).get(0).getLocation());
			_moveTime.put(player, System.currentTimeMillis());
			
			if (UtilTime.elapsed(_reverse.get(player), 2000))
			{
				speedIterator.remove();
				continue;
			}
		}
	}
	
	@EventHandler
	public void handleInteractEntityPacket(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Dead)
		{
			_tail.clear();
			_food.clear();
			_color.clear();
			_invul.clear();
			_speed.clear();
			_reverse.clear();
			_move.clear();
			_moveTime.clear();
			
			HandlerList.unregisterAll(this);
		}
	}
}
