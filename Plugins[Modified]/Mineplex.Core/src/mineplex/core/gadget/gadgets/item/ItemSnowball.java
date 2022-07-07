package mineplex.core.gadget.gadgets.item;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.Ammo;
import mineplex.core.gadget.types.ItemGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ItemSnowball extends ItemGadget
{
	
	private NautHashMap<Snowball, Player> _snowballs = new NautHashMap<Snowball, Player>();

	public ItemSnowball(GadgetManager manager)
	{
		super(manager, "Snowball", 
				UtilText.splitLineToArray(C.cWhite + "Join in on the festive fun by throwing snow at people!", LineFormat.LORE),
				-1, Material.SNOW_BALL, (byte) 0, 1, new Ammo("Snowball", "1 Snowball", Material.SNOW_BALL, (byte) 0, new String[]
				{
					C.cWhite + "50 Snowballs for you to throw!"
				}
		, -3, 1));

		setHidden(true);
	}

	@Override
	public void ActivateCustom(Player player)
	{
		Snowball ball = player.launchProjectile(Snowball.class);
		_snowballs.put(ball, player);
		ball.getWorld().playSound(ball.getLocation(), Sound.CHICKEN_EGG_POP, 1f, 0.3f);
	}
	
	@EventHandler
	public void onHit(EntityDamageByEntityEvent event)
	{
		if (!(event.getDamager() instanceof Snowball))
			return;
		
		Snowball ball = (Snowball) event.getDamager();
		
		if(!_snowballs.containsKey(ball)) return;
		
		if (!Manager.selectEntity(this, event.getEntity()))
		{
			return;
		}
		
		UtilAction.velocity(event.getEntity(), event.getDamager().getVelocity().normalize().add(new Vector(0,0.5,0)).multiply(0.5));
		event.getDamager().getWorld().playSound(event.getDamager().getLocation(), Sound.STEP_SNOW, 1, 0.5f);
	}

	@EventHandler
	public void cleanup(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		_snowballs.entrySet().removeIf(ent -> !ent.getKey().isValid());
	}
}
