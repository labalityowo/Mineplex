package nautilus.game.arcade.kit.perks;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.kit.Perk;

public class PerkWitherMinion extends Perk
{

	private final Map<Skeleton, Player> _minions = new HashMap<>();

	public PerkWitherMinion()
	{
		super("Wither Minions", new String[]
				{
						C.cYellow + "Left-Click" + C.cGray + " with Diamond Sword to use " + C.cGreen + "Wither Minions"
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

		if (!UtilGear.isMat(event.getPlayer().getItemInHand(), Material.DIAMOND_SWORD))
			return;

		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;

		if (!Recharge.Instance.use(player, GetName(), 10000, true, true))
			return;

		event.setCancelled(true);

		Manager.GetGame().CreatureAllowOverride = true;

		for (int i = 0; i < 2; i++)
		{
			Skeleton skel = player.getWorld().spawn(player.getEyeLocation(), Skeleton.class);
			_minions.put(skel, player);

			skel.getEquipment().setHelmet(ItemStackFactory.Instance.CreateStack(Material.SKULL_ITEM, (byte) 1, 1));

			ItemStack armor = new ItemStack(Material.LEATHER_CHESTPLATE);
			LeatherArmorMeta meta = (LeatherArmorMeta) armor.getItemMeta();
			meta.setColor(Color.BLACK);
			armor.setItemMeta(meta);
			skel.getEquipment().setChestplate(armor);

			Manager.GetCondition().Factory().Invisible("Skeleton", skel, skel, 9999, 0, false, false, false);
			Manager.GetCondition().Factory().Speed("Skeleton", skel, skel, 9999, 0, false, false, false);

			Vector random = new Vector(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5);
			random.normalize();
			random.multiply(0.1);

			UtilAction.velocity(skel, player.getLocation().getDirection().add(random), 1 + Math.random() * 0.4, false, 0, 0.2, 10, false);
		}

		Manager.GetGame().CreatureAllowOverride = false;

		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.WITHER_HURT, 2f, 0.6f);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void minionDamage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}

		LivingEntity damagee = event.GetDamageeEntity(), damager = event.GetDamagerEntity(true);

		if (event.GetCause() == DamageCause.FALL && _minions.containsKey(damagee))
		{
			event.SetCancelled("Minion Fall Damage");
		}
		else if (_minions.containsKey(damager))
		{
			event.SetDamager(_minions.get(damager));
		}
	}

	@EventHandler
	public void entityTarget(EntityTargetEvent event)
	{
		if (getWitherTeam() == null)
			return;

		if (getWitherTeam().GetPlayers(true).contains(event.getTarget()))
			event.setCancelled(true);
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		_minions.keySet().removeIf(skel ->
		{
			if (!skel.isValid() || skel.getTicksLived() > 300 || skel.getLocation().getY() < 0)
			{
				skel.remove();
				return true;
			}

			if (skel.getTarget() == null)
			{
				skel.setTarget(UtilPlayer.getClosest(skel.getLocation(), getWitherTeam().GetPlayers(true)));
			}

			return false;
		});
	}

	private GameTeam getWitherTeam()
	{
		if (Manager.GetGame() == null)
			return null;

		return Manager.GetGame().GetTeam(ChatColor.RED);
	}
}
