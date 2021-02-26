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

import com.google.common.primitives.Doubles;

import java.util.ArrayList;

import net.imglib2.realtransform.AffineTransform3D;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, headless = true, menuPath = "FMI>Apply 3D Affine Transform to Points")
public class ApplyAffineTransformPoints implements Command {
	@Parameter(label = "x Coordinates", required = false)
	private double[] xIn;

	@Parameter(label = "y Coordinates", required = false)
	private double[] yIn;

	@Parameter(label = "z Coordinates", required = false)
	private double[] zIn;

	@Parameter(label = "Affine transformation matrix", required = false)
	private double[] m;

	@Parameter(label = "Apply inverse", required = false, persist = false)
	private boolean inverse = true;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] xOut;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] yOut;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] zOut;

	@Override
	public void run() {
		AffineTransform3D transform = new AffineTransform3D();
		transform.set(m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8],
				m[9], m[10], m[11]);

		ArrayList<Double> xList = new ArrayList<>();
		ArrayList<Double> yList = new ArrayList<>();
		ArrayList<Double> zList = new ArrayList<>();

		double[] coords = new double[3];

		for (int i = 0; i < xIn.length; i++) {
			coords[0] = xIn[i];
			coords[1] = yIn[i];
			coords[2] = zIn[i];
			if (inverse) {
				transform.applyInverse(coords, coords);
			} else {
				transform.apply(coords, coords);
			}
			xList.add(coords[0]);
			yList.add(coords[1]);
			zList.add(coords[2]);
		}

		xOut = Doubles.toArray(xList);
		yOut = Doubles.toArray(yList);
		zOut = Doubles.toArray(zList);
	}
}
