package mineplex.core.boosters.tips;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.boosters.Booster;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import mineplex.core.thank.ThankManager;
import mineplex.core.thank.ThankResult;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This class handles "thanking" Amplifiers. This is a way of rewarding players for activating Amplifiers.
 *
 * @author Shaun Bennett
 */
public class BoosterThankManager extends MiniPlugin
{
	public static final int TIP_FOR_SPONSOR = 5;
	public static final int TIP_FOR_TIPPER = 5;

	private BoosterThankRepository _repository;
	private CoreClientManager _clientManager;
	private ThankManager _thankManager;

	public BoosterThankManager(JavaPlugin plugin, CoreClientManager clientManager, ThankManager thankManager)
	{
		super("Amplifier Thanks", plugin);

		_clientManager = clientManager;
		_repository = new BoosterThankRepository(plugin);
		_thankManager = thankManager;
	}

	/**
	 * Attempt to "Thank" an Amplifier. This checks with {@link BoosterThankRepository} if a player hasn't already
	 * thanked that Amplifier. If they havent, we proceed to use {@link ThankManager} to send a thank you to the player
	 * who activated that Amplifier.
	 *
	 * @param player The player sending the thanks
	 * @param booster The Amplifier to be thanked
	 * @param callback Callback with the result of sending the thanks
	 */
	public void addTip(Player player, Booster booster, Callback<TipAddResult> callback)
	{
		if (!Recharge.Instance.use(player, "Amplifier Thanks", 1000 * 5, false, false))
		{
			UtilPlayer.message(player, F.main("Amplifier", "Please wait before trying that again"));
			callback.run(TipAddResult.ON_COOLDOWN);
			return;
		}

		int accountId = _clientManager.getAccountId(player);

		// Break out if client manager has a bad account id
		if (accountId == -1)
		{
			callback.run(TipAddResult.INVALID_ACCOUNT_ID);
			return;
		}

		// You can't tip yourself, silly!
		if (accountId == booster.getAccountId())
		{
			callback.run(TipAddResult.CANNOT_TIP_SELF);
			return;
		}

		runAsync(() ->
		{
			if (_repository.checkAmplifierThank(accountId, booster.getId()))
			{
				// We can thank that amplifier!
				_thankManager.thankPlayer(booster.getPlayerName(), booster.getAccountId(), player,
						TIP_FOR_SPONSOR, TIP_FOR_TIPPER, "Amplifier", true, thankResult ->
								runSync(() -> callback.run(fromThankResult(thankResult))));
			}
			else
			{
				runSync(() -> callback.run(TipAddResult.ALREADY_TIPPED_BOOSTER));
			}
		});
	}


	private TipAddResult fromThankResult(ThankResult result)
	{
		return result == ThankResult.SUCCESS ? TipAddResult.SUCCESS : TipAddResult.UNKNOWN_ERROR;
	}
}
