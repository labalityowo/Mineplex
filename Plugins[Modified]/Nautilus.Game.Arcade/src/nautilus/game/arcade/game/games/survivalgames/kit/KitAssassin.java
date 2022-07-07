package nautilus.game.arcade.game.games.survivalgames.kit;

import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkBackstab;

public class KitAssassin extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkBackstab(),
			};

	private static final ItemStack IN_HAND = new ItemStack(Material.IRON_SWORD);

	private static final long MAP_COOLDOWN = TimeUnit.SECONDS.toMillis(45);
	private static final long MAP_TIME = TimeUnit.SECONDS.toMillis(5);

	public KitAssassin(ArcadeManager manager)
	{
		super(manager, GameKit.SG_ASSASSIN, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{

	}

	@EventHandler
	public void fallDamage(CustomDamageEvent event)
	{
		Player damagee = event.GetDamageePlayer();

		if (event.GetCause() != DamageCause.FALL || damagee == null || !HasKit(damagee))
		{
			return;
		}

		event.AddMod(GetName() + " Fall Damage", -event.GetDamage() / 2D);
	}

	@EventHandler
	public void showMapPlayers(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (itemStack == null || itemStack.getType() != Material.MAP || !HasKit(player) || !Recharge.Instance.use(player, "Assassin's Eye", MAP_COOLDOWN, true, true))
		{
			return;
		}

		player.sendMessage(F.main("Game", "You can now see all players on your map for " + F.time(UtilTime.MakeStr(MAP_TIME)) + "."));
		Recharge.Instance.useForce(player, "Show All Players", MAP_TIME);
	}
}
