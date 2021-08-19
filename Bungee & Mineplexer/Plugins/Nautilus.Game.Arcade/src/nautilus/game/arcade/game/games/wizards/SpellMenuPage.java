package nautilus.game.arcade.game.games.wizards;

import java.util.ArrayList;
import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.donation.DonationManager;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.shop.page.ShopPageBase;
import nautilus.game.arcade.game.games.wizards.SpellType.SpellElement;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SpellMenuPage extends ShopPageBase<WizardSpellMenu, WizardSpellMenuShop>
{
	private Wizards _wizard;

	public SpellMenuPage(WizardSpellMenu plugin, WizardSpellMenuShop shop, CoreClientManager clientManager,
			DonationManager donationManager, Player player, Wizards wizard)
	{
		super(plugin, shop, clientManager, donationManager, "Spell Menu", player);
		_wizard = wizard;
		buildPage();
	}

	@Override
	protected void buildPage()
	{
		Wizard wizard = getWizards().getWizard(getPlayer());

		ArrayList<Integer> usedNumbers = new ArrayList<Integer>();

		for (SpellElement ele : SpellElement.values())
		{
			addItem(ele.getSlot(), new ShopItem(ele.getIcon(), ele.name(), ele.name(), 1, true, true));

			for (int i = ele.getFirstSlot(); i <= ele.getSecondSlot(); i++)
			{
				usedNumbers.add(i);
			}
		}

		for (int i = 0; i < 54; i++)
		{
			SpellType spell = null;

			for (SpellType spells : SpellType.values())
			{
				if (spells.getSlot() == i)
				{
					spell = spells;
					break;
				}
			}

			if (usedNumbers.contains(i % 9) && spell != null)
			{

				int spellLevel = wizard == null ? 1 : wizard.getSpellLevel(spell);

				if (spellLevel > 0)
				{
					ItemBuilder builder = new ItemBuilder(spell.getSpellItem());

					builder.setTitle(spell.getElement().getColor() + C.Bold + spell.getSpellName());

					builder.setAmount(spellLevel);

					builder.addLore("");

					if (wizard == null)
					{
						builder.addLore(C.cYellow + C.Bold + "Max Level: " + C.cWhite + spell.getMaxLevel());
					}
					else
					{
						builder.addLore(C.cYellow + C.Bold + "Spell Level: " + C.cWhite + spellLevel);
					}

					builder.addLore(C.cYellow + C.Bold + "Mana Cost: " + C.cWhite
							+ (wizard == null ? spell.getBaseManaCost() : spell.getManaCost(wizard)));
					builder.addLore(C.cYellow + C.Bold + "Cooldown: " + C.cWhite
							+ (wizard == null ? spell.getBaseCooldown() : spell.getSpellCooldown(wizard)) + " seconds");
					builder.addLore("");

					for (String lore : spell.getDesc())
					{
						builder.addLore(C.cGray + lore, 40);
					}

					if (wizard == null)
					{
						addItem(i, new ShopItem(builder.build(), spell.name(), spell.name(), 1, true, true));
					}
					else
					{
						builder.addLore("");

						builder.addLore(C.cGreen + C.Bold + "Left-Click" + C.cWhite + " Bind to Wand");

						builder.addLore(C.cGreen + C.Bold + "Right-Click" + C.cWhite + " Quickcast Spell");

						addButton(i, new ShopItem(builder.build(), spell.name(), spell.name(), 1, true, true), new SpellButton(
								this, spell));
					}
				}
				else
				{
					addItem(i, new ShopItem(new ItemBuilder(Material.INK_SACK, 1, (byte) 6).setTitle(C.cRed + C.Bold + "Unknown")
							.build(), "Unknown", "Unknown", 1, true, true));
				}
			}
			else if (!usedNumbers.contains(i % 9))
			{
				addItem(i, new ShopItem(new ItemBuilder(Material.INK_SACK, 1, (byte) 9).setTitle(C.cRed + "").build(), "No Item",
						"No Item", 1, true, true));
			}
		}
	}

	public Wizards getWizards()
	{
		return _wizard;
	}
}
