package mineplex.minecraft.game.classcombat.Skill.Ranger;

import java.util.HashSet;
import java.util.Iterator;
import java.util.WeakHashMap;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.core.common.util.F;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;

public class Fletcher extends Skill
{
	private WeakHashMap<Player, Long> _time = new WeakHashMap<Player, Long>();
	private HashSet<Entity> _fletchArrows = new HashSet<Entity>();
	private HashSet<Entity> _fletchDisable = new HashSet<Entity>();

	public Fletcher(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels) 
	{
		super(skills, name, classType, skillType, cost, levels);

		SetDesc(new String[] 
				{
				"Craft arrows from your surroundings,",
				"creating 1 Arrow every #13#-3 seconds.",
				"",
				"Maximum of #2#2 Fletched Arrows.",
				"Fletched Arrows are temporary."
				});
	}

	@EventHandler
	public void ShootBow(EntityShootBowEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;

		Player player = (Player)event.getEntity();

		int level = getLevel(player);
		if (level == 0)		return;

		for (int i=0 ; i<=8 ; i++)
			if (player.getInventory().getItem(i) != null)
				if (player.getInventory().getItem(i).getType() == Material.ARROW)
					if (player.getInventory().getItem(i).getData() != null)
					{
						if (player.getInventory().getItem(i).getData().getData() == (byte)1)
							_fletchArrows.add(event.getProjectile());

						return;
					}	
	}

	@EventHandler
	public void ProjectileHit(ProjectileHitEvent event)
	{
		if (_fletchArrows.remove(event.getEntity()))
			event.getEntity().remove();
	}

	@EventHandler
	public void Fletch(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		for (Player cur : GetUsers())
		{
			if (!_time.containsKey(cur))
				_time.put(cur, System.currentTimeMillis());

			if (!UtilTime.elapsed(_time.get(cur), 10000))
				continue;

			if (UtilInv.contains(cur, Material.ARROW, (byte)1, 8))
				continue;
			
			if (_fletchDisable.contains(cur))
				continue;

			_time.put(cur, System.currentTimeMillis());

			//Add
			cur.getInventory().addItem(ItemStackFactory.Instance.CreateStack(262, (byte)1, 1, "Fletched Arrow"));

			//Inform
			//UtilPlayer.message(cur, F.main(getName(), "You crafted a " + F.item("Fletched Arrow") + "."));
		}
	}

	@EventHandler
	public void Drop(PlayerDropItemEvent event)
	{
		if (event.getItemDrop().getItemStack().getType() != Material.ARROW)
			return;

		if (event.getItemDrop().getItemStack().getData() == null)
			return;

		if (event.getItemDrop().getItemStack().getData().getData() != (byte)1)
			return;

		//Cancel
		event.setCancelled(true);

		//Inform
		UtilPlayer.message(event.getPlayer(), F.main(GetName(), "You cannot drop " + F.item("Fletched Arrow") + "."));
	}

	@EventHandler
	public void Dead(PlayerDeathEvent event)
	{	
		HashSet<ItemStack> remove = new HashSet<ItemStack>();

		for (ItemStack item : event.getDrops())
			if (item.getType() == Material.ARROW)
				if (item.getData() != null)
					if (item.getData().getData() == 1)
						remove.add(item);

		for (ItemStack item : remove)
			event.getDrops().remove(item);
	}
	
	@EventHandler
	public void ChestRemove(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R_BLOCK))
			return;

		if (	event.getClickedBlock().getType() != Material.CHEST &&
				event.getClickedBlock().getType() != Material.FURNACE &&
				event.getClickedBlock().getType() != Material.BURNING_FURNACE &&
				event.getClickedBlock().getType() != Material.WORKBENCH &&
				event.getClickedBlock().getType() != Material.DISPENSER &&
				event.getClickedBlock().getType() != Material.ENCHANTMENT_TABLE &&
				event.getClickedBlock().getType() != Material.BEACON)
			return;

		UtilInv.removeAll(event.getPlayer(), Material.ARROW, (byte)1);
	}
	
	@EventHandler
	public void InvDisable(InventoryOpenEvent event)
	{
		if (getLevel(event.getPlayer()) > 0)
			_fletchDisable.add(event.getPlayer());
	}
	
	@EventHandler
	public void InvEnable(InventoryCloseEvent event)
	{
		_fletchDisable.remove(event.getPlayer());
	}
	
	@EventHandler
	public void InvClick(InventoryClickEvent event)
	{
		if (event.getCurrentItem() == null)
			return;
		
		if (event.getCurrentItem().getType() != Material.ARROW)
			return;
		
		if (event.getCurrentItem().getData() == null)
			return;
		
		if (event.getCurrentItem().getData().getData() != 1)
			return;
		
		event.setCancelled(true);
		
		//Inform
		UtilPlayer.message(event.getWhoClicked(), F.main(GetName(), "You cannot move " + F.item("Fletched Arrow") + "."));
	}

	@EventHandler
	public void Clean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;
		
		for (Iterator<Entity> arrowIterator = _fletchArrows.iterator(); arrowIterator.hasNext();) 
		{
			Entity arrow = arrowIterator.next();
			
			if (arrow.isDead() || !arrow.isValid())
				arrowIterator.remove();
		}
	}

	@Override
	public void Reset(Player player) 
	{
		_time.remove(player);
		_fletchDisable.remove(player);
		UtilInv.removeAll(player, Material.ARROW, (byte)1);
	}
}
