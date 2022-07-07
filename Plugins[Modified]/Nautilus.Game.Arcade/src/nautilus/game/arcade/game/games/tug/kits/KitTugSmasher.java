package nautilus.game.arcade.game.games.tug.kits;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.F;
import mineplex.core.common.util.SpigotUtil;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilItem;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkMammoth;

public class KitTugSmasher extends Kit
{

	private static final ItemStack[] PLAYER_ITEMS =
			{
					new ItemBuilder(Material.STONE_SWORD)
							.setUnbreakable(true)
							.build()
			};

	private static final ItemStack[] PLAYER_ARMOUR =
			{
					new ItemBuilder(Material.IRON_BOOTS)
							.setUnbreakable(true)
							.build(),
					new ItemBuilder(Material.IRON_LEGGINGS)
							.setUnbreakable(true)
							.build(),
					new ItemBuilder(Material.IRON_CHESTPLATE)
							.setUnbreakable(true)
							.build(),
					new ItemBuilder(Material.IRON_HELMET)
							.setUnbreakable(true)
							.build()
			};

	private static final Perk[] PERKS =
			{
					new PerkMammoth()
			};

	private static final String WOLF_NAME = "Spawn Dog";
	private static final long WOLF_COOLDOWN = TimeUnit.SECONDS.toMillis(20);

	private final Map<LivingEntity, Player> _petOwners = new HashMap<>();

	public KitTugSmasher(ArcadeManager manager)
	{
		super(manager, GameKit.TUG_SMASHER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
		player.getInventory().setArmorContents(PLAYER_ARMOUR);
	}

	@EventHandler
	public void playerDropItem(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();

		if (!HasKit(player) || !UtilItem.isSword(event.getItemDrop().getItemStack()) || _petOwners.containsValue(player) || !Recharge.Instance.usable(player, WOLF_NAME, true))
		{
			return;
		}

		Manager.GetGame().CreatureAllowOverride = true;

		GameTeam team = Manager.GetGame().GetTeam(player);

		Wolf wolf = player.getWorld().spawn(player.getLocation(), Wolf.class);
		wolf.setCollarColor(team.getDyeColor());
		wolf.setCustomName(team.GetColor() + player.getName() + "'s Dog");
		wolf.setCustomNameVisible(true);
		wolf.setMaxHealth(25);
		wolf.setHealth(25);

		UtilEnt.vegetate(wolf);
		SpigotUtil.setOldOwner_RemoveMeWhenSpigotFixesThis(wolf, player);
		wolf.setOwner(player);
		player.sendMessage(F.main(Manager.getName(), "You spawned your " + F.name("Pet Dog") + "."));

		_petOwners.put(wolf, player);

		Manager.GetGame().CreatureAllowOverride = false;
	}

	@EventHandler
	public void updatePets(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
		{
			return;
		}

		_petOwners.forEach((wolf, player) ->
		{
			for (LivingEntity entity : UtilEnt.getInRadius(wolf.getLocation(), 6).keySet())
			{
				if (entity instanceof Sheep)
				{
					entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 1, false, false));
				}
			}

			UtilEnt.CreatureMove(wolf, player.getLocation(), 1);
		});
	}

	@EventHandler
	public void entityDeath(EntityDeathEvent event)
	{
		Player player = _petOwners.remove(event.getEntity());

		if (player != null)
		{
			Recharge.Instance.useForce(player, WOLF_NAME, WOLF_COOLDOWN, true);
		}
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		_petOwners.entrySet().removeIf(entry ->
		{
			if (event.getEntity().equals(entry.getValue()))
			{
				entry.getKey().remove();
				return true;
			}

			return false;
		});
	}

	@Override
	public void unregisterEvents()
	{
		_petOwners.clear();
	}
}
