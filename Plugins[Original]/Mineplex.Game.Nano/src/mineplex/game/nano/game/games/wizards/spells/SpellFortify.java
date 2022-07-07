package mineplex.game.nano.game.games.wizards.spells;

import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.nano.game.games.wizards.Spell;
import mineplex.game.nano.game.games.wizards.Wizards;

public class SpellFortify extends Spell
{

	public SpellFortify(Wizards game)
	{
		super(game, "Heal", SpellType.Defense, new ItemStack(Material.IRON_HOE), TimeUnit.SECONDS.toMillis(20));
	}

	@Override
	protected void onSpellUse(Player player)
	{
		Location location = player.getLocation().add(0, 0.1, 0);
		double r = 1.5;

		for (int i = 0; i < 13; i++)
		{
			for (double theta = 0; theta < 2 * Math.PI; theta += Math.PI / 10)
			{
				double x = r * Math.cos(theta), z = r * Math.sin(theta);

				location.add(x, 0, z);

				UtilParticle.PlayParticleToAll(ParticleType.DRIP_WATER, location, null, 0, 1, ViewDist.NORMAL);

				location.subtract(x, 0, z);
			}

			location.add(0, 0.2, 0);
		}

		location.getWorld().playSound(location, Sound.ZOMBIE_REMEDY, 0.5F, 1);
		UtilPlayer.health(player, 10);
	}
}
