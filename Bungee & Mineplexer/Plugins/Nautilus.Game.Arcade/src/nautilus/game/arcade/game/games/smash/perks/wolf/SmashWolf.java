package nautilus.game.arcade.game.games.smash.perks.wolf;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseWolf;
import mineplex.core.recharge.Recharge;
import mineplex.minecraft.game.core.condition.ConditionFactory;

import nautilus.game.arcade.game.games.smash.perks.SmashUltimate;

public class SmashWolf extends SmashUltimate
{

	public SmashWolf()
	{
		super("Frenzy", new String[] {}, Sound.WOLF_HOWL, 0);
	}
	
	@Override
	public void activate(Player player)
	{
		super.activate(player);

		ConditionFactory factory = Manager.GetCondition().Factory();

		factory.Strength(GetName(), player, player, 30, 1, false, false, false);
		factory.Speed(GetName(), player, player, 30, 2, false, false, false);
		factory.Regen(GetName(), player, player, 30, 2, false, false, false);

		DisguiseBase disguise = Manager.GetDisguise().getActiveDisguise(player);

		if (disguise instanceof DisguiseWolf)
		{
			((DisguiseWolf) disguise).setAngry(true);
			Manager.GetDisguise().updateDisguise(disguise);
		}

		Recharge.Instance.recharge(player, "Wolf Strike");
		Recharge.Instance.recharge(player, "Cub Tackle");
	}

	@Override
	public void cancel(Player player)
	{
		super.cancel(player);

		DisguiseBase disguise = Manager.GetDisguise().getActiveDisguise(player);
		
		if (disguise instanceof DisguiseWolf)
		{
			((DisguiseWolf) disguise).setAngry(false);
			Manager.GetDisguise().updateDisguise(disguise);
		}
	}
}
