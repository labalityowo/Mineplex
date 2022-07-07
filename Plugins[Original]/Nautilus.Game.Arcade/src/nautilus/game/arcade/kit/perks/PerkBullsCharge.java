package nautilus.game.arcade.kit.perks;

import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkBullsCharge extends Perk
{
	public PerkBullsCharge() 
	{
		super("Bulls Charge", new String[] 
				{ 
				C.cYellow + "Right-Click" + C.cGray + " with Axe to use " + C.cGreen + "Bulls Charge"
				});
	}
	
	@EventHandler
	public void skill(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		if (UtilBlock.usable(event.getClickedBlock()))
			return;
		
		if (!UtilGear.isAxe(event.getPlayer().getItemInHand()))
			return;
		
		Player player = event.getPlayer();
		
		if (!Kit.HasKit(player))
			return;
		
		if (!Recharge.Instance.use(player, GetName(), 12000, true, true))
			return;
		
		//Action
		Manager.GetCondition().Factory().Speed(GetName(), player, player, 6, 1, false, true, true);

		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.ENDERMAN_SCREAM, 1.5f, 0f);
		player.getWorld().playEffect(player.getLocation(), Effect.STEP_SOUND, 49);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void damage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;
		
		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
			return;

		Player damager = event.GetDamagerPlayer(true);
		LivingEntity damagee = event.GetDamageeEntity();

		if (damager == null || damagee == null)
			return;
		
		//Isn't using Bulls Charge
		if (!Manager.GetCondition().HasCondition(damager, ConditionType.SPEED, GetName()))
			return;

		//Condition
		Manager.GetCondition().Factory().Slow(GetName(), damagee, damager, 4, 1, false, true, true, true);
		Manager.GetCondition().EndCondition(damager, ConditionType.SPEED, GetName());

		//Damage
		event.SetKnockback(false);
		
		//Effect
		damager.getWorld().playSound(damager.getLocation(), Sound.ENDERMAN_SCREAM, 1.5f, 0f);
		damager.getWorld().playSound(damager.getLocation(), Sound.ZOMBIE_METAL, 1.5f, 0.5f);

		//Inform
		UtilPlayer.message(damagee, F.main("Game", F.name(damager.getName()) + " hit you with " + F.skill(GetName()) + "."));
		UtilPlayer.message(damager, F.main("Game", "You hit " + F.name(UtilEnt.getName(damagee)) +" with " + F.skill(GetName()) + "."));
	}
	
	@EventHandler
	public void particle(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (Manager.GetGame() == null)
			return;
		
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!Kit.HasKit(player))
				continue;
			
			if (player.hasPotionEffect(PotionEffectType.SPEED))
				UtilParticle.PlayParticle(ParticleType.CRIT, player.getLocation(), 
					(float)(Math.random() - 0.5), 0.2f + (float)(Math.random() * 1), (float)(Math.random() - 0.5), 0, 2,
					ViewDist.LONG, UtilServer.getPlayers());
		}
	}
}
