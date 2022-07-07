package mineplex.core.gadget.gadgets.wineffect;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import mineplex.core.common.animation.AnimationPoint;
import mineplex.core.common.animation.AnimatorEntity;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.WinEffectGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class WinEffectPodium extends WinEffectGadget
{
	
	private DisguisePlayer _npc;

	public WinEffectPodium(GadgetManager manager)
	{
		super(manager, "Podium", UtilText.splitLineToArray(C.cGray + "You are the wiener! I mean WINNER! Phew, glad that didn’t make it into the release.", LineFormat.LORE),
				0, Material.DIAMOND, (byte)0, true);
	}

	@Override
	public void play()
	{	
		_npc = getNPC(this._player, getBaseLocation());
		
		AnimatorEntity animator = new AnimatorEntity(Manager.getPlugin(), _npc.getEntity().getBukkitEntity());
		
		animator.addPoint(new AnimationPoint( 20, new Vector(0,0,0), new Vector(-1, 0.5, 0)));
		animator.addPoint(new AnimationPoint( 40, new Vector(0,0,0), new Vector( 0, 0.5,-1)));
		animator.addPoint(new AnimationPoint( 60, new Vector(0,0,0), new Vector( 1, 0.5, 0)));
		animator.addPoint(new AnimationPoint( 80, new Vector(0,0,0), new Vector( 0, 0.5, 1)));
		
		animator.setRepeat(true);
		
		Location loc = _npc.getEntity().getBukkitEntity().getLocation();
		loc.setDirection(new Vector(0,  0.5,  1));
		animator.start(loc);
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (!isRunning()) return;

		if (event.getType() == UpdateType.FASTER)
		{
			if (_npc != null)
				_npc.sendHit();
		}

		if (event.getType() != UpdateType.FAST) return;

		spawnFirework(event.getTick());

//		Location loc = getBaseLocation();
//
//		for(int i = 0; i < 3; i++)
//		{
//			double r = 3;
//			double rad = (((Math.PI*2)/3.0)*i) + ((event.getTick()%240) * Math.PI/120.0);
//			double x = Math.sin(rad) * r;
//			double z = Math.cos(rad) * r;
//
//			Location l = loc.clone().add(x, 0, z);
//			UtilFirework.launchFirework(l, Type.BALL, Color.fromRGB(UtilMath.r(255*255*255)), false, true, new Vector(0, 0.01, 0), 1);
//		}
	}

	@Override
	public void finish()
	{
		Manager.getDisguiseManager().undisguise(_npc);
		_npc = null;
	}

}
