package mineplex.hub.modules;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.event.StackerEvent;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.event.GadgetEnableEvent;
import mineplex.core.gadget.event.GadgetSelectLocationEvent;
import mineplex.core.gadget.gadgets.outfit.OutfitTeam;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.hub.HubManager;
import org.bukkit.Color;
import org.bukkit.EntityEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;

public class SoccerManager extends MiniPlugin
{
	private HubManager _hubManager;
	
	private HashSet<Player> _active = new HashSet<Player>();

	private ArrayList<OutfitTeam> _teamArmor = new ArrayList<OutfitTeam>();

	private Location _cornerFieldPlayerA;
	private Location _cornerFieldPlayerB;

	private Location _cornerGoalPlayerA;
	private Location _cornerGoalPlayerB;

	private Location _cornerFieldA;
	private Location _cornerFieldB;

	private Location _cornerRedGoalA;
	private Location _cornerRedGoalB;

	private Location _cornerBlueGoalA;
	private Location _cornerBlueGoalB;

	private int _blueGoals = 0;
	private int _redGoals = 0;

	private int _insideGoalTicks = 0;

	private Slime _ball;
	private Vector _ballVel;
	private long _ballDeadTime = -1;

	private String _lastRedKicker = "";
	private String _lastBlueKicker = "";
	private Color _lastKickColor = null;

	//Item Rebound
	protected Vector _vel;
	protected Location _lastLoc;
	protected ArrayList<Vector> _velHistory = new ArrayList<Vector>();

	public SoccerManager(HubManager hubManager, GadgetManager gadgetManager)
	{
		super("Football Manager", hubManager.getPlugin());

		_hubManager = hubManager;

		_cornerFieldPlayerA = new Location(hubManager.GetSpawn().getWorld(), -13.5, 67, -65.5);
		_cornerFieldPlayerB = new Location(hubManager.GetSpawn().getWorld(), 25.5, 100, -86.5);

		_cornerGoalPlayerA = new Location(hubManager.GetSpawn().getWorld(), -17.5, 67, -71.5);
		_cornerGoalPlayerB = new Location(hubManager.GetSpawn().getWorld(), 29.5, 100, -80.5);

		_cornerFieldA = new Location(hubManager.GetSpawn().getWorld(), -13.75, 67, -66.75);
		_cornerFieldB = new Location(hubManager.GetSpawn().getWorld(), 25.25, 100, -87.25);

		_cornerRedGoalA = new Location(hubManager.GetSpawn().getWorld(), 27.75, 66.5, -73.25);
		_cornerRedGoalB = new Location(hubManager.GetSpawn().getWorld(), 25.25, 74.5, -79.25);

		_cornerBlueGoalA = new Location(hubManager.GetSpawn().getWorld(), -15.75, 66.5, -79.75);
		_cornerBlueGoalB = new Location(hubManager.GetSpawn().getWorld(), -13.25, 74.5, -73.75);

		//Store Gadgets
		for (Gadget gadget : gadgetManager.getGadgets(GadgetType.COSTUME))
		{
			if (gadget instanceof OutfitTeam)
			{
				_teamArmor.add((OutfitTeam)gadget);
			}
		}
	}

	@EventHandler
	public void ballUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		if (_active.size() <= 0)
		{
			_blueGoals = 0;
			_redGoals = 0;

			if (_ball != null)
				_ball.remove();

				return;
		}

		//New Ball Needed
		if (_ballDeadTime == -1 && (_ball == null || !_ball.isValid()))
		{
			if (_ball != null)
				_ball.remove();

			_ballDeadTime = System.currentTimeMillis();
		}

		//Spawn Ball
		if (_ballDeadTime > 0 && UtilTime.elapsed(_ballDeadTime, 4000))
		{
			Location mid = UtilAlg.getMidpoint(_cornerFieldPlayerA, _cornerFieldPlayerB); 

			if (!mid.getChunk().isLoaded())
				return;

			_ball =  mid.getWorld().spawn(mid, Slime.class);
			_ball.setSize(2);
			
			UtilEnt.vegetate(_ball);
			UtilEnt.ghost(_ball, true, false);

			_ballVel = new Vector(0,-0.1,0);

			UtilFirework.playFirework(mid, Type.BALL, Color.YELLOW, false, false);

			_ballDeadTime = -1;

			_vel = new Vector(0,-0.1,0);
			_velHistory.add(_vel);
			_lastLoc = _ball.getLocation();
		}

