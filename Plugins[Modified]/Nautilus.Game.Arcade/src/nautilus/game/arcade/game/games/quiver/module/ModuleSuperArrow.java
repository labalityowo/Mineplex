package nautilus.game.arcade.game.games.quiver.module;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.quiver.Quiver;
import nautilus.game.arcade.game.games.quiver.QuiverTeamBase;
import nautilus.game.arcade.game.games.quiver.kits.KitNewNinja;

public class ModuleSuperArrow extends QuiverTeamModule implements Listener
{
	private static final String SUPER_ARROW_DAMAGE_SOURCE = "Projectile";
	public static final String SUPER_ARROW_DAMAGE_REASON = "Instagib";
	private static final double SUPER_ARROW_DAMAGE_MOD = 9001;
	private static final int RESPAWN_ARROW_GIVE_DELAY = 20;

	public ModuleSuperArrow(QuiverTeamBase base)
	{
		super(base);
	}

	@Override
	public void setup()
	{
		getBase().Manager.registerEvents(this);
	}

	@Override
	public void update(UpdateType updateType)
	{
	}

	@Override
	public void finish()
	{
		UtilServer.Unregister(this);
	}

	@EventHandler
	public void onCustomDamage(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity() instanceof Player)
		{
			if (event.GetProjectile() != null)
			{
				if (event.GetProjectile() instanceof Arrow)
				{
					event.AddMod(SUPER_ARROW_DAMAGE_SOURCE, SUPER_ARROW_DAMAGE_REASON, SUPER_ARROW_DAMAGE_MOD, false);

					event.SetKnockback(false);
				}
			}
		}
	}
	
	@EventHandler
	public void onCombatDeath(CombatDeathEvent event)
	{
		if (event.GetEvent().getEntity() == null || !event.GetLog().GetKiller().IsPlayer())
		{
			return;
		}

		if (!(event.GetEvent().getEntity() instanceof Player))
		{
			return;
		}

		Player player = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());
		Player killed = (Player) event.GetEvent().getEntity();

		if (player == null)
		{
			return;
		}

		player.getInventory().addItem(Quiver.SUPER_ARROW);

		if (getBase().GetKit(killed) instanceof KitNewNinja)
		{
			return;
		}

		new BukkitRunnable()
		{

			@Override
			public void run()
			{
				if (!killed.getInventory().contains(Material.ARROW))
				{
					killed.getInventory().addItem(Quiver.SUPER_ARROW);
				}
			}
		}.runTaskLater(getBase().Manager.getPlugin(), (long) (getBase().DeathSpectateSecs * 20 + RESPAWN_ARROW_GIVE_DELAY));
	}

	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event)
	{
		if (event.getEntity() instanceof Arrow)
		{
			event.getEntity().remove();
		}
	}
}
