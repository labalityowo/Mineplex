package mineplex.minecraft.game.classcombat.Skill;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import mineplex.core.MiniPlugin;
import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.donation.repository.GameSalesPackageToken;
import mineplex.core.energy.Energy;
import mineplex.core.movement.Movement;
import mineplex.core.projectile.ProjectileManager;
import mineplex.core.teleport.Teleport;
import mineplex.minecraft.game.classcombat.Class.IPvpClass;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.ISkill.SkillType;
import mineplex.minecraft.game.classcombat.Skill.Assassin.Assassin;
import mineplex.minecraft.game.classcombat.Skill.Assassin.BackStab;
import mineplex.minecraft.game.classcombat.Skill.Assassin.Blink;
import mineplex.minecraft.game.classcombat.Skill.Assassin.ComboAttack;
import mineplex.minecraft.game.classcombat.Skill.Assassin.Evade;
import mineplex.minecraft.game.classcombat.Skill.Assassin.Flash;
import mineplex.minecraft.game.classcombat.Skill.Assassin.Illusion;
import mineplex.minecraft.game.classcombat.Skill.Assassin.Leap;
import mineplex.minecraft.game.classcombat.Skill.Assassin.MarkedForDeath;
import mineplex.minecraft.game.classcombat.Skill.Assassin.Recall;
import mineplex.minecraft.game.classcombat.Skill.Assassin.ShockingStrikes;
import mineplex.minecraft.game.classcombat.Skill.Assassin.SilencingArrow;
import mineplex.minecraft.game.classcombat.Skill.Assassin.SmokeArrow;
import mineplex.minecraft.game.classcombat.Skill.Assassin.SmokeBomb;
import mineplex.minecraft.game.classcombat.Skill.Assassin.ViperStrikes;
import mineplex.minecraft.game.classcombat.Skill.Brute.BlockToss;
import mineplex.minecraft.game.classcombat.Skill.Brute.Bloodlust;
import mineplex.minecraft.game.classcombat.Skill.Brute.Brute;
import mineplex.minecraft.game.classcombat.Skill.Brute.Colossus;
import mineplex.minecraft.game.classcombat.Skill.Brute.CripplingBlow;
import mineplex.minecraft.game.classcombat.Skill.Brute.DwarfToss;
import mineplex.minecraft.game.classcombat.Skill.Brute.FleshHook;
import mineplex.minecraft.game.classcombat.Skill.Brute.Intimidation;
import mineplex.minecraft.game.classcombat.Skill.Brute.Overwhelm;
import mineplex.minecraft.game.classcombat.Skill.Brute.SeismicSlam;
import mineplex.minecraft.game.classcombat.Skill.Brute.Stampede;
import mineplex.minecraft.game.classcombat.Skill.Brute.Takedown;
import mineplex.minecraft.game.classcombat.Skill.Brute.WhirlwindAxe;
import mineplex.minecraft.game.classcombat.Skill.Global.BreakFall;
import mineplex.minecraft.game.classcombat.Skill.Global.Fitness;
import mineplex.minecraft.game.classcombat.Skill.Global.Recharge;
import mineplex.minecraft.game.classcombat.Skill.Global.Resistance;
import mineplex.minecraft.game.classcombat.Skill.Knight.AxeThrow;
import mineplex.minecraft.game.classcombat.Skill.Knight.BullsCharge;
import mineplex.minecraft.game.classcombat.Skill.Knight.Cleave;
import mineplex.minecraft.game.classcombat.Skill.Knight.DefensiveStance;
import mineplex.minecraft.game.classcombat.Skill.Knight.Deflection;
import mineplex.minecraft.game.classcombat.Skill.Knight.Fortitude;
import mineplex.minecraft.game.classcombat.Skill.Knight.HiltSmash;
import mineplex.minecraft.game.classcombat.Skill.Knight.HoldPosition;
import mineplex.minecraft.game.classcombat.Skill.Knight.Knight;
import mineplex.minecraft.game.classcombat.Skill.Knight.LevelField;
import mineplex.minecraft.game.classcombat.Skill.Knight.Riposte;
import mineplex.minecraft.game.classcombat.Skill.Knight.ShieldSmash;
import mineplex.minecraft.game.classcombat.Skill.Knight.Swordsmanship;
import mineplex.minecraft.game.classcombat.Skill.Knight.Vengeance;
import mineplex.minecraft.game.classcombat.Skill.Mage.ArcticArmor;
import mineplex.minecraft.game.classcombat.Skill.Mage.Blizzard;
import mineplex.minecraft.game.classcombat.Skill.Mage.FireBlast;
import mineplex.minecraft.game.classcombat.Skill.Mage.Fissure;
import mineplex.minecraft.game.classcombat.Skill.Mage.GlacialBlade;
import mineplex.minecraft.game.classcombat.Skill.Mage.IcePrison;
import mineplex.minecraft.game.classcombat.Skill.Mage.Immolate;
import mineplex.minecraft.game.classcombat.Skill.Mage.Inferno;
import mineplex.minecraft.game.classcombat.Skill.Mage.LifeBonds;
import mineplex.minecraft.game.classcombat.Skill.Mage.LightningOrb;
import mineplex.minecraft.game.classcombat.Skill.Mage.Mage;
import mineplex.minecraft.game.classcombat.Skill.Mage.MagmaBlade;
import mineplex.minecraft.game.classcombat.Skill.Mage.NullBlade;
import mineplex.minecraft.game.classcombat.Skill.Mage.Rupture;
import mineplex.minecraft.game.classcombat.Skill.Mage.StaticLazer;
import mineplex.minecraft.game.classcombat.Skill.Mage.Void;
import mineplex.minecraft.game.classcombat.Skill.Ranger.Agility;
import mineplex.minecraft.game.classcombat.Skill.Ranger.BarbedArrows;
import mineplex.minecraft.game.classcombat.Skill.Ranger.Barrage;
import mineplex.minecraft.game.classcombat.Skill.Ranger.Disengage;
import mineplex.minecraft.game.classcombat.Skill.Ranger.ExplosiveShot;
import mineplex.minecraft.game.classcombat.Skill.Ranger.HealingShot;
import mineplex.minecraft.game.classcombat.Skill.Ranger.HeavyArrows;
import mineplex.minecraft.game.classcombat.Skill.Ranger.Longshot;
import mineplex.minecraft.game.classcombat.Skill.Ranger.NapalmShot;
import mineplex.minecraft.game.classcombat.Skill.Ranger.Overcharge;
import mineplex.minecraft.game.classcombat.Skill.Ranger.PinDown;
import mineplex.minecraft.game.classcombat.Skill.Ranger.Ranger;
import mineplex.minecraft.game.classcombat.Skill.Ranger.RopedArrow;
import mineplex.minecraft.game.classcombat.Skill.Ranger.Sharpshooter;
import mineplex.minecraft.game.classcombat.Skill.Ranger.VitalitySpores;
import mineplex.minecraft.game.classcombat.Skill.Ranger.WolfsFury;
import mineplex.minecraft.game.classcombat.Skill.Ranger.WolfsPounce;
import mineplex.minecraft.game.classcombat.Skill.repository.SkillRepository;
import mineplex.minecraft.game.classcombat.Skill.repository.token.SkillToken;
import mineplex.minecraft.game.core.IRelation;
import mineplex.minecraft.game.core.combat.CombatManager;
import mineplex.minecraft.game.core.condition.ConditionManager;
import mineplex.minecraft.game.core.damage.DamageManager;
import mineplex.minecraft.game.core.fire.Fire;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.plugin.java.JavaPlugin;

