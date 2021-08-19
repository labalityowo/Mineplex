package mineplex.core.boosters.gui;

import mineplex.core.account.CoreClientManager;
import mineplex.core.boosters.Booster;
import mineplex.core.boosters.BoosterManager;
import mineplex.core.boosters.BoosterProcessor;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilTime;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.confirmation.ConfirmationPage;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.shop.page.ShopPageBase;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Shaun Bennett
 */
public class BoosterPage extends ShopPageBase<BoosterManager, BoosterShop>
{
	public BoosterPage(BoosterManager plugin, BoosterShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player)
	{
		super(plugin, shop, clientManager, donationManager, "Game Amplifiers", player, 45);

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		ArrayList<String> lore = new ArrayList<>();

		int amplifierCount = getPlugin().getAvailableBoosterCount(getPlayer());
		lore.add(" ");
		lore.add(C.cWhite + "You own " + C.cGreen + amplifierCount + C.cWhite +  " Game Amplifiers");
		if (getPlugin().canActivateBoosters() && amplifierCount > 0)
		{
			List<Booster> boosters = getPlugin().getBoosters();

			long waitTime = getPlugin().getBoostTime();
			if (waitTime == 0)
			{
				lore.add(C.cWhite + "Amplifier would activate " + C.cGreen + "now");
				lore.add(" ");
				lore.add(C.cGray + "Once this Amplifier is activated");
				lore.add(C.cGray + "or queued you are not able to");
				lore.add(C.cGray + "cancel or refund it. You will still");
				lore.add(C.cGray + "earn rewards if you are offline.");
				lore.add(" ");
				lore.add(C.cWhite + "Click to Activate Amplifier");
			}
			else
			{
				lore.add(" ");
				lore.add(C.cWhite + "Amplifier would activate in " + C.cGreen + UtilTime.convertColonString(waitTime, UtilTime.TimeUnit.HOURS, UtilTime.TimeUnit.SECONDS));
				if (boosters.size() - 1 == 1)
				{
					lore.add(C.cWhite + "There is " + C.cGreen + 1 + C.cWhite + " Amplifier queued");
				}
				else if (boosters.size() - 1 > 0)
				{
					lore.add(C.cWhite + "There are " + C.cGreen + (boosters.size() - 1) + C.cWhite + " Amplifiers queued");
				}
				lore.add(" ");
				lore.add(C.cGray + "Once this Amplifier is activated");
				lore.add(C.cGray + "or queued you are not able to");
				lore.add(C.cGray + "cancel or refund it. You will still");
				lore.add(C.cGray + "earn rewards if you are offline.");
				lore.add(" ");
				lore.add(C.cWhite + "Click to Queue Amplifier");
			}
		}
		else
		{
			lore.add(" ");
			lore.add(C.cGray + "Game Amplifiers allow you to");
			lore.add(C.cGray + "increase the shards");
			lore.add(C.cGray + "earned in that game for 1 hour.");
			lore.add(C.cGray + "You will also earn bonus rewards");
			lore.add(C.cGray + "from players thanking you while");
			lore.add(C.cGray + "your amplifier is active.");
			lore.add(" ");
			lore.add(C.cWhite + "Get Amplifiers at " + C.cGreen + "mineplex.com/shop");
		}

		ShopItem booster = new ShopItem(Material.SUGAR, "Game Amplifier", lore.toArray(new String[0]), 0, false, false);
		if (getPlugin().canActivateBoosters() && amplifierCount > 0)
		{
			addButton(13, booster, this::openConfirmation);
		}
		else
		{
			setItem(13, booster);
		}

		addBoosterQueue();
//		addOtherBoosters();
	}

	private void openConfirmation(Player player, ClickType type)
	{
		ArrayList<String> lore = new ArrayList<>();
		lore.add(" ");
		lore.add(C.cGray + "Once this Amplifier is activated");
		lore.add(C.cGray + "or queued you are not able to");
		lore.add(C.cGray + "cancel or refund it. You will still");
		lore.add(C.cGray + "earn rewards if you are offline.");
		ShopItem booster = new ShopItem(Material.SUGAR, "Game Amplifier", lore.toArray(new String[0]), 0, false, false);

		BoosterProcessor processor = new BoosterProcessor(getPlugin(), getPlayer());
		ConfirmationPage<BoosterManager, BoosterShop> page = new ConfirmationPage<>(getPlayer(), this, processor, booster);
		getShop().openPageForPlayer(getPlayer(), page);
	}

