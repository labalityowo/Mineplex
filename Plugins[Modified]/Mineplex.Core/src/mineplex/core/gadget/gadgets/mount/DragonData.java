package mineplex.core.gadget.gadgets.mount;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEnderDragon;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilServer;

public class DragonData extends MountData
{

	public EnderDragon Dragon;

	public Location Location = null;

	public Vector Velocity = new Vector(0, 0, 0);

	public Chicken Chicken;

	public DragonData(DragonMount dragonMount, Player rider)
	{
		super(rider);

		Velocity = rider.getLocation().getDirection().setY(0).normalize();

		Location = rider.getLocation();

		//Spawn Dragon
		Dragon = rider.getWorld().spawn(rider.getLocation(), EnderDragon.class);
		UtilEnt.vegetate(Dragon);
		UtilEnt.ghost(Dragon, true, false);

		rider.getWorld().playSound(rider.getLocation(), Sound.ENDERDRAGON_GROWL, 20f, 1f);

		Chicken = rider.getWorld().spawn(rider.getLocation(), Chicken.class);
		Chicken.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
		Dragon.setPassenger(Chicken);

		Chicken.setPassenger(getOwner());
		UtilServer.runSyncLater(() -> Chicken.setPassenger(getOwner()), 10);
	}

	public void Move()
	{
		getOwner().eject();
		((CraftEnderDragon) Dragon).getHandle().setTargetBlock(GetTarget().getBlockX(), GetTarget().getBlockY(), GetTarget().getBlockZ());
	}

	public Location GetTarget()
	{
		return getOwner().getLocation().add(getOwner().getLocation().getDirection().multiply(40));
	}

	@Override
	public List<Entity> getEntityParts()
	{
		return Arrays.asList(Dragon, Chicken);
	}
}
