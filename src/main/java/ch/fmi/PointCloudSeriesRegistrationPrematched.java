/*-
 * #%L
 * A collection of plugins developed at the FMI Basel.
 * %%
 * Copyright (C) 2016 - 2020 FMI Basel
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import com.google.common.primitives.Doubles;

import ch.fmi.registration.Utils;
import mpicbg.imglib.algorithm.scalespace.DifferenceOfGaussianPeak;
import mpicbg.imglib.type.numeric.real.FloatType;
import mpicbg.models.AbstractModel;
import mpicbg.models.IllDefinedDataPointsException;
import mpicbg.models.InvertibleBoundable;
import mpicbg.models.NotEnoughDataPointsException;
import mpicbg.models.Tile;
import mpicbg.models.TileConfiguration;
import net.imglib2.util.Pair;
import plugin.DescriptorParameters;
import process.ComparePair;
import process.Matching;

@Plugin(type = Command.class, headless = true,
	menuPath = "FMI>Register Series of Premached Point Clouds")
public class PointCloudSeriesRegistrationPrematched <M extends AbstractModel<M>> extends ContextCommand {

	@Parameter
	private LogService log;

	@Parameter(label = "Dimensionality", choices = { Utils.DIM2D, Utils.DIM3D })
	private String dim;

	@Parameter(label = "Type of Transformation", choices = { Utils.TRANSLATION, Utils.RIGID,
		Utils.SIMILARITY, Utils.AFFINE })
	private String transformType;

	@Parameter(label = "Regularize model")
	private boolean regularize;

	@Parameter(label = "Type of Regularization", choices = { Utils.TRANSLATION, Utils.RIGID, Utils.SIMILARITY })
	private String regularizationType;

	@Parameter(label = "Regularization Lambda", required = false)
	private Double lambda = 0.1;

	@Parameter(label = "Range", required = false)
	private Integer range = 10;

	@Parameter(label = "Frame Numbers", required = false)
	private double[] frame;

	@Parameter(label = "X Coordinates", required = false)
	private double[] xCoords;

	@Parameter(label = "Y Coordinates", required = false)
	private double[] yCoords;

	@Parameter(label = "Z Coordinates", required = false)
	private double[] zCoords = null;

	@Parameter(label = "Track IDs", required = false)
	private double[] trackIDs;

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
		if (!(frame.length == xCoords.length && frame.length == yCoords.length && frame.length == trackIDs.length)) {
			throw new IllegalArgumentException("All input vectors have to be same length.");
		}

		int[] frameInt = Arrays.stream(frame).mapToInt(v -> (int) v).toArray();
		Integer[] sortedUniqueFrames = Utils.getSortedUniqueFrames(frameInt);
		// Populate output for KNIME (needs to be int[])
		frameList = Arrays.stream(sortedUniqueFrames).mapToInt(Integer::intValue).toArray();

		int[] ids = Arrays.stream(trackIDs).mapToInt(v -> (int) v).toArray();
		Pair<ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>>, List<List<Integer>>> peaksAndCorrespondences = Utils
				.getPeaksAndCorrespondencesFromArrays(Arrays.asList(sortedUniqueFrames), frameInt, xCoords, yCoords,
						dim.equals(Utils.DIM3D) ? zCoords : null, ids);
		peaks = peaksAndCorrespondences.getA();
		List<List<Integer>> correspondences = peaksAndCorrespondences.getB();

		DescriptorParameters params = defaultParameters();
		params.regularize = regularize;
		if (regularize) {
			params.model = Utils.suitableRegularizedModel(dim, transformType, regularizationType, lambda);
		} else {
			params.model = Utils.suitableModel(dim, transformType);
		}
		params.range = range;

		Vector<ComparePair> pairs = Utils.getComparePairs(sortedUniqueFrames, peaks, range, params.model);
		Utils.populateComparePairs(pairs, peaks, correspondences);

		//ArrayList<InvertibleBoundable> models = Matching.globalOptimization(pairs, peaks.size(), params);

		// Global Optimization

		final ArrayList<Tile<?>> tiles = new ArrayList<>();
		// initialize tiles
		for ( int t = 0; t < peaks.size(); ++t )
		  tiles.add( new Tile<>( (M) params.model.copy() ) );
		// TODO restore coordinates (needed?)
		// add point matches to respective tiles
		for ( final ComparePair pair : pairs )
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

		// loop through tiles: models.add(tile.getModel()) / lastModel if not connected
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

		flatModels = Utils.flattenModels(models, dim);
		modelCosts = Doubles.toArray(costs);
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
		params.lambda = 0.1;
		return params;
	}

}
