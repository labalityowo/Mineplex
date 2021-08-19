package nautilus.game.arcade.kit.perks;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.kit.Perk;

public class PerkExplode extends Perk
{
	private String _name;
	private double _scale;
	private long _recharge;

	public PerkExplode(String name, double scale, long recharge) 
	{
		super("Explosive", new String[] 
				{
				C.cYellow + "Right-Click" + C.cGray + " with Axe to " + C.cGreen + name
				});

		_name = name;
		_scale = scale;
		_recharge = recharge;
	}

	@EventHandler
	public void Leap(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK &&
			event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (event.getPlayer().getItemInHand() == null)
			return;
		
		if (event.getPlayer().getItemInHand().getType() != Material.TNT)
			return;

		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;

		if (!Recharge.Instance.use(player, _name, _recharge, true, false))
			return;

		//Remove
		//Blast
		HashMap<Player, Double> hit = UtilPlayer.getInRadius(player.getLocation(), 8);
		for (Player other : hit.keySet())
		{
			//Velocity
			UtilAction.velocity(other, UtilAlg.getTrajectory(player.getLocation(), 
					other.getEyeLocation()), _scale*2.4*hit.get(other), false, 0, 0.2+_scale*0.6*hit.get(other), 1.6, true);

			//Damage Event
			Manager.GetDamage().NewDamageEvent(other, player, null, 
					DamageCause.CUSTOM, _scale*40*hit.get(other), false, true, false,
					UtilEnt.getName(player), GetName());

			//Inform
			UtilPlayer.message(other, F.main(GetName(), F.name(UtilEnt.getName(player)) +" hit you with " + F.item(GetName()) + "."));
		}

		//Effect
		UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, player.getLocation(), 0, 0, 0, 0, 1,
				ViewDist.MAX, UtilServer.getPlayers());
		player.getWorld().playSound(player.getLocation(), Sound.EXPLODE, 2f, 1f);
	}
}
