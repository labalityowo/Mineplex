package nautilus.game.arcade.game.games.wizards.spells;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilShapes;
import nautilus.game.arcade.game.games.wizards.Spell;
import nautilus.game.arcade.game.games.wizards.spellinterfaces.SpellClick;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SpellRainbowBeam extends Spell implements SpellClick
{

	@Override
	public void castSpell(Player p)
	{
		Entity entityTarget = UtilPlayer.getEntityInSight(p, 80, true, true, true, 1.9F);

		if (!(entityTarget instanceof LivingEntity))
		{
			entityTarget = null;
		}

		Location loc;
		if (entityTarget != null)
		{

			loc = p.getEyeLocation().add(
					p.getEyeLocation().getDirection().normalize()
							.multiply(0.3 + p.getEyeLocation().distance(((LivingEntity) entityTarget).getEyeLocation())));

			double damage = (getSpellLevel(p) * 2) + 2;
			double dist = loc.distance(p.getLocation()) - (80 * .2D);

			// If target is more than 20% away
			if (dist > 0)
			{
				damage -= damage * (dist / (80 * .8D));

				damage = Math.max(1, damage);
			}

			// The above code makes the beam appear to hit them where you aimed.
			Wizards.getArcadeManager()
					.GetDamage()
					.NewDamageEvent((LivingEntity) entityTarget, p, null, DamageCause.MAGIC, damage, true, true, false,
							"Rainbow Beam", "Rainbow Beam");

			p.playSound(entityTarget.getLocation(), Sound.LEVEL_UP, (getSpellLevel(p) * 2) + 6, 1);

		}
		else
		{
			loc = p.getLastTwoTargetBlocks(UtilBlock.blockAirFoliageSet, 80).get(0).getLocation().add(0.5, 0.5, 0.5);
		}

		for (Location l : UtilShapes.getLinesDistancedPoints(p.getEyeLocation().subtract(0, 0.1, 0), loc, 0.3))
		{
			l.getWorld().spigot().playEffect(l, Effect.POTION_SWIRL, 0, 0, 0, 0, 0, 500, 1, 30);
		}

		p.playSound(p.getLocation(), Sound.LEVEL_UP, 1.5F, 1);

		charge(p);
	}

}