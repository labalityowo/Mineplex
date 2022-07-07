package nautilus.game.arcade.game.games.wizards.spellinterfaces;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface SpellClickBlock
{
    public void castSpell(Player player, Block block);
}