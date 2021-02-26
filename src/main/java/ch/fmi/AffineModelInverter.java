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

import net.imglib2.realtransform.AffineTransform3D;

@Plugin(type = Command.class, headless = true, menuPath = "FMI>Invert Affine Matrix")
public class AffineModelInverter implements Command {
	@Parameter(label = "Input affine transformation matrix")
	private double[] mIn;

	@Parameter(type = ItemIO.OUTPUT, label = "Output affine transformation matrix")
	private double[] mOut;

	@Override
	public void run() {
		AffineTransform3D transform = new AffineTransform3D();
		transform.set(mIn[0], mIn[1], mIn[2], mIn[3], mIn[4], mIn[5], mIn[6], mIn[7], mIn[8],
				mIn[9], mIn[10], mIn[11]);

		mOut = new double[12];
		transform.inverse().toArray(mOut);
	}
}
