package mineplex.core.gadget.gadgets.wineffect;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.WinEffectGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class WinEffectLavaTrap extends WinEffectGadget
{
	
	private int _tick = 0;
	private Block _lever;
	
	private DisguisePlayer _playerNPC;
	
	public WinEffectLavaTrap(GadgetManager manager)
	{
		super(manager, "Lava Trap", UtilText.splitLinesToArray(new String[] {C.cGray + C.Italics + "\"Do you expect me to talk?\"", C.cGray + C.Italics + "\"No Mr. Bond, I expect you to die!\""}, LineFormat.LORE),
				-2, Material.LAVA_BUCKET, (byte) 0);
		
		_schematicName = "LavaTrapPodium";
	}

	@Override
	public void play()
	{
		_baseLocation = _baseLocation.getBlock().getLocation();
		
		_lever = getBaseLocation().add(_baseLocation.getDirection().normalize()).getBlock();
		_lever.setTypeIdAndData(Material.LEVER.getId(), (byte) 5, false);
		
		_playerNPC = getNPC(_player, getBaseLocation());
				
		{	
			Vector forward = getBaseLocation().getDirection().normalize().multiply(1.3);
			Vector right = UtilAlg.getRight(forward.clone()).multiply(1);
			
			int maxPerRow = 6;
			
			for(int index = 0; index < _nonTeam.size(); index++)
			{
				int row = index/maxPerRow;
				int inRow = index%maxPerRow;
				int rowSize = Math.min(_nonTeam.size()-(row*maxPerRow), maxPerRow);
				
				Vector f = forward.clone().multiply(2.5 + row);
				Vector r = right.clone().multiply((-rowSize/2.0) + 0.5);
				r.add(right.clone().multiply(inRow));
				
				Location loc = getBaseLocation().add(f).add(r);
				loc.setDirection(getBaseLocation().subtract(loc).toVector());
				
				((ArmorStand)getNPC(_nonTeam.get(index), loc).getEntity().getBukkitEntity()).setGravity(true);
			}
			
			for(int index = 0; index < _team.size(); index++)
			{
				int row = index/maxPerRow;
				int inRow = index%maxPerRow;
				int rowSize = Math.min(_team.size()-(row*maxPerRow), maxPerRow);
				
				Vector f = forward.clone().multiply(3 + row).multiply(-1);
				Vector r = right.clone().multiply((-rowSize/2.0) + 0.5);
				r.add(right.clone().multiply(inRow));
				
				Location loc = getBaseLocation().add(f).add(r);
				loc.setDirection(getBaseLocation().subtract(loc).toVector());
				
				((ArmorStand)getNPC(_team.get(index), loc).getEntity().getBukkitEntity()).setGravity(true);
			}
		}
		
		_tick = 0;
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (!isRunning()) return;
		
		if(event.getType() != UpdateType.TICK) return;
		
		_tick++;
		
		
		if(_tick == 20*2)
		{
			Location loc = getBaseLocation();
			loc.setDirection(_lever.getLocation().add(0.5, 0, 0.5).subtract(loc.clone().add(0, 1.8, 0)).toVector());
			
			_playerNPC.getEntity().getBukkitEntity().teleport(loc);
		}
		
		if(_tick == 20*3)
		{
			
			_playerNPC.sendHit();
			
			_lever.setTypeIdAndData(Material.LEVER.getId(), (byte) (5 | 0x8), false);
			_lever.getWorld().playSound(_lever.getLocation(), Sound.CLICK, 0.3f, 0.6f);
		}
		
		if(_tick == 20*3 + 1)
		{
			getBaseLocation().getWorld().playSound(getBaseLocation(), Sound.IRONGOLEM_HIT, 0.2f, 1.6f);
		}
		
		
		if(_tick == 20*3 + 10)
		{	
			getBaseLocation().getWorld().playSound(getBaseLocation(), Sound.IRONGOLEM_DEATH, 0.2f, 0.5f);
			
			getBaseLocation().getWorld().playSound(getBaseLocation(), Sound.DIG_STONE, 1, 1f);
			getBaseLocation().getWorld().playSound(getBaseLocation(), Sound.DIG_STONE, 1, 1.2f);
			getBaseLocation().getWorld().playSound(getBaseLocation(), Sound.DIG_STONE, 1, 1.8f);
			getBaseLocation().getWorld().playSound(getBaseLocation(), Sound.DIG_STONE, 1, 1.5f);
			getBaseLocation().getWorld().playSound(getBaseLocation(), Sound.DIG_STONE, 1, 0.5f);
			
			pasteSchematic("LavaTrapPodium-part-2");
		}
	}

	@Override
	public void finish()
	{
		_lever = null;
	}
	
	@Override
	public void teleport()
	{
		Vector dir = getBaseLocation().getDirection().normalize();
		Location loc = getBaseLocation().add(UtilAlg.getRight(dir).multiply(5)).add(0, 2, 0).subtract(dir);
		loc.setDirection(getBaseLocation().subtract(loc).toVector());
		
		super.teleport(loc);
	}
	
}
