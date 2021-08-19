package nautilus.game.arcade.game.games.cakewars.ui;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilText;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.mission.MissionTrackerType;
import mineplex.core.recharge.Recharge;
import mineplex.core.shop.item.IButton;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.cakewars.shop.CakeItem;
import nautilus.game.arcade.game.games.cakewars.shop.CakeResource;
import nautilus.game.arcade.game.games.cakewars.shop.CakeTeamItem;
import nautilus.game.arcade.game.games.cakewars.team.CakeTeam;

public class CakeResourceStarPage extends CakeResourcePage
{

	private final CakeTeam _cakeTeam;

	public CakeResourceStarPage(ArcadeManager plugin, CakeResourceShop shop, Player player, List<CakeItem> items)
	{
		super(plugin, shop, player, 27, CakeResource.STAR, items);

		_cakeTeam = _game.getCakeTeamModule().getCakeTeam(_game.GetTeam(player));
	}

	@Override
	protected void buildPage()
	{
		addCloseButton();

		int slot = 10;

		for (CakeItem item : _items)
		{
			CakeTeamItem teamItem = (CakeTeamItem) item;
			CakeShopResult result = getResultPurchase(teamItem);

			addButton(slot, prepareItem(item, result), new CakeTeamItemButton(teamItem));

			if (++slot % 9 == 8)
			{
				slot += 2;
			}
		}
	}

	@Override
	protected void buildMulti()
	{

	}

	@Override
	CakeShopResult getResultPurchase(CakeItem item)
	{
		CakeTeamItem teamItem = (CakeTeamItem) item;
		int level = _cakeTeam.getUpgrades().get(item);
		ItemStack itemStack = _resource.getItemStack();

		if (level == teamItem.getLevels().length)
		{
			return CakeShopResult.MAX_TIER;
		}
		else if (!UtilInv.contains(_player, null, itemStack.getType(), itemStack.getData().getData(), teamItem.getLevels()[level].getRight(), false, false, false))
		{
			return CakeShopResult.NOT_ENOUGH_RESOURCES;
		}

		return CakeShopResult.SUCCESSFUL;
	}

	@Override
	ItemStack prepareItem(CakeItem item, CakeShopResult result)
	{
		CakeTeamItem teamItem = (CakeTeamItem) item;
		ItemBuilder builder = new ItemBuilder(item.getItemStack());
		int level = _cakeTeam.getUpgrades().get(item);
		String name = C.mItem + teamItem.getName();
		boolean maxTier = result == CakeShopResult.MAX_TIER;

		if (!maxTier)
		{
			name += " " + UtilText.toRomanNumeral(level + 1);
		}

		builder.setTitle(name);

		if (maxTier)
		{
			builder.addLore("", C.cRed + result.getColour() + result.getFeedback());
		}
		else
		{
			builder.addLore("");
			builder.addLore(teamItem.getDescription(level));

			builder.addLore(
					"",
					"Cost: " + _resource.getChatColor() + teamItem.getLevels()[level].getRight() + " " + _resource.getName() + "s",
					"",
					result.getColour() + result.getFeedback()
			);

		}

		return builder.build();
	}

	private class CakeTeamItemButton implements IButton
	{

		private final CakeTeamItem _item;
		private final int _level;

		CakeTeamItemButton(CakeTeamItem item)
		{
			_item = item;
			_level = _cakeTeam.getUpgrades().get(item);
		}

		@Override
		public void onClick(Player player, ClickType clickType)
		{
			if (!Recharge.Instance.use(player, "Buy Team Upgrade", 250, false, false))
			{
				return;
			}

			CakeShopResult result = getResultPurchase(_item);

			if (result != CakeShopResult.SUCCESSFUL)
			{
				player.sendMessage(F.main("Game", result.getFeedback()));
				playDenySound(player);
				return;
			}

			ItemStack resource = _resource.getItemStack();
			UtilInv.remove(player, resource.getType(), resource.getData().getData(), _item.getLevels()[_level].getRight());

			int newLevel = _level + 1;
			String name = F.name(_item.getName() + " " + UtilText.toRomanNumeral(newLevel));
			String message = F.main("Game", F.color(_player.getName(), _team.GetColor().toString()) + " purchased the " + name + " team upgrade.");

			for (Player other : _team.GetPlayers(false))
			{
				other.playSound(other.getLocation(), Sound.NOTE_PLING, 1, 0.6F);
				other.sendMessage(message);
			}

			_cakeTeam.getUpgrades().put(_item, newLevel);

			boolean ownsAll = true;

			for (Entry<CakeTeamItem, Integer> entry : _cakeTeam.getUpgrades().entrySet())
			{
				if (entry.getValue() != entry.getKey().getLevels().length)
				{
					ownsAll = false;
					break;
				}
			}

			if (ownsAll)
			{
				_team.GetPlayers(false).forEach(other ->
				{
					if (other.isOnline())
					{
						_game.AddStat(player, "BuyAll", 1, true, false);
					}
				});
			}

			_game.getArcadeManager().getMissionsManager().incrementProgress(player, _item.getLevels()[_level].getRight(), MissionTrackerType.CW_SPEND_RESOURCE, _game.GetType().getDisplay(), _resource.getChatColor());

			playAcceptSound(player);
			getShop().getPageMap().values().forEach(page ->
			{
				if (page.getName().equals(getName()))
				{
					page.refresh();
				}
			});
		}
	}
}
