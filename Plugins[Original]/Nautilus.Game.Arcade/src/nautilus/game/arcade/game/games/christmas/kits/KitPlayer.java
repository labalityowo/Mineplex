package nautilus.game.arcade.game.games.christmas.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;

public class KitPlayer extends Kit
{

	public KitPlayer(ArcadeManager manager)
	{
		super(manager, GameKit.CHRISTMAS_CHAOS_PLAYER);
	}
	
	@Override
	public void GiveItems(Player player)
	{
		//Sword
		ItemStack item = ItemStackFactory.Instance.CreateStack(Material.DIAMOND_SWORD, (byte)0, 1, C.cGreen + C.Bold + "Elf Sword");
		player.getInventory().setItem(0, item);
		
		//Bow
		item = ItemStackFactory.Instance.CreateStack(Material.BOW, (byte)0, 1, C.cGreen + C.Bold + "Toy Bow");
		item.addEnchantment(Enchantment.ARROW_INFINITE, 1);
		player.getInventory().setItem(1, item);
		player.getInventory().setItem(9, ItemStackFactory.Instance.CreateStack(Material.ARROW));
		
		//Axe
		item = ItemStackFactory.Instance.CreateStack(Material.STONE_PICKAXE, (byte)0, 1, C.cGreen + C.Bold + "Coal Digger");
		player.getInventory().setItem(2, item);
		
		//Armor
		player.getInventory().setHelmet(ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_HELMET));
		player.getInventory().setChestplate(ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_CHESTPLATE));
		player.getInventory().setLeggings(ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_LEGGINGS));
		player.getInventory().setBoots(ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_BOOTS));
		
		Manager.GetCondition().Factory().Regen("Perm", player, player, 3600000, 0, false, false, true);
	}
}