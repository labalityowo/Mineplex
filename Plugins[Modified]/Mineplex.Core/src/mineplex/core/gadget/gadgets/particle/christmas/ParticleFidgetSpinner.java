package mineplex.core.gadget.gadgets.particle.christmas;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.shape.ShapeFidgetSpinner;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ParticleFidgetSpinner extends ParticleGadget
{

	private static final int MAX_ROTATION = 6;
	private static final int PETAL_COUNT = 3;
	private static final double HEIGHT = 0.9;

	private final List<ShapeFidgetSpinner> _shapes = new ArrayList<>(MAX_ROTATION);

	private int _shapeIndex;

	public ParticleFidgetSpinner(GadgetManager manager)
	{
		super(manager, "Fidget Spinner Aura",
				UtilText.splitLineToArray(C.cGray + "Grandma's gift to you this Christmas.", LineFormat.LORE),
				CostConstants.FOUND_IN_GINGERBREAD_CHESTS, Material.RECORD_3, (byte) 0);

		for (int rotation = 0; rotation < MAX_ROTATION; rotation++)
		{
			_shapes.add(new ShapeFidgetSpinner(PETAL_COUNT, rotation));
		}
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		Location location = player.getLocation().add(0, HEIGHT, 0);

		if (Manager.isMoving(player))
		{
			for (int i = 0; i < 5; i++)
			{
				UtilParticle.PlayParticleToAll(ParticleType.RED_DUST, UtilAlg.getRandomLocation(location, 0.5, 0.5, 0.5), null, 0, 1, ViewDist.NORMAL);
			}
			return;
		}

		ShapeFidgetSpinner shape = _shapes.get(_shapeIndex);
		shape.display(location);
	}

	@Override
	@EventHandler
	public void Caller(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		super.Caller(event);
		if (++_shapeIndex == _shapes.size())
		{
			_shapeIndex = 0;
		}
	}
}
