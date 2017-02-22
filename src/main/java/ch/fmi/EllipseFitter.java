package ch.fmi;

import java.util.ArrayList;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.IntegerType;

import org.doube.geometry.FitEllipse;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, headless = true, menuPath = "FMI>Center of Ellipse Fit")
public class EllipseFitter<T extends IntegerType<T>> implements Command {
	@Parameter
	private LogService logService;

	@Parameter(label = "Input image")
	private Img<T> img;

	@Parameter(label = "Background value")
	private int bg = 0;

	@Parameter(type = ItemIO.OUTPUT)
	private double centerX;

	@Parameter(type = ItemIO.OUTPUT)
	private double centerY;

	@Override
	public void run() {
		// Get points from binary mask (each white pixel will get added)
		ArrayList<double[]> polygonPoints = new ArrayList<>();
		Cursor<T> cursor = img.localizingCursor();
		while (cursor.hasNext()) {
			if (cursor.next().getInteger() > bg) {
				double[] pos = new double[2];
				pos[0] = cursor.getDoublePosition(0);
				pos[1] = cursor.getDoublePosition(1);
				polygonPoints.add(pos);
			}
		}

		double[][] points = new double[polygonPoints.size()][];
		points = polygonPoints.toArray(points);
		
		logService.info("Ellipse fitting: found " + points.length + " points.");
		
		// Fit ellipse to point cloud, using Michael Doube's FitEllipse
		double[] ellipseParams = FitEllipse.direct(points);
		double[] ellipseDims = FitEllipse.varToDimensions(ellipseParams);

		// Get center coordinates
		centerX = ellipseDims[0];
		centerY = ellipseDims[1];
	}
}
