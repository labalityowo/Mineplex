package mineplex.game.nano.game.games.wizards.spells;

import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.game.nano.game.games.wizards.Spell;
import mineplex.game.nano.game.games.wizards.Wizards;

public class SpellLevitation extends Spell
{

	public SpellLevitation(Wizards game)
	{
		super(game, "Whirlwind", SpellType.Utility, new ItemStack(Material.FEATHER), TimeUnit.SECONDS.toMillis(5));
	}

	@Override
	protected void onSpellUse(Player player)
	{
		Location location = player.getLocation();

		location.getWorld().playSound(location, Sound.BAT_TAKEOFF, 0.5F, 0.5F);
		UtilParticle.PlayParticleToAll(ParticleType.CLOUD, location, 0.4F, 0.4F, 0.F, 0.1F, 25, ViewDist.LONG);

		Vector velocity = location.getDirection();
		velocity.setY(Math.max(0, velocity.getY()) + 0.1);
		UtilAction.velocity(player, velocity.multiply(2));
	}
}
