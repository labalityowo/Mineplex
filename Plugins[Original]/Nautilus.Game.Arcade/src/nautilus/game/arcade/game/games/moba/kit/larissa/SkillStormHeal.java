package nautilus.game.arcade.game.games.moba.kit.larissa;

import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import nautilus.game.arcade.game.games.moba.util.MobaUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SkillStormHeal extends HeroSkill
{

	private static final String[] DESCRIPTION = {
			"All team members are consistently healed",
			"over the next 7 seconds."
	};
	private static final long DURATION = TimeUnit.SECONDS.toMillis(7);
	private static final int HEALTH_PER_SECOND = 2;
	private static final ItemStack SKILL_ITEM = new ItemStack(Material.NETHER_STAR);

	private final Map<Player, Long> _active = new HashMap<>();

	public SkillStormHeal(int slot)
	{
		super("Storm's Blessing", DESCRIPTION, SKILL_ITEM, slot, ActionType.ANY);

		setCooldown(60000);
		setDropItemActivate(true);
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (!isSkillItem(event) || _active.containsKey(event.getPlayer()))
		{
			return;
		}

		Player player = event.getPlayer();

		for (Player teamMember : Manager.GetGame().GetTeam(player).GetPlayers(true))
		{
			teamMember.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (DURATION / 50D), 0));
		}

		_active.put(player, System.currentTimeMillis());
		broadcast(player);
		useActiveSkill(player, DURATION - 500);
	}

	@EventHandler
	public void updateHeal(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		_active.forEach((player, start) ->
		{
			GameTeam team = Manager.GetGame().GetTeam(player);

			if (team == null)
			{
				return;
			}

			for (Player teamMember : team.GetPlayers(true))
			{
				MobaUtil.heal(teamMember, player, HEALTH_PER_SECOND);
			}
		});
	}

	@EventHandler
	public void updateParticles(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
		{
			return;
		}

		Iterator<Player> iterator = _active.keySet().iterator();

		while (iterator.hasNext())
		{
			Player player = iterator.next();

			if (UtilTime.elapsed(_active.get(player), DURATION))
			{
				iterator.remove();
			}

			GameTeam team = Manager.GetGame().GetTeam(player);

			if (team == null)
			{
				return;
			}

			for (Player teamMember : team.GetPlayers(true))
			{
				if (UtilPlayer.isSpectator(teamMember))
				{
					continue;
				}

				Location location = teamMember.getLocation().add(0, 3, 0);

				UtilParticle.PlayParticleToAll(ParticleType.CLOUD, location, 0.5F, 0.2F, 0.5F, 0.001F, 12, ViewDist.LONG);
				UtilParticle.PlayParticleToAll(ParticleType.DRIP_WATER, location.subtract(0, 0.2, 0), 0.4F, 0.1F, 0.4F, 0.1F, 2, ViewDist.LONG);
			}
		}
	}
}
