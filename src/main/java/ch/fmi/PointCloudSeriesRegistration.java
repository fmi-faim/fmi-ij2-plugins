package ch.fmi;

import com.google.common.primitives.Doubles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.stream.Collectors;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import mpicbg.imglib.algorithm.scalespace.DifferenceOfGaussian.SpecialPoint;
import mpicbg.imglib.algorithm.scalespace.DifferenceOfGaussianPeak;
import mpicbg.imglib.type.numeric.real.FloatType;
import mpicbg.models.AbstractAffineModel3D;
import mpicbg.models.AbstractModel;
import mpicbg.models.AffineModel2D;
import mpicbg.models.AffineModel3D;
import mpicbg.models.InvertibleBoundable;
import mpicbg.models.RigidModel2D;
import mpicbg.models.RigidModel3D;
import mpicbg.models.SimilarityModel2D;
import mpicbg.models.SimilarityModel3D;
import mpicbg.models.TranslationModel2D;
import mpicbg.models.TranslationModel3D;
import plugin.DescriptorParameters;
import process.ComparePair;
import process.Matching;

@Plugin(type = Command.class, headless = true,
	menuPath = "FMI>Register Series of Point Clouds")
public class PointCloudSeriesRegistration extends ContextCommand {

	final static protected String TRANSLATION = "Translation";
	final static protected String RIGID = "Rigid";
	final static protected String SIMILARITY = "Similarity";
	final static protected String AFFINE = "Affine";
	final static protected String DIM2D = "2D";
	final static protected String DIM3D = "3D";

	@Parameter
	private LogService log;

	@Parameter(label = "Type of Transformation", choices = { TRANSLATION, RIGID,
		SIMILARITY, AFFINE })
	private String transformType;

	@Parameter(label = "Dimensionality", choices = { DIM2D, DIM3D })
	private String dim;

	@Parameter(label = "Frame Numbers")
	private double[] frame;

	@Parameter(label = "X Coordinates")
	private double[] xCoords;

	@Parameter(label = "Y Coordinates")
	private double[] yCoords;

	@Parameter(label = "Z Coordinates", required = false)
	private double[] zCoords = null;

	@Parameter(label = "Range")
	private int range = 10;

	// --- OUTPUTS ---

	@Parameter(type = ItemIO.OUTPUT)
	private int[] frameList;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] flatModels;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] modelCosts;

	// TODO: update to newer API? using ImgLib2 FloatType etc.?
	ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>> peaks;

	// List of matrices
	// List of frames

	@Override
	public void run() {
		if (!(frame.length == xCoords.length && frame.length == yCoords.length)) {
			log.error("All input vectors have to be same length.");
			return;
		}

		// create peakListList
		createPeaks();
		int nFrames = peaks.size();
		DescriptorParameters params = defaultParameters();
		params.model = suitableModel(dim, transformType);
		params.range = range;
		
		Vector<ComparePair> comparePairs = Matching.descriptorMatching(peaks, nFrames, params, 1.0f);
		ArrayList<InvertibleBoundable> models = Matching.globalOptimization(comparePairs, nFrames, params);
		flatModels = flattenModels(models);
	}

	private AbstractModel<?> suitableModel(String d, String transform) {
		switch (transform) {
			case TRANSLATION:
				return d.equals(DIM2D) ? new TranslationModel2D()
					: new TranslationModel3D();
			case RIGID:
				return d.equals(DIM2D) ? new RigidModel2D() : new RigidModel3D();
			case SIMILARITY:
				return d.equals(DIM2D) ? new SimilarityModel2D()
					: new SimilarityModel3D();
			case AFFINE:
			default:
				return d.equals(DIM2D) ? new AffineModel2D() : new AffineModel3D();
		}
	}

	private double[] flattenModels(List<InvertibleBoundable> models) {
		List<Double> list = new ArrayList<>();
		for (InvertibleBoundable m : models) {
			double[] matrix = new double[12];
			// TODO allow 2D model here!
			// see ModelFitter for mapping of 2D/3D toMatrix order to affine
			((AbstractAffineModel3D<?>) m).getMatrix(matrix);
			list.addAll(Arrays.stream(matrix).boxed().collect(Collectors.toList()));
		}
		return Doubles.toArray(list);
	}

	private DescriptorParameters defaultParameters() {
		DescriptorParameters params = new DescriptorParameters();
		params.dimensionality = 3;
		// params.localization = 1;
		params.numNeighbors = 2;
		params.significance = 3;
		params.similarOrientation = true;
		params.ransacThreshold = 3;
		// params.channel1 = 0;
		// params.channel2 = 0;
		params.redundancy = 3;
		// params.fuse = 2; // no Overlay image
		params.globalOpt = 1;
		params.range = 10;
		params.silent = true;
		return params;
	}

	private void createPeaks()
	{
		List<Integer> list = new ArrayList<>(frame.length);
		Arrays.stream(frame).forEach(v -> list.add((int) v));
		SortedSet<Integer> frameSet = new TreeSet<>(list);

		// NB: KNIME only supports int[] output, but for SortedSet we need boxed types
		Integer[] frameArray = new Integer[frameSet.size()];
		frameSet.toArray(frameArray);
		List<Integer> frameLookup = Arrays.asList(frameArray);
		frameList = Arrays.stream(frameArray).mapToInt(Integer::intValue).toArray();

		peaks = new ArrayList<>(frameSet.size());
		for (int i=0; i < frameSet.size(); i++) {
			peaks.add(new ArrayList<>());
		}

		for (int i=0; i < frame.length; i++) {
			peaks.get(frameLookup.indexOf((int) frame[i])).add(createPeak(xCoords[i], yCoords[i], zCoords[i]));
		}
	}

	private DifferenceOfGaussianPeak<FloatType> createPeak(double x, double y,
		double z)
	{
		int[] loc = new int[] { (int) x, (int) y, (int) z };
		DifferenceOfGaussianPeak<FloatType> p = new DifferenceOfGaussianPeak<>(loc,
			new FloatType(), SpecialPoint.MAX);
		p.setSubPixelLocationOffset((float) (x - loc[0]), 0);
		p.setSubPixelLocationOffset((float) (y - loc[1]), 1);
		p.setSubPixelLocationOffset((float) (z - loc[2]), 2);
		return p;
	}

}
