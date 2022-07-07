package mineplex.game.nano.game.games.quick.challenges;

import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.game.nano.game.games.quick.Challenge;
import mineplex.game.nano.game.games.quick.ChallengeType;
import mineplex.game.nano.game.games.quick.Quick;

public class ChallengeMilkCow extends Challenge
{

	public ChallengeMilkCow(Quick game)
	{
		super(game, ChallengeType.MILK_A_COW);

		_timeout = TimeUnit.SECONDS.toMillis(5);
	}

	@Override
	public void challengeSelect()
	{
		_game.getWorldComponent().setCreatureAllowOverride(true);

		_game.getGreenPoints().forEach(location ->
		{
			Location spawn = location.clone();

			spawn.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(_game.getSpectatorLocation(), spawn)));

			Cow cow = spawn.getWorld().spawn(spawn, Cow.class);

			UtilEnt.vegetate(cow);
			UtilEnt.ghost(cow, true, false);
			UtilEnt.setFakeHead(cow, true);
		});

		_game.getWorldComponent().setCreatureAllowOverride(false);

		ItemStack itemStack = new ItemStack(Material.BUCKET);

		for (Player player : _players)
		{
			player.getInventory().addItem(itemStack);
			player.getInventory().setHeldItemSlot(0);
		}
	}

	@Override
	public void disable()
	{

	}

	@EventHandler
	public void playerItemConsume(PlayerItemConsumeEvent event)
	{
		if (event.getItem().getType() == Material.MILK_BUCKET)
		{
			completePlayer(event.getPlayer(), false);
		}
	}
}
