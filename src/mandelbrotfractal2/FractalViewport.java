
package mandelbrotfractal2;

import java.awt.*;
import java.awt.event.*;
import java.util.Stack;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * FractalViewport acts
 * @author Jesse Victors
 */
public final class FractalViewport implements MouseListener, MouseMotionListener, KeyListener
{
	private Rectangle newScreenBounds = new Rectangle(); //keeps track of the bounds of the box, can be negative-dimensional
	private Rectangle drawBounds = new Rectangle(); //this is final box drawn to the screen
	private Rectangle.Double fractalView; //the Rectangle that bounds the current view of the fractal
	private Dimension screenSize; //the size of the computer screen
	private Point.Double conversion; //conversion between fractalView and screenSize, stored rather than recalculated every time
	private boolean changingBounds = false; //are we currently drawing the box?
	
	private ChangeListener changeListener; //called when the user modifies anything (zooming, panning, etc)
	private int maxIterations; //how many iterations deep should the fractal be rendered?
	private double coloring = 1; //coloring coefficient potentially utilized by a fractal
	private Stack<Object> zoomStack = new Stack<>(); //stack used to keep track of variables when zooming
	
	
	public FractalViewport(Dimension screenSize, Rectangle.Double fractalView, int maxIterations)
	{
		this.screenSize = screenSize;
		this.fractalView = fractalView;
		this.maxIterations = maxIterations;
		conversion = computePixelConversion();
		
		setChangeListener(new ChangeListener()
		{ //set up a blank change listener because by default we don't need to do anything
			@Override
			public void stateChanged(ChangeEvent e)
			{ }
		});
	}
	
	
	
	/**
	 * Called when the user presses any of the mouse buttons.
	 * If the left mouse button is pressed, this methods starts the creation
	 * of the zoom box. If the right mouse is pressed, this method sets this to
	 * the previous zoom level and restores other variables like maxIterations and coloring.
	 * @param e the MouseEvent for this mouse press
	 */
	@Override
	public void mousePressed(MouseEvent e)
	{
		if (e.getButton() == MouseEvent.BUTTON1)
		{
			changingBounds = true;

			newScreenBounds.setLocation(e.getPoint());
			newScreenBounds.setSize(0, 0); //reset zoom box
			drawBounds.setRect(newScreenBounds);
		}
		else if (e.getButton() == MouseEvent.BUTTON3)
		{
			if (!zoomStack.empty())
			{ 
				//then restore variables from the stack
				Object[] objs = (Object[])zoomStack.pop();
				fractalView = (Rectangle.Double)objs[0];
				coloring = (Double)objs[1];
				maxIterations = (Integer)objs[2];
				conversion = computePixelConversion();
				
				changeListener.stateChanged(new ChangeEvent(this)); //need to rerender the fractal
			}
		}
	}
	
	
	
	/**
	 * Called when the user has pressed a mouse button and dragging the mouse around.
	 * If the user is drawing a box, this method resizes the drawn box in proportion to
	 * the screen resolution ratio to keep the aspect ratios proper when zooming.
	 * @param e the MouseEvent for this mouse drag
	 */
	@Override
	public void mouseDragged(MouseEvent e)
	{
		if (!changingBounds) //e.getButton() doesn't work correctly, but this does
			return;
		
		int newWidth = e.getX() - newScreenBounds.x; //can be negative
		newScreenBounds.setSize(newWidth, (int)(newWidth * screenSize.height / (float)screenSize.width)); //convert to screen ratio
		
		//we're keeping track of two different rectangles, this is the one that's actually on the screen
		drawBounds.setSize(Math.abs(newScreenBounds.width), Math.abs(newScreenBounds.height));
		drawBounds.setLocation(Math.min(newScreenBounds.x, newScreenBounds.x + newScreenBounds.width), 
							   Math.min(newScreenBounds.y, newScreenBounds.y + newScreenBounds.height));
	}
	
	
	
	/**
	 * Called when the user lets go of the mouse.
	 * If the user is finished drawing the box, it first checks if the box has less
	 * than five pixels to a side. If it does, the box is done being created but
	 * there's no zoom performed because it was probably a user mistake.
	 * However, if the box is larger than this, it zooms in on the fractal.
	 * The change listener is then called.
	 * @param e the MouseEvent for this mouse release
	 */
	@Override
	public void mouseReleased(MouseEvent e)
	{
		if (e.getButton() != MouseEvent.BUTTON1)
			return;
		
		if (drawBounds.width >= 5 && drawBounds.height >= 5)
		{
			Object[] objs = {fractalView.clone(), new Double(coloring), new Integer(maxIterations)};
			zoomStack.push(objs); //save variables to stack

			Point.Double fractalLoc = convertToFractalLoc(drawBounds.getLocation());
			fractalView.x = fractalLoc.x; //set the new view origin
			fractalView.y = fractalLoc.y;

			fractalView.width = drawBounds.width * conversion.x; //zoom in by the proper factor
			fractalView.height = drawBounds.height * conversion.y;

			conversion = computePixelConversion(); //the conversion has changed, so recompute
			changeListener.stateChanged(new ChangeEvent(this)); //need to rerender the fractal
		}
		
		changingBounds = false;
	}
	
	
	
