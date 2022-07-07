package mineplex.core.gadget.gadgets.gamemodifiers.moba.skins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.gamemodifiers.GameCosmeticCategory;
import mineplex.core.gadget.types.GameModifierGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.game.GameDisplay;
import mineplex.core.google.GoogleSheetsManager;
import mineplex.core.treasure.reward.RewardRarity;

public class HeroSkinGadget extends GameModifierGadget
{

	private static Map<String, List<HeroSkinGadgetData>> SKIN_DATA;
	public static Map<String, List<HeroSkinGadgetData>> getSkins()
	{
		if (SKIN_DATA != null)
		{
			return SKIN_DATA;
		}

		Map<String, List<HeroSkinGadgetData>> skinData = new HashMap<>();
		Map<String, List<List<String>>> sheet = Managers.require(GoogleSheetsManager.class).getSheetData("MOBA_SKINS");

		sheet.forEach((sheetName, rows) ->
		{
			List<HeroSkinGadgetData> heroSkins = new ArrayList<>();
			AtomicInteger rowIndex = new AtomicInteger();

			rows.forEach(columns ->
			{
				// Don't check the first row
				if (rowIndex.incrementAndGet() == 1 || columns.size() < 5)
				{
					return;
				}

				HeroSkinGadgetData heroSkin = new HeroSkinGadgetData(sheetName, columns.get(0), columns.get(2), RewardRarity.valueOf(columns.get(1).toUpperCase()), new SkinData(columns.get(3), columns.get(4)));
				heroSkins.add(heroSkin);
			});

			skinData.put(sheetName, heroSkins);
		});

		SKIN_DATA = skinData;
		return skinData;
	}

	private static String[] getDescription(HeroSkinGadgetData skinData)
	{
		boolean hasDescription = !skinData.getDescription().isEmpty();
		String[] description = new String[hasDescription ? 3 : 2];

		description[0] = C.cGray + "Rarity: " + skinData.getRarity().getColor() + skinData.getRarity().getName();
		description[1] = "";

		if (hasDescription)
		{
			description[2] = C.cGray + ChatColor.translateAlternateColorCodes('&', skinData.getDescription());
		}

		return description;
	}

	private final HeroSkinGadgetData _gadgetData;

	public HeroSkinGadget(GadgetManager manager, GameCosmeticCategory category, HeroSkinGadgetData skinData)
	{
		this(manager, category, skinData, CostConstants.FOUND_IN_MOBA_CHESTS);
	}

	public HeroSkinGadget(GadgetManager manager, GameCosmeticCategory category, HeroSkinGadgetData skinData, int cost)
	{
		super(manager, category, skinData.getName() + " (" + skinData.getHero() + ")", getDescription(skinData), cost, Material.GLASS, (byte) 0);

		setDisplayItem(skinData.getSkinData().getSkull());
		skinData.setGadget(this);
		_gadgetData = skinData;
	}

	public HeroSkinGadgetData getGadgetData()
	{
		return _gadgetData;
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		Manager.getGameCosmeticManager().getGadgetsFrom(GameDisplay.MOBA)
				.stream()
				.filter(
						gadget -> gadget instanceof HeroSkinGadget &&
						((HeroSkinGadget) gadget).getGadgetData().getHero().equals(_gadgetData.getHero()))
				.forEach(gadget -> gadget.disable(player));

		super.enableCustom(player, message);
	}
}
