package mineplex.gemhunters.worldevent.ufo;

import mineplex.core.Managers;
import mineplex.core.common.block.schematic.Schematic;
import mineplex.core.common.block.schematic.SchematicData;
import mineplex.core.common.block.schematic.UtilSchematic;
import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilTime;
import mineplex.core.utils.UtilVariant;
import mineplex.gemhunters.economy.EconomyModule;
import mineplex.gemhunters.loot.LootModule;
import mineplex.gemhunters.loot.rewards.LootChestReward;
import mineplex.gemhunters.worldevent.WorldEvent;
import mineplex.gemhunters.worldevent.WorldEventState;
import mineplex.gemhunters.worldevent.WorldEventType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class UFOWorldEvent extends WorldEvent
{

	private static final String SCHEMATIC_PATH = ".." + File.separator + ".." + File.separator + "update" + File.separator + "files" + File.separator + "UFO.schematic";
	private static final long MAX_TIME = TimeUnit.MINUTES.toMillis(10);
	private static final ItemStack HELMET = new ItemStack(Material.GLASS);
	private static final ItemStack SWORD = new ItemStack(Material.STONE_SWORD);
	private static final ItemStack SWORD_LEADER = new ItemStack(Material.IRON_SWORD);
	private static final long CASH_OUT_DELAY = TimeUnit.MINUTES.toMillis(10);

	private final EconomyModule _economy;
	private final LootModule _loot;

	private Skeleton _leader;
	private Set<Skeleton> _skeletons;
	private Set<Block> _ufoBlocks;

	public UFOWorldEvent()
	{
		super(WorldEventType.UFO);

		_economy = Managers.require(EconomyModule.class);
		_loot = Managers.require(LootModule.class);

		_skeletons = new HashSet<>();
		_ufoBlocks = new HashSet<>();
	}

	@Override
	public void onStart()
	{
		Location location = UtilAlg.Random(_worldData.getCustomLocation("NETHER_PORTAL")).clone().subtract(5, -10, 5);
		Schematic schematic;

		try
		{
			schematic = UtilSchematic.loadSchematic(new File(SCHEMATIC_PATH));
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return;
		}

		SchematicData data = schematic.paste(location, false, false, false);

		for (BlockVector vector : data.getBlocks())
		{
			Location block = location.add(vector);

			_ufoBlocks.add(block.getBlock());

			location.subtract(vector);
		}

		_leader = UtilVariant.spawnWitherSkeleton(location);
		_leader.setMaxHealth(200);
		_leader.setHealth(_leader.getMaxHealth());
		_leader.getEquipment().setItemInHand(SWORD_LEADER);
		_leader.setCustomName(C.cDGreenB + "Alien Leader");
		prepareSkeleton(_leader);

		for (int i = 0; i < 10; i++)
		{
			Skeleton skeleton = _leader.getWorld().spawn(location, Skeleton.class);
			skeleton.getEquipment().setItemInHand(SWORD);
			skeleton.setCustomName(C.cGreenB + "Alien");
			prepareSkeleton(skeleton);
			_skeletons.add(skeleton);
		}

		setEventState(WorldEventState.LIVE);
	}

	private void prepareSkeleton(Skeleton skeleton)
	{
		skeleton.getEquipment().setHelmet(HELMET);
		skeleton.setRemoveWhenFarAway(false);
		skeleton.setCustomNameVisible(true);
	}

	@EventHandler
	public void entityDeath(EntityDeathEvent event)
	{
		LivingEntity entity = event.getEntity();

		if (_skeletons.remove(entity))
		{
			event.getDrops().clear();
			event.setDroppedExp(0);

			Player killer = entity.getKiller();

			if (killer != null)
			{
				_economy.addToStore(killer, "Killing an Alien", 10);
			}
		}

		if (_leader != null && _leader.equals(entity))
		{
			Player killer = _leader.getKiller();

			if (killer != null)
			{
				ItemStack itemStack = SkinData.OMEGA_CHEST.getSkull(C.cAqua + "Omega Chest", new ArrayList<>());
				LootChestReward reward = new LootChestReward(CASH_OUT_DELAY, itemStack, "Omega", 1);
				_leader.getWorld().dropItemNaturally(_leader.getEyeLocation(), itemStack);
				_loot.addItemReward(reward);
				_economy.addToStore(killer, "Killing The Alien Leader", 1000);
			}
		}
	}

	@EventHandler
	public void entityDamage(EntityCombustEvent event)
	{
		Entity entity = event.getEntity();

		if (entity.equals(_leader) || _skeletons.contains(entity))
		{
			event.setCancelled(true);
		}
	}

	@Override
	public boolean checkToEnd()
	{
		return _leader.isDead() || UtilTime.elapsed(_start, MAX_TIME);
	}

	@Override
	public void onEnd()
	{
		_leader.remove();

		for (Skeleton skeleton : _skeletons)
		{
			skeleton.remove();
		}

		_skeletons.clear();

		for (Block block : _ufoBlocks)
		{
			block.setType(Material.AIR);
		}

		_ufoBlocks.clear();
	}

	@Override
	public Location[] getEventLocations()
	{
		return new Location[]{_leader.getLocation()};
	}

	@Override
	public double getProgress()
	{
		return _leader.getHealth() / _leader.getMaxHealth();
	}
}
