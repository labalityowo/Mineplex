package nautilus.game.arcade.game.games.hideseek.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkLeap;

public class KitSeekerLeaper extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkLeap("Leap", 1.1, 1, 8000),
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.IRON_AXE),
					new ItemBuilder(Material.BOW).setUnbreakable(true).addEnchantment(Enchantment.ARROW_INFINITE, 1).build(),
					ItemStackFactory.Instance.CreateStack(Material.ARROW)
			};

	private static final ItemStack[] PLAYER_ARMOR =
			{
					ItemStackFactory.Instance.CreateStack(Material.IRON_BOOTS),
					ItemStackFactory.Instance.CreateStack(Material.IRON_LEGGINGS),
					ItemStackFactory.Instance.CreateStack(Material.IRON_CHESTPLATE),
					ItemStackFactory.Instance.CreateStack(Material.IRON_HELMET),
			};

	public KitSeekerLeaper(ArcadeManager manager)
	{
		super(manager, GameKit.HIDE_AND_SEEK_LEAPER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		//Sword
		player.getInventory().addItem(PLAYER_ITEMS[0]);

		player.getInventory().setItem(1, PLAYER_ITEMS[1]);
		player.getInventory().setItem(28, PLAYER_ITEMS[2]);

		player.getInventory().setArmorContents(PLAYER_ARMOR);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void Damage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetCause() != DamageCause.ENTITY_EXPLOSION)
			return;

		Player damagee = event.GetDamageePlayer();
		if (damagee == null) return;

		if (HasKit(damagee))
			event.SetCancelled("TNT Resistant");
	}
}
