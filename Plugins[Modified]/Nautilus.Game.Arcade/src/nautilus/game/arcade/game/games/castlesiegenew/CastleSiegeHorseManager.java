package nautilus.game.arcade.game.games.castlesiegenew;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

public class CastleSiegeHorseManager implements Listener
{

	private static final int MAX_HEALTH = 60;
	private static final ItemStack SADDLE = new ItemStack(Material.SADDLE);
	private static final ItemStack ARMOUR = new ItemStack(Material.IRON_BARDING);

	private final CastleSiegeNew _host;

	CastleSiegeHorseManager(CastleSiegeNew host)
	{
		_host = host;
	}

	@EventHandler
	public void horseSpawn(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		_host.CreatureAllowOverride = true;

		for (Location location : _host.WorldData.GetDataLocs("BROWN"))
		{
			Horse horse = location.getWorld().spawn(location, Horse.class);

			horse.setColor(Horse.Color.BLACK);
			horse.setStyle(Style.BLACK_DOTS);
			horse.setMaxDomestication(1);
			horse.getInventory().setSaddle(SADDLE);
			horse.getInventory().setArmor(ARMOUR);

			horse.setMaxHealth(MAX_HEALTH);
			horse.setHealth(MAX_HEALTH);

			horse.setCustomName("War Horse");
		}

		_host.CreatureAllowOverride = false;
	}

	@EventHandler
	public void horseInteract(PlayerInteractEntityEvent event)
	{
		if (!(event.getRightClicked() instanceof Horse) || !_host.IsLive())
		{
			return;
		}

		Player player = event.getPlayer();

		if (UtilPlayer.isSpectator(player) || !_host.getDefenders().HasPlayer(player))
		{
			player.sendMessage(F.main("Game", "You cannot mount horses."));
			event.setCancelled(true);
		}
		else
		{
			((Horse) event.getRightClicked()).setOwner(player);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void horseDamage(CustomDamageEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		LivingEntity entity = event.GetDamageeEntity();

		if (!(entity instanceof Horse))
		{
			return;
		}

		Player damager = event.GetDamagerPlayer(true);

		if (damager == null || _host.getUndead().HasPlayer(damager))
		{
			return;
		}

		event.SetCancelled("Horse Team Damage");
	}

	@EventHandler
	public void horseDeath(EntityDeathEvent event)
	{
		Entity entity = event.getEntity();

		if (entity instanceof Horse)
		{
			Player killer = ((Horse) entity).getKiller();

			if (killer != null)
			{
				_host.AddStat(killer, "HorseKiller", 1, false, false);
			}

			event.setDroppedExp(0);
			event.getDrops().clear();
		}
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		event.getEntity().eject();
	}
}
