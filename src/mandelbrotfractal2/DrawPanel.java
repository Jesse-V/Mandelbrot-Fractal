
package mandelbrotfractal2;

import java.awt.Graphics;
import javax.swing.JPanel;

/**
 * The JPanel on which the fractal and the viewport's information is drawn onto.
 * This class launches a drawing thread which repeatedly paints onto the screen
 * the fractal as it currently appears. This makes the display independent of the
 * actual rendering of the fractal.
 * @author Jesse Victors
 */
public class DrawPanel extends JPanel implements Runnable
{
	private Fractal fractal; 
	private FractalViewport viewport;
	
	
	public DrawPanel(Fractal fractal, FractalViewport bounds)
	{
		this.fractal = fractal;
		this.viewport = bounds;
		new Thread(this).start();
	}
	
	
	
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.drawImage(fractal.getImage(), 0, 0, this);
		viewport.paint(g);
	}
	
	
	
	/**
	 * The function that is run by the thread.
	 * At 30 FPS this method repaints everything.
	 */
	public void run()
	{
		try
		{
			while (true)
			{
				repaint();
				Thread.sleep(33, 33); //30 fps
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
