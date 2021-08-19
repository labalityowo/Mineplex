package nautilus.game.arcade.game.games.christmas;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.recharge.Recharge;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class Sleigh
{

	public ChristmasCommon Host;

	//This is the central entity, all other sleigh entities have location relative to this.
	private Entity CentralEntity;
	private List<SleighPart> SleighEnts;
	private List<SleighHorse> SleighHorses = new ArrayList<>();

	private List<SleighPart> PresentSlots;

	private List<Location> PresentsCollected = new ArrayList<>();

	private Location Target;

	private Entity Santa;

	private boolean _loaded;

	public void setupSleigh(ChristmasCommon host, Location loc)
	{
		Host = host;

		Host.CreatureAllowOverride = true;

		Target = loc.clone();

		CentralEntity = loc.getWorld().spawn(loc, Chicken.class);
		UtilEnt.vegetate(CentralEntity, true);
		UtilEnt.ghost(CentralEntity, true, false);
		Host.Manager.GetCondition().Factory().Invisible("Sleigh", (LivingEntity) CentralEntity, null, Double.MAX_VALUE, 3, false, false, true);

		//Presents
		PresentSlots = new ArrayList<>();

		PresentSlots.add(new SleighPart(this, 2, 0, 0, loc.clone(), -1, -2));
		PresentSlots.add(new SleighPart(this, 2, 0, 0, loc.clone(), 0, -2));
		PresentSlots.add(new SleighPart(this, 2, 0, 0, loc.clone(), 1, -2));
		PresentSlots.add(new SleighPart(this, 2, 0, 0, loc.clone(), -1, -1));
		PresentSlots.add(new SleighPart(this, 2, 0, 0, loc.clone(), 0, -1));
		PresentSlots.add(new SleighPart(this, 2, 0, 0, loc.clone(), 1, -1));

		PresentSlots.add(new SleighPart(this, 6, 0, 0, loc.clone(), -1, -2));
		PresentSlots.add(new SleighPart(this, 6, 0, 0, loc.clone(), 0, -2));
		PresentSlots.add(new SleighPart(this, 6, 0, 0, loc.clone(), 1, -2));
		PresentSlots.add(new SleighPart(this, 6, 0, 0, loc.clone(), -1, -1));
		PresentSlots.add(new SleighPart(this, 6, 0, 0, loc.clone(), 0, -1));
		PresentSlots.add(new SleighPart(this, 6, 0, 0, loc.clone(), 1, -1));

		//Sleigh
		SleighEnts = new ArrayList<>();

		SleighEnts.addAll(PresentSlots);

		SleighEnts.add(new SleighPart(this, 0, 159, 14, loc.clone(), 0, -3));
		SleighEnts.add(new SleighPart(this, 0, 159, 14, loc.clone(), -1, -3));
		SleighEnts.add(new SleighPart(this, 0, 159, 14, loc.clone(), -2, -3));
		SleighEnts.add(new SleighPart(this, 0, 159, 14, loc.clone(), 1, -3));
		SleighEnts.add(new SleighPart(this, 0, 159, 14, loc.clone(), 2, -3));

		SleighEnts.add(new SleighPart(this, 0, 159, 14, loc.clone(), -2, -2));
		SleighEnts.add(new SleighPart(this, 0, 44, 7, loc.clone(), -1, -2));
		SleighEnts.add(new SleighPart(this, 0, 44, 7, loc.clone(), 0, -2));
		SleighEnts.add(new SleighPart(this, 0, 44, 7, loc.clone(), 1, -2));
		SleighEnts.add(new SleighPart(this, 0, 159, 14, loc.clone(), 2, -2));

		SleighEnts.add(new SleighPart(this, 0, 159, 14, loc.clone(), -2, -1));
		SleighEnts.add(new SleighPart(this, 0, 44, 7, loc.clone(), -1, -1));
		SleighEnts.add(new SleighPart(this, 0, 44, 7, loc.clone(), 0, -1));
		SleighEnts.add(new SleighPart(this, 0, 44, 7, loc.clone(), 1, -1));
		SleighEnts.add(new SleighPart(this, 0, 159, 14, loc.clone(), 2, -1));


		SleighEnts.add(new SleighPart(this, 0, 159, 14, loc.clone(), -2, 0));
		SleighEnts.add(new SleighPart(this, 0, 159, 14, loc.clone(), -1, 0));
		SleighEnts.add(new SleighPart(this, 0, 159, 14, loc.clone(), 0, 0));
		SleighEnts.add(new SleighPart(this, 0, 159, 14, loc.clone(), 1, 0));
		SleighEnts.add(new SleighPart(this, 0, 159, 14, loc.clone(), 2, 0));

		SleighEnts.add(new SleighPart(this, 0, 159, 14, loc.clone(), -2, 1));
		SleighEnts.add(new SleighPart(this, 0, 44, 7, loc.clone(), -1, 1));
		SleighEnts.add(new SleighPart(this, 0, 159, 15, loc.clone(), 0, 1));
		SleighEnts.add(new SleighPart(this, 0, 44, 7, loc.clone(), 1, 1));
		SleighEnts.add(new SleighPart(this, 0, 159, 14, loc.clone(), 2, 1));

		SleighEnts.add(new SleighPart(this, 0, 44, 7, loc.clone(), -2, 2));
		SleighEnts.add(new SleighPart(this, 0, 44, 7, loc.clone(), -1, 2));
		SleighEnts.add(new SleighPart(this, 0, 44, 7, loc.clone(), 0, 2));
		SleighEnts.add(new SleighPart(this, 0, 44, 7, loc.clone(), 1, 2));
		SleighEnts.add(new SleighPart(this, 0, 44, 7, loc.clone(), 2, 2));

		SleighEnts.add(new SleighPart(this, 0, 159, 14, loc.clone(), -2, 3));
		SleighEnts.add(new SleighPart(this, 0, 159, 14, loc.clone(), -1, 3));
		SleighEnts.add(new SleighPart(this, 0, 159, 14, loc.clone(), 0, 3));
		SleighEnts.add(new SleighPart(this, 0, 159, 14, loc.clone(), 1, 3));
		SleighEnts.add(new SleighPart(this, 0, 159, 14, loc.clone(), 2, 3));

		//Santa
		SleighPart santa = new SleighPart(this, 3, 0, 0, loc.clone(), 0, 1);
		Santa = santa.AddSanta();
		SleighEnts.add(santa);

		SleighHorses.add(new SleighHorse(loc.clone(), "Dasher", -1.5, 8));
		SleighHorses.add(new SleighHorse(loc.clone(), "Dancer", 1.5, 8));

		SleighHorses.add(new SleighHorse(loc.clone(), "Prancer", -1.5, 11));
		SleighHorses.add(new SleighHorse(loc.clone(), "Vixen", 1.5, 11));

		SleighHorses.add(new SleighHorse(loc.clone(), "Comet", -1.5, 14));
		SleighHorses.add(new SleighHorse(loc.clone(), "Cupid", 1.5, 14));

		SleighHorses.add(new SleighHorse(loc.clone(), "Donner", -1.5, 17));
		SleighHorses.add(new SleighHorse(loc.clone(), "Blitzen", 1.5, 17));

		for (SleighHorse horse : SleighHorses)
		{
			horse.spawnHorse();
		}

		_loaded = true;
	}

	public Location GetLocation()
	{
		return CentralEntity.getLocation();
	}

	public void SetTarget(Location loc)
	{
		Target = loc;
	}

	public Location getTarget()
	{
		return Target;
	}

	public void Update()
	{
		if (!_loaded)
		{
			return;
		}

		Bump();

		if (Target == null)
		{
			return;
		}

		Move(CentralEntity, Target, 1);

		Santa.setTicksLived(1);

		//Move Sleigh
		for (SleighPart part : SleighEnts)
		{
			part.RefreshBlocks();

			if (Move(part.Ent, CentralEntity.getLocation().add(part.OffsetX, 0, part.OffsetZ), 1.4))
			{
				if (part.OffsetZ == -3 || Math.abs(part.OffsetX) == 2)
				{
					if (Math.random() > 0.95)
					{
						part.Ent.getWorld().playEffect(part.Ent.getLocation().subtract(0, 1, 0), Effect.STEP_SOUND, 80);
					}
				}
			}
		}

		//Move Horses
		for (SleighHorse ent : SleighHorses)
		{
			Move(ent.Ent, CentralEntity.getLocation().add(ent.OffsetX, 0, ent.OffsetZ), 1.4);
		}
	}

	public boolean Move(Entity ent, Location target, double speed)
	{
		return UtilEnt.CreatureMoveFast(ent, target, (float) speed);
	}

	private void Bump()
	{
		double centralYZ = CentralEntity.getLocation().getZ();

		for (Player player : Host.GetPlayers(true))
		{
			if (!Recharge.Instance.use(player, "Sleigh Bump", 400, false, false))
			{
				continue;
			}

			double offset = centralYZ - player.getLocation().getZ();

			if (offset > 26)
			{
				player.teleport(CentralEntity);
			}
			else if (offset > 24)
			{
				Host.getArcadeManager().GetDamage().NewDamageEvent(player, null, null, DamageCause.CUSTOM, 1, false, true, true, Host.GetName(), "Too Slow");
				Host.sendSantaMessage(player, "Careful " + player.getName() + "! Keep up with my Sleigh!", null);
				UtilAction.velocity(player, UtilAlg.getTrajectory2d(player, CentralEntity), 0.7, true, 0.2, 0, 0, true);
			}
		}

		for (SleighPart part : SleighEnts)
		{
			bumpEntities(part.Ent.getLocation());
		}

		for (SleighHorse part : SleighHorses)
		{
			bumpEntities(part.Ent.getLocation());
		}
	}

	private void bumpEntities(Location location)
	{
		for (Entity ent : UtilEnt.getInRadius(location, 1).keySet())
		{
			if (isPart(ent))
			{
				continue;
			}

			if (ent instanceof Player)
			{
				if (!Recharge.Instance.use((Player) ent, "Sleigh Bump", 200, false, false))
				{
					continue;
				}
			}

			UtilAction.velocity(ent, UtilAlg.getTrajectory2d(CentralEntity, ent), 0.5, true, 0.3, 0, 0, true);
		}
	}

	public void unloadSleigh()
	{
		SleighEnts.forEach(part -> part.Ent.remove());
		SleighHorses.forEach(part -> part.Ent.remove());
		PresentSlots.forEach(part -> part.Ent.remove());
		Santa.remove();
		CentralEntity.remove();
		_loaded = false;
	}

	public boolean HasPresent(Location loc)
	{
		return PresentsCollected.contains(loc);
	}

	public void AddPresent(Location loc)
	{
		PresentsCollected.add(loc);

		SleighPart part = PresentSlots.remove(0);
		if (part == null)
		{
			return;
		}

		part.SetPresent();
	}

	public List<Location> getPresents()
	{
		return PresentsCollected;
	}

	public void onDamage(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity().equals(CentralEntity))
		{
			event.SetCancelled("Sleigh onDamage");
			return;
		}

		for (SleighPart part : SleighEnts)
		{
			if (part.HasEntity(event.GetDamageeEntity()))
			{
				event.SetCancelled("Sleigh onDamage");
				return;
			}
		}

		for (SleighHorse part : SleighHorses)
		{
			if (part.HasEntity(event.GetDamageeEntity()))
			{
				event.SetCancelled("Sleigh onDamage");
				return;
			}
		}
	}

	public List<SleighHorse> getHorses()
	{
		return SleighHorses;
	}

	public Entity getSanta()
	{
		return Santa;
	}

	private boolean isPart(Entity ent)
	{
		if (ent == null)
		{
			return false;
		}

		if (ent.equals(CentralEntity) || ent.equals(Santa))
		{
			return true;
		}


		for (SleighPart part : SleighEnts)
		{
			if (part.Block == ent || part.Ent == ent || (ent instanceof LivingEntity && part.HasEntity((LivingEntity) ent)))
			{
				return true;
			}
		}

		for (SleighHorse horse : SleighHorses)
		{
			if (horse.Ent == ent || (ent instanceof LivingEntity && horse.HasEntity((LivingEntity) ent)))
			{
				return true;
			}
		}

		for (SleighPart part : PresentSlots)
		{
			if (part.Block == ent || part.Ent == ent || (ent instanceof LivingEntity && part.HasEntity((LivingEntity) ent)))
			{
				return true;
			}
		}

		return false;
	}
}
