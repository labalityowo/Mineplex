package nautilus.game.arcade.game.games.build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import com.java.sk89q.jnbt.CompoundTag;
import com.java.sk89q.jnbt.DoubleTag;
import com.java.sk89q.jnbt.NBTUtils;
import com.java.sk89q.jnbt.StringTag;
import com.java.sk89q.jnbt.Tag;

import mineplex.core.common.block.schematic.Schematic;
import mineplex.core.common.block.schematic.UtilSchematic;
import mineplex.core.common.util.F;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.game.GameTeam;
import net.minecraft.server.v1_8_R3.EntityLightning;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityWeather;

public class BuildData 
{
	public Player Player;
	
	public GameTeam Team;
	
	public boolean Judged = false;
	
	public Location Spawn;
	
	public Location CornerBottomLeft;
	public Location CornerTopRight;

	public HashSet<Block> Blocks = new HashSet<Block>();
	
	public HashSet<Entity> Entities = new HashSet<Entity>();
	
	public HashSet<Entity> Items = new HashSet<Entity>();
	
	public NautHashMap<Location, ParticleType> Particles = new NautHashMap<Location, ParticleType>();
	
	public int Time = 6000;
	
	// This is used to show the player to use their inventory to grab items
	public boolean ClickedInventory = false;
	
	public WeatherType Weather = WeatherType.SUNNY;
	
	private double _totalPoints = 0;
	
	protected BuildData(Player player, Location spawn)
	{
		Player = player;
		Team = null;
		Spawn = spawn;
	}
	
	public BuildData(Player player, Location spawn, ArrayList<Location> buildBorders)
	{
		this(player, spawn);
		
		Location CornerA = UtilAlg.findClosest(spawn, buildBorders);
		buildBorders.remove(CornerA);
		Location CornerB = UtilAlg.findClosest(spawn, buildBorders);
		buildBorders.remove(CornerB);

		setCorners(CornerA, CornerB);
	}
	
	public BuildData(GameTeam team, Location spawn, ArrayList<Location> buildBorders)
	{
		Player = null;
		Team = team;
		Spawn = spawn;
		
		Location CornerA = UtilAlg.findClosest(spawn, buildBorders);
		buildBorders.remove(CornerA);
		Location CornerB = UtilAlg.findClosest(spawn, buildBorders);
		buildBorders.remove(CornerB);

		setCorners(CornerA, CornerB);
	}

	/**
	 * Normalizes corners by setting
	 * CornerBottomLeft to be the "bottom-left"
	 * corner (lowest y-value, lowest x, and
	 * lowest z) and CornerTopRight to be the
	 * "top-right" corner (highest y-value,
	 * highest x, and highest z)
	 */
	private void setCorners(Location CornerA, Location CornerB)
	{
		Location bottomLeft;
		Location topRight;

		// Find the lowest corner by comparing y-values
		if (CornerA.getY() < CornerB.getY())
		{
			bottomLeft = CornerA.clone();
			topRight = CornerB.clone();
		}
		else
		{
			bottomLeft = CornerB.clone();
			topRight = CornerA.clone();
		}

		bottomLeft.setX(Math.min(CornerA.getX(), CornerB.getX()));
		topRight.setX(Math.max(CornerA.getX(), CornerB.getX()));

		bottomLeft.setZ(Math.min(CornerA.getZ(), CornerB.getZ()));
		topRight.setZ(Math.max(CornerA.getZ(), CornerB.getZ()));

		CornerBottomLeft = bottomLeft;
		CornerTopRight = topRight;
	}
	
	public boolean addItem(Item item) 
	{
		if (Items.size() >= 16)
		{
			UtilPlayer.message(Player, F.main("Game", "You cannot drop more than 16 Items!"));
			item.remove();
			return false;
		}
		
		Items.add(item);
		
		ItemMeta meta = item.getItemStack().getItemMeta();
		meta.setDisplayName(item.getUniqueId() + " NoStack");
		item.getItemStack().setItemMeta(meta);
		return true;
	}
	
	public boolean addParticles(Player player, ParticleType particleType) 
	{
		if (Particles.size() >= 24)
		{
			UtilPlayer.message(player, F.main("Game", "You cannot spawn more than 24 Particles!"));
			return false;
		}
		
		Location toPlace = player.getEyeLocation().add(player.getLocation().getDirection());
		
		if (!inBuildArea(toPlace.getBlock()))
		{
			UtilPlayer.message(player, F.main("Game", "You cannot place particles outside your plot!"));
			return false;
		}
		
		Particles.put(toPlace, particleType);
		
		UtilPlayer.message(player, F.main("Game", "You placed " + particleType.getFriendlyName() + "!"));
		
		return true;
	}
	
	public void resetParticles(Player player) 
	{
		Particles.clear();
		
		UtilPlayer.message(player, F.main("Game", "You cleared your Particles!"));
	}
	
