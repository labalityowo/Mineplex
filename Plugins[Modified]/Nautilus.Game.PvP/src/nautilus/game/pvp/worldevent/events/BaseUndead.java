package nautilus.game.pvp.worldevent.events;

import java.util.HashSet;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilWorld;
import nautilus.game.pvp.worldevent.EventBase;
import nautilus.game.pvp.worldevent.EventManager;
import nautilus.game.pvp.worldevent.creature.*;

public class BaseUndead extends EventBase
{
	private Location _loc;

	private int _mod = 1;
	private int _areaSize = 18;

	private int _hutCur = 0;
	private int _poleCur = 0;
	private int _towerCur = 0;

	private int _hutMax = 0;
	private int _poleMax = 0;
	private int _towerMax = 0;

	private int _minZombie = 0;

	private HashSet<Block> _chests = new HashSet<Block>(); 

	public BaseUndead(EventManager manager) 
	{
		super(manager, "Undead Camp", 1);

		double rand = Math.random();

		rand = rand * UtilServer.getFilledPercent();
		
		if (rand > 0.98)
		{
			_mod = 4;
			SetEventName("Undead Fortress");
			//SetExpire(4);
		}
		else if (rand > 0.93)
		{
			_mod = 3;
			SetEventName("Undead Stronghold");
			//SetExpire(3);
		}
		else if (rand > 0.60)
		{
			_mod = 2;
			SetEventName("Undead Village");
			//SetExpire(2);
		}

		_areaSize = (int) (8 * _mod);

		_hutMax = (int) (_mod * _mod * (UtilMath.r(4) + 1));
		_towerMax = (int) (_mod * _mod * (UtilMath.r(4) + 3));
		_poleMax = (int) (_mod * _mod * (UtilMath.r(11) + 10));
		_minZombie = (int) (_mod * (UtilMath.r(9) + 8));
	}

	public BaseUndead(EventManager manager, int i) 
	{
		super(manager, "Undead Camp", 1);

		double rand = i;

		if (rand > 0.98)
		{
			_mod = 4;
			SetEventName("Undead Fortress");
			SetExpire(4);
		}
		else if (rand > 0.93)
		{
			_mod = 3;
			SetEventName("Undead Stronghold");
			SetExpire(3);
		}
		else if (rand > 0.75)
		{
			_mod = 2;
			SetEventName("Undead Village");
			SetExpire(2);
		}

		_areaSize = (int) (8 * _mod);

		_hutMax = (int) (_mod * _mod * (UtilMath.r(4) + 1));
		_towerMax = (int) (_mod * _mod * (UtilMath.r(4) + 3));
		_poleMax = (int) (_mod * _mod * (UtilMath.r(11) + 10));
		_minZombie = (int) (_mod * (UtilMath.r(9) + 8));
	}

	@Override
	public void Start() 
	{

	}

	@Override
	public void Stop() 
	{

	}

	@Override
	public void PrepareCustom() 
	{
		if (_loc == null)								FindLocation();
		else if (_hutCur < _hutMax)						CreateHut();
		else if (GetCreatures().size() < _minZombie)	CreateZombie();
		else if (_towerCur < _towerMax)					CreateTower();
		else if (_poleCur < _poleMax)					CreateLamp();

		else											
		{
			System.out.println("Constructed " + GetEventName() + " at " + UtilWorld.locToStrClean(_loc) + ".");
			AnnounceStart();
			SetState(EventState.LIVE);
		}	
	}

	private void FindLocation() 
	{
		_loc = Manager.TerrainFinder().FindArea(UtilWorld.getWorldType(Environment.NORMAL), _areaSize, 2);

		if (_loc != null)
			System.out.println("Constructing " + GetEventName() + " at " + UtilWorld.locToStrClean(_loc) + "...");
	}

