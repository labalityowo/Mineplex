package mineplex.game.clans.items.legendaries;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.recharge.Recharge;
import mineplex.game.clans.items.generation.ValueDistribution;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class HyperAxe extends LegendaryItem
{
	private static final long ATTACK_RATE_DURATION = 1000 / 10;
	private static final ValueDistribution AMOUNT_GEN = generateDistribution(0, 3);		// [1, 4] speed amount
	private static final ValueDistribution DURATION_GEN = generateDistribution(80, 320);	// [4, 16] seconds speed duration

	private final int _speedAmount;
	private final int _speedDuration;
	
	private long _lastAttack;
	
	public HyperAxe()
	{
		super("Hyper Axe", new String[]
		{
			C.cWhite + "Of all the weapons known to man,",
			C.cWhite + "none matches the savagery of the",
			C.cWhite + "Hyper Axe. Infused with a rabbit's",
			C.cWhite + "speed and a pigman's ferocity, this",
			C.cWhite + "blade can rip through any opponent.",
			C.cWhite + " ",
			C.cWhite + "Hit delay is reduced by " + C.cYellow + "50%",
			C.cWhite + "Deals " + C.cYellow + "6 Damage" + C.cWhite + " with attack",
			C.cYellow + "Right-Click" + C.cWhite + " to use " + C.cGreen + "Dash",
		}, Material.RECORD_3);
		_speedAmount = AMOUNT_GEN.generateIntValue();
		_speedDuration = DURATION_GEN.generateIntValue();
		_lastAttack = 0;
	}
	
	@Override
	public void update(Player wielder)
	{
		if (isHoldingRightClick())
		{
			buffPlayer(wielder);
		}
	}
	
	@Override
	public void onAttack(CustomDamageEvent event, Player wielder)
	{
		if (timeSinceLastAttack() >= ATTACK_RATE_DURATION)
		{
			event.SetIgnoreRate(true);
			
			event.AddMod("Hyper Axe", 5);
			_lastAttack = System.currentTimeMillis();
		}
		else
		{
			event.SetCancelled("Hyper Axe Cooldown");
		}
	}
	
	private long timeSinceLastAttack()
	{
		return System.currentTimeMillis() - _lastAttack;
	}
	
	private void buffPlayer(Player wielder)
	{
		if (Recharge.Instance.usable(wielder, "Hyper Rush", true))
		{
			Recharge.Instance.use(wielder, "Hyper Rush", 16000, true, false);
			// Give player speed buff
			wielder.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, _speedDuration, _speedAmount));
		}
	}
}