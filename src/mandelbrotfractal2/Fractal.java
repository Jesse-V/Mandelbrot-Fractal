
package mandelbrotfractal2;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * The base class for all Fractals, whether they are similar to the Koch Snowflake
 * or the Mandelbrot Set. All fractals are drawn onto a BufferedImage, and they all
 * use a FractalViewport for panning, zooming, and scaling to fit the screen.
 * @author Jesse Victors
 */
public abstract class Fractal
{
	protected BufferedImage fractalImage; //the image that the fractal is drawn on
	protected FractalViewport viewport; //used to convert the view to fit the screen
	
	public Fractal(FractalViewport viewport)
	{
		this.viewport = viewport;
		
		Dimension screenSize = viewport.getScreenSize();
		fractalImage = new BufferedImage(screenSize.width, screenSize.height, BufferedImage.TYPE_INT_RGB); //create to fit the screen
		
		startRendering(); //returns immediately
	}
	
	
	
	/**
	 * @return the BufferedImage that the fractal is drawn onto.
	 * There is no guarantee that the fractal is sufficiently rendered,
	 * rather the returned image is how the fractal currently appears.
	 */
	public final BufferedImage getImage()
	{
		return fractalImage;
	}
	
	
	/**
	 * Starts the rendering process. This function must return quickly,
	 * so the actual rendering may be done by threads started by this function.
	 */
	public abstract void startRendering();
	
	
	
	/**
	 * It is strongly recommended that this function be overridden.
	 * @return a Rectangle in double precision containing the entire fractal.
	 */
	public static Rectangle.Double getNormalViewingBounds()
	{
		throw new UnsupportedOperationException("Not supported by base class. Please use a subclass implementation instead.");
	}
}