	private void addBoosterQueue()
	{
		if (getPlugin().getBoosters() == null)
			return;

		List<Booster> boosters = getPlugin().getBoosters();
		int startIndex = Math.max(0, (9 - boosters.size()) / 2);
		for (int i = 0; i < boosters.size() && i < 18; i++)
		{
			int slot = startIndex + 27 + i;
			Booster booster = boosters.get(i);
			boolean active = booster.isActive();
			int queueIndex = Math.max(1, i);
			boolean owns = getPlayer().getUniqueId().equals(booster.getUuid());

			long timeActivatedDif = System.currentTimeMillis() - booster.getActivationTime().getTime();
			String activationTime = UtilTime.convertString(timeActivatedDif, 0, UtilTime.TimeUnit.FIT);

			List<String> lore = new ArrayList<>();
			if (active)
			{
				lore.add(C.cWhite + "Active");
				lore.add(" ");
				String expireTime = UtilTime.convertColonString(booster.getTimeRemaining(), UtilTime.TimeUnit.MINUTES, UtilTime.TimeUnit.SECONDS);
				lore.add(C.cWhite + "Added by " + C.cGreen + booster.getPlayerName());
				lore.add(C.cWhite + "Expires in " + C.cGreen + expireTime);
			}
			else
			{
				long timeToActive = booster.getStartTime().getTime() - System.currentTimeMillis();
				String activeString = UtilTime.convertColonString(timeToActive, UtilTime.TimeUnit.HOURS, UtilTime.TimeUnit.SECONDS);

				lore.add(" ");
				lore.add(C.cWhite + "Added by " + C.cGreen + booster.getPlayerName());
				lore.add(C.cWhite + "Starts in " + C.cGreen + activeString);
//				lore.add(C.cWhite + "Position " + C.cGreen + queueIndex + C.cWhite + " in queue");
			}

			lore.add(" ");
			lore.add(C.cGray + "Added " + activationTime + " ago");

			ShopItem shopItem = new ShopItem(booster.isActive() ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK,
					"Game Amplifier", lore.toArray(new String[0]), queueIndex, !active, false);
			setItem(slot, shopItem);

			// Add glow if the booster belongs to you
			if (owns)
			{
				addGlow(slot);
			}
		}
	}

	private void addOtherBoosters()
	{
		Map<String, List<Booster>> boosterMap = getPlugin().getBoosterCache();
		List<Triple<Integer, String, Booster>> tripleList = new ArrayList<>();
		for (Map.Entry<String, List<Booster>> entry : boosterMap.entrySet())
		{
			String boosterGroup = entry.getKey();
			// dont display boosters for the current booster group
			if (boosterGroup.equals(getPlugin().getBoosterGroup()))
				continue;

			List<Booster> boosters = entry.getValue();
			for (int i = 0; i < boosters.size(); i++)
			{
				Booster booster = boosters.get(i);
				if (booster.getUuid().equals(getPlayer().getUniqueId()))
				{
					tripleList.add(Triple.of(i, boosterGroup, booster));
				}
			}
		}


		int startIndex = Math.max(0, (9 - tripleList.size()) / 2);
		for (int i = 0; i < 9 && i < tripleList.size(); i++)
		{
			Triple<Integer, String, Booster> triple = tripleList.get(i);
			int deliveryAmount = Math.max(1, triple.getLeft());
			String boosterGroup = triple.getMiddle();
			Booster booster = triple.getRight();
			long timeActivatedDif = System.currentTimeMillis() - booster.getActivationTime().getTime();
			String activationTime = UtilTime.convertString(timeActivatedDif, 2, UtilTime.TimeUnit.FIT);

			List<String> lore = new ArrayList<String>();
			lore.add(" ");
			lore.add(C.cWhite + "Server: " + C.cGreen + boosterGroup);
			if (booster.isActive())
			{
				lore.add(C.cWhite + "Expires in " + C.cGreen + UtilTime.convertColonString(booster.getTimeRemaining(), UtilTime.TimeUnit.MINUTES, UtilTime.TimeUnit.SECONDS));
			}
			else
			{
				long timeToActive = booster.getStartTime().getTime() - System.currentTimeMillis();
				lore.add(C.cWhite + "Starts in " + C.cGreen + UtilTime.convertColonString(timeToActive, UtilTime.TimeUnit.HOURS, UtilTime.TimeUnit.SECONDS));
			}

			lore.add(" ");
			lore.add(C.cGray + "Added " + activationTime + " ago");

			ShopItem shopItem = new ShopItem(Material.GOLD_BLOCK,
					"Game Amplifier", lore.toArray(new String[0]), 1, false, false);
			setItem(startIndex + i + 27, shopItem);
		}
	}
}
