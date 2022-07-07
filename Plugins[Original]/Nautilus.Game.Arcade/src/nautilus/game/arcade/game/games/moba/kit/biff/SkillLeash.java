package nautilus.game.arcade.game.games.moba.kit.biff;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import nautilus.game.arcade.game.games.moba.kit.common.LeashedEntity;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SkillLeash extends HeroSkill
{

	private static final String[] DESCRIPTION = {
			"Enemy heroes near to Biff are hooked with a leash to you.",
			"Leashed players are slowed and it breaks if the leashed player moves",
			"too far away."
	};

	private static final ItemStack SKILL_ITEM = new ItemStack(Material.LEASH);

	private final Map<Player, List<LeashedEntity>> _leashed = new HashMap<>();

	public SkillLeash(int slot)
	{
		super("Tether", DESCRIPTION, SKILL_ITEM, slot, ActionType.ANY);
		setCooldown(12000);
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!isSkillItem(event) || _leashed.containsKey(player))
		{
			return;
		}

		List<Player> nearbyPlayers = UtilPlayer.getNearby(player.getLocation(), 8);
		nearbyPlayers.removeIf(other -> isTeamDamage(other, player));

		if (nearbyPlayers.isEmpty())
		{
			player.sendMessage(F.main("Game", "There was no one in range to leash."));
			return;
		}

		List<LeashedEntity> leashedEntities = new ArrayList<>(nearbyPlayers.size());

		StringBuilder builder = new StringBuilder(F.main("Game", "You leashed "));

		for (Player nearby : nearbyPlayers)
		{
			nearby.setLeashHolder(player);
			nearby.setPullWhileLeashed(false);
			nearby.setShouldBreakLeash(false);
			nearby.sendMessage(F.main("Game", F.name(player.getName()) + " leashed you."));
			nearby.playSound(nearby.getLocation(), Sound.DOOR_CLOSE, 1, 1);
			builder.append(F.name(nearby.getName())).append(", ");
			UtilAction.zeroVelocity(nearby);
			Manager.GetCondition().Factory().Slow(GetName(), nearby, player, 5, 1, false, true, true, false);
			leashedEntities.add(new LeashedEntity(Manager, nearby, player));
		}

		_leashed.put(player, leashedEntities);

		player.playSound(player.getLocation(), Sound.DOOR_CLOSE, 1, 1);
		player.sendMessage(builder.toString());

		useActiveSkill(() ->
		{
			for (LeashedEntity leashed : _leashed.remove(player))
			{
				removeEffect(leashed);
			}

		}, player, 5000);
	}

	@EventHandler
	public void updateLeashed(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		for (Entry<Player, List<LeashedEntity>> entry : _leashed.entrySet())
		{
			Iterator<LeashedEntity> iterator = entry.getValue().iterator();

			while (iterator.hasNext())
			{
				LeashedEntity leashed = iterator.next();

				if (!UtilPlayer.isSpectator(entry.getKey()) && UtilMath.offsetSquared(entry.getKey(), leashed.getHost()) < 100)
				{
					continue;
				}

				removeEffect(leashed);
				iterator.remove();
			}
		}
	}

	private void removeEffect(LeashedEntity entity)
	{
		entity.getHost().removePotionEffect(PotionEffectType.SLOW);
		entity.remove();
	}
}