		if (_ball == null)
			return;
		
		if (_lastKickColor == Color.AQUA)
		{
			for (int i = 0 ; i < 3 ; i++)
				UtilParticle.PlayParticle(ParticleType.RED_DUST, _ball.getLocation().add(0.0, 0.5, 0.0), -1, 1, 1, 1, 0,
						ViewDist.NORMAL, UtilServer.getPlayers());
		}
		else
		{
			for (int i = 0 ; i < 3 ; i++)
				UtilParticle.PlayParticle(ParticleType.RED_DUST, _ball.getLocation().add(0.0, 0.5, 0.0), 0, 0, 0, 0, 1,
						ViewDist.NORMAL, UtilServer.getPlayers());
		}
		
		//Kick
		for (Player player : _active)
		{
			if (UtilMath.offset(player, _ball) < 1.5 ||
				UtilMath.offset(player.getEyeLocation(), _ball.getLocation()) < 1.25)
			{
				if (Recharge.Instance.use(player, "Football Kick", 600, false, false))
				{
					_ballVel = player.getLocation().getDirection();
					_ballVel.setY(_ballVel.getY() + 0.4);

					if (UtilEnt.isGrounded(_ball) && _ballVel.getY() <= 0)
						_ballVel.setY(0);
					
					_ballVel.setY(Math.min(_ballVel.getY(), 1));

					_ball.getWorld().playSound(_ball.getLocation(), Sound.ZOMBIE_WOOD, 0.5f, 1.5f);

					UtilParticle.PlayParticle(ParticleType.SLIME, 
							UtilAlg.getMidpoint(_ball.getLocation(), player.getLocation()), 
							0, 0, 0, 0, 5, ViewDist.NORMAL, UtilServer.getPlayers());

					UtilAction.zeroVelocity(player);

					_lastKickColor = getTeamColor(player);
					if (_lastKickColor == Color.RED)
					{
						_lastRedKicker = C.cRed + player.getName();
						
						if (_lastBlueKicker.length() == 0)
							_lastBlueKicker = C.cRed + player.getName();
					}
						
					else if (_lastKickColor == Color.AQUA)
					{
						_lastBlueKicker = C.cAqua + player.getName();
						
						if (_lastRedKicker.length() == 0)
							_lastRedKicker = C.cRed + player.getName();
					}	
				}
			}
		}

		//Blue Goal
		if (UtilAlg.inBoundingBox(_ball.getLocation(), _cornerRedGoalA, _cornerRedGoalB) &&
				!UtilAlg.inBoundingBox(_ball.getLocation(), _cornerFieldA, _cornerFieldB))
		{
			_insideGoalTicks++;

			if (_insideGoalTicks > 3)
			{
				UtilFirework.playFirework(_ball.getLocation(), Type.BALL, Color.AQUA, true, true);

				_blueGoals++;

				for (Player player : _active)
					UtilTextMiddle.display("Goal! (" + C.cRed + _redGoals + C.cWhite + " : " + C.cBlue + _blueGoals + C.cWhite + ")", 
							_lastBlueKicker + C.cWhite + " scored for " + C.cAqua + "Blue Team", 0, 70, 20, player);

				_ball.remove();
				_ball = null;
				
				_lastRedKicker = "";
				_lastBlueKicker = "";
			}

			return;
		}

		//Red Goal
		else if (UtilAlg.inBoundingBox(_ball.getLocation(), _cornerBlueGoalA, _cornerBlueGoalB) &&
				!UtilAlg.inBoundingBox(_ball.getLocation(), _cornerFieldA, _cornerFieldB))
		{
			_insideGoalTicks++;

			if (_insideGoalTicks > 3)
			{
				UtilFirework.playFirework(_ball.getLocation(), Type.BALL, Color.RED, true, true);

				_redGoals++;

				for (Player player : _active)
					UtilTextMiddle.display("Goal! (" + C.cRed + _redGoals + C.cWhite + " : " + C.cBlue + _blueGoals + C.cWhite + ")", 
							_lastRedKicker + C.cWhite + " scored for " + C.cRed + "Red Team", 0, 70, 20, player);

				_ball.remove();
				_ball = null;
				
				_lastRedKicker = "";
				_lastBlueKicker = "";
			}

			return;
		}
		else
		{
			_insideGoalTicks = 0;
		}

