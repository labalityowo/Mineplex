package mineplex.core.gadget.gadgets.item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.event.PlayerConsumeMelonEvent;
import mineplex.core.gadget.gadgets.Ammo;
import mineplex.core.gadget.types.ItemGadget;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ItemMelonLauncher extends ItemGadget implements IThrown
{

	private final List<Item> _melon = new ArrayList<>();

	public ItemMelonLauncher(GadgetManager manager)
	{
		super(manager, "Melon Launcher",
				UtilText.splitLineToArray(C.cWhite + "Because who doesn't want to shoot watermelons at people?!", LineFormat.LORE),
				-1,
				Material.MELON_BLOCK, (byte) 0,
				1000, new Ammo("Melon Launcher", "100 Melons", Material.MELON_BLOCK, (byte) 0, new String[] {C.cWhite + "100 Melons for you to launch!"}, 500, 100));
	}

	@Override
	public void ActivateCustom(Player player)
	{
		//Action
		Item item = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()), ItemStackFactory.Instance.CreateStack(Material.MELON_BLOCK));
		UtilAction.velocity(item, player.getLocation().getDirection(),
				1, false, 0, 0.2, 10, false);

		Manager.getProjectileManager().AddThrow(item, player, this, 5000, true, true, true, true, 0.5f);

		//Inform
		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(getName()) + "."));

		//Effect
		item.getWorld().playSound(item.getLocation(), Sound.EXPLODE, 0.5f, 0.5f);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		if (target != null)
		{
			if (!Manager.selectEntity(this, target))
			{
				return;
			}

			//Push
			UtilAction.velocity(target,
					UtilAlg.getTrajectory2d(data.getThrown().getLocation(), target.getLocation()),
					1.4, false, 0, 0.8, 1.5, true);

			target.playEffect(EntityEffect.HURT);
		}

		smash(data.getThrown());
	}

	@Override
	public void Idle(ProjectileUser data)
	{
		smash(data.getThrown());
	}

	@Override
	public void Expire(ProjectileUser data)
	{
		smash(data.getThrown());
	}

	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}

	public void smash(Entity ent)
	{
		//Effect
		ent.getWorld().playEffect(ent.getLocation(), Effect.STEP_SOUND, Material.MELON_BLOCK);

		for (int i = 0; i < 10; i++)
		{
			Item item = ent.getWorld().dropItem(ent.getLocation(), ItemStackFactory.Instance.CreateStack(Material.MELON));
			item.setVelocity(new Vector(UtilMath.rr(0.5, true), UtilMath.rr(0.5, false), UtilMath.rr(0.5, true)));
			item.setPickupDelay(30);

			_melon.add(item);
		}

		//Remove
		ent.remove();
	}

	@EventHandler
	public void pickupMelon(PlayerPickupItemEvent event)
	{
		if (!_melon.remove(event.getItem()))
			return;

		UtilServer.CallEvent(new PlayerConsumeMelonEvent(event.getPlayer()));

		event.getItem().remove();

		event.setCancelled(true);

		event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.EAT, 1f, 1f);

		if (!event.getPlayer().hasPotionEffect(PotionEffectType.SPEED))
			event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 1), true);
	}

	@EventHandler
	public void cleanupMelon(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
			return;

		for (Iterator<Item> melonIterator = _melon.iterator(); melonIterator.hasNext(); )
		{
			Item melon = melonIterator.next();

			if (melon.isDead() || !melon.isValid() || melon.getTicksLived() > 100)
			{
				melonIterator.remove();
				melon.remove();
			}
		}

		while (_melon.size() > 60)
		{
			Item item = _melon.remove(0);
			item.remove();
		}
	}
}