package nautilus.game.arcade.game.games.moba.kit.hattori;

import mineplex.core.common.util.*;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.particles.effects.LineParticle;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import nautilus.game.arcade.game.games.moba.kit.common.DashSkill;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SkillNinjaDash extends DashSkill
{

	private static final String[] DESCRIPTION = {
			"Dash forward dealing damage to any enemy",
			"you collide with."
	};

	private static final ItemStack SKILL_ITEM = new ItemStack(Material.FEATHER);

	public SkillNinjaDash(int slot)
	{
		super("Ninja Dash", DESCRIPTION, SKILL_ITEM, slot);

		setCooldown(7000);

		_teleport = true;
		_range = 10;
	}

	@Override
	public void preDash(Player player)
	{
		playFirework(player);
	}

	@Override
	public void collideEntity(LivingEntity entity, Player player, double scale, boolean sameTeam)
	{
		Manager.GetDamage().NewDamageEvent(entity, player, null, DamageCause.CUSTOM, 8, true, true, false, UtilEnt.getName(player), GetName());
	}

	@Override
	public void postDash(Player player)
	{
		playFirework(player);
	}

	private void playFirework(Player player)
	{
		GameTeam team = Manager.GetGame().GetTeam(player);
		FireworkEffect effect = FireworkEffect.builder().with(Type.BALL).withColor(team.GetColorBase()).withFlicker().build();
		UtilFirework.playFirework(player.getLocation(), effect);
	}
}
