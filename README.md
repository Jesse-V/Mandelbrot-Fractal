Mandelbrot-Fractal
==================

Mandelbrot Fractal Viewer
License: GPL V3, http://www.gnu.org/licenses/gpl.html

Java application that renders the Mandelbrot fractal fullscreen. Navigation, smooth coloring, and some innovative optimizations are all implemented.

This application renders a fractal fullscreen and provides options for panning, zooming, unzooming, saving screenshots, adjusting the color scheme, and other options. It uses multiple CPU cores for rendering, and currently displays the Mandelbrot set with a smooth Normalized Iteration Count Algorithm for coloring. Optimizations are also in place to ensure that the most computationally expensive areas (the black areas that belong to the Mandelbrot set) are rendered in nearly a sixth of the time. The code is organized and commented, and is designed such that the MandelbrotFractal.java class can be swapped out so that the application can display any other point-based fractal, such as the Julia set. 


Controls: (managed primarily by FractalViewport.java, less so by Main.java)

Exit: Escape

Save screenshot: P (the file is saved in the running directory in the format "minX-maxX, minY-maxY (color coefficient, fractal resolution).png"

Pan: arrowkeys (up, down, left, right)

Zoom: zoom in by pressing the left mouse, dragging to create the new view box, release when ready. To unzoom, right click.

Change fractal iteration depth (resolution): increase with W, decrease with S

Change coloring scheme coefficient: increase with D, decrease with A

Usage notes: The zoom procedure is familiar because it's exactly the same as selecting items on a desktop. However, the zoom square is drawn proportional to the screen resolution to prevent severe deformation of the fractal image. When zooming in, it may be necessary to increase the fractal's resolution using the W key. Often times it may be also a good idea to substantially increase the coloring coefficient using the D key. Each time you zoom in, the fractal's resolution and coloring scheme is preserved so that when you unzoom the image looks exactly the same. The coloring coefficient and the fractal resolution are both displayed in a gray box near the bottom left corner of the screen. These numbers will not appear when P is pressed to take a screenshot.


Coloring:

Most implementations that display the Mandelbrot set use an Escape Time coloring algorithm for its simplicity. However, this approach produces bands of colors that detract from the beauty of the fractal. Instead, this uses a smooth coloring using the Normalized Iteration Count algorithm which is primarily based on the output of the fractal formula when it first exceeds the bailout radius (see MandelbrotFractal.java). The sine and cosine functions are then applied to produce a cyclic smooth gradient.


Optimizations:

Check main bulb: each pixel is checked to see if it lies inside the main bulb. I have implemented the simple formulas described on the Wikipedia article on the Mandelbrot Fractal. This means that the main bulb (the largest area of black) is rendered nearly instantly no matter the iteration resolution.

Multithreading: the program uses all available multiple CPU cores to complete the rendering proportionally faster. The screen is divided into "chunks" of 24 * 24 pixels (crucial for the optimization described below). These chunks are then inserted into a Queue and each thread pops off a chunk off and renders it, which gives the impression that the rendering is proceeding very fast, even if one rendering thread proceeds slower than the others.

Internal Area optimization: Typically, the black areas in the fractal (the points inside the set) are all computed pixel-by-pixel, which is often slow because the fractal equation must iterate max_number_of_iterations times. However, as this area is continuous, (i.e. there are no holes inside it) for any closed curve, if all points along the perimeter of the curve are in the set, then all the points inside the curve are also inside the set. Thus, those points in the curve's interior don't have to be computed! For each chunk of screen pixels (described above), it computes the color values around the perimeter. If they are all black, it then fills in black to the 22 * 22 inner pixels, which is computationally inexpensive. If color values for the perimeter are not all black, it then computes the 22 * 22 inner pixels one by one. Thus, without the optimization, there almost no additional work performed, but if the optimization can be performed, it only has to perform (24 * 4) / (24 ^ 2) = 16.66% (a sixth) of the work. I'm calling this the "Internal Area optimization", and to my knowledge it's unique to this implementation.