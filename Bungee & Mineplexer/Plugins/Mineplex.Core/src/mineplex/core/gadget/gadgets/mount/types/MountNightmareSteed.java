package mineplex.core.gadget.gadgets.mount.types;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.mount.HorseMount;
import mineplex.core.gadget.gadgets.mount.SingleEntityMountData;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MountNightmareSteed extends HorseMount
{

	private static final DustSpellColor COLOUR = new DustSpellColor(Color.BLACK);

	private boolean _foot = false;
	private final Map<Location, Long> _steps = new HashMap<>();

	public MountNightmareSteed(GadgetManager manager)
	{
		super(manager,
				"Nightmare Steed",
				UtilText.splitLineToArray(C.cGray + "The Nightmare Steed comes in the darkness of night, the fires of the underworld still trailing from its hooves.", LineFormat.LORE),
				CostConstants.FOUND_IN_HAUNTED_CHESTS,
				Material.WOOL,
				(byte) 15,
				Horse.Color.BLACK,
				Style.NONE,
				Variant.HORSE,
				1,
				null
		);
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() == UpdateType.SEC)
		{
			for (SingleEntityMountData<Horse> singleEntityMountData : getActiveMounts().values())
			{
				UtilParticle.PlayParticleToAll(UtilParticle.ParticleType.FOOTSTEP, singleEntityMountData.getEntity().getLocation(), 0f, 0f, 0f, 0f, 1, UtilParticle.ViewDist.NORMAL);
			}
		}

		if (event.getType() != UpdateType.FASTEST)
		{
			return;
		}

		for (SingleEntityMountData<Horse> data : getActiveMounts().values())
		{
			Entity entity = data.getEntity();

			_foot = !_foot;

			cleanSteps();

			if (!UtilEnt.isGrounded(entity))
			{
				return;
			}

			Vector offset;

			Vector dir = entity.getLocation().getDirection();
			dir.setY(0);
			dir.normalize();

			if (_foot)
			{
				offset = new Vector(dir.getZ() * -1, 0.1, dir.getX());
			}
			else
			{
				offset = new Vector(dir.getZ(), 0.1, dir.getX() * -1);
			}

			Location loc = entity.getLocation().add(offset.multiply(0.2));

			if (nearStep(loc))
			{
				return;
			}
			if (!UtilBlock.solid(loc.getBlock().getRelative(BlockFace.DOWN)))
			{
				return;
			}

			_steps.put(loc, System.currentTimeMillis());

			UtilParticle.PlayParticleToAll(UtilParticle.ParticleType.FOOTSTEP, loc, 0f, 0f, 0f, 0, 1, ViewDist.NORMAL);

			for (int i = 0; i < 10; i++)
			{
				Location randLoc = UtilAlg.getRandomLocation(data.getEntity().getLocation().clone().add(0, 1, 0), 1d);
				ColoredParticle coloredParticle = new ColoredParticle(UtilParticle.ParticleType.RED_DUST, COLOUR, randLoc);
				coloredParticle.display(2);
			}

		}
	}

	private void cleanSteps()
	{
		_steps.entrySet().removeIf(entry -> UtilTime.elapsed(entry.getValue(), 10000));
	}

	private boolean nearStep(Location loc)
	{
		for (Location other : _steps.keySet())
		{
			if (UtilMath.offset(loc, other) < 0.3)
			{
				return true;
			}
		}

		return false;
	}

}
