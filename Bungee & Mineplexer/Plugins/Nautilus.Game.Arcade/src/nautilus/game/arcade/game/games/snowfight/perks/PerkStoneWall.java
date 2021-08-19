package nautilus.game.arcade.game.games.snowfight.perks;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;

import nautilus.game.arcade.kit.Perk;

public class PerkStoneWall extends Perk
{
	private Material _type;
	private Material _itemInHand;
	
	public PerkStoneWall(String name, Material block, Material itemInHand) 
	{
		super(name, new String[]  
				{
				C.cYellow + "Click" + C.cGray + " with " + ItemStackFactory.Instance.GetName(itemInHand, (byte)0, false) + " to use " + C.cGreen + name
				});
		
		_type = block;
		
		_itemInHand = itemInHand;
	}

	@EventHandler
	public void Skill(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK &&
			event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		if (!UtilGear.isMat(event.getPlayer().getItemInHand(), _itemInHand))
			return;
		
		if (UtilBlock.usable(event.getClickedBlock()))
			return;
				
		Player player = event.getPlayer();
		
		if (!Kit.HasKit(player))
			return;
		
		if (!Recharge.Instance.use(player, GetName(), 20000, true, true))
			return;
		
		Recharge.Instance.setDisplayForce(player, GetName(), true);
		
		//Get Player Direction
		Vector dir = null;

		if (Math.abs(player.getLocation().getDirection().getX()) > Math.abs(player.getLocation().getDirection().getZ()))
		{
			if (player.getLocation().getDirection().getX() > 0)
			{
				dir = new Vector(1,0,0);
			}
			else
			{
				dir = new Vector(-1,0,0);
			}
		}
		else
		{
			if (player.getLocation().getDirection().getZ() > 0)
			{
				dir = new Vector(0,0,1);
			}
			else
			{
				dir = new Vector(0,0,-1);
			}
		}

		for (int i=-2 ; i<=2 ; i++)
			for (int j=-2 ; j<=2 ; j++)
			{
				if (Math.abs(i) == 2 && Math.abs(j) == 2)
					continue;
				
				Location loc = player.getEyeLocation().add(player.getLocation().getDirection().multiply(3));
				loc.add(UtilAlg.getLeft(dir).multiply(i));
				loc.add(UtilAlg.getUp(dir).multiply(j));
				
				Manager.GetBlockRestore().add(loc.getBlock(), _type.getId(), (byte)0, 4000);
				
				loc.getWorld().playEffect(loc, Effect.STEP_SOUND, _type);
			}
		
		player.getWorld().playSound(player.getLocation(), Sound.IRONGOLEM_DEATH, 2f, 1f);
				
		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));
	}
}
