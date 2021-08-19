package nautilus.game.arcade.game.games.moba.kit.larissa;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import nautilus.game.arcade.game.games.moba.kit.common.DashSkill;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeUnit;

public class SkillWaterDash extends DashSkill
{

	private static final String[] DESCRIPTION = {
			"Dash along the ground, crippling enemies you",
			"come into contact with."
	};
	private static final ItemStack SKILL_ITEM = new ItemStack(Material.FEATHER);

	public SkillWaterDash(int slot)
	{
		super("Water Dash", DESCRIPTION, SKILL_ITEM, slot);

		setCooldown(10000);

		_collide = false;
		_velocityTime = 600;
		_velocityStopOnEnd = true;
		_horizontial = true;
	}

	@Override
	public void dashTick(Player player)
	{
		UtilParticle.PlayParticleToAll(ParticleType.DRIP_WATER, player.getLocation().add(0, 1, 0), 0.5F, 0.5F, 0.5F, 0.01F, 5, ViewDist.LONG);
	}
}

