package nautilus.game.arcade.game.games.halloween.creatures;

import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.halloween.Halloween;

import org.bukkit.Location;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityTargetEvent;

public class MobZombie extends CreatureBase<Zombie> implements InterfaceMove
{
	public MobZombie(Halloween game, Location loc) 
	{
		super(game, null, Zombie.class, loc);
	}

	@Override
	public void SpawnCustom(Zombie ent) 
	{
		ent.setCustomName("Zombie");
	}

	@Override
	public void Damage(CustomDamageEvent event) 
	{

	}

	@Override
	public void Update(UpdateEvent event) 
	{
		if (event.getType() == UpdateType.SLOW)
			Speed();
	}

	public void Move() 
	{		
		CreatureMove(GetEntity());
	}

	@Override
	public void Target(EntityTargetEvent event)
	{

	}
	
	public void Speed()
	{
		if (GetEntity().getTicksLived() > 2400)
			Host.Manager.GetCondition().Factory().Speed("Speed", GetEntity(), GetEntity(), 10, 3, false, false, false);
		else if (GetEntity().getTicksLived() > 1800)
			Host.Manager.GetCondition().Factory().Speed("Speed", GetEntity(), GetEntity(), 10, 2, false, false, false);
		else if (GetEntity().getTicksLived() > 1200)
			Host.Manager.GetCondition().Factory().Speed("Speed", GetEntity(), GetEntity(), 10, 1, false, false, false);
		else if (GetEntity().getTicksLived() > 600)
			Host.Manager.GetCondition().Factory().Speed("Speed", GetEntity(), GetEntity(), 10, 0, false, false, false);
	}
}
