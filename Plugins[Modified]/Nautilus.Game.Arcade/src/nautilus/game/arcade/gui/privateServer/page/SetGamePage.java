package nautilus.game.arcade.gui.privateServer.page;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.common.util.C;
import mineplex.core.shop.item.ShopItem;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.gui.privateServer.PrivateServerShop;

public class SetGamePage extends BasePage
{

	private int _page;

	public SetGamePage(ArcadeManager plugin, PrivateServerShop shop, Player player)
	{
		super(plugin, shop, "Set Game", player);
		buildPage();
	}

	@Override
	protected void buildPage()
	{
		addBackButton(4);

		int gameSlot = 9;
		int gameIndex = _page * GAMES_PER_PAGE;
		List<GameType> types = new ArrayList<>(_manager.getGames());
		types = types.subList(gameIndex, Math.min(types.size(), (_page + 1) * GAMES_PER_PAGE));

		for (GameType type : types)
		{
			addGameButton(gameSlot++, type);
		}

		if (_page > 0)
		{
			addButton(45, PREVIOUS_PAGE, (player, clickType) ->
			{
				_page--;
				refresh();
			});
		}

		if (types.size() == GAMES_PER_PAGE)
		{
			addButton(53, NEXT_PAGE, (player, clickType) ->
			{
				_page++;
				refresh();
			});
		}
	}

	private void addGameButton(int slot, final GameType type)
	{
		List<String> lore = new ArrayList<>();
		lore.add(ChatColor.RESET + C.cGray + "Make this next Game Type");
		lore.add("§1");
		if (type == GameType.UHC || type == GameType.UHCSolo || type == GameType.UHCSoloSpeed || type == GameType.UHCTeamsSpeed)
		{
			lore.add(ChatColor.YELLOW + "Left-Click " + C.cGray + "to select");
		}
		else
		{
			lore.add(ChatColor.YELLOW + "Left-Click " + C.cGray + "for a §fRandom Map§7.");
			lore.add(ChatColor.YELLOW + "Right-Click " + C.cGray + "to §fChoose Map§7.");
		}
		if (_manager.hasWarning().contains(type))
		{
			lore.add("§2");
			lore.add("§c§lWARNING: §fThis game was rejected!");
		}

		ShopItem shopItem = new ShopItem(type.GetMaterial(), type.GetMaterialData(), type.getName(), lore.toArray(new String[lore.size()]), 1, false, false);
		addButton(slot, shopItem, (player, clickType) ->
		{
			if (clickType == ClickType.LEFT)
			{
				getPlugin().GetGame().setGame(type, player, true);
				player.closeInventory();
			}
			else if (clickType == ClickType.RIGHT)
			{
				//UHC has auto generating maps.
				if (type == GameType.UHC)
				{
					return;
				}
				getShop().openPageForPlayer(player, new ChooseMapPage(getPlugin(), getShop(), player, type));
			}
		});
	}
}
