package mineplex.game.clans.clans.worldevent.raid.wither.creature.corpse;

import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;

import mineplex.game.clans.clans.worldevent.api.BossPassive;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class CorpsePassive extends BossPassive<ReanimatedCorpse, Zombie>
{
	public CorpsePassive(ReanimatedCorpse creature)
	{
		super(creature);
	}
	
	@Override
	public int getCooldown()
	{
		return 0;
	}
	
	@Override
	public boolean isProgressing()
	{
		return false;
	}

	@Override
	public void tick() {}
	
	@EventHandler
	public void onDamage(CustomDamageEvent event)
	{
		if (event.GetDamagerEntity(false) == null)
		{
			return;
		}
		if (event.GetDamagerEntity(false).getEntityId() == getEntity().getEntityId())
		{
			event.AddMod("Corpse Attack", 1 - event.GetDamage());
		}
	}
}