package mineplex.hub.modules.mavericks.basketball;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.hub.HubManager;
import net.minecraft.server.v1_8_R3.BlockPosition;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;

/**
 * Instance class for running a basketball game in the hub
 */
public class BasketballGame implements Listener
{
	private HubManager _hub;
	private World _world;
	private List<Player> _players = Lists.newArrayList();
	private Entity _ball;
	private Player _dribbling;
	private ThrowData _throwData;
	
	private static final double THREE_POINTER_DISTANCE = 27;
	
	private boolean _frozen = false;
	private long _lastDribbleAnim = 0;
	private long _lastDribbleMove = 0;
	private long _start;
	
	private HashMap<BasketballTeam, Block> _hoops = new HashMap<>();
	
	private double _maxX = 0;
	private double _minX = 0;
	private double _maxZ = 0;
	private double _minZ = 0;
	
	private double _velocity = -7;
	
	private int _redScore;
	private int _blueScore;
	
	public BasketballGame(JavaPlugin plugin, HubManager hub, World world)
	{
		_hub = hub;
		_world = world;
		setupMap();
		Bukkit.getPluginManager().registerEvents(this, plugin);
		beginGame();
		_start = System.currentTimeMillis();
	}
	
	private Entity spawnBall(Location loc)
	{
		_velocity = -7;
		Entity e = loc.getWorld().spawnEntity(loc, EntityType.SLIME);
		UtilEnt.vegetate(e, true);
		UtilEnt.ghost(e, true, false);
		((Slime)e).setSize(1);
		
		return e;
	}
	
	private BasketballTeam getTeam(Player player)
	{
		if (player.getInventory().getHelmet() == null || !(player.getInventory().getHelmet().getItemMeta() instanceof LeatherArmorMeta))
		{
			return null;
		}
		Color color = ((LeatherArmorMeta)player.getInventory().getHelmet().getItemMeta()).getColor();
		return BasketballTeam.getFromColor(color);
	}
	
