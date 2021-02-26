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
package org.doube.geometry;

/**
 *  FitEllipse Copyright 2009 2010 Michael Doube
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

/**
 * Ellipse-fitting methods.
 *
 * @author Michael Doube
 *
 */
public class FitEllipse {

	/**
	 * Java port of Chernov's MATLAB implementation of the direct ellipse fit
	 *
	 * @param points
	 *            n * 2 array of 2D coordinates.
	 * @return
	 *         6-element array, {a b c d f g}, which are the algebraic
	 *         parameters of the fitting ellipse: <i>ax</i><sup>2</sup> + 2
	 *         <i>bxy</i> + <i>cy</i><sup>2</sup> +2<i>dx</i> + 2<i>fy</i> +
	 *         <i>g</i> = 0. The vector <b>A</b> represented in the array is
	 *         normed, so that ||<b>A</b>||=1.
	 *
	 * @see
	 *      <a href=
	 *      "http://www.mathworks.co.uk/matlabcentral/fileexchange/22684-ellipse-fit-direct-method"
	 *      >MATLAB script</a>
	 */
	public static double[] direct(final double[][] points) {
		final int nPoints = points.length;
		final double[] centroid = Centroid.getCentroid(points);
		final double xC = centroid[0];
		final double yC = centroid[1];
		final double[][] d1 = new double[nPoints][3];
		for (int i = 0; i < nPoints; i++) {
			final double xixC = points[i][0] - xC;
			final double yiyC = points[i][1] - yC;
			d1[i][0] = xixC * xixC;
			d1[i][1] = xixC * yiyC;
			d1[i][2] = yiyC * yiyC;
		}
		final Matrix D1 = new Matrix(d1);
		final double[][] d2 = new double[nPoints][3];
		for (int i = 0; i < nPoints; i++) {
			d2[i][0] = points[i][0] - xC;
			d2[i][1] = points[i][1] - yC;
			d2[i][2] = 1;
		}
		final Matrix D2 = new Matrix(d2);

		final Matrix S1 = D1.transpose().times(D1);

		final Matrix S2 = D1.transpose().times(D2);

		final Matrix S3 = D2.transpose().times(D2);

		final Matrix T = (S3.inverse().times(-1)).times(S2.transpose());

		final Matrix M = S1.plus(S2.times(T));

		final double[][] m = M.getArray();
		final double[][] n = { { m[2][0] / 2, m[2][1] / 2, m[2][2] / 2 }, { -m[1][0], -m[1][1], -m[1][2] },
				{ m[0][0] / 2, m[0][1] / 2, m[0][2] / 2 } };

		final Matrix N = new Matrix(n);

		final EigenvalueDecomposition E = N.eig();
		final Matrix eVec = E.getV();

		final Matrix R1 = eVec.getMatrix(0, 0, 0, 2);
		final Matrix R2 = eVec.getMatrix(1, 1, 0, 2);
		final Matrix R3 = eVec.getMatrix(2, 2, 0, 2);

		final Matrix cond = (R1.times(4)).arrayTimes(R3).minus(R2.arrayTimes(R2));

		int f = 0;
		for (int i = 0; i < 3; i++) {
			if (cond.get(0, i) > 0) {
				f = i;
				break;
			}
		}
		final Matrix A1 = eVec.getMatrix(0, 2, f, f);

		Matrix A = new Matrix(6, 1);
		A.setMatrix(0, 2, 0, 0, A1);
		A.setMatrix(3, 5, 0, 0, T.times(A1));

		final double[] a = A.getColumnPackedCopy();
		final double a4 = a[3] - 2 * a[0] * xC - a[1] * yC;
		final double a5 = a[4] - 2 * a[2] * yC - a[1] * xC;
		final double a6 = a[5] + a[0] * xC * xC + a[2] * yC * yC + a[1] * xC * yC - a[3] * xC - a[4] * yC;
		A.set(3, 0, a4);
		A.set(4, 0, a5);
		A.set(5, 0, a6);
		A = A.times(1 / A.normF());
		return A.getColumnPackedCopy();
	}

	/**
	 * <p>
	 * Convert variables a, b, c, d, f, g from the general ellipse equation ax²
	 * + bxy + cy² +dx + fy + g = 0 into useful geometric parameters semi-axis
	 * lengths, centre and angle of rotation.
	 * </p>
	 *
	 * @see
	 *      <a href="http://mathworld.wolfram.com/Ellipse.html">Eq. 19-23 at Wolfram
	 *      Mathworld Ellipse</a>
	 *
	 * @param ellipse
	 *            array containing a, b, c, d, f, g of the ellipse equation.
	 * @return
	 *         array containing centroid coordinates, axis lengths and angle of
	 *         rotation of the ellipse specified by the input variables.
	 */
	public static double[] varToDimensions(final double[] ellipse) {
		final double a = ellipse[0];
		final double b = ellipse[1] / 2;
		final double c = ellipse[2];
		final double d = ellipse[3] / 2;
		final double f = ellipse[4] / 2;
		final double g = ellipse[5];

		// centre
		final double cX = (c * d - b * f) / (b * b - a * c);
		final double cY = (a * f - b * d) / (b * b - a * c);

		// semiaxis length
		final double af = 2 * (a * f * f + c * d * d + g * b * b - 2 * b * d * f - a * c * g);

		final double aL = Math.sqrt((af) / ((b * b - a * c) * (Math.sqrt((a - c) * (a - c) + 4 * b * b) - (a + c))));

		final double bL = Math.sqrt((af) / ((b * b - a * c) * (-Math.sqrt((a - c) * (a - c) + 4 * b * b) - (a + c))));
		double phi = 0;
		if (b == 0) {
			if (a <= c)
				phi = 0;
			else if (a > c)
				phi = Math.PI / 2;
		} else {
			if (a < c)
				phi = Math.atan(2 * b / (a - c)) / 2;
			else if (a > c)
				phi = Math.atan(2 * b / (a - c)) / 2 + Math.PI / 2;
		}
		final double[] dimensions = { cX, cY, aL, bL, phi };
		return dimensions;
	}
}
