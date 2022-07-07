package nautilus.game.arcade.game.games.lobbers.kits;

import java.util.Collections;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.C;
import mineplex.core.game.kit.GameKit;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.lobbers.kits.perks.PerkCraftman;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDoubleJump;
import nautilus.game.arcade.kit.perks.PerkDummy;

public class KitJumper extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkDoubleJump("Double Jump", 1.2, 1.2, false),
					new PerkDummy("Feathered Boots", Collections.singletonList(C.cGray + "You take no fall damage.").toArray(new String[1])),
					new PerkCraftman()
			};

	public KitJumper(ArcadeManager manager)
	{
		super(manager, GameKit.BOMB_LOBBERS_JUMPER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{

	}

	@EventHandler
	public void onDamage(CustomDamageEvent event)
	{
		if (!Manager.GetGame().IsLive())
		{
			return;
		}
		if (!(event.GetDamageeEntity() instanceof Player))
		{
			return;
		}
		if (!HasKit(event.GetDamageePlayer()))
		{
			return;
		}
		if (event.GetCause() == DamageCause.FALL)
		{
			event.SetCancelled("Jumper no fall damage");
		}
	}
}
