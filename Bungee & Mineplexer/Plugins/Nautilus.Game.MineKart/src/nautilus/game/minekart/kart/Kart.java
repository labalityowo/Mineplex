package nautilus.game.minekart.kart;

import java.util.ArrayList;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.fakeEntity.FakeEntity;
import mineplex.core.fakeEntity.FakeEntityManager;
import mineplex.core.fakeEntity.FakeItemDrop;
import mineplex.core.fakeEntity.FakePlayer;
import mineplex.core.itemstack.ItemStackFactory;
import nautilus.game.minekart.gp.GP;
import nautilus.game.minekart.gp.GP.GPState;
import nautilus.game.minekart.gp.GPBattle;
import nautilus.game.minekart.item.KartItemActive;
import nautilus.game.minekart.item.KartItemType;
import nautilus.game.minekart.kart.condition.ConditionData;
import nautilus.game.minekart.kart.condition.ConditionType;
import nautilus.game.minekart.kart.crash.Crash;
import nautilus.game.minekart.track.Track.TrackState;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class Kart
{
	public enum DriftDirection
	{
		Left,
		Right,
		None
	}
	
	private Player _driver = null;
	private FakeEntity _entity = null;
	private FakeItemDrop _fakeItem = null;
	private FakePlayer _fakePlayer = null;
	
	private GP _gp;

	//Kart
	private KartType _kartType;
	private KartState _kartState = KartState.Drive;
	private long _kartStateTime = System.currentTimeMillis();
	private Vector _yaw;
	
	//Drift Data
	private DriftDirection _drift = DriftDirection.None;
	private long _driftStart = 0;
	
	//Crash Data
	private Crash _crash;
	
	//Items
	private int _itemCycles = 0;
	private KartItemActive _itemActive = null;
	private KartItemType _itemStored = null;
	
	//Conditions
	private ArrayList<ConditionData> _conditions = new ArrayList<ConditionData>();
	
	//Battle
	private int _lives = 3;
	
	//Lap
	private int _lap = 0;
	private int _lapNode = 0;
	private double _lapScore = 0;
	private int _lapMomentum = 10000;
	
	private int _lapPlace = 0;
	
	private int _lakituTick = 0;
	
	//Music
	private int _songStarTick = 0;
	
	//Physics
	private Vector _velocity = new Vector(0,0,0);

	public Kart(Player player, KartType kartType, GP gp)
	{
		_gp = gp;
		
		_driver = player;
		_kartType = kartType;
		_entity = new FakeEntity(kartType.GetType(), player.getLocation());
		
		Equip();
	}
	
	public GP GetGP()
	{
		return _gp;
	}

	public FakeEntity GetEntity()
	{
		return _entity;
	}

	public Player GetDriver()
	{		
		return _driver;
	}
	
	public KartType GetKartType()
	{
		return _kartType;
	}
	
	public void SetKartType(KartType type) 
	{
		_kartType = type;
		Equip();
	}
	
	public KartState GetKartState()
	{
		return _kartState;
	}
	
	public long GetKartStateTime()
	{
		return _kartStateTime;
	}
	
	public void SetKartState(KartState state)
	{
		_kartState = state;
		_kartStateTime = System.currentTimeMillis();
	}
	
	public Vector GetKartDirection()
	{
		return _velocity;
	}
	
	public int GetLakituTick()
	{
		return _lakituTick;
	}
	
	public void SetLakituTick(int tick)
	{
		_lakituTick += tick;
		
		if (_lakituTick <= 0)
			_lakituTick = 0;
	}
	
	public ArrayList<ConditionData> GetConditions()
	{
		return _conditions;
	}
	
	public void AddCondition(ConditionData data)
	{
		_conditions.add(data);
		
		if (data.IsCondition(ConditionType.Star))
			SetStarSongTick(0);
	}
	
	public boolean HasCondition(ConditionType type)
	{
		for (ConditionData data : _conditions)
		{
			if (data.IsCondition(type))
				return true;
		}
		
		return false;
	}
	
	public void ExpireCondition(ConditionType type)
	{
		for (ConditionData data : GetConditions())
			if (data.IsCondition(type))
				data.Expire();
	}
	
	public void ExpireConditions() 
	{
		for (ConditionData data : GetConditions())
			data.Expire();
	}
	
	public void Equip()
	{
		_driver.getInventory().clear();
		
		//Driving
		GetDriver().getInventory().setItem(0, ItemStackFactory.Instance.CreateStack(Material.STONE_SWORD, (byte)0, 1, "§a§lAccelerate"));
		GetDriver().getInventory().setItem(1, ItemStackFactory.Instance.CreateStack(Material.WOOD_SWORD, (byte)0, 1, "§a§lHand Brake"));

		//Item Slot
		GetDriver().getInventory().setItem(2, null);
		GetDriver().getInventory().setItem(3, null);
		GetDriver().getInventory().setItem(4, null);
		
		//State
		ItemStack a = ItemStackFactory.Instance.CreateStack(Material.WOOD_HOE, (byte)0, (int)(GetKartType().GetTopSpeed() * 100), "§e§lTop Speed");
		ItemStack b = ItemStackFactory.Instance.CreateStack(Material.STONE_HOE, (byte)0, (int)GetKartType().GetAcceleration() - 10, "§e§lAcceleration");
		ItemStack c = ItemStackFactory.Instance.CreateStack(Material.IRON_HOE, (byte)0, (int)GetKartType().GetHandling() - 10, "§e§lHandling");
		ItemStack d = ItemStackFactory.Instance.CreateStack(GetKartType().GetAvatar(), (byte)0, 1, "§e§l" + GetKartType().GetName() + " Kart");
		
		GetDriver().getInventory().setItem(5, d);
		GetDriver().getInventory().setItem(6, a);
		GetDriver().getInventory().setItem(7, b);
		GetDriver().getInventory().setItem(8, c);
			
	}
	
	public Vector GetVelocity()
	{
		return _velocity;
	}
	
	public Vector GetVelocityClone()
	{
		return new Vector(_velocity.getX(), _velocity.getY(), _velocity.getZ());
	}
	
	public void SetVelocity(Vector vec) 
	{
		_velocity = vec;
	}
	
	public Vector GetYaw()
	{
		return _yaw;
	}
	
	public void CrashStop()
	{
		_velocity = new Vector(0,0,0);
		ExpireCondition(ConditionType.Boost);
		
		if (GetKartState() == KartState.Drive)
			LoseLife();
	}
	
	public double GetSpeed()
	{
		Vector vec = new Vector(_velocity.getX(), 0, _velocity.getZ());
		return vec.length();
	}

	public void SetDrift() 
	{
		ClearDrift();
		
		//Check Speed Requirement
		if (GetSpeed() < 0.4)
		{
			return;
		}
					
		//Check Turn Requirement
		Vector look = GetDriver().getLocation().getDirection();
		look.setY(0);
		look.normalize();
		
		Vector vel = new Vector(GetVelocity().getX(), 0, GetVelocity().getZ());
		vel.normalize();
		
		look.subtract(vel);
		
		if (look.length() < 0.2)
		{
			return;
		}
			
		
		//Get Drift Direction
		Vector kartVec = new Vector(_velocity.getX(), 0, _velocity.getZ());
		kartVec.normalize();

		Vector lookVec = GetDriver().getLocation().getDirection();
		lookVec.setY(0);
		lookVec.normalize();
		
		Vector left = new Vector(kartVec.getZ(), 0, kartVec.getX() * -1);
		Vector right = new Vector(kartVec.getZ() * -1, 0, kartVec.getX());
		
		double distLeft = UtilMath.offset(left, lookVec);
		double distRight = UtilMath.offset(right, lookVec);
		
		if (distLeft < distRight)	_drift = DriftDirection.Left;
		else						_drift = DriftDirection.Right;

		_driftStart = System.currentTimeMillis();
	}
	
	public DriftDirection GetDrift()
	{
		return _drift;
	}
	
	public Vector GetDriftVector() 
	{
		if (_drift == DriftDirection.None)
			return new Vector(0,0,0);
		
		//Get Drift Direction
		Vector kartVec = new Vector(_velocity.getX(), 0, _velocity.getZ());
		kartVec.normalize();

		Vector lookVec = GetDriver().getLocation().getDirection();
		lookVec.setY(0);
		lookVec.normalize();
		
		if (_drift == DriftDirection.Left)
		{
			Vector drift = new Vector(kartVec.getZ(), 0, kartVec.getX() * -1);
			return drift.subtract(kartVec);
		}
		else
		{
			Vector drift = new Vector(kartVec.getZ() * -1, 0, kartVec.getX());
			return drift.subtract(kartVec);
		}
	}
	
	public long GetDriftTime()
	{
		return System.currentTimeMillis() - _driftStart;
	}

	public void ClearDrift() 
	{
		_driftStart = System.currentTimeMillis();
		_drift = DriftDirection.None;
	}

	public Crash GetCrash()
	{
		if (GetKartState() != KartState.Crash)
			return null;
		
		return _crash;
	}
	
	public void SetCrash(Crash crash)
	{
		_crash = crash;
	}

	public void PickupItem() 
	{
		if (GetItemStored() == null && GetItemCycles() == 0)
			SetItemCycles(40);
	}
	
	public void SetItemCycles(int cycles)
	{
		_itemCycles = cycles;
	}
	
	public int GetItemCycles()
	{
		return _itemCycles;
	}
	
	public KartItemType GetItemStored()
	{
		return _itemStored;
	}
	
	public KartItemActive GetItemActive()
	{
		return _itemActive;
	}
	
	public void SetItemStored(KartItemType item)
	{
		_itemStored = item;
		
		if (item == null)
		{
			if (_fakeItem != null)
				RemoveFakeKartItemInfo();
		}
		else
		{
			SetFakeKartItemInfo(item);
		}
	}
	
	private void SetFakeKartItemInfo(KartItemType item)
	{		
		boolean showPlayer = false;
		boolean spawnItem = false;
		
		if (_fakeItem == null)
		{
			_fakeItem = new FakeItemDrop(new ItemStack(item.GetMaterial()), GetDriver().getLocation());
			spawnItem = true;
		}
		else
			_fakeItem.SetItemStack(new ItemStack(item.GetMaterial()));
		
		if (_fakePlayer == null)
		{
			showPlayer = true;
			_fakePlayer = new FakePlayer("Buffer", GetDriver().getLocation().subtract(0, 10, 0));
		}
		
		//Set Item
		GetDriver().getInventory().setItem(3, ItemStackFactory.Instance.CreateStack(item.GetMaterial(), (byte)0, item.GetAmount(), "§a§l"+item.GetName()));
		
		for (Kart kart : GetGP().GetKarts())
		{
			if (kart == this)
			{
				if (spawnItem)
				{
					FakeEntityManager.Instance.SendPacketTo(_fakeItem.Spawn(), kart.GetDriver());
					FakeEntityManager.Instance.SendPacketTo(_fakeItem.SetVehicle(kart.GetDriver().getEntityId()), kart.GetDriver());
				}
				
				FakeEntityManager.Instance.SendPacketTo(_fakeItem.Show(), kart.GetDriver());
				
				continue;
			}

			if (showPlayer)
			{
				FakeEntityManager.Instance.SendPacketTo(_fakePlayer.Spawn(), kart.GetDriver());
				FakeEntityManager.Instance.SendPacketTo(_fakePlayer.Hide(), kart.GetDriver());
				FakeEntityManager.Instance.SendPacketTo(_fakePlayer.SetVehicle(GetDriver().getEntityId()), kart.GetDriver());
				
				FakeEntityManager.Instance.FakePassenger(kart.GetDriver(), GetDriver().getEntityId(), _fakePlayer.SetVehicle(GetDriver().getEntityId()));
			}
				
			if (spawnItem)
			{
				FakeEntityManager.Instance.SendPacketTo(_fakeItem.Spawn(), kart.GetDriver());
				FakeEntityManager.Instance.SendPacketTo(_fakeItem.SetVehicle(_fakePlayer.GetEntityId()), kart.GetDriver());
				
				FakeEntityManager.Instance.FakePassenger(kart.GetDriver(), _fakePlayer.GetEntityId(), _fakeItem.SetVehicle(_fakePlayer.GetEntityId()));
			}
			
			FakeEntityManager.Instance.SendPacketTo(_fakeItem.Show(), kart.GetDriver());
		}
	}

	private void RemoveFakeKartItemInfo()
	{
		GetDriver().getInventory().setItem(3, null);
		
		for (Kart kart : GetGP().GetKarts())
		{
			if (_fakeItem != null)
			{
				FakeEntityManager.Instance.SendPacketTo(_fakeItem.Destroy(), kart.GetDriver());
			}
			
			if (kart != this)
			{
				if (_fakeItem != null && _fakePlayer != null)
				{
					FakeEntityManager.Instance.RemoveFakePassenger(kart.GetDriver(), _fakePlayer.GetEntityId());
				}
			}
		}
		
		_fakeItem = null;
	}

	public void SetItemActive(KartItemActive item)
	{
		_itemActive = item;
	}

	public void SetStability(int i) 
	{
		GetDriver().setFoodLevel(i);
	}
	
	public boolean CanControl()
	{
		if (GetGP() == null)
			return true;
		
		if (GetGP().GetState() != GPState.Live)
			return false;
		
		if (GetGP().GetTrack().GetState() != TrackState.Live && GetGP().GetTrack().GetState() != TrackState.Ending)
			return false;
		
		if (HasFinishedTrack())
			return false;
		
		return true;
	}
	
	public int GetLives()
	{
		return _lives;
	}
	
	public void LoseLife()
	{
		if (_lives < 1)
			return;
		
		if (GetGP() == null)
			return;
		
		if (!(GetGP() instanceof GPBattle))
			return;
		
		GPBattle battle = (GPBattle)GetGP();
		_lives = _lives - 1;
		
		if (_lives == 0)
		{
			battle.GetTrack().GetPositions().add(0, this);
			battle.CheckBattleEnd();
		}
	}

	public int GetLap() 
	{
		return _lap;
	}

	public void SetLap(int lap) 
	{
		_lap = lap;
	}

	public int GetLapNode() 
	{
		return _lapNode;
	}

	public void SetLapNode(int lapNode) 
	{
		if ((lapNode > _lapNode && !(_lapNode <= GetGP().GetTrack().GetProgress().size() / 5 && lapNode >= GetGP().GetTrack().GetProgress().size() * 4 / 5)) || (lapNode <= GetGP().GetTrack().GetProgress().size() / 5 && _lapNode >= GetGP().GetTrack().GetProgress().size() * 4 / 5))
			SetLapMomentum(GetLapMomentum() + 1);
		else if (lapNode < _lapNode || (_lapNode <= GetGP().GetTrack().GetProgress().size() / 5 && lapNode >= GetGP().GetTrack().GetProgress().size() * 4 / 5))
			SetLapMomentum(GetLapMomentum() - 1);
		
		_lapNode = lapNode;
		
		//Next Lap
		if (lapNode <= GetGP().GetTrack().GetProgress().size() / 5)
		{
			if (_lapMomentum > GetGP().GetTrack().GetProgress().size() / 4)
			{				
				SetLap(GetLap() + 1);
				SetLapMomentum(0);
				
				GetDriver().getWorld().playSound(GetDriver().getLocation(), GetKartType().GetSoundMain(), 0.5f, 1f);
				
				if (GetLap() > 3)
				{
					int place = GetLapPlace() + 1;
					
					String 					placeString = "st";
					if (place == 2)	 		placeString = "nd";
					else if (place == 3)	placeString = "rd";
					else if (place >= 4)	placeString = "th";
					
					GetGP().Announce(F.main("MK", F.name(GetDriver().getName()) + " finished in " + F.elem(place + placeString) + " place."));
				}
				else
					UtilPlayer.message(GetDriver(), F.main("MK", "Lap " + GetLap()));
			}
		}
	}

	public double GetLapScore() 
	{
		return _lapScore;
	}

	public void SetLapScore(double lapScore) 
	{	
		_lapScore = lapScore + (1000000 * (_lap - (GetLapMomentum()  < 0 ? 1 : 0)));
	}

	public int GetLapMomentum() 
	{
		return _lapMomentum;
	}

	public void SetLapMomentum(int lapMomentum) 
	{
		_lapMomentum = lapMomentum;
	}

	public int GetLapPlace()
	{
		return _lapPlace;
	}
	
	public void SetLapPlace(int place) 
	{
		//Roar with joy!
		if (place < _lapPlace)
			GetDriver().getWorld().playSound(GetDriver().getLocation(), GetKartType().GetSoundMain(), 0.5f, 1f);

		_lapPlace = place;
	}

	public boolean HasFinishedTrack() 
	{
		return GetLap() > 3 || GetLives() <= 0;
	}

	public void ClearTrackData() 
	{
		//Battle
		_lives = 3;
		
		//Lap
		_lap = 0;
		_lapNode = 0;
		_lapScore = 0;
		_lapMomentum = 10000;
		
		_lapPlace = 0;
	}

	public int GetStarSongTick() 
	{
		return _songStarTick;
	}
	
	public void SetStarSongTick(int tick)
	{
		_songStarTick = tick;
	}

	public void SetPlayerArmor() 
	{
		GetDriver().getInventory().setHelmet(null);
		GetDriver().getInventory().setChestplate(null);
		GetDriver().getInventory().setLeggings(null);
		GetDriver().getInventory().setBoots(null);
	}

	public boolean IsInvulnerable(boolean useHeart) 
	{
		if (useHeart && HasCondition(ConditionType.WolfHeart))
		{
			ExpireCondition(ConditionType.WolfHeart);
			return true;
		}
		
		return HasCondition(ConditionType.Star) || HasCondition(ConditionType.Ghost);
	}
}
