package nautilus.game.arcade.game.games.halloween.creatures;

import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.halloween.Halloween;

import org.bukkit.Location;
import org.bukkit.entity.Creeper;
import org.bukkit.event.entity.EntityTargetEvent;

public class MobCreeper extends CreatureBase<Creeper> implements InterfaceMove
{
	public MobCreeper(Halloween game, Location loc) 
	{
		super(game, null, Creeper.class, loc);
	}
	
	@Override
	public void SpawnCustom(Creeper ent) 
	{
		ent.setCustomName("Creeper");
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

	@Override
	public void Move() 
	{		
		CreatureMove(GetEntity());
	}
}
