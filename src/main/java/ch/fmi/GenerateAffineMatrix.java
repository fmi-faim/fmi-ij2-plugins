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

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, headless = true, menuPath = "FMI>Generate 3D Translation/Rotation Matrix")
public class GenerateAffineMatrix implements Command {

	@Parameter
	private double translationX;

	@Parameter
	private double translationY;

	@Parameter
	private double translationZ;

	@Parameter(label="Angle (degree)")
	private double angle;

	@Parameter(choices = { "x", "y", "z" })
	private String axis;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] matrix;

	@Override
	public void run() {
		matrix = new double[12];

		matrix[0] = 1.0d;
		matrix[1] = 0.0d;
		matrix[2] = 0.0d;
		matrix[3] = translationX;

		matrix[4] = 0.0d;
		matrix[5] = 1.0d;
		matrix[6] = 0.0d;
		matrix[7] = translationY;

		matrix[8] = 0.0d;
		matrix[9] = 0.0d;
		matrix[10] = 1.0d;
		matrix[11] = translationZ;
		
		double sin = Math.sin(Math.toRadians(angle));
		double cos = Math.cos(Math.toRadians(angle));
		
		switch (axis) {
		case "x":
			matrix[5] = cos;
			matrix[6] = -sin;
			matrix[9] = sin;
			matrix[10] = cos;
			break;
		case "y":
			matrix[0] = cos;
			matrix[8] = -sin;
			matrix[2] = sin;
			matrix[10] = cos;
			break;
		case "z":
		default:
			matrix[0] = cos;
			matrix[1] = -sin;
			matrix[4] = sin;
			matrix[5] = cos;
			break;
		}
	}
}
