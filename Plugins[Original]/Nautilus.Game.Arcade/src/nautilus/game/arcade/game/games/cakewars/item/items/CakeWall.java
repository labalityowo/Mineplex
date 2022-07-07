package nautilus.game.arcade.game.games.cakewars.item.items;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.C;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.itemstack.ItemBuilder;

import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.cakewars.CakeWars;
import nautilus.game.arcade.game.games.cakewars.item.CakeSpecialItem;
import nautilus.game.arcade.game.games.cakewars.team.CakeTeam;

public class CakeWall extends CakeSpecialItem
{

	public static final ItemStack ITEM_STACK = new ItemBuilder(Material.STAINED_GLASS)
			.setTitle(C.cYellowB + "Wool Wall")
			.addLore("", "Creates a wall of wool above", "any block you click!", "Uses: " + C.cRed + "1")
			.build();
	private static final int PLATFORM_DELTA = 1;
	private static final int WALL_WARMUP_TICKS = 40;

	public CakeWall(CakeWars game)
	{
		super(game, ITEM_STACK, "Wool Wall", 500);
	}

	@Override
	protected boolean onClick(PlayerInteractEvent event, CakeTeam cakeTeam)
	{
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
		{
			return false;
		}

		event.setCancelled(true);

		Player player = event.getPlayer();
		Block block = event.getClickedBlock();

		BlockFace face = UtilBlock.getFace(player.getLocation().getYaw()).getOppositeFace();
		GameTeam team = cakeTeam.getGameTeam();
		byte teamData = team.GetColorData();
		boolean blockChanged = false;

		List<Block> changed = new ArrayList<>();
		boolean xAxis = face == BlockFace.NORTH || face == BlockFace.SOUTH;
		block = block.getRelative(BlockFace.UP, 2);

		for (int x = -PLATFORM_DELTA; x <= PLATFORM_DELTA; x++)
		{
			for (int y = -PLATFORM_DELTA; y <= PLATFORM_DELTA; y++)
			{
				Block nearby = block.getRelative(xAxis ? x : 0, y, xAxis ? 0 : x);

				if (isInvalidBlock(nearby))
				{
					continue;
				}

				_game.getCakePlayerModule().getPlacedBlocks().add(nearby);
				changed.add(nearby);
				blockChanged = true;
			}
		}

		DustSpellColor color = new DustSpellColor(team.GetColorBase());

		_game.getArcadeManager().runSyncTimer(new BukkitRunnable()
		{
			int ticks = 0;

			@Override
			public void run()
			{
				if (++ticks == WALL_WARMUP_TICKS)
				{
					cancel();

					for (Block wall : changed)
					{
						MapUtil.QuickChangeBlockAt(wall.getLocation(), Material.WOOL, teamData);

						if (Math.random() > 0.5)
						{
							wall.getWorld().playEffect(wall.getLocation(), Effect.STEP_SOUND, Material.WOOL, teamData);
						}
					}
				}
				else
				{
					int index = 0;
					double maxY = ((double) ticks / WALL_WARMUP_TICKS) * 3;

					for (Block wall : changed)
					{
						if (index++ % 3 == 0)
						{
							for (double y = 0; y < maxY; y += 0.2)
							{
								new ColoredParticle(ParticleType.RED_DUST, color, wall.getLocation().add(xAxis ? Math.random() : 0.5, y, xAxis ? 0.5 : Math.random()))
										.display();
							}
						}
					}
				}
			}
		}, 0, 1);

		return blockChanged;
	}
}
