package mineplex.core.memory;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mineplex.core.MiniPlugin;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import net.minecraft.server.v1_8_R3.CraftingManager;
import net.minecraft.server.v1_8_R3.EnchantmentManager;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.IInventory;

import net.minecraft.server.v1_8_R3.PathfinderGoal;
import net.minecraft.server.v1_8_R3.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class MemoryFix extends MiniPlugin
{
	private static Field _intHashMap;

	public MemoryFix(JavaPlugin plugin)
	{
		super("Memory Fix", plugin);

		//_intHashMap = IntHashMap.class.	
	}

	@EventHandler
	public void fixLastDamageEventLeaks(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
			return;

		List<World> worldList = Bukkit.getWorlds();
		Set<World> worlds = new HashSet<>(worldList);

		// Sanity check
		if (worlds.size() != worldList.size())
			throw new RuntimeException("Error: Duplicated worlds?!?!");
		
		for (World world : worlds)
		{
			WorldServer worldServer = ((CraftWorld) world).getHandle();

			for (net.minecraft.server.v1_8_R3.Entity nmsentity : worldServer.entityList)
			{
				Entity entity = nmsentity.getBukkitEntity();
				EntityDamageEvent lastDamageCause = entity.getLastDamageCause();
				if (lastDamageCause != null)
				{
					Entity damaged = lastDamageCause.getEntity();
					Entity damagerEntity = null;
					Block damagerBlock = null;
					if (lastDamageCause instanceof EntityDamageByEntityEvent)
						damagerEntity = ((EntityDamageByEntityEvent) lastDamageCause).getDamager();
					if (lastDamageCause instanceof EntityDamageByBlockEvent)
						damagerBlock = ((EntityDamageByBlockEvent) lastDamageCause).getDamager();

					boolean shouldRemove = false;

					if (!damaged.isValid())
						shouldRemove = true;
					else if (damagerEntity != null)
					{
						if (!damagerEntity.isValid())
							shouldRemove = true;
						else if (!worlds.contains(damagerEntity.getWorld()))
							shouldRemove = true;
					}
					else if (damagerBlock != null)
					{
						if (!worlds.contains(damagerBlock.getWorld()))
							shouldRemove = true;
					}

					if (shouldRemove)
						entity.setLastDamageCause(null);
				}
			}
		}
	}

	private static Field PATHFINDER_GOAL_SELECTOR_B;
	private static boolean PATHFINDER_GOAL_SELECTOR_B_SUCCESSFUL;

	private static Field PATHFINDER_GOAL_SELECTOR_C;
	private static boolean PATHFINDER_GOAL_SELECTOR_C_SUCCESSFUL;

	private static Field PATHFINDER_GOAL_SELECTOR_ITEM_A;
	private static boolean PATHFINDER_GOAL_SELECTOR_ITEM_A_SUCCESSFUL;

	private static Field PATHFINDER_GOAL_NEAREST_ATTACKABLE_TARGET_D;
	private static boolean PATHFINDER_GOAL_NEAREST_ATTACKABLE_TARGET_D_SUCCESSFUL;

	static
	{
		try
		{
			PATHFINDER_GOAL_SELECTOR_ITEM_A = Class.forName(PathfinderGoalSelector.class.getName() + "$PathfinderGoalSelectorItem").getDeclaredField("a");
			PATHFINDER_GOAL_SELECTOR_ITEM_A.setAccessible(true);
			PATHFINDER_GOAL_SELECTOR_ITEM_A_SUCCESSFUL = true;
		}
		catch (ReflectiveOperationException e)
		{
			e.printStackTrace();
		}
		try
		{
			PATHFINDER_GOAL_SELECTOR_B = PathfinderGoalSelector.class.getDeclaredField("b");
			PATHFINDER_GOAL_SELECTOR_B.setAccessible(true);
			PATHFINDER_GOAL_SELECTOR_B_SUCCESSFUL = true;
		}
		catch (ReflectiveOperationException e)
		{
			e.printStackTrace();
		}
		try
		{
			PATHFINDER_GOAL_SELECTOR_C = PathfinderGoalSelector.class.getDeclaredField("c");
			PATHFINDER_GOAL_SELECTOR_C.setAccessible(true);
			PATHFINDER_GOAL_SELECTOR_C_SUCCESSFUL = true;
		}
		catch (ReflectiveOperationException e)
		{
			e.printStackTrace();
		}
		try
		{
			PATHFINDER_GOAL_NEAREST_ATTACKABLE_TARGET_D = PathfinderGoalNearestAttackableTarget.class.getDeclaredField("d");
			PATHFINDER_GOAL_NEAREST_ATTACKABLE_TARGET_D.setAccessible(true);
			PATHFINDER_GOAL_NEAREST_ATTACKABLE_TARGET_D_SUCCESSFUL = true;
		}
		catch (ReflectiveOperationException e)
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@EventHandler
	public void fixPathfinderGoalLeaks(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
			return;

		List<World> worldList = Bukkit.getWorlds();
		Set<World> worlds = new HashSet<>(worldList);

		// Sanity check
		if (worlds.size() != worldList.size())
			throw new RuntimeException("Error: Duplicated worlds?!?!");

		for (World world : worlds)
		{
			WorldServer worldServer = ((CraftWorld) world).getHandle();

			for (net.minecraft.server.v1_8_R3.Entity nmsentity : worldServer.entityList)
			{
				if (nmsentity instanceof EntityInsentient)
				{
					EntityInsentient ei = (EntityInsentient) nmsentity;
					if (PATHFINDER_GOAL_SELECTOR_ITEM_A_SUCCESSFUL)
					{
						if (PATHFINDER_GOAL_SELECTOR_B_SUCCESSFUL)
						{
							try
							{
								PathfinderGoalSelector targetSelector = ei.targetSelector;
								List<Object> list = (List<Object>) PATHFINDER_GOAL_SELECTOR_B.get(targetSelector);
								for (Object object : list)
								{
									try
									{
										PathfinderGoal goal = (PathfinderGoal) PATHFINDER_GOAL_SELECTOR_ITEM_A.get(object);
										if (goal instanceof PathfinderGoalNearestAttackableTarget && PATHFINDER_GOAL_NEAREST_ATTACKABLE_TARGET_D_SUCCESSFUL)
										{
											net.minecraft.server.v1_8_R3.Entity original = (net.minecraft.server.v1_8_R3.Entity) PATHFINDER_GOAL_NEAREST_ATTACKABLE_TARGET_D.get(goal);
											boolean shouldClear = false;

											if (original != null)
											{
												if (!original.valid)
													shouldClear = true;
											}

											if (shouldClear)
												PATHFINDER_GOAL_NEAREST_ATTACKABLE_TARGET_D.set(goal, null);
										}
									}
									catch (ReflectiveOperationException ex)
									{
										ex.printStackTrace();
									}
								}
							}
							catch (ReflectiveOperationException e)
							{
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}

	private static Object ENCHANTMENTMANAGER_D;
	private static Field ENCHANTMENT_MODIFIER_THORNS_A;
	private static Field ENCHANTMENT_MODIFIER_THORNS_B;
	private static boolean ENCHANTMENTMANAGER_D_SUCCESSFUL;

	private static Object ENCHANTMENTMANAGER_E;
	private static Field ENCHANTMENT_MODIFIER_ARTHROPODS_A;
	private static Field ENCHANTMENT_MODIFIER_ARTHROPODS_B;
	private static boolean ENCHANTMENTMANAGER_E_SUCCESSFUL;

	static
	{
		try
		{
			Field field = EnchantmentManager.class.getDeclaredField("d");
			field.setAccessible(true);
			ENCHANTMENTMANAGER_D = field.get(null);
			ENCHANTMENT_MODIFIER_THORNS_A = ENCHANTMENTMANAGER_D.getClass().getDeclaredField("a");
			ENCHANTMENT_MODIFIER_THORNS_A.setAccessible(true);
			ENCHANTMENT_MODIFIER_THORNS_B = ENCHANTMENTMANAGER_D.getClass().getDeclaredField("b");
			ENCHANTMENT_MODIFIER_THORNS_B.setAccessible(true);
			ENCHANTMENTMANAGER_D_SUCCESSFUL = true;
		}
		catch (ReflectiveOperationException e)
		{
			e.printStackTrace();
		}
		try
		{
			Field field = EnchantmentManager.class.getDeclaredField("e");
			field.setAccessible(true);
			ENCHANTMENTMANAGER_E = field.get(null);
			ENCHANTMENT_MODIFIER_ARTHROPODS_A = ENCHANTMENTMANAGER_E.getClass().getDeclaredField("a");
			ENCHANTMENT_MODIFIER_ARTHROPODS_A.setAccessible(true);
			ENCHANTMENT_MODIFIER_ARTHROPODS_B = ENCHANTMENTMANAGER_E.getClass().getDeclaredField("b");
			ENCHANTMENT_MODIFIER_ARTHROPODS_B.setAccessible(true);
			ENCHANTMENTMANAGER_E_SUCCESSFUL = true;
		}
		catch (ReflectiveOperationException e)
		{
			e.printStackTrace();
		}
	}

	@EventHandler
	public void fixEnchantmentManager(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
			return;

		if (ENCHANTMENTMANAGER_D_SUCCESSFUL)
		{
			try
			{
				net.minecraft.server.v1_8_R3.Entity a = (net.minecraft.server.v1_8_R3.Entity) ENCHANTMENT_MODIFIER_THORNS_A.get(ENCHANTMENTMANAGER_D);
				net.minecraft.server.v1_8_R3.Entity b = (net.minecraft.server.v1_8_R3.Entity) ENCHANTMENT_MODIFIER_THORNS_B.get(ENCHANTMENTMANAGER_D);

				if ((a != null && !a.valid) || (b != null && !b.valid))
				{
					ENCHANTMENT_MODIFIER_THORNS_A.set(ENCHANTMENTMANAGER_D, null);
					ENCHANTMENT_MODIFIER_THORNS_B.set(ENCHANTMENTMANAGER_D, null);
				}
			}
			catch (ReflectiveOperationException e)
			{
				e.printStackTrace();
			}
		}

		if (ENCHANTMENTMANAGER_E_SUCCESSFUL)
		{
			try
			{
				net.minecraft.server.v1_8_R3.Entity a = (net.minecraft.server.v1_8_R3.Entity) ENCHANTMENT_MODIFIER_ARTHROPODS_A.get(ENCHANTMENTMANAGER_E);
				net.minecraft.server.v1_8_R3.Entity b = (net.minecraft.server.v1_8_R3.Entity) ENCHANTMENT_MODIFIER_ARTHROPODS_B.get(ENCHANTMENTMANAGER_E);

				if ((a != null && !a.valid) || (b != null && !b.valid))
				{
					ENCHANTMENT_MODIFIER_ARTHROPODS_A.set(ENCHANTMENTMANAGER_E, null);
					ENCHANTMENT_MODIFIER_ARTHROPODS_B.set(ENCHANTMENTMANAGER_E, null);
				}
			}
			catch (ReflectiveOperationException e)
			{
				e.printStackTrace();
			}
		}
	}

	@EventHandler
	public void fixInventoryLeaks(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
			return;

		for (World world : Bukkit.getWorlds())
		{
			for (Object tileEntity : ((CraftWorld) world).getHandle().tileEntityList)
			{
				if (tileEntity instanceof IInventory)
				{
					Iterator<HumanEntity> entityIterator = ((IInventory) tileEntity).getViewers().iterator();

					while (entityIterator.hasNext())
					{
						HumanEntity entity = entityIterator.next();

						if (entity instanceof CraftPlayer && !((CraftPlayer) entity).isOnline())
						{
							entityIterator.remove();
						}
					}
				}
			}
		}

		CraftingManager.getInstance().lastCraftView = null;
		CraftingManager.getInstance().lastRecipe = null;
	}

	@EventHandler
	public void fixEntityTrackerLeak(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
			return;

		// NEED TO FIX STUCK NETWORKMANAGERS.....
		/*
		for (World world : Bukkit.getWorlds())
		{
			EntityTracker tracker = ((CraftWorld)world).getHandle().getTracker();
			
	        EntityTrackerEntry entitytrackerentry1 = (EntityTrackerEntry) tracker.trackedEntities.d(entity.getId());

	        if (entitytrackerentry1 != null) {
	            this.c.remove(entitytrackerentry1);
	            entitytrackerentry1.a();
	        }
		}		
        if (entity instanceof EntityPlayer) {
            EntityPlayer entityplayer = (EntityPlayer) entity;
            

            while (iterator.hasNext()) {
                EntityTrackerEntry entitytrackerentry = (EntityTrackerEntry) iterator.next();

                entitytrackerentry.a(entityplayer);
            }
        }

        EntityTrackerEntry entitytrackerentry1 = (EntityTrackerEntry) this.trackedEntities.d(entity.getId());

        if (entitytrackerentry1 != null) {
            this.c.remove(entitytrackerentry1);
            entitytrackerentry1.a();
        }
        */
	}
}
