package mineplex.core.cosmetic.ui.page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilUI;
import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.cosmetic.ui.CosmeticShop;
import mineplex.core.donation.DonationManager;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.gadget.types.WeaponNameGadget;
import mineplex.core.gadget.types.WeaponNameGadget.WeaponType;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.itemstack.ItemStackFactory;

public class WeaponNamePage extends GadgetPage
{

	public WeaponNamePage(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player)
	{
		super(plugin, shop, clientManager, donationManager, "Weapon Names", player);

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		WeaponType[] types = WeaponType.values();
		int[] slots = UtilUI.getIndicesFor(types.length, 2);
		int index = 0;
		Map<WeaponType, List<Gadget>> gadgets = new HashMap<>(types.length);

		for (Gadget gadget : _plugin.getGadgetManager().getGadgets(GadgetType.WEAPON_NAME))
		{
			gadgets.computeIfAbsent(((WeaponNameGadget) gadget).getWeaponNameType().getWeaponType(), k -> new ArrayList<>()).add(gadget);
		}

		for (WeaponType type : types)
		{
			int own = 0, total = 0;
			List<Gadget> gadgetList = gadgets.get(type);

			for (Gadget gadget : gadgetList)
			{
				if (gadget.ownsGadget(getPlayer()))
				{
					own++;
				}
				total++;
			}

			ItemBuilder builder = new ItemBuilder(type.getMaterial());
			String name = ItemStackFactory.Instance.GetName(builder.getType(), (byte) 0, false);

			builder.setTitle(C.cGreenB + name);
			builder.addLore(
					"",
					C.cWhite + "You own " + own + "/" + total,
					"",
					C.cGreen + "Left-Click to view " + name + " cosmetics"
			);

			addButton(slots[index++], builder.build(), (player, clickType) -> getShop().openPageForPlayer(player, new ListGadgetPage(getPlugin(), getShop(), getClientManager(), getDonationManager(), name, player, gadgetList)));
		}

		addBackButton();
	}
}
