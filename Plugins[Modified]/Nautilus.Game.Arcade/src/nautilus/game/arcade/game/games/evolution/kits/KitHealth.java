package nautilus.game.arcade.game.games.evolution.kits;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.common.util.UtilPlayer;
import mineplex.core.game.kit.GameKit;
import mineplex.minecraft.game.core.combat.CombatComponent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;

public class KitHealth extends Kit
{

	public KitHealth(ArcadeManager manager)
	{
		super(manager, GameKit.EVOLUTION_HEALTH);
	}

	@Override
	public void GiveItems(Player player)
	{

	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onKill(CombatDeathEvent event)
	{
		if (!Manager.GetGame().IsLive())
		{
			return;
		}

		CombatComponent component = event.GetLog().GetKiller();
		if (component == null)
		{
			return;
		}

		Player killer = Bukkit.getPlayer(component.getUniqueIdOfEntity());
		if (killer == null || !killer.isOnline())
		{
			return;
		}

		if (!Manager.IsAlive(killer))
		{
			return;
		}

		if (UtilPlayer.isSpectator(killer))
		{
			return;
		}

		if (!HasKit(killer))
		{
			return;
		}

		killer.setMaxHealth(killer.getMaxHealth());
	}
}
