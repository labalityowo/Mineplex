package mineplex.core.gadget.gadgets.wineffect;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.WinEffectGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class WinEffectPartyAnimal extends WinEffectGadget
{

	private static final EntityType[] ENTITY_TYPES =
			{
					EntityType.PIG,
					EntityType.SHEEP,
					EntityType.CHICKEN,
					EntityType.COW
			};
	private static final int ANIMALS = 20;

	private final Set<LivingEntity> _entities;

	public WinEffectPartyAnimal(GadgetManager manager)
	{
		super(manager, "Party Animal", new String[]
				{
						C.cGray + "Anyone want any cake?"
				}, CostConstants.FOUND_IN_TREASURE_CHESTS, Material.CAKE, (byte) 0);

		_schematicName = "CakePodium";
		_entities = new HashSet<>();
	}

	@Override
	public void play()
	{
		_entities.add((LivingEntity) getNPC(_player, getBaseLocation(), false).getEntity().getBukkitEntity());
		List<Location> circle = UtilShapes.getPointsInCircle(getBaseLocation(), ANIMALS, 6);

		int i = 0;
		for (Location location : circle)
		{
			if (location.getBlock().getType() != Material.AIR)
			{
				location.add(0, 1, 0);
			}

			location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, getBaseLocation())));
			location.setPitch(UtilMath.rRange(-2, 5));

			LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, UtilMath.randomElement(ENTITY_TYPES));

			UtilEnt.vegetate(entity, true);
			UtilEnt.setFakeHead(entity, true);

			if (i < _other.size())
			{
				entity.setCustomName(C.cYellow + _other.get(i++).getName());
				entity.setCustomNameVisible(true);
			}

			_entities.add(entity);
		}
	}

	@Override
	public void finish()
	{
		_entities.clear();
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER || !isRunning())
		{
			return;
		}

		_entities.forEach(entity ->
		{
			double random = Math.random();

			if (random > 0.3 || !UtilEnt.isGrounded(entity))
			{
				return;
			}

			entity.setVelocity(new Vector(0, 0.3 + UtilMath.random(0.2, 0.5), 0));

			if (random < 0.15)
			{
				UtilFirework.launchFirework(entity.getLocation(), FireworkEffect.builder()
						.with(Type.values()[UtilMath.r(Type.values().length)])
						.withColor(Color.fromRGB(UtilMath.r(256), UtilMath.r(256), UtilMath.r(256)))
						.build(), null, 2);
			}
		});
	}
}
