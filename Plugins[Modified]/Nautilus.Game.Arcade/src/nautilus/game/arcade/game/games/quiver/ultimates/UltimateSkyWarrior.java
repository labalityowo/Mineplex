package nautilus.game.arcade.game.games.quiver.ultimates;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.particles.effects.LineParticle;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.quiver.Quiver;

public class UltimateSkyWarrior extends UltimatePerk
{

	private static final float CHARGE_PASSIVE = 0.4F;
	private static final float CHARGE_PAYLOAD = 0.4F;
	private static final float CHARGE_KILL = 5F;
	private static final float CHARGE_ASSIST = 2F;
	
	private static final int SHOTS = 3;
	private static final long LAUNCHER_FIRE_DELAY = 500;
	private static final long LAUNCHER_MAX_TIME = 15000;
	private static final int Y_INCREASE = 10;

	private double _damageTeleport;
	private double _radiusTeleport;
	private double _damageLauncher;
	private double _radiusLauncher;
	private int _rangeLauncher;

	private List<SkyWarriorData> _data = new ArrayList<>();

	public UltimateSkyWarrior(double damageTeleport, double radiusTeleport, double damageLauncher, double radiusLauncher, int rangeLauncher)
	{
		super("Bombardment", new String[] {}, 0, CHARGE_PASSIVE, CHARGE_PAYLOAD, CHARGE_KILL, CHARGE_ASSIST);

		_damageTeleport = damageTeleport;
		_radiusTeleport = radiusTeleport;
		_damageLauncher = damageLauncher;
		_radiusLauncher = radiusLauncher;
		_rangeLauncher = rangeLauncher;
	}

