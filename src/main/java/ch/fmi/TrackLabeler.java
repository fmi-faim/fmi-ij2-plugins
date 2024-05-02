/*-
 * #%L
 * A collection of plugins developed at the FMI Basel.
 * %%
 * Copyright (C) 2016 - 2024 FMI Basel
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
