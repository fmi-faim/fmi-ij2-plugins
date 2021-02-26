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
import mpicbg.models.IllDefinedDataPointsException;
import mpicbg.models.InvertibleBoundable;
import mpicbg.models.NotEnoughDataPointsException;
import mpicbg.models.RigidModel2D;
import mpicbg.models.RigidModel3D;
import mpicbg.models.SimilarityModel2D;
import mpicbg.models.SimilarityModel3D;
import mpicbg.models.Tile;
import mpicbg.models.TileConfiguration;
import mpicbg.models.TranslationModel2D;
import mpicbg.models.TranslationModel3D;
import plugin.DescriptorParameters;
import process.ComparePair;
import process.Matching;

@Plugin(type = Command.class, headless = true,
	menuPath = "FMI>Register Series of Point Clouds (with matching)")
public class PointCloudSeriesRegistration <M extends AbstractModel<M>> extends ContextCommand {

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

	@Parameter(label = "Frame Numbers", required = false)
	private double[] frame;

	@Parameter(label = "X Coordinates", required = false)
	private double[] xCoords;

	@Parameter(label = "Y Coordinates", required = false)
	private double[] yCoords;

	@Parameter(label = "Z Coordinates", required = false)
	private double[] zCoords = null;

	@Parameter(label = "Range", required = false)
	private Integer range = 10;

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

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		if (!(frame.length == xCoords.length && frame.length == yCoords.length)) {
			throw new IllegalArgumentException("All input vectors have to be same length.");
		}

		// create peakListList
		createPeaks();
		int nFrames = peaks.size();
		DescriptorParameters params = defaultParameters();
		params.model = suitableModel(dim, transformType);
		params.range = range;
		
		Vector<ComparePair> comparePairs = Matching.descriptorMatching(peaks, nFrames, params, 1.0f);
		//ArrayList<InvertibleBoundable> models = Matching.globalOptimization(comparePairs, nFrames, params);

		final ArrayList<Tile<?>> tiles = new ArrayList<>();
		// initialize tiles
		for ( int t = 0; t < nFrames; ++t )
		  tiles.add( new Tile<>( (M) params.model.copy() ) );
		// TODO restore coordinates (needed?)
		// add point matches to respective tiles
		for ( final ComparePair pair : comparePairs )
			Matching.addPointMatches( pair.inliers, tiles.get( pair.indexA ), tiles.get( pair.indexB ) );

		final TileConfiguration tc = new TileConfiguration();

		// add tiles / fix first tile
		boolean fixed = false;
		for (Tile<?> t : tiles) {
			if (t.getConnectedTiles().size() > 0) {
				tc.addTile(t);
				if (!fixed) {
					tc.fixTile(t);
					fixed = true;
				}
			}
		}
		
		try {
			tc.preAlign();
			tc.optimize(10, 10000, 200);
		}
		catch (NotEnoughDataPointsException exc) {
			throw new RuntimeException("Not enough data points.", exc);
		}
		catch (IllDefinedDataPointsException exc) {
			throw new RuntimeException("Ill-defined data points.", exc);
		}

		// loop through tiles: models.add(tile.getModel()) / params.model.copy() if not connected
		List<InvertibleBoundable> models = new ArrayList<>();
		List<Double> costs = new ArrayList<>();
		InvertibleBoundable lastModel = null;
		for (Tile<?> t : tiles) {
			if (t.getConnectedTiles().size() > 0) {
				lastModel = (InvertibleBoundable) t.getModel();
				models.add(lastModel);
				costs.add(t.getCost());
			} else {
				// models.add((InvertibleBoundable) params.model.copy());
				models.add(lastModel);
				costs.add(0.0);
			}
		}
		// errors.add(tile.getCost()) / or tile.getModel().getCost() ? and difference?

		flatModels = flattenModels(models);
		modelCosts = Doubles.toArray(costs);
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
