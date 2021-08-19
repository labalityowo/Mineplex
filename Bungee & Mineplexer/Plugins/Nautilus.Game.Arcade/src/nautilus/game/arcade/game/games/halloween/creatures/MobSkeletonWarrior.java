package nautilus.game.arcade.game.games.halloween.creatures;

import mineplex.core.disguise.disguises.DisguiseSkeleton;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.halloween.Halloween;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;

public class MobSkeletonWarrior extends CreatureBase<Zombie> implements InterfaceMove
{
	public MobSkeletonWarrior(Halloween game, Location loc) 
	{
		super(game, null, Zombie.class, loc);
	}
	
	@Override
	public void SpawnCustom(Zombie ent) 
	{
		DisguiseSkeleton spider = new DisguiseSkeleton(ent);
		Host.Manager.GetDisguise().disguise(spider);
		ent.setCustomName("Skeleton Warrior");
		ent.getEquipment().setItemInHand(new ItemStack(Material.WOOD_HOE));
		
		Host.Manager.GetCondition().Factory().Speed("Speed", ent, ent, 99999, 0, false, false, false);
	}

	@Override
	public void Damage(CustomDamageEvent event) 
	{
		
	}
	
	@Override
	public void Target(EntityTargetEvent event)
	{
		
	}
	
	@Override
	public void Update(UpdateEvent event) 
	{

	}

	public void Move() 
	{		
		CreatureMove(GetEntity());
	}
}
