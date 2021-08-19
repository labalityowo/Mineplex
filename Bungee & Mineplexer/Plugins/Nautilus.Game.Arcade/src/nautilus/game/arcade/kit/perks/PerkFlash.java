package nautilus.game.arcade.kit.perks;

import java.util.HashMap;

import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkFlash extends Perk
{
    private int _maxCharges;
    private boolean _explode;

    private HashMap<Player, Integer> _charges = new HashMap<Player, Integer>();

    public PerkFlash(int maxCharges, boolean explode)
    {
        super("Flash", new String[]
            {
                C.cYellow + "Right-Click" + C.cGray + " with Axe to use " + C.cGreen + "Flash"
            });

        _maxCharges = maxCharges;
        _explode = explode;
    }

    @EventHandler
    public void addCharge(UpdateEvent event)
    {
        if (Manager.GetGame() == null)
            return;

        if (_maxCharges <= 1)
            return;

        for (Player cur : Manager.GetGame().GetPlayers(true))
        {
            if (!Kit.HasKit(cur))
                continue;

            if (!_charges.containsKey(cur))
            {
                _charges.put(cur, 0);
            }
            else
            {
                int charges = _charges.get(cur);

                if (charges >= 4)
                    continue;

                if (!Recharge.Instance.use(cur, "Flash Recharge", 4000, false, false))
                    continue;

                _charges.put(cur, charges + 1);

                // Inform
                UtilPlayer.message(cur, F.main("Game", "Flash Charges: " + F.elem((charges + 1) + "")));
            }
        }
    }

    @EventHandler
    public void Skill(PlayerInteractEvent event)
    {
        if (event.isCancelled())
            return;

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (UtilBlock.usable(event.getClickedBlock()))
            return;

        if (!UtilGear.isAxe(event.getPlayer().getItemInHand()))
            return;

        Player player = event.getPlayer();

        if (!Kit.HasKit(player))
            return;

        // Use Recharge
        if (_maxCharges <= 1)
        {
            Recharge.Instance.use(player, "Flash", 8000, true, true);
        }
        else
        {
            int charges = _charges.get(player);

            if (charges <= 0)
            {
                if (!Recharge.Instance.use(player, "Flash Recharge", 4000, true, false))
                    return;
            }
            else
            {
                _charges.put(player, _charges.get(player) - 1);
            }
        }

        double maxRange = 6;
        double curRange = 0;
        while (curRange <= maxRange)
        {
            Location newTarget = player.getLocation().add(new Vector(0, 0.2, 0))
                    .add(player.getLocation().getDirection().multiply(curRange));

            if (!UtilBlock.airFoliage(newTarget.getBlock())
                    || !UtilBlock.airFoliage(newTarget.getBlock().getRelative(BlockFace.UP)))
                break;

            // Progress Forwards
            curRange += 0.2;

            // Smoke Trail
            UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, newTarget.clone().add(0, 0.5, 0), 0, 0, 0, 0, 1,
					ViewDist.LONGER, UtilServer.getPlayers());
        }

        // Modify Range
        curRange -= 0.4;
        if (curRange < 0)
            curRange = 0;

        // Destination
        Location loc = player.getLocation()
                .add(player.getLocation().getDirection().multiply(curRange).add(new Vector(0, 0.4, 0)));

        if (curRange > 0)
            player.teleport(loc);

        player.setFallDistance(0);

        // Inform
        UtilPlayer.message(player, F.main("Game", "Flash Charges: " + F.elem(_charges.get(player) + "")));

        // Effect
        player.getWorld().playSound(player.getLocation(), Sound.WITHER_SHOOT, 0.4f, 1.2f);
        player.getWorld().playSound(player.getLocation(), Sound.SILVERFISH_KILL, 1f, 1.6f);

        // Phase Blast
        if (_explode)
        {
            // Particle
            UtilFirework.playFirework(loc, Type.BALL_LARGE, Manager.GetGame().GetTeam(player).GetColorBase(), false, false);

            HashMap<LivingEntity, Double> targets = UtilEnt.getInRadius(player.getLocation(), 4);
            for (LivingEntity cur : targets.keySet())
            {
                if (!(cur instanceof Player))
                    return;

                if (cur.equals(player))
                    continue;

                Player other = (Player) cur;

                if (!Manager.GetGame().IsAlive(other))
                    continue;

                if (Manager.GetGame().GetTeam(other).equals(Manager.GetGame().GetTeam(player)))
                    continue;

                // Damage Event
                Manager.GetDamage().NewDamageEvent(cur, player, null, DamageCause.CUSTOM, 8, true, true, false, player.getName(),
                        "Phase Blast");
            }
        }
    }
}