public class SkillFactory extends MiniPlugin implements ISkillFactory
{
	private DamageManager _damageManager;
	private IRelation _relation;
	private CombatManager _combatManager;
	private ConditionManager _conditionManager;
	private ProjectileManager _projectileManager;
	private DisguiseManager _disguiseManager;
	private BlockRestore _blockRestore;
	private Fire _fire;
	private Movement _movement;
	private Teleport _teleport;
	private Energy _energy;
	private SkillRepository _repository;
	private HashMap<String, Skill> _skillMap;
	private HashMap<Integer, ISkill> _skillSalesPackageMap;

	public SkillFactory(JavaPlugin plugin, DamageManager damageManager, IRelation relation,
						CombatManager combatManager, ConditionManager conditionManager, ProjectileManager projectileManager, DisguiseManager disguiseManager,
						BlockRestore blockRestore, Fire fire, Movement movement, Teleport teleport, Energy energy)
	{
		super("Skill Factory", plugin);
		
		_repository = new SkillRepository();
		_damageManager = damageManager;
		_relation = relation;
		_combatManager = combatManager;
		_conditionManager = conditionManager;
		_projectileManager = projectileManager;
		_blockRestore = blockRestore;
		_disguiseManager = disguiseManager;
		_fire = fire;
		_movement = movement;
		_teleport = teleport;
		_energy = energy;
		_skillMap = new HashMap<>();
		_skillSalesPackageMap = new HashMap<>();

		PopulateSkills();
	}
	
