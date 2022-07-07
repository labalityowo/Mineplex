package nautilus.game.arcade.game.games.cakewars.kits.perk;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.kit.Perk;

public class PerkSlowSnowball extends Perk
{

	private static final ItemStack SNOW_BALL = new ItemBuilder(Material.SNOW_BALL)
			.setTitle(C.cPurpleB + "Frosting Balls")
			.build();
	private static final int MAX = 3;

	public PerkSlowSnowball()
	{
		super("Frosting");
	}

	@EventHandler
	public void updateGain(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !Manager.GetGame().IsLive())
		{
			return;
		}

		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (UtilPlayer.isSpectator(player) || !hasPerk(player) || UtilInv.contains(player, SNOW_BALL.getType(), SNOW_BALL.getData().getData(), MAX) || !Recharge.Instance.use(player, "Snowball Give", 6000, false, false))
			{
				continue;
			}

			player.getInventory().addItem(SNOW_BALL);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void snowballDamage(CustomDamageEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		Player damagee = event.GetDamageePlayer();
		Player damager = event.GetDamagerPlayer(true);
		Projectile projectile = event.GetProjectile();

		if (damagee == null || damager == null || !hasPerk(damager) || !(projectile instanceof Snowball))
		{
			return;
		}

		Manager.GetCondition().Factory().Slow(GetName(), damagee, damager, 2.5, 1, false, true, false, false);
		event.AddMod(damager.getName(), GetName(), 1, true);
		event.AddKnockback(GetName(), 0.5);
	}

	@EventHandler
	public void disallowMovement(InventoryClickEvent event)
	{
		UtilInv.DisallowMovementOf(event, null, SNOW_BALL.getType(), SNOW_BALL.getData().getData(), false);
	}

	@EventHandler
	public void disallowDrop(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();

		if (!hasPerk(player) || event.getItemDrop().getItemStack().getType() != SNOW_BALL.getType())
		{
			return;
		}

		event.setCancelled(true);
	}
}
