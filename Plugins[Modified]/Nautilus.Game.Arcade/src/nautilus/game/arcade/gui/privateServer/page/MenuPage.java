package nautilus.game.arcade.gui.privateServer.page;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.item.ShopItem;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.gui.privateServer.PrivateServerShop;
import nautilus.game.arcade.gui.privateServer.button.BanButton;
import nautilus.game.arcade.gui.privateServer.button.EditRotationButton;
import nautilus.game.arcade.gui.privateServer.button.GameVotingButton;
import nautilus.game.arcade.gui.privateServer.button.GiveAdminButton;
import nautilus.game.arcade.gui.privateServer.button.KillButton;
import nautilus.game.arcade.gui.privateServer.button.OptionsButton;
import nautilus.game.arcade.gui.privateServer.button.PlayerHeadButton;
import nautilus.game.arcade.gui.privateServer.button.RemoveAdminButton;
import nautilus.game.arcade.gui.privateServer.button.SetGameButton;
import nautilus.game.arcade.gui.privateServer.button.StartGameButton;
import nautilus.game.arcade.gui.privateServer.button.StopGameButton;
import nautilus.game.arcade.gui.privateServer.button.UnbanButton;
import nautilus.game.arcade.gui.privateServer.button.WhitelistButton;

public class MenuPage extends BasePage
{
	public MenuPage(ArcadeManager plugin, PrivateServerShop shop, Player player)
	{
		super(plugin, shop, "Private Server Menu", player, 9*4);
		buildPage();
	}

	@Override
	protected void buildPage()
	{
		addButton(4, getOwnerHead(), new PlayerHeadButton(getPlugin(), this));

		boolean host = _manager.isHost(getPlayer());
		boolean disableChangeButtons = !getClientManager().Get(getPlayer()).hasPermission(ArcadeManager.Perm.USE_MENU_DURING_GAME) && getPlugin().isGameInProgress();

		{
			// Change Buttons - If a game is in progress and user isn't Youtube+ rank, they cannot
			// make changes until the game has completed.

			int startSlot = host ? 9 : 1 + 9;
			int stopSlot = host ? 18 : 1 + 18;
			int banSlot = host ? 6 + 9 : 7 + 9;
			int setGameSlot = host ? 2 + 9 : 3 + 9;

			String[] lore = disableChangeButtons ? new String[]{ChatColor.RESET + C.cRed + "Game in Progress. Please Wait"} : new String[]{};
			ShopItem startItem = new ShopItem(Material.EMERALD_BLOCK, "Start Game", lore, 1, false);
			ShopItem stopItem = new ShopItem(Material.REDSTONE_BLOCK, "Stop Game", lore, 1, false);
			ShopItem banItem = new ShopItem(Material.LAVA_BUCKET, "Remove Player", lore, 1, false);
			ShopItem setGameItem = new ShopItem(Material.BOOK_AND_QUILL, "Set Game", lore, 1, false);


			if (disableChangeButtons)
			{
				addItem(startSlot, startItem);
				addItem(stopSlot, stopItem);
				addItem(banSlot, banItem);
				addItem(setGameSlot, setGameItem);
			}
			else
			{
				StartGameButton startGameButton = new StartGameButton(getPlugin());
				addButton(startSlot, startItem, startGameButton);

				StopGameButton stopGameButton = new StopGameButton(getPlugin());
				addButton(stopSlot, stopItem, stopGameButton);

				BanButton banButton = new BanButton(getPlugin(), getShop());
				addButton(banSlot, banItem, banButton);

				SetGameButton setGameButton = new SetGameButton(getPlugin(), getShop());
				addButton(setGameSlot, setGameItem, setGameButton);
			}
		}

		EditRotationButton editRotationButton = new EditRotationButton(getPlugin(), getShop());
		addButton(host ? 2 + 18 : 3 + 18, new ShopItem(Material.BOOK, "Edit Game Rotation", new String[]{}, 1, false), editRotationButton);

		if (host)
		{
			GiveAdminButton giveAdminButton = new GiveAdminButton(getPlugin(), getShop());
			addButton(4 + 9, new ShopItem(Material.DIAMOND_SWORD, "Give Co-Host", new String[]{}, 1, false), giveAdminButton);

			RemoveAdminButton removeAdminButton = new RemoveAdminButton(getPlugin(), getShop());
			addButton(4 + 18, new ShopItem(Material.GOLD_SWORD, "Remove Co-Host", new String[]{}, 1, false), removeAdminButton);

			KillButton killButton = new KillButton(getPlugin());
			addButton(8 + 18, new ShopItem(Material.TNT, "Kill Private Server",
					new String[]{ChatColor.RESET + C.cGray + "Shift-Right Click to Kill Private Server"}, 1, false), killButton);

			GameVotingButton votingButton = new GameVotingButton(getPlugin(), getShop());
			addButton(3 + 27, new ShopItem(Material.BOOKSHELF, "Game Voting", new String[]{}, 1, false), votingButton);
			
			if (!_manager.isCommunityServer())
			{
				WhitelistButton whitelistButton = new WhitelistButton(getPlugin(), getShop());
				addButton(5 + 27, new ShopItem(Material.PAPER, "Whitelisted Players", new String[]{}, 1, false), whitelistButton);
			}
		}

		OptionsButton optionsButton = new OptionsButton(getPlugin(), getShop());
		addButton(host ? 8 + 9 : 5 + 9, new ShopItem(Material.REDSTONE_COMPARATOR, "Game Options", new String[]{}, 1, false), optionsButton);

		UnbanButton unbanButton = new UnbanButton(getPlugin(), getShop());
		addButton(host ? 6 + 18 : 7 + 18, new ShopItem(Material.WATER_BUCKET, "Un-Remove Player", new String[]{}, 1, false), unbanButton);
	}

