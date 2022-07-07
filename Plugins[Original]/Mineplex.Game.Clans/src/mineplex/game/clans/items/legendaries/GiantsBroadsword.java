package mineplex.game.clans.items.legendaries;

import mineplex.core.recharge.Recharge;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class GiantsBroadsword extends LegendaryItem
{
	private static final int SLOW_AMPLIFIER = 43;
	private static final int REGEN_AMPLIFIER = 1;

	public GiantsBroadsword()
	{
		super("Giants Broadsword", new String[]
		{
			C.cWhite + "Forged in the godly mines of Plagieus,",
			C.cWhite + "this sword has endured thousands of",
			C.cWhite + "wars. It is sure to grant glorious",
			C.cWhite + "victory in battle.",
			C.cWhite + " ",
			C.cWhite + "Deals " + C.cYellow + "10 Damage" + C.cWhite + " with attack",
			C.cYellow + "Right-Click" + C.cWhite + " to use " + C.cGreen + "Shield",
		}, Material.GOLD_RECORD);
	}
	
	@Override
	public void update(Player wielder)
	{
		if (isHoldingRightClick())
		{
			buffPlayer(wielder);

			UtilParticle.PlayParticle(ParticleType.HEART, wielder.getEyeLocation().add(0, 0.25, 0), -.5f + (float) Math.random(), -.5f + (float) Math.random(), -.5f + (float) Math.random(), .2f, 1, ViewDist.NORMAL);
			wielder.playSound(wielder.getLocation(), Sound.LAVA_POP, 1f, 2f);
			return;
		}

		UtilParticle.PlayParticle(ParticleType.MAGIC_CRIT, wielder.getLocation().add(0, 1, 0), 0, 0, 0, .2f, 3, ViewDist.NORMAL);
	}
	
	@Override
	public void onAttack(CustomDamageEvent event, Player wielder)
	{
		if (isHoldingRightClick())
		{
			event.SetCancelled("Giants Broadsword effects");
			return;
		}
		
		event.AddMod("Giants Bonus", 9);
		event.AddKnockback("Giants Sword", 0.5d);
	}

	private void buffPlayer(Player player)
	{
		grantPotionEffect(player, PotionEffectType.SLOW, 40, SLOW_AMPLIFIER);
		if (Recharge.Instance.use(player, "Giants Broadsword Regen", 250L, false, false, false))
		{
			grantPotionEffect(player, PotionEffectType.REGENERATION, 5, REGEN_AMPLIFIER); //Regen
		}
	}
}