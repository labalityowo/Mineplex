package mineplex.staffServer.ui;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.bukkit.entity.Player;

import com.google.gson.Gson;

import mineplex.core.account.CoreClient;
import mineplex.core.bonuses.BonusClientData;
import mineplex.core.common.util.UtilServer;
import mineplex.core.pet.PetClient;
import mineplex.core.pet.repository.token.ClientPetTokenWrapper;
import mineplex.core.powerplayclub.PowerPlayData;
import mineplex.core.shop.ShopBase;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.staffServer.LinkedTemporaryItem;
import mineplex.staffServer.customerSupport.CustomerSupport;
import mineplex.staffServer.repository.BonusEntry;

public class SupportShop extends ShopBase<CustomerSupport>
{
	private LinkedTemporaryItem<Integer, List<BonusEntry>> _bonusLog;
	private LinkedTemporaryItem<Integer, PowerPlayData> _powerPlayData;
	private LinkedTemporaryItem<Integer, PetClient> _petClients;
	private LinkedTemporaryItem<Integer, BonusClientData> _bonusData;

	public SupportShop(CustomerSupport plugin)
	{
		super(plugin, plugin.getClientManager(), plugin.getDonationManager(), "Support");

		_bonusLog = new LinkedTemporaryItem<>();
		_powerPlayData = new LinkedTemporaryItem<>();
		_petClients = new LinkedTemporaryItem<>();
		_bonusData = new LinkedTemporaryItem<>();
	}

	public void handleOpen(Player caller, CoreClient target)
	{
		// Who cares if they fail to load, the barrier
		// button will just be there since the HomePage
		// knows not to put it in.
		UtilServer.runAsync(() ->
				loadBonusLog(caller, target.getAccountId(), (bS) ->
						loadPowerPlay(caller, target.getAccountId(), (ppcS) ->
								loadPetClient(caller, target, (petS) ->
										loadBonusData(caller, target.getAccountId(), (bonusS) ->
												UtilServer.runSync(() -> openPageForPlayer(caller, new SupportHomePage(getPlugin(), this, caller, target)))
										)
								)
						)
				)
		);
	}

	public void loadBonusLog(Player caller, int accountId, Consumer<Boolean> callback)
	{
		getPlugin().getRepository().loadBonusLog(accountId, resultSet ->
		{
			try
			{
				List<BonusEntry> bonusEntries = new LinkedList<>();

				while (resultSet.next())
				{
					// 1: accountId
					// 2: itemName
					// 3: itemChange (int)
					// 4: time
					bonusEntries.add(new BonusEntry(
							resultSet.getInt(1),
							resultSet.getString(2),
							resultSet.getInt(3),
							resultSet.getDate(4))
					);
				}

				_bonusLog.put(caller, accountId, bonusEntries);
				callback.accept(true);
			} catch (SQLException e)
			{
				e.printStackTrace();
				callback.accept(false);
			}
		});
	}

	public void loadPowerPlay(Player caller, int accountId, Consumer<Boolean> callback)
	{
		getPlugin().getPowerPlayRepo().loadData(accountId).thenAccept((PowerPlayData data) ->
		{
			if (data == null)
			{
				callback.accept(false);
				return;
			}

			_powerPlayData.put(caller, accountId, data);
			callback.accept(true);
		});
	}

	public void loadPetClient(Player caller, CoreClient client, Consumer<Boolean> callback)
	{
		Gson gson = new Gson();
		ClientPetTokenWrapper token;
		String response = getPlugin().getClientManager().getRepository().getClientByUUID(client.getUniqueId());
		token = gson.fromJson(response, ClientPetTokenWrapper.class);

		PetClient petClient = new PetClient();
		petClient.load(token.DonorToken);

		_petClients.put(caller, client.getAccountId(), petClient);
		callback.accept(true);
	}

	public void loadBonusData(Player caller, int accountId, Consumer<Boolean> callback)
	{
		getPlugin().getBonusRepository().getClientData(accountId, (data) ->
		{
			if (data == null)
			{
				callback.accept(false);
				return;
			}

			_bonusData.put(caller, accountId, data);
			callback.accept(true);
		});
	}

	public Map<Integer, List<BonusEntry>> getBonusLog() { return _bonusLog.getPrimaryMap(); }

	public Map<Integer, PowerPlayData> getPowerPlayData() { return _powerPlayData.getPrimaryMap(); }

	public Map<Integer, PetClient> getPetClients() { return _petClients.getPrimaryMap(); }

	public Map<Integer, BonusClientData> getBonusData() { return _bonusData.getPrimaryMap(); }

	@Override
	protected void closeShopForPlayer(Player player)
	{
		super.closeShopForPlayer(player);

		_bonusLog.remove(player);
		_powerPlayData.remove(player);
		_petClients.remove(player);
		_bonusData.remove(player);
	}

	@Override
	protected ShopPageBase<CustomerSupport, ? extends ShopBase<CustomerSupport>> buildPagesFor(Player player)
	{
		return null;
	}
}
