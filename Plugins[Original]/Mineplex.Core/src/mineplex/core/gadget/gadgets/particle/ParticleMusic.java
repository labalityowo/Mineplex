package mineplex.core.gadget.gadgets.particle;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

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

public class ParticleMusic extends ParticleGadget
{

	public ParticleMusic(GadgetManager manager)
	{
		super(manager, "Music",
                UtilText.splitLineToArray(C.cGray + C.Italics + "\u266B Don't stop the beat! Can't control my feet! \u266B", LineFormat.LORE),
                -2, Material.GREEN_RECORD, (byte) 0);
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		boolean moving = Manager.isMoving(player);
		UpdateType type = moving? UpdateType.FASTER : UpdateType.FAST;
		
		if (event.getType() != type) return;
		
		float[] ds = new float[]{0.24f, 0.25f, 0.26f, 0.56f, 0.58f, 0.6f, 0.91f};

		int index = Math.abs(event.getTick()%(ds.length*10)/10);
		float d = ds[index];
		double rad = Math.PI * 2 * (Math.abs(event.getTick()%(20*10)/(10*20d)));

		int amount = 4;

		double step = Math.PI * 2;
		step /= amount;

		double r = 1;

		if (moving)
		{
			UtilParticle.playParticleFor(player, ParticleType.NOTE, player.getLocation(), d, 0, 0, 1, 0, ViewDist.NORMAL);
			return;
		}

		for (int a = 0; a < amount; a++)
		{
			double rad2 = rad + step * a;

			double x = Math.sin(rad2) * r;
			double y = 1 + (Math.sin(rad2 * 11) + Math.cos(rad2 * 13)) / 4.0;
			double z = Math.cos(rad2) * r;


			Location loc = player.getLocation().add(x, y, z);

			UtilParticle.playParticleFor(player, ParticleType.NOTE, loc, d, 0, 0, 1, 0, ViewDist.NORMAL);
		}

		if (getSet() != null && getSet().isActive(player))
		{
			double red = Math.sin(((float) d + 0.0F) * (float) Math.PI * 2.0F) * 0.65F + 0.35F;
			double green = Math.sin(((float) d + 0.33333334F) * (float) Math.PI * 2.0F) * 0.65F + 0.35F;
			double blue = Math.sin(((float) d + 0.6666667F) * (float) Math.PI * 2.0F) * 0.65F + 0.35F;
			
			if(red == 0) red = 0.00001;
			
			double step2 = Math.PI/12;
			if(Manager.isMoving(player)) step2 = Math.PI/2;
			
			for(double rad2 = 0; rad2 < Math.PI*2; rad2 += step2)
			{
				double x = Math.sin(rad2 + step) * r;
				double z = Math.cos(rad2 + step) * r;
				
				Location loc = player.getLocation().add(x, 1.25, z);
				UtilParticle.playParticleFor(player, ParticleType.RED_DUST, loc, new Vector(red, green, blue), 1, 0, ViewDist.NORMAL);
			}
		}

	}

}