	public ConditionManager Condition()
	{
		return _conditionManager;
	}
	
	public Teleport Teleport()
	{
		return _teleport;
	}
	
	public Energy Energy()
	{
		return _energy;
	}

	private void PopulateSkills()
	{
		_skillMap.clear();

		AddAssassin();
		AddBrute();
		AddKnight();
		AddMage();
		AddRanger();
		//AddShifter();
		AddGlobal();

		for (Skill skill : _skillMap.values())
			getPlugin().getServer().getPluginManager().registerEvents(skill, getPlugin());

		List<SkillToken> skillTokens = new ArrayList<>();

		for (Skill skill : _skillMap.values())
		{
			for (int i=0; i < 1; i++)
			{
				SkillToken skillToken = new SkillToken();

				skillToken.Name = skill.GetName();				
				skillToken.Level = i + 1;
				skillToken.SalesPackage = new GameSalesPackageToken();
				skillToken.SalesPackage.Gems = 2000;

				skillTokens.add(skillToken);
			}
		}

		for (SkillToken skillToken : _repository.GetSkills(skillTokens))
		{
			if (_skillMap.containsKey(skillToken.Name))
			{
				Skill skill = _skillMap.get(skillToken.Name);
				_skillSalesPackageMap.put(skillToken.SalesPackage.GameSalesPackageId, skill);
				_skillMap.get(skillToken.Name).Update(skillToken);
			}
		}
	}

	public void AddGlobal()
	{
		//Passive C
		AddSkill(new BreakFall(this, "Break Fall", ClassType.Global, SkillType.GlobalPassive, 1, 3));	
		AddSkill(new Resistance(this, "Resistance", ClassType.Global, SkillType.GlobalPassive, 1, 3));
		//AddSkill(new Cooldown(this, "Quick Recovery", ClassType.Global, SkillType.GlobalPassive, 1, 3));
		//AddSkill(new Rations(this, "Rations", ClassType.Global, SkillType.GlobalPassive, 1, 2));
		
		AddSkill(new Fitness(this, "Mana Pool", ClassType.Mage, SkillType.GlobalPassive, 1, 3));
		AddSkill(new Recharge(this, "Mana Regeneration", ClassType.Mage, SkillType.GlobalPassive, 1, 3));
		
		//AddSkill(new Stamina(this, "Stamina", ClassType.Global, SkillType.GlobalPassive, 1, 1));
		//AddSkill(new Swim(this, "Swim", ClassType.Global, SkillType.GlobalPassive, 1, 1));
	}

	public void AddAssassin()
	{
		AddSkill(new Assassin(this, "Assassin Class", ClassType.Assassin, SkillType.Class, 0, 1));

		//Sword
		AddSkill(new Evade(this, "Evade", ClassType.Assassin, SkillType.Sword, 
				2, 1, 
				0, 0, 
				2500, -500, true,
				new Material[] {Material.IRON_SWORD, Material.GOLD_SWORD, Material.DIAMOND_SWORD}, 
				new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));
		
		AddSkill(new Illusion(this, "Illusion", ClassType.Assassin, SkillType.Sword, 
				1, 4, 
				0, 0, 
				17000, -1000, true,
				new Material[] {Material.IRON_SWORD, Material.GOLD_SWORD, Material.DIAMOND_SWORD}, 
				new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));

		//Axe
		AddSkill(new Blink(this, "Blink", ClassType.Assassin, SkillType.Axe, 
				1, 4, 
				0, 0, 
				9000, 1000, true,
				new Material[] {Material.IRON_AXE, Material.GOLD_AXE, Material.DIAMOND_AXE}, 
				new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));

		AddSkill(new Flash(this, "Flash", ClassType.Assassin, SkillType.Axe, 
				1, 4, 
				0, 0, 
				0, 0, true,
				new Material[] {Material.IRON_AXE, Material.GOLD_AXE, Material.DIAMOND_AXE}, 
				new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));

		AddSkill(new Leap(this, "Leap", ClassType.Assassin, SkillType.Axe, 
				1, 4,
				0, 0, 
				10500, -1500, true,
				new Material[] {Material.IRON_AXE, Material.GOLD_AXE, Material.DIAMOND_AXE}, 
				new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));

		//Bow
		AddSkill(new MarkedForDeath(this, "Marked for Death", ClassType.Assassin, SkillType.Bow, 
				1, 4,
				0, 0, 
				20000, -2000, true,
				new Material[] {Material.BOW}, 
				new Action[] {Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK}));

		AddSkill(new SmokeArrow(this, "Smoke Arrow", ClassType.Assassin, SkillType.Bow, 
				1, 4,
				0, 0, 
				20000, -2000, true,
				new Material[] {Material.BOW}, 
				new Action[] {Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK}));

		AddSkill(new SilencingArrow(this, "Silencing Arrow", ClassType.Assassin, SkillType.Bow, 	
				1, 4,
				0, 0,
				20000, -3000, true,
				new Material[] {Material.BOW}, 
				new Action[] {Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK}));


		//Passive A
		AddSkill(new SmokeBomb(this, "Smoke Bomb", ClassType.Assassin, SkillType.PassiveA, 1, 3));
		AddSkill(new Recall(this, "Recall", ClassType.Assassin, SkillType.PassiveA, 1, 3));
		//AddSkill(new Stealth(this, "Stealth", ClassType.Assassin, SkillType.PassiveA, 5, 3));

		//Passive B
		AddSkill(new ShockingStrikes(this, "Shocking Strikes", ClassType.Assassin, SkillType.PassiveB, 1, 3));
		AddSkill(new ComboAttack(this, "Combo Attack", ClassType.Assassin, SkillType.PassiveB, 1, 3));
		AddSkill(new ViperStrikes(this, "Viper Strikes", ClassType.Assassin, SkillType.PassiveB, 1, 3));
		AddSkill(new BackStab(this, "Backstab", ClassType.Assassin, SkillType.PassiveB, 1, 3));
	}

