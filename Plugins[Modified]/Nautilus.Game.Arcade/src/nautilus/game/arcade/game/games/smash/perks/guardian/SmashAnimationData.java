package nautilus.game.arcade.game.games.smash.perks.guardian;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.particles.effects.LineParticle;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.disguise.disguises.DisguiseGuardian;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.smash.TeamSuperSmash;

public class SmashAnimationData
{

	private static final double THETA_INCREMENTATION = Math.PI / 20;
	private static final int RADIUS = 3;
	private static final int DAMAGE_RADIUS = 11;
	private static final int VELOCITY_DAMAGE = 2;
	private static final int Y_INCREASE = 7;
	
	private ArcadeManager _manager;
	private Player _player;
	private ArmorStand _elder;
	private ArmorStand[] _guardians;
	private ArmorStand _target;

	private double _lastTheta;
	private double _thetaSpacing;
	
	private int _blockRadius;
	private int _duration;
	private int _ticks;
	
	public SmashAnimationData(ArcadeManager manager, Player player, int guardians, int duration)
	{
		_manager = manager;
		_player = player;
		_thetaSpacing = 2 * Math.PI / guardians;	
		_duration = duration;
		
		DisguiseManager disguiseManager = manager.GetDisguise();
		Location location = player.getLocation().add(0, Y_INCREASE, 0);
		ArmorStand target = player.getWorld().spawn(player.getLocation(), ArmorStand.class);

		target.setArms(false);
		target.setSmall(true);
		target.setBasePlate(false);
		target.setGravity(false);
		target.setVisible(false);

		_target = target;

		_elder = player.getWorld().spawn(location, ArmorStand.class);
		_elder.setGravity(false);
		
		DisguiseGuardian disguiseElder = new DisguiseGuardian(_elder);
		disguiseElder.setElder(true);
		disguiseElder.setTarget(target.getEntityId());
		disguiseManager.disguise(disguiseElder);
		
		_guardians = new ArmorStand[guardians];

		for (int i = 0; i < guardians; i++)
		{
			ArmorStand guardian = player.getWorld().spawn(location, ArmorStand.class);
			
			guardian.setGravity(false);
			
			DisguiseGuardian disguise = new DisguiseGuardian(guardian);
			disguise.setTarget(target.getEntityId());
			disguiseManager.disguise(disguise);
			
			_guardians[i] = guardian;
			
		}
	}

	@SuppressWarnings("deprecation")
	public void update()
	{
		_ticks++;
		
		if (_ticks % 5 == 0)
		{
			_blockRadius++;
			
			BlockRestore blockRestore = _manager.GetBlockRestore();
			Map<Block, Double> inRadius = UtilBlock.getInRadius(_target.getLocation().getBlock(), _blockRadius, true);
			Random random = new Random();
			
			for (Block block : inRadius.keySet())
			{
				if (blockRestore.contains(block) || UtilBlock.airFoliage(block) || UtilBlock.liquid(block))
				{
					continue;
				}
				
				blockRestore.add(block, Material.PRISMARINE.getId(), (byte) random.nextInt(3), (long) (_duration + 5000 * Math.random()));
			}
			
			List<Player> team = TeamSuperSmash.getTeam(_manager, _player, true);
			for (Player player : UtilPlayer.getNearby(_target.getLocation(), DAMAGE_RADIUS))
			{
				if (team.contains(player))
				{
					continue;
				}
				
				double offset = UtilMath.offset(_target, player);
				Vector vector = UtilAlg.getTrajectory(player, _target);
				
				if (offset < DAMAGE_RADIUS / 3)
				{
					continue;
				}
				
				LineParticle lineParticle = new LineParticle(player.getEyeLocation(), _target.getEyeLocation(), vector, 0.2, offset, ParticleType.WITCH_MAGIC, UtilServer.getPlayers());

				while (!lineParticle.update())
				{
				}
				
				player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_REMEDY, 1, 0.75F);
				UtilAction.velocity(player, vector.setY(player.getLocation().getY() - 3 > _target.getLocation().getY() ? 0 : 0.5).multiply(0.7));
				_manager.GetDamage().NewDamageEvent(player, _player, null, DamageCause.CUSTOM, VELOCITY_DAMAGE, false, true, false, _player.getName(), "Mini Guardian");
			}
		}
		
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
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public ArmorStand getElder()
	{
		return _elder;
	}

	public ArmorStand[] getGuardians()
	{
		return _guardians;
	}
	
	public ArmorStand getTarget()
	{
		return _target;
	}
}