	private boolean checkCatching()
	{
		if (_ball != null)
		{
			for (Player player : _players)
			{
				if (UtilMath.offset(player, _ball) <= 1.5)
				{
					if (Recharge.Instance.usable(player, "Catch Ball", false))
					{
						catchBall(player);
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	private int getWorth(Location start, Location hoop)
	{
		if (UtilMath.offset2d(start, hoop) >= THREE_POINTER_DISTANCE)
		{
			return 3;
		}
		
		return 2;
	}
	
	private void reboundBall()
	{
		if (_ball == null)
		{
			return;
		}
		
		Vector vec = _ball.getVelocity();
		//Rebound Y
		if (UtilEnt.isGrounded(_ball))
		{
			if (vec.getY() < 0)
			{
				vec.setY(_ball.getVelocity().getY() * _velocity);
				_velocity = Math.max(0, _velocity - .5);
			}
		}
		
		if (!UtilEnt.isGrounded(_ball))
		{
			vec.setY(vec.getY() - 0.08);
		}
		
		//Rebound X
		if ((vec.getX() > 0 && _ball.getLocation().getX() >= _maxX) || (vec.getX() < 0 && _ball.getLocation().getX() <= _minX))
		{
			vec.setX(vec.getX() * -1);

			vec = vec.multiply(0.9);
		}

		//Rebound Z
		if ((vec.getZ() > 0 && _ball.getLocation().getZ() >= _maxZ) || (vec.getZ() < 0 && _ball.getLocation().getZ() <= _minZ))
		{
			vec.setZ(vec.getZ() * -1);

			vec = vec.multiply(0.9);
		}
		
		_ball.setVelocity(vec);
	}
	
	private void spawnNeutralBall()
	{
		Location loc = DataLoc.CENTER_COURT.getLocation(_world);
		_ball = spawnBall(loc);
	}
	
	private void throwBall(Player origin, boolean right)
	{
		Entity e = spawnBall(origin.getEyeLocation());
		
		double power = 1.7;
		if (right)
		{
			power = 3.1;
		}
		e.setVelocity(origin.getEyeLocation().getDirection().normalize().multiply(power));
		
		Recharge.Instance.use(origin, "Catch Ball", 3000, false, false, false);
		_ball = e;
		
		if (_dribbling != null)
		{
			UtilInv.removeAll(_dribbling, Material.SLIME_BALL, (byte)0);
			_dribbling = null;
		}
		
		_throwData = new ThrowData(origin, getTeam(origin));
	}
	
	private void catchBall(Player player)
	{
		_lastDribbleMove = System.currentTimeMillis();
		_dribbling = player;
		for (int i = 0; i < 8; i++)
		{
			_dribbling.getInventory().setItem(i, new ItemBuilder(Material.SLIME_BALL).setTitle(C.cGold + "Basketball").build());
		}
		_ball.remove();
		_ball = null;
		_throwData = null;
	}
	
	private void setupMap()
	{
		Block redHoop = DataLoc.RED_HOOP.getLocation(_world).getBlock();
		redHoop.setType(Material.WEB);
		Block blueHoop = DataLoc.BLUE_HOOP.getLocation(_world).getBlock();
		blueHoop.setType(Material.WEB);
		
		_hoops.put(BasketballTeam.RED, redHoop);
		_hoops.put(BasketballTeam.BLUE, blueHoop);
		
		_maxX = DataLoc.CORNER_MAX.getLocation(_world).getX();
		_minX = DataLoc.CORNER_MIN.getLocation(_world).getX();
		_maxZ = DataLoc.CORNER_MAX.getLocation(_world).getZ();
		_minZ = DataLoc.CORNER_MIN.getLocation(_world).getZ();
	}
	
	private void beginGame()
	{
		spawnNeutralBall();
		_ball.setVelocity(_ball.getVelocity().add(new Vector(0, 1.5, 0)));
	}
	
	private void stealBall(Player to, Player from)
	{
		Recharge.Instance.use(to, "ThrowBall", 1500, false, false, false);
		_lastDribbleMove = System.currentTimeMillis();
		_dribbling = to;
		for (int i = 0; i < 8; i++)
		{
			to.getInventory().setItem(i, new ItemBuilder(Material.SLIME_BALL).setTitle(C.cGold + "Basketball").build());
		}
		UtilInv.removeAll(from, Material.SLIME_BALL, (byte)0);
		for (Player player : _players)
		{
			UtilPlayer.message(player, F.main("Game", getTeam(to).getChatColor() + to.getName() + C.cGray + " has stolen the ball from " + getTeam(from).getChatColor() + from.getName() + C.cGray + "!"));
		}
	}
	
	private void score(BasketballTeam team, Location hoop)
	{
		int points = getWorth(_throwData.getThrowOrigin(), hoop);
		if (team == BasketballTeam.RED)
		{
			_redScore += points;
		}
		else if (team == BasketballTeam.BLUE)
		{
			_blueScore += points;
		}
		UtilTextMiddle.display(team.getName() + " has scored!", BasketballTeam.RED.getChatColor() + "" + _redScore + C.cWhite + " - " + BasketballTeam.BLUE.getChatColor() + _blueScore, 0, 20 * 2, 0, _players.toArray(new Player[] {}));
		_ball.remove();
		_ball = null;
		_throwData = null;
		
		BasketballTeam other = getOtherTeam(team);
		List<Player> teamD = Lists.newArrayList();
		List<Player> teamO = Lists.newArrayList();
		
		for (Player player : _players)
		{
			if (getTeam(player) == team)
			{
				teamO.add(player);
			}
			else
			{
				teamD.add(player);
			}
		}
		
		for (int i = 0; i < teamO.size(); i++)
		{
			Player player = teamO.get(i);
			int locId = Math.min(i, team.getSpawns(_world).length);
			player.teleport(team.getSpawns(_world)[locId]);
		}
		
		if (!teamD.isEmpty())
		{
			//Select player to get ball
			Player carrier = teamD.get(new Random().nextInt(teamD.size()));

			for (int i = 0; i < teamD.size(); i++)
			{
				Player player = teamD.get(i);
				if (carrier.getEntityId() != player.getEntityId())
				{
					Location loc = null;
					int locId = i;
					if (other == BasketballTeam.BLUE)
					{
						locId = Math.min(DataLoc.BLUE_SCORE_SPAWN.getLocations(_world).length - 1, locId);
						loc = DataLoc.BLUE_SCORE_SPAWN.getLocations(_world)[locId];
					}
					else if (other == BasketballTeam.RED)
					{
						locId = Math.min(DataLoc.RED_SCORE_SPAWN.getLocations(_world).length - 1, locId);
						loc = DataLoc.RED_SCORE_SPAWN.getLocations(_world)[locId];
					}

					if (loc != null)
					{
						player.teleport(loc);
					}
				}
			}

			Location teleport = DataLoc.BLUE_HOOP.getLocation(_world);
			if (other == BasketballTeam.RED)
				teleport = DataLoc.RED_UNDER_HOOP.getLocation(_world);

			teleport.setPitch(UtilAlg.GetPitch(UtilAlg.getTrajectory(teleport, DataLoc.CENTER_COURT.getLocation(_world))));
			teleport.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(teleport, DataLoc.CENTER_COURT.getLocation(_world))));
			carrier.teleport(teleport);

			_lastDribbleMove = System.currentTimeMillis();
			_dribbling = carrier;
			for (int i = 0; i < 8; i++)
			{
				_dribbling.getInventory().setItem(i, new ItemBuilder(Material.SLIME_BALL).setTitle(C.cGold + "Basketball").build());
			}
		}
		else
		{
			spawnNeutralBall();
		}
	}
	
	private void checkScoring()
	{
		if (_ball == null)
		{
			return;
		}
		
		for (BasketballTeam team : _hoops.keySet())
		{
			Location check = _hoops.get(team).getLocation().add(.5, 0, .5);
			
			if (UtilMath.offset(check, _ball.getLocation()) <= .9)
			{
				score(getOtherTeam(team), check);
				return;
			}
		}
	}
	
	private void dribble()
	{
		if (_dribbling == null)
		{
			_lastDribbleAnim = System.currentTimeMillis();
			return;
		}
		
		UtilTextBottom.display(C.cRed + "You have the ball!", _dribbling);
		UtilParticle.PlayParticleToAll(ParticleType.HAPPY_VILLAGER, _dribbling.getEyeLocation(), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0, 1, ViewDist.MAX);
		
		if (System.currentTimeMillis() - _lastDribbleAnim > 333)
		{
			_lastDribbleAnim = System.currentTimeMillis();
			Item item = _dribbling.getWorld().dropItem(_dribbling.getLocation(), new ItemStack(Material.SLIME_BALL));
			item.setPickupDelay(Integer.MAX_VALUE);
			Bukkit.getScheduler().runTaskLater(_hub.getPlugin(), () -> {
				item.remove();
			}, 7);
		}
	}
	
	/**
	 * Checks how long this game has been running
	 * @return The amount of time this game has been running
	 */
	public long getGameAge()
	{
		return System.currentTimeMillis() - _start;
	}
	
	/**
	 * Checks whether a location is out of bounds of the main arena
	 * @param loc The location to check
	 * @param ball Whether to base calculations on the location being the location of the ball
	 * @return Whether the location is out of bounds
	 */
	public boolean isOutOfBounds(Location loc, boolean ball)
	{
		if (ball)
		{
			if ((loc.getX() - 1) > _maxX || (loc.getX() + 1) < _minX)
			{
				return true;
			}
			if ((loc.getZ() - 1) > _maxZ || (loc.getZ() + 1) < _minZ)
			{
				return true;
			}
			
			return false;
		}
		
		if (loc.getX() >= _maxX || loc.getX() <= _minX)
		{
			return true;
		}
		if (loc.getZ() >= _maxZ || loc.getZ() <= _minZ)
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * Fetches the entity representing the ball in this game
	 * @return The entity representing the ball, or null if a player is dribbling
	 */
	public Entity getBall()
	{
		return _ball;
	}
	
	/**
	 * Quickly searches for the opposite team of the one given
	 * @param team The team to check
	 * @return The opposite team
	 */
	public BasketballTeam getOtherTeam(BasketballTeam team)
	{
		if (team == BasketballTeam.RED)
		{
			return BasketballTeam.BLUE;
		}
		if (team == BasketballTeam.BLUE)
		{
			return BasketballTeam.RED;
		}
		
		return null;
	}
	
	/**
	 * Fetches the list of players in this game
	 * @return The list containing all players in this game
	 */
	public List<Player> getPlayers()
	{
		return _players;
	}
	
	/**
	 * Forces the game to end, unregistering all listeners and cleaning up created entities
	 */
	public void end()
	{
		HandlerList.unregisterAll(this);
		if (_ball != null)
		{
			_ball.remove();
		}
		if (_dribbling != null)
		{
			UtilInv.removeAll(_dribbling, Material.SLIME_BALL, (byte)0);
		}
	}
	
	@EventHandler
	public void onSwipe(EntityDamageByEntityEvent event)
	{
		if (_frozen)
			return;
		
		if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player))
		{
			return;
		}
		
		if (!_players.contains((Player)event.getEntity()) || !_players.contains((Player)event.getDamager()))
		{
			return;
		}
		
		Player player = (Player)event.getDamager();
		Player target = (Player)event.getEntity();
		
		if (_dribbling != null && getTeam(player) != getTeam(target) && target.getEntityId() == _dribbling.getEntityId())
		{
			if (Recharge.Instance.usable(player, "Steal Ball", true))
			{
				Recharge.Instance.use(player, "Steal Ball", 1000 * 5, true, false, false);
				if (new Random().nextDouble() <= .15)
				{
					stealBall(player, target);
				}
				else
				{
					player.sendMessage(F.main("Game", "Your steal attempt failed!"));
				}
			}
		}
	}
	
	@EventHandler
	public void onThrow(PlayerInteractEvent event)
	{
		if (_frozen)
			return;
		
		if (_dribbling != null && event.getPlayer().getEntityId() == _dribbling.getEntityId() && Recharge.Instance.usable(_dribbling, "ThrowBall", false))			
		{
			throwBall(event.getPlayer(), event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK);
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() == UpdateType.FASTEST)
		{
			for (Player player : _players)
			{
				CraftPlayer cp = (CraftPlayer)player;
				for (int x = (int)Math.floor(cp.getHandle().getBoundingBox().a + 0.001); x <= (int)Math.floor(cp.getHandle().getBoundingBox().d - 0.001); x++)
				{
					for (int y = (int)Math.floor(cp.getHandle().getBoundingBox().b + 0.001); y <= (int)Math.floor(cp.getHandle().getBoundingBox().e - 0.001); y++)
					{
						for (int z = (int)Math.floor(cp.getHandle().getBoundingBox().c + 0.001); z <= (int)Math.floor(cp.getHandle().getBoundingBox().f - 0.001); z++)
						{
							CraftWorld world = (CraftWorld)_world;
							if (net.minecraft.server.v1_8_R3.Block.getId(world.getHandle().getType(new BlockPosition(x, y, z)).getBlock()) == Material.WEB.getId())
							{
								player.teleport(new Location(player.getWorld(), x, y - 2, z));
							}
						}
					}
				}
			}
			if (_dribbling != null && (!_dribbling.isOnline() || !_players.contains(_dribbling)))
			{
				if (_dribbling != null)
				{
					UtilInv.removeAll(_dribbling, Material.SLIME_BALL, (byte)0);
				}
				_dribbling = null;
				spawnNeutralBall();
			}
			
			if (_ball != null)
			{
				if (isOutOfBounds(_ball.getLocation(), true))
				{
					if (_ball.hasMetadata("Respawn"))
					{
						if (UtilTime.elapsed(_ball.getMetadata("Respawn").get(0).asLong(), 2000))
						{
							_ball.removeMetadata("Respawn", _hub.getPlugin());
							_ball.setVelocity(UtilAlg.getTrajectory(_ball.getLocation(), DataLoc.CENTER_COURT.getLocation(_world)));
						}
					}
					else
					{
						_ball.setMetadata("Respawn", new FixedMetadataValue(_hub.getPlugin(), System.currentTimeMillis()));
					}
				}
				else
				{
					if (_ball.hasMetadata("Respawn"))
					{
						_ball.removeMetadata("Respawn", _hub.getPlugin());
					}
				}
			}
			
			reboundBall();
			
			if (!checkCatching())
			{
				checkScoring();
			}
			
			dribble();
			
			if (_dribbling != null)
			{
				if (UtilTime.elapsed(_lastDribbleMove, 15000))
				{
					throwBall(_dribbling, false);
				}
			}
		}
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent event)
	{	
		if (!_players.contains(event.getPlayer()))
		{
			return;
		}
		
		if (UtilMath.offset2d(event.getFrom(), event.getTo()) <= 0)
		{
			return;
		}
		
		if (_frozen)
		{
			event.setTo(event.getFrom());
			return;
		}
		
		if (_dribbling != null && event.getPlayer().getEntityId() == _dribbling.getEntityId())
		{
			_lastDribbleMove = System.currentTimeMillis();
		}
	}
}