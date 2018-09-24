
package ch.fmi;

import ij.ImagePlus;
import ij.ImageStack;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.morphology.MinimaAndMaxima3D;
import inra.ijpb.watershed.Watershed;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, headless = true,
	menuPath = "FMI>Morphological Segmentation (gradient image)")
public class MorphoLibJSegmentation implements Command {

	@Parameter(label = "Input image")
	private ImagePlus imp;

	@Parameter(label = "Tolerance")
	private Double tolerance;

	@Parameter(type = ItemIO.OUTPUT)
	private ImagePlus resultImp;

	@Override
	public void run() {
		int connectivity = 6; // or 26

		ImageStack stack = imp.getStack();

		ImageStack regionalMinima = MinimaAndMaxima3D.extendedMinima(stack,
			tolerance, connectivity);
		ImageStack imposedMinima = MinimaAndMaxima3D.imposeMinima(stack,
			regionalMinima, connectivity);
		ImageStack labeledMinima = BinaryImages.componentsLabeling(regionalMinima,
			connectivity, 32);
		ImageStack resultStack = Watershed.computeWatershed(imposedMinima,
			labeledMinima, connectivity, false);

		resultImp = new ImagePlus("Result", resultStack);
		resultImp.setCalibration(imp.getCalibration());
	}
}
