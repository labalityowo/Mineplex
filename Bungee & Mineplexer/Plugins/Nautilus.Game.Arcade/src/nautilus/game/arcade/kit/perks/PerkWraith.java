package nautilus.game.arcade.kit.perks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkWraith extends Perk
{
    private boolean _foot = false;

    private HashMap<Location, Long> _steps = new HashMap<Location, Long>();

    public PerkWraith()
    {
        super("Invisibility", new String[]
            {
                    "You are permanently invisible.", "Enemies are able to see your weapon", "and a trail of footsteps."
            });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void invisible(UpdateEvent event)
    {
        if (event.getType() != UpdateType.FAST)
            return;

        if (Manager.GetGame() == null)
            return;

        for (Player cur : Manager.GetGame().GetPlayers(true))
        {
            if (!Kit.HasKit(cur))
                continue;

            Manager.GetCondition().Factory().Invisible(GetName(), cur, cur, 2.9, 0, false, false, false);
        }
    }

    @EventHandler
    public void playParticle(UpdateEvent event)
    {
        if (event.getType() != UpdateType.FASTEST)
            return;

        _foot = !_foot;

        cleanSteps();

        if (Manager.GetGame() == null)
            return;

        for (Player player : Manager.GetGame().GetPlayers(true))
        {
            if (!Kit.HasKit(player))
                continue;
            
            if (!UtilEnt.isGrounded(player))
                continue;

            Vector offset;

            Vector dir = player.getLocation().getDirection();
            dir.setY(0);
            dir.normalize();

            if (_foot)
            {
                offset = new Vector(dir.getZ() * -1, 0.1, dir.getX());
            }
            else
            {
                offset = new Vector(dir.getZ(), 0.1, dir.getX() * -1);
            }

            Location loc = player.getLocation().add(offset.multiply(0.2));

            if (nearStep(loc))
                continue;

            if (!UtilBlock.solid(loc.getBlock().getRelative(BlockFace.DOWN)))
                continue;

            _steps.put(loc, System.currentTimeMillis());

            UtilParticle.PlayParticle(ParticleType.FOOTSTEP, loc, 0f, 0f, 0f, 0, 1,
					ViewDist.NORMAL, UtilServer.getPlayers());
        }
    }

    public void cleanSteps()
    {
        if (_steps.isEmpty())
            return;

        Iterator<Entry<Location, Long>> stepIterator = _steps.entrySet().iterator();

        while (stepIterator.hasNext())
        {
            Entry<Location, Long> entry = stepIterator.next();

            if (UtilTime.elapsed(entry.getValue(), 10000))
                stepIterator.remove();
        }
    }

    public boolean nearStep(Location loc)
    {
        for (Location other : _steps.keySet())
        {
            if (UtilMath.offset(loc, other) < 0.3)
                return true;
        }

        return false;
    }
}
