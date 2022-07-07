package mineplex.game.clans.clans.worldevent.boss.ironwizard.abilities;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.updater.UpdateType;
import mineplex.game.clans.clans.worldevent.api.BossAbility;
import mineplex.game.clans.clans.worldevent.boss.ironwizard.GolemCreature;

public class GolemIronHook extends BossAbility<GolemCreature, IronGolem>
{
	private static final Integer MAX_TARGETS = 3;
	private boolean _shot, _complete;
	
	private List<Item> _hooks = new ArrayList<>();
	
	public GolemIronHook(GolemCreature creature)
	{
		super(creature);
		_shot = false;
		_complete = false;
	}
	
	private int getPosition(Player toAdd, List<Player> ordered, Map<Player, Double> distances)
	{
		int position = ordered.size();
		int index = 0;
		for (Player player : ordered)
		{
			if (distances.get(player) < distances.get(toAdd))
			{
				position = index;
			}
			index++;
		}
		
		return position;
	}
	
	private void shoot()
	{
		IronGolem wizard = getBoss().getEntity();
		List<Player> selections = new LinkedList<>();
		List<Player> targeted = new ArrayList<>();
		
		Map<Player, Double> near = UtilPlayer.getInRadius(wizard.getLocation(), 40D); 
		
		for (Player nearby : near.keySet())
		{
			if (nearby.getGameMode() == GameMode.CREATIVE || nearby.getGameMode() == GameMode.SPECTATOR)
			{
				continue;
			}
			
			if (selections.isEmpty())
			{
				selections.add(nearby);
			}
			else
			{
				selections.add(getPosition(nearby, selections, near), nearby);
			}
		}
		
		for (int i = 0; i < MAX_TARGETS; i++)
		{
			if (i < selections.size())
			{
				targeted.add(selections.get(i));
			}
		}
		
		if (targeted.isEmpty())
		{
			_complete = true;
			setFinished();
			return;
		}
		
		for (Player target : targeted)
		{
			Item item = wizard.getWorld().dropItem(wizard.getEyeLocation().add(UtilAlg.getTrajectory(wizard, target)), ItemStackFactory.Instance.CreateStack(131));
			UtilAction.velocity(item, UtilAlg.getTrajectory(wizard, target).normalize(), 
					2, false, 0, 0.2, 20, false);
			 
			getBoss().getEvent().getProjectileManager().AddThrow(item, getBoss().getEntity(), new IronHook(getBoss().getEvent()), -1, true, true, true, true, 
					Sound.FIRE_IGNITE, 1.4f, 0.8f, ParticleType.CRIT, UpdateType.TICK, 1f);

			item.getWorld().playSound(item.getLocation(), Sound.IRONGOLEM_THROW, 2f, 0.8f);
			_hooks.add(item);
		}
	}
	
	@Override
	public int getCooldown()
	{
		return 10;
	}

	@Override
	public boolean canMove()
	{
		return true;
	}

	@Override
	public boolean inProgress()
	{
		return false;
	}

	@Override
	public boolean hasFinished()
	{
		return _complete;
	}

	@Override
	public void setFinished()
	{
		for (Item hook : _hooks)
		{
			if (!hook.isDead() && hook.isValid())
			{
				getBoss().getEvent().getProjectileManager().deleteThrown(hook);
				hook.remove();
			}
		}
		
		_hooks.clear();
	}

	@Override
	public void tick()
	{
		if (_shot)
		{
			return;
		}
		shoot();
		_shot = true;
		Bukkit.getScheduler().runTaskLater(UtilServer.getPlugin(), () ->
		{
			_complete = true;
			setFinished();
		}, 2 * 20);
	}
}