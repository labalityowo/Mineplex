package mineplex.core.communities.data;

public enum CommunityRole
{
	LEADER("Leader"),
	COLEADER("Co-Leader"),
	MEMBER("Member")
	;
	
	private String _displayName;
	
	private CommunityRole(String displayName)
	{
		_displayName = displayName;
	}
	
	public String getDisplay()
	{
		return _displayName;
	}
	
	public static CommunityRole parseRole(String role)
	{
		for (CommunityRole test : CommunityRole.values())
		{
			if (test.toString().equalsIgnoreCase(role))
			{
				return test;
			}
		}
		
		return CommunityRole.MEMBER;
	}
}