package mineplex.game.clans.items.legendaries;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.game.clans.items.generation.ValueDistribution;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class AlligatorsTooth extends LegendaryItem
{
	private static final ValueDistribution BOOST_GEN = generateDistribution(0.8d, 1.4d);
	private static final double LAND_DAMAGE_BONUS = 7;
	private static final double WATER_DAMAGE_BONUS = 11;

	private double _swimSpeed;
	private int _soundUpdateCounter;
	
	public AlligatorsTooth()
	{
		super("Alligators Tooth", new String[]
		{
			C.cWhite + "This deadly tooth was stolen from",
			C.cWhite + "a nest of reptilian beasts long",
			C.cWhite + "ago. Legends say that the holder",
			C.cWhite + "is granted the underwater agility",
			C.cWhite + "of an Alligator.",
			" ",
			C.cWhite + "Deals " + C.cYellow + "8 Damage" + C.cWhite + " with attack on land",
			C.cWhite + "Deals " + C.cYellow + "12 Damage" + C.cWhite + " with attack in water",
			C.cYellow + "Right-Click" + C.cWhite  + " to use " + C.cGreen + "Gator Stroke",
		}, Material.RECORD_4);
		
		_swimSpeed = BOOST_GEN.generateValue();
	}
	
	@Override
	public void update(Player wielder)
	{
		if (isInWater(wielder))
		{
			// Player gain water breathing while under water with legendary equipped
			grantPotionEffect(wielder, PotionEffectType.WATER_BREATHING, 0, 50);
			
			if (isHoldingRightClick())
			{
				propelPlayer(wielder);
				if (++_soundUpdateCounter % 3 == 0)
				{
					wielder.playSound(wielder.getLocation(), Sound.SPLASH2, .5f, 1.25f);
					wielder.getLocation().getWorld().playEffect(wielder.getLocation(), Effect.STEP_SOUND, Material.LAPIS_BLOCK.getId());
				}
			}
		}
	}
	
	@Override
	public void onAttack(CustomDamageEvent event, Player wielder)
	{
		if (isInWater(wielder))
		{
			event.AddMod("Alligators Tooth Water Bonus", WATER_DAMAGE_BONUS);
			event.AddKnockback("Alligators Tooth Water Bonus", 0.5d);
		}
		else
		{
			event.AddMod("Alligators Tooth Land Bonus", LAND_DAMAGE_BONUS);
		}
		
	}
	
	private void propelPlayer(Player player)
	{
		Vector direction = player.getLocation().getDirection().normalize();
		direction.multiply(_swimSpeed);
		player.setVelocity(direction);
	}
	
	private boolean isInWater(Player player)
	{
		Material type = player.getLocation().getBlock().getType();
		return type == Material.WATER || type == Material.STATIONARY_WATER;
	}
}