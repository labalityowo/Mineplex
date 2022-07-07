package mineplex.core.gadget.gadgets.particle.king;

import java.awt.Color;
import java.time.Month;
import java.time.YearMonth;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.shape.ShapeWings;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ParticleKing extends ParticleGadget
{

	private static final int CROWN_POINTS = 12;

	private ShapeWings _capeRed = new ShapeWings(UtilParticle.ParticleType.RED_DUST.particleName, new Vector(0.2,0.2,0.2), 1, 0, false, ShapeWings.NO_ROTATION, ShapeWings.KINGS_CAPE);
	private ShapeWings _capeWhite = new ShapeWings(UtilParticle.ParticleType.RED_DUST.particleName, new Vector(0.1,0.1,0.1), 1, 0, true, ShapeWings.NO_ROTATION, ShapeWings.KINGS_CAPE);

	private CastleManager _castleManager;

	public ParticleKing(GadgetManager manager, CastleManager castleManager)
	{
		super(manager, "King", UtilText.splitLinesToArray(new String[]
						{
								C.cGray + "Hail to the King!",
								C.cBlack + "",
								C.cWhite + "Right Click players in the Lobby to claim them for your Kingdom.",
								C.cWhite + "The King with the biggest Kingdom will own the Castle in our Lobby."
						}, LineFormat.LORE),
				-14,
				Material.GOLD_HELMET, (byte) 0);

		setPPCYearMonth(YearMonth.of(2017, Month.MAY));

		_castleManager = castleManager;
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		Location loc = player.getLocation().add(0, 0.8, 0).add(player.getLocation().getDirection().multiply(-0.3));
		if (Manager.isMoving(player))
		{
			if (event.getType() == UpdateType.TICK)
			{
				_capeRed.displayColoredParticle(loc, Color.RED);
				_capeWhite.displayColoredParticle(loc, Color.WHITE);
			}
		}
		else if (event.getType() == UpdateType.FAST)
		{
			_capeRed.displayColored(loc, Color.RED);
			_capeWhite.displayColored(loc, Color.WHITE);
		}
		if (event.getType() == UpdateType.FAST)
		{
			for (int i = 0; i < 360; i += 360 / CROWN_POINTS)
			{
				double angle = (i * Math.PI / 180);
				double x = 0.5 * Math.cos(angle);
				double z = 0.5 * Math.sin(angle);
				Location crown = player.getEyeLocation().add(x, 0.3, z);
				UtilParticle.PlayParticleToAll(UtilParticle.ParticleType.FLAME, crown, null, 0, 1, UtilParticle.ViewDist.NORMAL);
			}
		}
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		super.enableCustom(player, message);
		_castleManager.setPlayerAsKing(player);
		_castleManager.updateLobbyKing();
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		super.disableCustom(player, message);
		_castleManager.setPlayerAsPeasant(player);
		_castleManager.updateLobbyKing();
	}
}
