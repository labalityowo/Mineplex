package nautilus.game.arcade.game.games.wizards;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class Spell implements Listener
{

    private SpellType Spell;
    protected Wizards Wizards;

    /**
     * Charges him for the cost of the spell
     */
    public void charge(Player player)
    {
        Wizard wizard = Wizards.getWizard(player);

        wizard.setMana(wizard.getMana() - getSpell().getManaCost(wizard));
        wizard.setUsedSpell(getSpell());

        Wizards.drawUtilTextBottom(player);
        Wizards.changeWandsTitles(player);
    }

    public SpellType getSpell()
    {
        return Spell;
    }

    protected int getSpellLevel(Player player)
    {
        return Wizards.getWizard(player).getSpellLevel(getSpell());
    }

    public void setSpellType(SpellType spell)
    {
        Spell = spell;
    }

}
