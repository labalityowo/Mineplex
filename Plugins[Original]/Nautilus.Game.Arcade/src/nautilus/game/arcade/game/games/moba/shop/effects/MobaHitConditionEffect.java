package nautilus.game.arcade.game.games.moba.shop.effects;

import mineplex.core.Managers;
import mineplex.core.common.util.F;
import mineplex.minecraft.game.core.condition.Condition;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;
import mineplex.minecraft.game.core.condition.ConditionManager;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.moba.shop.MobaItemEffect;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class MobaHitConditionEffect extends MobaItemEffect
{

	private String _reason;
	private ConditionType _conditionType;
	private double _duration;
	private int _multi;
	private boolean _applyToDamagee;

	public MobaHitConditionEffect(String reason, ConditionType conditionType, double duration, int multi, boolean applyToDamagee)
	{
		_reason = reason;
		_conditionType = conditionType;
		_duration = duration;
		_multi = multi;
		_applyToDamagee = applyToDamagee;
	}

	@Override
	protected void onDamage(CustomDamageEvent event)
	{
		Player damagee = event.GetDamageePlayer();
		Player damager = event.GetDamagerPlayer(true);

		if (!_applyToDamagee)
		{
			// Swap damagee and damager
			Player temp = damagee;
			damagee = damager;
			damager = temp;
		}

		ConditionManager conditionManager = Managers.get(ArcadeManager.class).GetCondition();

		conditionManager.AddCondition(new Condition(conditionManager, _reason, damagee, damager, _conditionType, _multi, (int) (_duration * 20), false, Material.WEB, (byte) 0, true, false));
	}

	@Override
	public String getDescription()
	{
		return "Hitting a player gives " + (_applyToDamagee ? "them" : "you") + " " + F.greenElem(format(_conditionType, _multi)) + " for " + F.time(format(_duration)) + " seconds.";
	}
}
