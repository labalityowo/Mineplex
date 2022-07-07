package nautilus.game.arcade.game.games.wizards;

import mineplex.core.common.util.C;
import mineplex.core.shop.item.IButton;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class SpellButton implements IButton
{

	private SpellType _spell;
	private SpellMenuPage _spellPage;

	public SpellButton(SpellMenuPage spellPage, SpellType spell)
	{
		_spell = spell;
		_spellPage = spellPage;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		Wizard wizard = _spellPage.getWizards().getWizard(player);

		if (player.getInventory().getHeldItemSlot() >= wizard.getWandsOwned())
		{
			return;
		}

		if (wizard != null)
		{
			if (clickType.isLeftClick())
			{
				wizard.setSpell(player.getInventory().getHeldItemSlot(), _spell);

				player.sendMessage(C.cBlue + "Spell on wand set to " + _spell.getElement().getColor() + _spell.getSpellName());

				player.playSound(player.getLocation(), Sound.ORB_PICKUP, 10, 1);
			}
			else
			{
				_spellPage.getWizards().castSpell(player, wizard, _spell, null);
			}

			_spellPage.getWizards().drawUtilTextBottom(player);
			_spellPage.getWizards().changeWandsTitles(player);
			_spellPage.getWizards().changeWandsType(player, -1, player.getInventory().getHeldItemSlot());

			player.closeInventory();
		}
	}

}