	public void AddBrute()
	{
		AddSkill(new Brute(this, "Brute Class", ClassType.Brute, SkillType.Class, 0, 1));

		//Sword
		AddSkill(new DwarfToss(this, "Dwarf Toss", ClassType.Brute, SkillType.Sword, 
				2, 1,
				0, 0, 
				16000, 0, true,
				new Material[] {Material.IRON_SWORD, Material.GOLD_SWORD, Material.DIAMOND_SWORD}, 
				new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));

		AddSkill(new FleshHook(this, "Flesh Hook", ClassType.Brute, SkillType.Sword, 
				1, 5,
				0, 0, 
				15000, -1000, true,
				new Material[] {Material.IRON_SWORD, Material.GOLD_SWORD, Material.DIAMOND_SWORD}, 
				new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));

		AddSkill(new BlockToss(this, "Block Toss", ClassType.Brute, SkillType.Sword, 1, 5));

		//Axe
		AddSkill(new SeismicSlam(this, "Seismic Slam", ClassType.Brute, SkillType.Axe, 
				1, 5,
				0, 0, 
				21000, -1000, true,
				new Material[] {Material.IRON_AXE, Material.GOLD_AXE, Material.DIAMOND_AXE}, 
				new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));

		AddSkill(new Takedown(this, "Takedown", ClassType.Brute, SkillType.Axe, 
				1, 5,
				0, 0, 
				17000, -1000, true,
				new Material[] {Material.IRON_AXE, Material.GOLD_AXE, Material.DIAMOND_AXE}, 
				new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));
		
		AddSkill(new WhirlwindAxe(this, "Whirlwind Axe", ClassType.Brute, SkillType.Axe, 
				1, 5,
				0, 0, 
				21000, -1000, true,
				new Material[] {Material.IRON_AXE, Material.GOLD_AXE, Material.DIAMOND_AXE}, 
				new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));

		//Passive A
		AddSkill(new Stampede(this, "Stampede", ClassType.Brute, SkillType.PassiveA, 1, 3));
		AddSkill(new Bloodlust(this, "Bloodlust", ClassType.Brute, SkillType.PassiveA, 1, 3));
		AddSkill(new Intimidation(this, "Intimidation", ClassType.Brute, SkillType.PassiveA, 1, 3));

		//Passive B
		AddSkill(new CripplingBlow(this, "Crippling Blow", ClassType.Brute, SkillType.PassiveB, 2, 1));
		AddSkill(new Colossus(this, "Colossus", ClassType.Brute, SkillType.PassiveB, 2, 1));
		AddSkill(new Overwhelm(this, "Overwhelm", ClassType.Brute, SkillType.PassiveB, 1, 3));
	}

