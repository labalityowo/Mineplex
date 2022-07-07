package nautilus.game.arcade.game.modules;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.events.PlayerKitApplyEvent;

public class ChampionsModule extends Module
{	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void fixDwarf(PlayerKitApplyEvent event)
	{
		if (!getGame().IsLive())
			return;
		
		if (event.isCancelled())
			return;
		
		if (event.getPlayer().getPassenger() == null)
			return;
		
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void fixBlizzard(PlayerKitApplyEvent event)
	{
		if (!getGame().IsLive())
		{
			return;
		}
		
		if (event.isCancelled())
		{
			return;
		}
		
		getGame().Manager.getClassManager().GetSkillFactory().GetSkill("Blizzard").Reset(event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void fixFlash(PlayerKitApplyEvent event)
	{
		if (!getGame().IsLive())
		{
			return;
		}
		
		if (event.isCancelled())
		{
			return;
		}
		
		getGame().Manager.getClassManager().GetSkillFactory().GetSkill("Flash").Reset(event.getPlayer());
	}
	
	@EventHandler
	public void resetSkillsWhileInInventory(UpdateEvent event)
	{
		if (!getGame().IsLive())
		{
			return;
		}
		
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}
		
		for (Player player : UtilServer.getPlayers())
		{
			if (player.getOpenInventory() == null || player.getOpenInventory().getTopInventory() == null)
			{
				continue;
			}
			
			if (player.getOpenInventory().getTopInventory().getType() == InventoryType.CHEST)
			{
				for (Skill skill : getGame().Manager.getClassManager().GetSkillFactory().GetAllSkills())
				{
					skill.Reset(player);
				}
			}
		}
	}
	
	@EventHandler
	public void fixAxe(BlockDamageEvent event)
	{
		if (!getGame().IsLive())
		{
			return;
		}
		
		event.setCancelled(true);
	}
	
	@EventHandler
	public void fixArmor(InventoryClickEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler
	public void WaterArrowCancel(EntityShootBowEvent event)
	{
		if (event.getEntity().getLocation().getBlock().isLiquid())
		{
			UtilPlayer.message(event.getEntity(), F.main("Game", "You cannot use your Bow while swimming."));
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void InventoryDamageCancel(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}
		
		Player player = event.GetDamageePlayer();
		if (player == null)
		{
			return;
		}
		
		if (!getGame().IsAlive(player))
		{
			return;
		}
		
		if (player.getOpenInventory() == null || player.getOpenInventory().getTopInventory() == null)
		{
			return;
		}
		
		if (player.getOpenInventory().getTopInventory().getType() == InventoryType.CHEST)
		{
			player.closeInventory();
		}
	}
	
	@EventHandler
	public void validateSkills(UpdateEvent event)
	{
		if (event.getType() == UpdateType.SEC)
		{
			for (Player player : getGame().GetPlayers(true))
			{
				getGame().Manager.getClassManager().Get(player).validateClassSkills(player);
			}
		}
	}
}