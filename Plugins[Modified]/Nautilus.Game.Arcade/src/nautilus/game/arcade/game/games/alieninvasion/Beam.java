package nautilus.game.arcade.game.games.alieninvasion;

import mineplex.core.common.util.*;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.particles.effects.LineParticle;
import nautilus.game.arcade.ArcadeManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.Set;

public class Beam extends LineParticle
{

	private static final int EXPLOSION_RADIUS = 20;
	private static final int BEAM_BLOCK_TIME = 1500;

	private final ArcadeManager _manager;
	private final Location _target;
	private final int _id;

	public Beam(ArcadeManager manager, int id, BeamSource source, Location target)
	{
		super(source.getSource(), target, null, 1, Integer.MAX_VALUE, ParticleType.FIREWORKS_SPARK, UtilServer.getPlayers());

		_manager = manager;
		_id = id;
		_target = target;
	}

	@Override
	public boolean update()
	{
		super.update();
		Location last = getLastLocation();
		boolean hit = UtilMath.offset(last, _target) < 5;

		if (hit)
		{
			last.getWorld().playSound(last, Sound.EXPLODE, 2f, 0.75f);
			UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, last, 4, 1, 4, 0.5F, 10, ViewDist.LONG);

			Set<Block> blocks = UtilBlock.getInRadius(last, EXPLOSION_RADIUS).keySet();
			//blocks.removeIf(block -> block.getRelative(BlockFace.DOWN).getType() == Material.AIR);

			blocks.forEach(block -> MapUtil.QuickChangeBlockAt(block.getLocation(), Material.AIR));
		}
		else
		{
			last.getWorld().playSound(last, Sound.ZOMBIE_REMEDY, 2f, 0.75f);
			_manager.GetBlockRestore().add(last.getBlock(), Material.SEA_LANTERN.getId(), (byte) 0, BEAM_BLOCK_TIME);
		}

		return hit;
	}

	public int getId()
	{
		return _id;
	}
}
