package nautilus.game.arcade.game.games.quiver.ultimates;

import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.util.Vector;

public class UltimateBarrage extends UltimatePerk
{

	private static final float CHARGE_PASSIVE = 0.8F;
	private static final float CHARGE_PAYLOAD = 0.4F;
	private static final float CHARGE_KILL = 5F;
	private static final float CHARGE_ASSIST = 2F;

	private static final float VELOCITY_MULTIPLIER = 3;
	
	private int _arrows;

	public UltimateBarrage(int arrows)
	{
		super("Arrow Barrage", new String[] {}, 0, CHARGE_PASSIVE, CHARGE_PAYLOAD, CHARGE_KILL, CHARGE_ASSIST);
		
		_arrows = arrows;
	}

	@EventHandler
	public void onEntityShootBow(EntityShootBowEvent event)
	{
		if (event.isCancelled() || !(event.getEntity() instanceof Player))
		{
			return;
		}

		Player player = (Player) event.getEntity();
		
		if (isUsingUltimate(player))
		{
			for (int i = 0; i < _arrows; i++)
			{
	            Vector random = new Vector((Math.random() - 0.5) / 3, (Math.random() - 0.5) / 3, (Math.random() - 0.5) / 3);
	            Arrow arrow = player.launchProjectile(Arrow.class);
	            
	            arrow.setCritical(true);
	            arrow.setVelocity(player.getLocation().getDirection().add(random).multiply(VELOCITY_MULTIPLIER));
	            player.getWorld().playSound(player.getLocation(), Sound.SHOOT_ARROW, 1F, 1F);
			}
			
			cancel(player);
		}
	}

}
