package nautilus.game.arcade.game.games.wizards.spells;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import nautilus.game.arcade.game.games.wizards.Spell;
import nautilus.game.arcade.game.games.wizards.spellinterfaces.SpellClick;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class SpellFlash extends Spell implements SpellClick
{
	@Override
	public void castSpell(Player player)
	{
		int maxRange = 20 + (10 * getSpellLevel(player));

		double curRange = 0;

		while (curRange <= maxRange)
		{
			Location newTarget = player.getEyeLocation().add(new Vector(0, 0.2, 0))
					.add(player.getLocation().getDirection().multiply(curRange));

			if (!UtilBlock.airFoliage(newTarget.getBlock())
					|| !UtilBlock.airFoliage(newTarget.getBlock().getRelative(BlockFace.UP)))
				break;

			// Progress Forwards
			curRange += 0.2;

			// Smoke Trail
			UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, newTarget.clone().add(0, 0.5, 0), 0, 0, 0, 0, 1,
					ViewDist.LONG, UtilServer.getPlayers());
		}

		// Modify Range
		curRange -= 0.4;
		if (curRange < 0)
			curRange = 0;

		// Destination
		Location loc = player.getEyeLocation().add(new Vector(0, 0.2, 0))
				.add(player.getLocation().getDirection().multiply(curRange)).add(new Vector(0, 0.4, 0));

		if (curRange > 0)
		{

			player.getWorld().playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1.2F);
			player.getWorld().playEffect(player.getLocation(), Effect.ENDER_SIGNAL, 9);

			player.setFallDistance(0);

			player.eject();
			player.leaveVehicle();

			player.teleport(loc);

			player.getWorld().playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1.2F);
			player.getWorld().playEffect(player.getLocation(), Effect.ENDER_SIGNAL, 9);

			charge(player);
		}
	}
}
