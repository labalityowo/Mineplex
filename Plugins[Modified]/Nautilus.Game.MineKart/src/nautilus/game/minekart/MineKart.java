package nautilus.game.minekart;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import mineplex.core.account.CoreClientManager;
import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.command.CommandCenter;
import mineplex.core.common.CurrencyType;
import mineplex.core.creature.Creature;
import mineplex.core.donation.DonationManager;
import mineplex.core.explosion.Explosion;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.memory.MemoryFix;
import mineplex.core.monitor.LagMeter;
import mineplex.core.npc.NpcManager;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.punish.Punish;
import mineplex.core.recharge.Recharge;
import mineplex.core.spawn.Spawn;
import mineplex.core.status.ServerStatusManager;
import mineplex.core.teleport.Teleport;
import mineplex.core.updater.Updater;
import nautilus.game.minekart.gp.GPManager;
import nautilus.game.minekart.kart.KartManager;
import nautilus.game.minekart.menu.KartMenu;
import nautilus.game.minekart.repository.KartRepository;
import nautilus.game.minekart.shop.KartShop;
import nautilus.game.minekart.track.TrackManager;
import nautilus.game.minekart.track.TrackProcessor;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.util.org.apache.commons.io.FileDeleteStrategy;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


 
public class MineKart extends JavaPlugin implements INautilusPlugin, Listener
{
	private String WEB_CONFIG = "webServer";
	
	//Modules
	private CoreClientManager _clientManager;
	private DonationManager _donationManager;
	private BlockRestore _blockRestore;
	private Creature _creature;
	private Spawn _spawn;
	private Teleport _teleport;
	
	private GPManager _gpManager;

	private ServerListener _serverListener;

	private Location _spawnLocation;
	
	private FakeEntity _chicken;
	private FakeEntity _wolf;
	private FakeEntity _pig;
	private FakeEntity _spider;
	private FakeEntity _sheep;
	private FakeEntity _cow;
	private FakeEntity _golem;
	private FakeEntity _blaze;
	private FakeEntity _enderman;
	
	@Override
	public void onEnable()
	{
		ClearRaceFolders();
		
		getConfig().addDefault(WEB_CONFIG, "http://accounts.mineplex.com/");
		getConfig().set(WEB_CONFIG, getConfig().getString(WEB_CONFIG));
		saveConfig();

		_spawnLocation = new Location(this.getServer().getWorlds().get(0), 8.5, 17, -22.5, 0f, 0f);
		
		_clientManager = CoreClientManager.Initialize(this, GetWebServerAddress());
		CommandCenter.Initialize(this, _clientManager);
		FakeEntityManager.Initialize(this);
		Recharge.Initialize(this);
		
		_donationManager = new DonationManager(this, GetWebServerAddress());
		
		_creature = new Creature(this);
		
		new Punish(this, GetWebServerAddress(), _clientManager);
		new Explosion(this, _blockRestore);

		_teleport = new Teleport(this, _clientManager, _spawn);

		//Unreferenced Modules
		new AntiStack(this);
		//new Chat(this, GetClans(), _repository);
		new JoinQuit();
		new ServerStatusManager(this, new LagMeter(this, _clientManager));
		
		
		PacketHandler packetHandler = new PacketHandler(this);
		
		ItemStackFactory.Initialize(this, true);
		
		//Kart
		_gpManager = new GPManager(this, _donationManager, _teleport, Recharge.Instance, new KartManager(this, Recharge.Instance), new TrackManager(this, _teleport));
		new TrackProcessor();
		
		//Updates
		new Updater(this);

		//_serverListener = new ServerListener(GetWebServerAddress(), getServer().getIp(), getServer().getPort() + 1);

		//new TabLobbyList(this, playerNamer.PacketHandler, _clientManager, _donationManager, true);
		
		FakeEntityManager.Instance.SetPacketHandler(packetHandler);
		DonationManager donationManager = new DonationManager(this, GetWebServerAddress());

		new NpcManager(this, _creature);
		KartFactory _kartFactory = new KartFactory(this, new KartRepository(GetWebServerAddress()));
		new KartShop(_kartFactory, _clientManager, donationManager, CurrencyType.Gems);
		new KartMenu(_kartFactory, _clientManager, donationManager, _gpManager);
		
		new MemoryFix(this);
		
		getServer().getPluginManager().registerEvents(this,  this);
		
		CreateFakeKarts();
	}

	@EventHandler
	public void OnPlayerJoin(PlayerJoinEvent event)
	{
		event.getPlayer().teleport(_spawnLocation);
		event.getPlayer().setGameMode(GameMode.SURVIVAL);
		event.getPlayer().setFoodLevel(20);
		event.getPlayer().setHealth(20d);
		ShowFakeKarts(event.getPlayer());
	}

