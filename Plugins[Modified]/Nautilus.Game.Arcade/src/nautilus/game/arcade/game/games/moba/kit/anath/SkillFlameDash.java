package nautilus.game.arcade.game.games.moba.kit.anath;

import mineplex.core.common.util.UtilBlock;
import nautilus.game.arcade.game.games.moba.kit.common.DashSkill;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SkillFlameDash extends DashSkill
{

	private static final String[] DESCRIPTION = {
			"Dash along the ground, leaving fire behind you.",
	};
	private static final ItemStack SKILL_ITEM = new ItemStack(Material.FEATHER);

	public SkillFlameDash(int slot)
	{
		super("Flame Dash", DESCRIPTION, SKILL_ITEM, slot);

		setCooldown(12000);

		_collide = false;
		_velocityTime = 600;
		_velocityStopOnEnd = true;
		_horizontial = true;
	}

	@Override
	public void dashTick(Player player)
	{
		Block block = player.getLocation().getBlock();

		while (!UtilBlock.solid(block))
		{
			block = block.getRelative(BlockFace.DOWN);
		}

		Block fBlock = block;
		Manager.runSyncLater(() -> Manager.GetBlockRestore().add(fBlock.getRelative(BlockFace.UP), Material.FIRE.getId(), (byte) 0, 5000), 10);
	}
}