	public void AddKnight()
	{
		AddSkill(new Knight(this, "Knight Class", ClassType.Knight, SkillType.Class, 0, 1));

		//Sword
		AddSkill(new HiltSmash(this, "Hilt Smash", ClassType.Knight, SkillType.Sword,
				1, 5,
				0, 0, 
				15000, -1000, true,
				new Material[] {Material.IRON_SWORD, Material.GOLD_SWORD, Material.DIAMOND_SWORD}, 
				new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));

		AddSkill(new Riposte(this, "Riposte", ClassType.Knight, SkillType.Sword, 
				1, 5,
				0, 0, 
				15000, -1000, false,
				new Material[] {Material.IRON_SWORD, Material.GOLD_SWORD, Material.DIAMOND_SWORD}, 
				new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));

		AddSkill(new DefensiveStance(this, "Defensive Stance", ClassType.Knight, SkillType.Sword, 
				2, 1,
				0, 0, 
				0, 0, true,
				new Material[] {Material.IRON_SWORD, Material.GOLD_SWORD, Material.DIAMOND_SWORD}, 
				new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));


		//Axe
		AddSkill(new BullsCharge(this, "Bulls Charge", ClassType.Knight, SkillType.Axe, 
				1, 5,
				0, 0, 
				10000, 1000, true,
				new Material[] {Material.IRON_AXE, Material.GOLD_AXE, Material.DIAMOND_AXE}, 
				new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));

		AddSkill(new HoldPosition(this, "Hold Position", ClassType.Knight, SkillType.Axe, 
				1, 5,
				0, 0, 
				16000, 2000, true,
				new Material[] {Material.IRON_AXE, Material.GOLD_AXE, Material.DIAMOND_AXE}, 
				new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));
		
		AddSkill(new ShieldSmash(this, "Shield Smash", ClassType.Knight, SkillType.Axe, 
				1, 5,
				0, 0, 
				15000, -1000, true,
				new Material[] {Material.IRON_AXE, Material.GOLD_AXE, Material.DIAMOND_AXE}, 
				new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));
		
		AddSkill(new AxeThrow(this, "Roped Axe Throw", ClassType.Knight, SkillType.Axe, 
				1, 5,
				0, 0, 
				4300, -300, true,
				new Material[] {Material.IRON_AXE, Material.GOLD_AXE, Material.DIAMOND_AXE}, 
				new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));


		//Passive A
		AddSkill(new Cleave(this, "Cleave", ClassType.Knight, SkillType.PassiveA, 1, 3));
		AddSkill(new Swordsmanship(this, "Swordsmanship", ClassType.Knight, SkillType.PassiveA, 1, 3));
		AddSkill(new Deflection(this, "Deflection", ClassType.Knight, SkillType.PassiveA, 1, 3));

		//Passive B
		AddSkill(new Vengeance(this, "Vengeance", ClassType.Knight, SkillType.PassiveB, 1, 3));
		AddSkill(new Fortitude(this, "Fortitude", ClassType.Knight, SkillType.PassiveB, 1, 3));
		AddSkill(new LevelField(this, "Level Field", ClassType.Knight, SkillType.PassiveB, 1, 3));
	}

