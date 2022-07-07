package mineplex.game.clans.clans.moderation.antialt;

import com.google.gson.annotations.SerializedName;

public class IpAPIData
{
	public String status;
	
	public String msg;
	
	@SerializedName("package")
	public String accessPackage;
	
	public int remaining_requests;
	
	public String ipaddress;
	
	@SerializedName("host-ip")
	public boolean host_ip;
	
	public String org;
	
	public RegionInfo country;
	
	public RegionInfo subdivision;
	
	public String city;
	
	public String postal;
	
	public LocationInfo location;
	
	public ImmutableIpAPIData makeImmutable()
	{
		return ImmutableIpAPIData.create(this);
	}
	
	public static class ImmutableIpAPIData
	{
		public final String status;
		
		public final String msg;
		
		@SerializedName("package")
		public final String accessPackage;
		
		public final int remaining_requests;
		
		public final String ipaddress;
		
		@SerializedName("host-ip")
		public final boolean host_ip;
		
		public final String org;
		
		public final ImmutableRegionInfo country;
		
		public final ImmutableRegionInfo subdivision;
		
		public final String city;
		
		public final String postal;
		
		public final ImmutableLocationInfo location;
		
		private ImmutableIpAPIData(IpAPIData base)
		{
			status = base.status;
			msg = base.msg;
			accessPackage = base.accessPackage;
			remaining_requests = base.remaining_requests;
			ipaddress = base.ipaddress;
			host_ip = base.host_ip;
			org = base.org;
			country = ImmutableRegionInfo.create(base.country);
			subdivision = ImmutableRegionInfo.create(base.subdivision);
			city = base.city;
			postal = base.postal;
			location = ImmutableLocationInfo.create(base.location);
		}
		
		public static ImmutableIpAPIData create(IpAPIData base)
		{
			if (base == null)
			{
				return null;
			}
			return new ImmutableIpAPIData(base);
		}
	}
	
	public static class RegionInfo
	{
		public String name = "";
		
		public String code = "";
	}
	
	public static class ImmutableRegionInfo
	{
		public final String name;
		
		public final String code;
		
		private ImmutableRegionInfo(RegionInfo base)
		{
			name = base.name;
			code = base.code;
		}
		
		public static ImmutableRegionInfo create(RegionInfo base)
		{
			if (base == null)
			{
				return null;
			}
			
			return new ImmutableRegionInfo(base);
		}
	}
	
	public static class LocationInfo
	{
		@SerializedName("lat")
		public double latitude = 0;
		
		@SerializedName("long")
		public double longitude = 0;
	}
	
	public static class ImmutableLocationInfo
	{
		@SerializedName("lat")
		public final double latitude;
		
		@SerializedName("long")
		public final double longitude;
		
		private ImmutableLocationInfo(LocationInfo base)
		{
			latitude = base.latitude;
			longitude = base.longitude;
		}
		
		public static ImmutableLocationInfo create(LocationInfo base)
		{
			if (base == null)
			{
				return null;
			}
			
			return new ImmutableLocationInfo(base);
		}
	}
}