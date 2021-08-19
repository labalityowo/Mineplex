package mineplex.core.game.kit.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.game.MineplexGameManager;
import mineplex.core.game.kit.GameKit;
import mineplex.core.game.kit.KitAvailability;
import mineplex.core.game.kit.PlayerKitData;
import mineplex.core.game.kit.ui.processors.KitPackageProcessor;
import mineplex.core.game.kit.upgrade.KitStat;
import mineplex.core.game.kit.upgrade.LinearUpgradeTree;
import mineplex.core.game.kit.upgrade.UpgradeTree;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.shop.confirmation.ConfirmationPage;
import mineplex.core.shop.item.IButton;

public class KitMainPage extends KitPage
{

	public KitMainPage(MineplexGameManager plugin, Player player, GameKit kit)
	{
		super(plugin, player, kit, kit.getDisplayName(), 27);

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		List<String> lore = new ArrayList<>(Arrays.asList(_kit.getDescription()));
		boolean unlocked = _plugin.isUnlocked(_player, _kit);

		lore.add(0, C.blankLine);
		lore.add("");

		if (_plugin.isActive(_player, _kit))
		{
			lore.add(C.cRed + "You already have this kit selected.");
		}
		else if (unlocked)
		{
			lore.add(C.cRedB + "Click Me " + C.cRed + "to select as your kit.");
		}
		else
		{
			switch (_kit.getAvailability())
			{
				case Gem:
					lore.add(C.cRedB + "Click Me " + C.cRed + "to buy this kit for " + GlobalCurrency.GEM.getString(_kit.getCost()) + C.cRed + ".");
					break;
				case Achievement:
					lore.add(C.cRed + "This kit requires " + C.cPurple + _kit.getAchievements().length + " Achievements" + C.cRed + " to unlock.");
					lore.add(C.cRedB + "Click Me " + C.cRed + "to view the required achievements.");
					break;
			}
		}

		Optional<UpgradeTree> optional = _kit.getUpgradeTree();
		boolean hasUpgrades = optional.isPresent() && unlocked;

		addButton(hasUpgrades ? 11 : 13, new ItemBuilder(_kit.getEntityData().getInHand())
				.setTitle(_kit.getFormattedName())
				.addLore(C.cWhite + "Kit Description:")
				.addLore(lore.toArray(new String[0]))
				.build(), (player, clickType) ->
		{
			if (!Recharge.Instance.use(player, "Kit Menu Interact", 500, false, false))
			{
				return;
			}

			// Kit is either unlocked or it's free
			if (_plugin.isUnlocked(player, _kit))
			{
				// If it isn't already active
				if (!_plugin.isActive(player, _kit))
				{
					// Check if the player has an entry in the database about this kit. If not unlock it.
					// This only happens with free kits on their first selection
					if (!_plugin.ownsKit(player, _kit))
					{
						_plugin.unlock(player, _kit);
					}
					else
					{
						_plugin.setActiveKit(player, _kit);
					}
				}
			}
			else if (_plugin.canUnlock(player, _kit))
			{
				MineplexGameManager plugin = _plugin;
				Runnable unlockRunnable = () -> plugin.unlock(player, _kit);

				switch (_kit.getAvailability())
				{
					case Gem:
						_shop.openPageForPlayer(player, new ConfirmationPage<>(
								_player,
								this,
								new KitPackageProcessor(
										_plugin,
										_kit,
										_player,
										unlockRunnable
								),
								getItem(13)
						));
						break;
					default:
						unlockRunnable.run();
						break;
				}
			}
			else if (_kit.getAvailability() == KitAvailability.Achievement)
			{
				_shop.openPageForPlayer(player, new KitAchievementPage(_plugin, player, _kit));
			}
			else
			{
				playDenySound(player);
			}
		});

		if (hasUpgrades)
		{
			UpgradeTree upgradeTree = optional.get();
			Optional<PlayerKitData> kitData = _plugin.getPlayerKitData(_player, _kit);

			if (!kitData.isPresent())
			{
				return;
			}

			ItemBuilder builder = new ItemBuilder(Material.EXP_BOTTLE);
			IButton button;
			Map<KitStat, Integer> playerStats = kitData.get().getStats();

			if (upgradeTree instanceof LinearUpgradeTree)
			{
				LinearUpgradeTree linearTree = (LinearUpgradeTree) upgradeTree;

				int xp = playerStats.getOrDefault(KitStat.XP, 0);
				int level = LinearUpgradeTree.getLevel(xp);
				int upgradeLevel = playerStats.getOrDefault(KitStat.UPGRADE_LEVEL, 0);

				builder.setTitle(C.cYellowB + "Kit Level - " + C.cGreen + level);
				builder.addLore(
						"",
						"Play games with this kit to earn experience for it",
						"and unlock special bonuses over time!"
				);

				if (upgradeLevel > 0)
				{
					builder.addLore("", "Current Bonus: " + C.cGreen + "Rank " + upgradeLevel);

					for (String upgrade : linearTree.getUpgrades().get(upgradeLevel - 1))
					{
						builder.addLore(upgrade);
					}
				}

				if (upgradeLevel < linearTree.getUpgrades().size())
				{
					builder.addLore("", "Next Bonus: " + C.cRed + "Rank " + (upgradeLevel + 1));

					for (String upgrade : linearTree.getUpgrades().get(upgradeLevel))
					{
						builder.addLore(upgrade);
					}

					int levelsUtilUpgrade = LinearUpgradeTree.getLevelsUntilNextUpgrade(level, upgradeLevel + 1);

					if (levelsUtilUpgrade > 0)
					{
						builder.addLore("", "You can buy the next rank in " + C.cGreen + levelsUtilUpgrade + C.cGray + " levels.");
					}
					else
					{
						builder.addLore("", C.cGreen + "You can by the next rank upgrade!");
					}
				}

				builder.addLore("", C.cYellowB + "Click Me " + C.cYellow + "to see all the rank unlocks for this kit.");

				button = (player, clickType) -> _shop.openPageForPlayer(player, new KitUpgradePage(_plugin, player, _kit));
			}
			else
			{
				return;
			}

			addButton(15, builder.build(), button);
		}
	}
}
