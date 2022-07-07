package nautilus.game.arcade.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.combat.CombatComponent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;

import nautilus.game.arcade.game.Game;

public class DeathBomberStatTracker extends StatTracker<Game>
{

	private final int _required;
	private final Map<UUID, Integer> _killCount = new HashMap<>();

	public DeathBomberStatTracker(Game game, int requiredKills)
	{
		super(game);
		
		_required = requiredKills;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCombatDeath(CombatDeathEvent event)
	{
		if (!getGame().IsLive())
		{
			return;
		}

		CombatComponent killerComp = event.GetLog().GetKiller();;

		if (killerComp == null || !killerComp.IsPlayer())
		{
			return;
		}

		if (!event.GetLog().GetKiller().IsPlayer())
			return;

		Player killer = UtilPlayer.searchExact(killerComp.GetName());

		if (killer == null)
		{
			return;
		}

		if (event.GetLog().GetPlayer() == null || !event.GetLog().GetPlayer().IsPlayer())
		{
			return;
		}
		
		Player killed = UtilPlayer.searchExact(event.GetLog().GetPlayer().GetName());
		
		if(killer.equals(killed))
		{
			return;
		}

		if (event.GetLog().GetKiller() != null && event.GetLog().GetKiller().GetReason().contains("Throwing TNT"))
		{
			Integer count = _killCount.get(killer.getUniqueId());

			count = (count == null ? 0 : count) + 1;

			System.out.println("Death Bomber: " + killer.getName() + " " + count);

			_killCount.put(killer.getUniqueId(), count);

			if (count >= _required)
			{
				addStat(killer, "DeathBomber", 1, true, false);
			}
		}
	}

	@EventHandler
	public void removeFakeThrowingTNT(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !getGame().IsLive())
		{
			return;
		}

		for (Player player : getGame().GetPlayers(true))
		{
			Inventory inventory = player.getInventory();
			ItemStack[] contents = inventory.getContents();

			for (int i = 0; i < contents.length; i++)
			{
				ItemStack itemStack = contents[i];

				if (itemStack == null || itemStack.getType() == Material.TNT)
				{
					continue;
				}

				ItemMeta itemMeta = itemStack.getItemMeta();

				if (itemMeta == null || itemMeta.getDisplayName() == null)
				{
					continue;
				}

				String displayName = itemMeta.getDisplayName();

				if (displayName == null || !displayName.contains("Throwing TNT"))
				{
					continue;
				}

				inventory.setItem(i, null);
			}
		}
	}
}
