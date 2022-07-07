package mineplex.bungee;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import mineplex.bungee.api.ApiDeleteCall;
import mineplex.bungee.api.ApiGetCall;
import mineplex.bungee.api.ApiPostCall;
import mineplex.bungee.api.HttpCallBase;
import mineplex.bungee.api.token.ARecord;
import mineplex.bungee.api.token.DnsRecord;
import mineplex.bungee.api.token.DomainRecords;
import mineplex.serverdata.Region;
import mineplex.serverdata.data.BungeeServer;
import mineplex.serverdata.data.DataRepository;
import mineplex.serverdata.redis.RedisDataRepository;
import mineplex.serverdata.servers.ConnectionData;
import mineplex.serverdata.servers.ServerManager;
import mineplex.serverdata.servers.ConnectionData.ConnectionType;

public class BungeeRotator
{
	private static DataRepository<BungeeServer> _repository;
	private static DataRepository<BungeeServer> _secondRepository;
	private static PlayerStatsRepository _ipRepository;
	//private static ServerRepository _repository = null;

	private static SimpleDateFormat _dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	private static Logger _logger = Logger.getLogger("BungeeRotator");
	private static boolean _debug = false;
	
	public static void main(String args[])
	{		
		try
		{
			FileHandler fileHandler = new FileHandler("rotator.log", true);
			fileHandler.setFormatter(new Formatter()
			{
				@Override
				public String format(LogRecord record)
				{
					return record.getMessage() + "\n";
				}
			});
			_logger.addHandler(fileHandler);
			_logger.setUseParentHandlers(false);
		}
		catch (SecurityException | IOException e1)
		{
			e1.printStackTrace();
		}


		/*
	        String username = "mineplex";
	        String password = "gjxcVf%nk.";
	        String accountId = null;

	        try {
	            UltraAPIClient ultraAPIClient = new UltraAPIClientImpl(username, password);
	            System.out.println(ultraAPIClient.getNeustarNetworkStatus());
	            AccountDetailsList accountDetailsForUser = ultraAPIClient.getAccountDetailsForUser();
	            System.out.println(accountDetailsForUser.getAccountDetailsData().get(0).getAccountID());
	            if (accountId == null) {
	                accountId = accountDetailsForUser.getAccountDetailsData().get(0).getAccountID();
	            }
	            String zoneName = RandomStringUtils.randomAlphanumeric(16).toLowerCase()+".com.";
	            try {
	                //System.out.println(ultraAPIClient.deleteZone(zoneName));
	            } catch (UltraAPIException e) {
	                e.printStackTrace();
	                if (e.getCode() != 1801) {
	                    System.exit(1);
	                }
	            }
	            System.out.println(ultraAPIClient.createPrimaryZone(accountId, zoneName));
	            System.out.println(ultraAPIClient.getSecondaryZonesOfAccount(accountId));
	            System.out.println(ultraAPIClient.createARecord(zoneName, "foo."+zoneName, "1.2.3.4", 86400));
	            //System.out.println(ultraAPIClient.deleteZone(zoneName));
	        } catch (UltraAPIException e) {
	            e.printStackTrace();
	        }
	        
		*/
		_debug = new File("debug.dat").exists();
		
		_repository = new RedisDataRepository<BungeeServer>(ServerManager.getConnection(true, ServerManager.SERVER_STATUS_LABEL), ServerManager.getConnection(false, ServerManager.SERVER_STATUS_LABEL),
				Region.ALL, BungeeServer.class, "bungeeServers");
		
		_secondRepository = new RedisDataRepository<BungeeServer>(new ConnectionData("10.81.1.156", 6379, ConnectionType.MASTER, "ServerStatus"), new ConnectionData("10.81.1.156", 6377, ConnectionType.SLAVE, "ServerStatus"),
				Region.ALL, BungeeServer.class, "bungeeServers");
		
		//_ipRepository = new PlayerStatsRepository();
		
		BungeeSorter bungeeSorter = new BungeeSorter();
		int maxRecordCount = 10;

		while (true)
		{
			try
			{
				List<BungeeServer> bungeeServers = new ArrayList<BungeeServer>(_repository.getElements());
				bungeeServers.addAll(_secondRepository.getElements());
				
				Collections.sort(bungeeServers, bungeeSorter);
				
				if (_debug)
				{
					int totalPlayers = 0;
					int usPlayers = 0;
					int euPlayers = 0;
					
					for (BungeeServer server : bungeeServers)
					{
						if (server.getPublicAddress().equalsIgnoreCase("127.0.0.1") || server.getPublicAddress().equalsIgnoreCase("0.0.0.0"))
							continue;
						
						totalPlayers += server.getPlayerCount();
						
						if (server.getRegion() == Region.US)
							usPlayers += server.getPlayerCount();
						else
							euPlayers += server.getPlayerCount();
						
						System.out.println(server.getRegion().toString() + " " + server.getName() + " " + server.getPublicAddress() + " " + server.getPlayerCount() + "/" + server.getPlayerCount());
					}
					
					System.out.println("US Players : " + usPlayers);
					System.out.println("EU Players : " + euPlayers);
					System.out.println("Total Players : " + totalPlayers);
					System.out.println("Count : " + bungeeServers.size());
				}
				else
				{
					HashSet<String> usServers = new HashSet<String>();
					HashSet<String> euServers = new HashSet<String>();
										
		
					for (BungeeServer server : bungeeServers)
					{
						if (server.getPublicAddress().equalsIgnoreCase("127.0.0.1"))
							continue;
						
						if (usServers.size() < maxRecordCount && server.getRegion() == Region.US)
						{
							if (usServers.size() >= 2 && server.getPlayerCount() > 900)
								continue;
							
							log("SELECTED " + server.getPublicAddress() + " " + (server.getRegion() == Region.US ? "us" : "eu") + " " + server.getPlayerCount() + "/" + server.getPlayerCount());
							usServers.add(server.getPublicAddress());
						}
						else if (euServers.size() < maxRecordCount && server.getRegion() != Region.US)
						{
							if (euServers.size() >= 2 && server.getPlayerCount() > 900)
								continue;
							
							log("SELECTED " + server.getPublicAddress() + " " + (server.getRegion() == Region.US ? "us" : "eu") + " " + server.getPlayerCount() + "/" + server.getPlayerCount());
							euServers.add(server.getPublicAddress());
						}
					}
		
					DomainRecords records = new ApiGetCall("https://api.dnsmadeeasy.com/V2.0/dns/managed/", 962728,
							"/records", "").Execute(DomainRecords.class);
					List<DnsRecord> recordsToDelete = new ArrayList<DnsRecord>();
					List<DnsRecord> recordsToAdd = new ArrayList<DnsRecord>();
		
					for (DnsRecord record : records.data)
					{
						if (record.type.equalsIgnoreCase("A"))
						{
							if (record.name.equalsIgnoreCase("us"))
							{
								if (usServers.contains(record.value))
									usServers.remove(record.value);
								else
									recordsToDelete.add(record);
							}
							else if (record.name.equalsIgnoreCase("eu"))
							{
								if (euServers.contains(record.value))
									euServers.remove(record.value);
								else
									recordsToDelete.add(record);
							}
						}
					}
					
					for (String address : usServers)
					{
						recordsToAdd.add(new ARecord("us", address, 300));
						log("Adding server address in DNS : " + "us " + address);
					}
					
					if (recordsToAdd.size() > 0)
					{
						new ApiPostCall("https://api.dnsmadeeasy.com/V2.0/dns/managed/", 962728, "/records/", "createMulti").Execute(recordsToAdd);
						log("Created " + recordsToAdd.size() + " records.");
					}
					
					recordsToAdd.clear();
					
					for (String address : euServers)
					{
						recordsToAdd.add(new ARecord("eu", address, 300));
						log("Adding server address in DNS : " + "eu " + address);
					}
					
					if (recordsToAdd.size() > 0)
					{
						new ApiPostCall("https://api.dnsmadeeasy.com/V2.0/dns/managed/", 962728, "/records/", "createMulti").Execute(recordsToAdd);
						log("Created " + recordsToAdd.size() + " records.");
					}	
					recordsToAdd.clear();
		
	
					if (recordsToDelete.size() > 0)
					{
						StringBuilder idBuilder = new StringBuilder();
		
						for (DnsRecord record : recordsToDelete)
						{
							if (idBuilder.length() != 0)
								idBuilder.append("&");
		
							idBuilder.append("ids=" + record.id);
						}
		
						new ApiDeleteCall("https://api.dnsmadeeasy.com/V2.0/dns/managed/", 962728, "/records?" + idBuilder.toString()).Execute();
						log("Deleted " + recordsToDelete.size() + " records.");
					}
					
					_repository.clean();
					_secondRepository.clean();
				}
				
				/*
				List<IpInfo> updatedAddresses = new ArrayList<IpInfo>(1000);
				
				for (IpInfo ipInfo : _ipRepository.getIpAddresses())
				{
					IPGeoData recor = new HttpCallBase("http://www.freegeoip.net/json/" + ipInfo.ipAddress).Execute(IPGeoData.class);
					ipInfo.countryCode = recor.country_code;
					ipInfo.countryName = recor.country_name;
					ipInfo.regionCode = recor.region_code;
					ipInfo.regionName = recor.region_name;
					ipInfo.city = recor.city;
					ipInfo.zipCode = recor.zip_code;
					ipInfo.timeZone = recor.time_zone;
					ipInfo.latitude = recor.latitude;
					ipInfo.longitude = recor.longitude;
					ipInfo.metroCode = recor.metro_code;
					
					updatedAddresses.add(ipInfo);
				}
				
				_ipRepository.updateIps(updatedAddresses);
				*/
				try
				{
					Thread.sleep(15000);
					log("Natural Sleep");
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				log("Error doing something : " + ex.getMessage());
				
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void log(String message)
	{
		log(message, false);
	}
	
	private static void log(String message, boolean fileOnly)
	{
		_logger.info("[" + _dateFormat.format(new Date()) + "] " + message);
		
		if (!fileOnly)
			System.out.println("[" + _dateFormat.format(new Date()) + "] " + message);
	}
}
