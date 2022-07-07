package mineplex.core.gadget.gadgets.wineffect;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.WinEffectGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class WinEffectMrPunchMan extends WinEffectGadget
{
	
	private List<ArmorStand> _playersLeft;
	private DisguisePlayer _npc;
	private long _nextHit = -1;

	public WinEffectMrPunchMan(GadgetManager manager)
	{
		super(manager, "Mr. Punchman", UtilText.splitLineToArray(C.cGray + "Some say he's still punching on that oversized punching bag in the sky.", LineFormat.LORE),
				-2, Material.IRON_AXE, (byte) 0, "Mr.Punchman");
		
		_schematicName = "FlatPodium";
	}

	@Override
	public void play()
	{
		_npc = getNPC(_player, getBaseLocation());
		
		List<Location> circle = UtilShapes.getPointsInCircle(getBaseLocation(), _other.size(), 3);
		
		_playersLeft = new ArrayList<>();
		
		for(int i = 0; i < _other.size(); i++)
		{
			_playersLeft.add((ArmorStand) getNPC(_other.get(i), circle.get(i)).getEntity().getBukkitEntity());
		}
		
		_nextHit = getNextHit() + 1000;
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event) 
	{	
		if(event.getType() != UpdateType.TICK) return;
		
		if(!isRunning()) return;
		
		if(_playersLeft == null || _playersLeft.isEmpty()) return;
		
		if(System.currentTimeMillis() < _nextHit) return;
		
		ArmorStand player = _playersLeft.get(0);
			
		Location loc = _npc.getEntity().getBukkitEntity().getLocation();
		loc.setDirection(player.getLocation().subtract(loc).toVector());
		_npc.getEntity().getBukkitEntity().teleport(loc);
		
		
		_npc.sendHit();
		
		player.setHealth(0);
		_playersLeft.remove(0);
		
		player.getWorld().playSound(player.getLocation(), Sound.VILLAGER_HIT, 2, 1);
		player.getWorld().playSound(player.getLocation(), Sound.VILLAGER_DEATH, 1, 1);
		
		_nextHit = getNextHit();
		
	}
	
	public long getNextHit()
	{
		if(_playersLeft.isEmpty()) return 0;
		long timeLeft = _finish-System.currentTimeMillis();
		_finish -= 150;
		long diff = Math.min(1000, _playersLeft.size()/timeLeft);
		return diff+System.currentTimeMillis();
	}

	@Override
	public void finish()
	{
		_npc.getEntity().getBukkitEntity().remove();
	}

}
