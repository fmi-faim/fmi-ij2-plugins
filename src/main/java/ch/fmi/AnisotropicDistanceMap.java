package ch.fmi;

import java.util.stream.IntStream;

import net.imagej.Dataset;
import net.imagej.ImgPlus;
import net.imagej.ops.OpService;
import net.imagej.ops.Ops.Image.DistanceTransform;
import net.imagej.ops.convert.ConvertImages;
import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;
import net.imglib2.view.Views;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Create a Euclidean distance map from a binary image, taking into account the
 * actual image calibration.
 * 
 * @author Jan Eglinger
 *
 */
@Plugin(type = Command.class, headless = true, menuPath = "FMI>Distance Map (with Calibration)")
public class AnisotropicDistanceMap extends ContextCommand {

	@Parameter
	private OpService ops;

	@Parameter(label = "Input image")
	private Dataset input;

	@Parameter(type = ItemIO.OUTPUT)
	private ImgPlus<?> output;

	@Override
	public void run() {
		/*
		 * double[] cal = new double[input.numDimensions()];
		 * 
		 * for (int d = 0; d < cal.length; d++) { cal[d] =
		 * input.averageScale(d); }
		 */

		double[] cal = IntStream.range(0, input.numDimensions())
				.mapToDouble(d -> input.averageScale(d)).toArray();

		// Convert to BitType image
		// (required for working with 8-bit "binary" images in ImageJ 1.x)
		@SuppressWarnings("unchecked")
		Img<BitType> bitImg = (Img<BitType>) ops.run(ConvertImages.Bit.class,
				input);

		Img<?> outImg = (Img<?>) ops.run(DistanceTransform.class, Views.zeroMin(bitImg), cal);

		output = new ImgPlus<>(outImg, input, false);
	}
}
