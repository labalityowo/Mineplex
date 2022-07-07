package mineplex.game.clans.clans.bosstoken;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.worldevent.WorldEventManager;
import mineplex.game.clans.clans.worldevent.WorldEventType;

public class BossTokenPage extends ShopPageBase<WorldEventManager, BossTokenShop>
{
	public BossTokenPage(WorldEventManager plugin, BossTokenShop shop, String name, Player player)
	{
		super(plugin, shop, ClansManager.getInstance().getClientManager(), ClansManager.getInstance().getDonationManager(), name, player, 27);
		
		buildPage();
	}

	@Override
	protected void buildPage()
	{
		int[] slots = {12, 14};
		int i = 0;
		for (TokenType type : TokenType.values())
		{
			int owned = ClansManager.getInstance().getInventoryManager().Get(_player).getItemCount(type.getItemName());
			int slot = slots[i++];
			addButton(slot, type.getButton(C.cRed + type.getDisplay() + " Summon Token", Arrays.asList(
					C.cYellow + "Summon the powerful " + type.getDisplay(),
					C.cYellow + "In the " + C.cRed + "Borderlands",
					C.cRed + " ",
					C.cGreen + ">Click to Activate<",
					C.cBlue + " ",
					C.cDAqua + "You own " + F.greenElem(String.valueOf(Math.max(owned, 0))) + C.cDAqua + " " + type.getDisplay() + " Summon Tokens"
				)
			), (player, clickType) ->
			{
				if (!Recharge.Instance.use(player, "Clans Box Click", 1000, false, false))
				{
					return;
				}
				if (owned < 1)
				{
					playDenySound(player);
					UtilPlayer.message(player, F.main(getPlugin().getName(), "You do not have enough of that token! Purchase some at http://www.mineplex.com/shop!"));
					return;
				}
				if (!getPlugin().getEvents().isEmpty())
				{
					playDenySound(player);
					UtilPlayer.message(player, F.main(getPlugin().getName(), "There is already an ongoing event! Try again later!"));
					return;
				}
				WorldEventManager manager = getPlugin();
				player.closeInventory();
				manager.startEventFromType(type.getType());
				ClansManager.getInstance().getInventoryManager().addItemToInventory(player, type.getItemName(), -1);
			});
		}
	}
	
	private enum TokenType
	{
		SKELETON("Skeleton", "Skeleton King", WorldEventType.SKELETON_KING, (name, lore) -> new ItemBuilder(Material.SKULL_ITEM).setData((short)1).setTitle(name).addLores(lore).build()),
		WIZARD("Wizard", "Iron Wizard", WorldEventType.IRON_WIZARD, SkinData.IRON_GOLEM::getSkull)
		;
		
		private final String _itemEnding;
		private final String _display;
		private final WorldEventType _type;
		private final BiFunction<String, List<String>, ItemStack> _buttonCreator;
		
		private TokenType(String itemEnding, String display, WorldEventType type, BiFunction<String, List<String>, ItemStack> buttonCreator)
		{
			_itemEnding = itemEnding;
			_display = display;
			_type = type;
			_buttonCreator = buttonCreator;
		}
		
		public String getItemName()
		{
			return "Clans Boss Token " + _itemEnding;
		}
		
		public String getDisplay()
		{
			return _display;
		}
		
		public WorldEventType getType()
		{
			return _type;
		}
		
		public ItemStack getButton(String itemName, List<String> lore)
		{
			return _buttonCreator.apply(itemName, lore);
		}
	}
}