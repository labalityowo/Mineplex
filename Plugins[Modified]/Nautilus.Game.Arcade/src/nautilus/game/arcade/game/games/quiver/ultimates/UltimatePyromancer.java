package nautilus.game.arcade.game.games.quiver.ultimates;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;

public class UltimatePyromancer extends UltimatePerk
{

	private static final float CHARGE_PASSIVE = 0.4F;
	private static final float CHARGE_PAYLOAD = 0.4F;
	private static final float CHARGE_KILL = 5F;
	private static final float CHARGE_ASSIST = 2F;
		
	private Map<UUID, Integer> _tasks = new HashMap<>();
	private Map<UUID, Integer> _kills = new HashMap<>();

	public UltimatePyromancer(long length)
	{
		super("Fire Blossom", new String[] {}, length, CHARGE_PASSIVE, CHARGE_PAYLOAD, CHARGE_KILL, CHARGE_ASSIST);
	}
	
	@Override
	public void activate(Player player)
	{
		super.activate(player);
				
		player.setWalkSpeed(0.05F);
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, (int) ((getLength() / 1000) * 20), -10));
		
		_tasks.put(player.getUniqueId(), (new BukkitRunnable()
		{
			
			@Override
			public void run()
			{
				UtilParticle.PlayParticleToAll(ParticleType.LARGE_SMOKE, player.getLocation().add(0, 0.5, 0), 2F, 0, 2F, 0.01F, 5, ViewDist.LONG);
				UtilParticle.PlayParticleToAll(ParticleType.FLAME, player.getLocation().add(0, 0.5, 0), 2F, 0, 2F, 0.01F, 5, ViewDist.LONG);
				
				Arrow arrow = player.launchProjectile(Arrow.class);
				arrow.setCritical(true);
				arrow.setVelocity(new Vector((Math.random() - 0.5) * 9, 0.1, (Math.random() - 0.5) * 9));
			}
		}.runTaskTimer(Manager.getPlugin(), 0, 1)).getTaskId());	
	}
	
	@Override
	public void cancel(Player player)
	{
		super.cancel(player);
		
		if (_kills.containsKey(player.getUniqueId()))
		{
			int kills = _kills.get(player.getUniqueId());
			
			if (kills >= 4)
			{
				Manager.GetGame().AddStat(player, "Blossom", 1, true, false);
			}
		}
		
		_kills.remove(player.getUniqueId());
		player.setWalkSpeed(0.2F);
		Bukkit.getScheduler().cancelTask(_tasks.get(player.getUniqueId()));
	}

	@EventHandler
	public void onCombatDeath(CombatDeathEvent event)
	{
		if (event.GetEvent().getEntity() == null || event.GetLog().GetKiller() == null)
		{
			return;
		}

		if (!(event.GetEvent().getEntity() instanceof Player))
		{
			return;
		}

		Player player = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());

		if (player == null)
		{
			return;
		}
		
		if (isUsingUltimate(player))
		{
			UUID uuid = player.getUniqueId();
			
			 _kills.putIfAbsent(uuid, 0);
			_kills.put(uuid, _kills.get(uuid) + 1);
		}
	}
	
}
