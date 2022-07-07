package nautilus.game.arcade.game.games.quiver.ultimates;

import java.util.Iterator;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

import mineplex.core.itemstack.ItemBuilder;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.minecraft.game.core.damage.DamageChange;
import nautilus.game.arcade.game.games.quiver.module.ModuleSuperArrow;

public class UltimateBeserker extends UltimatePerk
{

	private static final float CHARGE_PASSIVE = 0.4F;
	private static final float CHARGE_PAYLOAD = 0.4F;
	private static final float CHARGE_KILL = 5F;
	private static final float CHARGE_ASSIST = 2F;

	public UltimateBeserker(long length)
	{
		super("Berserker Shield", new String[] {}, length, CHARGE_PASSIVE, CHARGE_PAYLOAD, CHARGE_KILL, CHARGE_ASSIST);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCustomDamage(CustomDamageEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}
		
		if (event.GetDamageePlayer() == null)
		{
			return;
		}
		
		Player player = event.GetDamageePlayer();
		
		if (!isUsingUltimate(player))
		{
			return;
		}
			
		Iterator<DamageChange> iterator = event.GetDamageMod().iterator();

		while (iterator.hasNext())
		{
			DamageChange damageChange = iterator.next();

			if (damageChange.GetReason().equals(ModuleSuperArrow.SUPER_ARROW_DAMAGE_REASON))
			{
				Manager.GetGame().AddStat(player, "Unstoppable", 1, false, false);
				
				iterator.remove();
			}
		}
	}

	@Override
	public void activate(Player player)
	{
		super.activate(player);

		player.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
		player.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
		player.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
	}

	@Override
	public void cancel(Player player)
	{
		super.cancel(player);
		
		Color color = Manager.GetGame().GetTeam(player).GetColorBase();
		
		player.getInventory().setChestplate(new ItemBuilder(Material.LEATHER_CHESTPLATE).setColor(color).build());
		player.getInventory().setLeggings(new ItemBuilder(Material.LEATHER_LEGGINGS).setColor(color).build());
		player.getInventory().setBoots(new ItemBuilder(Material.LEATHER_BOOTS).setColor(color).build());
	}

}
