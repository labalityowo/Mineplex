package mineplex.googlesheets;

import java.awt.*;

import mineplex.googlesheets.sheetparser.SheetProviderImpl;
import mineplex.googlesheets.skinhelper.SkinHelperUI;

public class GoogleSheetController
{

	public static void main(String[] args)
	{
		String module = System.getProperty("module");

		if (module == null || module.equalsIgnoreCase("sheetparser"))
		{
			new SheetProviderImpl();
		}
		else if (module.equalsIgnoreCase("skinhelper"))
		{
			EventQueue.invokeLater(() ->
			{
				SkinHelperUI frame = new SkinHelperUI();
				frame.setVisible(true);
			});
		}
	}

}
