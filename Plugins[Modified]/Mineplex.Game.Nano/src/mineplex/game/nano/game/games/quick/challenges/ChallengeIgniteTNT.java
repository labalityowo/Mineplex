package mineplex.game.nano.game.games.quick.challenges;

import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.game.nano.game.games.quick.Challenge;
import mineplex.game.nano.game.games.quick.ChallengeType;
import mineplex.game.nano.game.games.quick.Quick;

public class ChallengeIgniteTNT extends Challenge
{

	private int _tnt;

	public ChallengeIgniteTNT(Quick game)
	{
		super(game, ChallengeType.IGNITE_TNT);
	}

	@Override
	public void challengeSelect()
	{
		List<Location> corners = _game.getOrangePoints();
		List<Block> blocks = UtilBlock.getInBoundingBox(corners.get(0), corners.get(1), false);
		_tnt = Math.min(20, _players.size() / 2);

		for (int i = 0; i < _tnt; i++)
		{
			Block block = UtilAlg.Random(blocks);

			if (block == null)
			{
				return;
			}

			MapUtil.QuickChangeBlockAt(block.getLocation(), Material.TNT);
		}

		ItemStack itemStack = new ItemStack(Material.FLINT_AND_STEEL);

		for (Player player : _players)
		{
			player.getInventory().addItem(itemStack);
			player.getInventory().setHeldItemSlot(0);
			player.setGameMode(GameMode.SURVIVAL);
		}
	}

	@Override
	public void disable()
	{

	}

	@EventHandler
	public void playerInteract(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
		{
			return;
		}

		Player  player = event.getPlayer();

		if (!isParticipating(player))
		{
			return;
		}

		ItemStack itemStack = player.getItemInHand();
		Block block = event.getClickedBlock();

		if (itemStack == null || itemStack.getType() != Material.FLINT_AND_STEEL || block.getType() != Material.TNT || !inArena(block))
		{
			return;
		}

		_game.getManager().runSyncLater(() ->
		{
			completePlayer(player, true);

			if (--_tnt <= 0)
			{
				end();
			}
		}, 0);
	}
}
