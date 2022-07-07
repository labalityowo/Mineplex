package nautilus.game.arcade.kit.perks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.kit.Perk;

public class PerkWitherWeb extends Perk implements IThrown
{
	public PerkWitherWeb() 
	{
		super("Web Blast", new String[] 
				{
				C.cYellow + "Left-Click" + C.cGray + " with Iron Sword to use " + C.cGreen + "Web Blast"
				});
	}

	@EventHandler
	public void ShootWeb(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;

		if (!UtilEvent.isAction(event, ActionType.L))
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (!UtilGear.isMat(event.getPlayer().getItemInHand(), Material.IRON_SWORD))
			return;

		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;

		if (!Recharge.Instance.use(player, GetName(), 6000, true, true))
			return;

		event.setCancelled(true);

		for (int i=0 ; i<40 ; i++)
		{
			org.bukkit.entity.Item ent = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()), ItemStackFactory.Instance.CreateStack(Material.WEB));
			
			Vector random = new Vector(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5);
			random.normalize();
			random.multiply(0.25);
			
			UtilAction.velocity(ent, player.getLocation().getDirection().add(random), 1 + Math.random() * 0.4, false, 0, 0.2, 10, false);	
			Manager.GetProjectile().AddThrow(ent, player, this, -1, true, true, true, false, 0.5f);
		}

		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.WITHER_HURT, 2f, 0.6f);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data) 
	{
		Web(data);
	}

	@Override
	public void Idle(ProjectileUser data) 
	{
		Web(data);
	}

	@Override
	public void Expire(ProjectileUser data) 
	{
		Web(data);
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}

	public void Web(ProjectileUser data)
	{
		Location loc = data.getThrown().getLocation();
		data.getThrown().remove();

		if (data.getThrown().getTicksLived() > 3)
			Manager.GetBlockRestore().add(loc.getBlock(), 30, (byte) 0, 3000);
	}
}