package nautilus.game.arcade.game.games.christmas.parts;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.hologram.Hologram;
import mineplex.core.hologram.HologramManager;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.utils.UtilVariant;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.christmas.Christmas;
import nautilus.game.arcade.game.games.christmas.ChristmasAudio;
import nautilus.game.arcade.game.games.christmas.content.CaveGiant;
import nautilus.game.arcade.game.games.christmas.content.Snake;

public class Part4 extends Part
{
	private ArrayList<Location> _roofIce;
	private ArrayList<Location> _mobSpawns;
	private ArrayList<Location> _gate;
	private ArrayList<Location> _checkpoints;
	
	private ArrayList<Snake> _snakes;

	private Location _giantSpawn;
	private CaveGiant _giant;

	private boolean _a = false;
	private boolean _b = false;
	private HologramManager _holoManager;
	
	private String[] _evilElfNames = new String[]
	{ "Bing", "Bling", "Blitz", "Larry", "Buddy", "Buster", "Cedar", "Dash", "Eggnog", "Elfie", "Elm", "Elvis",
			"Evergreen", "Figgy", "Flake", "Frank", "Frost", "Gabriel", "George", "Henry", "Hermey", "Ice", "Jangle",
			"Jingle", "Jinx", "Kringle", "Kris", "Louie", "Max", "Mistletoe", "Nat", "Nick", "Noel", "Pax", "Peppermin",
			"Pine", "Ralphie", "Rudy", "Snow", "Snowball", "Star", "Tinsel", "Tiny", "Topper", "Trinket", "Wayne",
			"Wink", "Yule", "Zippy" };
	
	public Part4(HologramManager holoManager, Christmas host, Location sleigh, Location[] presents, ArrayList<Location> roofIce, ArrayList<Location> mobs, 
			ArrayList<Location> snakeHead, ArrayList<Location> snakeTrail, ArrayList<Location> gate, ArrayList<Location> checkpoints, Location giant) 
	{
		super(host, sleigh, presents);

		_holoManager = holoManager;
		_roofIce = roofIce;
		_mobSpawns = mobs;
		_giantSpawn = giant;
		_gate = gate;
		_checkpoints = checkpoints;

		for (Location loc : roofIce)
			loc.getBlock().setType(Material.AIR);

		for (Location loc : gate)
			loc.getBlock().setType(Material.FENCE);

		//Create Snakes
		_snakes = new ArrayList<Snake>();

		for (Location loc : snakeHead)
			_snakes.add(new Snake(loc, snakeTrail));
	}

	@Override
	public void Activate() 
	{

	}

	@EventHandler
	public void GateUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (_gate.isEmpty())
			return;

		int lowest = 255;

		for (Location loc : _gate)
			if (loc.getBlockY() < lowest)
				lowest = loc.getBlockY();

		Iterator<Location> gateIterator = _gate.iterator();

		boolean sound = true;

		while (gateIterator.hasNext())
		{
			Location loc = gateIterator.next();

			if (loc.getBlockY() == lowest)
			{
				loc.getBlock().setType(Material.AIR);
				gateIterator.remove();

				if (sound)
				{
					loc.getWorld().playSound(loc, Sound.PISTON_RETRACT, 3f, 1f);
					sound = false;
				}
			}
		}
	}

	@EventHandler
	public void SnakeUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;

		for (Snake snake : _snakes)
			snake.Update();
	}

	@EventHandler
	public void IceUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;
		/*
		Block block = UtilAlg.Random(_roofIce).getBlock().getRelative(BlockFace.DOWN);

		while (block.getType() != Material.AIR && block.getY() > 0)
			block = block.getRelative(BlockFace.DOWN);

		block.getWorld().spawnFallingBlock(block.getLocation().add(0.5, 0.5, 0.5), Material.ICE, (byte)0);
		 */
	}

	@EventHandler
	public void IceLand(EntityChangeBlockEvent event)
	{
		if (event.getEntity() instanceof FallingBlock)
		{
			event.setCancelled(true);
			event.getEntity().getWorld().playEffect(event.getEntity().getLocation(), Effect.STEP_SOUND, 79);
			event.getEntity().remove();
		}
	}

	@EventHandler
	public void MonstersUpdate(UpdateEvent event)
	{
		if (GetCreatures().size() > 15)
		{
			if (event.getType() != UpdateType.SEC)
				return;
		}
		else 
		{
			if (event.getType() != UpdateType.FASTER)
				return;
		}

		if (HasPresents())
			return;

		if (GetCreatures().size() > 30)
			return;

		//Create
		Location loc = UtilAlg.Random(_mobSpawns);
		
		Host.CreatureAllowOverride = true;
		Zombie ent = UtilVariant.spawnZombieVillager(loc);
		Host.CreatureAllowOverride = false;
		
		ent.getEquipment().setItemInHand(new ItemStack(Material.WOOD_PICKAXE));
		ent.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
		ent.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
		ent.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));

		ent.setCustomName("Evil Elf " + UtilMath.randomElement(_evilElfNames));

		ent.setHealth(9);

		this.AddCreature(ent);
	}

	@EventHandler
	public void GiantUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		if (_giant == null && HasPresents())
		{
			_giant = new CaveGiant(this, _giantSpawn);

			Host.SantaSay("OH NO! ITS A FROST GIANT! KILL IT!", ChristmasAudio.P4_C);	
			SetObjectiveText("Kill the Giant before it reaches Santa", 1);
		}
		else if (_giant != null)
		{
			_giant.MoveUpdate();
			SetObjectiveText("Kill the Giant before it reaches Santa", _giant.GetEntity().getHealth()/_giant.GetEntity().getMaxHealth());
		}
	}

	@EventHandler
	public void UpdateIntro(UpdateEvent event) 
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (!_a)
		{
			if (UtilMath.offset(Host.GetSleigh().GetLocation(), GetSleighWaypoint()) > 10)
				return;

			_a = true;

			Host.SantaSay("That wall of ice is blocking our path!", ChristmasAudio.P4_A);	
		}	
		if (!_b)
		{
			if (UtilMath.offset(Host.GetSleigh().GetLocation(), GetSleighWaypoint()) > 2)
				return;

			_b = true;

			Host.SantaSay("Get those presents while I think of a way through!", ChristmasAudio.P4_B);	
			SetObjectivePresents();
		}	
	}

	@Override
	public boolean CanFinish() 
	{
		return (_giant != null && _giant.IsDead());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void GiantKnockback(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity() instanceof Giant)
			event.SetKnockback(false);
	}
	
	@EventHandler
	public void CheckpointTrigger(PlayerInteractEvent event)
	{
		if (!Host.IsAlive(event.getPlayer()))
			return;
		
		if (event.getClickedBlock() == null)
			return;
		
		if (event.getClickedBlock().getType() != Material.LEVER)
			return;
		
		Iterator<Location> locIter = _checkpoints.iterator();
		
		while (locIter.hasNext())
		{
			Location loc = locIter.next();
			
			if (UtilMath.offset(loc, event.getClickedBlock().getLocation()) < 5)
			{
			    new Hologram(_holoManager, loc.clone().add(0, 2, 0), C.cGold + C.Bold + "Checkpoint").start();
				loc.getBlock().setType(Material.WATER);
				locIter.remove();
			}
		}
	}
}
