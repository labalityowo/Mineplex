package nautilus.game.arcade.game.games.halloween2016.creatures;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.halloween.creatures.CreatureBase;
import nautilus.game.arcade.game.games.halloween.creatures.InterfaceMove;
import nautilus.game.arcade.game.games.halloween2016.Halloween2016;

public class MobPumpling extends CreatureBase<Skeleton> implements InterfaceMove
{
	private static double EXTRA_DAMAGE = 5;
	private static float SPEED = 1;
	
	public MobPumpling(Halloween2016 game, Location loc)
	{
		super(game, C.cYellow + "Pumpling", Skeleton.class, loc);
	}
	
	@Override
	public void SpawnCustom(Skeleton ent)
	{
		Host.Manager.GetCondition().Factory().Invisible("Cloak", ent, ent, 999999, 0, false, false, false);

		ent.getEquipment().setHelmet(new ItemStack(Material.PUMPKIN));

		ent.setMaxHealth(15);
		ent.setHealth(ent.getMaxHealth());

		UtilEnt.setTickWhenFarAway(ent, true);
	}

	@Override
	public void Damage(CustomDamageEvent event) 
	{
		if(GetEntity().equals(event.GetDamagerEntity(false)))
		{
			event.AddMod("Damage", EXTRA_DAMAGE);
		}
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
		UtilEnt.CreatureMove(GetEntity(), getClosestPlayer(), SPEED);
	}
	
	public Location getClosestPlayer()
	{
		Player p = null;
		double d = 0;
		for(Player pp : Host.GetPlayers(true))
		{
			double dd = pp.getLocation().distanceSquared(GetEntity().getLocation());
			if(p == null || dd < d)
			{
				d = dd;
				p = pp;
			}
		}
		return p.getLocation();
	}
	
	@Override
	public void remove()
	{
		UtilParticle.PlayParticleToAll(ParticleType.LARGE_SMOKE, GetEntity().getLocation(), null, 0.3f, 20, ViewDist.NORMAL);
		GetEntity().getWorld().playSound(GetEntity().getLocation(), Sound.SKELETON_DEATH, 1, 1);
		GetEntity().getWorld().playSound(GetEntity().getLocation(), Sound.FIZZ, 1, 1);
		super.remove();
	}

}
