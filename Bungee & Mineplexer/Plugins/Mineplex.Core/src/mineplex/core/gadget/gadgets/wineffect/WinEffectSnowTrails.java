package mineplex.core.gadget.gadgets.wineffect;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.WeatherType;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import mineplex.core.common.animation.AnimationPoint;
import mineplex.core.common.animation.AnimatorEntity;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.WinEffectGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class WinEffectSnowTrails extends WinEffectGadget
{
	private DisguisePlayer _npc;
	
	public WinEffectSnowTrails(GadgetManager manager)
	{
		super(manager, "Snow Trail", UtilText.splitLineToArray(C.cGray + "What killed the dinosaurs? THE ICE AGE! Talk about the cold shoulder. Haha, puns.", LineFormat.LORE),
				-2, Material.SNOW_BALL, (byte) 0);
		
		_schematicName = "SnowPodium";
	}

	@Override
	public void play()
	{	
		int i = 0;
		int points = 12;
		double r = 3;
		
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
		
		if(event.getType() != UpdateType.TICK) return;
		
		Location loc = _npc.getEntity().getBukkitEntity().getLocation();
		
		UtilParticle.PlayParticleToAll(ParticleType.SNOW_SHOVEL, loc.clone().add(0, 1, 0), 0.3f, 1, 0.3f, 0, 20, ViewDist.NORMAL);
		UtilParticle.PlayParticleToAll(ParticleType.SNOW_SHOVEL, loc.clone().add(0, 0.2, 0), 0.6f, 0.2f, 0.6f, 0, 20, ViewDist.NORMAL);
		
		if(event.getTick()%3 == 0) loc.getWorld().playSound(loc, Sound.STEP_SNOW, 1.25f, 0.75f);
		
	}
	
	@Override
	public void finish()
	{
	}
	
	@Override
	public void buildWinnerRoom()
	{
		super.buildWinnerRoom();
		
		Location loc = getBaseLocation();
		
		for(int x = loc.getBlockX()-20; x < loc.getBlockX()+20; x++)
		{
			for(int z = loc.getBlockZ()-20; z < loc.getBlockZ()+20; z++)
			{
				loc.getWorld().setBiome(x, z, Biome.ICE_PLAINS);
			}
		}
	}
	
	@Override
	public void teleport()
	{
		super.teleport();
		
		for(Player p : UtilServer.getPlayers())
		{
			p.setPlayerWeather(WeatherType.DOWNFALL);
		}
	}

}
