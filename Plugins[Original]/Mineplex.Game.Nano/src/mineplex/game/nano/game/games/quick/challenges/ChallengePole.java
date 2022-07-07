package mineplex.game.nano.game.games.quick.challenges;

import java.util.concurrent.TimeUnit;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.game.nano.game.games.quick.Challenge;
import mineplex.game.nano.game.games.quick.ChallengeType;
import mineplex.game.nano.game.games.quick.Quick;

public class ChallengePole extends Challenge
{

	private static final int GOAL = 10;

	private int _goalY;

	public ChallengePole(Quick game)
	{
		super(game, ChallengeType.POLE);

		_timeout = TimeUnit.SECONDS.toMillis(25);
		_winConditions.setTimeoutAfterFirst(true, 1);
	}

	@Override
	public void challengeSelect()
	{
		_goalY = _game.getCenter().getBlockY() + GOAL;

		ItemStack itemStack = new ItemStack(Material.COBBLESTONE, 64);

		for (Player player : _players)
		{
			player.setGameMode(GameMode.SURVIVAL);
			player.getInventory().addItem(itemStack);
		}
	}

	@Override
	public void disable()
	{
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

		if (player.getLocation().getY() > _goalY)
		{
			completePlayer(player, true);
		}
	}
}
