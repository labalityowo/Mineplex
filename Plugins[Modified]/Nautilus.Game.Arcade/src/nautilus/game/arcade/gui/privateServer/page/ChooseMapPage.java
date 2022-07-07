package nautilus.game.arcade.gui.privateServer.page;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.shop.item.ShopItem;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.gui.privateServer.PrivateServerShop;
import nautilus.game.arcade.gui.privateServer.button.ChooseMapButton;

public class ChooseMapPage extends BasePage
{
	private GameType _gameType;

	public ChooseMapPage(ArcadeManager plugin, PrivateServerShop shop, Player player, GameType gameType)
	{
		super(plugin, shop, "Choose Map", player);
		_gameType = gameType;

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		addBackToSetGamePage();

		int slot = 9;
		String loadMaps = _gameType.getName();
		if(_gameType.getMapSource() != null)
		{
			loadMaps = _gameType.getMapSource()[0].getName();
		}
		for(String cur : getPlugin().LoadFiles(loadMaps))
		{
			String name = cur.contains("_") ? cur.split("_")[1] : cur;
			ChooseMapButton btn = new ChooseMapButton(getPlugin(), getShop(), _gameType, cur);
			addButton(slot, new ShopItem(Material.PAPER, name, new String[]{"§7Click to select map."}, 1, false), btn);
			slot++;
		}
	}
}
