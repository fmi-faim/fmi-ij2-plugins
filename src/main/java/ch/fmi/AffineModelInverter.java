package ch.fmi;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imglib2.realtransform.AffineTransform3D;

@Plugin(type = Command.class, headless = true, menuPath = "FMI>Invert Affine Matrix")
public class AffineModelInverter implements Command {
	@Parameter(label = "Input affine transformation matrix")
	private double[] mIn;

	@Parameter(type = ItemIO.OUTPUT, label = "Output affine transformation matrix")
	private double[] mOut;

	@Override
	public void run() {
		AffineTransform3D transform = new AffineTransform3D();
		transform.set(mIn[0], mIn[1], mIn[2], mIn[3], mIn[4], mIn[5], mIn[6], mIn[7], mIn[8],
				mIn[9], mIn[10], mIn[11]);

		mOut = new double[12];
		transform.inverse().toArray(mOut);
	}
}