	public void AddMage()
	{
		AddSkill(new Mage(this, "Mage Class", ClassType.Mage, SkillType.Class, 0, 1));

		//Sword
		AddSkill(new Blizzard(this, "Blizzard", ClassType.Mage, SkillType.Sword, 
				1, 5,
				0, 0, 
				0, 0, true,
				new Material[] {Material.IRON_SWORD, Material.GOLD_SWORD, Material.DIAMOND_SWORD}, 
				new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));

		AddSkill(new Inferno(this, "Inferno", ClassType.Mage, SkillType.Sword, 
				1, 5,
				0, 0, 
				0, 0, true,
				new Material[] {Material.IRON_SWORD, Material.GOLD_SWORD, Material.DIAMOND_SWORD}, 
				new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));

		AddSkill(new Rupture(this, "Rupture", ClassType.Mage, SkillType.Sword, 
				1, 5,
				0, 0, 
				0, 0, true,
				new Material[] {Material.IRON_SWORD, Material.GOLD_SWORD, Material.DIAMOND_SWORD}, 
				new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));

		AddSkill(new StaticLazer(this, "Static Lazer", ClassType.Mage, SkillType.Sword, 1, 5));

		//Axe
		AddSkill(new FireBlast(this, "Fire Blast", ClassType.Mage, SkillType.Axe, 
				1, 5,
				54, -4,
				13000, -1000, true,
				new Material[] {Material.IRON_AXE, Material.GOLD_AXE, Material.DIAMOND_AXE}, 
				new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));

		AddSkill(new IcePrison(this, "Ice Prison", ClassType.Mage, SkillType.Axe, 
				1, 5,
				60, -3, 
				21000, -1000, true,
				new Material[] {Material.IRON_AXE, Material.GOLD_AXE, Material.DIAMOND_AXE}, 
				new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));
		
		AddSkill(new LightningOrb(this, "Lightning Orb", ClassType.Mage, SkillType.Axe, 
				1, 5,
				60, -2, 
				13000, -1000, true,
				new Material[] {Material.IRON_AXE, Material.GOLD_AXE, Material.DIAMOND_AXE}, 
				new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));

		
		AddSkill(new Fissure(this, "Fissure", ClassType.Mage, SkillType.Axe, 
				1, 5,
				53, -3,
				13000, -1000, true,
				new Material[] {Material.IRON_AXE, Material.GOLD_AXE, Material.DIAMOND_AXE}, 
				new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));

		/*
		AddSkill(new FreezingBlast(this, "Freezing Blast", ClassType.Mage, SkillType.Axe, 
				5, 5,
				40, -2, 
				4000, 0, true,
				new Material[] {Material.IRON_AXE, Material.GOLD_AXE, Material.DIAMOND_AXE}, 
				new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));
		 */
		
		// AddSkill(new Tundra(this, "Tundra", ClassType.Mage, SkillType.Axe, 830, 200, 1));


		//Passive A
		AddSkill(new ArcticArmor(this, "Arctic Armor", ClassType.Mage, SkillType.PassiveA, 1, 3));
		AddSkill(new Immolate(this, "Immolate", ClassType.Mage, SkillType.PassiveA, 2, 1));
		AddSkill(new Void(this, "Void", ClassType.Mage, SkillType.PassiveA, 1, 3));
		AddSkill(new LifeBonds(this, "Life Bonds", ClassType.Mage, SkillType.PassiveA, 1, 3));

		//Passive B
		AddSkill(new GlacialBlade(this, "Glacial Blade", ClassType.Mage, SkillType.PassiveB, 
				1, 3,
				11, -2,
				1000, -100, false,
				new Material[] {Material.IRON_SWORD, Material.GOLD_SWORD, Material.DIAMOND_SWORD}, 
				new Action[] {Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK}));

		AddSkill(new MagmaBlade(this, "Magma Blade", ClassType.Mage, SkillType.PassiveB, 1, 3));
		AddSkill(new NullBlade(this, "Null Blade", ClassType.Mage, SkillType.PassiveB, 1, 3));
	}

	public void AddRanger()
	{
		AddSkill(new Ranger(this, "Ranger Class", ClassType.Ranger, SkillType.Class, 0, 1));

		//Sword
		AddSkill(new Disengage(this, "Disengage", ClassType.Ranger, SkillType.Sword, 
				1, 4,
				0, 0, 
				16000, -1000, true,
				new Material[] {Material.IRON_SWORD, Material.GOLD_SWORD, Material.DIAMOND_SWORD}, 
				new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));

		AddSkill(new WolfsPounce(this, "Wolfs Pounce", ClassType.Ranger, SkillType.Sword, 1, 4));

		//Axe
		AddSkill(new Agility(this, "Agility", ClassType.Ranger, SkillType.Axe, 
				1, 4,
				0, 0, 
				14000, 1000, true,
				new Material[] {Material.IRON_AXE, Material.GOLD_AXE, Material.DIAMOND_AXE}, 
				new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));

		AddSkill(new WolfsFury(this, "Wolfs Fury", ClassType.Ranger, SkillType.Axe, 
				1, 4,
				0, 0, 
				17000, 2000, true,
				new Material[] {Material.IRON_AXE, Material.GOLD_AXE, Material.DIAMOND_AXE}, 
				new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));

		//Bow
		AddSkill(new HealingShot(this, "Healing Shot", ClassType.Ranger, SkillType.Bow, 
				1, 4,
				0, 0, 
				20000, -3000, true,
				new Material[] {Material.BOW}, 
				new Action[] {Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK}));

//		AddSkill(new IncendiaryShot(this, "Incendiary Shot", ClassType.Ranger, SkillType.Bow, 
//				1, 4,
//				0, 0,
//				20000, -2000, true,
//				new Material[] {Material.BOW}, 
//				new Action[] {Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK}));

		AddSkill(new NapalmShot(this, "Napalm Shot", ClassType.Ranger, SkillType.Bow, 				
				1, 4,
				0, 0, 
				30000, -2000, true,
				new Material[] {Material.BOW},  
				new Action[] {Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK}));

		AddSkill(new PinDown(this, "Pin Down", ClassType.Ranger, SkillType.Bow, 
				1, 4,
				0, 0, 
				13000, -1000, true,
				new Material[] {Material.BOW}, 
				new Action[] {Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK}));

		AddSkill(new RopedArrow(this, "Roped Arrow", ClassType.Ranger, SkillType.Bow, 
				1, 4,
				0, 0, 
				9000, -1000, false,
				new Material[] {Material.BOW}, 
				new Action[] {Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK}));
		
		AddSkill(new ExplosiveShot(this, "Explosive Arrow", ClassType.Ranger, SkillType.Bow, 
				1, 4,
				0, 0, 
				22000, -2000, false,
				new Material[] {Material.BOW}, 
				new Action[] {Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK}));

		//Passive A
		AddSkill(new Barrage(this, "Barrage", ClassType.Ranger, SkillType.PassiveA, 1, 3));
		AddSkill(new Overcharge(this, "Overcharge", ClassType.Ranger, SkillType.PassiveA, 1, 3));
		AddSkill(new VitalitySpores(this, "Vitality Spores", ClassType.Ranger, SkillType.PassiveA, 1, 3));

		//Passive B	
		AddSkill(new BarbedArrows(this, "Barbed Arrows", ClassType.Ranger, SkillType.PassiveB, 1, 3));
		AddSkill(new HeavyArrows(this, "Heavy Arrows", ClassType.Ranger, SkillType.PassiveB, 1, 3));
		AddSkill(new Longshot(this, "Longshot", ClassType.Ranger, SkillType.PassiveB, 1, 3));
		AddSkill(new Sharpshooter(this, "Sharpshooter", ClassType.Ranger, SkillType.PassiveB, 1, 3));
		//AddSkill(new Shadowmeld(this, "Shadowmeld", ClassType.Ranger, SkillType.PassiveB, 5, 3));
		//AddSkill(new Fletcher(this, "Fletcher", ClassType.Ranger, SkillType.PassiveB, 5, 3));
	}