	@Override
	public void activate(Player player)
	{
		super.activate(player);

		Location playerLocation = player.getLocation();
		// This is to stop the players getting killed by the border if they were
		// teleported above it.
		Location toTeleport = new Location(player.getWorld(), playerLocation.getX(), Math.min(Manager.GetGame().WorldData.MaxY - 3, playerLocation.getY() + Y_INCREASE), playerLocation.getZ());
		
		toTeleport.setYaw(playerLocation.getYaw());
		toTeleport.setPitch(playerLocation.getPitch());
		
		Block block = toTeleport.getBlock().getRelative(BlockFace.DOWN);

		block.setType(Material.BARRIER);
		player.setWalkSpeed(0);
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, -10));
		player.teleport(toTeleport);
		
		_data.add(new SkyWarriorData(player, block, SHOTS, UtilInv.getAmount(player, Material.ARROW), System.currentTimeMillis()));

		player.getInventory().clear();

		ItemStack itemStack = new ItemBuilder(Material.IRON_HOE).setTitle(C.cGreenB + GetName() + C.cDGreenB + " Click To Fire!").build();

		for (int i = 0; i < 9; i++)
		{
			player.getInventory().addItem(itemStack);
		}
		
		Game game = Manager.GetGame();
		
		UtilFirework.playFirework(playerLocation, Type.BALL, game.GetTeam(player).GetColorBase(), false, false);
		UtilFirework.playFirework(toTeleport, Type.BALL, game.GetTeam(player).GetColorBase(), false, false);
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() == UpdateType.TICK)
		{
			for (SkyWarriorData data : _data)
			{			
				UtilParticle.PlayParticleToAll(ParticleType.CLOUD, data.getPlayer().getLocation().subtract(0, 0.5, 0), 0.5F, 0.25F, 0.5F, 0.01F, 6, ViewDist.MAX);
			}	
		}
		else if (event.getType() == UpdateType.FAST)
		{
			for (SkyWarriorData data : _data)
			{			
				if (UtilTime.elapsed(data.getStartTimeStamp(), LAUNCHER_MAX_TIME))
				{
					cancel(data.getPlayer());
				}
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		SkyWarriorData data = getData(player);

		if (data == null)
		{
			return;
		}

		if (player.getItemInHand().getType() != Material.IRON_HOE)
		{
			return;
		}
		
		if (!hasPerk(player))
		{
			return;
		}

		if (!Recharge.Instance.use(player, GetName(), LAUNCHER_FIRE_DELAY, true, false))
		{
			return;
		}

		player.getWorld().playSound(player.getLocation(), Sound.FIREWORK_BLAST, 5, 1);
		
		LineParticle lineParticle = new LineParticle(player.getEyeLocation(), player.getLocation().getDirection(), 0.5, _rangeLauncher, ParticleType.FIREWORKS_SPARK, UtilServer.getPlayers());
		
		while (!lineParticle.update())
		{ 
		}
		
		Location location = lineParticle.getDestination();
		
		// Damage Players
		for (Player other : UtilPlayer.getNearby(location, _radiusLauncher))
		{
			Manager.GetDamage().NewDamageEvent(other, player, null, DamageCause.CUSTOM, _damageLauncher, true, true, false, player.getName(), GetName());
		}
		
		UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, location, 0, 0, 0, 1F, 1, ViewDist.LONG);

		data.setShotsLeft(data.getShotsLeft() - 1);

		if (data.getShotsLeft() == 0)
		{
			cancel(player);
		}
	}  
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event)
	{
		if (!isUsingUltimate(event.getPlayer().getUniqueId()))
		{
			return;
		}
		
		Location from = event.getFrom();
		Location to = event.getTo();
		
		if (from.getX() == to.getX() && from.getZ() == to.getZ())
		{
			return;
		}
		
		event.setTo(from);
	}

	@Override
	public void cancel(Player player)
	{
		super.cancel(player);

		player.getInventory().clear();
		Kit.GiveItems(player);
		
		player.setWalkSpeed(0.2F);
		player.removePotionEffect(PotionEffectType.JUMP);

		SkyWarriorData data = getData(player);
		Game game = Manager.GetGame();

		data.getBlock().setType(Material.AIR);

		for (int i = 0; i < data.getPreviousArrows(); i++)
		{
			player.getInventory().addItem(Quiver.SUPER_ARROW);
		}
		
		boolean found = false;

		for (Player other : game.GetPlayers(true))
		{
			if (UtilPlayer.isSpectator(other) || player.equals(other))
			{
				continue;
			}

			if (game.GetTeam(player).equals(game.GetTeam(other)))
			{
				player.sendMessage(F.main("Game", "You were teleported to " + F.elem(other.getName()) + "."));
				player.teleport(other);
				other.getWorld().strikeLightningEffect(other.getLocation());
				UtilFirework.playFirework(other.getLocation(), Type.STAR, game.GetTeam(player).GetColorBase(), false, false);
				
				for (Player toDamage : UtilPlayer.getNearby(other.getEyeLocation(), _radiusTeleport))
				{
					Manager.GetDamage().NewDamageEvent(toDamage, player, null, DamageCause.CUSTOM, _damageTeleport, false, true, false, player.getName(), GetName() + " Teleportation");
				}

				found = true;
				break;
			}
		}

		if (!found)
		{
			player.sendMessage(F.main("Game", "A player could not be found!"));
		}

		_data.remove(data);
	}
	
	@Override
	public boolean isUsable(Player player)
	{
		for (int i = 2; i <= Y_INCREASE; i++)
		{
			if (player.getLocation().add(0, i, 0).getBlock().getType() != Material.AIR)
			{
				player.sendMessage(F.main("Game", "You do not have enough room to use this!"));;
				return false;
			}
		}
		
		return true;
	}

	private SkyWarriorData getData(Player player)
	{
		for (SkyWarriorData data : _data)
		{
			if (data.getPlayer().equals(player))
			{
				return data;
			}
		}

		return null;
	}

	private final class SkyWarriorData
	{

		private Player _player;
		private Block _block;
		private int _shotsLeft;
		private int _previousArrows;
		private long _startTimeStamp;

		public SkyWarriorData(Player player, Block block, int shots, int previousArrows, long startTimeStamp)
		{
			_player = player;
			_block = block;
			_shotsLeft = shots;
			_previousArrows = previousArrows;
			_startTimeStamp = startTimeStamp;
		}

		public void setShotsLeft(int slotsLeft)
		{
			_shotsLeft = slotsLeft;
		}

		public Player getPlayer()
		{
			return _player;
		}

		public Block getBlock()
		{
			return _block;
		}

		public int getShotsLeft()
		{
			return _shotsLeft;
		}
		
		public int getPreviousArrows()
		{
			return _previousArrows;
		}
		
		public long getStartTimeStamp()
		{
			return _startTimeStamp;
		}

	}
}
