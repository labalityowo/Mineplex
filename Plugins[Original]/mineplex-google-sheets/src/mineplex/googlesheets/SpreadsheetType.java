package mineplex.googlesheets;

/**
 * An enum containing all the google spreadsheet links relating to Mineplex.<br>
 */
public enum SpreadsheetType
{

	GEM_HUNTERS_CHESTS("11Noztgbpu_gUKkc5F4evKKfyxS-Jv1coE0IrBToX_gg"),
	GEM_HUNTERS_SHOP("1OcYktxVZaW6Fm29Zh6w4Lb-UVyuN8r1x-TFb_3USYYI"),
	MOBA_SKINS("1bgTz46jdnaywOXlmkWKZ5LNWfTDFGTzrTI7QrVEtkDA"),
	QUESTS_SHEET("1Gy1a7GCVopmOLwYE3Sk1DNVCAIwT8ReaLu4wRe0sfDE"),
	SMASH_KITS("1Z_SLBzjiIVqu25PMGw9TwNKR3wd9Y9sX7rSDBl_rpxk"),
	;
	
	private String _id;
	
	SpreadsheetType(String id)
	{
		_id = id;
	}
	
	public String getId()
	{
		return _id;
	}
}