	private void AddChest(Block chest) 
	{
		Block side;
		int adj = 0;

		side = chest.getRelative(BlockFace.NORTH);
		if (side.getType() == Material.CHEST)
		{
			adj++;
			if (side.getRelative(BlockFace.NORTH).getType() == Material.CHEST)		return;
			if (side.getRelative(BlockFace.SOUTH).getType() == Material.CHEST)		return;
			if (side.getRelative(BlockFace.EAST).getType() == Material.CHEST)		return;
			if (side.getRelative(BlockFace.WEST).getType() == Material.CHEST)		return;
		}

		side = chest.getRelative(BlockFace.SOUTH);
		if (side.getType() == Material.CHEST)
		{
			adj++;
			if (side.getRelative(BlockFace.NORTH).getType() == Material.CHEST)		return;
			if (side.getRelative(BlockFace.SOUTH).getType() == Material.CHEST)		return;
			if (side.getRelative(BlockFace.EAST).getType() == Material.CHEST)		return;
			if (side.getRelative(BlockFace.WEST).getType() == Material.CHEST)		return;
		}

		side = chest.getRelative(BlockFace.EAST);
		if (side.getType() == Material.CHEST)
		{
			adj++;
			if (side.getRelative(BlockFace.NORTH).getType() == Material.CHEST)		return;
			if (side.getRelative(BlockFace.SOUTH).getType() == Material.CHEST)		return;
			if (side.getRelative(BlockFace.EAST).getType() == Material.CHEST)		return;
			if (side.getRelative(BlockFace.WEST).getType() == Material.CHEST)		return;
		}

		side = chest.getRelative(BlockFace.WEST);
		if (side.getType() == Material.CHEST)
		{
			adj++;
			if (side.getRelative(BlockFace.NORTH).getType() == Material.CHEST)		return;
			if (side.getRelative(BlockFace.SOUTH).getType() == Material.CHEST)		return;
			if (side.getRelative(BlockFace.EAST).getType() == Material.CHEST)		return;
			if (side.getRelative(BlockFace.WEST).getType() == Material.CHEST)		return;
		}

		if (adj > 1)
			return;

		AddBlock(chest, 130, (byte)(UtilMath.r(4) + 2));
		_chests.add(chest);	
	}

	private void AddFurnace(Block chest) 
	{
		AddBlock(chest, 61, (byte)(UtilMath.r(4) + 2));
		_chests.add(chest);	
	}

	private void CreateHut() 
	{
		int hutX = UtilMath.r(4) + 2;
		int hutZ = UtilMath.r(4) + 2;
		int hutY = UtilMath.r(2) + 3;
		
		int buffer = Math.max(hutX, hutZ)/2 + 1;

		Location loc = Manager.TerrainFinder().LocateSpace(_loc, _areaSize-buffer, hutX, hutY+2, hutZ, true, false, GetBlocks().keySet());

		if (loc == null)
			return;

		boolean xWall = (Math.random() > 0.5);
		boolean zWall = (Math.random() > 0.5);

		//Base
		for (int x=-hutX ; x<=hutX ; x++)
			for (int z=-hutZ ; z<=hutZ ; z++)
			{
				Block block = loc.getBlock().getRelative(x, -1, z);

				//Space
				for (int y=0 ; y<=hutY ; y++)
					AddBlock(loc.getBlock().getRelative(x, y, z), 0, (byte)0);

				//Walls
				if (!xWall && x == -hutX || xWall && x == hutX || !zWall && z == -hutZ || zWall && z == hutZ)
					for (int y=0 ; y<=hutY ; y++)
						AddBlock(loc.getBlock().getRelative(x, y, z), 5, (byte)0);

				//Corners
				if (Math.abs(x) == hutX && Math.abs(z) == hutZ)
				{
					AddBlock(block, 17, (byte)0);
					for (int y=0 ; y<=hutY ; y++)
						AddBlock(loc.getBlock().getRelative(x, y, z), 17, (byte)0);

					//Support Stands
					boolean support = true;
					Block below = block;
					while (support)
					{
						below = below.getRelative(BlockFace.DOWN);

						if (!UtilBlock.fullSolid(below) || below.isLiquid())
							AddBlock(below, 17, (byte)0);

						else
							support = false;
					}
				}

				//Floor & Roof
				else
				{
					AddBlock(block, 5, (byte)0);
					AddBlock(loc.getBlock().getRelative(x, hutY-1, z), 126, (byte)8);
				}

				//Insides
				if (Math.abs(x) != hutX && Math.abs(z) != hutZ)
				{
					if (Math.random() > 0.90)
						AddChest(block.getRelative(BlockFace.UP));

					else if (Math.random() > 0.95)
						AddFurnace(block.getRelative(BlockFace.UP));

					else if (Math.random() > 0.95)
						CreatureRegister(new UndeadWarrior(this, block.getRelative(BlockFace.UP).getLocation().add(0.5, 0.5, 0.5)));
				}	
			}

		_hutCur++;
		ResetIdleTicks();
	}



