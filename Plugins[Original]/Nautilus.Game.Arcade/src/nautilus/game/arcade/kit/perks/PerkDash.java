package nautilus.game.arcade.kit.perks;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.particles.effects.LineParticle;
import mineplex.core.recharge.Recharge;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.TeamGame;
import nautilus.game.arcade.kit.Perk;

public class PerkDash extends Perk
{

	private static final double DAMAGE_RADIUS = 2.5;

	private long _cooldown;
	private double _damage;
	private double _distance;
	
	public PerkDash(long cooldown, double damage, double distance)
	{
		super("Dash", new String[] {});

		_cooldown = cooldown;
		_damage = damage;
		_distance = distance;
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
		{
			return;
		}

		Player player = event.getPlayer();

		if (player.getItemInHand() == null)
		{
			return;
		}

		if (!UtilItem.isSword(player.getItemInHand()))
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}

		if (!Recharge.Instance.use(player, GetName(), _cooldown, true, true))
		{
			return;
		}

		LineParticle lineParticle = new LineParticle(player.getEyeLocation(), player.getLocation().getDirection(), 0.8, _distance, ParticleType.FIREWORKS_SPARK,
				UtilServer.getPlayers());
		Set<UUID> hitPlayers = new HashSet<>();
		
		while (!lineParticle.update())
		{
			for (Player other : UtilPlayer.getNearby(lineParticle.getLastLocation(), DAMAGE_RADIUS))
			{
				if (hitPlayers.contains(other.getUniqueId()) || player.equals(other))
				{
					continue;
				}
				
				if (Manager.GetGame() instanceof TeamGame)
				{
					Game game = Manager.GetGame();
					
					if (game.GetTeam(player).equals(game.GetTeam(other)))
					{
						continue;
					}
				}
				 
				hitPlayers.add(other.getUniqueId());
				Manager.GetDamage().NewDamageEvent(other, player, null, DamageCause.CUSTOM, _damage, true, true, false, player.getName(), GetName());
				player.sendMessage(F.main("Game", "You hit " + F.elem(other.getName()) + " with " + F.skill(GetName()) + "."));
			}
		}

		Game game = Manager.GetGame();
		Location location = lineParticle.getDestination();

		UtilFirework.playFirework(player.getEyeLocation(), Type.BALL, game.GetTeam(player).GetColorBase(), false, false);

		player.playSound(player.getLocation(), Sound.SHOOT_ARROW, 1, 1);
		player.teleport(location.add(0, 0.5, 0));
		player.playSound(player.getLocation(), Sound.SHOOT_ARROW, 1, 1);
		
		UtilFirework.playFirework(player.getEyeLocation(), Type.BALL, game.GetTeam(player).GetColorBase(), false, false);

		player.setFallDistance(0);

		player.sendMessage(F.main("Game", "You used " + F.skill(GetName()) + "."));

	}
}
