package nautilus.game.arcade.game.games.monstermaze.kits;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.F;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkConstructor;

public class KitSlowball extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkConstructor("Slowballer", 2, 16, Material.SNOW_BALL, "Slowball", true)
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.COMPASS, (byte) 0, 1, F.item("Safe Pad Locator"))
			};

	public KitSlowball(ArcadeManager manager)
	{
		super(manager, GameKit.MONSTER_MAZE_SLOW_BALL, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().setItem(4, PLAYER_ITEMS[0]);
	}

	@EventHandler
	public void SnowballHit(CustomDamageEvent event)
	{
		if (event.GetProjectile() == null)
			return;

		if (!(event.GetProjectile() instanceof Snowball))
			return;

		event.GetProjectile().remove();

		Manager.GetCondition().Factory().Slow("Snowball Slow", event.GetDamageeEntity(), (LivingEntity) event.GetProjectile().getShooter(), 2, 1, false, false, true, false);
	}
}
