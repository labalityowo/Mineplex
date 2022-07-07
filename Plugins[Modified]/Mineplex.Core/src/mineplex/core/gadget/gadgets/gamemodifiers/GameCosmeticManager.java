package mineplex.core.gadget.gadgets.gamemodifiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.gadget.event.GadgetChangeEvent;
import mineplex.core.gadget.event.GadgetChangeEvent.GadgetState;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GameModifierGadget;
import mineplex.core.game.GameDisplay;

@ReflectivelyCreateMiniPlugin
// TODO Remove all the string literals for cosmetic categories
public class GameCosmeticManager extends MiniPlugin
{

	private final Map<GameCosmeticType, List<GameCosmeticCategory>> _cosmetics;

	private GameCosmeticManager()
	{
		super("Game Cosmetics");

		_cosmetics = new HashMap<>();
	}

	public void addCosmeticType(GameCosmeticType type, List<GameCosmeticCategory> cosmetics)
	{
		_cosmetics.put(type, cosmetics);
	}

	@EventHandler
	public void gadgetEquip(GadgetChangeEvent event)
	{
		if (event.getGadgetState() != GadgetState.ENABLED)
		{
			return;
		}

		Gadget gadget = event.getGadget();

		if (!(gadget instanceof GameModifierGadget))
		{
			return;
		}

		GameCosmeticCategory category = ((GameModifierGadget) gadget).getCategory();

		if (category.isAllowingMultiple())
		{
			return;
		}

		// Disable all other gadgets if not allowing multiple.
		category.getGadgets().stream()
				.filter(gadget1 -> !gadget.equals(gadget1))
				.forEach(gadget1 -> gadget1.disable(event.getPlayer()));
	}

	public GameModifierGadget getActiveCosmetic(Player player, GameDisplay gameType, String categoryName)
	{
		return getActiveCosmetic(player, gameType, categoryName, null);
	}

	public GameModifierGadget getActiveCosmetic(Player player, GameDisplay gameType, String categoryName, Predicate<GameModifierGadget> selector)
	{
		GameCosmeticCategory category = getCategoryFrom(gameType, categoryName);

		if (category == null)
		{
			return null;
		}

		List<GameModifierGadget> gadgets = category.getGadgets();

		if (gadgets == null)
		{
			return null;
		}

		for (GameModifierGadget gadget : gadgets)
		{
			if (selector != null && !selector.test(gadget))
			{
				continue;
			}

			if (gadget.isActive(player))
			{
				return gadget;
			}
		}

		return null;
	}

	public List<GameModifierGadget> getActiveCosmetics(Player player, GameDisplay gameType, String categoryName)
	{
		return getActiveCosmetics(player, gameType, categoryName, null);
	}

	public List<GameModifierGadget> getActiveCosmetics(Player player, GameDisplay gameType, String categoryName, Predicate<GameModifierGadget> selector)
	{
		GameCosmeticCategory category = getCategoryFrom(gameType, categoryName);

		if (category == null)
		{
			return null;
		}

		List<GameModifierGadget> gadgets = category.getGadgets();

		if (gadgets == null)
		{
			return null;
		}

		List<GameModifierGadget> gadgetsCloned = new ArrayList<>(gadgets);
		gadgetsCloned.removeIf(gadget -> !gadget.isActive(player));

		if (selector != null)
		{
			gadgets.removeIf(gadget -> !selector.test(gadget));
		}

		return gadgetsCloned;
	}

	public GameCosmeticCategory getCategoryFrom(GameDisplay gameType, String categoryName)
	{
		if (gameType == null || categoryName == null)
		{
			return null;
		}

		for (Entry<GameCosmeticType, List<GameCosmeticCategory>> entry : _cosmetics.entrySet())
		{
			GameCosmeticType type = entry.getKey();

			if (type.getGame() != gameType)
			{
				continue;
			}

			for (GameCosmeticCategory category : entry.getValue())
			{
				if (!category.getCategoryName().equals(categoryName))
				{
					continue;
				}

				return category;
			}
		}

		return null;
	}

	public GameModifierGadget getGadgetFrom(String name)
	{
		for (List<GameCosmeticCategory> categories : _cosmetics.values())
		{
			for (GameCosmeticCategory category : categories)
			{
				for (GameModifierGadget gadget : category.getGadgets())
				{
					if (gadget.getName().equals(name))
					{
						return gadget;
					}
				}
			}
		}

		return null;
	}

	public List<GameModifierGadget> getGadgetsFrom(GameDisplay gameType)
	{
		List<GameModifierGadget> gadgets = new ArrayList<>();
		GameCosmeticType type = null;

		for (GameCosmeticType cosmeticType : _cosmetics.keySet())
		{
			if (cosmeticType.getGame() == gameType)
			{
				type = cosmeticType;
				break;
			}
		}

		if (type == null)
		{
			return gadgets;
		}

		type.getCategories().forEach(category -> gadgets.addAll(category.getGadgets()));
		return gadgets;
	}

	public Map<GameCosmeticType, List<GameCosmeticCategory>> getTypes()
	{
		return _cosmetics;
	}

}
