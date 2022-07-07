package mineplex.core.gadget.gadgets.morph.managers;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

public class GoldPotStands
{

	private ArmorStand _helmet, _armsA, _armsB, _armsC;
	private Block _block;

	public void setBlock(Block block)
	{
		_block = block;
	}

	public void createStands()
	{
		if (_block == null)
		{
			return;
		}
		Location loc = _block.getLocation().clone().add(0.5, 0, 0.5);

		// Spawns main armorstand
		Location asHelmetGoldLoc = loc.clone().subtract(0, 1, 0);
		ArmorStand asHelmetGold = loc.getWorld().spawn(asHelmetGoldLoc, ArmorStand.class);
		asHelmetGold.setVisible(false);
		asHelmetGold.setGravity(false);
		asHelmetGold.setHelmet(new ItemStack(Material.GOLD_BLOCK));

		// Spawns second armorstand
		Location asArmsGoldALoc = asHelmetGoldLoc.clone();
		ArmorStand asArmsGoldA = loc.getWorld().spawn(asArmsGoldALoc, ArmorStand.class);
		asArmsGoldA.setVisible(false);
		asArmsGoldA.setGravity(false);
		asArmsGoldA.setItemInHand(new ItemStack(Material.GOLD_BLOCK));
		double asArmsGoldAX = Math.toRadians(158), asArmsGoldAY = Math.toRadians(75);
		EulerAngle asArmsGoldAEuler = new EulerAngle(asArmsGoldAX, asArmsGoldAY, 0);
		asArmsGoldA.setRightArmPose(asArmsGoldAEuler);

		// Spawns third armorstand
		Location asArmsGoldBLoc = asHelmetGoldLoc.clone();
		ArmorStand asArmsGoldB = loc.getWorld().spawn(asArmsGoldBLoc, ArmorStand.class);
		asArmsGoldB.setVisible(false);
		asArmsGoldB.setGravity(false);
		asArmsGoldB.setItemInHand(new ItemStack(Material.GOLD_BLOCK));
		double asArmsGoldBX = Math.toRadians(202), asArmsGoldBY = Math.toRadians(245);
		EulerAngle asArmsGoldBEuler = new EulerAngle(asArmsGoldBX, asArmsGoldBY, 0);
		asArmsGoldB.setRightArmPose(asArmsGoldBEuler);

		// Spawns fourth armorstand
		Location asArmsGoldCLoc = loc.clone().add(0.4, 0.1, 0.1);
		ArmorStand asArmsGoldC = loc.getWorld().spawn(asArmsGoldCLoc, ArmorStand.class);
		asArmsGoldC.setVisible(false);
		asArmsGoldC.setGravity(false);
		asArmsGoldC.setSmall(true);
		asArmsGoldC.setItemInHand(new ItemStack(Material.GOLD_BLOCK));
		double asArmsGoldCX = Math.toRadians(191), asArmsGoldCY = Math.toRadians(245);
		EulerAngle asArmsGoldCEuler = new EulerAngle(asArmsGoldCX, asArmsGoldCY, 0);
		asArmsGoldC.setRightArmPose(asArmsGoldCEuler);

		_helmet = asHelmetGold;
		_armsA = asArmsGoldA;
		_armsB = asArmsGoldB;
		_armsC = asArmsGoldC;
	}

	public void removeStands()
	{
		if (_helmet != null)
		{
			_helmet.remove();
			_helmet = null;
		}
		if (_armsA != null)
		{
			_armsA.remove();
			_armsA = null;
		}
		if (_armsB != null)
		{
			_armsB.remove();
			_armsB = null;
		}
		if (_armsC != null)
		{
			_armsC.remove();
			_armsC = null;
		}
		_block = null;
	}

}