	public ISkill GetSkillBySalesPackageId(int id)
	{
		return _skillSalesPackageMap.get(id);
	}

	public Skill GetSkill(String skillName)
	{
		return _skillMap.get(skillName);
	}

	public Collection<Skill> GetAllSkills()
	{
		return _skillMap.values();
	}

	public void AddSkill(Skill skill)
	{
		_skillMap.put(skill.GetName(), skill);
	}

	public void RemoveSkill(String skillName, String defaultReplacement)
	{
		if (skillName == null)
		{
			System.out.println("[Skill Factory] Remove Skill: Remove Skill NULL [" + skillName + "].");
			return;
		}

		Skill remove = _skillMap.get(skillName);
		if (remove == null)
		{
			System.out.println("[Skill Factory] Remove Skill: Remove Skill NULL [" + skillName + "].");
			return;
		}

		Skill replacement = null;
		if (defaultReplacement != null)
		{
			replacement = _skillMap.get(defaultReplacement);
			if (replacement == null)
			{
				System.out.println("[Skill Factory] Remove Skill: Replacement Skill NULL [" + defaultReplacement + "].");
				return;
			}
		}

		//Remove
		_skillMap.remove(remove.GetName());
		HandlerList.unregisterAll(remove);

		System.out.println("Skill Factory: Removed " + remove.GetName() + " from SkillMap.");
	}

	public void removeSkill(String skillName)
	{
		RemoveSkill(skillName, null);
	}

	@Override
	public List<ISkill> GetGlobalSkillsFor(IPvpClass gameClass) 
	{
		List<ISkill> skills = new LinkedList<ISkill>();

		for (ISkill cur : _skillMap.values())
		{
			if (cur.GetSkillType() == SkillType.GlobalPassive && (cur.GetClassType() == ClassType.Global || (gameClass != null && cur.GetClassType() == gameClass.GetType())))
			{
				skills.add(cur);
			}
		}

		return skills;
	}

	@Override
	public List<ISkill> GetSkillsFor(IPvpClass gameClass) 
	{
		List<ISkill> skills = new LinkedList<ISkill>();

		for (ISkill cur : _skillMap.values())
		{
			if (cur.GetClassType() == gameClass.GetType() && cur.GetSkillType() != SkillType.GlobalPassive)
			{
				skills.add(cur);
			}
		}

		return skills;
	}

