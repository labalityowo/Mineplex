package mineplex.core.gadget.gadgets.item;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ItemGadget;
import mineplex.core.gadget.util.CostConstants;

public class ItemTrampoline extends ItemGadget
{

	private static final long DURATION = TimeUnit.SECONDS.toMillis(20);

	public ItemTrampoline(GadgetManager manager)
	{
		super(manager, "Trampoline", new String[]
				{
						C.cGray + "Create a trampoline for all",
						C.cGray + "of your bouncing needs",
						C.blankLine,
						C.cWhite + "Sneak to go higher!"
				}, CostConstants.NO_LORE, Material.SLIME_BLOCK, (byte) 0, TimeUnit.SECONDS.toMillis(30), null);
	}

	@Override
	public void ActivateCustom(Player player)
	{
		Block block = player.getLocation().getBlock();
		List<Block> blocks = new ArrayList<>(UtilBlock.getSurrounding(block, true));
		blocks.add(block);

		Manager.selectBlocks(this, blocks);

		for (Block nearby : blocks)
		{
			if (nearby.getY() != block.getY())
			{
				continue;
			}

			Manager.getBlockRestore().add(nearby, Material.SLIME_BLOCK.getId(), getDisplayData(), DURATION);
		}

		player.teleport(player.getLocation().add(0, 1, 0));

		Location location = block.getLocation().add(0.5, 1.25, 0.5);

		Manager.runSyncTimer(new BukkitRunnable()
		{
			long iterations = DURATION / 50 / 10;

			@Override
			public void run()
			{
				for (Player nearby : block.getWorld().getPlayers())
				{
					double y = nearby.getVelocity().getY();

					if (y < 0.2 && y > -0.2 && blocks.contains(nearby.getLocation().getBlock().getRelative(BlockFace.DOWN)))
					{
						nearby.getWorld().playSound(nearby.getLocation(), Sound.FIREWORK_LAUNCH, 2, 1);
						UtilAction.velocity(nearby, 0.1, 3, 4, true);
					}
				}

				UtilParticle.PlayParticleToAll(ParticleType.HAPPY_VILLAGER, location, 1, 0.5F, 1, 0, 5, ViewDist.NORMAL);

				if (--iterations == 0)
				{
					cancel();
				}
			}
		}, 2, 10);
	}
}
