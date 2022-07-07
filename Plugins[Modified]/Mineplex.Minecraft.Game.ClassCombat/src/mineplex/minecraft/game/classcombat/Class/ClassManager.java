package mineplex.minecraft.game.classcombat.Class;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;

import mineplex.core.MiniClientPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.event.ClientWebResponseEvent;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.donation.DonationManager;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Class.repository.ClassRepository;
import mineplex.minecraft.game.classcombat.Class.repository.token.ClientClassTokenWrapper;
import mineplex.minecraft.game.classcombat.Class.repository.token.CustomBuildToken;
import mineplex.minecraft.game.classcombat.Skill.ISkill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.classcombat.item.ItemFactory;

public class ClassManager extends MiniClientPlugin<ClientClass> implements IClassFactory
{
	public enum Perm implements Permission
	{
		VIEW_OTHER_SKILLS,
	}

	private CoreClientManager _clientManager;
	private DonationManager _donationManager;
	private SkillFactory _skillFactory;
	private ItemFactory _itemFactory;
	private ClassRepository _repository;
	private GadgetManager _gadgetManager;
	private Map<String, IPvpClass> _classes;
	private Map<Integer, IPvpClass> _classSalesPackageIdMap;

	private final Object _clientLock = new Object();
	
	private boolean _enabled = true;
	
	private Map<String, Callback<String>> _messageSuppressed;

	public ClassManager(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager,
						SkillFactory skillFactory, ItemFactory itemFactory)
	{
		super("Class Manager", plugin);

		_plugin = plugin;
		_clientManager = clientManager;
		_donationManager = donationManager;
		_skillFactory = skillFactory;
		_itemFactory = itemFactory;
		_repository = new ClassRepository();
		_classes = new HashMap<>();
		_classSalesPackageIdMap = new HashMap<>();
		_messageSuppressed = new ConcurrentHashMap<>();

		PopulateClasses();
		
		generatePermissions();
	}
	
	public ClassManager(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager, GadgetManager gadgetManager,
						SkillFactory skillFactory, ItemFactory itemFactory)
	{
		super("Class Manager", plugin);

		_plugin = plugin;
		_clientManager = clientManager;
		_donationManager = donationManager;
		_gadgetManager = gadgetManager;
		_skillFactory = skillFactory;
		_itemFactory = itemFactory;
		_repository = new ClassRepository();
		_classes = new HashMap<>();
		_classSalesPackageIdMap = new HashMap<>();
		_messageSuppressed = new ConcurrentHashMap<>();

		PopulateClasses();
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.MOD.setPermission(Perm.VIEW_OTHER_SKILLS, true, true);
	}

	@EventHandler
	public void OnClientWebResponse(ClientWebResponseEvent event)
	{
		ClientClassTokenWrapper token = new Gson().fromJson(event.GetResponse(), ClientClassTokenWrapper.class);
		LoadClassBuilds(token, event.getUniqueId());
	}

	private void LoadClassBuilds(ClientClassTokenWrapper token, UUID uuid)
	{
		synchronized (_clientLock)
		{
			Set(uuid,
					new ClientClass(this, _skillFactory, _itemFactory, _clientManager.Get(uuid), _donationManager
							.Get(uuid), token.DonorToken));
		}
	}

	private void PopulateClasses()
	{
		_classes.clear();

		AddKnight();
		AddRanger();
		AddBrute();
		AddMage();
		AddAssassin();

		/*
		 * AddClass(new PvpClass(this, 6, ClassType.Shapeshifter, new String[] {
		 * "Able to transform into various creatures." },
		 * Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE,
		 * Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, Color.fromRGB(20,
		 * 100, 0)));
		 */
		
		for (IPvpClass pvpClass : GetAllClasses())
		{
			CustomBuildToken customBuild = pvpClass.getDefaultBuild();
			ISkill swordSkill = _skillFactory.GetSkill(customBuild.SwordSkill);
			ISkill axeSkill = _skillFactory.GetSkill(customBuild.AxeSkill);
			ISkill bowSkill = _skillFactory.GetSkill(customBuild.BowSkill);
			ISkill classPassiveASkill = _skillFactory.GetSkill(customBuild.ClassPassiveASkill);
			ISkill classPassiveBSkill = _skillFactory.GetSkill(customBuild.ClassPassiveBSkill);
			ISkill globalPassive = _skillFactory.GetSkill(customBuild.GlobalPassiveSkill);

			if (swordSkill != null)
				swordSkill.setFree(true);

			if (axeSkill != null)
				axeSkill.setFree(true);

			if (bowSkill != null)
				bowSkill.setFree(true);

			if (classPassiveASkill != null)
				classPassiveASkill.setFree(true);

			if (classPassiveBSkill != null)
				classPassiveBSkill.setFree(true);

			if (globalPassive != null)
				globalPassive.setFree(true);
		}
	}

