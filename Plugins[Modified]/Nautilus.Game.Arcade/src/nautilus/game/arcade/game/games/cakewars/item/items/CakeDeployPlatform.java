package nautilus.game.arcade.game.games.cakewars.item.items;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.itemstack.ItemBuilder;

import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.cakewars.CakeWars;
import nautilus.game.arcade.game.games.cakewars.item.CakeSpecialItem;
import nautilus.game.arcade.game.games.cakewars.team.CakeTeam;

public class CakeDeployPlatform extends CakeSpecialItem
{

	public static final ItemStack ITEM_STACK = new ItemBuilder(Material.INK_SACK)
			.setTitle(C.cYellowB + "Deploy Platform")
			.addLore("", "Creates a platform of wool next to", "any block you click!", "Uses: " + C.cRed + "1")
			.build();
	private static final int PLATFORM_DELTA = 1;

	public CakeDeployPlatform(CakeWars game)
	{
		super(game, ITEM_STACK);
	}

	@Override
	protected boolean onClick(PlayerInteractEvent event, CakeTeam cakeTeam)
	{
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();

		if (block == null)
		{
			return false;
		}

		BlockFace face = UtilBlock.getFace(player.getLocation().getYaw()).getOppositeFace();
		GameTeam team = cakeTeam.getGameTeam();
		byte teamData = team.GetColorData();
		boolean blockChanged = false;

		block = block.getRelative(face, 2);

		for (int x = -PLATFORM_DELTA; x <= PLATFORM_DELTA; x++)
		{
			for (int z = -PLATFORM_DELTA; z <= PLATFORM_DELTA; z++)
			{
				Block nearby = block.getRelative(x, 0, z);
				Location nearbyLocation = nearby.getLocation();

				if (isInvalidBlock(nearby))
				{
					continue;
				}

				_game.getCakePlayerModule().getPlacedBlocks().add(nearby);
				MapUtil.QuickChangeBlockAt(nearbyLocation, Material.WOOL, teamData);
				blockChanged = true;
			}
		}

		return blockChanged;
	}
}
