
package mandelbrotfractal2;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * The base class used for all point-based fractals (such as the Mandelbrot or Julia sets).
 * This class uses two key algorithms: multithreading and the Internal Area Optimization.
 * This last one utilizes the continual nature of the points inside sets like the Mandelbrot
 * by dividing the screen into "chunks" of 32 * 32 pixels. If the perimeter of a chunk
 * consists of black pixels (points inside the set) then the interior of the chunk is
 * therefore black. Without the optimization, there almost no additional work performed, 
 * but if the optimization can be performed,
 * it only has to do (24 * 4) / (24 ^ 2) = 16.66% (a sixth) of the work, a noticable speedup.
 * @author Jesse Victors
 */
public abstract class PointBasedFractal extends Fractal implements ChangeListener
{
	private LinkedBlockingQueue<Chunk> chunksToRender = new LinkedBlockingQueue<>();
	private LinkedBlockingQueue<Chunk> allChunks = new LinkedBlockingQueue<>(); //backup used to refill chunksToRender
	private ReentrantLock queueLocker = new ReentrantLock();
	
	
	public PointBasedFractal(FractalViewport viewport)
	{
		super(viewport);
		viewport.setChangeListener(this);
		
		//initialize all the chunks, create the work queue
		int numOfChunksW = 1 + viewport.getScreenSize().width / Chunk.SIZE; //may have overlap over edge of screen
		int numOfChunksH = 1 + viewport.getScreenSize().height / Chunk.SIZE;
		for (int j = 0; j < numOfChunksW; j++)
			for (int k = 0; k < numOfChunksH; k++)
				allChunks.add(new Chunk(new Point(j * Chunk.SIZE, k * Chunk.SIZE)));
		
		chunksToRender.addAll(allChunks);
	}
	
	
	
	/**
	 * Called by the FractalViewport whenever a user changes something.
	 * This restarts the rendering to start from the beginning.
	 * @param e the ChangeEvent for the state change
	 */
	@Override
	public void stateChanged(ChangeEvent e)
	{
		restartRendering();
	}
	
	
	
	/**
	 * Starts the rendering process. This thread returns very quickly, since it
	 * simply starts a set of threads which do the actual rendering. The number
	 * of rendering threads started is equal to the number of CPU processors
	 * available to the JVM.
	 */
	@Override
	public void startRendering()
	{
		int numProcessors = 2;//Runtime.getRuntime().availableProcessors();
		for (int j = 0; j < numProcessors; j++)
		{
			Thread renderingThread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						Graphics fractalGraphics = fractalImage.getGraphics();
						
						while (true)
						{
							if (chunksToRender == null || chunksToRender.isEmpty())
								Thread.sleep(250);
							else
								renderChunks(fractalGraphics);
						}
							
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			});
			
			renderingThread.start();
		}
	}
	
	
	
	/**
	 * Pulls a Chunk from the work queue and renders it.
	 * This function returns immediately if there is no work to be done.
	 */
	private void renderChunks(Graphics fractalGraphics)
	{
		queueLocker.lock();
		Chunk chunk = chunksToRender.poll();
		queueLocker.unlock();
		
		if (chunk != null)
			chunk.render(fractalGraphics);
	}
	
	
	
	/**
	 * Determines the appropriate color for the pixel at the given screen location,
	 * paints it onto the given graphics object, and then returns the color.
	 * @param screenPixel the given location on the screen
	 * @param g the graphics object to draw to
	 * @return the color determined at screenPixel
	 */
	public Color colorPixel(Point screenPixel, Graphics g)
	{
		Point.Double loc = viewport.convertToFractalLoc(screenPixel);
		Color color = getColorAt(loc);
		g.setColor(color);
		g.drawLine(screenPixel.x, screenPixel.y, screenPixel.x, screenPixel.y);
		return color;
	}
	
	
	
	/**
	 * Restarts the rendering of the fractal back to the beginning.
	 * All rendering threads will finish whatever they are doing (either rendering or sleeping)
	 * and then they will start working on the first bunch of Chunks.
	 */
	public void restartRendering()
	{
		queueLocker.lock();
		
		chunksToRender.clear();
		chunksToRender.addAll(allChunks);
		
		queueLocker.unlock();
	}
	
	
	
	public abstract Color getColorAt(Point.Double pt);
	
	
	
	private class Chunk
	{
		private static final int SIZE = 24; //24 seems to work well, 32 is also another option
		private Point origin; //the upper left-hand corner of this Chunk
		
		
		public Chunk(Point origin)
		{
			this.origin = origin;
		}
		
		
		/**
		 * Renders this chunk of pixels.
		 * This applies the Internal Area optimization, wherein if the entire
		 * perimeter of the chunk consists of black pixels, then the inner ones
		 * need not be computed because they are black too. However, if the
		 * perimeter doesn't consist of all black pixels, then the inner pixels
		 * are computed individually. So it just works smarter, not harder.
		 * @param fractalGraphics the Graphics to draw onto
		 */
		public void render(Graphics fractalGraphics)
		{
			boolean boundaryIsBlack = true;
			Point pt = new Point();

			pt.y = origin.y; //scan top
			if (!checkHoriz(origin, pt, fractalGraphics))
				boundaryIsBlack = false;

			pt.y = origin.y + SIZE - 1; //scan bottom
			if (!checkHoriz(origin, pt, fractalGraphics))
				boundaryIsBlack = false;

			pt.x = origin.x; //scan left side
			if (!checkVert(origin, pt, fractalGraphics))
				boundaryIsBlack = false;

			pt.x = origin.x + SIZE - 1; //scan right side
			if (!checkVert(origin, pt, fractalGraphics))
				boundaryIsBlack = false;

			if (boundaryIsBlack)
			{
				fractalGraphics.setColor(Color.BLACK);
				fractalGraphics.fillRect(origin.x + 1, origin.y + 1, SIZE - 2, SIZE - 2);
			}
			else
			{
				for (pt.x = origin.x + 1; pt.x < origin.x + SIZE - 1; pt.x++)
					for (pt.y = origin.y + 1; pt.y < origin.y + SIZE - 1; pt.y++)
						colorPixel(pt, fractalGraphics);
			}
		}
		
		
		
		/**
		 * Scans horizontally down the side of a chunk of pixels.
		 */
		private boolean checkHoriz(Point origin, Point pt, Graphics graphics)
		{
			boolean allBlack = true;
			for (pt.x = origin.x + 1; pt.x < origin.x + SIZE - 1; pt.x++)
				if (colorPixel(pt, graphics) != Color.BLACK)
					allBlack = false;
			return allBlack;
		}
		
		
		
		/**
		 * Scans vertically down the side of a chunk of pixels.
		 */
		private boolean checkVert(Point origin, Point pt, Graphics graphics)
		{
			boolean allBlack = true;
			for (pt.y = origin.y; pt.y < origin.y + SIZE; pt.y++)
				if (colorPixel(pt, graphics) != Color.BLACK)
					allBlack = false;
			return allBlack;
		}
	}
}
