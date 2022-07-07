package mineplex.enjinTranslator;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClient;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.account.permissions.PermissionGroupHelper;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.Callback;
import mineplex.core.donation.DonationManager;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.powerplayclub.PowerPlayClubRepository;
import mineplex.core.punish.Category;
import mineplex.core.punish.Punish;
import mineplex.core.server.util.TransactionResponse;
import mineplex.core.website.WebsiteLinkManager;
import mineplex.enjinTranslator.purchase.PurchaseManager;

public class Enjin extends MiniPlugin implements CommandExecutor
{
	private CoreClientManager _clientManager;
	private DonationManager _donationManager;
	private InventoryManager _inventoryManager;
	private PurchaseManager _purchaseManager;
	private PowerPlayClubRepository _powerPlayClubRepository;
	private Punish _punish;

	private static Object _commandLock = new Object();

	public long _lastPoll = System.currentTimeMillis() - 120000;

	private SimpleDateFormat _dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

	public Enjin(EnjinTranslator plugin, CoreClientManager clientManager, DonationManager donationManager, InventoryManager inventoryManager)
	{
		super("Enjin", plugin);

		_clientManager = clientManager;
		_donationManager = donationManager;
		_inventoryManager = inventoryManager;
		_punish = new Punish(plugin, clientManager);

		_purchaseManager = new PurchaseManager(plugin);
		_powerPlayClubRepository = new PowerPlayClubRepository(plugin, clientManager, donationManager);
		require(WebsiteLinkManager.class);

		plugin.getCommand("enjin_mineplex").setExecutor(this);
		plugin.getCommand("pull").setExecutor(this);
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args)
	{
		synchronized (_commandLock)
		{
			try
			{
				if (sender instanceof Player)
					((Player) sender).kickPlayer("Like bananas? I don't.  Here take these and go have fun.");

				if (label.equalsIgnoreCase("enjin_mineplex"))
				{
					final UUID uuid;
					try
					{
						uuid = UUID.fromString(args[1]);
					}
					catch (IllegalArgumentException e)
					{
						System.out.println("[" + _dateFormat.format(new Date()) + "] ERROR processing " + args[1] + "; invalid UUID.");
						return true;
					}

					_clientManager.loadClientByUUID(uuid, client ->
					{
						if (client == null)
						{
							System.out.println("[" + _dateFormat.format(new Date()) + "] ERROR processing " + uuid + ", isn't in our database.");
						}
						else
						{
							final String name = client.getName();

							if (args[0].equalsIgnoreCase("chargeback"))
							{
								_punish.AddPunishment(name, Category.Other, "Chargeback", "Strutt20", 1, true, -1, true);
								System.out.println("[" + _dateFormat.format(new Date()) + "] " + name + " was banned for charging back!");
								return;
							}

							if (!checkForClansPurchase(args, name, client))
							{
								if (!checkForBoosterPurchase(args, name, uuid, client))
								{
									if (!checkForCoinPurchase(args, name, uuid, client))
									{
										if (!checkForRankPurchase(args, name, uuid, client))
										{
											if (!checkForPurchase(args, name, client))
											{
												if (!checkForPowerPlayClub(args, name, uuid, client))
												{
													StringBuilder sb = new StringBuilder();

													for (String arg : args)
													{
														sb.append(arg + " ");
													}

													System.out.println("Received Command : " + sb.toString());
												}
											}
										}
									}
								}
							}
						}
					});
				}
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}

			try
			{
				Thread.sleep(20);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}

		return true;
	}

	protected boolean checkForRankPurchase(String[] args, final String name, final UUID playerUUID, final CoreClient client)
	{
		if (args.length != 3 || !args[0].equalsIgnoreCase("rank"))
		{
			return false;
		}

		PermissionGroup c = PermissionGroup.getGroup(args[2]).orElse(PermissionGroupHelper.getGroupFromLegacy(args[2]));
		if (c == null)
		{
			return false;
		}
		final PermissionGroup rank = PermissionGroup.valueOf(args[2]);

		_clientManager.loadClientByName(name, loadedClient ->
		{
			if (rank == PermissionGroup.PLAYER || loadedClient.getPrimaryGroup() == PermissionGroup.PLAYER || !loadedClient.getPrimaryGroup().inheritsFrom(rank))
			{
				_clientManager.setPrimaryGroup(client.getAccountId(), rank, () -> _purchaseManager.addAccountPurchaseToQueue(client.getAccountId(), rank.name() + "Permanent", 1, true));

				System.out.println("[" + _dateFormat.format(new Date()) + "] " + name + " received " + rank.name() + " " + "permanently.");
			}
			else
			{
				System.out.println("[" + _dateFormat.format(new Date()) + "] " + name + " DENIED INFERIOR " + rank.name() + " " + "permanently.");
				_purchaseManager.addAccountPurchaseToQueue(client.getAccountId(), rank.name() + " Permanent", 1, false);
			}
		});

		return true;
	}

	protected boolean checkForPurchase(String[] args, final String name, final CoreClient client)
	{
		if (args.length < 3 || !args[0].equalsIgnoreCase("purchase"))
			return false;

		final int amount = Integer.parseInt(args[2]);
		String tempName = args[4];

		for (int i = 5; i < args.length; i++)
		{
			tempName += " " + args[i];
		}

		final String packageName = tempName;

		_donationManager.purchaseUnknownSalesPackage(client, amount == 1 ? packageName : packageName + " " + amount, GlobalCurrency.GEM, 0, false, data ->
		{
			if (data == TransactionResponse.Success)
			{
				_inventoryManager.addItemToInventoryForOffline(new Callback<Boolean>()
				{
					public void run(Boolean success)
					{
						if (success)
						{
							_purchaseManager.addAccountPurchaseToQueue(client.getAccountId(), packageName, amount, true);
							System.out.println("[" + _dateFormat.format(new Date()) + "] " + name + " received " + amount + " " + packageName + ".");
						}
						else
						{
							System.out.println("[" + _dateFormat.format(new Date()) + "] ERROR processing " + name + " " + packageName + ".  Queuing for run later.");
							_purchaseManager.addAccountPurchaseToQueue(client.getAccountId(), packageName, amount, false);
						}
					}
				}, client.getAccountId(), packageName, amount);
			}
			else
			{
				System.out.println("[" + _dateFormat.format(new Date()) + "] ERROR processing " + name + " " + amount + ".  Queuing for run later.");
				_purchaseManager.addAccountPurchaseToQueue(client.getAccountId(), packageName, amount, false);
			}
		});

		return true;
	}

	protected boolean checkForCoinPurchase(String[] args, final String name, final UUID playerUUID, final CoreClient client)
	{
		if (args.length != 3 || !args[0].equalsIgnoreCase("coin"))
			return false;

		final int amount = Integer.parseInt(args[2]);

		_donationManager.rewardCurrency(GlobalCurrency.TREASURE_SHARD, client, "purchase", amount, response ->
		{
			if (response)
				System.out.println("[" + _dateFormat.format(new Date()) + "] " + name + " received " + amount + " coins.");
			else
				System.out.println("[" + _dateFormat.format(new Date()) + "] ERROR processing " + name + " " + amount + " coins.  Queuing for run later.");

			_purchaseManager.addAccountPurchaseToQueue(client.getAccountId(), "Coins", amount, response);
		});

		return true;
	}

	protected boolean checkForBoosterPurchase(String[] args, final String name, final UUID playerUUID, final CoreClient client)
	{
		if (args.length != 3 || !args[0].equalsIgnoreCase("booster"))
			return false;

		final int amount = Integer.parseInt(args[2]);

		_inventoryManager.addItemToInventoryForOffline(new Callback<Boolean>()
		{
			public void run(Boolean response)
			{
				if (response)
					System.out.println("[" + _dateFormat.format(new Date()) + "] " + name + " received " + amount + " gem boosters.");
				else
					System.out.println("[" + _dateFormat.format(new Date()) + "] ERROR processing " + name + " " + amount + " gem boosters.  Queuing for run later.");

				_purchaseManager.addAccountPurchaseToQueue(client.getAccountId(), "Gem Boosters", amount, response);
			}
		}, client.getAccountId(), "Game Booster", amount);

		return true;
	}

	protected boolean checkForPowerPlayClub(String[] args, final String name, final UUID playerUUID, final CoreClient client)
	{
		if (args.length < 5 || !args[0].equalsIgnoreCase("powerplayclub"))
			return false;

		if (args[2].equalsIgnoreCase("add"))
		{
			boolean bc = args.length > 5 && args[5].equalsIgnoreCase("bc");
			String[] splitDate = args[3].split("/");
			LocalDate date;
			if (bc)
			{
				date = LocalDate.of(2000 + Integer.parseInt(splitDate[2]), Integer.parseInt(splitDate[1]), Integer.parseInt(splitDate[0]));
			}
			else
			{
				date = LocalDate.of(Integer.parseInt(splitDate[2]), Integer.parseInt(splitDate[0]), Integer.parseInt(splitDate[1]));
			}

			String duration = args[4];

			_powerPlayClubRepository.addSubscription(client.getAccountId(), date, duration);

		}
		else if (args[2].equalsIgnoreCase("cancel"))
		{
			// TODO: cancel it in our logs? I don't think this is necessary.
		}

		return false;
	}

	protected boolean checkForClansPurchase(String[] args, final String name, final CoreClient client)
	{
		if (args.length >= 3 && args[0].equalsIgnoreCase("clansBanner"))
		{
			String purchase = "Clan Banner Usage";
			if (args[2].equalsIgnoreCase("true"))
			{
				purchase = "Clan Banner Editor";
			}
			final String packageName = purchase;

			_donationManager.purchaseUnknownSalesPackage(client, packageName, GlobalCurrency.GEM, 0, false, data ->
			{
				if (data == TransactionResponse.Success)
				{
					_inventoryManager.addItemToInventoryForOffline(new Callback<Boolean>()
					{
						public void run(Boolean success)
						{
							if (success)
							{
								_purchaseManager.addAccountPurchaseToQueue(client.getAccountId(), packageName, 1, true);
								System.out.println("[" + _dateFormat.format(new Date()) + "] " + name + " received their " + packageName + " access.");
							}
							else
							{
								System.out.println("[" + _dateFormat.format(new Date()) + "] ERROR processing " + name + " " + packageName + ".  Queuing for run later.");
								_purchaseManager.addAccountPurchaseToQueue(client.getAccountId(), packageName, 1, false);
							}
						}
					}, client.getAccountId(), packageName, 1);
				}
				else
				{
					System.out.println("[" + _dateFormat.format(new Date()) + "] ERROR processing " + name + " 1" + ".  Queuing for run later.");
					_purchaseManager.addAccountPurchaseToQueue(client.getAccountId(), packageName, 1, false);
				}
			});
			//enjin_mineplex clansBanner AlexTheCoder true
			return true;
		}
		if (args.length >= 4 && args[0].equalsIgnoreCase("clansAmplifier"))
		{
			//enjin_mineplex clansAmplifier AlexTheCoder 20 1
			final String item = "Rune Amplifier " + args[2];
			final int amount = Integer.parseInt(args[3]);

			_inventoryManager.addItemToInventoryForOffline(new Callback<Boolean>()
			{
				public void run(Boolean response)
				{
					if (response)
						System.out.println("[" + _dateFormat.format(new Date()) + "] " + name + " received " + amount + " rune amplifiers.");
					else
						System.out.println("[" + _dateFormat.format(new Date()) + "] ERROR processing " + name + " " + amount + " rune amplifiers.  Queuing for run later.");

					_purchaseManager.addAccountPurchaseToQueue(client.getAccountId(), item, amount, response);
				}
			}, client.getAccountId(), item, amount);
		}

		return false;
	}
}