	public boolean addEntity(Entity entity) 
	{
		if (entity instanceof Ghast)
		{
			if (Player != null)
			{
				UtilPlayer.message(Player, F.main("Game", "You cannot spawn Ghasts!"));
			}
			else
			{
				for (Player player : Team.GetPlayers(true))
				{
					UtilPlayer.message(player, F.main("Game", "You cannot spawn Ghasts!"));
				}
			}
			entity.remove();
			return false;
		}
		
		if (Entities.size() >= 16)
		{	
			if (Player != null)
			{
				UtilPlayer.message(Player, F.main("Game", "You cannot spawn more than 16 Entities!"));
			}
			else
			{
				for (Player player : Team.GetPlayers(true))
				{
					UtilPlayer.message(player, F.main("Game", "You cannot spawn more than 16 Entities!"));
				}
			}
			entity.remove();
			return false;
		}
		
		if (entity instanceof LivingEntity)
		{
			((LivingEntity)entity).setRemoveWhenFarAway(false);
			((LivingEntity)entity).setCustomName(UtilEnt.getName(entity));
			
		}
		
		Entities.add(entity);
		UtilEnt.vegetate(entity, true);
		UtilEnt.ghost(entity, true, false);
		return true;
	}
	
	public void removeEntity(Entity entity) 
	{
		if (Entities.remove(entity))
		{
			entity.remove();
			
			UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, entity.getLocation().add(0, 0.5, 0), 0, 0, 0, 0, 1,
					ViewDist.NORMAL, UtilServer.getPlayers());
		}	
	}
	
	public void addBlock(Block block)
	{
		Blocks.add(block);
	}
	
	public boolean inBuildArea(Block block)
	{
		if (!block.getWorld().getName().equals(Spawn.getWorld().getName())) return false;
		
		return inBuildArea(block.getLocation().toVector());
	}

	public boolean inBuildArea(Vector vec)
	{
		if (vec.getBlockX() < CornerBottomLeft.getBlockX())
			return false;

		if (vec.getBlockY() < CornerBottomLeft.getBlockY())
			return false;

		if (vec.getBlockZ() < CornerBottomLeft.getBlockZ())
			return false;

		if (vec.getBlockX() > CornerTopRight.getBlockX())
			return false;

		if (vec.getBlockY() > CornerTopRight.getBlockY())
			return false;

		if (vec.getBlockZ() > CornerTopRight.getBlockZ())
			return false;

		return true;
	}

	public enum WeatherType
	{
		SUNNY, RAINING, STORMING;
	}

	public void clean() 
	{
		//Clean Ents
		Iterator<Entity> entIter = Entities.iterator();
		
		while (entIter.hasNext())
		{
			Entity ent = entIter.next();
			if (!ent.isValid() || !inBuildArea(ent.getLocation().getBlock()))
			{
				entIter.remove();
				ent.remove();
				UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, ent.getLocation().add(0, 0.5, 0), 0, 0, 0, 0, 1,
						ViewDist.NORMAL, UtilServer.getPlayers());
			}
				
		}
		
		//Clean Items
		Iterator<Entity> itemIter = Items.iterator();
		
		while (itemIter.hasNext())
		{
			Entity ent = itemIter.next();
			if (!ent.isValid() || !inBuildArea(ent.getLocation().getBlock()))
			{
				itemIter.remove();
				ent.remove();
				UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, ent.getLocation().add(0, 0.5, 0), 0, 0, 0, 0, 1,
						ViewDist.NORMAL, UtilServer.getPlayers());
			}
		}
	}

	public void playParticles(boolean all) 
	{
		for (Location loc : Particles.keySet())
		{
			int amount = 8;
			
			ParticleType type = Particles.get(loc);
			
			if (type == ParticleType.HUGE_EXPLOSION ||
				type == ParticleType.LARGE_EXPLODE ||
				type == ParticleType.NOTE)
				amount = 1;
				
			if (all)
			{
				UtilParticle.PlayParticle(type, loc, 0.4f, 0.4f, 0.4f, 0, amount,
						ViewDist.LONGER, UtilServer.getPlayers());
			}
			else
			{
				if (Player != null)
				{
					UtilParticle.PlayParticle(type, loc, 0.4f, 0.4f, 0.4f, 0, amount, 
							ViewDist.LONGER, Player);
				}
				else
				{
					for (Player player : Team.GetPlayers(true))
					{
						UtilParticle.PlayParticle(type, loc, 0.4f, 0.4f, 0.4f, 0, amount, 
								ViewDist.LONGER, player);
					}
				}
			}
		}
	}

	public void playWeather(boolean all)
	{
		org.bukkit.WeatherType type = org.bukkit.WeatherType.CLEAR;
		if (Weather == WeatherType.STORMING || Weather == WeatherType.RAINING)
			type = org.bukkit.WeatherType.DOWNFALL;
		
		if (all)
		{
			for (Player player : UtilServer.getPlayers())
			{
				playWeather(player, type);
			}
		}
		else
		{
			if (Player != null)
			{
				playWeather(Player, type);
			}
			else
			{
				for (Player player : Team.GetPlayers(true))
				{
					playWeather(player, type);
				}
			}
		}
	}
	
	public void playWeather(Player player, org.bukkit.WeatherType type)
	{		
		player.setPlayerWeather(type);
		player.setPlayerTime(Time, false); 
		
		if (Weather == WeatherType.STORMING)
		{
			if (Math.random() > 0.7)
				player.playSound(player.getLocation(), Sound.AMBIENCE_THUNDER, 4f, 1f);
			
			//Strike Lightning Here
			if (Math.random() > 0.9)
			{
				Location loc = UtilBlock.getHighest(player.getWorld(),
						(int) (Spawn.getX() + Math.random() * 200 - 100),
						(int) (Spawn.getX() + Math.random() * 200 - 100)).getLocation();

				EntityLightning entity = new EntityLightning(((CraftWorld) loc.getWorld()).getHandle(), loc.getX(), loc.getY(), loc.getZ(), true);
				PacketPlayOutSpawnEntityWeather packet = new PacketPlayOutSpawnEntityWeather(entity);
				UtilPlayer.sendPacket(player, packet);
			}
		}
	}

	public void setGround(Player player, GroundData ground)
	{ 
		if (!Recharge.Instance.use(player, "Change Ground", 2000, true, false))
		{
			player.playSound(player.getLocation(), Sound.NOTE_BASS_GUITAR, 1f, 0.1f);
			return;
		}
		
		Material mat = ground.getMaterial();
		byte data = ground.getData();

		if (mat == Material.LAVA_BUCKET) mat = Material.LAVA;
		else if (mat == Material.WATER_BUCKET) mat = Material.WATER;

		//Set everything to air first to prevent the forming of obby.
		if (mat == Material.LAVA || mat == Material.WATER)
		{
			int y = CornerBottomLeft.getBlockY() - 1;
			for (int x = CornerBottomLeft.getBlockX(); x <= CornerTopRight.getBlockX(); x++)
			{
				for (int z = CornerBottomLeft.getBlockZ(); z <= CornerTopRight.getBlockZ(); z++)
				{
					MapUtil.QuickChangeBlockAt(player.getWorld(), x, y, z, Material.AIR, data);
				}
			}
		}
		
		int x = CornerBottomLeft.getBlockX();
		int y = CornerBottomLeft.getBlockY() - 1;
		int z = CornerBottomLeft.getBlockZ();
		
		if (ground.hasSchematic())
		{
			ground.getSchematic().paste(new Location(CornerBottomLeft.getWorld(), x, y, z), true);
			return;
		}
		
		for (int dx = x; dx <= CornerTopRight.getBlockX(); dx++)
		{
			for (int dz = z; dz <= CornerTopRight.getBlockZ(); dz++)
			{
				MapUtil.QuickChangeBlockAt(player.getWorld(), dx, y, dz, mat, data);
			}
		}
	}

	public void addPoints(double points) 
	{
		_totalPoints += points;
	}
	
	public double getPoints()
	{
		return _totalPoints;
	}

	public void clearPoints() 
	{
		_totalPoints = 0;
	}
	
	protected Location getMin()
	{
		return Vector.getMinimum(CornerBottomLeft.toVector(), CornerTopRight.toVector()).toBlockVector().toLocation(CornerBottomLeft.getWorld()).subtract(0, 1, 0);
	}
	
	protected Location getMax()
	{
		return Vector.getMaximum(CornerBottomLeft.toVector(), CornerTopRight.toVector()).toBlockVector().toLocation(CornerBottomLeft.getWorld());
	}
	
	public double getMaxHeight()
	{
		return CornerTopRight.getY();
	}
	
	/**
	 * Converts all the blocks inside the build to a schematic
	 * @return Returns a schematic of the build
	 */
	public Schematic convertToSchematic()
	{
		Location min = getMin();
		Location max = getMax();
		
		return UtilSchematic.createSchematic(min, max);
	}
	
	/**
	 * @return Returns a map of the particles with their relative vector paths to the build.
	 */
	public Map<Vector, ParticleType> getParticles()
	{
		Vector min = getMin().toVector();
		
		Map<Vector, ParticleType> map = new HashMap<>();
		for (Entry<Location, ParticleType> e : Particles.entrySet())
		{
			Vector v = e.getKey().toVector().subtract(min);
			map.put(v, e.getValue());
		}
				
		return map;
	}
	
	/**
	 * @return Returns the byte map from {@link #getParticles()} in byte form converted to NBT data and compressed with Gzip
	 */
	public byte[] getParticlesBytes()
	{
		Map<String, Tag> map = new HashMap<String, Tag>();
		int i = 0;
		
		for (Entry<Vector, ParticleType> e : getParticles().entrySet())
		{
			Map<String, Tag> entryMap = new HashMap<>();
			entryMap.put("x", new DoubleTag(e.getKey().getX()));
			entryMap.put("y", new DoubleTag(e.getKey().getY()));
			entryMap.put("z", new DoubleTag(e.getKey().getZ()));
			entryMap.put("particle", new StringTag(e.getValue().name()));
			
			CompoundTag entry = new CompoundTag(entryMap);
			map.put("particle" + i++, entry);
		}
		
		CompoundTag parent = new CompoundTag(map);
		
		return NBTUtils.toBytesCompressed("particles", parent);
	}
}