package mineplex.game.clans.tutorial.tutorials.clans.objective.goals.energy;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.game.clans.clans.ClansManager;
import org.bukkit.DyeColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.game.clans.clans.event.EnergyPageBuildEvent;
import mineplex.game.clans.clans.event.PreEnergyShopBuyEvent;
import mineplex.game.clans.tutorial.objective.ObjectiveGoal;
import mineplex.game.clans.tutorial.tutorials.clans.objective.EnergyObjective;

public class BuyEnergyGoal extends ObjectiveGoal<EnergyObjective>
{
	public BuyEnergyGoal(EnergyObjective objective)
	{
		super(
				objective,
				"Buy Energy",
				"Buy Clan Energy from the Energy Shop",
				"You can buy Clan Energy at the Shops.",
				DyeColor.RED
		);
	}

	@Override
	protected void customStart(Player player)
	{
		ClansManager.getInstance().runSyncLater(() -> {
			UtilPlayer.message(player, F.main("Clans", "WARNING: Clan Energy is running very low!"));
			UtilTextMiddle.display("Clan Energy", "is running very low", 10, 100, 10, player);

			player.playSound(player.getLocation(), Sound.NOTE_BASS, 1.0f, 1.0f);
		}, 3L);
	}

	@Override
	protected void customFinish(Player player)
	{
	}
	
	@EventHandler
	public void energyBuy(PreEnergyShopBuyEvent event)
	{
		if (contains(event.getPlayer()))
		{
			finish(event.getPlayer());
			event.getPlayer().closeInventory();
		}
	}
	
	@EventHandler
	public void energyBuild(EnergyPageBuildEvent event)
	{
		if (contains(event.getPlayer()))
		{
			event.setFree(true);
		}
	}
}