		//Wind Drag
		_ballVel = _ballVel.multiply(0.99);

		//Ground Drag
		if (UtilEnt.isGrounded(_ball))
			_ballVel = _ballVel.multiply(0.98);

		//Rebound Y
		if (UtilEnt.isGrounded(_ball))
		{
			if (_ballVel.getY() < -0.1)
			{
				_ballVel.setY(_ballVel.getY() * -0.65);
			}
		}
		
		//Gravity
		if (!UtilEnt.isGrounded(_ball))
			_ballVel.setY(_ballVel.getY() - 0.04);

		//Rebound X
		if ((_ballVel.getX() > 0 && _ball.getLocation().getX() > Math.max(_cornerFieldA.getX(), _cornerFieldB.getX())) ||
				(_ballVel.getX() < 0 && _ball.getLocation().getX() < Math.min(_cornerFieldA.getX(), _cornerFieldB.getX())))
		{
			_ballVel.setX(_ballVel.getX() * -1);

			_ballVel = _ballVel.multiply(0.9);

			_ball.getWorld().playSound(_ball.getLocation(), Sound.ZOMBIE_WOOD, 0.5f, 1.5f);
			UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, _ball.getLocation(), 0, 0, 0, 0, 1, ViewDist.NORMAL, UtilServer.getPlayers());
		}

		//Rebound Z
		if ((_ballVel.getZ() > 0 && _ball.getLocation().getZ() > Math.max(_cornerFieldA.getZ(), _cornerFieldB.getZ())) || 
				(_ballVel.getZ() < 0 && _ball.getLocation().getZ() < Math.min(_cornerFieldA.getZ(), _cornerFieldB.getZ())))
		{
			_ballVel.setZ(_ballVel.getZ() * -1);

			_ballVel = _ballVel.multiply(0.9);

			_ball.getWorld().playSound(_ball.getLocation(), Sound.ZOMBIE_WOOD, 0.5f, 1.5f);
			UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, _ball.getLocation(), 0, 0, 0, 0, 1, ViewDist.NORMAL, UtilServer.getPlayers());
		}

		//Move Ball
		_ball.setVelocity(_ballVel);
	}

	public Color getTeamColor(Player player)
	{
		//All peices are always same color!
		for (OutfitTeam outfit : _teamArmor)
		{
			if (outfit.isActive(player))
				return outfit.getTeamColor(player);
		}

		return null;
	}

	public boolean inPlayerArena(Entity entity)
	{
		if(!entity.getWorld().equals(_cornerFieldA.getWorld())) return false;
		
		return UtilAlg.inBoundingBox(entity.getLocation(), _cornerFieldPlayerA, _cornerFieldPlayerB) || 
				UtilAlg.inBoundingBox(entity.getLocation(), _cornerGoalPlayerA, _cornerGoalPlayerB);
	}
	
	@EventHandler
	public void clean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;

		for (Entity ent : _cornerFieldA.getWorld().getEntities())
		{
			if (ent instanceof Player)
				continue;
			
			if (_ball != null && _ball.equals(ent))
				continue;

			if (inPlayerArena(ent))
			{
				if (ent instanceof Bat || ent instanceof WitherSkull || ent instanceof TNTPrimed || ent instanceof Firework)
				{
					ent.remove();
					continue;
				}
				
				Location bounce = UtilAlg.getMidpoint(_cornerFieldPlayerA, _cornerFieldPlayerB);
				bounce.setY(Math.min(_cornerFieldPlayerA.getY(), _cornerFieldPlayerB.getY()));

				Entity bottom = ent;
				if (bottom.getVehicle() != null)
					bottom = bottom.getVehicle();

				UtilAction.velocity(bottom, UtilAlg.getTrajectory(bounce, ent.getLocation()), 1, false, 0, 0.4, 1, true);
			}
		}
	}

	@EventHandler
	public void joinLeaveGame(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			if (_active.contains(player))
			{
				if (!inPlayerArena(player))
				{
					setSoccerMode(player, false, null);
					continue;
				}

				//Took armor off
				Color color = getTeamColor(player);
				if (color == null || (color != Color.RED && color != Color.AQUA))
				{
					setSoccerMode(player, false, null);
				}
			}
			else
			{
				if (inPlayerArena(player))
				{					
					Color color = getTeamColor(player);
					
					//Join
					if (color != null && (color == Color.RED || color == Color.AQUA) && _active.size() < 8)
					{
						setSoccerMode(player, true, color);
					}
					//Eject
					else
					{
						Location bounce = UtilAlg.getMidpoint(_cornerFieldPlayerA, _cornerFieldPlayerB);
						bounce.setY(Math.min(_cornerFieldPlayerA.getY(), _cornerFieldPlayerB.getY()));

						Entity bottom = player;
						if (bottom.getVehicle() != null)
							bottom = bottom.getVehicle();

						UtilAction.velocity(bottom, UtilAlg.getTrajectory2d(bounce, player.getLocation()), 1, false, 0, 0.8, 1, true);

						if (Recharge.Instance.use(player, "Soccer Eject", 5000, false, false))
						{
							if (_active.size() < 8)
							{
								UtilPlayer.message(player, F.main("Slimeball", "You must be wearing Red/Blue Team Outfit."));
								UtilPlayer.message(player, F.main("Slimeball", "Type " + F.elem("/team red") + " or " + F.elem("/team blue") + "!"));
							}
							else
							{
								UtilPlayer.message(player, F.main("Slimeball", "The game is currently full!"));
							}
						}
					}
				}
			}
		}
	}

	public void setSoccerMode(Player player, boolean enabled, Color color)
	{
		if (enabled)
		{
			_active.add(player);

			if (color == null)
				UtilPlayer.message(player, F.main("Slimeball", "You have entered " + F.elem("Slimeball Mode") + "."));
			else if (color == Color.RED)
				UtilPlayer.message(player, F.main("Slimeball", "You have joined " + F.elem(C.cRed + "Red Slimeball Team") + "."));
			else if (color == Color.AQUA)
				UtilPlayer.message(player, F.main("Slimeball", "You have joined " + F.elem(C.cAqua + "Blue Slimeball Team") + "."));

			ArrayList<String> outfit = new ArrayList<String>();
			outfit.add("Team Helmet");
			outfit.add("Team Shirt");
			outfit.add("Team Pants");
			outfit.add("Team Boots");

			_hubManager.GetGadget().disableAll(player, outfit);
			_hubManager.getPetManager().disableAll(player);
		}
		else
		{
			_active.remove(player);
			UtilPlayer.message(player, F.main("Parkour", "You have exited " + F.elem("Slimeball Mode") + "."));
		}
	}

	public boolean isSoccerMode(Entity entity)
	{
		return _active.contains(entity);
	}

	@EventHandler
	public void disableGadgets(GadgetEnableEvent event)
	{
		if (isSoccerMode(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerGrabSlime(PlayerInteractEntityEvent event)
	{
		if (_active.contains(event.getPlayer()))
			event.setCancelled(true);
		
		if (_ball != null && event.getRightClicked().equals(_ball))
			event.setCancelled(true);
	}

	@EventHandler
	public void disableStacker(StackerEvent event)
	{
		if (isSoccerMode(event.getEntity()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void disableGuardianLazer(GadgetSelectLocationEvent event)
	{
		if (UtilAlg.inBoundingBox(event.getLocation(), _cornerFieldA, _cornerFieldB))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void disableVelocity(PlayerVelocityEvent event)
	{
		// Disable velocity but allow double jumping.

		if (isSoccerMode(event.getPlayer()) && !_hubManager.getJumpManager().hasDoubleJumped(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_active.remove(event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void allowDamage(EntityDamageByEntityEvent event)
	{
		if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player))
			return;

		Player damager = (Player)event.getDamager();
		Player damagee = (Player)event.getEntity();

		if (!_active.contains(damager) || !_active.contains(damagee))
			return;
		
		if (getTeamColor(damager) == null || getTeamColor(damagee) == null)
			return;
		
		if (getTeamColor(damager) == getTeamColor(damagee))
			return;

		if (Recharge.Instance.use(damagee, "Football Damage", 800, false, false))
		{
			UtilAction.velocity(damagee, UtilAlg.getTrajectory2d(damager, damagee), 0.6, false, 0, 0.3, 1, true);
			
			damagee.playEffect(EntityEffect.HURT);
		}
	}
}
