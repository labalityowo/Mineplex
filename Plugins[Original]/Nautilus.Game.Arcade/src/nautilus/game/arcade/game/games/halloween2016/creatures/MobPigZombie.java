package nautilus.game.arcade.game.games.halloween2016.creatures;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.disguise.disguises.DisguisePigZombie;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.halloween2016.Halloween2016;

public class MobPigZombie extends CryptBreaker<Zombie>
{
	
	private static float SPEED = 1;
	private static int CRYPT_DAMAGE = 3;
	private static int CRYPT_RATE = 20;
	
	private static int FIRE_TICKS = 60;

	public MobPigZombie(Halloween2016 game, Location loc)
	{
		super(game, C.cYellow + "Pigman Warrior", Zombie.class, loc, CRYPT_DAMAGE, CRYPT_RATE, SPEED);
		
		_extraDamage = 5;
	}

	@Override
	public void SpawnCustom(Zombie ent)
	{
		ent.getEquipment().setItemInHand(new ItemStack(Material.GOLD_SWORD));
		
		DisguisePigZombie disg = new DisguisePigZombie(ent);
		Host.getArcadeManager().GetDisguise().disguise(disg);
	}
	
	@Override
	public void Update(UpdateEvent event)
	{
		super.Update(event);
		if(event.getType() == UpdateType.TICK)
		{
			GetEntity().setFireTicks(5);
		}
	}
	
	@Override
	public void Damage(CustomDamageEvent event) 
	{
		if(event.GetDamageeEntity() instanceof Player)
		{
			if(GetEntity().equals(event.GetDamagerEntity(false)))
			{
				event.GetDamageeEntity().setFireTicks(FIRE_TICKS);
			}
		}
		
		else if(GetEntity().equals(event.GetDamageeEntity()))
		{
			if(event.GetCause() == DamageCause.FIRE || event.GetCause() == DamageCause.FIRE_TICK)
			{
				event.SetCancelled("Cancel Fire");
			}
		}
	}

}
