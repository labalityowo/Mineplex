package nautilus.game.arcade.game.games.evolution.mobs.perks;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilInv;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import nautilus.game.arcade.game.games.evolution.events.EvolutionAbilityUseEvent;
import nautilus.game.arcade.kit.Perk;

import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;

public class PerkWebEVO extends Perk implements IThrown
{
	/**
	 * @author Mysticate
	 */
	
	public PerkWebEVO()
	{
		super("Web Shot", new String[0]);
	}

	@EventHandler
	public void Throw(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R))
			return;
		
		if (UtilBlock.usable(event.getClickedBlock()))
			return;
		
		if (!Manager.GetGame().IsLive())
			return;
		
		if (!Manager.IsAlive(event.getPlayer()))
			return;
		
		if (!UtilInv.contains(event.getPlayer(), Material.WEB, (byte) 0, 1))
			return;
		
		if (!UtilInv.IsItem(event.getItem(), Material.SPIDER_EYE, (byte) 0))
			return;
		
		Player player = event.getPlayer();
		
		if (!Kit.HasKit(player))
			return;
		
		EvolutionAbilityUseEvent useEvent = new EvolutionAbilityUseEvent(player, GetName(), 0);
		Bukkit.getServer().getPluginManager().callEvent(useEvent);
		
		if (useEvent.isCancelled())
			return;
		
		event.setCancelled(true);
		
		UtilInv.remove(player, Material.WEB, (byte) 0, 1);
		UtilInv.Update(player);
		
		Item ent = player.getWorld().dropItem(player.getEyeLocation(), ItemStackFactory.Instance.CreateStack(Material.WEB));
		UtilAction.velocity(ent, player.getLocation().getDirection(), 0.8, false, 0, 0.2, 10, false);
		Manager.GetProjectile().AddThrow(ent, player, this, -1, true, true, true, false, 1f);
	}
	
	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data) 
	{
		if (target != null)
		{
			if (target instanceof Player)
			{
				if (Manager.GetGame().IsAlive((Player)target))
				{
					Manager.GetDamage().NewDamageEvent(target, data.getThrower(), null, DamageCause.CUSTOM, 8, false, false, false, "Web Shot", "Webbed Net");
					target.playEffect(EntityEffect.HURT);
				}
				else
				{
					return;
				}
			}
		}
		
		Web(data);
	}

	@Override
	public void Idle(ProjectileUser data) 
	{
		Web(data);
	}

	@Override
	public void Expire(ProjectileUser data) 
	{
		Web(data);
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}
	
	public void Web(ProjectileUser data)
	{
		Location loc = data.getThrown().getLocation();

		data.getThrown().remove();

		if (loc.getBlock().getType() != Material.AIR)
			return;
		
		Manager.GetBlockRestore().add(loc.getBlock(), 30, (byte)0, 4000);
	}
}