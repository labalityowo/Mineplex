package nautilus.game.arcade.game.games.moba.progression.ui;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilUI;
import mineplex.core.donation.DonationManager;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.server.util.TransactionResponse;
import mineplex.core.shop.page.ShopPageBase;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.MobaRole;
import nautilus.game.arcade.game.games.moba.kit.HeroKit;
import nautilus.game.arcade.game.games.moba.progression.MobaLevelData;
import nautilus.game.arcade.game.games.moba.progression.MobaProgression;
import nautilus.game.arcade.game.games.moba.progression.MobaUnlockAnimation;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MobaRolePage extends ShopPageBase<ArcadeManager, MobaRoleShop>
{

	private static final int SIZE = 45;
	private static final int[] MAPPINGS = UtilUI.getIndicesFor(4, 3);
	private static final ItemStack COMING_SOON = new ItemBuilder(Material.STAINED_CLAY, (byte) 15)
			.setTitle(C.cRed + "Coming Soon")
			.build();
	private static final int ANIMATION_TIME = 20;

	private final Moba _host;
	private final MobaRole _role;

	public MobaRolePage(ArcadeManager plugin, MobaRoleShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player, Moba host, MobaRole role)
	{
		super(plugin, shop, clientManager, donationManager, role.getName(), player, SIZE);

		_host = host;
		_role = role;

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		MobaLevelData levelData = new MobaLevelData(_host.getProgression().getExperience(_player, _role));

		addButtonNoAction(13, new ItemBuilder(_role.getSkin().getSkull())
				.setTitle(_role.getChatColor() + _role.getName())
				.addLore(
						"",
						"Every " + F.elem(10) + " levels you unlock a new",
						"hero within the " + F.name(_role.getName()) + " category.",
						"",
						"Your Level: " + C.cGreen + levelData.getDisplayLevel(),
						"Next Level: " + C.cGreen + levelData.getExpLevelProgress() + C.cGray + "/" + C.cGreen + levelData.getExpJustThisLevel() + C.cGray +  " (" + C.cAqua + MobaProgression.FORMAT.format(levelData.getPercentageComplete() * 100D) + C.cGray + "%)"
				)
				.build());

		List<HeroKit> kits = new ArrayList<>();

		for (HeroKit kit : _host.getKits())
		{
			if (!kit.getRole().equals(_role) || !kit.isVisible() || kit.getUnlockLevel() == 0)
			{
				continue;
			}

			kits.add(kit);
		}

		int i = 0;
		for (int slot : MAPPINGS)
		{
			if (i >= kits.size())
			{
				addButtonNoAction(slot, COMING_SOON);
				continue;
			}

			HeroKit kit = kits.get(i++);
			String packageName = _host.getProgression().getPackageName(kit);
			boolean hasUnlocked = _plugin.GetDonation().Get(_player).ownsUnknownSalesPackage(packageName);
			boolean canUnlock = _host.getProgression().getLevel(_player, kit) >= kit.getUnlockLevel() - 1;
			ItemBuilder builder = new ItemBuilder(Material.STAINED_CLAY);

			builder.setTitle(C.cGreen + kit.GetName());
			builder.addLore("", "Unlocks at " + _role.getName() + " Level " + C.cGreen + kit.getUnlockLevel());

			if (hasUnlocked)
			{
				builder.setData((byte) 5);
				builder.addLore(C.cRed + "You have already unlocked this hero!");
			}
			else
			{
				builder.setData((byte) 14);
				builder.setGlow(canUnlock);

				if (canUnlock)
				{
					builder.addLore(C.cGreen + "Click to unlock!");
				}
				else
				{
					builder.addLore(C.cRed + "You cannot unlock this hero!");
				}
			}

			addButton(slot, builder.build(), (player, clickType) ->
			{
				if (!Recharge.Instance.use(player, "Hero Unlock", 1000, false, false))
				{
					playDenySound(player);
					return;
				}

				boolean allowAnimation = (_host.GetCountdown() > ANIMATION_TIME || _host.GetCountdown() < 0) && _host.getProgression().getCurrentAnimation() == null;

				if (!hasUnlocked && canUnlock)
				{
					if (allowAnimation)
					{
						_host.getArcadeManager().GetDonation().purchaseUnknownSalesPackage(player, packageName, GlobalCurrency.GEM, 0, true, data ->
						{
							if (data != TransactionResponse.Success)
							{
								player.sendMessage(F.main("Game", "Failed to unlock " + kit.GetName() + " please try again in a few seconds."));
								return;
							}

							new MobaUnlockAnimation(_host, player, kit);
							playAcceptSound(player);
							player.closeInventory();
						});
					}
					else
					{
						player.sendMessage(F.main("Game", "You cannot unlock a Hero right now."));
						playDenySound(player);
					}
				}
				else
				{
					playDenySound(player);
				}
			});
		}
	}
}
