package mineplex.bungee.api.token;

public class ARecord extends DnsRecord
{
	public ARecord(String recordName, String ip, int recordTtl)
	{
		name = recordName;
		value = ip;
		ttl = recordTtl;
		
		type = "A";
		gtdLocation = "DEFAULT";
	}
}