	private ShopItem getOwnerHead()
	{
		if (_manager.isCommunityServer())
		{
			String title = C.cGreen + C.Bold + _manager.getOwner().getName() + "'s Mineplex Community Server";
			ItemStack head = new ItemBuilder(new ItemStack(_manager.getOwner().getFavoriteGame().getMaterial(), 1, _manager.getOwner().getFavoriteGame().getMaterialData(), null)).setTitle(ChatColor.RESET + title).build();
			List<String> lore = new ArrayList<>();
			lore.add(" ");
			lore.add(ChatColor.RESET + C.cYellow + "Server Name: " + C.cWhite + getPlugin().getPlugin().getConfig().getString("serverstatus.name"));
			lore.add(ChatColor.RESET + C.cYellow + "Players Online: " + C.cWhite + UtilServer.getPlayers().length);
			lore.add(ChatColor.RESET + C.cYellow + "Players Max: " + C.cWhite + getPlugin().GetServerConfig().MaxPlayers);
			lore.add(" ");
			lore.add(ChatColor.RESET + "Left-Click to increase Max Players");
			lore.add(ChatColor.RESET + "Right-Click to decrease Max Players");
			ItemMeta meta = head.getItemMeta();
			meta.setLore(lore);
			head.setItemMeta(meta);

			return new ShopItem(head, title, title, 1, false, false);
		}
		String title = C.cGreen + C.Bold + getPlugin().GetHost() + "'s Mineplex Private Server";
		ItemStack head = getPlayerHead(getPlugin().GetHost(), ChatColor.RESET + title);
		List<String> lore = new ArrayList<>();
		lore.add(" ");
		lore.add(ChatColor.RESET + C.cYellow + "Server Name: " + C.cWhite + getPlugin().getPlugin().getConfig().getString("serverstatus.name"));
		lore.add(ChatColor.RESET + C.cYellow + "Players Online: " + C.cWhite + UtilServer.getPlayers().length);
		lore.add(ChatColor.RESET + C.cYellow + "Players Max: " + C.cWhite + getPlugin().GetServerConfig().MaxPlayers);
		lore.add(" ");
		lore.add(ChatColor.RESET + "Left-Click to increase Max Players");
		lore.add(ChatColor.RESET + "Right-Click to decrease Max Players");
		ItemMeta meta = head.getItemMeta();
		meta.setLore(lore);
		head.setItemMeta(meta);

		return new ShopItem(head, title, title, 1, false, false);
	}
}