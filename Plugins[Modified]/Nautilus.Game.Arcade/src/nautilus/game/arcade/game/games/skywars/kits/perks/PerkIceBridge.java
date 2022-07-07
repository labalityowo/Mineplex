package nautilus.game.arcade.game.games.skywars.kits.perks;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Effect;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.kit.perks.data.IcePathData;

public class PerkIceBridge extends SkywarsPerk
{

	private final Set<IcePathData> _data = new HashSet<>();
	private final long _cooldown, _bridgeUpTime;

	public PerkIceBridge(ItemStack itemStack, long cooldown, long bridgeUpTime)
	{
		super("Ice Bridge", itemStack);

		_cooldown = cooldown;
		_bridgeUpTime = bridgeUpTime;
	}

	@Override
	public void onUseItem(Player player)
	{
		if (!Recharge.Instance.use(player, GetName(), _cooldown, true, true))
		{
			return;
		}

		player.teleport(player.getLocation().add(0, 1, 0));
		UtilAction.velocity(player, new Vector(0, 0.5, 0));

		_data.add(new IcePathData(player));

		// Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		Iterator<IcePathData> dataIterator = _data.iterator();

		while (dataIterator.hasNext())
		{
			IcePathData data = dataIterator.next();

			Block block = data.GetNextBlock();

			if (block == null)
			{
				dataIterator.remove();
			}
			else
			{
				block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, 79);
				Manager.GetBlockRestore().add(block, 79, (byte) 0, _bridgeUpTime);
			}
		}
	}
}
