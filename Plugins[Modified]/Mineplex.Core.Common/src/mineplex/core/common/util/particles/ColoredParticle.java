package mineplex.core.common.util.particles;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import mineplex.core.common.util.UtilParticle;

public class ColoredParticle extends ParticleData
{
	private ParticleColor _color;

	public ColoredParticle(UtilParticle.ParticleType particleType, ParticleColor color, Location location)
	{
		super(particleType, location);
		if ((particleType == UtilParticle.ParticleType.RED_DUST || particleType == UtilParticle.ParticleType.MOB_SPELL_AMBIENT)
				&& !(color instanceof DustSpellColor))
			throw new IllegalArgumentException("RED_DUST and MOB_SPELL_AMBIENT particle types require a DustSpellColor!");
		else if(particleType == UtilParticle.ParticleType.NOTE && !(color instanceof NoteColor))
			throw new IllegalArgumentException("NOTE particle type requires a NoteColor!");
		else if(particleType != UtilParticle.ParticleType.RED_DUST && particleType != UtilParticle.ParticleType.MOB_SPELL_AMBIENT
				&& particleType != UtilParticle.ParticleType.NOTE)
			throw new IllegalArgumentException("Particle Type must be RED_DUST, MOB_SPELL_AMBIENT!");
		_particleType = particleType;
		_color = color;
		_location = location;
	}

	@Override
	public void display(UtilParticle.ViewDist viewDist, Player... players)
	{
		float x = _color.getX();
		if (_particleType == UtilParticle.ParticleType.RED_DUST && x == 0)
			x = Float.MIN_NORMAL;
		UtilParticle.PlayParticle(_particleType, _location, x, _color.getY(), _color.getZ(), 1, 0, viewDist, players);
	}

	@Override
	public void display(int count, UtilParticle.ViewDist viewDist, Player... players)
	{
		// It's not possible to have colored particles with count, so just repeat it
		for (int i = 0; i < count; i++)
		{
			display(viewDist, players);
		}
	}

	public void setColor(ParticleColor color)
	{
		_color = color;
	}
}