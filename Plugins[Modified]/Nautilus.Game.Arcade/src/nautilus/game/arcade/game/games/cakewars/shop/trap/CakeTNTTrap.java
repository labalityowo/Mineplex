package nautilus.game.arcade.game.games.cakewars.shop.trap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;

public class CakeTNTTrap extends CakeTrapItem
{

	public CakeTNTTrap(int cost)
	{
		super(new ItemStack(Material.TNT), cost, "TNT Trap",
				C.cWhite + "When a player attempts to eat your Cake.",
				C.cWhite + "They are thrown into the air!"
		);
	}

	@Override
	public void onTrapTrigger(Player player, Location cake)
	{
		player.getWorld().playSound(player.getLocation(), Sound.EXPLODE, 2, 0.6F);
		UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, player.getLocation().add(0, 1.5, 0), 0, 0, 0, 1, 1, ViewDist.NORMAL);

		Vector direction = UtilAlg.getTrajectory2d(cake, player.getLocation());
		direction.multiply(1.5);
		direction.setY(1.3 + (Math.random() / 2D));

		UtilAction.velocity(player, direction);
	}
}
