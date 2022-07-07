package nautilus.game.arcade.game.games.cakewars.shop.trap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;

public class CakeBearTrap extends CakeTrapItem
{

	public CakeBearTrap(int cost)
	{
		super(new ItemStack(Material.TRIPWIRE_HOOK), cost, "Bear Trap",
				C.cWhite + "When a player gets near your Cake.",
				C.cWhite + "They are blinded and slowed for " + C.cGreen + "4 Seconds" + C.cWhite + "."
		);

		_trapTrigger = TrapTrigger.CAKE_NEAR;
	}

	@Override
	public void onTrapTrigger(Player player, Location cake)
	{
		player.getWorld().playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 2, 1);
		UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, player.getLocation().add(0, 1.5, 0), 0.25F, 0.25F, 0.25F, 0.5F, 30, ViewDist.NORMAL);
		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 4));
		player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0, false, false));
	}
}
