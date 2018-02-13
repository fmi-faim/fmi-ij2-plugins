package ch.fmi;

import net.imagej.ops.OpService;
import net.imagej.ops.Ops.Geometric.Contour;
import net.imglib2.img.Img;
import net.imglib2.roi.geometric.Polygon;
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
		Polygon polygon = (Polygon) opService.run(Contour.class, input, true);
		/*
		ArrayList<Double> xList = new ArrayList<>();
		ArrayList<Double> yList = new ArrayList<>();
		polygon.getVertices().forEach(vertex -> {
			xList.add(vertex.getDoublePosition(0));
			yList.add(vertex.getDoublePosition(1));
		});
		*/
		xpoints = polygon.getVertices().stream()
				.mapToDouble(vertex -> vertex.getDoublePosition(0)).toArray();
		ypoints = polygon.getVertices().stream()
				.mapToDouble(vertex -> vertex.getDoublePosition(1)).toArray();
	}
}
