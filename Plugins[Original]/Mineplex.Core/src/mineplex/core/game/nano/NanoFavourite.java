package mineplex.core.game.nano;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.entity.Player;

import mineplex.core.MiniDbClientPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.common.util.F;
import mineplex.core.donation.DonationManager;

@ReflectivelyCreateMiniPlugin
public class NanoFavourite extends MiniDbClientPlugin<List<NanoDisplay>>
{

	private static final int MAX_FAVOURITES = 3;

	private final DonationManager _donationManager;

	private final NanoRepository _repository;
	private final NanoShop _shop;

	private NanoFavourite()
	{
		super("Favourite Nano");

		_donationManager = require(DonationManager.class);

		_repository = new NanoRepository();
		_shop = new NanoShop(this);
	}

	@Override
	protected List<NanoDisplay> addPlayer(UUID uuid)
	{
		return new ArrayList<>();
	}

	@Override
	public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
	{
		List<NanoDisplay> games = Get(uuid);

		while (resultSet.next())
		{
			// To fix an issue where players could set more than 3 by being on EU and US at the same time
			if (games.size() >= MAX_FAVOURITES)
			{
				_repository.clearFavourites(accountId);
				games.clear();
				return;
			}

			int gameId = resultSet.getInt("gameId");
			NanoDisplay display = NanoDisplay.getFromId(gameId);

			if (display == null)
			{
				_repository.setFavourite(accountId, gameId, false);
				continue;
			}

			games.add(display);
		}
	}

	@Override
	public String getQuery(int accountId, String uuid, String name)
	{
		return "SELECT gameId FROM accountFavouriteNano WHERE accountId=" + accountId + ";";
	}

	public void setFavourite(Consumer<Boolean> callback, Player player, NanoDisplay display, boolean favourite)
	{
		int accountId = ClientManager.getAccountId(player);

		if (favourite && Get(player).size() >= MAX_FAVOURITES)
		{
			player.sendMessage(F.main(getName(), "You cannot set more than " + F.count(MAX_FAVOURITES) + " games as favorite!"));

			if (callback != null)
			{
				callback.accept(false);
			}
			return;
		}

		runAsync(() ->
		{
			_repository.setFavourite(accountId, display.getGameId(), favourite);

			runSync(() ->
			{
				if (favourite)
				{
					Get(player).add(display);
					player.sendMessage(F.main(getName(), "Added " + F.name(display.getName()) + " to your favorite games list."));
				}
				else
				{
					Get(player).remove(display);
					player.sendMessage(F.main(getName(), "Removed " + F.name(display.getName()) + " from your favorite games list."));
				}

				if (callback != null)
				{
					callback.accept(true);
				}
			});
		});
	}

	public Map<NanoDisplay, Integer> getFavourites()
	{
		Map<NanoDisplay, Integer> favourites = new HashMap<>();

		GetValues().forEach(games -> games.forEach(display -> favourites.put(display, favourites.getOrDefault(display, 0) + 1)));

		return favourites;
	}

	public DonationManager getDonationManager()
	{
		return _donationManager;
	}

	public NanoShop getShop()
	{
		return _shop;
	}
}
