package nautilus.game.arcade.game.games.lobbers.kits.perks;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilInv;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.kit.Perk;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class PerkCraftman extends Perk
{
	private Recharge _recharge;
		
	public PerkCraftman()
	{
		super("Craftman", new String[]
				{
				C.cGray + "Recieve 1 TNT every so often. Maximum of 3."
				}, false);
		
		_recharge = Recharge.Instance;
	}
	
	@EventHandler
	public void give(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		if (!Manager.GetGame().IsLive())
			return;
		
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!Kit.HasKit(player))
				continue;
			//If game time is 56 seconds - 56000
			//8 - 1
			if (!_recharge.use(player, "Bomb Give", getRechargeTime(), false, false))
				continue;
			
			//Has 3
			if (UtilInv.contains(player, Material.TNT, (byte) 0, 3))
				continue;
			
			UtilInv.insert(player, new ItemBuilder(Material.TNT).setTitle(F.item("Throwing TNT")).build());
		}
	}
	
	private long getRechargeTime()
	{
		long ingame = Manager.GetGame().getGameLiveTime();
		
		if (ingame <= 20000)
			return 8000;
		
		if (ingame <= 40000)
			return 7000;
		
		if (ingame <= 60000)
			return 6000;
		
		if (ingame <= 620000)
			return 5000;
		
		return 4000;
	}
}
