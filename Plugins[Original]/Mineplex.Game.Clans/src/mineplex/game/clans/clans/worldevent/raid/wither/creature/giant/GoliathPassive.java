package mineplex.game.clans.clans.worldevent.raid.wither.creature.giant;

import org.bukkit.entity.Giant;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.worldevent.api.BossPassive;
import mineplex.game.clans.clans.worldevent.raid.wither.WitherRaid;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class GoliathPassive extends BossPassive<Goliath, Giant>
{
	public GoliathPassive(Goliath creature)
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
	public void tick()
	{
		for (Player player : ((WitherRaid)getBoss().getEvent()).getPlayers())
		{
			if (UtilPlayer.isSpectator(player) || player.isDead() || !player.isValid())
			{
				continue;
			}
			if (UtilMath.offset(getEntity(), player) <= 7)
			{
				getBoss().getEvent().getDamageManager().NewDamageEvent(player, getEntity(), null, DamageCause.ENTITY_ATTACK, 100, false, true, true, getEntity().getName(), "Smash");
			}
		}
	}
	
	@EventHandler
	public void onDamage(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity() != null)
		{
			if (event.GetDamageeEntity().getEntityId() == getEntity().getEntityId())
			{
				event.SetCancelled("Giant Invulnerability");
			}
		}
	}
}