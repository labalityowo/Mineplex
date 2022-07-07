package mineplex.game.clans.clans.cash;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;

import mineplex.core.Managers;
import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.inventory.ClientInventory;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.confirmation.ConfirmationCallback;
import mineplex.core.shop.confirmation.ConfirmationPage;
import mineplex.core.shop.confirmation.ConfirmationProcessor;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.amplifiers.AmplifierGUI;
import mineplex.game.clans.clans.boxes.BoxManager;
import mineplex.game.clans.clans.boxes.BoxManager.BoxType;
import mineplex.game.clans.clans.supplydrop.SupplyDropManager;

public class CashOverviewPage extends ShopPageBase<CashShopManager, CashShop>
{
	private static final int OVERVIEW_BUTTON_SLOT = 13;
	private static final int RUNE_BUTTON_SLOT = 21;
	private static final int BUILDERS_BUTTON_SLOT = 23;
	private static final int SUPPLY_BUTTON_SLOT = 29;
	private static final int DYE_BUTTON_SLOT = 31;
	private static final int BOSS_BUTTON_SLOT = 33;
	private static final int MOUNT_BUTTON_SLOT = 39;
	private static final int BANNER_BUTTON_SLOT = 41;
	
	public CashOverviewPage(CashShopManager plugin, CashShop shop, String name, Player player)
	{
		super(plugin, shop, ClansManager.getInstance().getClientManager(), ClansManager.getInstance().getDonationManager(), name, player);

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		ClientInventory inv = ClansManager.getInstance().getInventoryManager().Get(getPlayer());
		int rune20 = inv.getItemCount("Rune Amplifier 20");
		int rune60 = inv.getItemCount("Rune Amplifier 60");
		int builders = inv.getItemCount(BoxType.BUILDER_BOX.getItemName());
		int supply = inv.getItemCount("Clans Supply Drop");
		int supplyGilded = inv.getItemCount("Clans Gilded Supply Drop");
		int dye = inv.getItemCount("Clans Dye Box");
		int dyeGilded = inv.getItemCount("Clans Gilded Dye Box");
		int skeleton = inv.getItemCount("Clans Boss Token Skeleton");
		int wizard = inv.getItemCount("Clans Boss Token Wizard");
		addButtonNoAction(OVERVIEW_BUTTON_SLOT, new ItemBuilder(Material.EMERALD)
				.setTitle(C.cRed + C.Scramble + "1 " + C.cRed + "Mineplex Shop" + " " + C.Scramble + "1")
				.addLore(C.cYellow + "Purchase powerful supply drops, boss summoning items,")
				.addLore(C.cYellow + "and exclusive cosmetic items in our online store!")
				.addLore(C.cRed + " ")
				.addLore(C.cGreen + "http://www.mineplex.com/shop")
				.build()
		);
		addButton(RUNE_BUTTON_SLOT, new ItemBuilder(Material.NETHER_STAR)
				.setTitle(C.cRed + "Rune Amplifier")
				.addLore(C.cYellow + "Open a portal to the Nether in Shops")
				.addLore(C.cYellow + "And double the chances for Rune drops!")
				.addLore(C.cRed + " ")
				.addLore(C.cDAqua + "You own " + F.greenElem(String.valueOf(rune20)) + C.cDAqua + " Twenty Minute Amplifiers")
				.addLore(C.cDAqua + "You own " + F.greenElem(String.valueOf(rune60)) + C.cDAqua + " One Hour Amplifiers")
				.build(),
		(player, clickType) ->
		{
			player.closeInventory();
			new AmplifierGUI(player, ClansManager.getInstance().getAmplifierManager());
		});
		addButton(BUILDERS_BUTTON_SLOT, SkinData.CLANS_BUILDERS_BOX.getSkull(C.cRed + "Builder's Box", Arrays.asList(
				C.cYellow + "Transform normal blocks into alternate forms",
				C.cYellow + "Or otherwise unobtainable cosmetic blocks!",
				C.cRed + " ",
				C.cDAqua + "You own " + F.greenElem(String.valueOf(builders)) + C.cDAqua + " Builder's Boxes")),
		(player, clickType) ->
		{
			if (builders < 1)
			{
				return;
			}
			getShop().openPageForPlayer(player, new ConfirmationPage<>(player, this, new ConfirmationProcessor()
			{
				@Override
				public void init(Inventory inventory)
				{
					try
					{
						Field returnPage = ConfirmationPage.class.getDeclaredField("_returnPage");
						returnPage.setAccessible(true);
						returnPage.set(inventory, null);
						returnPage.setAccessible(false);
					}
					catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
					{
						e.printStackTrace();
					}
				}

				@Override
				public void process(ConfirmationCallback callback)
				{
					BoxType.BUILDER_BOX.onUse(player);
					callback.resolve("Opening Builder's Box...");
				}
			}, SkinData.CLANS_BUILDERS_BOX.getSkull(C.cRed + "Open Builder's Box", UtilText.splitLines(new String[]
			{
				C.cRed,
				C.cRedB + "WARNING: " + C.cWhite + "This will convert all applicable items in your inventory!"
			}, LineFormat.LORE)), "Confirm Opening Box"));
		});
		addButton(SUPPLY_BUTTON_SLOT, SkinData.CLANS_GILDED_SUPPLY_DROP.getSkull(C.cRed + "Supply Drop", Arrays.asList(
				C.cYellow + "Call down Supply Drops to obtain",
				C.cYellow + "Rare and valuable resources!",
				C.cRed + " ",
				C.cYellow + "Use " + C.cGreen + "/inventory" + C.cYellow + " to access Supply Drops",
				C.cBlue + " ",
				C.cDAqua + "You own " + F.greenElem(String.valueOf(supply)) + C.cDAqua + " Supply Drops",
				C.cDAqua + "You own " + F.greenElem(String.valueOf(supplyGilded)) + C.cDAqua + " Gilded Supply Drops")),
		(player, clickType) ->
		{
			player.closeInventory();
			Managers.get(SupplyDropManager.class).getShop().attemptShopOpen(player);
		});
		addButton(DYE_BUTTON_SLOT, SkinData.CLANS_DYE_BOX.getSkull(C.cRed + "Dye Box", Arrays.asList(
				C.cYellow + "Open a box containing a large amount",
				C.cYellow + "Of different dyes to use!",
				C.cRed + " ",
				C.cDAqua + "You own " + F.greenElem(String.valueOf(dye)) + C.cDAqua + " Dye Boxes",
				C.cDAqua + "You own " + F.greenElem(String.valueOf(dyeGilded)) + C.cDAqua + " Gilded Dye Boxes")),
		(player, clickType) ->
		{
			player.closeInventory();
			Managers.get(BoxManager.class).openDyePage(player);
		});
		addButton(BOSS_BUTTON_SLOT, new ItemBuilder(Material.GOLD_INGOT)
				.setTitle(C.cRed + "Boss Summon Token")
				.addLore(C.cYellow + "Summon powerful World Bosses to fight")
				.addLore(C.cYellow + "Whenever you are most prepared!")
				.addLore(C.cRed + " ")
				.addLore(C.cYellow + "Use " + C.cGreen + "/inventory" + C.cYellow + " to access Boss Summon Tokens")
				.addLore(C.cBlue + " ")
				.addLore(C.cDAqua + "You own " + F.greenElem(String.valueOf(skeleton)) + C.cDAqua + " Skeleton King Tokens")
				.addLore(C.cDAqua + "You own " + F.greenElem(String.valueOf(wizard)) + C.cDAqua + " Iron Wizard Tokens")
				.build(),
		(player, clickType) ->
		{
			player.closeInventory();
			ClansManager.getInstance().getWorldEvent().getShop().attemptShopOpen(player);
		});
		addButtonNoAction(MOUNT_BUTTON_SLOT, new ItemBuilder(Material.DIAMOND_BARDING)
				.setTitle(C.cRed + "Mount Skins")
				.addLore(C.cYellow + "Stroll around the map in style!")
				.build()
		);
		addButton(BANNER_BUTTON_SLOT, new ItemBuilder(Material.BANNER)
				.setTitle(C.cRed + "Clan Banner")
				.addLore(C.cYellow + "Show off your Clan Pride!")
				.build(),
		(player, clickType) ->
		{
			player.closeInventory();
			UtilServer.CallEvent(new PlayerCommandPreprocessEvent(player, "/banner"));
		});
	}
}