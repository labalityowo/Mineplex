package nautilus.game.arcade.game.games.basketball;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.TeamGame;
import nautilus.game.arcade.game.games.basketball.data.ScoringManager;
import nautilus.game.arcade.game.games.basketball.data.ThrowData;
import nautilus.game.arcade.game.games.basketball.kit.BasketballPlayerKit;
import nautilus.game.arcade.game.modules.TeamArmorModule;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;
import net.minecraft.server.v1_8_R3.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Main game class for Basketball
 */
public class Basketball extends TeamGame
{
	private Entity _ball;
	private Player _dribbling;
	private ThrowData _throwData;
	
	private static final double THREE_POINTER_DISTANCE = 27;
	
	private boolean _frozen = false;
	private long _lastDribbleAnim = 0;
	private long _lastDribbleMove = 0;
	
	private HashMap<GameTeam, Block> _hoops = new HashMap<>();
	private ScoringManager _score;
	
	private double _maxX = 0;
	private double _minX = 0;
	private double _maxZ = 0;
	private double _minZ = 0;
	
	private double _velocity = -7;
	
	public Basketball(ArcadeManager manager)
	{
		super(manager, GameType.Basketball, new Kit[] {new BasketballPlayerKit(manager)}, new String[]
				{
				"Dribble the ball to the other side of the court",
				"Shoot into the hoop to score 2 or 3 points",
				"Team with most points at the end wins!",
				"Left Click to pass or do a layup",
				"Right Click to make a distance shot",
				"Left Click an opposing player to try and steal the ball"
				}
		);

		this.HealthSet = 20;
		this.HungerSet = 20;
		this.Damage = false;
		this.CreatureAllow = false;
		this.AllowParticles = false;
		this.GameTimeout = -1;
		_score = new ScoringManager(this);

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
	
	private boolean isOutOfBounds(Location loc, boolean ball)
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
	
	private Entity spawnBall(Location loc)
	{
		this.CreatureAllowOverride = true;
		_velocity = -7;
		Entity e = Manager.GetCreature().SpawnEntity(loc, EntityType.SLIME);
		UtilEnt.vegetate(e, true);
		UtilEnt.ghost(e, true, false);
		((Slime)e).setSize(1);
		this.CreatureAllowOverride = false;
		
		return e;
	}
	
	private void announceDebug(Object debugMessage)
	{
		for (Player player : Bukkit.getOnlinePlayers())
		{
			if (player.getItemInHand() != null && player.getItemInHand().getType() == Material.BOOK)
			{
				player.sendMessage(debugMessage + "");
			}
		}
	}
	
	private void reboundBall()
	{
		if (_ball == null)
		{
			return;
		}
		
		if (_throwData != null)
		{
			for (Player player : GetTeam(_throwData.getThrower()).GetPlayers(true))
			{
				if (UtilMath.offset(_ball, player) <= 2.5)
				{
					if (Recharge.Instance.usable(player, "Catch Ball", false))
					{
						catchBall(player);
						return;
					}
				}
			}
		}
		
		Vector vec = _ball.getVelocity();
		announceDebug(vec.toString());
		//Rebound Y
		if (UtilEnt.isGrounded(_ball))
		{
			announceDebug("First");
			if (vec.getY() < 0)
			{
				announceDebug("Second");
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
		
		announceDebug(vec);
		_ball.setVelocity(vec);
	}
	
	private void spawnNeutralBall()
	{
		Location loc = WorldData.GetCustomLocs(DataLoc.CENTER_COURT.getKey()).get(0);
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
		
		_throwData = new ThrowData(origin);
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
	
	@Override
	public void ParseData()
	{
		Block redHoop = WorldData.GetCustomLocs(DataLoc.RED_HOOP.getKey()).get(0).getBlock();
		redHoop.setType(Material.WEB);
		Block blueHoop = WorldData.GetCustomLocs(DataLoc.BLUE_HOOP.getKey()).get(0).getBlock();
		blueHoop.setType(Material.WEB);
		
		_hoops.put(GetTeam(ChatColor.RED), redHoop);
		_hoops.put(GetTeam(ChatColor.AQUA), blueHoop);
		
		_maxX = WorldData.GetCustomLocs(DataLoc.CORNER_MAX.getKey()).get(0).getX();
		_minX = WorldData.GetCustomLocs(DataLoc.CORNER_MIN.getKey()).get(0).getX();
		_maxZ = WorldData.GetCustomLocs(DataLoc.CORNER_MAX.getKey()).get(0).getZ();
		_minZ = WorldData.GetCustomLocs(DataLoc.CORNER_MIN.getKey()).get(0).getZ();
	}
	
	@Override
	public void ScoreboardUpdate(UpdateEvent event)
	{
		
	}
	
	public void stealBall(Player to, Player from)
	{
		Recharge.Instance.use(to, "ThrowBall", 1500, false, false, false);
		_lastDribbleMove = System.currentTimeMillis();
		_dribbling = to;
		for (int i = 0; i < 8; i++)
		{
			to.getInventory().setItem(i, new ItemBuilder(Material.SLIME_BALL).setTitle(C.cGold + "Basketball").build());
		}
		UtilInv.removeAll(from, Material.SLIME_BALL, (byte)0);
		Bukkit.broadcastMessage(F.main("Game", GetTeam(to).GetColor() + to.getName() + C.cGray + " has stolen the ball from " + GetTeam(from).GetColor() + from.getName() + C.cGray + "!"));
	}
	
	private boolean checkCatching()
	{
		if (_ball != null)
		{
			for (Player player : GetPlayers(true))
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
	
	private void score(GameTeam team, Location hoop)
	{
		UtilTextMiddle.display("", team.GetColor() + team.getDisplayName() + " has scored!", 0, 20 * 2, 0);
		int points = getWorth(_throwData.getThrowOrigin(), hoop);

		if(_throwData.getThrower() != null)
		{
			String player = team.GetColor() + _throwData.getThrower().getName() + C.mBody + "";
			String broad = player + " has scored a " + F.elem(points + " pointer!");
			Location location = _throwData.getThrower().getLocation();
			double dist = UtilMath.offset2d(_throwData.getThrowOrigin(), hoop);

			if(dist <= 3)
			{
				broad = player + C.cGreen + " scored a layup!";
				if(hoop.getY() < location.getY())
				{
					broad = player + C.cGreenB + " dunked on em!";
				}
			}

			UtilServer.broadcast(broad);
		}

		_score.addPoint(team, points);
		_ball.remove();
		_ball = null;
		_throwData = null;
		
		GameTeam other = getOtherTeam(team);
		List<Player> teamP = other.GetPlayers(true);
		
		//Select player to get ball
		Player carrier = teamP.get(new Random().nextInt(teamP.size()));
		
		for (int i = 0; i < teamP.size(); i++)
		{
			Player player = teamP.get(i);
			if (carrier.getEntityId() != player.getEntityId())
			{
				String key = DataLoc.BLUE_SCORE_SPAWN.getKey();
				if (other.GetColor() == ChatColor.RED)
					key = DataLoc.RED_SCORE_SPAWN.getKey();
				
				player.teleport(WorldData.GetCustomLocs(key).get(i % WorldData.GetCustomLocs(key).size()));
				player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
			}
		}

		for (Player player : team.GetPlayers(true))
		{
			player.teleport(team.GetSpawn());
			player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
		}
		
		Location teleport = WorldData.GetCustomLocs(DataLoc.BLUE_UNDER_HOOP.getKey()).get(0);
		if (other.GetColor() == ChatColor.RED)
			teleport = WorldData.GetCustomLocs(DataLoc.RED_UNDER_HOOP.getKey()).get(0);
		
		teleport.setPitch(UtilAlg.GetPitch(UtilAlg.getTrajectory(teleport, WorldData.GetCustomLocs(DataLoc.CENTER_COURT.getKey()).get(0))));
		teleport.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(teleport, WorldData.GetCustomLocs(DataLoc.CENTER_COURT.getKey()).get(0))));
		carrier.teleport(teleport);
		
		_lastDribbleMove = System.currentTimeMillis();
		_dribbling = carrier;
		for (int i = 0; i < 8; i++)
		{
			_dribbling.getInventory().setItem(i, new ItemBuilder(Material.SLIME_BALL).setTitle(C.cGold + "Basketball").build());
		}
	}
	
	private void checkScoring()
	{
		if (_ball == null)
		{
			return;
		}
		
		for (GameTeam team : _hoops.keySet())
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
			Bukkit.getScheduler().runTaskLater(Manager.getPlugin(), () -> {
				item.remove();
			}, 7);
		}
	}
	
	private int getWorth(Location start, Location hoop)
	{
		if (UtilMath.offset2d(start, hoop) >= THREE_POINTER_DISTANCE)
		{
			return 3;
		}
		
		return 2;
	}
	
	private boolean endCheck()
	{
		//Check scores
		for (int scoreId = 0; scoreId <= 1; scoreId++)
		{
			int score = _score.getScores()[scoreId];
			
			if (score >= 50)
			{
				GameTeam team = null;
				if (scoreId == 0)
				{
					team = GetTeam(ChatColor.RED);
				}
				else if (scoreId == 1)
				{
					team = GetTeam(ChatColor.AQUA);
				}
				
				end(team);
				return true;
			}
		}
		
		//Check time
		if ((GetStateTime() + UtilTime.convert(5, TimeUnit.MINUTES, TimeUnit.MILLISECONDS)) - System.currentTimeMillis() <= 0)
		{
			int red = _score.getScores()[0];
			int blue = _score.getScores()[1];
			
			if (red > blue)
			{
				end(GetTeam(ChatColor.RED));
				return true;
			}
			else if (blue > red)
			{
				end(GetTeam(ChatColor.AQUA));
				return true;
			}
		}
		
		return false;
	}
	
	private void end(GameTeam winner)
	{
		AnnounceEnd(winner);
		
		for (GameTeam team : GetTeamList())
		{
			if (winner != null && team.equals(winner))
			{
				for (Player player : team.GetPlayers(false))
				{
					AddGems(player, 10, "Winning Team", false, false);
				}
			}

			for (Player player : team.GetPlayers(false))
			{
				if (player.isOnline())
				{
					AddGems(player, 10, "Participation", false, false);
				}
			}
		}
		_score.displayScores(Scoreboard, true, winner.GetColor() + winner.GetName());
		SetState(GameState.End);
	}
	
	/**
	 * Quickly searches for the opposite team of the one given
	 * @param team The team to check
	 * @return The opposite team
	 */
	public GameTeam getOtherTeam(GameTeam team)
	{
		if (team.GetColor() == ChatColor.RED)
		{
			return GetTeam(ChatColor.AQUA);
		}
		if (team.GetColor() == ChatColor.AQUA)
		{
			return GetTeam(ChatColor.RED);
		}
		
		return null;
	}
	
	@EventHandler
	public void onSwipe(EntityDamageByEntityEvent event)
	{
		if (!IsLive())
			return;
		if (_frozen)
			return;
		
		if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player))
		{
			return;
		}
		
		if (!IsPlaying((Player)event.getEntity()) || !IsPlaying((Player)event.getDamager()))
		{
			return;
		}
		
		Player player = (Player)event.getDamager();
		Player target = (Player)event.getEntity();
		
		if (_dribbling != null && GetTeam(player).GetColor() != GetTeam(target).GetColor() && target.getEntityId() == _dribbling.getEntityId())
		{
			if (Recharge.Instance.usable(player, "Steal Ball", true))
			{
				Recharge.Instance.use(player, "Steal Ball", 1000 * 5, true, false, false);
				if (new Random().nextDouble() <= .20)
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
		if (!IsLive())
			return;
		
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
		if (!IsLive())
			return;
		
		if (event.getType() == UpdateType.FASTEST)
		{
			if (endCheck())
			{
				return;
			}
			for (Player player : GetPlayers(true))
			{
				CraftPlayer cp = (CraftPlayer)player;
				for (int x = (int)Math.floor(cp.getHandle().getBoundingBox().a + 0.001); x <= (int)Math.floor(cp.getHandle().getBoundingBox().d - 0.001); x++)
				{
					for (int y = (int)Math.floor(cp.getHandle().getBoundingBox().b + 0.001); y <= (int)Math.floor(cp.getHandle().getBoundingBox().e - 0.001); y++)
					{
						for (int z = (int)Math.floor(cp.getHandle().getBoundingBox().c + 0.001); z <= (int)Math.floor(cp.getHandle().getBoundingBox().f - 0.001); z++)
						{
							CraftWorld world = (CraftWorld)WorldData.World;
							if (net.minecraft.server.v1_8_R3.Block.getId(world.getHandle().getType(new BlockPosition(x, y, z)).getBlock()) == Material.WEB.getId())
							{
								player.teleport(new Location(player.getWorld(), x, y - 2, z));
							}
						}
					}
				}
			}
			if (_dribbling != null && !_dribbling.isOnline())
			{
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
							_ball.removeMetadata("Respawn", Manager.getPlugin());
							_ball.setVelocity(UtilAlg.getTrajectory(_ball.getLocation(), WorldData.GetCustomLocs(DataLoc.CENTER_COURT.getKey()).get(0)));
						}
					}
					else
					{
						_ball.setMetadata("Respawn", new FixedMetadataValue(Manager.getPlugin(), System.currentTimeMillis()));
					}
				}
				else
				{
					if (_ball.hasMetadata("Respawn"))
					{
						_ball.removeMetadata("Respawn", Manager.getPlugin());
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
		
		if (event.getType() == UpdateType.FASTER)
		{
			_score.displayScores(Scoreboard, false, "");
		}
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent event)
	{
		if (!IsLive())
			return;
		
		if (!IsPlaying(event.getPlayer()))
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
		
		if (isOutOfBounds(event.getTo(), false))
		{
			event.getPlayer().teleport(event.getFrom());
			Vector bounce = UtilAlg.getTrajectory(event.getTo(), WorldData.GetCustomLocs(DataLoc.CENTER_COURT.getKey()).get(0));
			event.getPlayer().setVelocity(bounce.normalize());
			event.getPlayer().sendMessage(F.main("Game", "You aren't allowed to go out of bounds!"));
		}
		if (_dribbling != null && event.getPlayer().getEntityId() == _dribbling.getEntityId())
		{
			_lastDribbleMove = System.currentTimeMillis();
		}
	}
	
	@EventHandler
	public void onStart(GameStateChangeEvent event)
	{
		if (event.GetGame().equals(this) && event.GetState() == GameState.Live)
		{
			spawnNeutralBall();
			_ball.setVelocity(_ball.getVelocity().add(new Vector(0, 1.5, 0)));
			/*_frozen = true;			
			Bukkit.getScheduler().runTaskLater(Manager.getPlugin(), () ->
			{
				_frozen = false;
				spawnNeutralBall();
			}, 20 * 3);*/
		}
	}
	
	@EventHandler
	public void onDebug(PlayerCommandPreprocessEvent event)
	{
		if (event.getMessage().toLowerCase().contains("/setvelocity ") && event.getPlayer().isOp())
		{
			event.setCancelled(true);
			_velocity = Double.parseDouble(event.getMessage().replace("/setvelocity ", "")) * -1;
		}
	}
}
