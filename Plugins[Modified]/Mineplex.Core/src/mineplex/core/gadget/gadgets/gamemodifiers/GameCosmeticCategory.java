package mineplex.core.gadget.gadgets.gamemodifiers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.cosmetic.ui.page.GadgetPage;
import mineplex.core.cosmetic.ui.page.gamemodifiers.GameCosmeticCategoryPage;
import mineplex.core.cosmetic.ui.page.gamemodifiers.GameCosmeticGadgetPage;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.GameModifierGadget;

public abstract class GameCosmeticCategory
{

	private final GameCosmeticType _type;
	private final GadgetManager _manager;
	private final String _categoryName;
	private final List<GameModifierGadget> _gadgets;
	private final ItemStack _itemStack;
	private final boolean _allowMultiple;

	public GameCosmeticCategory(GameCosmeticType type, String categoryName, ItemStack itemStack, boolean allowMultiple)
	{
		_type = type;
		_manager = type.getManager();
		_categoryName = categoryName;
		_gadgets = new ArrayList<>();
		_itemStack = itemStack;
		_allowMultiple = allowMultiple;

		addGadgets();

		type.addCategory(this);
	}

	public abstract void addGadgets();

	protected void addGameGadget(GameModifierGadget gadget)
	{
		_gadgets.add(gadget);
		_manager.addGadget(gadget);
	}

	public GadgetPage getGadgetPage(CosmeticManager manager, Player player)
	{
		return new GameCosmeticGadgetPage(manager, manager.getShop(), _manager.getClientManager(), _manager.getDonationManager(), _categoryName, player, null, this);
	}

	public GadgetPage getGadgetPage(GameCosmeticCategoryPage parent)
	{
		return new GameCosmeticGadgetPage(parent.getPlugin(), parent.getShop(), parent.getClientManager(), parent.getDonationManager(), _categoryName, parent.getClient().GetPlayer(), parent, this);
	}

	public GameCosmeticType getType()
	{
		return _type;
	}

	public String getCategoryName()
	{
		return _categoryName;
	}

	public List<GameModifierGadget> getGadgets()
	{
		return _gadgets;
	}

	public ItemStack getItemStack()
	{
		return _itemStack;
	}

	public boolean isAllowingMultiple()
	{
		return _allowMultiple;
	}
}
