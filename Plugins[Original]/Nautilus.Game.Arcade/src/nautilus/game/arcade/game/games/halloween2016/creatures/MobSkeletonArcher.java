package nautilus.game.arcade.game.games.halloween2016.creatures;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import nautilus.game.arcade.game.games.halloween2016.Halloween2016;

public class MobSkeletonArcher extends CryptBreaker<Skeleton>
{
	
	private static final int CRYPT_DAMAGE = 3;
	private static final int CRYPT_DAMAGE_RATE = 20;
	private static final float SPEED = 1;
	private static final double HEALTH = 15;

	public MobSkeletonArcher(Halloween2016 game, Location loc)
	{
		super(game, C.cYellow + "Skeleton Archer", Skeleton.class, loc, CRYPT_DAMAGE, CRYPT_DAMAGE_RATE, SPEED);
		
		_extraDamage = 1;
		_customCryptRange = 3;
		
		_playerTargetBackRange = 2;
	}
	
	@Override
	public void SpawnCustom(Skeleton ent)
	{
		ent.getEquipment().setItemInHand(new ItemStack(Material.BOW));
		
		ent.setMaxHealth(HEALTH);
		ent.setHealth(HEALTH);
	}
	
	@Override
	public void attackCrypt()
	{
		if(!_crypt.tryDamage(GetEntity(), _cryptDamage, _cryptDamageRate)) return;
		
		Location door = getClosestDoor();
		Vector diff = door.toVector().subtract(GetEntity().getLocation().toVector());
		diff.setY(diff.getY() + 0.5);
		Arrow a = GetEntity().launchProjectile(Arrow.class, diff);
		((CraftArrow)a).getHandle().noclip = true;
	}

}
