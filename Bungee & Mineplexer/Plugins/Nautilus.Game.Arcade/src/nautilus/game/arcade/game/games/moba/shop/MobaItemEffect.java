package nautilus.game.arcade.game.games.moba.shop;

import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.minecraft.game.core.condition.events.ConditionApplyEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.events.PlayerGameRespawnEvent;
import nautilus.game.arcade.game.games.moba.kit.hp.MobaHPRegenEvent;
import nautilus.game.arcade.game.games.moba.kit.AmmoGiveEvent;
import nautilus.game.arcade.game.games.moba.kit.CooldownCalculateEvent;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public abstract class MobaItemEffect
{

	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.#");

	protected static String format(double d)
	{
		return DECIMAL_FORMAT.format(d);
	}

	protected static String format(ConditionType conditionType, int multi)
	{
		String condition = conditionType.toString().toLowerCase();
		char first = Character.toUpperCase(condition.charAt(0));
		condition = first + condition.substring(1);

		return condition + (multi == -1 ? "" : " " + (multi + 1));
	}

	protected void onAmmoGive(AmmoGiveEvent event)
	{
	}

	protected void onCooldownCheck(CooldownCalculateEvent event)
	{
	}

	protected void onDamage(CustomDamageEvent event)
	{
	}

	protected void onDeath(Player killed, Player killer)
	{
	}

	protected void onHPRegen(MobaHPRegenEvent event)
	{
	}

	protected void onHPRegenOthers(MobaHPRegenEvent event)
	{
	}

	protected void onRespawn(PlayerGameRespawnEvent event, boolean fake)
	{
	}

	protected void onCondition(ConditionApplyEvent event)
	{
	}

	public abstract String getDescription();

}
