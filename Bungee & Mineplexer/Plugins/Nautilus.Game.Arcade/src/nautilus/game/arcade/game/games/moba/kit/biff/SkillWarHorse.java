package nautilus.game.arcade.game.games.moba.kit.biff;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.buff.BuffManager;
import nautilus.game.arcade.game.games.moba.buff.buffs.BuffRooting;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import nautilus.game.arcade.game.games.moba.util.MobaUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SkillWarHorse extends HeroSkill
{

	private static final String[] DESCRIPTION = {
			"Mounts you on a horse.",
			"Any nearby enemy heroes are rooted and will be",
			"unable to move."
	};

	private static final ItemStack SKILL_ITEM = new ItemStack(Material.NETHER_STAR);
	private static final ItemStack HORSE_ARMOUR = new ItemStack(Material.IRON_BARDING);
	private static final ItemStack SADDLE = new ItemStack(Material.SADDLE);

	private final Set<WarHorseData> _data = new HashSet<>();

	public SkillWarHorse(int slot)
	{
		super("Cavalry Charge", DESCRIPTION, SKILL_ITEM, slot, ActionType.ANY);

		setCooldown(45000);
		setDropItemActivate(true);
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (!isSkillItem(event))
		{
			return;
		}

		Player player = event.getPlayer();

		for (WarHorseData data : _data)
		{
			if (data.Owner.equals(player))
			{
				return;
			}
		}

		Manager.GetGame().CreatureAllowOverride = true;

		Horse horse = player.getWorld().spawn(player.getLocation(), Horse.class);

		UtilParticle.PlayParticleToAll(ParticleType.CLOUD, horse.getLocation().add(0, 1, 0), 1, 1, 1, 0.1F, 50, ViewDist.LONG);
		horse.getWorld().strikeLightningEffect(horse.getLocation());
		horse.getWorld().playSound(horse.getLocation(), Sound.HORSE_DEATH, 1, 1.1F);
		horse.setHealth(20);
		horse.setMaxHealth(horse.getHealth());
		horse.setJumpStrength(1);
		horse.setMaxDomestication(1);
		horse.setDomestication(horse.getMaxDomestication());
		horse.getInventory().setArmor(HORSE_ARMOUR);
		horse.getInventory().setSaddle(SADDLE);
		horse.setOwner(player);
		horse.setPassenger(player);
		MobaUtil.setTeamEntity(horse, Manager.GetGame().GetTeam(player));

		Manager.GetGame().CreatureAllowOverride = false;

		_data.add(new WarHorseData(player, horse));

		broadcast(player);
		useActiveSkill(player, 5500);
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}

		Iterator<WarHorseData> iterator = _data.iterator();

		while (iterator.hasNext())
		{
			WarHorseData data = iterator.next();
			Player owner = data.Owner;
			Horse horse = data.Horse;

			if (UtilTime.elapsed(data.Start, 6000) || UtilPlayer.isSpectator(owner) || horse.isDead() || !horse.isValid())
			{
				horse.getWorld().playSound(horse.getLocation(), Sound.HORSE_BREATHE, 1, 1.1F);
				UtilParticle.PlayParticleToAll(ParticleType.CLOUD, horse.getLocation().add(0, 1, 0), 0.5F, 0.5F, 0.5F, 0.1F, 50, ViewDist.LONG);
				horse.remove();
				iterator.remove();
			}
			else
			{
				Moba game = (Moba) Manager.GetGame();
				BuffManager buffManager = game.getBuffManager();

				for (Player player : UtilPlayer.getNearby(horse.getLocation(), 5))
				{
					if (isTeamDamage(owner, player) || !Recharge.Instance.use(player, GetName() + "Rooting", 2000, false, false))
					{
						continue;
					}

					owner.sendMessage(F.main("Game", "You hit " + F.name(player.getName()) + "."));
					Manager.GetDamage().NewDamageEvent(player, owner, null, DamageCause.CUSTOM, 4, false, true, false, UtilEnt.getName(owner), GetName());
					buffManager.apply(new BuffRooting(game, player, 1000));
				}
			}
		}
	}

	@EventHandler
	public void horseDamage(CustomDamageEvent event)
	{
		for (WarHorseData data : _data)
		{
			if (data.Horse.equals(event.GetDamageeEntity()))
			{
				event.SetCancelled("Biff Horse");
				return;
			}
		}

	}

	private class WarHorseData
	{
		public Player Owner;
		public Horse Horse;
		public long Start;

		WarHorseData(Player owner, Horse horse)
		{
			Owner = owner;
			Horse = horse;
			Start = System.currentTimeMillis();
		}
	}
}
