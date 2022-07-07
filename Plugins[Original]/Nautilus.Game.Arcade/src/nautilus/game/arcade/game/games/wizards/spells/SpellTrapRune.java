package nautilus.game.arcade.game.games.wizards.spells;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.wizards.Spell;
import nautilus.game.arcade.game.games.wizards.spellinterfaces.SpellClick;
import nautilus.game.arcade.game.games.wizards.spells.subclasses.TrapRune;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class SpellTrapRune extends Spell implements SpellClick
{
	private ArrayList<TrapRune> _runes = new ArrayList<TrapRune>();

	@EventHandler
	public void onTick(UpdateEvent event)
	{
		if (event.getType() == UpdateType.TICK)
		{
			Iterator<TrapRune> itel = _runes.iterator();
			while (itel.hasNext())
			{
				TrapRune rune = itel.next();

				if (rune.onRuneTick())
				{
					itel.remove();
				}
			}
		}
	}

	@Override
	public void castSpell(Player p)
	{
		List<Block> list = p.getLastTwoTargetBlocks(UtilBlock.blockAirFoliageSet, (getSpellLevel(p) * 4) + 4);

		if (list.size() > 1)
		{
			Location loc = list.get(0).getLocation().add(0.5, 0, 0.5);

			if (loc.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR)
			{
				return;
			}

			TrapRune rune = new TrapRune(Wizards, p, loc, getSpellLevel(p));
			if (!rune.isValid())
			{
				p.sendMessage(C.cGreen + "Cannot draw rune on wall");
				return;
			}

			rune.initialParticles();

			_runes.add(rune);
			charge(p);
		}
	}

}
