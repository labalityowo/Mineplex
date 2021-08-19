package nautilus.game.arcade.kit.perks;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.ore.OreHider;
import nautilus.game.arcade.ore.OreObsfucation;

public class PerkOreFinder extends Perk
{
	private HashMap<Material, Material> _blockMap = new HashMap<Material, Material>();
	
	public PerkOreFinder() 
	{
		super("Ore Finder", new String[] 
				{ 
				C.cYellow + "Right-Click" + C.cGray + " with Pickaxe to " + C.cGreen + "Locate Ore",
				C.cGray + "Locates Ore of same type as your Pickaxe",
				});
		
		_blockMap.put(Material.STONE_PICKAXE, Material.COAL_ORE);
		_blockMap.put(Material.IRON_PICKAXE, Material.IRON_ORE);
		_blockMap.put(Material.GOLD_PICKAXE, Material.GOLD_ORE);
		_blockMap.put(Material.DIAMOND_PICKAXE, Material.DIAMOND_ORE);
	}
		
	@EventHandler
	public void SearchOre(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		if (Manager.GetGame() == null)
			return;
		
		if (event.getPlayer().getItemInHand() == null)
			return;
		
		if (event.getClickedBlock() != null)
			if (UtilBlock.usable(event.getClickedBlock()))
				return;
		
		Material type = event.getPlayer().getItemInHand().getType();
		
		if (!_blockMap.containsKey(type))
			return;
		
		Player player = event.getPlayer();
		
		if (!Kit.HasKit(player))
			return;
		
		String oreType = ItemStackFactory.Instance.GetName(_blockMap.get(type), (byte)0, false);
		
		if (!Recharge.Instance.use(player, "Ore Scanner", 30000, true, false))
			return;

		Block bestBlock = null;
		double bestDist = 10;
		
		//Unhidden Ores
		for (Block block : UtilBlock.getInRadius(player.getLocation(), 8d).keySet())
		{
			if (block.getType() != _blockMap.get(type))
				continue;
			
			double dist = UtilMath.offset(block.getLocation(), player.getLocation());
			
			if (bestBlock == null || dist < bestDist)
			{
				bestBlock = block;
				bestDist = dist;
			}
		}
		
		//Hidden Ores
		if (Manager.GetGame() instanceof OreObsfucation)
		{
			OreHider ore = ((OreObsfucation)Manager.GetGame()).GetOreHider();
			
			for (Location loc : ore.GetHiddenOre().keySet())
			{
				if (ore.GetHiddenOre().get(loc) != _blockMap.get(type))
					continue;
				
				double dist = UtilMath.offset(loc, player.getLocation());
				
				if (dist > 8d)
					continue;
				
				if (bestBlock == null || dist < bestDist)
				{
					bestBlock = loc.getBlock();
					bestDist = dist;
				}
			}
		}
		
		UtilPlayer.message(player, F.main("Skill", "Scanning for " + F.skill(oreType) + "..."));
		
		if (bestBlock == null)
		{
			UtilPlayer.message(player, F.main("Skill", "No " + F.skill(oreType) + " found."));
		}
		else
		{
			Vector vec = UtilAlg.getTrajectory(player.getEyeLocation(), bestBlock.getLocation().add(0.5, 0.5, 0.5));
			
			Location loc = player.getLocation();
			loc.setPitch(UtilAlg.GetPitch(vec));
			loc.setYaw(UtilAlg.GetYaw(vec));
			
			player.teleport(loc);
			
			UtilPlayer.message(player, F.main("Skill", "Located nearby " + F.skill(oreType) + "!"));
		}
	}
}