	//Called once, upon Class creation.
	@Override
	public HashMap<ISkill, Integer> GetDefaultSkillsFor(IPvpClass classType) 
	{
		HashMap<ISkill, Integer> skills = new HashMap<ISkill, Integer>();
		if (classType.GetType() == ClassType.Knight)
		{
			AddSkill(skills, "Knight Class", 1);         	 //Class

			AddSkill(skills, "Bulls Charge", 1);			//Axe
			AddSkill(skills, "Riposte", 1);					//Sword
			AddSkill(skills, "Deflection", 1);				//Passive A
			AddSkill(skills, "Vengeance", 1);				//Passive B

			AddSkill(skills, "Resistance", 1);				//Passive C
		}

		else if (classType.GetType() == ClassType.Ranger)
		{
			AddSkill(skills, "Ranger Class", 1);          	//Class

			AddSkill(skills, "Napalm Shot", 1);				//Bow
			AddSkill(skills, "Agility", 1);					//Axe
			AddSkill(skills, "Disengage", 1);				//Sword
			AddSkill(skills, "Barrage", 1);					//Passive A
			AddSkill(skills, "Barbed Arrows", 1);			//Passive B

			AddSkill(skills, "Quick Recovery", 1);			//Passive D
		}

		else if (classType.GetType() == ClassType.Brute)
		{
			AddSkill(skills, "Brute Class", 1);              //Class

			AddSkill(skills, "Seismic Slam", 1);			//Axe
			AddSkill(skills, "Dwarf Toss", 1);				//Sword
			AddSkill(skills, "Stampede", 1);				//Passive A
			AddSkill(skills, "Crippling Blow", 1);			//Passive B

			AddSkill(skills, "Resistance", 1);				//Passive C
		}

		else if (classType.GetType() == ClassType.Assassin)
		{
			AddSkill(skills, "Assassin Class", 1);          //Class

			AddSkill(skills, "Blink", 1);					//Axe
			AddSkill(skills, "Evade", 1);					//Sword
			AddSkill(skills, "Toxic Arrow", 1);				//Bow
			AddSkill(skills, "Smoke Bomb", 1);				//Passive A
			AddSkill(skills, "Repeated Strikes", 1);		//Passive B

			AddSkill(skills, "Break Fall", 1);				//Passive C
		}

		else if (classType.GetType() == ClassType.Mage)
		{
			AddSkill(skills, "Mage Class", 1);              //Class

			AddSkill(skills, "Freezing Blast", 1);			//Axe
			AddSkill(skills, "Blizzard", 1);				//Sword
			AddSkill(skills, "Arctic Armor", 1);			//Passive A
			AddSkill(skills, "Glacial Blade", 1);			//Passive B

			AddSkill(skills, "Fitness", 1);					//Passive D
		}

		else if (classType.GetType() == ClassType.Shifter)
		{
			AddSkill(skills, "Shifter Class", 1);      		//Class

			AddSkill(skills, "Tree Shift", 1);				//Axe
			AddSkill(skills, "Polysmash", 1);				//Sword
			AddSkill(skills, "Golem Form", 1);				//Passive A
			AddSkill(skills, "Chicken Form", 1);			//Passive B

			AddSkill(skills, "Quick Recovery", 1);			//Passive D
		}

		skills.remove(null);

		return skills;
	}

	public void AddSkill(HashMap<ISkill, Integer> skills, String skillName, int level)
	{
		ISkill skill = GetSkill(skillName);

		if (skill == null)
			return;

		skills.put(skill, level);
	}

	public Movement Movement()
	{
		return _movement;
	}
	
	public DamageManager Damage()
	{
		return _damageManager;
	}
	
	public CombatManager Combat()
	{
		return _combatManager;
	}
	
	public ProjectileManager Projectile()
	{
		return _projectileManager;
	}
	
	public BlockRestore BlockRestore()
	{
		return _blockRestore;
	}
	
	public DisguiseManager Disguise()
	{
		return _disguiseManager;
	}
	
	public Fire Fire()
	{
		return _fire;
	}
	
	public IRelation Relation()
	{
		return _relation;
	}
	
	public void ResetAll()
	{
		for (ISkill skill : _skillMap.values())
			for (Player player : skill.GetUsers())
				skill.Reset(player);
	}
	
	@Override
	public void registerSelf()
	{
		registerEvents(this);
		
		for (Skill skill : _skillMap.values())
			registerEvents(skill);
	}
	
	@Override
	public void deregisterSelf()
	{
		HandlerList.unregisterAll(this);
		
		for (Skill skill : _skillMap.values())
			HandlerList.unregisterAll(skill);
	}
}
