package mineplex.game.clans.clans.boxes;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.game.clans.clans.ClansManager;

public class DyeBoxPage extends ShopPageBase<BoxManager, BoxShop>
{
	private static final List<DyeColor> COMMON_COLORS = Stream.of(DyeColor.values()).filter(c -> c != DyeColor.BLACK && c != DyeColor.WHITE).collect(Collectors.toList());
	private static final List<DyeColor> RARE_COLORS = Arrays.asList(DyeColor.WHITE, DyeColor.BLACK);
	
	public DyeBoxPage(BoxManager plugin, BoxShop shop, String name, Player player)
	{
		super(plugin, shop, ClansManager.getInstance().getClientManager(), ClansManager.getInstance().getDonationManager(), name, player, 27);
		
		buildPage();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void buildPage()
	{
		int[] slots = {12, 14};
		int i = 0;
		for (DyeType type : DyeType.values())
		{
			int owned = ClansManager.getInstance().getInventoryManager().Get(_player).getItemCount(type.getItemName());
			int slot = slots[i++];
			addButton(slot, SkinData.CLANS_DYE_BOX.getSkull(C.cRed + (type.isGilded() ? "Gilded " : "") + "Dye Box", Arrays.asList(
					C.cYellow + "Open a box containing " + F.greenElem(String.valueOf(type.getDyeCount())) + C.cYellow + " random dyes!",
					C.cRed + " ",
					C.cGreen + ">Click to Activate<",
					C.cBlue + " ",
					C.cDAqua + "You own " + F.greenElem(String.valueOf(Math.max(owned, 0))) + C.cDAqua + (type.isGilded() ? " Gilded" : "") + " Dye Boxes")),
			(player, clickType) ->
			{
				if (!Recharge.Instance.use(player, "Clans Box Click", 1000, false, false))
				{
					return;
				}
				if (owned < 1)
				{
					playDenySound(player);
					UtilPlayer.message(player, F.main(getPlugin().getName(), "You do not have enough of that box! Purchase some at http://www.mineplex.com/shop!"));
					return;
				}
				player.closeInventory();
				ClansManager.getInstance().getInventoryManager().addItemToInventory(player, type.getItemName(), -1);
				for (int dye = 0; dye < type.getDyeCount(); dye++)
				{
					List<DyeColor> options = ThreadLocalRandom.current().nextDouble() <= 0.05 ? RARE_COLORS : COMMON_COLORS;
					DyeColor color = UtilMath.randomElement(options);
					player.getInventory().addItem(new ItemBuilder(Material.INK_SACK).setData(color.getDyeData()).setTitle(C.cGold + "Dye").build());
				}
			});
		}
	}
	
	private enum DyeType
	{
		NORMAL("Clans Dye Box", false, 32),
		GILDED("Clans Gilded Dye Box", true, 64)
		;
		
		private final String _itemName;
		private final boolean _gilded;
		private final int _dyeCount;
		
		private DyeType(String itemName, boolean gilded, int dyeCount)
		{
			_itemName = itemName;
			_gilded = gilded;
			_dyeCount = dyeCount;
		}
		
		public String getItemName()
		{
			return _itemName;
		}
		
		public boolean isGilded()
		{
			return _gilded;
		}
		
		public int getDyeCount()
		{
			return _dyeCount;
		}
	}
}