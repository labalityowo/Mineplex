package nautilus.game.arcade.game.games.moba.shop;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilUI;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.menu.Button;
import mineplex.core.menu.Menu;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.MobaRole;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MobaShopMenu extends Menu<ArcadeManager>
{

	private static final MobaShopCategory CONSUMABLES = new MobaShopCategory("Consumables", Arrays.asList(
			new MobaItem(new ItemBuilder(Material.POTION)
					.setTitle(C.cGreenB + "Small Health Potion")
					.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 1, 0))
					.build(), 100),
			new MobaItem(new ItemBuilder(Material.POTION)
					.setTitle(C.cYellowB + "Large Health Potion")
					.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 1, 1))
					.build(), 200),
			new MobaItem(new ItemBuilder(Material.POTION)
					.setTitle(C.cYellowB + "Power Potion")
					.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 90 * 20, 0))
					.build(), 1000),
			new MobaItem(new ItemBuilder(Material.POTION)
					.setTitle(C.cYellowB + "Speed Potion")
					.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 180 * 20, 0))
					.build(), 500),
			new MobaItem(new ItemBuilder(Material.ENDER_PEARL)
					.setTitle(C.cYellowB + "Ender Pearl")
					.build(), 750)
	), new ItemStack(Material.POTION)).dropOnDeath().allowMultiple().dontTrackPurchases();
	private static final int SLOTS = 27;

	private final Moba _host;
	private final MobaShop _shop;
	private final List<MobaShopCategory> _categories;

	public MobaShopMenu(Moba host, MobaShop shop, MobaRole role)
	{
		super(role.getName() + " Upgrade Shop", host.getArcadeManager());

		_host = host;
		_shop = shop;
		_categories = new ArrayList<>();
	}

	protected void addCategory(MobaShopCategory category)
	{
		_categories.add(category);
	}

	protected void addConsumables()
	{
		_categories.add(CONSUMABLES);
	}

	@Override
	protected Button[] setUp(Player player)
	{
		Button[] buttons = new Button[SLOTS];
		int[] slots = UtilUI.getIndicesFor(_categories.size(), 1);
		int slot = 0;

		for (MobaShopCategory category : _categories)
		{
			ItemBuilder builder;
			MobaItem owned = null;

			for (MobaItem item : category.getItems())
			{
				if (_shop.ownsItem(player, item))
				{
					owned = item;
					break;
				}
			}

			if (owned == null)
			{
				builder = new ItemBuilder(category.getMenuItem());
			}
			else
			{
				builder = new ItemBuilder(owned.getItem());
			}

			builder.setTitle(C.cGreen + category.getName());
			builder.addLore("");

			if (category.isAllowingMultiple())
			{
				builder.addLore(C.cWhite + "Current Upgrades:");
				boolean ownsAtLeastOne = false;

				for (MobaItem item : category.getItems())
				{
					if (_shop.ownsItem(player, item))
					{
						ownsAtLeastOne = true;
						builder.addLore(" - " + item.getItem().getItemMeta().getDisplayName());
					}
				}

				if (!ownsAtLeastOne)
				{
					builder.addLore(" - None");
				}
			}
			else
			{
				builder.addLore(C.cWhite + "Current Upgrade: " + (owned == null ? C.cGray + "None" : owned.getItem().getItemMeta().getDisplayName()));
			}

			builder.addLore("", C.cYellow + "Click to view the upgrades.");

			buttons[slots[slot++]] = new MobaCategoryButton(builder.build(), getPlugin(), category);
		}

		return buttons;
	}

	class MobaCategoryButton extends Button<ArcadeManager>
	{

		private MobaShopCategory _category;

		public MobaCategoryButton(ItemStack itemStack, ArcadeManager plugin, MobaShopCategory category)
		{
			super(itemStack, plugin);

			_category = category;
		}

		@Override
		public void onClick(Player player, ClickType clickType)
		{
			new MobaShopCategoryMenu(_host, _shop, _category, getPlugin()).open(player);
		}
	}

	public List<MobaShopCategory> getCategories()
	{
		return _categories;
	}
}
