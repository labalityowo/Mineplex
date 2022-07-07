package mineplex.game.nano.game.games.quick.challenges;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.game.event.PlayerGameApplyEvent;
import mineplex.game.nano.game.games.quick.Challenge;
import mineplex.game.nano.game.games.quick.ChallengeType;
import mineplex.game.nano.game.games.quick.Quick;

public class ChallengeSpeedBridge extends Challenge
{

	private int _goalX;

	public ChallengeSpeedBridge(Quick game)
	{
		super(game, ChallengeType.SPEED_BRIDGE);

		_winConditions.setTimeoutAfterFirst(true);
		_timeout = TimeUnit.SECONDS.toMillis(30);
	}

	@Override
	public void challengeSelect()
	{
		_goalX = _game.getMineplexWorld().getIronLocation("LIGHT_BLUE").getBlockX();

		List<Location> corners = _game.getYellowPoints();

		for (Block block : UtilBlock.getInBoundingBox(corners.get(0).clone().subtract(0, 1, 0), corners.get(1).clone().subtract(0, 1, 0)))
		{
			MapUtil.QuickChangeBlockAt(block.getLocation(), Material.AIR);
		}

		ItemStack itemStack = new ItemStack(Material.COBBLESTONE, 64);

		for (Player player : _players)
		{
			player.getInventory().addItem(itemStack);
			player.setGameMode(GameMode.SURVIVAL);
		}
	}

	@Override
	public void disable()
	{
	}

	@EventHandler
	public void playerApply(PlayerGameApplyEvent event)
	{
		event.setRespawnLocation(UtilAlg.getLocationAwayFromPlayers(_game.getYellowSpawns(), _players));
	}

	@EventHandler
	public void blockPlace(BlockPlaceEvent event)
	{
		Player player = event.getPlayer();

		if (!isParticipating(player) || !inArena(event.getBlock()))
		{
			return;
		}

		event.setCancelled(false);
	}

	@EventHandler
	public void updateComplete(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}

		for (Player player : _game.getAlivePlayers())
		{
			if (player.getLocation().getX() <= _goalX)
			{
				completePlayer(player, true);
			}
		}
	}
}
