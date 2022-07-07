package mineplex.core.gadget.gadgets.morph;

import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguiseSkeleton;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.recharge.Recharge;
import mineplex.core.recharge.RechargeData;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MorphChristmasKing extends MorphGadget
{
	private final Map<Projectile, Player> _snowball = new WeakHashMap<>();

	public MorphChristmasKing(GadgetManager manager)
	{
		super(manager, "Christmas Kings Head", UtilText.splitLinesToArray(new String[] 
				{
				C.cGray + "Transforms the wearer into the",
						C.cGray + "Pumpkin King's not so jolly Winter Form!",
						"",
						C.cBlue + "Earned by defeating the Pumpkin King",
						C.cBlue + "in the 2016 Christmas Chaos Event",
						C.blankLine,
		                "#" + C.cWhite + "Sneak to use Snowstorm"
				}, LineFormat.LORE),
				-1,
				Material.PUMPKIN, (byte)0);

	}

	@Override
	public void enableCustom(final Player player, boolean message)
	{
		this.applyArmor(player, message);

		DisguiseSkeleton disguise = new DisguiseSkeleton(player);
		disguise.showArmor();
		disguise.SetSkeletonType(SkeletonType.WITHER);
		UtilMorph.disguise(player, disguise, Manager);

		player.getInventory().setHelmet(new ItemStack(Material.JACK_O_LANTERN));
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		this.removeArmor(player);
		UtilMorph.undisguise(player, Manager.getDisguiseManager());
		player.getInventory().setHelmet(null);
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() == UpdateType.FASTEST)
		{
			for (Player player : getActive())
			{
				NautHashMap<String, RechargeData> map = Recharge.Instance.Get(player);
				if (map == null)
					continue;
				
				RechargeData data = map.get("Snowstorm");
				if (data == null)
					continue;
				if (data.GetRemaining() < 25000)
					continue;
				
				for (int i = 0; i < 6; i++)
				{
					Snowball snow = player.getWorld().spawn(player.getEyeLocation().add(player.getLocation().getDirection()), Snowball.class);
					double x = 0.1 - (UtilMath.r(20)/100d);
					double y = UtilMath.r(20)/100d;
					double z = 0.1 - (UtilMath.r(20)/100d);
					snow.setShooter(player);
					snow.setVelocity(player.getLocation().getDirection().add(new Vector(x,y,z)).multiply(2));
					_snowball.put(snow, player);
				}
	
				//Effect
				player.getWorld().playSound(player.getLocation(), Sound.STEP_SNOW, 0.2f, 0.5f);
			}
		}
		
		if (event.getType() == UpdateType.TICK)
		{
			for (Player player : getActive())
			{
				if (Manager.isMoving(player))
				{
					continue;
				}
				for (double y = 0; y < 5; y++)
				{
					double sin = Math.sin(y);
					double cos = Math.cos(y);
					
					Location loc1 = player.getLocation().add(sin, y, cos);
					Location loc2 = player.getLocation().add(cos, y, sin);
					UtilParticle.PlayParticleToAll(ParticleType.SNOWBALL_POOF, loc1, null, 0, 1, ViewDist.NORMAL);
					UtilParticle.PlayParticleToAll(ParticleType.SNOWBALL_POOF, loc2, null, 0, 1, ViewDist.NORMAL);
				}
			}
		}
	}
	
	@EventHandler
	public void onSneak(PlayerToggleSneakEvent event)
	{
		if (!isActive(event.getPlayer()))
			return;
		
		if (!event.isSneaking())
			return;
		
		if (Recharge.Instance.use(event.getPlayer(), "Snowstorm", 30000, true, true))
		{
			event.getPlayer().sendMessage(F.main("Recharge", "You used " + F.skill("Snowstorm") + "."));
		}
	}
	
	@EventHandler
	public void onSnowballHit(EntityDamageByEntityEvent event)
	{
		if (!(event.getDamager() instanceof Snowball))
			return;
		
		Snowball proj = (Snowball) event.getDamager();

		if (_snowball.remove(proj) == null || !Manager.selectEntity(this, event.getEntity()))
		{
			return;
		}

		UtilAction.velocity(event.getEntity(), proj.getVelocity().multiply(0.15).add(new Vector(0, 0.15, 0)));
	}
}