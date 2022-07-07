package mineplex.core.twofactor;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapFont;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MinecraftFont;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class TwoFactorMapRenderer extends MapRenderer
{
	private static final String SETUP_TEXT =
			"\u00A7100;Two-factor Auth Setup\n\n" +
			"\u00A744;1. Use your device to\n" +
			"scan the QR code, or\n" +
			"enter your \u00A716;secret code:\n" +
			"\u00A728;%s\n\n" +
			"\u00A744;2. Type the\n" +
			"code from\n" +
			"the app\n" +
			"in chat.";
	private static final MapFont FONT = MinecraftFont.Font;
	private static final byte QR_COLOR = (byte)116; // Black
	private static final QRCodeWriter writer = new QRCodeWriter();

	private final Player _player;
	private final byte[][] contents = new byte[128][128];

	public TwoFactorMapRenderer(Player player, String issuer, String secret)
	{
		_player = player;

		BitMatrix matrix;
		try
		{
			matrix = writer.encode(String.format("otpauth://totp/%s?secret=%s&issuer=%s", _player.getName(), secret, issuer), BarcodeFormat.QR_CODE, 32, 32, ImmutableMap.of(EncodeHintType.MARGIN, 0));

		}
		catch (WriterException e)
		{
			e.printStackTrace();
			return;
		}

		// Set background color to white
		for (byte[] column : contents)
		{
			Arrays.fill(column, (byte)32);
		}

		String spacedSecret = Joiner.on(' ').join(Splitter.fixedLength(4).split(secret));
		renderText(contents, 2, 2, String.format(SETUP_TEXT, spacedSecret));
		renderQR(contents, 62, 62, matrix, 2);

	}

	private static void renderText(byte[][] contents, int startX, int startY, String text)
	{
		int x = startX;
		int y = startY;
		byte color = (byte)44;

		for (int i = 0; i < text.length(); i++)
		{
			char c = text.charAt(i);
			if (c == '\n')
			{
				x = startX;
				y += FONT.getHeight() + 1;
				continue;
			}

			if (c == '\u00A7')
			{
				int end = text.indexOf(';', i);
				if (end >= 0)
				{
					color = Byte.parseByte(text.substring(i+1, end));
					i = end;
					continue;
				}
			}

			MapFont.CharacterSprite sprite = FONT.getChar(c);
			for (int k = 0; k < sprite.getWidth(); k++)
			{
				for (int l = 0; l < FONT.getHeight(); l++)
				{
					if (sprite.get(l, k))
					{
						if (x+k >= 128 || y+l >= 128)
						{
							continue;
						}
						contents[x+k][y+l] = color;
					}
				}
			}
			x += sprite.getWidth() + 1;
		}
	}

	private static void renderQR(byte[][] contents, int x, int y, BitMatrix matrix, int scale)
	{
		for (int matrixX = 0 ; matrixX < matrix.getWidth(); matrixX++)
		{
			for (int matrixY = 0; matrixY < matrix.getHeight(); matrixY++)
			{

				if (matrix.get(matrixX, matrixY))
				{
					for (int i = 0; i < scale; i++)
					{
						for (int k = 0; k < scale; k++)
						{
							if (x + (matrixX * scale) + i >= 128 || y + (matrixY * scale) + k >= 128)
							{
								continue;
							}
							contents[x + (matrixX * scale) + i][y + (matrixY * scale) + k] = QR_COLOR;
						}
					}
				}
			}
		}
	}

	@Override
	public void render(MapView mapView, MapCanvas mapCanvas, Player player)
	{
		if (player != _player)
		{
			return;
		}

		for (int x = 0; x < 128; x++)
		{
			for (int y = 0; y < 128; y++)
			{
				mapCanvas.setPixel(x, y, contents[x][y]);
			}
		}
	}
}
