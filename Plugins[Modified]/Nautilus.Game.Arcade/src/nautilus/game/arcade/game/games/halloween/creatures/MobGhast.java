package nautilus.game.arcade.game.games.halloween.creatures;

import mineplex.core.common.util.UtilMath;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.halloween.Halloween;

import org.bukkit.Location;
import org.bukkit.entity.Ghast;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class MobGhast extends CreatureBase<Ghast>
{
	public MobGhast(Halloween game, Location loc) 
	{
		super(game, null, Ghast.class, loc);
	}

	@Override
	public void SpawnCustom(Ghast ent) 
	{
		ent.setMaxHealth(55);
		ent.setHealth(55);
		
		ent.setCustomName("Ghast");
	}
	
	@Override
	public void Damage(CustomDamageEvent event) 
	{
		if (event.GetCause() == DamageCause.SUFFOCATION)
			event.SetCancelled("Suffocation Cancel");
	}

	@Override
	public void Target(EntityTargetEvent event)
	{
		
	}
	
	@Override
	public void Update(UpdateEvent event) 
	{
		if (event.getType() == UpdateType.SLOW)
			Teleport();
	}
	
	private void Teleport() 
	{
		Location loc = GetEntity().getLocation();
		loc.setY(30);
		loc.setX(0);
		loc.setZ(0);
		
		if (UtilMath.offset2d(GetEntity().getLocation(), loc) > 50 || GetEntity().getLocation().getY() > 80)
		{
			loc.setY(30 + 20 * Math.random());
			loc.setX(60 * Math.random() - 30);
			loc.setZ(60 * Math.random() - 30);
			GetEntity().teleport(loc);
		}
	}
}
