package mineplex.game.clans.clans.worldevent.boss.skeletonking.abilities;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.worldevent.api.BossAbility;
import mineplex.game.clans.clans.worldevent.api.EventCreatureDeathEvent;
import mineplex.game.clans.clans.worldevent.boss.skeletonking.SkeletonBoss;
import mineplex.game.clans.clans.worldevent.boss.skeletonking.SkeletonCreature;
import mineplex.game.clans.clans.worldevent.boss.skeletonking.minion.MinionType;
import mineplex.game.clans.clans.worldevent.boss.skeletonking.minion.WraithCreature;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class SkeletonWraithSummon extends BossAbility<SkeletonCreature, Skeleton>
{
	private static final int WRAITH_AMOUNT = 4;
	private static final double DISTANCE_FROM_KING = 4;
	private static final double FINAL_SKELETON_HEALTH = 100;
	private static final double FINAL_WRAITH_MULTIPLIER = 2;
	private final int WRAITH_AMOUNT_THIS_USE;
	
	private Map<WraithCreature, String> _wraiths = new HashMap<>();
	private Location[] _spawns;
	private int _ticks;
	
	public SkeletonWraithSummon(SkeletonCreature creature)
	{
		super(creature);
		
		_spawns = new Location[]
		{
			getEntity().getLocation().add(DISTANCE_FROM_KING, 0, DISTANCE_FROM_KING),
			getEntity().getLocation().add(DISTANCE_FROM_KING * -1, 0, DISTANCE_FROM_KING),
			getEntity().getLocation().add(DISTANCE_FROM_KING, 0, DISTANCE_FROM_KING * -1),
			getEntity().getLocation().add(DISTANCE_FROM_KING * -1, 0, DISTANCE_FROM_KING * -1),
			getEntity().getLocation().add(DISTANCE_FROM_KING / 2, 0, DISTANCE_FROM_KING / 2),
			getEntity().getLocation().add((DISTANCE_FROM_KING / 2) * -1, 0, DISTANCE_FROM_KING / 2),
			getEntity().getLocation().add(DISTANCE_FROM_KING / 2, 0, (DISTANCE_FROM_KING / 2) * -1),
			getEntity().getLocation().add((DISTANCE_FROM_KING / 2) * -1, 0, (DISTANCE_FROM_KING / 2) * -1)
		};
		
		if (creature.getHealth() <= FINAL_SKELETON_HEALTH)
		{
			WRAITH_AMOUNT_THIS_USE = (int)(WRAITH_AMOUNT * FINAL_WRAITH_MULTIPLIER);
		}
		else
		{
			WRAITH_AMOUNT_THIS_USE = WRAITH_AMOUNT;
		}
	}
	
	private String getNumberString(Integer number)
	{
		String num = number.toString();
		char last = num.toCharArray()[num.length() - 1];
		
		String formatted = number.toString();
		String ending = "";
		
		if (last == '1' && !num.equals("1" + last))
		{
			ending = "st";
		}
		if (last == '2' && !num.equals("1" + last))
		{
			ending = "nd";
		}
		if (last == '3' && !num.equals("1" + last))
		{
			ending = "rd";
		}
		if (ending.equals(""))
		{
			ending = "th";
		}
		
		return formatted + ending;
	}
	
	private void spawnWraith(Location loc, int number)
	{
		WraithCreature wraith = (WraithCreature)((SkeletonBoss)getBoss().getEvent()).spawnMinion(MinionType.WRAITH, loc);
		_wraiths.put(wraith, getNumberString(number));
	}
	
	@Override
	public int getCooldown()
	{
		return 0;
	}

	@Override
	public boolean canMove()
	{
		return false;
	}

	@Override
	public boolean inProgress()
	{
		return true;
	}

	@Override
	public boolean hasFinished()
	{
		return _wraiths.isEmpty() && _ticks > 40;
	}

	@Override
	public void setFinished()
	{
		for (WraithCreature wraith : _wraiths.keySet())
		{
			wraith.remove();
		}
		_wraiths.clear();
		_ticks = 41;
	}

	@Override
	public void tick()
	{
		if (_ticks == 0)
		{
			if (WRAITH_AMOUNT > 0)
			{
				for (int i = 0; i < WRAITH_AMOUNT_THIS_USE; i++)
				{
					int spawnIndex = i;
					if (spawnIndex >= _spawns.length)
					{
						spawnIndex = spawnIndex % _spawns.length;
					}
					spawnWraith(_spawns[spawnIndex], i + 1);
				}
				
				for (Player player : UtilPlayer.getInRadius(getEntity().getLocation(), 80).keySet())
				{
					if (player.isDead() || !player.isValid() || !player.isOnline())
					{
						continue;
					}
					if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)
					{
						continue;
					}
					
					player.sendMessage(F.main(getBoss().getEvent().getName(), "You must slay all " + WRAITH_AMOUNT_THIS_USE + " wraiths before continuing to fight the " +  getBoss().getEvent().getName() + "!"));
				}
			}
		}
		_ticks++;
		if (!hasFinished())
		{
			int ticks = 10;
			int hticks = 40;
			boolean up = getEntity().getTicksLived() % (hticks * 2) < hticks;
			int tick = getEntity().getTicksLived() % ticks;
			double htick = getEntity().getTicksLived() % hticks;
			int splits = 4;

			Location loc = getEntity().getLocation().add(0, 2, 0);

			for (double d = tick * (Math.PI * 2 / splits) / ticks; d < Math.PI * 2; d += Math.PI * 2 / splits)
			{
				Vector v = new Vector(Math.sin(d), 0, Math.cos(d));
				v.normalize().multiply(Math.max(0.2, Math.sin((htick / hticks) * Math.PI) * 1.0));
				v.setY((htick / hticks) * -2);
				if (up) v.setY(-2 + 2 * (htick / hticks));

				Location lloc = loc.clone().add(v);

				UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, lloc, null, 0f, 2, ViewDist.MAX);
			}
		}
	}
	
	@EventHandler
	public void onWraithDie(EventCreatureDeathEvent event)
	{
		if (event.getCreature() instanceof WraithCreature)
		{
			String number = _wraiths.remove(event.getCreature());
			if (number != null)
			{
				double remainPercent = new BigDecimal(_wraiths.size()).divide(new BigDecimal(WRAITH_AMOUNT_THIS_USE)).doubleValue();
				ChatColor remainColor = ChatColor.GREEN;
				if (remainPercent < .66)
				{
					remainColor = ChatColor.YELLOW;
				}
				if (remainPercent < .33)
				{
					remainColor = ChatColor.RED;
				}
				Bukkit.broadcastMessage(F.main(getBoss().getEvent().getName(), "A wraith has been slain!" + " (" + remainColor + _wraiths.size() + "/" + WRAITH_AMOUNT_THIS_USE + C.cGray + ") wraiths remaining!"));
				System.out.println(F.main(getBoss().getEvent().getName(), "The " + number + " wraith has been slain!"));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDamage(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity().equals(getBoss().getEntity()))
		{
			if (!hasFinished())
			{
				event.SetCancelled("Wraiths Alive");
			}
		}
	}
}