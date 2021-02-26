/*-
 * #%L
 * A collection of plugins developed at the FMI Basel.
 * %%
 * Copyright (C) 2016 - 2021 FMI Basel
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
