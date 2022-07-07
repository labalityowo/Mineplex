package nautilus.game.arcade.game.games.smash.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.kit.Perk;

public class PerkSmashStats extends Perk
{
	private double _damage;
	private double _knockbackTaken;
	private double _regen;
	private double _armor;

	public PerkSmashStats()
	{
		super("Smash Stats");
	}

	@Override
	public void setupValues()
	{
		_damage = getPerkDouble("Damage", _damage);
		_knockbackTaken = getPerkPercentage("Knockback Taken", _knockbackTaken);
		_regen = getPerkDouble("Regeneration", _regen);
		_armor = getPerkDouble("Armor", _armor);

		setDesc(
				(C.cAqua + "Damage: " + C.cWhite + _damage) + C.cWhite + "        " + (C.cAqua + "Knockback Taken: " + C.cWhite + (int) (_knockbackTaken * 100) + "%"),
				(C.cAqua + "Armor: " + C.cWhite + _armor) + C.cWhite + "          " + (C.cAqua + "Health Regeneration: " + C.cWhite + _regen + " per Second")
		);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void Damage(CustomDamageEvent event)
	{
		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
		{
			return;
		}

		Player damager = event.GetDamagerPlayer(false);

		if (damager == null || !Kit.HasKit(damager) || !Manager.IsAlive(damager))
		{
			return;
		}

		double mod = _damage - event.GetDamageInitial();

		event.AddMod(damager.getName(), "Attack", mod, true);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void Knockback(CustomDamageEvent event)
	{
		Player damagee = event.GetDamageePlayer();

		if (damagee == null || !Kit.HasKit(damagee) || !Manager.IsAlive(damagee))
		{
			return;
		}

		event.AddKnockback("Knockback Multiplier", _knockbackTaken);
	}

	@EventHandler
	public void Regeneration(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!Kit.HasKit(player))
			{
				continue;
			}

			UtilPlayer.health(player, _regen);
		}
	}
}