	private void AddAssassin()
	{
		CustomBuildToken customBuild = new CustomBuildToken(ClassType.Assassin);
		customBuild.Name = "Default Build";

		customBuild.SwordSkill = "Evade";
		customBuild.SwordSkillLevel = 1;
		customBuild.AxeSkill = "Leap";
		customBuild.AxeSkillLevel = 3;
		customBuild.BowSkill = "Smoke Arrow";
		customBuild.BowSkillLevel = 2;
		customBuild.ClassPassiveASkill = "Smoke Bomb";
		customBuild.ClassPassiveASkillLevel = 2;
		customBuild.ClassPassiveBSkill = "Combo Attack";
		customBuild.ClassPassiveBSkillLevel = 2;
		customBuild.GlobalPassiveSkill = "Break Fall";
		customBuild.GlobalPassiveSkillLevel = 1;

		AddClass(new PvpClass(this, 5, ClassType.Assassin, customBuild, new String[] { "Extremely nimble and smart.",
				"Excels at ambushing and takedowns.", "", "Permanent Speed II" }, Material.LEATHER_HELMET,
				Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, null));
	}

	private void AddMage()
	{
		CustomBuildToken customBuild = new CustomBuildToken(ClassType.Mage);
		customBuild.Name = "Default Build";

		customBuild.SwordSkill = "Inferno";
		customBuild.SwordSkillLevel = 3;
		customBuild.AxeSkill = "Fire Blast";
		customBuild.AxeSkillLevel = 3;
		customBuild.BowSkill = "";
		customBuild.ClassPassiveASkill = "Immolate";
		customBuild.ClassPassiveASkillLevel = 1;
		customBuild.ClassPassiveBSkill = "Magma Blade";
		customBuild.ClassPassiveBSkillLevel = 2;
		customBuild.GlobalPassiveSkill = "Break Fall";
		customBuild.GlobalPassiveSkillLevel = 2;

		AddClass(new PvpClass(this, 4, ClassType.Mage, customBuild, new String[] { "Trained in the ancient arts.",
				"Able to adapt to many roles in combat." }, Material.GOLD_HELMET, Material.GOLD_CHESTPLATE,
				Material.GOLD_LEGGINGS, Material.GOLD_BOOTS, null));
	}

	private void AddBrute()
	{
		CustomBuildToken customBuild = new CustomBuildToken(ClassType.Brute);
		customBuild.Name = "Default Build";

		customBuild.SwordSkill = "Block Toss";
		customBuild.SwordSkillLevel = 4;
		customBuild.AxeSkill = "Seismic Slam";
		customBuild.AxeSkillLevel = 3;
		customBuild.BowSkill = "";
		customBuild.BowSkillLevel = 0;
		customBuild.ClassPassiveASkill = "Stampede";
		customBuild.ClassPassiveASkillLevel = 2;
		customBuild.ClassPassiveBSkill = "Crippling Blow";
		customBuild.ClassPassiveBSkillLevel = 1;
		customBuild.GlobalPassiveSkill = "Recharge";
		customBuild.GlobalPassiveSkillLevel = 1;

		AddClass(new PvpClass(this, 3, ClassType.Brute, customBuild, new String[] { "Uses pure strength to dominate.",
				"Great at crowd control." }, Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE,
				Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS, null));
	}

	private void AddRanger()
	{
		CustomBuildToken customBuild = new CustomBuildToken(ClassType.Ranger);
		customBuild.Name = "Default Build";

		customBuild.SwordSkill = "Disengage";
		customBuild.SwordSkillLevel = 3;
		customBuild.AxeSkill = "Agility";
		customBuild.AxeSkillLevel = 2;
		customBuild.BowSkill = "Napalm Shot";
		customBuild.BowSkillLevel = 3;
		customBuild.ClassPassiveASkill = "Barrage";
		customBuild.ClassPassiveASkillLevel = 2;
		customBuild.ClassPassiveBSkill = "Barbed Arrows";
		customBuild.ClassPassiveBSkillLevel = 1;
		customBuild.GlobalPassiveSkill = "Resistance";
		customBuild.GlobalPassiveSkillLevel = 1;

		AddClass(new PvpClass(this, -1, ClassType.Ranger, customBuild, new String[] { "Mastery with a Bow and Arrow.",
				"Adept in Wilderness Survival" }, Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE,
				Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS, null));
	}

