/*-
 * #%L
 * A collection of plugins developed at the FMI Basel.
 * %%
 * Copyright (C) 2016 - 2020 FMI Basel
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

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
