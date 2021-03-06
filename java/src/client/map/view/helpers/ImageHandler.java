package client.map.view.helpers;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import client.utils.ImageUtils;
import shared.definitions.HexType;
import shared.definitions.PortType;

/**
 * Class for storing images necessary for the map component.
 * @author Jonathan Sadler
 *
 */
public class ImageHandler
{	
	private Map<HexType, BufferedImage> HEX_IMAGES;
	private Map<Integer, BufferedImage> PORT_IMAGES;
	private Map<PortType, BufferedImage> PORT_RESOURCE_IMAGES;
	private Map<Integer, BufferedImage> NUMBER_IMAGES;
	private BufferedImage ROBBER_IMAGE;
	private BufferedImage DISALLOW_IMAGE;
	
	private ImageHandler()
	{
		//Load hex images
		HEX_IMAGES = new HashMap<HexType, BufferedImage>();
		
		for (HexType hexType : HexType.values())
		{
			BufferedImage hexImage = loadHexImage(hexType);
			HEX_IMAGES.put(hexType, hexImage);
		}
		
		//Load ports
		PORT_IMAGES = new HashMap<Integer, BufferedImage>();
		
		for (int i = 0; i < 360; i += 60)
			PORT_IMAGES.put(i, loadPortImage(i));
		
		PORT_RESOURCE_IMAGES = new HashMap<PortType, BufferedImage>();
		
		for (PortType portType : PortType.values())
		{
			if (portType != PortType.NONE)
				PORT_RESOURCE_IMAGES.put(portType, loadPortResourceImage(portType));
		}
		
		//Load numbers
		NUMBER_IMAGES = new HashMap<Integer, BufferedImage>();
		
		for (int i = 2; i <= 12; ++i)
		{
			if(i != 7)
				NUMBER_IMAGES.put(i, loadNumberImage(i));
		}
		
		//Load robber and disallowed
		ROBBER_IMAGE = loadRobberImage();
		DISALLOW_IMAGE = loadDisallowImage();
	}
	
	private static ImageHandler handler;

	private static ImageHandler GetHandler()
	{
		if (handler == null)
			handler = new ImageHandler();
		
		return handler;
	}
	
	/**
	 * Gets the image associated with a hex.
	 * @param hexType The hex type desired.
	 * @return The hex image.
	 */
	public static BufferedImage getHexImage(HexType hexType)
	{
		return GetHandler().HEX_IMAGES.get(hexType);
	}
	
	/**
	 * Gets the port image at a specified angle. Ports are stored at the following angles:
	 * 0, 60, 120, 180, 240, 300 (degrees)
	 * This prevents the need for rotation at runtime, thus aligning ports better.
	 * @param angle The desired angle.
	 * @return The associated image.
	 */
	public static BufferedImage getPortImage(int angle)
	{
		return GetHandler().PORT_IMAGES.get(angle);
	}
	
	/**
	 * Gets the image associated with a port.
	 * @param portType The port type desired.
	 * @return The port image.
	 */
	public static BufferedImage getPortResourceImage(PortType portType)
	{
		return GetHandler().PORT_RESOURCE_IMAGES.get(portType);
	}
	
	/**
	 * Gets the number image desired.
	 * @param num The number of the image.
	 * @return The number image.
	 */
	public static BufferedImage getNumberImage(int num)
	{	
		return GetHandler().NUMBER_IMAGES.get(num);
	}
	
	/**
	 * Gets the robber image.
	 * @return The robber.
	 */
	public static BufferedImage getRobberImage()
	{
		return GetHandler().ROBBER_IMAGE;
	}
	
	/**
	 * Gets the disallow image.
	 * @return The disallow image.
	 */
	public static BufferedImage getDisallowImage()
	{
		return GetHandler().DISALLOW_IMAGE;
	}
	
	private BufferedImage loadHexImage(HexType hexType)
	{
		String imageFile = ImageLocation.getHexImageFile(hexType);
		
		return ImageUtils.loadImage(imageFile);
	}
	
	private BufferedImage loadPortImage(int angle)
	{
		String imageFile = ImageLocation.getPortImageFile(angle);
		
		return ImageUtils.loadImage(imageFile);
	}
	
	private BufferedImage loadPortResourceImage(PortType portType)
	{
		String imageFile = ImageLocation.getPortResourceImageFile(portType);
		
		return ImageUtils.loadImage(imageFile);
	}
	
	private BufferedImage loadNumberImage(int num)
	{
		String imageFile = ImageLocation.getNumberImageFile(num);
		
		return ImageUtils.loadImage(imageFile);
	}
	
	private BufferedImage loadRobberImage()
	{	
		String imageFile = ImageLocation.getRobberImageFile();
		
		return ImageUtils.loadImage(imageFile);
	}
	
	private BufferedImage loadDisallowImage()
	{
		String imageFile = ImageLocation.getDisallowImageFile();
		
		return ImageUtils.loadImage(imageFile);
	}
}
