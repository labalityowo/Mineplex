package nautilus.game.arcade.game.games.wizards.spells;

import nautilus.game.arcade.game.games.wizards.Spell;
import nautilus.game.arcade.game.games.wizards.spellinterfaces.SpellClick;

import org.bukkit.entity.Player;

public class SpellSpeedBoost extends Spell implements SpellClick
{
	@Override
	public void castSpell(Player p)
	{
		Wizards.getArcadeManager().GetCondition().Factory().Speed("Speed Boost", p, p, 20, getSpellLevel(p), false, false, false);

		charge(p);
	}
}
