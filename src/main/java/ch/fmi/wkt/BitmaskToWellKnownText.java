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
package ch.fmi.wkt;

import net.imagej.ops.OpService;
import net.imagej.ops.Ops;
import net.imglib2.img.Img;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.logic.BitType;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Plug-in to convert a 2D binary mask image to a polygon geometry serialized to
 * well-known text (wkt)
 * 
 * @author Charismatic Claire
 */
@Plugin(type = Command.class, headless = true, menuPath = "FMI>2D Binary Mask to Well-Known Text (WKT)")
public class BitmaskToWellKnownText extends ContextCommand {

    @Parameter
    private OpService opService;

    @Parameter
    private Img<BitType> input;

    @Parameter(type = ItemIO.OUTPUT)
    private String output;

    @Override
    public void run() {
        // get imglib2-roi polygon from input
        Polygon2D polygon2d = (Polygon2D) opService.run(Ops.Geometric.Contour.class, input, true);
        // get coordinates
        int numVertices = polygon2d.numVertices();
        Coordinate[] coordinates = new Coordinate[numVertices + 1];
        for (int i = 0; i < numVertices + 1; i++) {
            Double x = polygon2d.vertex(i % numVertices).getDoublePosition(0);
            Double y = polygon2d.vertex(i % numVertices).getDoublePosition(1);
            coordinates[i] = new Coordinate(x, y);
        }
        // create GIS polygon
        GeometryFactory factory = new GeometryFactory();
        Polygon polygon = factory.createPolygon(coordinates);
        // return result
        output = polygon.toText();
    }
}
