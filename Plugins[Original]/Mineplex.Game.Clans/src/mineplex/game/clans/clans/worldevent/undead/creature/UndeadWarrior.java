package mineplex.game.clans.clans.worldevent.undead.creature;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.worldevent.api.EventCreature;
import mineplex.game.clans.clans.worldevent.api.WorldEvent;

public class UndeadWarrior extends EventCreature<Zombie>
{
	public UndeadWarrior(WorldEvent event, Location spawnLocation)
	{
		super(event, spawnLocation, "Undead Warrior", true, 50, 30, true, Zombie.class);
		
		spawnEntity();
	}

	@Override
	protected void spawnCustom()
	{
		Zombie entity = getEntity();
		EntityEquipment eq = entity.getEquipment();
		eq.setHelmet(new ItemStack(Material.IRON_HELMET));
		eq.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
		eq.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
		eq.setBoots(new ItemStack(Material.IRON_BOOTS));
		eq.setItemInHand(new ItemStack(Material.STONE_SWORD));
		eq.setHelmetDropChance(0.f);
		eq.setChestplateDropChance(0.f);
		eq.setLeggingsDropChance(0.f);
		eq.setBootsDropChance(0.f);
		eq.setItemInHandDropChance(0.f);
		entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 0));
	}

	@Override
	public void dieCustom()
	{
		if (Math.random() > 0.97)
			getEntity().getWorld().dropItem(getEntity().getLocation(), new org.bukkit.inventory.ItemStack(Material.IRON_HELMET));

		if (Math.random() > 0.97)
			getEntity().getWorld().dropItem(getEntity().getLocation(), new org.bukkit.inventory.ItemStack(Material.IRON_CHESTPLATE));

		if (Math.random() > 0.97)
			getEntity().getWorld().dropItem(getEntity().getLocation(), new org.bukkit.inventory.ItemStack(Material.IRON_LEGGINGS));

		if (Math.random() > 0.97)
			getEntity().getWorld().dropItem(getEntity().getLocation(), new org.bukkit.inventory.ItemStack(Material.IRON_BOOTS));

		for (int i=0 ; i<UtilMath.r(5) + 1 ; i++)
		{
			getEntity().getWorld().dropItem(getEntity().getLocation(), new org.bukkit.inventory.ItemStack(Material.EMERALD));
		}
	}

	@EventHandler
	public void leap(UpdateEvent event)
	{
		if (getEntity() == null)
			return;

		if (event.getType() != UpdateType.FAST)
			return;

		if (Math.random() < 0.9)
			return;

		Zombie zombie = getEntity();

		if (zombie.getTarget() == null)
			return;

		double dist = UtilMath.offset(zombie.getTarget(), zombie);

		if (dist <= 3 || dist > 16)
			return;


		double power = 0.8 + (1.2 * ((dist-3)/13d));

		//Leap
		UtilAction.velocity(zombie, UtilAlg.getTrajectory(zombie, zombie.getTarget()),
				power, false, 0, 0.2, 1, true);

		//Effect
		zombie.getWorld().playSound(zombie.getLocation(), Sound.ZOMBIE_HURT, 1f, 2f);
	}
	
	@EventHandler
	public void onTarget(EntityTargetLivingEntityEvent event)
	{
		if (getEntity().equals(event.getEntity()))
		{
			if (!(event.getTarget() instanceof Player))
			{
				event.setCancelled(true);
			}
		}
	}
}