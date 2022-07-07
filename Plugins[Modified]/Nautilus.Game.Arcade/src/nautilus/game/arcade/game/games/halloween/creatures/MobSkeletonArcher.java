package nautilus.game.arcade.game.games.halloween.creatures;

import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.halloween.Halloween;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;

public class MobSkeletonArcher extends CreatureBase<Skeleton> implements InterfaceMove
{
	public MobSkeletonArcher(Halloween game, Location loc) 
	{
		super(game, null, Skeleton.class, loc);
	}
	
	@Override
	public void SpawnCustom(Skeleton ent) 
	{
		ent.getEquipment().setItemInHand(new ItemStack(Material.BOW));
		ent.setCustomName("Skeleton Archer");
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
