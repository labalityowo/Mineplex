package nautilus.game.arcade.kit.perks;

import java.util.Iterator;

import mineplex.core.common.util.NautHashMap;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.kit.Perk;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class PerkPinned extends Perk
{

    public PerkPinned()
    {
        super("Pinned", new String[]
            {
                    "Gravity is suddenly much stronger", "You also have a hard time moving"
            });
    }

    private NautHashMap<LivingEntity, Integer> _ticksPinned = new NautHashMap<LivingEntity, Integer>();

    @EventHandler
    public void onSecond(UpdateEvent event)
    {
        if (event.getType() != UpdateType.FASTEST)
        {
            return;
        }

        Iterator<LivingEntity> itel = _ticksPinned.keySet().iterator();
        while (itel.hasNext())
        {
            LivingEntity entity = itel.next();
            if (entity.isDead() || (entity instanceof Player && !Manager.IsAlive((Player) entity)))
            {
                itel.remove();
                continue;
            }
            entity.setVelocity(entity.getVelocity().add(new Vector(0, -0.2, 0)));
            if (_ticksPinned.get(entity) <= 1)
            {
                itel.remove();
            }
            else
            {
                _ticksPinned.put(entity, _ticksPinned.get(entity) - 1);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(CustomDamageEvent event)
    {
        if (event.GetCause() == DamageCause.PROJECTILE)
        {
            Player player = event.GetDamagerPlayer(true);
            if (player != null && this.Kit.HasKit(player))
            {
                LivingEntity entity = event.GetDamageeEntity();
                entity.setVelocity(entity.getVelocity().add(new Vector(0, -0.4, 0)));
                entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1), true);
                _ticksPinned.put(entity, 40);
            }
        }
    }
}
