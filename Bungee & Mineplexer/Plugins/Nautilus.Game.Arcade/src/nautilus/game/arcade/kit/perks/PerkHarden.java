package nautilus.game.arcade.kit.perks;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.kit.Perk;

public class PerkHarden extends Perk
{
    private boolean _isSword;

    public PerkHarden(boolean isSword)
    {
        super("Harden", new String[]
            {
                C.cYellow + "Right-Click" + C.cGray + " with Axe to use " + C.cGreen + "Harden"
            });
        _isSword = isSword;
    }

    @EventHandler
    public void skill(PlayerInteractEvent event)
    {
        if (event.isCancelled())
            return;

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (UtilBlock.usable(event.getClickedBlock()))
            return;
        
        ItemStack item = event.getPlayer().getItemInHand();
        if (_isSword ? !UtilGear.isSword(item) : !UtilGear.isAxe(item))
            return;

        Player player = event.getPlayer();

        if (!Kit.HasKit(player))
            return;

        if (!Recharge.Instance.use(player, GetName(), 30000, true, true))
            return;

        // Action
        Manager.GetCondition().Factory().Slow(GetName(), player, player, 8, 1, false, false, false, false);
        Manager.GetCondition().Factory().HealthBoost(GetName(), player, player, 8, 3, false, false, false);

        // Inform
        UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));

        // Effect
        player.getWorld().playSound(player.getLocation(), Sound.HORSE_ARMOR, 1f, 1f);
    }
}
