package nautilus.game.arcade.game.games.minecraftleague.data;

import java.util.ArrayList;
import java.util.List;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.hologram.Hologram;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.minecraftleague.DataLoc;
import nautilus.game.arcade.game.games.minecraftleague.MinecraftLeague;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.util.Vector;

public abstract class TeamTowerBase
{
	private MinecraftLeague _host;
	private TowerManager _manager;
	private Location _spawn;
	private GameTeam _team;
	private Double _health, _maxHealth;
	private EnderCrystal _entity;
	private Hologram _name, _healthTag;
	private String _type;
	public boolean Alive;
	public boolean Vulnerable;
	
	public TeamTowerBase(MinecraftLeague host, TowerManager manager, GameTeam team, Location spawn)
	{
		_host = host;
		_manager = manager;
		_spawn = spawn.clone().add(0, 3.2, 0);
		_team = team;
		_maxHealth = 11111D;
		_health = 11111D;
		_type = "Tower";
		if (this instanceof TeamCrystal)
			_type = "Core";
		_name = new Hologram(_host.getArcadeManager().getHologramManager(), _spawn.clone().add(1, 3, 0), team.GetColor() + team.getDisplayName() + "'s " + _type);
		_healthTag = new Hologram(_host.getArcadeManager().getHologramManager(), _spawn.clone().add(1, 2, 0), formatHealth(_health));
		
		spawn();
	}
	
	private void spawn()
	{
		_name.start();
		_healthTag.start();
		_entity = (EnderCrystal) _host.getArcadeManager().GetCreature().SpawnEntity(_spawn, EntityType.ENDER_CRYSTAL);
		_health = _maxHealth;
		Alive = true;
	}
	
	private void kill(Player player)
	{		
		String message = "";
		if (player != null)
			message = _host.GetTeam(player).GetColor() + player.getName() + _team.GetColor() + " has destroyed " + _team.getDisplayName() + "'s " + _type + "!";
		else
			message = _team.GetColor() + _team.getDisplayName() + "'s " + _type + " has been destroyed!";
		
		//Bukkit.broadcastMessage(message);
		UtilTextMiddle.display("", message);
		
		if (!_type.equalsIgnoreCase("Tower"))
		{
			for (Player inform : _team.GetPlayers(true))
				UtilTextMiddle.display(C.cGold + "Team Crystal Destroyed", C.cGold + "You will no longer respawn and will be poisoned in 5 minutes!", inform);
		}
		
		Alive = false;
		_entity.remove();
		_healthTag.stop();
		_name.stop();
		Bukkit.getScheduler().runTaskLater(_host.Manager.getPlugin(), new Runnable()
		{
			public void run()
			{
				playDeathAnimation(_spawn);
			}
		}, 20 * 5);
		detonate();
		_manager.handleTowerDeath(this);
	}
	