	private void AddKnight()
	{
		CustomBuildToken customBuild = new CustomBuildToken(ClassType.Knight);
		customBuild.Name = "Default Build";

		customBuild.SwordSkill = "Hilt Smash";
		customBuild.SwordSkillLevel = 3;
		customBuild.AxeSkill = "Bulls Charge";
		customBuild.AxeSkillLevel = 3;
		customBuild.BowSkill = "";
		customBuild.ClassPassiveASkill = "Swordsmanship";
		customBuild.ClassPassiveASkillLevel = 2;
		customBuild.ClassPassiveBSkill = "Vengeance";
		customBuild.ClassPassiveBSkillLevel = 2;
		customBuild.GlobalPassiveSkill = "Resistance";
		customBuild.GlobalPassiveSkillLevel = 2;

		AddClass(new PvpClass(this, -1, ClassType.Knight, customBuild, new String[] {
				"Trained in the arts of melee combat.", "Able to stand his ground against foes." },
				Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS, null));
	}

	public IPvpClass GetClass(String className)
	{
		return _classes.get(className);
	}

	public IPvpClass GetClass(int id)
	{
		return _classSalesPackageIdMap.get(id);
	}

	public Collection<IPvpClass> GetAllClasses()
	{
		return _classes.values();
	}

	public void AddClass(PvpClass newClass)
	{
		_classes.put(newClass.GetName(), newClass);
	}

	@Override
	public Collection<IPvpClass> GetGameClasses()
	{
		return _classes.values();
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (!_enabled)
			return;
		
		if (event.getType() != UpdateType.FAST)
			return;

		for (IPvpClass cur : _classes.values())
		{
			cur.checkEquip();
		}
	}

	public SkillFactory GetSkillFactory()
	{
		return _skillFactory;
	}
	
	public ItemFactory GetItemFactory()
	{
		return _itemFactory;
	}

	@Override
	protected ClientClass addPlayer(UUID uuid)
	{
		return new ClientClass(this, _skillFactory, _itemFactory, _clientManager.Get(uuid), _donationManager.Get(uuid), null);
	}

	public ClassRepository GetRepository()
	{
		return _repository;
	}
	
	public GadgetManager getGadgetManager()
	{
		return _gadgetManager;
	}

	@EventHandler
	public void SkillDisplay(PlayerCommandPreprocessEvent event)
	{
		if (!_enabled)
			return;
		
		if (event.getMessage().length() < 1)
			return;
		
		String[] args = event.getMessage().toLowerCase().split(" ");
		
		if (args[0].equals("/skill"))
		{
			Player target = event.getPlayer();
			
			//Target Other
			if (args.length > 1 && _clientManager.Get(event.getPlayer()).hasPermission(Perm.VIEW_OTHER_SKILLS))
			{
				target = UtilPlayer.searchOnline(event.getPlayer(), args[1], true);
				
				if (target == null)
				{
					return;
				}
			}
			
			ClientClass client = Get(target.getUniqueId());

			if (client == null)
			{
				event.getPlayer().sendMessage(target.getName() + " does not have a ClientClass.");
			}
			else
			{
				client.DisplaySkills(event.getPlayer());
			}

			event.setCancelled(true);
		}
	}
	
	public void setEnabled(boolean var)
	{
		_enabled = var;
	}
	
	public void hideNextEquipMessage(String name)
	{
		_messageSuppressed.put(name, new Callback<String>()
		{
			public void run(String name)
			{
				_messageSuppressed.remove(name);
			}
		});
	}
	
	public void forceRemoveFromSuppressed(String name)
	{
		_messageSuppressed.remove(name);
	}
	
	public Callback<String> getMessageSuppressedCallback(String name)
	{
		return _messageSuppressed.get(name);
	}
	
	public CoreClientManager getClientManager()
	{
		return _clientManager;
	}
}