package mineplex.core.gadget.gadgets.wineffect;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import mineplex.core.common.animation.AnimationPoint;
import mineplex.core.common.animation.AnimatorEntity;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.WinEffectGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class WinEffectFlames extends WinEffectGadget
{
	
	private DisguisePlayer _npc;
	private int _nextSound = 0;
	
	public WinEffectFlames(GadgetManager manager)
	{
		super(manager, "Flames", UtilText.splitLineToArray(C.cGray + "Is this NBA Jam 1993? Because you’re ON FIRE!", LineFormat.LORE),
				-2, Material.BLAZE_POWDER, (byte) 0);
		
		_schematicName = "FlamesPodium";
	}

	@Override
	public void play()
	{
		
		int i = 0;
		int points = 12;
		double r = 4;
		
		Location start = getBaseLocation();
		start.add(0, 0, r);
		start.setDirection(new Vector(1, 0, 0));
		
		_npc = getNPC(_player, start);
		AnimatorEntity animator = new AnimatorEntity(Manager.getPlugin(), _npc.getEntity().getBukkitEntity());
		
		
		for(double rad = 0; rad < Math.PI*2; rad+= Math.PI/points)
		{
			i++;
			double s = Math.sin(rad)*3;
			double c = Math.cos(rad)*3;
			animator.addPoint(new AnimationPoint(2*i, new Vector(s, 0, c-r), new Vector(c, 0, -s)));
		}
		animator.setRepeat(true);
		animator.start(start);
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if(!isRunning()) return;
		if(event.getType() == UpdateType.FASTEST)
		{
			UtilParticle.PlayParticleToAll(ParticleType.FLAME, _npc.getEntity().getBukkitEntity().getLocation(), null, 0.1f, 50, ViewDist.NORMAL);
		}
		
		if(event.getType() == UpdateType.FASTEST)
		{
			Entity e = _npc.getEntity().getBukkitEntity();
			if(_nextSound == 0) e.getWorld().playSound(e.getLocation(), Sound.FIRE, 3, UtilMath.random.nextFloat()*2);
			if(_nextSound == 1) e.getWorld().playSound(e.getLocation(), Sound.LAVA, 1.5f, UtilMath.random.nextFloat());
			
			_nextSound = (_nextSound +1 ) % 2;
		}
	}
	
	@Override
	public void finish()
	{
	}

}
