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

import net.imagej.ops.OpService;
import net.imagej.ops.Ops.Geometric.Contour;
import net.imglib2.img.Img;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.logic.BitType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, headless = true, menuPath = "FMI>Get Polygon Points from 2D Binary Mask")
public class ContourPolygon extends ContextCommand {

	@Parameter
	private OpService opService;

	@Parameter
	private Img<BitType> input;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] xpoints;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] ypoints;

	@Override
	public void run() {
		Polygon2D polygon = (Polygon2D) opService.run(Contour.class, input, true);
		xpoints = new double[polygon.numVertices()];
		ypoints = new double[polygon.numVertices()];		
		for (int i = 0; i < polygon.numVertices(); i++) {
			xpoints[i] = polygon.vertex(i).getDoublePosition(0);
			ypoints[i] = polygon.vertex(i).getDoublePosition(1);
		}
	}
}
