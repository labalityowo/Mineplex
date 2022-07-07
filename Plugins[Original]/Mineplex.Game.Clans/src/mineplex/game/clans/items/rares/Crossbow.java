package mineplex.game.clans.items.rares;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilText;
import mineplex.core.recharge.Recharge;

public class Crossbow extends RareItem
{
	private static final Material LOADED_TYPE = Material.COMMAND_MINECART;
	private static final Material UNLOADED_TYPE = Material.RECORD_6;
	
	private long _lastFire = System.currentTimeMillis();
	private long _interactWait;
	
	public Crossbow()
	{
		super("Crossbow", UtilText.splitLinesToArray(new String[] {
					"#" + C.cYellow + "Right-Click" + C.cWhite + " to fire Crossbow."
				}, LineFormat.LORE), UNLOADED_TYPE);
	}
	
	@Override
	public void update(Player wielder)
	{
		if (UtilInv.contains(wielder, Material.ARROW, (byte) 0, 1))
		{
			wielder.getItemInHand().setType(LOADED_TYPE);
		}
		else
		{
			wielder.getItemInHand().setType(UNLOADED_TYPE);
		}
		
		if ((System.currentTimeMillis() - _lastBlock) < 98 && (System.currentTimeMillis() - _interactWait) >= 98)
		{
			if (UtilInv.remove(wielder, Material.ARROW, (byte) 0, 1))
			{
				if (Recharge.Instance.use(wielder, "Crossbow", 4000, true, true))
				{
					fire(wielder);
					
					_interactWait = System.currentTimeMillis();
				}
			}
		}
	}
	
	private void fire(final Player player)
	{
		Arrow arrow = player.shootArrow();
		
		arrow.setShooter(player);
		
		_lastFire = System.currentTimeMillis();
	}
}
