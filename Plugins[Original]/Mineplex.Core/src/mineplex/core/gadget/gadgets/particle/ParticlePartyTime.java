package mineplex.core.gadget.gadgets.particle;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ParticlePartyTime extends ParticleGadget
{

	private Byte[] _data = new Byte[]{1, 2, 4, 5, 6, 9, 10, 11, 12, 13, 14, 15};

	public ParticlePartyTime(GadgetManager manager)
	{
		super(manager, "Party Time",
				UtilText.splitLineToArray(C.cGray + "It is " + C.cPurple + "PARTY TIME!", LineFormat.LORE),
				-3, Material.FIREWORK, (byte) -1);
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Location loc = player.getLocation().add(Math.random() * 2 - 1, 2.3 + Math.random() * 0.7, Math.random() * 2 - 1);

		List<Byte> list = Arrays.asList(_data);
		Collections.shuffle(list);
		for (int i = 0; i < 1; i++)
		{
			String particle = ParticleType.ICON_CRACK.getParticle(Material.INK_SACK, list.get(i));
			if (Manager.isMoving(player))
			{
				UtilParticle.playParticleFor(player, particle, player.getLocation().add(0, 1, 0), null, 0.08f, 1, ViewDist.NORMAL);
			}
			else
			{
				UtilParticle.playParticleFor(player, particle, loc, null, 0.08f, 10, ViewDist.NORMAL);
			}
		}
	}
}
