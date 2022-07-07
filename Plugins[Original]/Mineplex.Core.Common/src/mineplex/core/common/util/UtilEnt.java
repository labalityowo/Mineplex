package mineplex.core.common.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.EntityArrow;
import net.minecraft.server.v1_8_R3.EntityBat;
import net.minecraft.server.v1_8_R3.EntityCreature;
import net.minecraft.server.v1_8_R3.EntityEnderDragon;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntityTrackerEntry;
import net.minecraft.server.v1_8_R3.EntityTypes;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NavigationAbstract;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_8_R3.PathfinderGoal;
import net.minecraft.server.v1_8_R3.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_8_R3.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_8_R3.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_8_R3.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.TrigMath;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftArrow;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Giant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import mineplex.core.common.Pair;

public class UtilEnt
{
	public static final String FLAG_NO_REMOVE = "noremove";
	public static final String FLAG_ENTITY_COMPONENT = "component";

	//Custom Entity Names
	private static HashMap<Entity, String> _nameMap = new HashMap<>();
	private static HashMap<String, EntityType> creatureMap = new HashMap<>();
	
	private static Field _goalSelector;
	private static Field _targetSelector;
	private static Field _bsRestrictionGoal;
	private static Field _pathfinderBList;
	private static Field _pathfinderCList;

	public static HashMap<Entity, String> GetEntityNames() 
	{
		return _nameMap;
	}
	
