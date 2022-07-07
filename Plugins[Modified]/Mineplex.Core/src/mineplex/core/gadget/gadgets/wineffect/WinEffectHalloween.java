package mineplex.core.gadget.gadgets.wineffect;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import mineplex.core.Managers;
import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.common.util.UtilSkull;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.WinEffectGadget;
import mineplex.core.particleeffects.BabyFireworkEffect;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.utils.UtilVariant;
import mineplex.core.visibility.VisibilityManager;

public class WinEffectHalloween extends WinEffectGadget
{

	private DisguisePlayer _npc;
	private List<DisguisePlayer> _disguisePlayers = new ArrayList<>();
	private int _tick;

	public WinEffectHalloween(GadgetManager manager)
	{
		super(manager, "Return of The Pumpkin", UtilText.splitLineToArray(C.cGray + "The Pumpkin King will rise again...", LineFormat.LORE),
				-9, Material.PUMPKIN, (byte) 0);
		_schematicName = "HalloweenRoom";
		setGameTime(16000);
	}

	@Override
	public void setup(Player player, List<Player> team, List<Player> nonTeam, Location loc)
	{
		Location fixedLoc = loc.setDirection(loc.getDirection().multiply(-1));
		super.setup(player, team, nonTeam, fixedLoc);
	}

	@Override
	public void teleport()
	{
		Location loc = getBaseLocation().add(getBaseLocation().getDirection().normalize().multiply(17)).add(0, 3, 0);
		loc.setDirection(getBaseLocation().clone().subtract(loc).toVector());
		super.teleport(loc);
	}

	@Override
	public void play()
	{
		spawnNPC();
		_tick = 0;
	}

	@Override
	public void finish()
	{
		VisibilityManager vm = Managers.require(VisibilityManager.class);
		Bukkit.getOnlinePlayers().forEach(pl ->
		{
			vm.showPlayer(pl, _player, "Halloween Win Effect");
			_team.forEach(p -> vm.showPlayer(pl, p, "Halloween Win Effect"));
			_nonTeam.forEach(p -> vm.showPlayer(pl, p, "Halloween Win Effect"));
		});
		_disguisePlayers.forEach(d -> d.getEntity().getBukkitEntity().remove());
		_disguisePlayers.clear();
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (!isRunning())
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		_tick++;

		if (_tick == 50)
		{
			transformPlayer();
		}

		if (_tick > 50 && _tick <= 60)
		{
			int particles = 50;
			double radius = 0.5;

			for (int i = 0; i < particles; i++)
			{
				double angle = (double) 2 * Math.PI * i / particles;
				double x = Math.cos(angle) * radius;
				double z = Math.sin(angle) * radius;
				Location loc = getBaseLocation().clone().add(x, .2 * (_tick - 50), z);
				ColoredParticle coloredParticle = new ColoredParticle(UtilParticle.ParticleType.RED_DUST, new DustSpellColor((i % 2 == 0) ? Color.ORANGE : Color.BLACK), loc);
				coloredParticle.display();
			}
		}
	}

	private void spawnNPC()
	{
		Location loc = getBaseLocation().setDirection(getBaseLocation().getDirection());
		_npc = getNPC(_player, loc);
	}

	private void transformPlayer()
	{
		_npc.getEntity().getBukkitEntity().remove();
		spawnSkeleton();
		spawnGhosts();
		VisibilityManager vm = Managers.require(VisibilityManager.class);
		Bukkit.getOnlinePlayers().forEach(pl -> vm.hidePlayer(pl, _player, "Halloween Win Effect"));
		_player.getWorld().playSound(getBaseLocation(), Sound.CAT_MEOW, .5f, .5f);
	}

	private void spawnSkeleton()
	{
		Skeleton skeleton = UtilVariant.spawnWitherSkeleton(getBaseLocation());
		skeleton.setCustomName(getRank(_player) + _player.getName());
		skeleton.setCustomNameVisible(true);
		skeleton.getEquipment().setHelmet(new ItemStack(Material.JACK_O_LANTERN));
		UtilEnt.ghost(skeleton, true, false);
		UtilEnt.vegetate(skeleton);
		for (int i = 0; i < 15; i++)
		{
			playFirework(skeleton.getLocation().clone().add(0, 2, 0), i, true);
		}
	}

	private void spawnGhosts()
	{
		int i = 0;
		List<Location> circle = UtilShapes.getPointsInCircle(getBaseLocation(), _nonTeam.size(), 7);
		VisibilityManager vm = Managers.require(VisibilityManager.class);
		for (Player player : _nonTeam)
		{
			ItemStack playerHead = UtilSkull.getPlayerHead(player.getName(), player.getName() + " skull", null);
			Location ghostLoc = circle.get(i);
			// Makes sure the block under the zombie is not air
			while (ghostLoc.clone().subtract(0, 1, 0).getBlock().getType() == Material.AIR)
			{
				ghostLoc.subtract(0, 1, 0);
			}
			DisguisePlayer disguisePlayer = getNPC(player, ghostLoc, SkinData.GHOST);
			disguisePlayer.setHelmet(playerHead);
			UtilEnt.CreatureLook(disguisePlayer.getEntity().getBukkitEntity(), getBaseLocation());
			Bukkit.getOnlinePlayers().forEach(pl -> vm.hidePlayer(pl, player, "Halloween Win Effect"));
			for (int j = 0; j < 5; j++)
			{
				playFirework(ghostLoc.clone().add(0, 1, 0), j, false);
			}
			i++;
		}
	}

	private void playFirework(Location entityLocation, int i, boolean randomLoc)
	{
		Location randLocation = UtilAlg.getRandomLocation(entityLocation.clone(), 0.7d, 0.7d, 0d);
		Color color = (i == 0) ? Color.ORANGE : Color.BLACK;
		BabyFireworkEffect babyFireworkEffect;
		if (i %2 == 0)
		{
			babyFireworkEffect = new BabyFireworkEffect((randomLoc) ? randLocation : entityLocation, color);
		}
		else
		{
			babyFireworkEffect = new BabyFireworkEffect((randomLoc) ? randLocation : entityLocation, Color.ORANGE, Color.BLACK);
		}
		// Starts without the firework trail
		babyFireworkEffect.setCount(6);
		babyFireworkEffect.start();
	}
}