	private void CreateZombie()
	{

		Location loc = Manager.TerrainFinder().LocateSpace(_loc, _areaSize, 0, 3, 0, false, true, GetBlocks().keySet());

		if (loc == null)
			return;

		CreatureRegister(new UndeadWarrior(this, loc.add(0.5, 0.5, 0.5)));

		_poleCur++;
		ResetIdleTicks();	
	}

	private void CreateTower()
	{
		int towerX = UtilMath.r(3) + 1;
		int towerZ = UtilMath.r(3) + 1;
		int towerY = UtilMath.r(4) + 3;

		int buffer = Math.max(towerX, towerZ)/2 + 1;
		
		Location loc = Manager.TerrainFinder().LocateSpace(_loc, _areaSize-buffer, towerX, towerY+2, towerZ, false, true, GetBlocks().keySet());

		if (loc == null)
			return;

		int ladder = UtilMath.r(4);


		//Base
		for (int x=-towerX ; x<=towerX ; x++)
			for (int z=-towerZ ; z<=towerZ ; z++)
			{
				Block block = loc.getBlock().getRelative(x, towerY, z);

				//Space
				for (int y=0 ; y<=towerY ; y++)
					AddBlock(loc.getBlock().getRelative(x, y, z), 0, (byte)0);

				//Corner
				if (Math.abs(x) == towerX && Math.abs(z) == towerZ)
				{
					AddBlock(block, 5, (byte)0);
					AddBlock(block.getRelative(BlockFace.UP), 85, (byte)0);

					//Support Stands
					boolean support = true;
					Block below = block;
					while (support)
					{
						below = below.getRelative(BlockFace.DOWN);

						if (!UtilBlock.fullSolid(below) && !below.isLiquid())
							AddBlock(below, 85, (byte)0);

						else if (below.isLiquid())
							AddBlock(below, 17, (byte)0);

						else
							support = false;
					}

					//Ladder
					if (ladder == 0 && x == -towerX && z == -towerZ ||
							ladder == 1 && x == -towerX && z == towerZ ||
							ladder == 2 && x == towerX && z == -towerZ ||
							ladder == 3 && x == towerX && z == towerZ)
					{
						boolean laddering = true;
						below = block;
						while (laddering)
						{
							below = below.getRelative(BlockFace.DOWN);

							if (!UtilBlock.fullSolid(below))
							{
								AddBlock(below, 5, (byte)0);

								if (ladder == 0)			AddBlock(below.getRelative(-1, 0, 0), 65, (byte)4);
								else if (ladder == 1)		AddBlock(below.getRelative(-1, 0, 0), 65, (byte)4);
								else if (ladder == 2)		AddBlock(below.getRelative(1, 0, 0), 65, (byte)5);
								else if (ladder == 3)		AddBlock(below.getRelative(1, 0, 0), 65, (byte)5);
							}

							else
								laddering = false;
						}
					}
				}

				//Platform
				else
					AddBlock(block, 126, (byte)8);

				//Features
				if (Math.random() > 0.95)
					AddChest(block.getRelative(BlockFace.UP));

				else if (Math.random() > 0.95)
					CreatureRegister(new UndeadArcher(this, block.getRelative(BlockFace.UP).getLocation().add(0.5, 0.5, 0.5)));
			}

		_towerCur++;
		ResetIdleTicks();
	}

	private void CreateLamp()
	{
		Location loc = Manager.TerrainFinder().LocateSpace(_loc, _areaSize, 0, 4, 0, false, true, GetBlocks().keySet());

		if (loc == null)
			return;

		AddBlock(loc.getBlock(), 85, (byte)0);
		AddBlock(loc.getBlock().getRelative(BlockFace.UP), 85, (byte)0);
		AddBlock(loc.getBlock().getRelative(BlockFace.UP).getRelative(BlockFace.UP), 50, (byte)0);

		_poleCur++;
		ResetIdleTicks();
	}

