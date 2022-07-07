package nautilus.game.arcade.kit.perks;

import mineplex.core.common.util.UtilMath;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.kit.Perk;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.metadata.FixedMetadataValue;

public class PerkSniper extends Perk
{
    public PerkSniper()
    {
        super("Sniper", new String[]
            {
                    "The further your arrow travels", "The more damage it does!"
            });
    }

    @EventHandler
    public void ShootBow(EntityShootBowEvent event)
    {
        if (!(event.getEntity() instanceof Player))
            return;

        if (!Kit.HasKit((Player) event.getEntity()))
            return;

        // Save
        event.getProjectile().setMetadata("ShotFrom",
                new FixedMetadataValue(this.Manager.getPlugin(), event.getProjectile().getLocation()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void Damage(CustomDamageEvent event)
    {
        if (event.IsCancelled())
            return;

        if (event.GetCause() != DamageCause.PROJECTILE)
            return;

        Projectile projectile = event.GetProjectile();
        if (projectile == null)
            return;

        if (!projectile.hasMetadata("ShotFrom"))
            return;

        Player damager = event.GetDamagerPlayer(true);
        if (damager == null)
            return;

        Location loc = (Location) projectile.getMetadata("ShotFrom").get(0).value();
        double length = UtilMath.offset(loc, projectile.getLocation());

        length /= 13;
        // Damage
        double damage = Math.max(event.GetDamageInitial(), Math.pow(length, length));

        event.AddMod(damager.getName(), "Sniper", damage, damage > 0);
    }
}
