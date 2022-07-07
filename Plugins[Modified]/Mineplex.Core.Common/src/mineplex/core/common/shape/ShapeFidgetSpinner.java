package mineplex.core.common.shape;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;

public class ShapeFidgetSpinner extends Shape implements CosmeticShape
{

	public ShapeFidgetSpinner(int petalCount, int rotationDegrees)
	{
		double x0, y0;
		double sinRotation = Math.sin(rotationDegrees);
		double cosRotation = Math.cos(rotationDegrees);

		for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / 90)
		{
			double radius = Math.sin(petalCount * theta);
			double x1 = radius * Math.cos(theta);
			double y1 = radius * Math.sin(theta);

			x0 = cosRotation * x1 - sinRotation * y1;
			y0 = 0.2 + sinRotation * x1 + cosRotation * y1;

			addPoint(new Vector(x0, y0, 0.4));
		}
	}

	@Override
	public void display(Location location)
	{
		Shape clone = clone();
		clone.rotateOnYAxis(Math.toRadians(location.getYaw() + 180));

		for (Vector vector : clone.getPoints())
		{
			UtilParticle.PlayParticleToAll(ParticleType.RED_DUST, location.clone().add(vector), null, 0, 1, ViewDist.NORMAL);
		}
	}
}
