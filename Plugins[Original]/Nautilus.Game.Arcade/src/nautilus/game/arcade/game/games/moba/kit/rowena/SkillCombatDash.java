package nautilus.game.arcade.game.games.moba.kit.rowena;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import nautilus.game.arcade.game.games.moba.kit.common.DashSkill;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SkillCombatDash extends DashSkill
{

	private static final String[] DESCRIPTION = {
			"Dash very fast along the ground.",
	};
	private static final ItemStack SKILL_ITEM = new ItemStack(Material.FEATHER);

	public SkillCombatDash(int slot)
	{
		super("Combat Slide", DESCRIPTION, SKILL_ITEM, slot);

		setCooldown(8000);

		_collide = false;
		_velocityTime = 250;
		_velocityMagnitude = 1.5;
		_horizontial = true;
	}

	@Override
	public void preDash(Player player)
	{
		UtilParticle.PlayParticleToAll(ParticleType.CLOUD, player.getLocation().add(0, 1, 0), 0.5F, 0.5F, 0.5F, 0.001F, 20, ViewDist.LONG);
		player.getWorld().playSound(player.getLocation(), Sound.BAT_TAKEOFF, 1, 1);
	}
}

