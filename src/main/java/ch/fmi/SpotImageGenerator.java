package ch.fmi;

import java.util.Random;

import net.imagej.ImgPlus;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, headless = true, menuPath = "FMI>Generate Spot Image (2D)")
public class SpotImageGenerator implements Command {

	@Parameter
	private OpService ops;

	@Parameter(label = "Width")
	private int width;

	@Parameter(label = "Height")
	private int height;

	@Parameter(label = "Number of spots")
	private int numSpots;

	@Parameter(label = "Spot intensity")
	private double intensity;

	@Parameter(label = "Blur radius")
	private Double radius;

	@Parameter(type = ItemIO.OUTPUT)
	private ImgPlus<DoubleType> resultImg;

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		Img<DoubleType> img = ops.create().img(new int[] { width, height });

		RandomAccess<DoubleType> ra = img.randomAccess();
		Random rand = new Random();

		for (int i = 0; i < numSpots; i++) {
			ra.setPosition(new int[] { rand.nextInt(width),
					rand.nextInt(height) });
			ra.get().setReal(intensity);
		}

		RandomAccessibleInterval<DoubleType> kernel = ops.create().kernelGauss(
				radius * 2.0, 2);

		resultImg = ImgPlus.wrap((Img<DoubleType>) ops.filter().convolve(img,
				kernel));
	}

}
