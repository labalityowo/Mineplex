package mineplex.bungee.api.token;

public class CNameRecord extends DnsRecord
{
	public CNameRecord(String recordName, String ip, int recordTtl)
	{
		name = recordName;
		value = ip;
		ttl = recordTtl;
		
		type = "CNAME";
		gtdLocation = "DEFAULT";
	}
}
