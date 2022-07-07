package mineplex.game.clans.clans.mounts.gui;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.confirmation.ConfirmationCallback;
import mineplex.core.shop.confirmation.ConfirmationPage;
import mineplex.core.shop.confirmation.ConfirmationProcessor;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.clans.mounts.Mount.MountType;
import mineplex.game.clans.clans.mounts.MountManager;
import mineplex.game.clans.clans.mounts.MountStatToken;
import mineplex.game.clans.clans.mounts.MountToken;

public class MountOverviewPage extends ShopPageBase<MountManager, MountShop>
{
	private final String STAR = "✩";
	
	public MountOverviewPage(MountManager plugin, MountShop shop, String name, Player player)
	{
		super(plugin, shop, plugin.getClientManager(), plugin.getDonationManager(), name, player, 27);
		
		buildPage();
	}
	
	private ItemStack toItem(MountToken mountToken, MountStatToken statToken, boolean overview)
	{
		ItemBuilder builder = new ItemBuilder(mountToken.Type.getDisplayType());
		builder.setTitle(mountToken.Type.getDisplayName() + " Mount");
		builder.addLore(C.cPurple + "Skin: " + (mountToken.Skin == null ? C.cYellow + "Default" : mountToken.Skin.getDisplayName()));
		String strength = C.cYellow;
		for (int i = 0; i < statToken.StrengthStars; i++)
		{
			strength += STAR;
		}
		builder.addLore(C.cPurple + "Strength: " + strength);
		String speed = C.cYellow;
		for (int i = 0; i < statToken.SpeedStars; i++)
		{
			speed += STAR;
		}
		builder.addLore(C.cPurple + "Speed: " + speed);
		String jump = C.cYellow;
		for (int i = 0; i < statToken.JumpStars; i++)
		{
			jump += STAR;
		}
		builder.addLore(C.cPurple + "Jump: " + jump);
		if (overview)
		{
			builder.addLore(C.cRed);
			builder.addLore(C.cDGreen + "Left-Click to Summon");
			builder.addLore(C.cDGreen + "Right-Click to Manage Skin");
			builder.addLore(C.cDRed + "Shift Right-Click to Destroy");
		}
		
		return builder.build();
	}

	@Override
	protected void buildPage()
	{
		int[] horseSlots = {10, 11, 12};
		List<Pair<MountToken, MountStatToken>> horses = getPlugin().Get(getPlayer()).getOwnedMounts(true, MountType.HORSE);
		int[] donkeySlots = {14, 15, 16};
		List<Pair<MountToken, MountStatToken>> donkeys = getPlugin().Get(getPlayer()).getOwnedMounts(true, MountType.DONKEY);
		
		for (int i = 0; i < horseSlots.length; i++)
		{
			int slot = horseSlots[i];
			ItemStack item = new ItemBuilder(Material.INK_SACK).setData((short)8).setTitle(C.cRed).build();
			IButton button = null;
			if (horses.size() > i)
			{
				Pair<MountToken, MountStatToken> tokens = horses.get(i);
				item = toItem(tokens.getLeft(), tokens.getRight(), true);
				button = (player, clickType) ->
				{
					if (clickType == ClickType.LEFT)
					{
						if (!getPlugin().summonMount(player, tokens.getLeft()))
						{
							playDenySound(player);
						}
					}
					else if (clickType == ClickType.RIGHT)
					{
						getShop().openPageForPlayer(player, new MountSkinPage(this, player, tokens.getLeft()));
					}
					else if (clickType == ClickType.SHIFT_RIGHT)
					{
						getShop().openPageForPlayer(player, new ConfirmationPage<MountManager, MountShop>(player, MountOverviewPage.this, new ConfirmationProcessor()
						{
							@Override
							public void init(Inventory inventory) {}

							@Override
							public void process(ConfirmationCallback callback)
							{
								getPlugin().removeMountToken(player, tokens.getLeft(), () ->
								{
									MountOverviewPage.this.refresh();
									callback.resolve("Mount successfully deleted!");
								});
							}
						}, toItem(tokens.getLeft(), tokens.getRight(), false)));
					}
				};
			}
			addButton(slot, item, button);
		}
		for (int i = 0; i < donkeySlots.length; i++)
		{
			int slot = donkeySlots[i];
			ItemStack item = new ItemBuilder(Material.INK_SACK).setData((short)8).setTitle(C.cRed).build();
			IButton button = null;
			if (donkeys.size() > i)
			{
				Pair<MountToken, MountStatToken> tokens = donkeys.get(i);
				item = toItem(tokens.getLeft(), tokens.getRight(), true);
				button = (player, clickType) ->
				{
					if (clickType == ClickType.LEFT)
					{
						if (!getPlugin().summonMount(player, tokens.getLeft()))
						{
							playDenySound(player);
						}
					}
					else if (clickType == ClickType.RIGHT)
					{
						getShop().openPageForPlayer(player, new MountSkinPage(this, player, tokens.getLeft()));
					}
					else if (clickType == ClickType.SHIFT_RIGHT)
					{
						getShop().openPageForPlayer(player, new ConfirmationPage<MountManager, MountShop>(player, new MountOverviewPage(getPlugin(), getShop(), getName(), getPlayer()), new ConfirmationProcessor()
						{
							@Override
							public void init(Inventory inventory) {}

							@Override
							public void process(ConfirmationCallback callback)
							{
								getPlugin().removeMountToken(player, tokens.getLeft(), () ->
								{
									MountOverviewPage.this.refresh();
									callback.resolve("Mount successfully deleted!");
								});
							}
						}, toItem(tokens.getLeft(), tokens.getRight(), false)));
					}
				};
			}
			addButton(slot, item, button);
		}
	}
}