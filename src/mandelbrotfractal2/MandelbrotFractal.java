
package mandelbrotfractal2;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * Class to render the Mandelbrot fractal.
 * @author Jesse Victors
 */
public class MandelbrotFractal extends PointBasedFractal
{
	private static final double LOG_2 = Math.log(2); //so it doesn't have to be recomputed
	
	
	public MandelbrotFractal(FractalViewport viewport)
	{
		super(viewport);
	}
	
	
	
	public static Rectangle.Double getNormalViewingBounds()
	{
		return new Rectangle.Double(-2.05, -1.2, 2.7, 2.4);
	}
	
	
	// https://en.wikipedia.org/wiki/Mandelbrot_set#Cardioid_.2F_bulb_checking
	// Checks if the point is within the second period
	public boolean secondPeriodTest(double x, double y) {
		return Math.pow(x + 1, 2) + Math.pow(y, 2) < 1/16;
	}
	
	/**
	 * Computes the color at the specified point the fractal and returns the result.
	 * This uses the Normalized Iteration Count algorithm for coloring, which uses the
	 * escape radius and the iteration count together, and the sine and cosine function
	 * are then applied on top to generate a smooth cyclic gradient.
	 * @param pt the specified location in the fractal
	 * @return the color for that location using the
	 * maxIterations and coloring variables from the viewport.
	 */
	@Override
	public Color getColorAt(Point.Double pt)
	{
		double ptYSq = pt.y * pt.y;
		double xOff = pt.x - 0.25;
		double q = Math.pow(xOff, 2) + ptYSq;
		if (q * (q + xOff) < ptYSq / 4)
			return Color.BLACK; //http://en.wikipedia.org/wiki/Mandelbrot_fractal#Optimizations
		
		if (secondPeriodTest(xOff, ptYSq)) 
			return Color.BLACK;
		
		double x = 0, xSq = 0, y = 0, ySq = 0;
		int iterations;
		for (iterations = 0; iterations < viewport.getMaxIterations() && (xSq + ySq <= 144); iterations++)
		{
			y = 2 * x * y + pt.y;
			x = xSq - ySq + pt.x;
			xSq = x * x;
			ySq = y * y;
		}

		if (iterations == viewport.getMaxIterations())
			return Color.BLACK;
		else
		{
			double mu = iterations - Math.log(Math.log(xSq + ySq)) / LOG_2;
			float sin = (float)Math.sin(mu / viewport.getColoring()) / 2 + 0.5f;
			float cos = (float)Math.cos(mu / viewport.getColoring()) / 2 + 0.5f;
			return new Color(cos, cos, sin);
		}
	}
}
