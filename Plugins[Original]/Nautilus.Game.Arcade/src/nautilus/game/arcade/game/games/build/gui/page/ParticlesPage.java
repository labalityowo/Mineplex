package nautilus.game.arcade.game.games.build.gui.page;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.donation.DonationManager;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.shop.page.ShopPageBase;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.build.Build;
import nautilus.game.arcade.game.games.build.BuildData;
import nautilus.game.arcade.game.games.build.gui.OptionsShop;

public class ParticlesPage extends ShopPageBase<ArcadeManager, OptionsShop>
{
	private Build _game;

	public ParticlesPage(Build game, ArcadeManager plugin, OptionsShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player)
	{
		super(plugin, shop, clientManager, donationManager, "Add Particles", player);
		_game = game;
		buildPage();
	}

	@Override
	protected void buildPage()
	{
		final BuildData buildData = _game.getBuildData(getPlayer());

		if (buildData == null)
		{
			getPlayer().closeInventory();
			return;
		}

		int index = 0;
		for (final UtilParticle.ParticleType particleType : UtilParticle.ParticleType.values())
		{
			if (particleType.hasFriendlyData())
			{
				ShopItem shopItem = new ShopItem(particleType.getMaterial(), particleType.getData(), particleType.getFriendlyName(), null, 0, false, false);
				addButton(index, shopItem, new IButton()
				{
					@Override
					public void onClick(Player player, ClickType clickType)
					{
						String[] lore = { ChatColor.GRAY + "Right click to place" };
						ItemStack itemStack = ItemStackFactory.Instance.CreateStack(particleType.getMaterial(), particleType.getData(), 1, ChatColor.GREEN + "Place " + particleType.getFriendlyName(), Arrays.asList(lore));
						player.getInventory().addItem(itemStack);
					}
				});

				index++;
			}
		}

		ShopItem clearButton = new ShopItem(Material.TNT, "Clear Particles", null, 0, false);
		addButton(53, clearButton, new IButton()
		{
			@Override
			public void onClick(Player player, ClickType clickType)
			{
				buildData.resetParticles(player);
			}
		});

		addButton((9 * 5) + 4, new ShopItem(Material.BED, C.cGray + " \u21FD Go Back", new String[]{}, 1, false), new IButton()
		{
			@Override
			public void onClick(Player player, ClickType clickType)
			{
				getShop().openPageForPlayer(player, new OptionsPage(_game, getPlugin(), getShop(), getClientManager(), getDonationManager(), player));
			}
		});
	}
}
