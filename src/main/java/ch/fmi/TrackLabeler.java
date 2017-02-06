package ch.fmi;

import net.imagej.ImgPlus;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.UnsignedIntType;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, headless = true, menuPath = "FMI>Label Tracks")
public class TrackLabeler<T> implements Command {
	@Parameter(label = "Original image")
	private ImgPlus<T> input;

	@Parameter(label = "X coordinates")
	private double[] x;

	@Parameter(label = "Y coordinates")
	private double[] y;

	@Parameter(label = "Frames")
	private double[] frame;

	@Parameter(label = "Track IDs")
	private double[] trackID;

	@Parameter
	private OpService ops;

	@Parameter(type = ItemIO.OUTPUT)
	private Img<UnsignedIntType> output;

	@Override
	public void run() {
		// Create Labeling with same dimensions as input image
		// output = ops.create().imgLabeling(input);
		output = ops.create().img(input, new UnsignedIntType());

		double xScale = input.averageScale(0);
		double yScale = input.averageScale(1);

		RandomAccess<UnsignedIntType> randomAccess = output.randomAccess();
		int[] coords = new int[3];
		for (int i = 0; i < x.length; i++) {
			coords[0] = (int) (x[i] / xScale);
			coords[1] = (int) (y[i] / yScale);
			coords[2] = (int) frame[i];
			randomAccess.setPosition(coords);
			randomAccess.get().setInteger((int) trackID[i] + 1);
		}

	}
}
