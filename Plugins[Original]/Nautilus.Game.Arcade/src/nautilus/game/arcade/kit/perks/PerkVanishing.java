package nautilus.game.arcade.kit.perks;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.kit.Perk;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class PerkVanishing extends Perk
{
	public PerkVanishing()
	{
		super("Vanishing Act", new String[]
				{
				"Become invisible for 1.2 seconds each kill.",
				"Attacking with melee removes invisibility."
				});
	}

	@EventHandler
	public void kill(CombatDeathEvent event)
	{
		if (!Manager.GetGame().IsLive())
			return;
		
//		//If it's an arrow kill (OITQ)
//		if (!event.GetLog().GetKiller().GetReason().toLowerCase().contains("instagib"))
//			return;
		
		if (event.GetLog().GetKiller() == null || !event.GetLog().GetKiller().IsPlayer())
			return;
		
		Player killer = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());
		
		if (killer == null)
			return;
		
		if (!Manager.IsAlive(killer))
			return;
		
		if (!Kit.HasKit(killer))
			return;
		
		Manager.GetCondition().Factory().Cloak("Vanishing Act", killer, null, 1.2, false, true);
		
		UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, killer.getLocation().add(0, 1, 0), 0, 0, 0, 0, 1, ViewDist.LONG, UtilServer.getPlayers());
		
		killer.getWorld().playSound(killer.getLocation(), Sound.FIZZ, 1f, 2f);
	}
	
	@EventHandler
	public void remove(CustomDamageEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (!Manager.GetGame().IsLive())
			return;
		
		//Arrow damage, ignore it!
		if (event.GetDamage() > 10)
			return;
		
		Player damager = event.GetDamagerPlayer(true);
		
		if (damager == null)
			return;
		
		if (!Manager.IsAlive(damager))
			return;
		
		if (!Kit.HasKit(damager))
			return;
		
		Manager.GetCondition().EndCondition(damager, ConditionType.CLOAK, null);
	}
	
//	@EventHandler
//	public void remove(UpdateEvent event)
//	{
//		if (event.getType() != UpdateType.TICK)
//			return;
//		
//		if (!Manager.GetGame().IsLive())
//			return;
//		
//		for (Player player : Manager.GetGame().GetPlayers(true))
//		{
//			if (!Kit.HasKit(player))
//				continue;
//			
//			if (!UtilPlayer.isChargingBow(player))
//				continue;
//			
//			if (Manager.GetCondition().IsCloaked(player))
//				Manager.GetCondition().Clean(player);
//		}
//	}
}
