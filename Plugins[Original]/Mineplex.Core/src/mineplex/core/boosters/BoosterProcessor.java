package mineplex.core.boosters;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilTime;
import mineplex.core.shop.confirmation.ConfirmationCallback;
import mineplex.core.shop.confirmation.ConfirmationProcessor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * @author Shaun Bennett
 */
public class BoosterProcessor implements ConfirmationProcessor
{
	private BoosterManager _boosterManager;
	private Player _player;

	public BoosterProcessor(BoosterManager boosterManager, Player player)
	{
		_boosterManager = boosterManager;
		_player = player;
	}

	@Override
	public void init(Inventory inventory) {}

	@Override
	public void process(ConfirmationCallback callback)
	{
		_boosterManager.chargeBooster(_player, data -> {
			if (data)
			{
				_boosterManager.activateBooster(_player, response -> {
					if (response.isSuccess())
					{
						long timeToStart = response.getStartTime().getTime() - System.currentTimeMillis();
						if (timeToStart <= 0) _player.sendMessage(F.main("Amplifier", "Game Amplifier Activated!"));
						else                  _player.sendMessage(F.main("Amplifier", "Game Amplifier Added. It will start in " + F.elem(UtilTime.convertString(timeToStart, 2, UtilTime.TimeUnit.FIT))));
						callback.resolve("Success!");
					}
					else
					{
						_player.sendMessage(C.cRed + "There was an error trying to enable your Game Amplifier");
						if (response.getStatusCode() == 503 && response.getError() != null && response.getError().length() > 0)
						{
							// Service Unavailable HTTP Code
							_player.sendMessage(C.cRed + "Error: " + response.getError());
						}

						_boosterManager.refundBooster(_player, null);
						callback.reject("Failed. Try again later.");
					}
				});
			}
			else
			{
				callback.reject("Failed charging account.");
				_player.sendMessage(F.main("Amplifier", "There was an error charging your account. Try again later!"));
			}
		});
	}
}
