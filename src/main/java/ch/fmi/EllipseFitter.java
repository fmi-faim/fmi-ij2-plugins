package ch.fmi;

import ij.ImagePlus;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, headless = true, menuPath = "FMI>Center of Ellipse Fit")
public class EllipseFitter implements Command {
	@Parameter(label="Input image")
	private ImagePlus imp;

	@Parameter(type=ItemIO.OUTPUT)
	private double centerX;
	
	@Parameter(type=ItemIO.OUTPUT)
	private double centerY;

	@Parameter(type=ItemIO.OUTPUT)
	private double[] testOut;

	@Override
	public void run() {
		// Get points from binary mask (each white pixel will get added)
		// TODO Get Cursor from Img<T>, for all pixels != background: add point
		
		// Fit ellipse to point cloud, using Michael Doube's EllipseFit
		
		// Get center coordinates
		
		
		// DEBUG Return fake coordinates
		centerX=imp.getWidth()/2.0;
		centerY=imp.getHeight()/2.0;
		
		testOut = new double[2];
		testOut[0] = 0.1;
		testOut[1] = 1.345;
	}
}
