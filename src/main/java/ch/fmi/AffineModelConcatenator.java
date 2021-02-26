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

import net.imglib2.realtransform.AffineTransform3D;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, headless = true,
	menuPath = "FMI>Concatenate Affine Matrices")
public class AffineModelConcatenator implements Command {

	@Parameter(label = "First input affine transformation matrix")
	private double[] mIn1;

	@Parameter(label = "Second input affine transformation matrix")
	private double[] mIn2;

	@Parameter(type = ItemIO.OUTPUT,
		label = "Output affine transformation matrix")
	private double[] mOut;

	@Override
	public void run() {
		AffineTransform3D transform1 = new AffineTransform3D();
		transform1.set(mIn1[0], mIn1[1], mIn1[2], mIn1[3], mIn1[4], mIn1[5],
			mIn1[6], mIn1[7], mIn1[8], mIn1[9], mIn1[10], mIn1[11]);

		AffineTransform3D transform2 = new AffineTransform3D();
		transform2.set(mIn2[0], mIn2[1], mIn2[2], mIn2[3], mIn2[4], mIn2[5],
			mIn2[6], mIn2[7], mIn2[8], mIn2[9], mIn2[10], mIn2[11]);

		mOut = new double[12];
		transform1.concatenate(transform2).toArray(mOut);
	}
}
