package mineplex.game.clans.clans.potato;

import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilInv;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.game.clans.clans.ClansManager;

public class PotatoManager extends MiniPlugin implements IThrown
{
	private static final String POTATO_NAME = C.cGray + "Potato";

	private ClansManager _clansManager;

	public PotatoManager(JavaPlugin plugin, ClansManager clansManager)
	{
		super("Potato", plugin);

		_clansManager = clansManager;
	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent event)
	{
		give(event.getPlayer(), 5);
	}

	private void give(Player player, int count)
	{
		ItemStack potato = new ItemStack(Material.BAKED_POTATO, count);
		ItemMeta meta = potato.getItemMeta();
		meta.setDisplayName(POTATO_NAME);
		potato.setItemMeta(meta);
		player.getInventory().addItem(potato);
	}

	public boolean isPotato(ItemStack item)
	{
		if (item == null)
			return false;
		else if (item.getType() != Material.BAKED_POTATO)
			return false;
		else
			return item.getItemMeta() != null && POTATO_NAME.equals(item.getItemMeta().getDisplayName());
	}

	@EventHandler
	public void tossPotato(PlayerInteractEvent event)
	{
		if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)
		{
			Player player = event.getPlayer();
			ItemStack item = event.getPlayer().getItemInHand();
			if (isPotato(item))
			{
				UtilInv.remove(event.getPlayer(), Material.BAKED_POTATO, (byte) 0, 1);
				UtilInv.Update(event.getPlayer());

				Item ent = player.getWorld().dropItem(player.getEyeLocation(), ItemStackFactory.Instance.CreateStack(Material.BAKED_POTATO));
				UtilAction.velocity(ent, player.getLocation().getDirection(), 1.2, false, 0, 0.2, 10, false);
				_clansManager.getProjectile().AddThrow(ent, player, this, -1, true, true, true, false, 0.5f);
			}
		}
	}

	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event)
	{
		ItemStack item = event.getItemDrop().getItemStack();
		if (isPotato(item))
		{
			event.setCancelled(true);
			ItemStack hand = event.getPlayer().getItemInHand();
			if (hand != null && isPotato(hand))
			{
				hand.setAmount(hand.getAmount() - 1);
				event.getPlayer().setItemInHand(hand);
			}
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event)
	{
		Iterator<ItemStack> iterator = event.getDrops().iterator();
		while (iterator.hasNext())
		{
			if (isPotato(iterator.next())) iterator.remove();
		}
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		if (target instanceof Player)
		{
			Player player = ((Player) target);
			give(player, 1);
		}

		data.getThrown().getWorld().playSound(data.getThrown().getLocation(), Sound.CHICKEN_EGG_POP, 1f, 1.6f);

		data.getThrown().remove();
	}

	@Override
	public void Idle(ProjectileUser data)
	{

	}

	@Override
	public void Expire(ProjectileUser data)
	{

	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}
}