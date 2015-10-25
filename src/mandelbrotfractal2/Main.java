
package mandelbrotfractal2;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

/**
* The Main class for the fractal-viewing application. Currently displays the Mandelbrot set, 
* but the MandelbrotFractal.java class can easily be swapped out to display any fractal.
* See the README.txt files for help and documentation.
* License for all classes: GPL V3, http://www.gnu.org/licenses/gpl.html
* @author Jesse Victors
*/
public class Main extends JFrame implements KeyListener
{
	private Window w = new Window(this); //the window used for full screen display
	private Dimension screenSize; //the dimensions of the primary display
	private Fractal fractal; //the fractal currently being rendered
	private FractalViewport viewport; //the "magnifying glass" class, has helper conversion functions
	private DrawPanel drawPanel; //the JPanel that everything is drawn on
	
	
	public static void main(String[] args)
	{
		new Main();
	}
	
	
	
	public Main()
	{
		super("Mandelbrot Fractal 2.1");
	
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];
		DisplayMode displayMode = gd.getDisplayMode();
		
		screenSize = new Dimension(new Dimension(displayMode.getWidth(), displayMode.getHeight()));
		viewport = new FractalViewport(screenSize, MandelbrotFractal.getNormalViewingBounds(), 500);
		fractal = new MandelbrotFractal(viewport);
		drawPanel = new DrawPanel(fractal, viewport);
		
		addKeyListener(this);
		addKeyListener(viewport);
		
		makeFullscreen(gd);
	}
	
	
	public final void makeWindowed()
	{
		setSize(screenSize);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setResizable(false);
		
		add(drawPanel);
		
		addMouseListener(viewport);
		addMouseMotionListener(viewport);
		
		setVisible(true);
	}
	
	
	public final void makeFullscreen(GraphicsDevice gd)
	{
		w.addMouseListener(viewport);
		w.addMouseMotionListener(viewport);
		
		w.add(drawPanel);
		
		
		setVisible(true);
		w.setVisible(true);
		gd.setFullScreenWindow(w);
	}
	
	
	/**
	 * Checks for the Escape button, which it uses to quit the application,
	 * and the P button, which is used to output the current view to a file.
	 * @param e the KeyEvent for the key press
	 */
	@Override
	public void keyPressed(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_ESCAPE:
				System.exit(0);
				break;
			
			case KeyEvent.VK_P:
				saveView();
				break;
		}
	}
	
	
	/**
	 * Renders the current fractal image as a .png file and saves it to disk.
	 * The view coordinates, the coloring coefficient, and the fractal resolution
	 * are all included in the filename.
	 */
	public void saveView()
	{
		try
		{
			Rectangle.Double view = viewport.getFractalViewport();
			File outputfile = new File(view.getMinX()+" - "+view.getMaxX()+", "+view.getMinY()+" - "+view.getMaxY()
					+" ("+viewport.getColoring()+", "+viewport.getMaxIterations()+")"+".png");
			ImageIO.write(fractal.getImage(), "png", outputfile);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	
	
	@Override
	public void keyTyped(KeyEvent e)
	{ }

	
	
	@Override
	public void keyReleased(KeyEvent e)
	{ }
}
