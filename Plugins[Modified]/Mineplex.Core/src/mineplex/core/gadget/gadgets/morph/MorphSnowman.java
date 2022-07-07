package mineplex.core.gadget.gadgets.morph;

import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguiseSnowman;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.recharge.Recharge;
import mineplex.core.recharge.RechargeData;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MorphSnowman extends MorphGadget
{

	private final Map<Projectile, Player> _snowball = new WeakHashMap<>();

	public MorphSnowman(GadgetManager manager)
	{
		super(manager, "Olaf Morph",
				UtilText.splitLinesToArray(new String[]
						{
								C.cGray + "Do you wanna build a snowman?",
								C.cGray + "It doesn't have to be a snowman...",
								C.cGray + "Or... it kind of does...",
								C.blankLine,
								"#" + C.cWhite + "Left-Click to use Blizzard",
								"#" + C.cWhite + "Sneak to use Snow Slide",
						}, LineFormat.LORE),
				-3, Material.SNOW_BALL, (byte) 0);
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		this.applyArmor(player, message);

		DisguiseSnowman disguise = new DisguiseSnowman(player);
		UtilMorph.disguise(player, disguise, Manager);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		this.removeArmor(player);
		UtilMorph.undisguise(player, Manager.getDisguiseManager());
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event)
	{
		if (!isActive(event.getPlayer())) return;

		if (!UtilEvent.isAction(event, ActionType.L)) return;

		if (event.getItem() != null && event.getItem().getType() != Material.AIR) return;

		if (Recharge.Instance.use(event.getPlayer(), "Blizzard", 12000, true, true))
		{
			event.getPlayer().sendMessage(F.main("Recharge", "You used " + F.skill("Blizzard") + "."));
		}
	}

	@EventHandler
	public void Snow(UpdateEvent event)
	{
		if (event.getType() == UpdateType.FASTEST)
		{

			for (Player player : getActive())
			{
				NautHashMap<String, RechargeData> map = Recharge.Instance.Get(player);
				if (map == null) continue;

				RechargeData data = map.get("Blizzard");
				if (data == null) continue;
				if (data.GetRemaining() < 10000) continue;

				for (int i = 0; i < 4; i++)
				{
					Snowball snow = player.getWorld().spawn(player.getEyeLocation().add(player.getLocation().getDirection()), Snowball.class);
					double x = 0.1 - (UtilMath.r(20) / 100d);
					double y = UtilMath.r(20) / 100d;
					double z = 0.1 - (UtilMath.r(20) / 100d);
					snow.setShooter(player);
					snow.setVelocity(player.getLocation().getDirection().add(new Vector(x, y, z)).multiply(2));
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
				if (player.isSneaking() && UtilEnt.isGrounded(player))
				{
					player.setVelocity(player.getLocation().getDirection().setY(0).normalize());
					UtilParticle.PlayParticle(ParticleType.SNOW_SHOVEL, player.getLocation(), 0.3f, 0.1f, 0.3f, 0, 10, ViewDist.NORMAL);
					player.getWorld().playSound(player.getLocation(), Sound.STEP_SNOW, 0.3f, 0.3f);
				}
			}
		}
	}

	@EventHandler
	public void Snowball(EntityDamageByEntityEvent event)
	{
		if (!(event.getDamager() instanceof Snowball))
			return;

		Snowball proj = (Snowball) event.getDamager();

		if (_snowball.remove(proj) == null || Manager.selectEntity(this, event.getEntity()))
		{
			return;
		}

		UtilAction.velocity(event.getEntity(), proj.getVelocity().multiply(0.15).add(new Vector(0, 0.15, 0)));
	}


}
