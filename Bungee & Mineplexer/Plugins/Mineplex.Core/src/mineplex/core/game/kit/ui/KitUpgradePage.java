package mineplex.core.game.kit.ui;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.game.MineplexGameManager;
import mineplex.core.game.kit.GameKit;
import mineplex.core.game.kit.ui.processors.KitUpgradeProcessor;
import mineplex.core.game.kit.upgrade.KitStat;
import mineplex.core.game.kit.upgrade.LinearUpgradeTree;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.confirmation.ConfirmationPage;

public class KitUpgradePage extends KitPage
{

	KitUpgradePage(MineplexGameManager plugin, Player player, GameKit kit)
	{
		super(plugin, player, kit, kit.getDisplayName() + " Upgrades");

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		addBackButton();

		LinearUpgradeTree upgradeTree = (LinearUpgradeTree) _kit.getUpgradeTree().get();
		int currentLevel = LinearUpgradeTree.getLevel(_plugin, _player, _kit);
		int currentUpgradeLevel = LinearUpgradeTree.getUpgradeLevel(_plugin, _player, _kit);

		int i = 0;
		int menuIndex = 11;
		boolean previousUnlock = false;
		for (List<String> upgrades : upgradeTree.getUpgrades())
		{
			int displayI = i + 1;
			int upgradeAtLevel = LinearUpgradeTree.getUpgradeAtLevel(i);
			int cost = LinearUpgradeTree.getUpgradeCost(i);
			boolean unlocked = currentUpgradeLevel >= displayI;
			boolean canUnlock = !previousUnlock && LinearUpgradeTree.getLevelsUntilNextUpgrade(currentLevel, displayI) <= 0 && !unlocked && _plugin.getDonationManager().Get(_player).getBalance(GlobalCurrency.GEM) >= cost;
			ItemBuilder builder = new ItemBuilder(Material.STAINED_CLAY);
			String colour;

			builder.addLore("");

			if (unlocked)
			{
				colour = C.cGreenB;
				builder.addLore("Rank Unlocked");
				builder.setData((byte) 5);
			}
			else if (canUnlock)
			{
				colour = C.cPurpleB;
				builder.addLore(C.cGreen + "Rank available for purchase", C.cYellowB + "Click Me " + C.cYellow + "to buy for " + GlobalCurrency.GEM.getString(cost), "");
				builder.setData((byte) 2);
				previousUnlock = true;
			}
			else
			{
				colour = C.cRedB;
				builder.addLore(C.cRed + "Rank Locked");
				builder.setData((byte) 3);
			}

			builder.setTitle(colour + "Rank " + (i + 1));
			builder.addLore("Available at level " + upgradeAtLevel + (!unlocked ? " (Current Level: " + currentLevel + ")" : ""), "");

			upgrades.forEach(builder::addLore);

			int fMenuIndex = menuIndex;

			addButton(menuIndex, builder.build(), (player, clickType) ->
			{
				if (canUnlock)
				{
					_shop.openPageForPlayer(player, new ConfirmationPage<>(
							_player,
							_plugin,
							_shop,
							_clientManager,
							_donationManager,
							new KitUpgradeProcessor(
									_plugin,
									_player,
									cost,
									() -> _plugin.incrementKitStat(player, _kit, KitStat.UPGRADE_LEVEL, 1)
							),
							getItem(fMenuIndex)
					));
				}
				else
				{
					playDenySound(player);
				}
			});

			i++;
			menuIndex++;
		}
	}
}