	public static void silence(Entity entity, boolean silence)
	{
		net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) entity).getHandle();
		NBTTagCompound tag = new NBTTagCompound();
		nmsEntity.c(tag);
		tag.setByte("Silent", (byte) ((silence) ? 1 : 0));
		nmsEntity.f(tag);
		// Not working right now
		//((CraftEntity)entity).getHandle().setSilent(silence);
	}

	public static void addFlag(Entity entity, String flag)
	{
		if (entity == null)
			return;

		entity.setMetadata("flag:" + flag, new FixedMetadataValue(UtilServer.getPlugin(), true));
	}

	public static void removeFlag(Entity entity, String flag)
	{
		if (entity == null)
			return;

		entity.removeMetadata("flag:" + flag, UtilServer.getPlugin());
	}

	public static boolean hasFlag(Entity entity, String flag)
	{
		if (entity == null)
		{
			return false;
		}
		return entity.hasMetadata("flag:" + flag);
	}
	
	public static void ghost(Entity entity, boolean ghost, boolean invisible)
	{
		if (entity instanceof LivingEntity)
		{
			((CraftLivingEntity)entity).getHandle().setGhost(ghost);
		}
		
//		((CraftEntity)entity).getHandle().Invisible = invisible;
		((CraftEntity)entity).getHandle().setInvisible(invisible);
	}
	
	public static void leash(LivingEntity leashed, Entity holder, boolean pull, boolean breakable)
	{
		leashed.setLeashHolder(holder);
		
		if (!(((CraftLivingEntity)leashed).getHandle() instanceof EntityInsentient))
			return;
		
		((EntityInsentient)((CraftLivingEntity)leashed).getHandle()).setPullWhileLeashed(pull);
		((EntityInsentient)((CraftLivingEntity)leashed).getHandle()).setShouldBreakLeash(breakable);
	}

	public static void addLookAtPlayerAI(Entity entity, float dist)
	{
		if (((CraftEntity) entity).getHandle() instanceof EntityInsentient)
		{
			addAI(entity, 7, new PathfinderGoalLookAtPlayer(((EntityInsentient) ((CraftEntity) entity).getHandle()), EntityHuman.class, dist));
			addAI(entity, 8, new PathfinderGoalRandomLookaround(((EntityInsentient) ((CraftEntity) entity).getHandle())));
		}
	}

	public static void addAI(Entity entity, int value, PathfinderGoal ai)
	{
		if (((CraftEntity) entity).getHandle() instanceof EntityInsentient)
		{
			EntityInsentient ei = ((EntityInsentient) ((CraftEntity) entity).getHandle());

			if (_goalSelector == null)
			{
				try
				{
					_goalSelector = EntityInsentient.class.getDeclaredField("goalSelector");
				}
				catch (NoSuchFieldException e)
				{
					e.printStackTrace();
					return;
				}
				_goalSelector.setAccessible(true);
			}

			try
			{
				((PathfinderGoalSelector) _goalSelector.get(ei)).a(value, ai);
			}
			catch (IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}

	}
	
	/**
	 * See {@link #getEntitiesInsideEntity(Entity, List)}
	 * Uses all players in the same world as the entity as input
	 */
	public static List<Player> getPlayersInsideEntity(Entity ent)
	{
		return getEntitiesInsideEntity(ent, ent.getWorld().getPlayers());
	}
	
	/**
	 * See {@link #getEntitiesInsideEntity(Entity, List)}
	 * Uses all entities in the same world as the entity as input
	 */
	public static List<Entity> getEntitiesInsideEntity(Entity ent)
	{
		return getEntitiesInsideEntity(ent, ent.getWorld().getEntities());
	}
	
	/**
	 * See {@link #getEntitiesInsideEntity(Entity, List)}
	 * Auto cast to list of players
	 */
	public static List<Player> getPlayersInsideEntity(Entity ent, List<Player> players)
	{
		return getEntitiesInsideEntity(ent, players);
	}
	
	/**
	 * Returns entities which are inside the provided entity's boundingbox
	 * @param ent The entity to check inside
	 * @param entities List of entities to check
	 * @return Returns a sublist of entities which are inside the entity's boundingbox
	 */
	public static <T extends Entity> List<T> getEntitiesInsideEntity(Entity ent, List<T> entities)
	{
		AxisAlignedBB box = ((CraftEntity)ent).getHandle().getBoundingBox();
		
		List<T> list = new ArrayList<>();
		
		for(T e : entities)
		{
			AxisAlignedBB box2 = ((CraftEntity)e).getHandle().getBoundingBox();
			if(box2.b(box)) list.add(e);
		}
		return list;
	}
	
	/**
	 * @return Returns true if the entities boundinbox collides or is inside the given bounding box
	 */
	public static boolean isInsideBoundingBox(Entity ent, Vector a, Vector b)
	{
		AxisAlignedBB box = ((CraftEntity)ent).getHandle().getBoundingBox();
		AxisAlignedBB box2 = new AxisAlignedBB(a.getX(), a.getY(), a.getZ(), b.getX(), b.getY(), b.getZ());
		
		return box.b(box2);
	}
	
	public static void vegetate(Entity entity)
	{
		vegetate(entity, false);
	}

	public static void vegetate(Entity entity, boolean mute)
	{
    	try
		{
    		if (_goalSelector == null)
    		{
				_goalSelector = EntityInsentient.class.getDeclaredField("goalSelector");
				_goalSelector.setAccessible(true);
    		}
    		
    		if (_targetSelector == null)
    		{
				_targetSelector = EntityInsentient.class.getDeclaredField("targetSelector");
				_targetSelector.setAccessible(true);
    		}
    		
    		if (_pathfinderBList == null)
    		{
    			_pathfinderBList = PathfinderGoalSelector.class.getDeclaredField("b");
    			_pathfinderBList.setAccessible(true);
    		}
    		
    		if (_pathfinderCList == null)
    		{
    			_pathfinderCList = PathfinderGoalSelector.class.getDeclaredField("c");
    			_pathfinderCList.setAccessible(true);
    		}
    		
    		if (entity instanceof CraftCreature)
    		{
    			EntityCreature creature = ((CraftCreature) entity).getHandle();
    			
	    		if (_bsRestrictionGoal == null)
	    		{
					_bsRestrictionGoal = EntityCreature.class.getDeclaredField("c");
					_bsRestrictionGoal.setAccessible(true);
	    		}
	    		
	    		_bsRestrictionGoal.set(creature, new PathfinderGoalMoveTowardsRestriction(creature, 0D));
    		}
        	
    		if (((CraftEntity)entity).getHandle() instanceof EntityInsentient)
    		{
    			EntityInsentient creature = (EntityInsentient) ((CraftEntity) entity).getHandle();
		        
				creature.setVegetated(true);
				creature.setSilent(mute);

				((List<?>) _pathfinderBList.get(((PathfinderGoalSelector) _goalSelector.get(creature)))).clear();
    			((List<?>) _pathfinderCList.get(((PathfinderGoalSelector) _goalSelector.get(creature)))).clear();
    			
    			((List<?>) _pathfinderBList.get(((PathfinderGoalSelector) _targetSelector.get(creature)))).clear();
    			((List<?>) _pathfinderCList.get(((PathfinderGoalSelector) _targetSelector.get(creature)))).clear();
    		}
    		
    		if (((CraftEntity)entity).getHandle() instanceof EntityBat)
    		{
    			((EntityBat) ((CraftEntity) entity).getHandle()).setVegetated(true);
    		}
	    	
    		if (((CraftEntity) entity).getHandle() instanceof EntityEnderDragon)
    		{
    			EntityEnderDragon creature = (EntityEnderDragon) ((CraftEntity) entity).getHandle();
		        
    			creature.setVegetated(true);
    		}
		} 
    	catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e)
		{
			e.printStackTrace();
		}
	}

	public static void removeGoalSelectors(Entity entity)
	{
		if (((CraftEntity)entity).getHandle() instanceof EntityInsentient)
		{
			EntityInsentient creature = (EntityInsentient)((CraftEntity)entity).getHandle();
			creature.goalSelector = new PathfinderGoalSelector(((CraftWorld)entity.getWorld()).getHandle().methodProfiler);
		}
	}

	public static void removeTargetSelectors(Entity entity)
	{
		if (((CraftEntity)entity).getHandle() instanceof EntityInsentient)
		{
			EntityInsentient creature = (EntityInsentient)((CraftEntity)entity).getHandle();
			creature.targetSelector = new PathfinderGoalSelector(((CraftWorld)entity.getWorld()).getHandle().methodProfiler);
		}
	}
	
	public static void addGoalSelector(Entity entity, int priority, PathfinderGoal goal) 
	{
		try
		{
			if(((CraftEntity)entity).getHandle() instanceof EntityInsentient) 
			{
				((EntityInsentient)((CraftEntity)entity).getHandle()).goalSelector.a(priority, goal);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
    public static void Rotate(LivingEntity entity, float yaw, float pitch)
    {
		EntityLiving handle = ((CraftLivingEntity) entity).getHandle();
    	
		while (yaw < -180.0F) yaw += 360.0F;
		while (yaw >= 180.0F) yaw -= 360.0F;
        
		handle.yaw = yaw;
		handle.aK = yaw;
		handle.aI = yaw;
		handle.aL = yaw;
		handle.pitch = pitch;
    }
	
	public static void LookAt(LivingEntity entity, Location location)
	{
		if (!(entity.getWorld().equals(location.getWorld())))
			return;
		
		Vector dir = entity.getEyeLocation().toVector().subtract(location.toVector()).normalize();
		Location loc = entity.getEyeLocation().clone();
        
        loc.setYaw(180 - (float) Math.toDegrees(TrigMath.atan2(dir.getX(), dir.getZ())));
        loc.setPitch(90 - (float) Math.toDegrees(Math.acos(dir.getY())));
        
        Rotate(entity, loc.getYaw(), loc.getPitch());
	}
	
	public static void populate()
	{
		if (creatureMap.isEmpty())
		{
			creatureMap.put("Bat", EntityType.BAT);
			creatureMap.put("Blaze", EntityType.BLAZE);
			creatureMap.put("Arrow", EntityType.ARROW);
			creatureMap.put("Cave Spider", EntityType.CAVE_SPIDER);
			creatureMap.put("Chicken", EntityType.CHICKEN);
			creatureMap.put("Cow", EntityType.COW);
			creatureMap.put("Creeper", EntityType.CREEPER);
			creatureMap.put("Ender Dragon", EntityType.ENDER_DRAGON);
			creatureMap.put("Enderman", EntityType.ENDERMAN);
			creatureMap.put("Ghast", EntityType.GHAST);
			creatureMap.put("Giant", EntityType.GIANT);
			creatureMap.put("Horse", EntityType.HORSE);
			creatureMap.put("Iron Golem", EntityType.IRON_GOLEM);
			creatureMap.put("Item", EntityType.DROPPED_ITEM);
			creatureMap.put("Magma Cube", EntityType.MAGMA_CUBE);
			creatureMap.put("Mooshroom", EntityType.MUSHROOM_COW);
			creatureMap.put("Ocelot", EntityType.OCELOT);
			creatureMap.put("Pig", EntityType.PIG);
			creatureMap.put("Pig Zombie", EntityType.PIG_ZOMBIE);
			creatureMap.put("Sheep", EntityType.SHEEP);
			creatureMap.put("Silverfish", EntityType.SILVERFISH);
			creatureMap.put("Skeleton", EntityType.SKELETON);
			creatureMap.put("Slime", EntityType.SLIME);
			creatureMap.put("Snowman", EntityType.SNOWMAN);
			creatureMap.put("Spider", EntityType.SPIDER);
			creatureMap.put("Squid", EntityType.SQUID);
			creatureMap.put("Villager", EntityType.VILLAGER);
			creatureMap.put("Witch", EntityType.WITCH);
			creatureMap.put("Wither", EntityType.WITHER);
			creatureMap.put("WitherSkull", EntityType.WITHER_SKULL);
			creatureMap.put("Wolf", EntityType.WOLF);
			creatureMap.put("Zombie", EntityType.ZOMBIE);
			creatureMap.put("Guardian", EntityType.GUARDIAN);
			creatureMap.put("Rabbit", EntityType.RABBIT);
		}
	}
	
	/**
	 * Set whether this entity should be ticked normally when far away. By default entities are only ticked once every 20 ticks
	 * when they are outside the activation range.
	 * 
	 *  Default ranges are calculated in a AABB fashion from their closest player:
	 *  	animalActivationRange = 32
	 *  	monsterActivationRange = 32
  	 *  	miscActivationRange = 16
  	 *  
  	 *  Entities that are unaffected by range (always active):
  	 *  	Players, Projectiles, Enderdragon, Wither, Fireballs, Lightning strikes, TNT, Ender Crystals and Fireworks.
  	 *  
  	 *  You can make entities which are by default active (Projectiles etc) not load when far away
  	 *  or make entities that are not active by default (mobs, animals etc) load when far away
	 */
	public static void setTickWhenFarAway(Entity ent, boolean loadWhenFar)
	{
		try
		{
			Field state = net.minecraft.server.v1_8_R3.Entity.class.getDeclaredField("defaultActivationState");
			state.setAccessible(true);
			state.setBoolean(((CraftEntity)ent).getHandle(), loadWhenFar);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static String getName(Entity ent)
	{
		if (ent == null)
			return "Null";
		
		if (ent.getType() == EntityType.PLAYER)
			return ((Player)ent).getName();
		
		if (GetEntityNames().containsKey(ent))
			return GetEntityNames().get(ent);
		
		if (ent instanceof LivingEntity)
		{
			LivingEntity le = (LivingEntity)ent;
			if (le.getCustomName() != null)
				return le.getCustomName();
		}
		
		return getName(ent.getType());  
	}

	public static String getName(EntityType type)
	{
		populate();

		for (String cur : creatureMap.keySet())
			if (creatureMap.get(cur) == type)
				return cur;

		return type.getName();
	}

	public static String searchName(Player caller, String arg, boolean inform)
	{
		populate();

		arg = arg.toLowerCase().replaceAll("_", " ");
		LinkedList<String> matchList = new LinkedList<String>();
		for (String cur : creatureMap.keySet())
		{
			if (cur.equalsIgnoreCase(arg))
				return cur;
			
			if (cur.toLowerCase().contains(arg))
				matchList.add(cur);
		}
			

		//No / Non-Unique
		if (matchList.size() != 1)
		{
			if (!inform)
				return null;

			//Inform
			UtilPlayer.message(caller, F.main("Creature Search", "" +
					C.mCount + matchList.size() +
					C.mBody + " matches for [" +
					C.mElem + arg +
					C.mBody + "]."));

			if (matchList.size() > 0)
			{
				String matchString = "";
				for (String cur : matchList)
					matchString += F.elem(cur) + ", ";
				if (matchString.length() > 1)
					matchString = matchString.substring(0 , matchString.length() - 2);

				UtilPlayer.message(caller, F.main("Creature Search", "" +
						C.mBody + "Matches [" +
						C.mElem + matchString +
						C.mBody + "]."));
			}

			return null;
		}

		return matchList.get(0);
	}

	public static EntityType searchEntity(Player caller, String arg, boolean inform)
	{
		populate();

		arg = arg.toLowerCase();
		LinkedList<EntityType> matchList = new LinkedList<EntityType>();
		for (String cur : creatureMap.keySet())
		{
			if (cur.equalsIgnoreCase(arg))
				return creatureMap.get(cur);
			
			if (cur.toLowerCase().contains(arg))
				matchList.add(creatureMap.get(cur));
		}
			

		//No / Non-Unique
		if (matchList.size() != 1)
		{
			if (!inform)
				return null;

			//Inform
			UtilPlayer.message(caller, F.main("Creature Search", "" +
					C.mCount + matchList.size() +
					C.mBody + " matches for [" +
					C.mElem + arg +
					C.mBody + "]."));

			if (matchList.size() > 0)
			{
				String matchString = "";
				for (EntityType cur : matchList)
					matchString += F.elem(cur.getName()) + ", ";
				if (matchString.length() > 1)
					matchString = matchString.substring(0 , matchString.length() - 2);

				UtilPlayer.message(caller, F.main("Creature Search", "" +
						C.mBody + "Matches [" +
						C.mElem + matchString +
						C.mBody + "]."));
			}

			return null;
		}

		return matchList.get(0);
	}
	
	public static HashMap<LivingEntity, Double> getInRadius(Location loc,	double dR) 
	{
		HashMap<LivingEntity, Double> ents = new HashMap<>();

		for (Entity cur : loc.getWorld().getEntities())
		{
			if (!(cur instanceof LivingEntity) || UtilPlayer.isSpectator(cur))
				continue;
			
			LivingEntity ent = (LivingEntity)cur;
			
			//Feet
			double offset = UtilMath.offset(loc, ent.getLocation());
			
			if (offset < dR)
			{
				ents.put(ent, 1 - (offset / dR));
				continue;
			}
			
			//Eyes
			offset = UtilMath.offset(loc, ent.getEyeLocation());
			
			if (offset < dR)
			{
				ents.put(ent, 1 - (offset / dR));
			}
		}

		return ents;
	}
	
	public static HashMap<Entity, Double> getAllInRadius(Location loc,	double dR) 
	{
		HashMap<Entity, Double> ents = new HashMap<Entity, Double>();

		for (Entity cur : loc.getWorld().getEntities())
		{
			if (UtilPlayer.isSpectator(cur))
				continue;
			
			//Loc
			double offset = UtilMath.offset(loc, cur.getLocation());
			
			if (offset < dR)
			{
				ents.put(cur, 1 - (offset / dR));
			}
		}

		return ents;
	}
	
	public static boolean hitBox(Location loc, LivingEntity ent, double mult, EntityType disguise)
	{
		if (disguise != null)
		{
			if (disguise == EntityType.SQUID)
			{
				return UtilMath.offset(loc, ent.getLocation().add(0, 0.4, 0)) < 0.6 * mult;
			}
		}
		
		if (ent instanceof Player)
		{
			Player player = (Player)ent;
			
			if (UtilMath.offset(loc, player.getEyeLocation()) < 0.4 * mult)
			{
				return true;
			}
			else if (UtilMath.offset2d(loc, player.getLocation()) < 0.6 * mult)
			{
				if (loc.getY() >= player.getLocation().getY() - 0.2*mult && loc.getY() <= player.getEyeLocation().getY() + 0.2*mult)
				{
					return true;
				}		
			}
		}
		else
		{
			if (ent instanceof Giant)
			{
				if (loc.getY() > ent.getLocation().getY() && loc.getY() < ent.getLocation().getY() + 12)
					if (UtilMath.offset2d(loc, ent.getLocation()) < 4)
						return true;
			}
			else
			{
				if (loc.getY() > ent.getLocation().getY() && loc.getY() < ent.getLocation().getY() + 2)
					if (UtilMath.offset2d(loc, ent.getLocation()) < 0.5 * mult)
						return true;
			}
		}
			
		

		return false;
	}
	
	public static float getStepHeight(Entity ent)
	{
		return ((CraftEntity)ent).getHandle().S;
	}
	
	public static void setStepHeight(Entity ent, float stepHeight)
	{
		((CraftEntity)ent).getHandle().S = stepHeight;
	}
	
	public static boolean isGrounded(Entity ent) 
	{ 
		
		if(!(ent instanceof Player)) {
			return ent.isOnGround();
		}
		
		AxisAlignedBB box = ((CraftEntity)ent).getHandle().getBoundingBox();
		Location bottom_corner_1 = new Location(ent.getWorld(), box.a, ent.getLocation().getY()-0.1, box.c);
		Location bottom_corner_2 = new Location(ent.getWorld(), box.d, ent.getLocation().getY()-0.1, box.f);
		
		for(Block b : UtilBlock.getInBoundingBox(bottom_corner_1, bottom_corner_2)){
			if(UtilBlock.solid(b)) return true;
		}
		return false;
	}
	
	public static boolean isGrounded(Entity ent, Location loc) 
	{ 
		AxisAlignedBB box = ((CraftEntity)ent).getHandle().getBoundingBox();
		Location bottom_corner_1 = new Location(ent.getWorld(), box.a, loc.getY()-0.1, box.c);
		Location bottom_corner_2 = new Location(ent.getWorld(), box.d, loc.getY()-0.1, box.f);
		
		for(Block b : UtilBlock.getInBoundingBox(bottom_corner_1, bottom_corner_2)){
			if(UtilBlock.solid(b)) return true;
		}
		return false;
	}

	public static void PlayDamageSound(LivingEntity damagee) 
	{
		Sound sound = Sound.HURT_FLESH;
		
		if (damagee.getType() == EntityType.BAT)				sound = Sound.BAT_HURT;
		else if (damagee.getType() == EntityType.BLAZE)			sound = Sound.BLAZE_HIT;
		else if (damagee.getType() == EntityType.CAVE_SPIDER)	sound = Sound.SPIDER_IDLE;
		else if (damagee.getType() == EntityType.CHICKEN)		sound = Sound.CHICKEN_HURT;
		else if (damagee.getType() == EntityType.COW)			sound = Sound.COW_HURT;
		else if (damagee.getType() == EntityType.CREEPER)		sound = Sound.CREEPER_HISS;
		else if (damagee.getType() == EntityType.ENDER_DRAGON)	sound = Sound.ENDERDRAGON_GROWL;
		else if (damagee.getType() == EntityType.ENDERMAN)		sound = Sound.ENDERMAN_HIT;
		else if (damagee.getType() == EntityType.GHAST)			sound = Sound.GHAST_SCREAM;
		else if (damagee.getType() == EntityType.GIANT)			sound = Sound.ZOMBIE_HURT;
		//else if (damagee.getType() == EntityType.HORSE)		sound = Sound.
		else if (damagee.getType() == EntityType.IRON_GOLEM)	sound = Sound.IRONGOLEM_HIT;
		else if (damagee.getType() == EntityType.MAGMA_CUBE)	sound = Sound.MAGMACUBE_JUMP;
		else if (damagee.getType() == EntityType.MUSHROOM_COW)	sound = Sound.COW_HURT;
		else if (damagee.getType() == EntityType.OCELOT)		sound = Sound.CAT_MEOW;
		else if (damagee.getType() == EntityType.PIG)			sound = Sound.PIG_IDLE;
		else if (damagee.getType() == EntityType.PIG_ZOMBIE)	sound = Sound.ZOMBIE_HURT;
		else if (damagee.getType() == EntityType.SHEEP)			sound = Sound.SHEEP_IDLE;
		else if (damagee.getType() == EntityType.SILVERFISH)	sound = Sound.SILVERFISH_HIT;
		else if (damagee.getType() == EntityType.SKELETON)		sound = Sound.SKELETON_HURT;
		else if (damagee.getType() == EntityType.SLIME)			sound = Sound.SLIME_ATTACK;
		else if (damagee.getType() == EntityType.SNOWMAN)		sound = Sound.STEP_SNOW;
		else if (damagee.getType() == EntityType.SPIDER)		sound = Sound.SPIDER_IDLE;
		//else if (damagee.getType() == EntityType.SQUID)		sound = Sound;
		//else if (damagee.getType() == EntityType.VILLAGER)	sound = Sound;
		//else if (damagee.getType() == EntityType.WITCH)		sound = Sound.;
		else if (damagee.getType() == EntityType.WITHER)		sound = Sound.WITHER_HURT;
		else if (damagee.getType() == EntityType.WOLF)			sound = Sound.WOLF_HURT;
		else if (damagee.getType() == EntityType.ZOMBIE)		sound = Sound.ZOMBIE_HURT;	

		damagee.getWorld().playSound(damagee.getLocation(), sound, 1.5f + (float)(0.5f * Math.random()), 0.8f + (float)(0.4f * Math.random()));
	}

	public static boolean onBlock(Player player) 
	{
		//Side Standing
		double xMod = player.getLocation().getX() % 1;
		if (player.getLocation().getX() < 0)
			xMod += 1;
		
		double zMod = player.getLocation().getZ() % 1;
		if (player.getLocation().getZ() < 0)
			zMod += 1;

		int xMin = 0;
		int xMax = 0;
		int zMin = 0;
		int zMax = 0;
		
		if (xMod < 0.3)	xMin = -1;
		if (xMod > 0.7)	xMax = 1;
		
		if (zMod < 0.3)	zMin = -1;
		if (zMod > 0.7)	zMax = 1;

		for (int x=xMin ; x<=xMax ; x++)
		{
			for (int z=zMin ; z<=zMax ; z++)
			{				
				//Standing on SOMETHING
				if (player.getLocation().add(x, -0.5, z).getBlock().getType() != Material.AIR && !player.getLocation().add(x, -0.5, z).getBlock().isLiquid())
					return true;
				
				//Inside a Lillypad
				if (player.getLocation().add(x, 0, z).getBlock().getType() == Material.WATER_LILY)
					return true;
				
				//Fences/Walls
				Material beneath = player.getLocation().add(x, -1.5, z).getBlock().getType();
				if (player.getLocation().getY() % 0.5 == 0 &&
					(beneath.toString().contains("FENCE") || beneath == Material.COBBLE_WALL))
					return true;
			}	
		}
		
		return false;
	}

	public static boolean CreatureLook(Entity ent, Entity target)
	{
		return CreatureLook(ent, target instanceof LivingEntity ? ((LivingEntity) target).getEyeLocation() : target.getLocation());
	}

	public static boolean CreatureLook(Entity ent, Location target)
	{
		Vector vec = UtilAlg.getTrajectory(ent.getLocation(), target);

		return CreatureLook(ent, UtilAlg.GetPitch(vec), UtilAlg.GetYaw(vec));
	}

	public static boolean CreatureLook(Entity ent, double srcx, double srcy, double srcz, double dstx, double dsty, double dstz)
	{
		double[] vec = UtilAlg.getTrajectory(srcx, srcy, srcz, dstx, dsty, dstz);

		return CreatureLook(ent, UtilAlg.GetPitch(vec), UtilAlg.GetYaw(vec));
	}

	public static boolean CreatureLook(Entity ent, Vector target)
	{
		return CreatureLook(ent, UtilAlg.GetPitch(target), UtilAlg.GetYaw(target));
	}

	public static void setFakeHead(Entity ent, boolean fakeHead)
	{
		net.minecraft.server.v1_8_R3.Entity ec = ((CraftEntity) ent).getHandle();

		ec.setFakeHead(fakeHead);
	}

	public static void CreatureForceLook(Entity ent, float pitch, float yaw)
	{
		net.minecraft.server.v1_8_R3.Entity ec = ((CraftEntity) ent).getHandle();

		ec.setFakeHead(true);

		ec.fakePitch = pitch;
		ec.fakeYaw = yaw;
	}

	public static boolean CreatureLook(Entity ent, float pitch, float yaw)
	{
		if (!(ent instanceof LivingEntity))
			return false;

		EntityLiving ec = ((CraftLivingEntity) ent).getHandle();

		ec.yaw = yaw;
		ec.pitch = pitch;
		ec.aK = yaw;
		ec.fakePitch = pitch;
		ec.fakeYaw = yaw;

		EntityTrackerEntry entry = ((WorldServer) ec.getWorld()).tracker.trackedEntities.get(ec.getId());

		if (entry != null)
		{
			byte ya = (byte) (yaw * 256.0F / 360.0F);
			byte pi = (byte) (pitch * 256.0F / 360.0F);

			entry.yRot = ya;
			entry.xRot = pi;
			entry.i = ya;

			// Looks like both packets need to be sent. EntityLook packet for body yaw and head pitch. Head rotation for head yaw.
			entry.broadcast(new PacketPlayOutEntity.PacketPlayOutEntityLook(ent.getEntityId(), ya, pi, ec.onGround));
			entry.broadcast(new PacketPlayOutEntityHeadRotation(ec, ya));
		}

		return true;
	}

	public static boolean CreatureLook(Entity ent, float yaw)
	{
		return CreatureLook(ent, 0, yaw);
	}

	public static void CreatureMove(Entity ent, Location target, float speed) 
	{
		if (!(ent instanceof Creature))
			return;
		
		if (UtilMath.offsetSquared(ent.getLocation(), target) < 0.01)
			return;

		EntityCreature ec = ((CraftCreature)ent).getHandle();
		NavigationAbstract nav = ec.getNavigation();
		
		if (UtilMath.offsetSquared(ent.getLocation(), target) > 16 * 16)
		{
			Location newTarget = ent.getLocation();

			newTarget.add(UtilAlg.getTrajectory(ent.getLocation(), target).multiply(16));

			nav.a(newTarget.getX(), newTarget.getY(), newTarget.getZ(), speed);
		}
		else
		{
			nav.a(target.getX(), target.getY(), target.getZ(), speed);
		}
	}
	
	public static boolean CreatureMoveFast(Entity ent, Location target, float speed) 
	{
		return CreatureMoveFast(ent, target, speed, true);
	}
	
	public static boolean CreatureMoveFast(Entity ent, Location target, float speed, boolean slow) 
	{
		if (!(ent instanceof Creature))
			return false;
		
		if (UtilMath.offsetSquared(ent.getLocation(), target) < 0.01)
			return false;
		
		if (UtilMath.offsetSquared(ent.getLocation(), target) < 4)
			speed = Math.min(speed, 1f);
		
		EntityCreature ec = ((CraftCreature)ent).getHandle();
		ec.getControllerMove().a(target.getX(), target.getY(), target.getZ(), speed);
		
		return true;
	}
	
	/**
	 * Returns true if the entity got a path that will lead it closer to the current navigation path finding target.
	 * It will return false, it it is as close as it can get. Using this got an advantage compared to distance checking, as the target
	 * might be inside blocks, leaving the entity unable to get any closer.
	 * @param ent The entity to check
	 * @return Returns whether the entity can walk any closer to the current navigation target.
	 */
	public static boolean canEntityWalkCloserToNavigationTarget(Creature ent)
	{
		return ((CraftCreature)ent).getHandle().getNavigation().m();
	}

	public static int getNewEntityId()
	{
		return getNewEntityId(true);
	}
	
	/**
	 * Use false if you don't want to modify the next entityid to be used.
	 * 
	 * Normally you want true if you want a unique entityid to use.
	 **/
	public static int getNewEntityId(boolean modifynumber)
    {
		try
		{
			Field field = net.minecraft.server.v1_8_R3.Entity.class.getDeclaredField("entityCount");
			field.setAccessible(true);
			int entityId = field.getInt(null);
			if (modifynumber)
			{
				field.set(null, entityId + 1);
			}
			return entityId;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return -1;
    }
	
	public static Entity getEntityById(int entityId)
	{
		for (World world : Bukkit.getWorlds())
		{
			for (Entity entity : world.getEntities())
			{
				if (entity.getEntityId() == entityId)
				{
					return entity;
				}
			}
		}
		
		return null;
	}
	
	public static void setAI(LivingEntity entity, boolean ai)
	{
		if(entity instanceof ArmorStand)
		{
			((ArmorStand)entity).setGravity(ai);
			return;
		}
		CraftEntity e = (CraftEntity)entity;
		if(e.getHandle() instanceof EntityInsentient)
		{
			((EntityInsentient)e.getHandle()).k(!ai);
		}
	}
	
	public static double getBlockSizeOfSlime(int size)
	{
		return .51 * ((double) size);
	}
	
	public static void setBoundingBox(Entity ent, double width, double height)
	{
		((CraftEntity)ent).getHandle().setSize((float) width, (float)height);
	}
	
	public static double getHeight(Entity ent)
	{
		return ((CraftEntity)ent).getHandle().length;
	}

	public static double getWidth(Entity ent)
	{
		return ((CraftEntity)ent).getHandle().width;
	}
	
	public static void SetMetadata(Entity entity, String key, Object value)
	{
		entity.setMetadata(key, new FixedMetadataValue(UtilServer.getPlugin(), value));
	}
	
	// Nicer than doing entity.getMetadata(key).get(0);
	@SuppressWarnings("unchecked")
	public static <T> T GetMetadata(Entity entity, String key)
	{
		if (!entity.hasMetadata(key))
		{
			return null;
		}
		
		return (T) entity.getMetadata(key).get(0).value();
	}
	
	public static void removeMetadata(Entity entity, String key)
	{
		entity.removeMetadata(key, UtilServer.getPlugin());
	}

    public static byte getEntityEggData(EntityType type)
    {
        switch (type)
        {
            case CREEPER: return (byte) 50;
            case SKELETON: return (byte) 51;
            case SPIDER: return (byte) 52;
            case ZOMBIE: return (byte) 54;
            case SLIME: return (byte) 55;
            case GHAST: return (byte) 56;
            case PIG_ZOMBIE: return (byte) 57;
            case ENDERMAN: return (byte) 58;
            case CAVE_SPIDER: return (byte) 59;
            case SILVERFISH: return (byte) 60;
            case BLAZE: return (byte) 61;
            case MAGMA_CUBE: return (byte) 62;
            case BAT: return (byte) 65;
            case WITCH: return (byte) 66;
            case ENDERMITE: return (byte) 67;
            case GUARDIAN: return (byte) 68;
            //case SHULKER: return (byte) 69;
            case PIG: return (byte) 90;
            case SHEEP: return (byte) 91;
            case COW: return (byte) 92;
            case CHICKEN: return (byte) 93;
            case SQUID: return (byte) 94;
            case WOLF: return (byte) 95;
            case MUSHROOM_COW: return (byte) 96;
            case OCELOT: return (byte) 98;
            case HORSE: return (byte) 100;
            case RABBIT: return (byte) 101;
            case VILLAGER: return (byte) 120;
            default: return 0;
        }
    }

    public static boolean isInWater(Entity entity)
	{
		return ((CraftEntity) entity).getHandle().inWater;
	}

	public static void registerEntityType(Class<? extends net.minecraft.server.v1_8_R3.Entity> customClass, EntityType entityType, String name)
	{
		EntityTypes.getNameToClassMap().remove(name);
		EntityTypes.getIdToClassMap().remove((int) entityType.getTypeId());
		EntityTypes.register(customClass, name, (int) entityType.getTypeId());
	}

	public static void spawnEntity(net.minecraft.server.v1_8_R3.Entity entity, Location location)
	{
		entity.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		((CraftWorld) location.getWorld()).getHandle().addEntity(entity, SpawnReason.CUSTOM);
	}

	public static Block getHitBlock(Entity arrow)
	{
		if (!(arrow instanceof Arrow))
		{
			return null;
		}

		try
		{
			EntityArrow entityArrow = ((CraftArrow) arrow).getHandle();
			Class<?> clazz = entityArrow.getClass();

			Field fieldX = clazz.getDeclaredField("d");
			Field fieldY = clazz.getDeclaredField("e");
			Field fieldZ = clazz.getDeclaredField("f");

			fieldX.setAccessible(true);
			fieldY.setAccessible(true);
			fieldZ.setAccessible(true);

			int x = fieldX.getInt(entityArrow);
			int y = fieldY.getInt(entityArrow);
			int z = fieldZ.getInt(entityArrow);

			return arrow.getWorld().getBlockAt(x, y, z);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	public static void setPosition(Entity entity, Location location)
	{
		((CraftEntity) entity).getHandle().setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
	}

	public static Pair<Location, Location> getSideStandingBox(Entity entity)
	{
		Location location = entity.getLocation();

		double xMod = location.getX() % 1;
		double zMod = location.getZ() % 1;

		if (location.getX() < 0)
		{
			xMod += 1;
		}

		if (location.getZ() < 0)
		{
			zMod += 1;
		}

		int xMin = 0;
		int xMax = 0;
		int zMin = 0;
		int zMax = 0;

		if (xMod < 0.3)
		{
			xMin = -1;
		}
		else if (xMod > 0.7)
		{
			xMax = 1;
		}

		if (zMod < 0.3)
		{
			zMin = -1;
		}
		else if (zMod > 0.7)
		{
			zMax = 1;
		}

		return Pair.create(new Location(location.getWorld(), xMin, 0, zMin), new Location(location.getWorld(), xMax, 0, zMax));
	}
}