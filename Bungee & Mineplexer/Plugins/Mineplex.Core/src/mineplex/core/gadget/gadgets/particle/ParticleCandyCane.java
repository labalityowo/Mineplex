package mineplex.core.gadget.gadgets.particle;

import java.util.HashMap;
import java.util.UUID;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

public class ParticleCandyCane extends ParticleGadget
{

	private HashMap<UUID, Double> _map = new HashMap<>();

	public ParticleCandyCane(GadgetManager manager)
	{
		super(manager, "Crushed Candy Cane",
				UtilText.splitLineToArray(C.cGray + "There's no such thing as too much Christmas Candy. Don't listen to your dentist.", LineFormat.LORE),
				-3, Material.INK_SACK, (byte)1);
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK) return;

		boolean moving = Manager.isMoving(player);
		Location loc = player.getLocation();
		float y = moving ? 0.2f : 0.15f;
		double yadd = getY(player);
		if (!moving && yadd < 2.1)
		{
			yadd += 0.05;
			setY(player, yadd);
		}
		if (moving && yadd > 0.7)
		{
			yadd = 0.7;
			setY(player, yadd);
		}
		loc.add(0, yadd, 0);

		float w = 0.2f;
		int a = moving ? 2 : 6;

		if (!moving)
		{
			double d = Math.PI * 2 * (event.getTick() / 50.0);
			Vector v = new Vector(Math.sin(d), 0, Math.cos(d));
			loc.add(v);
		}

		UtilParticle.playParticleFor(player, UtilParticle.ParticleType.ICON_CRACK.getParticle(Material.INK_SACK, 15), loc, w, y, w, 0, a, UtilParticle.ViewDist.NORMAL);
		UtilParticle.playParticleFor(player, UtilParticle.ParticleType.ICON_CRACK.getParticle(Material.INK_SACK, 1), loc, w, y, w, 0, a, UtilParticle.ViewDist.NORMAL);
		UtilParticle.playParticleFor(player, UtilParticle.ParticleType.ICON_CRACK.getParticle(Material.INK_SACK, 15), loc, w, y, w, 0, a, UtilParticle.ViewDist.NORMAL);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		_map.remove(event.getPlayer().getUniqueId());
	}

	private void setY(Player p, double y)
	{
		_map.put(p.getUniqueId(), y);
	}

	private double getY(Player p)
	{
		Double y = _map.get(p.getUniqueId());
		if (y == null) return 3;
		return y;
	}
}