	@EventHandler
	public void PreventFoodChange(FoodLevelChangeEvent event)
	{
		if (event.getEntity() instanceof Player && !_gpManager.InGame((Player)event.getEntity()))
		{
			event.setCancelled(true);
		}
	}
	
    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event)
    {
        if (!event.getPlayer().isOp())
        {
        	event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event)
    {
        if (!event.getPlayer().isOp())
            event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event)
    {
        if (!event.getPlayer().isOp())
            event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerBucketFill(PlayerBucketFillEvent event)
    {
        if (!event.getPlayer().isOp())
            event.setCancelled(true);
    }
    
    @EventHandler
    public void PreventDrop(PlayerDropItemEvent event)
    {
    	event.setCancelled(true);
    }
    
    @EventHandler
    public void BurnCancel(BlockBurnEvent event)
    {
    	event.setCancelled(true);
    }
    
    @EventHandler
    public void SpreadCancel(BlockFromToEvent event)
    {
    	event.setCancelled(true);
    }

    @EventHandler
    public void GrowCancel(BlockGrowEvent event)
    {
    	event.setCancelled(true);
    }
	
	@Override
	public void onDisable()
	{
		_serverListener.Shutdown();
	}

	@Override
	public JavaPlugin GetPlugin() 
	{
		return this;
	}

	@Override
	public String GetWebServerAddress()  
	{
		return getConfig().getString(WEB_CONFIG);
	}

	@Override
	public Server GetRealServer()
	{
		return getServer();
	}

	@Override
	public PluginManager GetPluginManager() 
	{
		return GetRealServer().getPluginManager();
	}
	
	private void CreateFakeKarts()
	{
		_chicken = new FakeEntity(EntityType.CHICKEN, new Location(_spawnLocation.getWorld(), 6.5, 17.5, -39.5, 0f, 0f));
		_wolf = new FakeEntity(EntityType.WOLF, new Location(_spawnLocation.getWorld(), 8.5, 17.5, -39.5, 0f, 0f));
		_pig = new FakeEntity(EntityType.PIG, new Location(_spawnLocation.getWorld(), 10.5, 17.5, -39.5, 0f, 0f));
		_spider = new FakeEntity(EntityType.SPIDER, new Location(_spawnLocation.getWorld(), 6.5, 19.5, -39.5, 0f, 0f));
		_sheep = new FakeEntity(EntityType.SHEEP, new Location(_spawnLocation.getWorld(), 8.5, 19.5, -39.5, 0f, 0f));
		_cow = new FakeEntity(EntityType.COW, new Location(_spawnLocation.getWorld(), 10.5, 19.5, -39.5, 0f, 0f));
		_golem = new FakeEntity(EntityType.IRON_GOLEM, new Location(_spawnLocation.getWorld(), 6.5, 21.5, -39.5, 0f, 0f));
		_blaze = new FakeEntity(EntityType.BLAZE, new Location(_spawnLocation.getWorld(), 8.5, 21.5, -39.5, 0f, 0f));
		_enderman = new FakeEntity(EntityType.ENDERMAN, new Location(_spawnLocation.getWorld(), 10.5, 21.5, -39.5, 0f, 0f));
	}
	
	private void ShowFakeKarts(Player player)
	{
		EntityPlayer mcPlayer = ((CraftPlayer)player).getHandle();
		
		mcPlayer.playerConnection.sendPacket(_chicken.Spawn());
		mcPlayer.playerConnection.sendPacket(_wolf.Spawn());
		mcPlayer.playerConnection.sendPacket(_pig.Spawn());
		mcPlayer.playerConnection.sendPacket(_spider.Spawn());
		mcPlayer.playerConnection.sendPacket(_sheep.Spawn());
		mcPlayer.playerConnection.sendPacket(_cow.Spawn());
		mcPlayer.playerConnection.sendPacket(_golem.Spawn());
		mcPlayer.playerConnection.sendPacket(_blaze.Spawn());
		mcPlayer.playerConnection.sendPacket(_enderman.Spawn());
	}
	
	private void ClearRaceFolders()
	{
		File mainDirectory = new File(".");
	    
	    FileFilter statsFilter = new FileFilter() 
	    {
			@Override
			public boolean accept(File arg0)
			{
				return arg0.isDirectory() && arg0.getName().contains("-");
			}
	    };
	    
	    for (File f : mainDirectory.listFiles(statsFilter))
	    {
	    	try
			{
				FileDeleteStrategy.FORCE.delete(f);
				
			} 
	    	catch (IOException e)
			{
				System.out.println("Error deleting " + f.getName() + " on startup.");
			}
	    }
	}
}
