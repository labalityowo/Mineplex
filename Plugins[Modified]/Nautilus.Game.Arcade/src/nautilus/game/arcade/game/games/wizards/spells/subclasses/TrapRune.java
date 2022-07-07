package nautilus.game.arcade.game.games.wizards.spells.subclasses;

import java.util.ArrayList;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.minecraft.game.core.explosion.CustomExplosion;
import nautilus.game.arcade.game.games.wizards.Wizards;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class TrapRune
{

	private Location _runeLocation;
	private float _runeSize;
	private Player _runeCaster;
	private int _ticksLived;
	private Wizards _wizards;
	private int _spellLevel;

	public boolean onRuneTick()
	{

		if (!_runeCaster.isOnline() || UtilPlayer.isSpectator(_runeCaster))
		{
			return true;
		}
		else if (_ticksLived++ > 2000)
		{
			return true;
		}
		else
		{
			if (_ticksLived <= 100)
			{
				if (_ticksLived % 15 == 0)
				{
					initialParticles();
				}

				if (_ticksLived == 100)
				{
					UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, _runeLocation, 0, _runeSize / 4, 0, _runeSize / 4,
							(int) (_runeSize * 10),
							ViewDist.LONG, UtilServer.getPlayers());
				}
			}
			else
			{
				if (!isValid())
				{
					trapCard();
					return true;
				}
				else
				{
					for (Player player : _wizards.GetPlayers(true))
					{
						if (isInTrap(player.getLocation()))
						{
							trapCard();
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public TrapRune(Wizards wizards, Player player, Location location, int spellLevel)
	{
		_wizards = wizards;
		_runeCaster = player;
		_runeLocation = location;
		_spellLevel = spellLevel;
		_runeSize = Math.max(1, spellLevel * 0.8F);
	}

	public void initialParticles()
	{
		for (Location loc : getBox(0.3))
		{
			for (double y = 0; y < 1; y += 0.2)
			{
				_runeLocation.getWorld().spigot().playEffect(loc, Effect.SMALL_SMOKE, 0, 0, 0, 0, 0, 0, 1, 30);
			}
		}
	}

	public ArrayList<Location> getBox(double spacing)
	{
		ArrayList<Location> boxLocs = getBoxCorners();
		ArrayList<Location> returns = new ArrayList<Location>();

		for (int i = 0; i < boxLocs.size(); i++)
		{

			int a = i + 1 >= boxLocs.size() ? 0 : i + 1;
			returns.addAll(UtilShapes.getLinesDistancedPoints(boxLocs.get(i), boxLocs.get(a), spacing));
			returns.add(boxLocs.get(i));

		}
		return returns;
	}

	public ArrayList<Location> getBoxCorners()
	{
		ArrayList<Location> boxPoints = new ArrayList<Location>();

		boxPoints.add(_runeLocation.clone().add(-_runeSize, 0, -_runeSize));
		boxPoints.add(_runeLocation.clone().add(_runeSize, 0, -_runeSize));
		boxPoints.add(_runeLocation.clone().add(_runeSize, 0, _runeSize));
		boxPoints.add(_runeLocation.clone().add(-_runeSize, 0, _runeSize));

		return boxPoints;
	}

	public boolean isInTrap(Location loc)
	{
		if (loc.getX() >= _runeLocation.getX() - _runeSize && loc.getX() <= _runeLocation.getX() + _runeSize)
		{
			if (loc.getZ() >= _runeLocation.getZ() - _runeSize && loc.getZ() <= _runeLocation.getZ() + _runeSize)
			{
				if (loc.getY() >= _runeLocation.getY() - 0.1 && loc.getY() <= _runeLocation.getY() + 0.9)
				{
					return true;
				}
			}
		}
		return false;
	}

	public boolean isValid()
	{
		return !UtilBlock.solid(_runeLocation.getBlock())
				|| UtilBlock.solid(_runeLocation.getBlock().getRelative(BlockFace.DOWN));
		/*
		for (double x = -RuneSize; x <= RuneSize; x++)
		{
		    for (double z = -RuneSize; z <= RuneSize; z++)
		    {

		        Block b = RuneLocation.clone().add(x, 0, z).getBlock();
		        if (UtilBlock.solid(b) || !UtilBlock.solid(b.getRelative(BlockFace.DOWN)))
		        {
		            return false;
		        }

		    }
		}*/
	}

	public void trapCard()
	{
		_runeLocation.getWorld().playSound(_runeLocation, Sound.WITHER_SHOOT, 5, (float) _runeSize * 2);

		CustomExplosion explosion = new CustomExplosion(_wizards.getArcadeManager().GetDamage(), _wizards.getArcadeManager()
				.GetExplosion(), _runeLocation.clone().add(0, 0.3, 0), (float) _runeSize * 1.2F, "Trap Rune");

		explosion.setPlayer(_runeCaster, true);

		explosion.setBlockExplosionSize((float) _runeSize * 2F);

		explosion.setFallingBlockExplosion(true);

		explosion.setDropItems(false);
		
		explosion.setMaxDamage((_spellLevel * 4) + 6);

		explosion.explode();

		for (Location loc : getBox(0.3))
		{
			for (double y = 0; y < 1; y += 0.2)
			{
				_runeLocation.getWorld().spigot().playEffect(loc, Effect.SMOKE, 0, 0, 0, 0, 0, 0, 1, 30);
			}
		}
	}
}
