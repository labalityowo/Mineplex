package nautilus.game.arcade.game.games.evolution.evolve;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map.Entry;

import mineplex.core.common.util.C;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.hologram.Hologram;
import mineplex.core.hologram.Hologram.HologramTarget;
import mineplex.core.hologram.HologramManager;
import nautilus.game.arcade.game.games.evolution.EvoKit;
import nautilus.game.arcade.game.games.evolution.events.EvolutionBeginEvent;
import nautilus.game.arcade.game.games.evolution.events.EvolutionEndEvent;
import nautilus.game.arcade.game.games.evolution.events.EvolutionPostEvolveEvent;
import net.minecraft.server.v1_8_R3.EntityBlaze;
import net.minecraft.server.v1_8_R3.EntityChicken;
import net.minecraft.server.v1_8_R3.EntityCreeper;
import net.minecraft.server.v1_8_R3.EntityEnderman;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.EntityIronGolem;
import net.minecraft.server.v1_8_R3.EntitySkeleton;
import net.minecraft.server.v1_8_R3.EntitySlime;
import net.minecraft.server.v1_8_R3.EntitySnowman;
import net.minecraft.server.v1_8_R3.EntitySpider;
import net.minecraft.server.v1_8_R3.EntityWolf;
import net.minecraft.server.v1_8_R3.EntityZombie;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.World;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class EvolveData
{
	/**
	 * @author Mysticate
	 */
	
	private boolean _ended = false;
	
	private final long _timestamp = System.currentTimeMillis();
	private long _endTime = System.currentTimeMillis();
	
	private final long _preEvolve = 2200;
	private final long _postEvolve = 4800;
	
//	private EvolveManager _manager;
	private HologramManager _holoManager;
	
	private final PlatformToken _token;
	
	private boolean _active = true;
	
	private final Player _player;
//	private Zombie _zombie;
//	private DisguiseArmorStand _disguise;
	
	private final Location _eggLoc;
	private final SimpleEntry<Material, Byte> _eggLocData;
	
	private final Hologram _eggHolo;
	
	private final EvoKit _to;
	
	private Hologram _holo;
	
	private final EntityInsentient _eFrom;
	private EntityInsentient _eTo;
	
	@SuppressWarnings("deprecation")
	public EvolveData(EvolveManager manager, HologramManager holo, PlatformToken token, Player player, EvoKit from, EvoKit to)
	{
		
//		_manager = manager;
		_holoManager = holo;
		
		_token = token;
		
		_player = player;
		
		_eggLoc = player.getLocation();
		_eggLocData = new SimpleEntry<Material, Byte>(_eggLoc.getBlock().getType(), _eggLoc.getBlock().getData());
		
		_eggHolo = new Hologram(holo, _eggLoc.getBlock().getLocation().clone().add(0.5, 1.0, 0.5), C.cYellow + _player.getName()).start();
		
		_to = to;
				
		_eFrom = spawn(from.getEntity(), token.Platform, false);
				
		Bukkit.getServer().getPluginManager().callEvent(new EvolutionBeginEvent(_player));
		
		setupViewingToken(_eFrom);
//		spawnZombie();
//		setupZombie();
		setupPlayer();
		setupEgg();
		setupHologram(holo, _eFrom, from);
	}

	private void setupEgg()
	{
		MapUtil.QuickChangeBlockAt(_eggLoc, Material.DRAGON_EGG);
	}
	
	private void restoreEgg()
	{
		MapUtil.QuickChangeBlockAt(_eggLoc, _eggLocData.getKey(), _eggLocData.getValue());
	}
	
	public Location getEggLocation()
	{
		return _eggLoc;
	}
	
	private void setupViewingToken(EntityInsentient ent)
	{
		Location playerEye = _token.Viewing.clone().add(.5, .62, .5);
		Location entityHalf = _token.Platform.clone().add(.5, -1, .5).add(0, (ent.getBoundingBox().e - ent.getBoundingBox().b) * .75, 0);
		
		Vector viewing = UtilAlg.getTrajectory(playerEye, entityHalf);
		_token.Viewing.setPitch(UtilAlg.GetPitch(viewing));
	}
	
//	private void spawnZombie()
//	{
//		_manager.Host.CreatureAllowOverride = true;
//		_zombie = (Zombie) _player.getWorld().spawnEntity(_token.Viewing, EntityType.ZOMBIE);
//		_manager.Host.CreatureAllowOverride = true;
//		UtilEnt.Vegetate(_zombie, true);
//		UtilEnt.ghost(_zombie, true, true);
//		
//		_disguise = new DisguiseArmorStand(_zombie);
//		
//		_disguise.setInvisible(true);
//		_disguise.Global = false;
//		
//		_manager.Host.Manager.GetDisguise().disguise(_disguise, _player);
//	}
//	
//	private void setupZombie()
//	{
//		_zombie.teleport(_token.Viewing);
//		
//		_disguise.setHeadPosition(_token.Viewing.getDirection());
//		
////		_disguise.UpdateDataWatcher();
//		_manager.Host.Manager.GetDisguise().updateDisguise(_disguise);
//		
//	}
	
	private void setupPlayer()
	{
		if (_token.Viewing.getX() != _player.getLocation().getX())
		{
			_player.teleport(_token.Viewing);
			return;
		}
		
		if (_token.Viewing.getY() != _player.getLocation().getY())
		{
			_player.teleport(_token.Viewing);
			return;
		}
		
		if (_token.Viewing.getZ() != _player.getLocation().getZ())
		{
			_player.teleport(_token.Viewing);
			return;
		}
		
		if (_token.Viewing.getPitch() != _player.getLocation().getPitch())
		{
			_player.teleport(_token.Viewing);
			return;
		}
		
		if (_token.Viewing.getYaw() != _player.getLocation().getYaw())
		{
			_player.teleport(_token.Viewing);
			return;
		}
		
//		_player.teleport(_token.Store);
//		_player.teleport(_token.Viewing);
		
//		UtilPlayer.sendPacket(_player, new PacketPlayOutCamera(_zombie.getEntityId()));
	}

	private void setupHologram(HologramManager manager, EntityInsentient entity, EvoKit kit)
	{
		if (_holo == null)
			_holo = new Hologram(manager, _token.Platform);
		
		double add = entity.getBoundingBox().e - entity.getBoundingBox().b + .3;
		
		_holo.setLocation(_token.Platform.clone().add(0, add, 0));
		
		_holo.setText(kit.buildHologram());
		
		_holo.setHologramTarget(HologramTarget.WHITELIST);
		_holo.addPlayer(_player);
		
		_holo.start();
	}
	
	//Boolean completed
	public boolean tick()
	{
		//Failsafe
		if (_ended)
		{
			return true;
		}

//		setupZombie();
		teleport();
		
		//Hasn't ended yet
		if (_active)
		{
			//If 3 seconds past
			if (UtilTime.elapsed(_timestamp, _preEvolve))
			{
				_active = false;
				_endTime = System.currentTimeMillis();
								
				EvolutionEndEvent event = new EvolutionEndEvent(_player);
				Bukkit.getServer().getPluginManager().callEvent(event);

				if (event.isCancelled())
				{
					return true;
				}
				
				_eTo = spawn(_to.getEntity(), _token.Platform, false);
				despawn(_eFrom);
				
				setupViewingToken(_eTo);
//				setupZombie();
				setupPlayer();
				
				setupHologram(_holoManager, _eTo, _to);
				
				UtilFirework.packetPlayFirework(_player, _token.Platform.clone().add(0.0, 1.5, 0.0), Type.BALL, Color.GREEN, false, false);
			}
			else
			{
				//Play particles
				if (UtilTime.elapsed(_timestamp, 500))
				{
					UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, _token.Platform.clone().add(0.0, 1.0, 0.0), 0.5F, 1.0F, 0.5F, 0, 5, ViewDist.SHORT, _player);	
					UtilParticle.PlayParticle(ParticleType.HAPPY_VILLAGER, _eggLoc.getBlock().getLocation().clone().add(0.5, 0.5, 0.5), 0.5F, 0.5F, 0.5F, 0, 3, ViewDist.NORMAL, UtilServer.getPlayers());	
				}
			}
			return false;
		}
		else
		{
			if (!UtilTime.elapsed(_endTime, _postEvolve))
				return false;
			
			end();
			
			UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, _eggLoc.clone().add(0.0, 1.0, 0.0), 1.4F, 3.0F, 1.4F, 0, 12, ViewDist.NORMAL, UtilServer.getPlayers());	
			knockback();
			
			Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugins()[0], new Runnable()
			{
				@Override
				public void run()
				{
					Bukkit.getServer().getPluginManager().callEvent(new EvolutionPostEvolveEvent(_player));
				}
			}, 2);
			return true;
		}
	}
	
	public void end()
	{
		_active = false;
		
		if (_eFrom != null)
			despawn(_eFrom);
		
		if (_eTo != null)
			despawn(_eTo);
		
		_eggHolo.stop();
		restoreEgg();
		
		if (_holo != null)
			_holo.stop();
		
//		UtilPlayer.sendPacket(_player, new PacketPlayOutCamera(_player));
//		_zombie.remove();
		
		try 
		{
			_player.teleport(_eggLoc.clone());
		}
		catch (NullPointerException ex)
		{}
		
		_ended = true;
	}
	
	public void teleport()
	{
		setupPlayer();
	}

	private void knockback()
	{
		HashMap<Player, Double> radius = UtilPlayer.getInRadius(_eggLoc, 8.0);
		
		for (Entry<Player, Double> entry : radius.entrySet())
		{
			if (entry.getKey() == _player)
				continue;

			UtilAction.velocity(entry.getKey(), UtilAlg.getTrajectory2d(_eggLoc, entry.getKey().getLocation()), 1.6 - (entry.getValue() / 10), true, 0.8, 0, 10, true);
		}
	}
	
	private EntityInsentient spawn(EntityType type, Location loc, boolean invisible)
	{
		try 
		{
			EntityInsentient entity = getEntity(type).getConstructor(World.class).newInstance(((CraftWorld) loc.getWorld()).getHandle());
			entity.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
			PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(entity);
			entity.setCustomNameVisible(false);
			
			if (entity instanceof EntitySlime)
			{
				((EntitySlime) entity).setSize(2);
			}
			
			if (invisible)
			{
				entity.setInvisible(true);
			}
			
			entity.setGhost(true);
			
			UtilPlayer.sendPacket(_player, packet);
			return entity;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
	
	private void despawn(EntityInsentient entity)
	{
		if (entity == null)
			return;

		PacketPlayOutEntityDestroy packet = new	PacketPlayOutEntityDestroy(new int[]
				{
				entity.getId()
				});
		
		UtilPlayer.sendPacket(_player, packet);
	}
	
	private Class<? extends EntityInsentient> getEntity(EntityType type)
	{
		if (type == EntityType.BLAZE)
			return EntityBlaze.class;
		
		if (type == EntityType.CHICKEN)
			return EntityChicken.class;
		
		if (type == EntityType.CREEPER)
			return EntityCreeper.class;
		
		if (type == EntityType.ENDERMAN)
			return EntityEnderman.class;
		
		if (type == EntityType.IRON_GOLEM)
			return EntityIronGolem.class;
		
		if (type == EntityType.SKELETON)
			return EntitySkeleton.class;
		
		if (type == EntityType.SLIME)
			return EntitySlime.class;
		
		if (type == EntityType.SNOWMAN)
			return EntitySnowman.class;
		
		if (type == EntityType.SPIDER)
			return EntitySpider.class;
		
		if (type == EntityType.WOLF)
			return EntityWolf.class;
		
		return EntityZombie.class;
	}
}