	private void playDeathAnimation(Location loc)
	{
		_spawn.getWorld().playSound(loc, Sound.EXPLODE, 10, 0);
		//GFX subject to change
		Location loc1 = loc.clone().add(-2, 3, -2);
		Location loc2 = loc.clone().add(2, 0, 2);
		Location loc3 = loc.clone().add(2, 3, 2);
		Location loc4 = loc.clone().add(-2, 0, -2);
		Location particle1 = loc2.clone();
		Location particle2 = loc4.clone();
		while (UtilMath.offset(loc1, loc) >= UtilMath.offset(particle1, loc))
		{
			Vector v = UtilAlg.getTrajectory(particle1, loc1);
			//UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, particle, v, 0, 1, ViewDist.NORMAL, UtilServer.getPlayers());
			UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, particle1, v, 0, 5, ViewDist.MAX, UtilServer.getPlayers());
			particle1.add(v);
		}
		while (UtilMath.offset(loc3, loc) >= UtilMath.offset(particle2, loc))
		{
			Vector v = UtilAlg.getTrajectory(particle2, loc3);
			UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, particle2, v, 0, 5, ViewDist.MAX, UtilServer.getPlayers());
			particle2.add(v);
		}
	}
	
	private void detonate()
	{
		UtilParticle.PlayParticleToAll(ParticleType.LARGE_EXPLODE, _spawn, null, 0, 2, ViewDist.NORMAL);
		_spawn.getWorld().playSound(_spawn, Sound.EXPLODE, 10, 0);
		List<Block> blocks = new ArrayList<Block>();
		Location bottom = _spawn.clone().subtract(0, -3.2, 0);
		for (int i = 0; i < 23; i++)
		{
			blocks.addAll(UtilBlock.getInSquare(bottom.clone().add(0, i, 0).getBlock(), 4));
		}
		for (int i = 0; i < 3; i++)
		{
			getBeacon().clone().add(0, i, 0).getBlock().setType(Material.BEDROCK);
		}
		_host.Manager.GetExplosion().BlockExplosion(blocks, _spawn, false, true);
		for (Entity e : _host.WorldData.World.getEntities())
		{
			if (e instanceof Wither)
			{
				LivingEntity le = (LivingEntity) e;
				le.setHealth(le.getHealth() / 2);
			}
		}
		for (LivingEntity le : UtilEnt.getInRadius(_spawn, 5).keySet())
		{
			if (UtilMath.offset(le.getLocation(), _spawn) <= 5)
			{
				if (UtilPlayer.isSpectator(le))
					continue;
				if (le instanceof Player)
				{
					_host.storeGear((Player)le);
				}
				le.damage(6);
			}
		}
	}
	
	public Location getBeacon()
	{
		Location ret = null;
		for (Location loc : _host.WorldData.GetDataLocs(DataLoc.BEACON.getKey()))
		{
			if (ret == null || UtilMath.offset(ret, _spawn) > UtilMath.offset(loc, _spawn))
				ret = loc;
		}
		
		return ret;
	}
	
	public Entity getEntity()
	{
		return _entity;
	}
	
	public boolean isEntity(Entity e)
	{
		return e.getEntityId() == _entity.getEntityId();
	}
	
	public boolean canDamage(Player player)
	{
		if (UtilPlayer.isSpectator(player))
			return false;
		
		if (_host.GetTeam(player) == _team)
			return false;
		
		if (!_host.IsPlaying(player))
			return false;
		
		if (!Recharge.Instance.usable(player, "Damage TeamTower"))
			return false;
		
		return true;
	}
	
	public Double getHealth()
	{
		if (!Alive)
			return 0D;
		
		return _health;
	}
	
	public String formatHealth(Double healthNumber)
	{
		String tag = healthNumber.toString();
		
		if (healthNumber > (.9 * _maxHealth))
			tag = C.cGreen + tag;
		else if (healthNumber < (.45 * _maxHealth))
			tag = C.cRed + tag;
		else
			tag = C.cYellow + tag;
		
		return tag;
	}
	
	public ChatColor getHealthColor()
	{
		if (!Alive)
			return ChatColor.GRAY;
		
		ChatColor color = ChatColor.YELLOW;
		
		if (_health > (.9 * _maxHealth))
			color = ChatColor.GREEN;
		else if (_health < (.45 * _maxHealth))
			color = ChatColor.RED;
		
		return color;
	}
	
	public boolean damage(double damage, Player player)
	{
		if (!Vulnerable)
			return false;
		
		Double newHealth = Math.max(_health - damage, 0);
		
		if (newHealth == 0)
		{
			kill(player);
			return true;
		}
		
		_health = newHealth;
		if (player != null)
			Recharge.Instance.use(player, "Damage TeamTower", 400, false, false);
		return false;
	}
	
	public Location getLocation()
	{
		return _spawn;
	}
	
	public GameTeam getTeam()
	{
		return _team;
	}
	
	public void update()
	{
		_healthTag.setText(formatHealth(_health));
		
		if (Alive)
		{
			if (_entity.isDead() || !_entity.isValid())
			{
				_entity = (EnderCrystal) _host.getArcadeManager().GetCreature().SpawnEntity(_spawn, EntityType.ENDER_CRYSTAL);
			}
			
			if (_health > _maxHealth)
			{
				_health = _maxHealth;
			}
		}
	}
	
	public void setMaxHealth(Double health)
	{
		_maxHealth = Math.abs(health);
	}
	
	public void setVulnerable(boolean vulnerable)
	{
		if (vulnerable)
		{
			getBeacon().getBlock().setType(Material.BEACON);
			Vulnerable = vulnerable;
		}
		else
		{
			getBeacon().getBlock().setType(Material.BEDROCK);
			Vulnerable = vulnerable;
		}
	}
}