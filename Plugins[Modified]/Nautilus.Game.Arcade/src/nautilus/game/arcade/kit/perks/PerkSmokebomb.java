package nautilus.game.arcade.kit.perks;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.minecraft.game.core.condition.Condition;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.kit.Perk;

import java.util.*;

public class PerkSmokebomb extends Perk
{
	private final Set<Material> activatorTypes;
	private final int effectDuration;
	private final boolean itemConsumed;

	public PerkSmokebomb(Material activatorType, int effectDuration, boolean itemConsumed)
	{
		this(activatorType, effectDuration, itemConsumed, C.cYellow + "Right-Click" + C.cGray + " to " + C.cGreen + "Smoke Bomb");
	}

	public PerkSmokebomb(Material activatorType, int effectDuration, boolean itemConsumed, String... description)
	{
		this(EnumSet.of(activatorType), effectDuration, itemConsumed, description);
	}

	public PerkSmokebomb(Set<Material> activatorTypes, int effectDuration, boolean itemConsumed, String... description)
	{
		super("Smoke Bomb", description);

		this.activatorTypes = activatorTypes;
		this.effectDuration = effectDuration;
		this.itemConsumed = itemConsumed;
	}

	public Set<Material> getActivatorTypes()
	{
		return activatorTypes;
	}

	public int getEffectDuration()
	{
		return effectDuration;
	}

	public boolean isItemConsumed()
	{
		return itemConsumed;
	}

	@EventHandler
	public void Use(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;

		if (!getActivatorTypes().contains(event.getPlayer().getItemInHand().getType()))
			return;

		event.setCancelled(true);

		if (isItemConsumed())
		{
			if (player.getItemInHand().getAmount() > 1)
				player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
			else
				player.setItemInHand(null);
		}
		else
		{
			if (!Recharge.Instance.use(player, GetName(), GetName(), 20000, true, true))
				return;
		}

		//Inform
		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));

		//Action
		Manager.GetCondition().Factory().Cloak(GetName(), player, player, 1.2, false, false);
		//Manager.GetCondition().Factory().Vulnerable(getName(), player, player, 6, 3, false, false, true);

		//Blind
		for (Entity other : player.getNearbyEntities(5, 5, 5))
		{
			if (other.equals(player) || !(other instanceof LivingEntity))
				continue;
			
			if (other instanceof Player)
				if (Manager.isSpectator((Player)other))
					continue;

			LivingEntity living = (LivingEntity) other;
			
			Manager.GetCondition().Factory().Blind(GetName() + " Effect", living, player, getEffectDuration(), 0, false, false, true);
			//Manager.GetCondition().Factory().Slow(getName() + " Effect", living, player, getEffectDuration(), 0, false, false, true, false);
		}

		//Effects
		player.getWorld().playSound(player.getLocation(), Sound.FIZZ, 2f, 0.5f);
		UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, player.getLocation(), 0f, 0f, 0f, 0, 1,
				ViewDist.MAX, UtilServer.getPlayers());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void EndDamager(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		Player damager = event.GetDamagerPlayer(true);
		if (damager == null) return;

		if (!Kit.HasKit(damager))
			return;

		//End
		Manager.GetCondition().EndCondition(damager, null, GetName());
	}

	@EventHandler
	public void Smoke(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		for (Player cur : UtilServer.getPlayers())
		{
			if (!Kit.HasKit(cur))
				return;

			Condition cond = Manager.GetCondition().GetActiveCondition(cur, ConditionType.CLOAK);
			if (cond == null) continue;

			if (!cond.GetReason().equals(GetName()))
				continue;

			//Smoke
			cur.getWorld().playEffect(cur.getLocation(), Effect.SMOKE, 4);
		}
	}

	@EventHandler
	public void Reset(PlayerDeathEvent event)
	{
		//Remove Condition
		Manager.GetCondition().EndCondition(event.getEntity(), null, GetName());
	}
}
