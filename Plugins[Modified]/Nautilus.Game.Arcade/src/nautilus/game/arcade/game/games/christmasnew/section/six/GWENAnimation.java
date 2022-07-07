package nautilus.game.arcade.game.games.christmasnew.section.six;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;

import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.disguise.disguises.DisguiseGuardian;

import nautilus.game.arcade.ArcadeManager;

public class GWENAnimation
{

	private static final double THETA_INCREMENTATION = Math.PI / 20;
	private static final int RADIUS = 3;
	private static final int Y_INCREASE = 7;

	private ArcadeManager _manager;
	private ArmorStand _elder;
	private ArmorStand[] _guardians;
	private ArmorStand _target;

	private double _lastTheta;
	private double _thetaSpacing;

	private int _blockRadius;
	private int _duration;
	private int _ticks;

	public GWENAnimation(ArcadeManager manager, Location location, int guardians, int duration)
	{
		_manager = manager;
		_thetaSpacing = 2 * Math.PI / guardians;
		_duration = duration;
		
		DisguiseManager disguiseManager = manager.GetDisguise();
		ArmorStand target = location.getWorld().spawn(location, ArmorStand.class);

		target.setArms(false);
		target.setSmall(true);
		target.setBasePlate(false);
		target.setGravity(false);
		target.setVisible(false);

		_target = target;

		location = location.clone().add(0, Y_INCREASE, 0);
		_elder = location.getWorld().spawn(location, ArmorStand.class);
		_elder.setGravity(false);
		
		DisguiseGuardian disguiseElder = new DisguiseGuardian(_elder);
		disguiseElder.setElder(true);
		disguiseElder.setTarget(target.getEntityId());
		disguiseManager.disguise(disguiseElder);
		
		_guardians = new ArmorStand[guardians];

		for (int i = 0; i < guardians; i++)
		{
			ArmorStand guardian = location.getWorld().spawn(location, ArmorStand.class);
			
			guardian.setGravity(false);
			
			DisguiseGuardian disguise = new DisguiseGuardian(guardian);
			disguise.setTarget(target.getEntityId());
			disguiseManager.disguise(disguise);
			
			_guardians[i] = guardian;
			
		}
	}

	@SuppressWarnings("deprecation")
	public boolean update()
	{
		_ticks++;
		
		if (_ticks % 5 == 0)
		{
			_blockRadius++;
			
			BlockRestore blockRestore = _manager.GetBlockRestore();
			Map<Block, Double> inRadius = UtilBlock.getInRadius(_target.getLocation().getBlock(), _blockRadius, true);

			for (Block block : inRadius.keySet())
			{
				if (blockRestore.contains(block) || UtilBlock.airFoliage(block) || UtilBlock.liquid(block))
				{
					continue;
				}
				
				blockRestore.add(block, Material.PRISMARINE.getId(), (byte) UtilMath.r(3), (long) (_duration + 5000 * Math.random()));
			}
		}

		if (_ticks % 3 == 0)
		{

			Location center = _target.getLocation().add(0, Y_INCREASE, 0);
			int i = 0;

			//Teleport the elder guardian around in a circle
			{
				double x = Math.cos(_lastTheta);
				double z = Math.sin(_lastTheta);

				center.add(x, 0, z);
				_elder.teleport(center);
				center.subtract(x, 0, z);
			}

			//Teleport all guardians around in a circle
			for (ArmorStand guardian : _guardians)
			{
				double theta = _lastTheta + _thetaSpacing * i++;
				double x = RADIUS * Math.cos(theta);
				double z = RADIUS * Math.sin(theta);

				center.add(x, 0, z);
				guardian.teleport(center);
				center.subtract(x, 0, z);
			}
			_lastTheta += THETA_INCREMENTATION;
		}

		return _ticks > _duration / 50D;
	}

	public void clean()
	{
		_elder.remove();

		for (ArmorStand stand : _guardians)
		{
			stand.remove();
		}
	}
}
