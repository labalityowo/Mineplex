package nautilus.game.arcade.game.games.smash.perks.snowman;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Effect;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.data.IcePathData;

public class PerkIcePath extends Perk
{
	
	private int _cooldown;
	private int _meltTime;
	
	private Set<IcePathData> _data = new HashSet<>();

	public PerkIcePath()
	{
		super("Ice Path", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Axe to " + C.cGreen + "Ice Path" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_meltTime = getPerkTime("Melt Time");
	}

	@EventHandler
	public void Skill(PlayerInteractEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		if (UtilBlock.usable(event.getClickedBlock()))
		{
			return;
		}

		Player player = event.getPlayer();

		if (!UtilItem.isAxe(player.getItemInHand()))
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}

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
	public void Freeze(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
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
				Manager.GetBlockRestore().add(block, 79, (byte) 0, _meltTime);
			}
		}
	}
}
