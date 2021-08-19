package nautilus.game.arcade.gui.privateServer.page;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.shop.item.ShopItem;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.gui.privateServer.PrivateServerShop;

public class EditRotationPage extends BasePage
{

	private int _page;

	public EditRotationPage(ArcadeManager plugin, PrivateServerShop shop, Player player)
	{
		super(plugin, shop, "Edit Rotation", player);
		buildPage();
	}

	@Override
	protected void buildPage()
	{
		List<GameType> enabledGames = getPlugin().GetServerConfig().GameList;

		addBackButton(4);

		int gameSlot = 9;
		int gameIndex = _page * GAMES_PER_PAGE;
		List<GameType> types = new ArrayList<>(_manager.getGames());
		types = types.subList(gameIndex, Math.min(types.size(), (_page + 1) * GAMES_PER_PAGE));

		for (GameType type : types)
		{
			addGameButton(gameSlot++, type, enabledGames.contains(type));
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

	private void addGameButton(int slot, final GameType type, boolean enabled)
	{
		String titleString = ChatColor.RESET + (enabled ? C.cGreen : C.cRed) + ChatColor.BOLD + type.getName();
		String infoString = ChatColor.RESET + C.cGray + (enabled ? "Click to Disable" : "Click to Enable");
		String[] lore = new String[]{infoString};
		if (_manager.hasWarning().contains(type))
		{
			lore = new String[]{infoString, "§1", "§c§lWARNING: §fThis game was rejected!"};
		}
		ShopItem shopItem = new ShopItem(type.GetMaterial(), type.GetMaterialData(), titleString, lore, 1, false, false);

		addButton(slot, shopItem, (player, clickType) ->
		{
			String announceString = C.Bold + type.GetLobbyName();

			if (getPlugin().GetServerConfig().GameList.contains(type))
			{
				if (getPlugin().GetServerConfig().GameList.size() > 1)
				{
					getPlugin().GetServerConfig().GameList.remove(type);
					announceString = C.cRed + announceString + " removed from rotation.";
				}
				else
				{
					UtilPlayer.message(getPlayer(), "You must keep at least one game in rotation!");
					getPlayer().playSound(getPlayer().getLocation(), Sound.NOTE_BASS_GUITAR, 1f, 1f);
					announceString = null;
				}
			}
			else
			{
				getPlugin().GetServerConfig().GameList.add(type);
				announceString = C.cGreen + announceString + " added to rotation.";
			}

			getPlugin().GetGame().Announce(announceString);
			refresh();
		});

		if (enabled)
			addGlow(slot);
	}
}
