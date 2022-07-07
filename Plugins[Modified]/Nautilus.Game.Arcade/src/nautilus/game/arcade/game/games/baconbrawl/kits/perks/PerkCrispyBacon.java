package nautilus.game.arcade.game.games.baconbrawl.kits.perks;

import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.kit.Perk;

public class PerkCrispyBacon extends Perk
{

	private static final long COOLDOWN = TimeUnit.SECONDS.toMillis(5);

	public PerkCrispyBacon()
	{
		super("Crispy Bacon");
	}

	@EventHandler
	public void playerInteract(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (!hasPerk(player) || !UtilItem.isAxe(itemStack) || !Recharge.Instance.use(player, GetName(), COOLDOWN, true, true))
		{
			return;
		}

		Location location = player.getLocation().add(0, 1.5, 0);
		location.getWorld().playSound(location, Sound.EXPLODE, 1, 0.2F);
		UtilParticle.PlayParticleToAll(ParticleType.LAVA, location, 0.5F, 0.5F, 0.5F, 0, 4, ViewDist.NORMAL);

		for (int i = 0; i < 3; i++)
		{
			Item item = player.getWorld().dropItem(location, new ItemBuilder(Material.GRILLED_PORK)
					.setTitle(String.valueOf(UtilMath.r(1000)))
					.build());
			item.setVelocity(new Vector((Math.random() - 0.5) * 0.5, 0.4, (Math.random() - 0.5) * 0.5));
			Manager.GetFire().Add(item, player, 10, 1, 2, 2, GetName(), false);
		}
	}

	@EventHandler
	public void knockbackIncrease(CustomDamageEvent event)
	{
		Player damgee = event.GetDamageePlayer();
		Player damager = event.GetDamagerPlayer(true);

		if (damgee == null || damager == null)
		{
			return;
		}

		if (hasPerk(damager) && damgee.getFireTicks() > 0)
		{
			event.AddKnockback(GetName(), 1.3);
		}
	}
}
