package nautilus.game.arcade.gui.privateServer.page;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.shop.item.IButton;
import mineplex.core.shop.item.ShopItem;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.GameServerConfig;
import nautilus.game.arcade.gui.privateServer.PrivateServerShop;

public class OptionsPage extends BasePage
{
	private GameServerConfig _config;

	public OptionsPage(ArcadeManager plugin, PrivateServerShop shop, Player player)
	{
		super(plugin, shop, "Game Options", player);

		_config = getPlugin().GetServerConfig();
		buildPage();
	}

	@Override
	protected void buildPage()
	{

		addBackButton(4);

		//TeamForceBalance
		//GameAutoStart
		//GameTimeout
		//PlayerKickIdle
		
		if (!_plugin.GetGameHostManager().isCommunityServer())
		{
			buildPreference(11, Material.EYE_OF_ENDER, "Public Server", _config.PublicServer, new IButton()
			{
				@Override
				public void onClick(Player player, ClickType clickType)
				{
					togglePublic();
				}
			});
		}
		
		if (!_plugin.GetGameHostManager().isCommunityServer())
		{
			buildPreference(13, Material.PAPER, "Enforce Whitelist", _config.PlayerServerWhitelist, new IButton()
			{
				@Override
				public void onClick(Player player, ClickType clickType)
				{
					toggleWhitelist();
				}
			});
		}

		buildPreference(15, Material.RAILS, "Force Team Balancing", _config.TeamForceBalance, new IButton()
		{
			@Override
			public void onClick(Player player, ClickType clickType)
			{
				toggleTeamBalance();
			}
		});

		buildPreference(38, Material.SLIME_BALL, "Game Auto Start", _config.GameAutoStart, new IButton()
		{
			@Override
			public void onClick(Player player, ClickType clickType)
			{
				toggleAutoStart();
			}
		});

		buildPreference(40, Material.COMPASS, "Game Timeout", _config.GameTimeout, new IButton()
		{
			@Override
			public void onClick(Player player, ClickType clickType)
			{
				toggleTimeout();
			}
		});

		buildPreference(42, Material.WATCH, "Kick Idle Players", _config.PlayerKickIdle, new IButton()
		{
			@Override
			public void onClick(Player player, ClickType clickType)
			{
				toggleKickIdle();
			}
		});

	}

	private void toggleTeamBalance()
	{
		_config.TeamForceBalance = !_config.TeamForceBalance;
		refresh();
	}

	private void toggleAutoStart()
	{
		_config.GameAutoStart = !_config.GameAutoStart;
		refresh();
	}

	private void toggleTimeout()
	{
		_config.GameTimeout = !_config.GameTimeout;
		refresh();
	}

	private void toggleKickIdle()
	{
		_config.PlayerKickIdle = !_config.PlayerKickIdle;
		refresh();
	}

	private void toggleWhitelist()
	{
		_config.PlayerServerWhitelist = !_config.PlayerServerWhitelist;
		if (_config.PlayerServerWhitelist == true)
			_config.PublicServer = false;
		refresh();
	}
	
	private void togglePublic()
	{
		if (_config.PlayerServerWhitelist==true)
		{
			_config.PublicServer = false;
			return;
		}
		_config.PublicServer = !_config.PublicServer;
		refresh();
	}

	private void buildPreference(int index, Material material, String name, boolean preference, IButton button)
	{
		buildPreference(index, material, (byte) 0, name, preference, button);
	}

	private void buildPreference(int index, Material material, byte data, String name, boolean preference, IButton button)
	{
		String[] description = new String[] {
				"" + (preference ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"),
				ChatColor.RED + " ",
				ChatColor.RESET + "Click to " + (preference ? "Disable" : "Enable") };

		if (name.equalsIgnoreCase("Enforce Whitelist"))
		{
			description = new String[] {
					"" + (preference ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"),
					ChatColor.RED + " ",
					ChatColor.RESET + "Click to " + (preference ? "Disable" : "Enable"), "", "§7Use §a/whitelist §e<name>" };
		}

		addButton(index, new ShopItem(material, data, (preference ? ChatColor.GREEN : ChatColor.RED) + name, description, 1, false, false), button);
		addButton(index + 9, new ShopItem(Material.INK_SACK, (preference ? (byte) 10 : (byte) 8), (preference ? ChatColor.GREEN : ChatColor.RED) + name, description, 1, false, false), button);
	}
}
