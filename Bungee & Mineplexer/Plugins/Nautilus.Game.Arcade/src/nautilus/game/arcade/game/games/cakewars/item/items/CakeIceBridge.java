package nautilus.game.arcade.game.games.cakewars.item.items;

import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.itemstack.ItemBuilder;

import nautilus.game.arcade.game.games.cakewars.CakeWars;
import nautilus.game.arcade.game.games.cakewars.item.CakeSpecialItem;
import nautilus.game.arcade.game.games.cakewars.team.CakeTeam;

public class CakeIceBridge extends CakeSpecialItem
{

	public static final ItemStack ITEM_STACK = new ItemBuilder(Material.ICE)
			.setTitle(C.cYellowB + "Ice Bridge")
			.addLore("", "Creates a huge bridge of ice", "Warning! Ice Bridges have a", C.cRed + "20 second" + C.cGray + " cooldown between uses ", "and only last for " + C.cRed + "7 Seconds" + C.cGray + "!", "Uses: " + C.cRed + "1")
			.build();
	private static final int MAX_DISTANCE = 30;
	private static final long BRIDGE_TIME = TimeUnit.SECONDS.toMillis(7);

	public CakeIceBridge(CakeWars game)
	{
		super(game, ITEM_STACK, "Ice Bridge", TimeUnit.SECONDS.toMillis(20));
	}

	@Override
	protected boolean onClick(PlayerInteractEvent event, CakeTeam cakeTeam)
	{
		event.setCancelled(true);

		Player player = event.getPlayer();
		Block block = event.getClickedBlock();

		if (block == null)
		{
			block = player.getLocation().getBlock();
		}
		else
		{
			block = block.getRelative(BlockFace.UP);
		}

		if (isInvalidBlock(block))
		{
			return false;
		}

		Location location = player.getLocation();
		location.setPitch(0);
		BlockFace direction = UtilBlock.getFace(player.getLocation().getYaw()).getOppositeFace();
		BlockRestore restore = _game.getArcadeManager().GetBlockRestore();
		Block finalBlock = block;

		_game.getArcadeManager().runSyncTimer(new BukkitRunnable()
		{
			Block target = finalBlock;
			int i = 0;

			@Override
			public void run()
			{
				if (!_game.IsLive())
				{
					cancel();
					return;
				}

				target = target.getRelative(direction);
				boolean blockChanged = false;

				for (BlockFace face : UtilBlock.horizontals)
				{
					Block nearby = target.getRelative(face);

					if (!isInvalidBlock(nearby))
					{
						restore.add(nearby, Material.ICE.getId(), (byte) 0,BRIDGE_TIME + UtilMath.rRange(0, 500));
						blockChanged = true;
					}
				}

				if (!blockChanged || i++ > MAX_DISTANCE)
				{
					cancel();
				}
				else
				{
					target.getWorld().playSound(target.getLocation(), Sound.DIG_SNOW, 1, 1);
				}
			}
		}, 0, 5);

		return true;
	}
}
