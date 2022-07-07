package nautilus.game.arcade.game.games.wizards.spells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.wizards.Spell;
import nautilus.game.arcade.game.games.wizards.spellinterfaces.SpellClick;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SpellRainbowRoad extends Spell implements SpellClick
{
	final BlockFace[] radial =
		{
				BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST, BlockFace.NORTH,
				BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST
		};
	final int[] _rainbow = new int[]
		{
				1, 2, 3, 4, 5, 6, 9, 10, 11, 13, 14
		};

	private HashMap<Block, Long> _wallExpires = new HashMap<Block, Long>();

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		Iterator<Entry<Block, Long>> itel = _wallExpires.entrySet().iterator();

		while (itel.hasNext())
		{
			Entry<Block, Long> entry = itel.next();

			if (entry.getValue() < System.currentTimeMillis())
			{
				itel.remove();

				if (entry.getKey().getType() == Material.STAINED_GLASS)
				{
					entry.getKey().setType(Material.AIR);
				}
			}
		}
	}

	@Override
	public void castSpell(Player p)
	{
		final BlockFace face = radial[Math.round(p.getLocation().getYaw() / 45f) & 0x7];

		double yMod = Math.min(Math.max(p.getLocation().getPitch() / 30, -1), 1);

		final Vector vector = new Vector(face.getModX(), -yMod, face.getModZ());

		final Location loc = p.getLocation().getBlock().getLocation().add(0.5, -0.5, 0.5);

		final int maxDist = 3 + (10 * getSpellLevel(p));

		makeRoad(loc, face, 0);

		new BukkitRunnable()
		{
			int blocks;
			int colorProgress;

			@Override
			public void run()
			{
				if (!Wizards.IsLive() || blocks++ >= maxDist)
				{
					cancel();
					return;
				}

				colorProgress = makeRoad(loc, face, colorProgress);

				loc.add(vector);
			}
		}.runTaskTimer(Wizards.getArcadeManager().getPlugin(), 5, 5);

		charge(p);
	}

	private int makeRoad(Location loc, BlockFace face, int colorProgress)
	{
		Block block = loc.getBlock();

		BlockFace[] faces = UtilShapes.getSideBlockFaces(face);

		ArrayList<Block> bs = new ArrayList<Block>();

		bs.add(block);

		for (int i = 0; i < 2; i++)
		{
			bs.add(block.getRelative(faces[i]));
		}

		bs.addAll(UtilShapes.getDiagonalBlocks(block, face, 1));

		boolean playSound = false;

		for (Block b : bs)
		{
			if (!Wizards.isInsideMap(b.getLocation()))
			{
				continue;
			}

			if (!_wallExpires.containsKey(block) && UtilBlock.solid(b))
			{
				continue;
			}

			b.setType(Material.STAINED_GLASS);
			b.setData((byte) _rainbow[colorProgress++ % _rainbow.length]);

			b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, Material.WOOL, b.getData());

			_wallExpires.put(b, System.currentTimeMillis() + ((14 + UtilMath.r(7)) * 1000L));

			playSound = true;
		}

		if (playSound)
		{
			block.getWorld().playSound(block.getLocation(), Sound.ZOMBIE_UNFECT, 1.5F, 1);
		}

		return colorProgress;
	}
}
