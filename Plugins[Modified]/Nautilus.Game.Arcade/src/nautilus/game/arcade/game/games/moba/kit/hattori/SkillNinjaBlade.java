package nautilus.game.arcade.game.games.moba.kit.hattori;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import nautilus.game.arcade.game.games.moba.kit.common.SkillSword;
import nautilus.game.arcade.kit.Perk;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkillNinjaBlade extends HeroSkill
{

	private static final String[] DESCRIPTION = {
			"Turns into a Diamond Sword that deals extreme",
			"damage to any player hit by it."
	};
	private static final ItemStack SKILL_ITEM = new ItemStack(Material.NETHER_STAR);
	private static final ItemStack ACTIVE_ITEM = new ItemBuilder(Material.DIAMOND_SWORD)
			.setTitle(C.cGreenB + "ENDER BLADE")
			.setUnbreakable(true)
			.build();
	private static final int ACTIVE_SLOT = 0;
	private static final int BASE_DAMAGE_INCREASE = 4;

	private Map<UUID, Integer> _active = new HashMap<>();

	public SkillNinjaBlade(int slot)
	{
		super("Ender Blade", DESCRIPTION, SKILL_ITEM, slot, ActionType.ANY);

		setCooldown(60000);
		setDropItemActivate(true);
	}

	@Override
	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (!isSkillItem(event) || _active.containsKey(event.getPlayer().getUniqueId()))
		{
			return;
		}

		Player player = event.getPlayer();
		PlayerInventory inventory = player.getInventory();

		inventory.setItem(ACTIVE_SLOT, ACTIVE_ITEM);
		inventory.setHeldItemSlot(ACTIVE_SLOT);

		int damage = BASE_DAMAGE_INCREASE;
		ItemStack sword = inventory.getItem(ACTIVE_SLOT);

		if (sword == null)
		{
			return;
		}

		Material material = sword.getType();

		// Increase damage based on the sword they had previously
		switch (material)
		{
			case WOOD_SWORD:
				damage += 2;
				break;
			case STONE_SWORD:
				damage += 3;
				break;
			case GOLD_SWORD:
			case IRON_SWORD:
				damage += 4;
				break;
			case DIAMOND_SWORD:
				damage += 5;
				break;
		}

		_active.put(player.getUniqueId(), damage);

		int i = 0;
		for (ItemStack itemStack : inventory.getContents())
		{
			if (itemStack != null && !itemStack.equals(ACTIVE_ITEM) && UtilItem.isSword(itemStack))
			{
				inventory.setItem(i, null);
			}

			i++;
		}

		broadcast(player);
		useActiveSkill(() ->
		{
			_active.remove(player.getUniqueId());

			for (Perk perk : Kit.GetPerks())
			{
				if (perk instanceof SkillSword)
				{
					((SkillSword) perk).giveItem(player);
				}
			}

		}, player, 7000);
	}

	@EventHandler
	public void damage(CustomDamageEvent event)
	{
		Entity entity = event.GetDamageeEntity();
		Player player = event.GetDamagerPlayer(false);
		Player damageePlayer = event.GetDamageePlayer();

		if (player == null || damageePlayer == null || isTeamDamage(damageePlayer, player))
		{
			return;
		}

		ItemStack itemStack = player.getItemInHand();

		if (!_active.containsKey(player.getUniqueId()) || itemStack == null || itemStack.getType() != Material.DIAMOND_SWORD || itemStack.getItemMeta() == null || !itemStack.getItemMeta().getDisplayName().equals(ACTIVE_ITEM.getItemMeta().getDisplayName()))
		{
			return;
		}

		UtilParticle.PlayParticleToAll(ParticleType.HAPPY_VILLAGER, entity.getLocation().add(0, 1, 0), 1F, 1F, 1F, 0.1F, 50, ViewDist.LONG);
		entity.getWorld().playSound(entity.getLocation(), Sound.EXPLODE, 2, 0.5F);
		event.AddMod(GetName(), _active.get(player.getUniqueId()));
	}
}

