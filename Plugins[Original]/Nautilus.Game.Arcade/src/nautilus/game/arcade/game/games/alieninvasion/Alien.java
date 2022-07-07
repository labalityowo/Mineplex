package nautilus.game.arcade.game.games.alieninvasion;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.utils.UtilVariant;
import nautilus.game.arcade.ArcadeManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class Alien
{

	private static final int RADIUS = 3;
	private static final ItemStack BLASTER = new ItemStack(Material.DIAMOND_BARDING);
	private static final ItemStack HELMET = new ItemStack(Material.GLASS);

	private final ArcadeManager _manager;
	private ArmorStand _stand;
	private Skeleton _skeleton;
	private Location _center;

	private double _theta;

	public Alien(ArcadeManager manager, Location location)
	{
		_manager = manager;
		_stand = location.getWorld().spawn(location, ArmorStand.class);
		_skeleton = UtilVariant.spawnWitherSkeleton(location);
		_center = location;

		_stand.setSmall(true);
		_stand.setVisible(false);
		_stand.setGravity(false);
		_stand.setPassenger(_skeleton);
		_stand.setRemoveWhenFarAway(false);

		_skeleton.setMaxHealth(3);
		_skeleton.setRemoveWhenFarAway(false);

		EntityEquipment equipment = _skeleton.getEquipment();
		equipment.setItemInHand(BLASTER);
		equipment.setHelmet(HELMET);

		UtilEnt.silence(_stand, true);
		UtilEnt.vegetate(_skeleton);

		_theta = Math.random();
	}

	public void update()
	{
		double x = RADIUS * Math.cos(_theta);
		double z = RADIUS * Math.sin(_theta);

		_center.add(x, 0, z);
		((CraftLivingEntity) _stand).getHandle().setPosition(_center.getX(), _center.getY(), _center.getZ());
		_center.subtract(x, 0, z);

		_theta += Math.PI / 30;

		if (Math.random() < 0.9)
		{
			return;
		}

		Vector last = null;

		for (Player player : UtilPlayer.getNearby(_skeleton.getLocation(), 20))
		{
			last = UtilAlg.getTrajectory(_skeleton.getEyeLocation(), player.getEyeLocation());

			new PhaserProjectile(_manager, _skeleton, last);
		}

		if (last != null)
		{
			UtilEnt.CreatureLook(_skeleton, last);
		}
	}

	public boolean isValid()
	{
		boolean remove = !_stand.isValid() || !_skeleton.isValid();

		if (remove)
		{
			_stand.remove();
			_skeleton.remove();
		}

		return !remove;
	}
}
