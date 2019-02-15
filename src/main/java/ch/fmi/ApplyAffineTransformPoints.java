package ch.fmi;

import com.google.common.primitives.Doubles;

import java.util.ArrayList;

import net.imglib2.realtransform.AffineTransform3D;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, headless = true, menuPath = "FMI>Apply 3D Affine Transform to Points")
public class ApplyAffineTransformPoints implements Command {
	@Parameter(label = "x Coordinates")
	private double[] xIn;

	@Parameter(label = "y Coordinates")
	private double[] yIn;

	@Parameter(label = "z Coordinates")
	private double[] zIn;

	@Parameter(label = "Affine transformation matrix")
	private double[] m;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] xOut;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] yOut;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] zOut;

	@Override
	public void run() {
		AffineTransform3D transform = new AffineTransform3D();
		transform.set(m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8],
				m[9], m[10], m[11]);

		ArrayList<Double> xList = new ArrayList<>();
		ArrayList<Double> yList = new ArrayList<>();
		ArrayList<Double> zList = new ArrayList<>();

		double[] oldCoords = new double[3];
		double[] newCoords = new double[3];

		for (int i = 0; i < xIn.length; i++) {
			oldCoords[0] = xIn[i];
			oldCoords[1] = yIn[i];
			oldCoords[2] = zIn[i];
			transform.applyInverse(newCoords, oldCoords);
			xList.add(newCoords[0]);
			yList.add(newCoords[1]);
			zList.add(newCoords[2]);
		}

		xOut = Doubles.toArray(xList);
		yOut = Doubles.toArray(yList);
		zOut = Doubles.toArray(zList);
	}
}
