package mineplex.minecraft.game.classcombat.Skill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import mineplex.core.common.util.C;
import mineplex.core.common.util.NautHashMap;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.repository.token.SkillToken;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public abstract class Skill implements ISkill, Listener
{	
	//Properties
	private String _name;
	private String[] _desc;
	private HashMap<Integer, String[]> _descLevels = new HashMap<Integer, String[]>();

	private ClassType _classType;
	private SkillType _skillType;	

	private int _salesPackageId;
	private int _gemCost = 1000;
	private int _tokenCost = 0;
	private int _maxLevel = 1;

	private boolean _free;
	private NautHashMap<Player, Integer> _users;
	
	private boolean _isAchievementSkill = false;

	private LocationFilter _locationFilter = LocationFilter.ACCEPT_ALL;

	public SkillFactory Factory;

	public Skill(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int maxLevel)
	{
		Factory = skills;
		_name = name;
		_desc = new String[] { "<Skill Description>" };
		_classType = classType;
		_skillType = skillType;
		_users = new NautHashMap<Player, Integer>();
		_maxLevel = maxLevel;
		_tokenCost = cost;
	}

	@Override
	public String GetName() 
	{
		return _name;
	}

	public String GetName(int level) 
	{
		if (level <= 1)
			return GetName();

		return _name + " " + level;
	}

	public String GetName(String type) 
	{
		return _name + " (" + type + ")";
	}

	@Override
	public Integer GetSalesPackageId()
	{
		return _salesPackageId;
	}

	@Override
	public ClassType GetClassType() 
	{
		return _classType;
	}

	@Override
	public SkillType GetSkillType()
	{
		return _skillType;
	}

	@Override
	public int GetGemCost()
	{
		return _gemCost;
	}
	
	@Override
	public int GetTokenCost()
	{
		return _tokenCost;
	}

	@Override
	public int getMaxLevel()
	{
		return _maxLevel;
	}

	public int getLevel(Entity ent)
	{
		if (!(ent instanceof Player))
			return 0;

		Player player = (Player)ent;

		if (!_users.containsKey(player))
			return 0;
		
		int level = _users.get(player);
		
		if (GetSkillType() == SkillType.Sword)
			if (UtilGear.isMat(player.getItemInHand(), Material.GOLD_SWORD))
				level += 2;

		if (GetSkillType() == SkillType.Axe)
			if (UtilGear.isMat(player.getItemInHand(), Material.GOLD_AXE))
				level += 2;
		
		return Math.min(level, _maxLevel + 1);
	}

	@Override
	public String[] GetDesc(int curLevel)
	{
		//Desc of this level has already been generated
		if (_descLevels.containsKey(curLevel))
		{
			return _descLevels.get(curLevel);
		}

		//Generate DESC


		ArrayList<String> descOut = new ArrayList<String>();

		//Parse Desc
		for (String line : _desc)
			descOut.add(ModifyLineToLevel(line, curLevel));

		//Append Energy & Recharge
		if (GetEnergyString() != null || GetRechargeString() != null)
			descOut.add("");

		if (GetEnergyString() != null)
			descOut.add(ModifyLineToLevel(GetEnergyString(), curLevel));

		if (GetRechargeString() != null)
			descOut.add(ModifyLineToLevel(GetRechargeString(), curLevel));
		
		//Return
		String[] out = new String[descOut.size()];

		for (int i=0 ; i<descOut.size() ; i++)
			out[i] = descOut.get(i);

		//Store
		_descLevels.put(curLevel, out);

		return out;
	}

	public String ModifyLineToLevel(String line, int level)
	{
		String newLine = "";

		//Check for Level TOKEN
		for (String token : line.split(" "))
		{
			if (token.length() <= 0)
				continue;

			//Parse Level Token
			if (token.charAt(0) == '#') 
			{
				token = token.substring(1, token.length());
				String[] numberToks = token.split("\\#");

				try
				{
					float base = Float.parseFloat(numberToks[0]);
					float bonus = Float.parseFloat(numberToks[1]);

					float levelValue = base + (level * bonus);

					String plusMinus = "+";
					if (bonus < 0)
						plusMinus = "";
					
					//Round off 0's
					String bonusString = bonus + "";
					if (bonus % 1 == 0)
						bonusString = (int)bonus + "";

					String totalString = levelValue + "";
					if (levelValue % 1 == 0)
						totalString = (int)levelValue + "";
					
					//Only display what you'd get with level 1
					if (level == 0 )			
					{
						levelValue = base + (1f * bonus);
						
						totalString = levelValue + "";
						if (levelValue % 1 == 0)
							totalString = (int)levelValue + "";
						
						token = C.cGreen + totalString + C.cGray;
					}
					//Maxed
					else if (level == getMaxLevel())			
					{
						token = C.cYellow + totalString + C.cGray;	
					}
					else
					{
						token = C.cYellow + totalString + C.cGray + " (" + C.cGreen + plusMinus + bonusString + C.cGray + ")";	
					}
				}
				catch (Exception e)
				{
					token = C.cRed + token + C.cGray;
				}
			}

			newLine += token + " ";
		}

		//Remove Space
		if (newLine.length() > 0)
			newLine = newLine.substring(0, newLine.length()-1);

		return newLine;
	}

	public String GetEnergyString() 
	{
		return null;
	}

	public String GetRechargeString() 
	{
		return null;
	}

	protected boolean doesUserHaveSkill(Player player)
	{
		return _users.containsKey(player);
	}
	
	@Override
	public Set<Player> GetUsers()
	{
		_users.remove(null);
		return _users.keySet();
	}

	public void AddUser(Player player, int level)
	{
		_users.put(player, level);
		OnPlayerAdd(player);
	}

	public void OnPlayerAdd(Player player)
	{
		//Null Default
	}

	@Override
	public void RemoveUser(Player player)
	{
		_users.remove(player);
		Reset(player);
	}

	public void SetDesc(String[] desc)
	{
		_desc = desc;
	}

	@EventHandler
	public final void Death(PlayerDeathEvent event)
	{
		Reset(event.getEntity());
	}
	
	/**
	 * Trigger {@link UtilPlayer#onHotbarChange(Player)} to appropriately check
	 * if players are blocking.
	 * @param event
	 */
	@EventHandler
	public void onHotbarChange(PlayerItemHeldEvent event)
	{
		UtilPlayer.onHotbarChange(event.getPlayer()); 
	} 

	@EventHandler
	public final void Quit(PlayerQuitEvent event)
	{
		Reset(event.getPlayer());
		_users.remove(event.getPlayer());
	}

	@Override
	public boolean IsFree()
	{
		return _free;
	}
	
	@Override
	public void setFree(boolean free)
	{
		_free = free;
	}

	public void Update(SkillToken skillToken) 
	{
		_salesPackageId = skillToken.SalesPackage.GameSalesPackageId;
	}
	
	public void DisplayProgress(Player player, String ability, float amount)
	{
		UtilTextBottom.displayProgress(C.Bold + ability, amount, player);
		
		if (amount < 1)
			player.playSound(player.getLocation(), Sound.NOTE_PIANO, 0.5f, 0.5f + (amount * 1.5f));
	}
	
	public void setAchievementSkill(boolean var)
	{
		_isAchievementSkill = var;
	}
	
	@Override
	public boolean isAchievementSkill()
	{
		return _isAchievementSkill;
	}

	@Override
	public void setLocationFilter(LocationFilter filter)
	{
		_locationFilter = filter;
	}

	@Override
	public LocationFilter getLocationFilter()
	{
		return _locationFilter;
	}

	protected boolean isInWater(Entity entity)
	{
		// Unfortunately we cannot use the nms inWater for the entity. Why?
		// Well inWater becomes false if the player tries to get out of the water vertically.
		// This leads to abilities being used in water which is bad news for Clans.
		Block block = entity.getLocation().getBlock();
		return block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER;
	}
}