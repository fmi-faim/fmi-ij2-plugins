package ch.fmi;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, headless = true, menuPath = "FMI>Generate 3D Translation/Rotation Matrix")
public class GenerateAffineMatrix implements Command {

	@Parameter
	private double translationX;

	@Parameter
	private double translationY;

	@Parameter
	private double translationZ;

	@Parameter(label="Angle (degree)")
	private double angle;

	@Parameter(choices = { "x", "y", "z" })
	private String axis;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] matrix;

	@Override
	public void run() {
		matrix = new double[12];

		matrix[0] = 1.0d;
		matrix[1] = 0.0d;
		matrix[2] = 0.0d;
		matrix[3] = translationX;

		matrix[4] = 0.0d;
		matrix[5] = 1.0d;
		matrix[6] = 0.0d;
		matrix[7] = translationY;

		matrix[8] = 0.0d;
		matrix[9] = 0.0d;
		matrix[10] = 1.0d;
		matrix[11] = translationZ;
		
		double sin = Math.sin(Math.toRadians(angle));
		double cos = Math.cos(Math.toRadians(angle));
		
		switch (axis) {
		case "x":
			matrix[5] = cos;
			matrix[6] = -sin;
			matrix[9] = sin;
			matrix[10] = cos;
			break;
		case "y":
			matrix[0] = cos;
			matrix[8] = -sin;
			matrix[2] = sin;
			matrix[10] = cos;
			break;
		case "z":
		default:
			matrix[0] = cos;
			matrix[1] = -sin;
			matrix[4] = sin;
			matrix[5] = cos;
			break;
		}
	}
}
