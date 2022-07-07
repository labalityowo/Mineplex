package nautilus.game.arcade.game.games.moba.kit.bob;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import nautilus.game.arcade.game.games.moba.kit.common.DashSkill;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SkillBeatTheDevil extends DashSkill
{

	private static final String[] DESCRIPTION = {
			"Bob Ross"
	};
	private static final ItemStack SKILL_ITEM = new ItemStack(Material.FEATHER);

	public SkillBeatTheDevil(int slot)
	{
		super("Beat The Devil Out Of It", DESCRIPTION, SKILL_ITEM, slot);

		setCooldown(12000);

		_collide = false;
		_velocityTime = 800;
		_velocityStopOnEnd = true;
	}

	@Override
	public void dashTick(Player player)
	{
		player.getWorld().playSound(player.getLocation(), player.getTicksLived() % 2 == 0 ? Sound.DOOR_OPEN : Sound.DOOR_CLOSE, 1, 0.5F);

		for (int i = 0; i < 10; i++)
		{
			UtilParticle.playColoredParticleToAll(Color.RED, ParticleType.RED_DUST, UtilAlg.getRandomLocation(player.getLocation().add(0, 1, 0), 2), 1, ViewDist.LONG);
		}
	}
}

