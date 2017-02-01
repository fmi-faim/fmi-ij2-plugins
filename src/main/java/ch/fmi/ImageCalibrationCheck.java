package ch.fmi;

import ij.ImagePlus;
import ij.measure.Calibration;

import net.imagej.ImgPlus;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, headless = true, menuPath = "FMI>Check Image Calibration")
public class ImageCalibrationCheck<T> implements Command {
	@Parameter(label = "Input image (ImageJ 1.x)")
	private ImagePlus imp;

	@Parameter(label = "Input image (ImageJ2)")
	private ImgPlus<T> img;

	@Parameter(type = ItemIO.OUTPUT)
	private double x1;

	@Parameter(type = ItemIO.OUTPUT)
	private double y1;

	@Parameter(type = ItemIO.OUTPUT)
	private double z1;

	@Parameter(type = ItemIO.OUTPUT)
	private double t1;

	@Parameter(type = ItemIO.OUTPUT)
	private double dim0;

	@Parameter(type = ItemIO.OUTPUT)
	private double dim1;

	@Parameter(type = ItemIO.OUTPUT)
	private double dim2;

	@Parameter(type = ItemIO.OUTPUT)
	private double dim3;

	@Override
	public void run() {
		Calibration cal = imp.getCalibration();
		x1 = cal.pixelWidth;
		y1 = cal.pixelHeight;
		z1 = cal.pixelDepth;
		t1 = cal.frameInterval;

		dim0 = img.averageScale(0);
		dim1 = img.averageScale(1);
		dim2 = img.averageScale(2);
		dim3 = img.averageScale(3);
	}
}
