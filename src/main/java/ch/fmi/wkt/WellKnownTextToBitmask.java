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

import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccessibleRealInterval;
import net.imglib2.converter.Converters;
import net.imglib2.img.ImgView;
import net.imglib2.roi.Masks;
import net.imglib2.roi.geom.GeomMasks;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.logic.BoolType;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Plug-in to convert a polygon geometry represented in well-known text (wkt) to
 * a 2D binary mask image
 * 
 * @author Charismatic Claire
 */
@Plugin(type = Command.class, headless = true, menuPath = "FMI>Well-Known Text (WKT) to 2D Binary Mask")
public class WellKnownTextToBitmask extends ContextCommand {

    @Parameter
    private LogService logService;

    @Parameter(label = "Well-Known Text Polygons")
    private String text;

    @Parameter(type = ItemIO.OUTPUT)
    private Img<BitType> output;

    private FinalInterval envelopeToDims(Geometry envelope) throws IllegalArgumentException {
        Coordinate boxMin, boxMax;
        boxMin = envelope.getCoordinates()[0];
        if(envelope.getDimension() == 2) boxMax = envelope.getCoordinates()[2];
        else if(envelope.getDimension() == 1) boxMax = envelope.getCoordinates()[1];
        else throw new IllegalArgumentException(
                String.format("Your envelop <%s> stems from another world!", envelope.toText()));
        long[] min = new long[] { (long) boxMin.getX(), (long) boxMin.getY() };
        long[] max = new long[] { (long) boxMax.getX(), (long) boxMax.getY() };
        return new FinalInterval(min, max);
    }

    @Override
    public void run() {
        GeometryFactory factory = new GeometryFactory();
        WKTReader reader = new WKTReader(factory);
        try {
            // get GIS polygon from input
            Polygon polygon = (Polygon) reader.read(text);
            // construct bitmask
            List<RealLocalizable> points = Arrays.stream(polygon.getCoordinates())
                    .map(coordinate -> new RealPoint(coordinate.getX(), coordinate.getY()))
                    .collect(Collectors.toList());
            Polygon2D polygon2d = GeomMasks.closedPolygon2D(points);  // imglib2-roi polygon
            RealRandomAccessibleRealInterval<BoolType> bitmask = Masks.toRealRandomAccessibleRealInterval(polygon2d);
            // create view
            FinalInterval dims = envelopeToDims(polygon.getEnvelope());
            IntervalView<BoolType> view = Views.interval(Views.raster(bitmask), dims);
            // create image
            RandomAccessibleInterval<BitType> interval = Converters.convert(
                    (RandomAccessibleInterval<BoolType>) view, (in, out) -> out.set(in.get()), new BitType());
            Img<BitType> image = ImgView.wrap(interval, Util.getSuitableImgFactory(interval, new BitType()));
            // return result
            output =  image;
        } catch (ParseException e) {
            logService.error(String.format("Could not create a polygon from input <%s>", text), e);
        } catch(IllegalArgumentException e) {
            logService.error(
                    String.format("Could not define bounding box of polygon derived from input <%s>", text), e);
        }
    }

}
