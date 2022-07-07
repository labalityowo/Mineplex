package nautilus.game.arcade.game.games.halloween2016.creatures;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityTargetEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.game.games.halloween.Halloween;
import nautilus.game.arcade.game.games.halloween.creatures.CreatureBase;
import nautilus.game.arcade.game.games.halloween.creatures.InterfaceMove;

public class MobMiniZombie extends CreatureBase<Zombie> implements InterfaceMove
{
	
	private static final double HEALTH = 5;

	public MobMiniZombie(Halloween game, Location loc)
	{
		super(game, C.cYellow + "Baby Zombie", Zombie.class, loc);
	}

	@Override
	public void SpawnCustom(Zombie ent)
	{
		ent.setBaby(true);
		
		ent.setMaxHealth(HEALTH);
		ent.setHealth(HEALTH);
	}

	@Override
	public void Update(UpdateEvent event)
	{
		
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
	public void Move() 
	{		
		CreatureMove(GetEntity(), 1f);
	}
	
	@Override
	public void remove()
	{
		super.remove();
		UtilParticle.PlayParticleToAll(ParticleType.LARGE_SMOKE, GetEntity().getLocation(), null, 0.1f, 10, ViewDist.NORMAL);
		GetEntity().getWorld().playSound(GetEntity().getLocation(), Sound.FIZZ, 0.8f, 0.3f);
	}

}
