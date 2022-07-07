package mineplex.core.gadget.gadgets.item;

import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.Ammo;
import mineplex.core.gadget.types.ItemGadget;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.mission.MissionTrackerType;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.updater.UpdateType;

public class ItemFleshHook extends ItemGadget implements IThrown
{
	public ItemFleshHook(GadgetManager manager)
	{
		super(manager, "Flesh Hook", 
				UtilText.splitLineToArray(C.cWhite + "Make new friends by throwing a hook into their face and pulling them towards you!", LineFormat.LORE), 
				-1,  
				Material.getMaterial(131), (byte)0, 
				2000, new Ammo("Flesh Hook", "50 Flesh Hooks", Material.getMaterial(131), (byte)0, new String[] { C.cWhite + "50 Flesh Hooks for you to use!" }, 1000, 50));
	}

	@Override
	public void ActivateCustom(Player player)
	{
		//Action
		Item item = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()), ItemStackFactory.Instance.CreateStack(131));
		UtilAction.velocity(item, player.getLocation().getDirection(), 
				1.6, false, 0, 0.2, 10, false);

		Manager.getProjectileManager().AddThrow(item, player, this, -1, true, true, true, true, 
				Sound.FIRE_IGNITE, 1.4f, 0.8f, ParticleType.CRIT, null, 0, UpdateType.TICK, 0.5f);

		//Inform
		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(getName()) + "."));

		//Effect
		item.getWorld().playSound(item.getLocation(), Sound.IRONGOLEM_THROW, 2f, 0.8f);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		data.getThrown().remove();

		if (!(data.getThrower() instanceof Player))
			return;

		Player player = (Player)data.getThrower();

		if (target == null || !Manager.selectEntity(this, target))
		{
			return;
		}

		if (Manager.getCastleManager().isInsideCastle(target.getLocation()))
			return;

		//Pull
		UtilAction.velocity(target, 
				UtilAlg.getTrajectory(target.getLocation(), player.getLocation()), 
				3, false, 0, 0.8, 1.5, true);

		//Effect
		target.playEffect(EntityEffect.HURT);

		//Inform
		UtilPlayer.message(target, F.main("Skill", F.name(player.getName()) + " hit you with " + F.skill(getName()) + "."));

		Manager.getMissionManager().incrementProgress(player, 1, MissionTrackerType.LOBBY_FLESH_HOOK, null, null);
	}

	@Override
	public void Idle(ProjectileUser data)
	{
		data.getThrown().remove();
	}

	@Override
	public void Expire(ProjectileUser data)
	{
		data.getThrown().remove();
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}
}