	/**
	 * Calls mouseReleased(e)
	 * @param e the MouseEvent for this mouse exit
	 */
	@Override
	public void mouseExited(MouseEvent e)
	{
		mouseReleased(e);
	}
	
	
	
	/**
	 * Called when the user presses a key (including when they hold it down).
	 * This method handles panning and changing of the coloring coefficient
	 * or the fractal's resolution. The change listener is then called.
	 * @param e 
	 */
	@Override
	public void keyPressed(KeyEvent e)
	{
		int code = e.getKeyCode();
		switch (code)
		{
			case KeyEvent.VK_D :
				coloring += 0.5;
				break;
				
			case KeyEvent.VK_A :
				coloring = Math.max(0, coloring - 0.5);
				break;
				
			case KeyEvent.VK_W :
				maxIterations += 50;
				break;
				
			case KeyEvent.VK_S :
				maxIterations = Math.max(0, maxIterations - 50);
				break;
				
			case KeyEvent.VK_UP:
				fractalView.y -= getPixelConversion().y * 10;
				break;
				
			case KeyEvent.VK_DOWN:
				fractalView.y += getPixelConversion().y * 10;
				break;
				
			case KeyEvent.VK_LEFT:
				fractalView.x -= getPixelConversion().x * 10;
				break;
				
			case KeyEvent.VK_RIGHT:
				fractalView.x += getPixelConversion().x * 10;
				break;
		}
		
		changeListener.stateChanged(new ChangeEvent(this)); //need to rerender the fractal
	}
	
	
	
	/**
	 * Paints this viewport to the given Graphics object.
	 * This involves drawing the coloring coefficient and the fractal's resolution (maxIterations)
	 * to a small gray box in the lower left hand corner of the screen.
	 * If the user is creating a zoom box, that is drawn as well in dark gray.
	 * @param g the Graphics object to draw to
	 */
	public void paint(Graphics g)
	{
		g.setColor(Color.GRAY);
		g.fillRect(5, screenSize.height - 21, 73, 15); //g.fillRect(5, screenSize.height - 46, 73, 15);
		g.setColor(Color.BLACK);
		g.drawString(coloring+", "+maxIterations, 5, screenSize.height - 10); //g.drawString(coloring+", "+maxIterations, 5, screenSize.height - 35);
		
		if (changingBounds)
		{
			g.setColor(Color.darkGray);
			g.drawRect(drawBounds.x, drawBounds.y, drawBounds.width, drawBounds.height);
		}
	}
	
	
	
	/**
	 * Converts the given screen point to it's location in the fractal and returns the result.
	 * @param screenLoc the location of a screen pixel
	 * @return the equivalent location in the fractal
	 */
	public Point.Double convertToFractalLoc(Point screenLoc)
	{
		return new Point.Double(screenLoc.x * conversion.y + fractalView.x, 
								screenLoc.y * conversion.y + fractalView.y);
	}
	
	
	
	/**
	 * @return the size of the screen
	 */
	public Dimension getScreenSize()
	{
		return screenSize;
	}
	
	
	
	/**
	 * @return the Rectangle that bounds the current view of the fractal
	 */
	public Rectangle.Double getFractalViewport()
	{
		return fractalView;
	}
	
	
	
	/**
	 * @return the conversion ratios between the fractal's bounding rectangle and the screen resolution
	 */
	public Point.Double getPixelConversion()
	{
		return conversion;
	}
	
	
	
	/**
	 * Computes the ratio between the fractal's bounding rectangle and the screen resolution,
	 * and returns the result.
	 * @return the conversion ratios. This is used to update the "conversion" variable,
	 * which getPixelConversion can quickly return.
	 */
	private Point.Double computePixelConversion()
	{
		return new Point.Double(fractalView.width / screenSize.width, 
								fractalView.height / screenSize.height);
	}
	
	
	
	/**
	 * @return the coefficient that can be used for the fractal's coloring scheme
	 */
	public double getColoring()
	{
		return coloring;
	}

	
	
	/**
	 * @return the resolution of the fractal, in other words the maximum iterations
	 * needed to sufficiently render it.
	 */
	public final int getMaxIterations()
	{
		return maxIterations;
	}
	
	
	
	/**
	 * Sets the resolution of the fractal by resetting its maximum iterations.
	 * @param newMaxIterations the new value
	 */
	public final void setMaxIterations(int newMaxIterations)
	{
		maxIterations = newMaxIterations;
	}
	
	
	
	/**
	 * Sets the only change listener to the given parameter. This listener will
	 * be called whenever this viewport changes due to user input.
	 * @param newChangeListener the desired ChangeListener to use
	 */
	public void setChangeListener(ChangeListener newChangeListener)
	{
		changeListener = newChangeListener;
	}
	
	
	
	@Override
	public void mouseEntered(MouseEvent e)
	{ }

	@Override
	public void keyTyped(KeyEvent e)
	{ }

	@Override
	public void keyReleased(KeyEvent e)
	{ }
	
	@Override
	public void mouseMoved(MouseEvent e)
	{ }
	
	@Override
	public void mouseClicked(MouseEvent e)
	{ }
}
