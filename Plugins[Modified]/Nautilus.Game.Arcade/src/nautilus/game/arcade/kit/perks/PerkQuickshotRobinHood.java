package nautilus.game.arcade.kit.perks;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilInv;
import nautilus.game.arcade.kit.Perk;

public class PerkQuickshotRobinHood extends Perk
{
	public PerkQuickshotRobinHood() 
	{
		super("Quick Shot", new String[] 
				{
				C.cYellow + "Left-Click" + C.cGray + " with Bow to " + C.cGreen + "Quick Shot"
				});
	}

	@EventHandler
	public void Leap(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK)
			return;
		
		if (UtilBlock.usable(event.getClickedBlock()))
			return;
		
		if (event.getPlayer().getItemInHand() == null)
			return;
		
		if (event.getPlayer().getItemInHand().getType() != Material.BOW)
			return;
		
		Player player = event.getPlayer();
		
		if (!Kit.HasKit(player))
			return;
		
		if (!player.getInventory().contains(Material.ARROW))
			return;
		
		UtilInv.remove(player, Material.ARROW, (byte)0, 1);
		
		Arrow arrow = player.launchProjectile(Arrow.class);
		arrow.setVelocity(player.getLocation().getDirection().multiply(2));
		
		player.getWorld().playSound(player.getLocation(), Sound.SHOOT_ARROW, 1f, 1f);
	}
}
