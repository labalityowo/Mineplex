package nautilus.game.arcade.game.games.wizards.spells;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.wizards.Spell;
import nautilus.game.arcade.game.games.wizards.spellinterfaces.SpellClick;
import nautilus.game.arcade.game.games.wizards.spellinterfaces.SpellClickBlock;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class SpellFrostBarrier extends Spell implements SpellClick, SpellClickBlock
{
	private HashMap<Block, Long> _wallExpires = new HashMap<Block, Long>();

	@Override
	public void castSpell(Player player)
	{
		Location loc = player.getLocation().add(player.getLocation().getDirection().setY(0).normalize().multiply(1.5));
		
		castSpell(player, loc.getBlock().getRelative(BlockFace.DOWN));
	}

	@Override
	public void castSpell(Player player, Block block)
	{
		final Block starter = block.getRelative(BlockFace.UP);
		final int wallWidth = 4 + (getSpellLevel(player) * 2);
		final BlockFace facing = UtilShapes.getFacing(player.getEyeLocation().getYaw());
		final int wallHeight = 1 + getSpellLevel(player);

		new BukkitRunnable()
		{
			Block block = starter;
			int currentRun;

			@Override
			public void run()
			{

				currentRun++;

				BlockFace[] faces = UtilShapes.getCornerBlockFaces(block, facing);

				if (block.getType() == Material.AIR)
				{
					block.setTypeIdAndData(Material.ICE.getId(), (byte) 0, false);
					_wallExpires.put(block, System.currentTimeMillis() + ((20 + UtilMath.r(10)) * 1000L));
				}

				block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId());

				for (BlockFace face : faces)
				{
					for (int i = 1; i < wallWidth; i++)
					{

						Block b = block.getRelative(face.getModX() * i, 0, face.getModZ() * i);

						if (!UtilBlock.airFoliage(b))
							break;

						b.setTypeIdAndData(Material.ICE.getId(), (byte) 0, false);
						b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, b.getTypeId());

						_wallExpires.put(b, System.currentTimeMillis() + ((20 + UtilMath.r(10)) * 1000L));
					}
				}

				block = block.getRelative(BlockFace.UP);
				if (currentRun >= wallHeight)
				{
					cancel();
				}
			}
		}.runTaskTimer(Wizards.getArcadeManager().getPlugin(), 0, 5);

		charge(player);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		Block block = event.getBlock();

		if (_wallExpires.containsKey(block))
		{
			event.setCancelled(true);
			block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId());
			block.setType(Material.AIR);
		}
	}

	@EventHandler
	public void onBlockMelt(BlockFadeEvent event)
	{
		Block block = event.getBlock();

		if (_wallExpires.containsKey(block))
		{
			event.setCancelled(true);
			block.setType(Material.AIR);
		}
	}

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

				if (entry.getKey().getType() == Material.ICE)
				{
					entry.getKey().setType(Material.AIR);
				}
			}
		}
	}
}
