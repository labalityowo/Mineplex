package mineplex.core.gadget.gadgets.wineffect;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguiseGuardian;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.WinEffectGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import net.minecraft.server.v1_8_R3.Packet;

public class WinEffectRiseOfTheElderGuardian extends WinEffectGadget
{
	private List<DisguiseGuardian> _guardians;
	private DisguisePlayer _npcPlayer;
	private DisguiseGuardian _npcGuardian;
	private int _tick;
	
	public WinEffectRiseOfTheElderGuardian(GadgetManager manager)
	{
		super(manager, "Rise of the Elder Guardian", UtilText.splitLinesToArray(new String[]{C.cGray + C.Italics + "Say hello to my little friend...", " ", " ", C.cGray + C.Italics + "the Elder Guardian."}, LineFormat.LORE),
				-2, Material.PRISMARINE, (byte) 2);
		
		_schematicName = "ElderGuardianPodium";
	}

	@Override
	public void play()
	{
		_guardians = new ArrayList<DisguiseGuardian>();
		
		int amount = 4;
		for(int i = 0; i < amount; i++)
		{
			double rad = ((Math.PI*2)/amount) * i + Math.PI/4.0;
			double x = Math.sin(rad);
			double z = Math.cos(rad);
			
			Vector diff = new Vector(x, 0, z).multiply(5.7).setY(1);
			
			Location loc = getBaseLocation().add(diff);
			
			loc.setDirection(diff.add(new Vector(0, -1, 0)).multiply(-1));
			
			ArmorStand stand = getBaseLocation().getWorld().spawn(loc, ArmorStand.class);
			stand.setGravity(false);
			
			DisguiseGuardian g = new DisguiseGuardian(stand);
			Manager.getDisguiseManager().disguise(g);
			
			_guardians.add(g);
		}
		
		
		
		Location loc = getBaseLocation();
		loc.setDirection(_player.getLocation().subtract(loc).toVector());
		_npcPlayer = getNPC(_player, loc);
		_npcPlayer.setHeldItem(new ItemStack(Material.DIAMOND_SWORD));
		_npcPlayer.sendPacket(_npcPlayer.getEquipmentPackets().toArray(new Packet[5]));
		
		_tick = 0;
		
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if(!isRunning()) return;
		
		if(event.getType() != UpdateType.TICK) return;
		
		if(_tick == 20*1)
		{
			for(DisguiseGuardian g : _guardians)
			{
				g.setTarget(_npcPlayer.getEntityId());
				Manager.getDisguiseManager().updateDisguise(g);
			}
		}
		
		if(_tick >= 20 && _tick <= 20*4)
		{
			float progress = (_tick-20)/40.0f;
			
			getBaseLocation().getWorld().playSound(getBaseLocation(), Sound.FIZZ, 1.25f + progress, 0.25f + progress);
		}
		
		if(_tick == 20*4)
		{			
			UtilParticle.PlayParticleToAll(ParticleType.EXPLODE, getBaseLocation().add(0, 1, 0), 0.3f, 0.6f, 0.3f, 0.07f, 400, ViewDist.NORMAL);
			UtilParticle.PlayParticleToAll(ParticleType.EXPLODE, getBaseLocation().add(0, 0.3, 0), 0.7f, 0.1f, 0.7f, 0.07f, 400, ViewDist.NORMAL);
			getBaseLocation().getWorld().playSound(getBaseLocation(), Sound.ZOMBIE_REMEDY, 6f, 0.75f);
			
			getBaseLocation().getWorld().playSound(getBaseLocation(), Sound.EXPLODE, 1, 1);
			
			for(DisguiseGuardian g : _guardians)
			{
				g.setTarget(0);
				Manager.getDisguiseManager().updateDisguise(g);
			}
			
			ArmorStand stand = (ArmorStand) _npcPlayer.getEntity().getBukkitEntity();
			
			Manager.getDisguiseManager().undisguise(stand);
			
			_npcGuardian = new DisguiseGuardian(stand);
			_npcGuardian.setElder(true);
			
			Manager.getDisguiseManager().disguise(_npcGuardian);
		}
		
		_tick++;
	}

	@Override
	public void finish()
	{
		for(DisguiseGuardian g : _guardians)
		{
			g.getEntity().getBukkitEntity().remove();
		}
		_guardians.clear();
		_guardians = null;
	}

}
