package nautilus.game.arcade.kit.perks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.WeakHashMap;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.kit.Perk;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class PerkSharpshooter extends Perk
{
    private WeakHashMap<Player, Integer> _hitCount = new WeakHashMap<Player, Integer>();
    private HashMap<Entity, Player> _arrows = new HashMap<Entity, Player>();

    public PerkSharpshooter()
    {
        super("Sharpshooter", new String[]
            {
                    "Consecutive arrow hits deal an", "additional 2 damage.", "", "Stacks up to 6 times", "",
                    "Missing an arrow resets the bonus.",
            });
    }

    @EventHandler
    public void ShootBow(EntityShootBowEvent event)
    {
        if (!(event.getEntity() instanceof Player))
            return;

        if (!Kit.HasKit((Player) event.getEntity()))
            return;

        // Store
        _arrows.put(event.getProjectile(), (Player) event.getEntity());
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

        if (!_arrows.containsKey(projectile))
            return;

        Player player = _arrows.remove(projectile);

        if (_hitCount.containsKey(player))
        {
            // Damage
            event.AddMod(player.getName(), GetName(), _hitCount.get(player) * 2, true);

            int limit = Math.min(6, _hitCount.get(player) + 1);

            _hitCount.put(player, limit);

            // Inform
            UtilPlayer.message(
                    (Entity) projectile.getShooter(),
                    GetName() + ": " + F.elem(_hitCount.get(player) + " Consecutive Hits") + C.cGray + " ("
                            + F.skill("+" + (limit * 2) + " Damage") + C.cGray + ")");
        }
        else
        {
            _hitCount.put(player, 1);
        }

        projectile.remove();
    }

    @EventHandler
    public void Clean(UpdateEvent event)
    {
        if (event.getType() != UpdateType.FAST)
            return;

        HashSet<Entity> remove = new HashSet<Entity>();

        Iterator<Player>itel = this._hitCount.keySet().iterator();
        while (itel.hasNext()) {
            Player p = itel.next();
            if (!Kit.HasKit(p) || !this.Manager.IsAlive(p)) {
                itel.remove();
            }
        }
        
        for (Entity cur : _arrows.keySet())
            if (cur.isDead() || !cur.isValid() || cur.isOnGround())
                remove.add(cur);

        for (Entity cur : remove)
        {
            Player player = _arrows.remove(cur);

            if (player != null)
                if (_hitCount.remove(player) != null)
                    UtilPlayer.message(player, GetName() + ": " + F.elem("0 Consecutive Hits"));
        }
    }
}
