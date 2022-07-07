package mineplex.core.boosters.gui;

import mineplex.core.boosters.BoosterApiResponse;
import mineplex.core.boosters.BoosterManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilTime;
import mineplex.core.shop.item.IButton;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

/**
 * @author Shaun Bennett
 */
@Deprecated
public class ActivateBoosterButton implements IButton
{
	private BoosterShop _boosterShop;
	private BoosterManager _boosterManager;

	public ActivateBoosterButton(BoosterShop boosterShop, BoosterManager boosterManager)
	{
		_boosterShop = boosterShop;
		_boosterManager = boosterManager;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		player.closeInventory();

		_boosterManager.chargeBooster(player, data -> {
			if (data)
			{
				_boosterManager.activateBooster(player, response -> {
					if (response.isSuccess())
					{
						long timeToStart = response.getStartTime().getTime() - System.currentTimeMillis();
						if (timeToStart <= 0) player.sendMessage(F.main("Amplifier", "Amplifier Activated!"));
						else                  player.sendMessage(F.main("Amplifier", "Game Amplifier Added. It will start in " + F.elem(UtilTime.convertString(timeToStart, 2, UtilTime.TimeUnit.FIT))));
					}
					else
					{
						player.sendMessage(C.cRed + "There was an error trying to enable your Amplifier :(");
					}
				});
			}
			else
			{
				player.sendMessage(F.main("Amplifier", "There was an error charging your account. Try again later!"));
			}
		});
	}
}
