package nautilus.game.arcade.kit.perks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.kit.Perk;

public class PerkSmasher extends Perk
{
	public PerkSmasher() 
	{
		super("Smasher", new String[] 
				{
				C.cGray + "Hitting blocks damages all surrounding blocks",
				});
	}

	@EventHandler
	public void BlockSmash(BlockDamageEvent event)
	{
		Player player = event.getPlayer();
		
		if (!Kit.HasKit(player))
			return;
		
		if (this.Manager.GetGame() == null)
			return;
		
		if (!this.Manager.GetGame().IsAlive(player))
			return;
		
		if (!Recharge.Instance.use(player, GetName(), 250, false, false))
			return;
		
		for (Block block : UtilBlock.getSurrounding(event.getBlock(), false))
		{
			BlockDamageEvent blockDamage = new BlockDamageEvent(event.getPlayer(), block, event.getPlayer().getItemInHand(), false);
			Manager.getPlugin().getServer().getPluginManager().callEvent(blockDamage);
		}
		
		BlockDamageEvent blockDamage = new BlockDamageEvent(event.getPlayer(), event.getBlock(), event.getPlayer().getItemInHand(), false);
		Manager.getPlugin().getServer().getPluginManager().callEvent(blockDamage);
	}
	
	@EventHandler 
	public void BlockSmash(BlockBreakEvent event)
	{
		if (event.isCancelled())
			return;
		
		Player player = event.getPlayer();
		
		if (!Kit.HasKit(player))
			return;
		
		if (this.Manager.GetGame() == null)
			return;
		
		if (!this.Manager.GetGame().IsAlive(player))
			return;
		
		if (!Recharge.Instance.use(player, GetName(), 50, false, false))
			return;
		
		for (Block block : UtilBlock.getSurrounding(event.getBlock(), false))
		{
			BlockBreakEvent blockDamage = new BlockBreakEvent(block, player);
			Manager.getPlugin().getServer().getPluginManager().callEvent(blockDamage);
		}
	}
}