	@Override
	public void AnnounceStart() 
	{
		UtilServer.broadcast(F.main("World Event", F.elem(GetEventName()) + " has been constructed."));
	}

	@Override
	public void AnnounceDuring() 
	{
		Location loc = _loc;
		for (Block block : _chests)
		{
			loc = block.getLocation();
			break;
		}

		UtilServer.broadcast(F.main("World Event", F.elem(GetEventName()) + " is near " + 
				F.elem(UtilWorld.locToStrClean(loc)) + "."));
	}

	@Override
	public void AnnounceEnd() 
	{
		UtilServer.broadcast(F.main("World Event", F.elem(GetEventName()) + " has been pillaged."));
	}

	@Override
	public void AnnounceExpire() 
	{
		UtilServer.broadcast(F.main("World Event", F.elem(GetEventName()) + " has been abandoned."));
	}

	@EventHandler(priority = EventPriority.LOW)
	public void ChestOpen(PlayerInteractEvent event)
	{
		if (event.getClickedBlock() == null)
			return;

		if (event.getClickedBlock().getType() != Material.ENDER_CHEST &&
				event.getClickedBlock().getType() != Material.FURNACE)
			return;

		if (!GetBlocks().containsKey(event.getClickedBlock()))
			return;

		event.setCancelled(true);

		if (!Recharge.Instance.use(event.getPlayer(), "Loot Chest", 60000, true, false))
			return;

		if (event.getClickedBlock().getType() == Material.ENDER_CHEST)		LootChest(event.getClickedBlock());
		else if (event.getClickedBlock().getType() == Material.FURNACE)		LootFurance(event.getClickedBlock());

		//Inform
		UtilPlayer.message(event.getPlayer(), F.main(GetEventName(), "You smash open an " + F.elem("Undead Chest") + "!"));

		//Effect
		event.getClickedBlock().getWorld().playEffect(event.getClickedBlock().getLocation(), Effect.STEP_SOUND, event.getClickedBlock().getTypeId());

		//Clear
		_chests.remove(event.getClickedBlock());
		event.getClickedBlock().setTypeId(0);
	}

	private void LootFurance(Block block) 
	{
		for (int i=0 ; i<UtilMath.r(25) ; i++)
			block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), 
					ItemStackFactory.Instance.CreateStack(Material.IRON_INGOT));
		
		for (int i=0 ; i<UtilMath.r(25) ; i++)
			block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), 
					ItemStackFactory.Instance.CreateStack(Material.GOLD_INGOT));
		
		for (int i=0 ; i<UtilMath.r(25) ; i++)
			block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), 
					ItemStackFactory.Instance.CreateStack(Material.DIAMOND));
		
		for (int i=0 ; i<UtilMath.r(25) ; i++)
			block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), 
					ItemStackFactory.Instance.CreateStack(Material.LEATHER));
	}

	private void LootChest(Block block) 
	{
		Manager.Loot().DropLoot(block.getLocation().add(0.5, 0.5, 0.5), 4, 4, 0.04f, 0.005f, 1d);
	}

	@EventHandler
	public void ChestBreak(BlockBreakEvent event)
	{
		if (event.isCancelled())
			return;

		if (!GetBlocks().containsKey(event.getBlock()))
			return;

		event.setCancelled(true);
	}

	@EventHandler
	public void EndCheck(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		if (GetState() != EventState.LIVE)
			return;

		InvalidChest();
		
		if (!_chests.isEmpty())
			return;

		AnnounceEnd();
		TriggerStop();
	}

	private void InvalidChest() 
	{
		HashSet<Block> remove = new HashSet<Block>();
		
		for (Block cur : _chests)
			if (cur.getType() != Material.ENDER_CHEST && cur.getType() != Material.FURNACE && cur.getType() != Material.BURNING_FURNACE)
				remove.add(cur);
		
		for (Block cur : remove)
			_chests.remove(cur);
	}	

	@Override
	public boolean CanExpire() 
	{
		if (GetState() != EventState.LIVE)
			return false;

		return true;
	}
}
