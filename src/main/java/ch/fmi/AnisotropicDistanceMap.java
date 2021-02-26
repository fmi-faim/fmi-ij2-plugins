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

import java.util.stream.IntStream;

import net.imagej.Dataset;
import net.imagej.ImgPlus;
import net.imagej.ops.OpService;
import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Image.DistanceTransform;
import net.imagej.ops.convert.ConvertImages;
import net.imglib2.algorithm.morphology.distance.DistanceTransform.DISTANCE_TYPE;
import net.imglib2.converter.read.ConvertedRandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
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
	
	private static final String OPS = "ImageJ-Ops";
	private static final String IMGLIB = "Imglib2";

	@Parameter
	private OpService ops;

	@Parameter(label = "Input image")
	private Dataset input;
	
	@Parameter(label = "Implemention to use", choices = {OPS, IMGLIB}, required = false)
	private String implementation = OPS;

	@Parameter(type = ItemIO.OUTPUT)
	private ImgPlus<?> output;

	@SuppressWarnings("unchecked")
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

		Img<?> outImg = null;
		switch (implementation) {
		case OPS:
		default:
			// Convert to BitType image
			// (required for working with 8-bit "binary" images in ImageJ 1.x)
			Img<BitType> bitImg = (Img<BitType>) ops.run(
					ConvertImages.Bit.class, input);

			outImg = (Img<?>) ops.run(DistanceTransform.class,
					Views.zeroMin(bitImg), cal);
			break;
		case IMGLIB:
			FloatType type = new FloatType();
			ConvertedRandomAccessibleInterval<RealType<?>, FloatType> conv = new ConvertedRandomAccessibleInterval<>(
					input, (s, t) -> t.set(s.getRealDouble() > 0.0 ? 100000000 : 0.0f),
					type);
			outImg = (Img<?>) ops.run(Ops.Create.Img.class, conv);
			net.imglib2.algorithm.morphology.distance.DistanceTransform
					.transform(conv, (Img<FloatType>) outImg,
							DISTANCE_TYPE.EUCLIDIAN, cal);
			break;
		}

		output = new ImgPlus<>(outImg, input, false);
	}
}
