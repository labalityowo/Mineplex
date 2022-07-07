package nautilus.game.arcade.game.games.smash.perks.villager;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.game.games.smash.kits.KitVillager;
import nautilus.game.arcade.game.games.smash.kits.KitVillager.VillagerType;
import nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkVillagerShot extends SmashPerk implements IThrown
{

	private long _cooldown;
	private float _bulletVelocity;
	private float _bulletKnockback;
	private int _bullets;
	private float _hitBox;

	private double _normalRecoil, _attackRecoil, _defenseRecoil, _speedRecoil;
	private double _normalSpread, _attackSpread, _defenseSpread, _speedSpread;
	private double _normalDamage, _attackDamage, _defenseDamage, _speedDamage;

	private final Map<Item, VillagerType> _items;

	public PerkVillagerShot()
	{
		super("Trade Scatter", new String[]
				{
						C.cYellow + "Right-Click" + C.cGray + " Hoe to use " + C.cGreen + "Trade Scatter",
				});

		_items = new HashMap<>();
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_bulletVelocity = getPerkFloat("Bullet Velocity");
		_bulletKnockback = getPerkFloat("Bullet Knockback");
		_bullets = getPerkInt("Bullets");
		_hitBox = getPerkFloat("Hitbox");

		_normalRecoil = getPerkDouble("Normal.Recoil");
		_attackRecoil = getPerkDouble("Attack.Recoil");
		_defenseRecoil = getPerkDouble("Defense.Recoil");
		_speedRecoil = getPerkDouble("Speed.Recoil");

		_normalSpread = getPerkDouble("Normal.Spread");
		_attackSpread = getPerkDouble("Attack.Spread");
		_defenseSpread = getPerkDouble("Defense.Spread");
		_speedSpread = getPerkDouble("Speed.Spread");

		_normalDamage = getPerkDouble("Normal.Damage");
		_attackDamage = getPerkDouble("Attack.Damage");
		_defenseDamage = getPerkDouble("Defense.Damage");
		_speedDamage = getPerkDouble("Speed.Damage");
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerInteract(PlayerInteractEvent event)
	{
		if (event.isCancelled() || !UtilEvent.isAction(event, ActionType.R) || UtilBlock.usable(event.getClickedBlock()))
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (!UtilItem.isHoe(itemStack) || !hasPerk(player) || !Recharge.Instance.use(player, GetName(), _cooldown, true, true))
		{
			return;
		}

		KitVillager kit = ((KitVillager) Kit);
		VillagerType type = kit.getActiveArt(player);
		Material material = Material.EMERALD;
		ParticleType particleType = ParticleType.HAPPY_VILLAGER;
		double recoil = _normalRecoil, spread = _normalSpread;
		boolean front = type != null && (type == VillagerType.ATTACK || type == VillagerType.DEFENSE);

		if (isSuperActive(player))
		{
			material = Material.NETHER_STAR;
			particleType = ParticleType.ANGRY_VILLAGER;
			recoil = _speedRecoil;
			spread = _attackSpread;
			type = VillagerType.ATTACK;
			front = true;
		}
		else if (type != null)
		{
			switch (type)
			{
				case ATTACK:
					material = Material.RAW_BEEF;
					particleType = ParticleType.FLAME;
					recoil = _attackRecoil;
					spread = _attackSpread;
					break;
				case DEFENSE:
					material = Material.IRON_INGOT;
					particleType = ParticleType.FIREWORKS_SPARK;
					recoil = _defenseRecoil;
					spread = _defenseSpread;
					break;
				case SPEED:
					material = Material.FEATHER;
					particleType = ParticleType.ENCHANTMENT_TABLE;
					recoil = _speedRecoil;
					spread = _speedSpread;
					break;
			}
		}

		Location location;
		Vector direction;

		if (front)
		{
			location = player.getEyeLocation();
			direction = location.getDirection();
			recoil = -recoil;
		}
		else
		{
			location = player.getLocation().add(0, 1.3, 0);
			direction = location.getDirection().multiply(-_bulletVelocity);
		}

		UtilAction.velocity(player, recoil, 0.3, 1.2, true);
		player.getWorld().playSound(location, Sound.FIREWORK_LARGE_BLAST, 1.5F, 1);

		for (int i = 0; i < _bullets; i++)
		{
			boolean reduceParticles = type != null && type == VillagerType.ATTACK;
			Item item = location.getWorld().dropItem(location, new ItemBuilder(material).setTitle("Bullet" + UtilMath.r(100)).build());
			Vector itemDirection = direction.clone().add(new Vector((Math.random() - 0.5) * spread, (Math.random() - 0.5) * spread, (Math.random() - 0.5) * spread));
			item.setVelocity(itemDirection);
			_items.put(item, type);
			Manager.GetProjectile().AddThrow(item, player, this, 5000, true, true, false, true, null, 0, 0, particleType, reduceParticles ? UpdateType.FASTEST : UpdateType.TICK, _hitBox);
		}

		player.sendMessage(F.main("Game", "You used " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void damage(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
		{
			return;
		}

		event.AddKnockback(GetName(), _bulletKnockback);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		VillagerType type = remove(data);
		double damage = _normalDamage;

		if (target == null)
		{
			return;
		}

		if (type != null)
		{
			switch (type)
			{
				case ATTACK:
					damage = _attackDamage;
					break;
				case DEFENSE:
					damage = _defenseDamage;
					break;
				case SPEED:
					damage = _speedDamage;
					break;
			}
		}

		Manager.GetDamage().NewDamageEvent(target, data.getThrower(), null, DamageCause.CUSTOM, damage, true, true, false, data.getThrower().getName(), GetName());
	}

	@Override
	public void Idle(ProjectileUser data)
	{
		remove(data);
	}

	@Override
	public void Expire(ProjectileUser data)
	{
		remove(data);
	}

	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		remove(data);
	}

	private VillagerType remove(ProjectileUser data)
	{
		data.getThrown().remove();
		return _items.remove(data.getThrown());
	}
}
