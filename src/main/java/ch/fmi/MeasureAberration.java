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

import com.google.common.primitives.Doubles;

import java.util.ArrayList;
import java.util.Vector;

import mpicbg.imglib.algorithm.scalespace.DifferenceOfGaussian.SpecialPoint;
import mpicbg.imglib.algorithm.scalespace.DifferenceOfGaussianPeak;
import mpicbg.imglib.type.numeric.real.FloatType;
import mpicbg.models.AbstractAffineModel3D;
import mpicbg.models.AffineModel3D;
import mpicbg.models.InvertibleBoundable;
import mpicbg.models.Point;
import mpicbg.models.PointMatch;
import mpicbg.models.RigidModel3D;
import mpicbg.models.SimilarityModel3D;
import mpicbg.models.TranslationModel3D;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import plugin.DescriptorParameters;
import process.ComparePair;
import process.Matching;

@Plugin(type = Command.class, headless = true, menuPath = "FMI>Measure 3D Transformation Between Spots")
public class MeasureAberration implements Command {
	final static private String TRANSLATION = "Translation";
	final static private String RIGID = "Rigid";
	final static private String SIMILARITY = "Similarity";
	final static private String AFFINE = "Affine";

	@Parameter
	private LogService log;

	@Parameter(label = "Type of transformation", choices = { TRANSLATION,
			RIGID, SIMILARITY, AFFINE })
	private String transformType;

	@Parameter(label = "Set 1 - X Coordinates")
	private double[] x1;

	@Parameter(label = "Set 1 - Y Coordinates")
	private double[] y1;

	@Parameter(label = "Set 1 - Z Coordinates")
	private double[] z1;

	@Parameter(label = "Set 2 - X Coordinates")
	private double[] x2;

	@Parameter(label = "Set 2 - Y Coordinates")
	private double[] y2;

	@Parameter(label = "Set 2 - Z Coordinates")
	private double[] z2;

	@Parameter(type = ItemIO.OUTPUT)
	private int nRemaining;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] ix1;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] iy1;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] iz1;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] ix2;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] iy2;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] iz2;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] cx1;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] cy1;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] cz1;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] distances;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] correctedDistances;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] affine;

	@Override
	public void run() {
		// Create Peaks list for first set
		ArrayList<DifferenceOfGaussianPeak<FloatType>> spotList1 = populateSpotList(
				x1, y1, z1);

		ArrayList<DifferenceOfGaussianPeak<FloatType>> spotList2 = populateSpotList(
				x2, y2, z2);

		ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>> listOfSpotLists = new ArrayList<>();
		listOfSpotLists.add(spotList1);
		listOfSpotLists.add(spotList2);

		DescriptorParameters params = new DescriptorParameters();
		switch (transformType) {
		case TRANSLATION:
			params.model = new TranslationModel3D();
			break;
		case RIGID:
			params.model = new RigidModel3D();
			break;
		case SIMILARITY:
			params.model = new SimilarityModel3D();
			break;
		case AFFINE:
		default:
			params.model = new AffineModel3D();
			break;
		}
		params.dimensionality = 3;
		params.localization = 1;
		params.numNeighbors = 3;
		params.significance = 3;
		params.similarOrientation = true;
		params.ransacThreshold = 5;
		params.channel1 = 0;
		params.channel2 = 0;
		params.redundancy = 1;
		params.fuse = 2; // no Overlay image

		Vector<ComparePair> pair = Matching.descriptorMatching(listOfSpotLists,
				2, params, 1.0f); // what about the original coordinates? changed?

		ArrayList<Double> distanceList = new ArrayList<>();
		ArrayList<Double> cDistanceList = new ArrayList<>();
		ArrayList<Double> x1List = new ArrayList<>();
		ArrayList<Double> y1List = new ArrayList<>();
		ArrayList<Double> z1List = new ArrayList<>();
		ArrayList<Double> x2List = new ArrayList<>();
		ArrayList<Double> y2List = new ArrayList<>();
		ArrayList<Double> z2List = new ArrayList<>();
		ArrayList<Double> cx1List = new ArrayList<>();
		ArrayList<Double> cy1List = new ArrayList<>();
		ArrayList<Double> cz1List = new ArrayList<>();

		ArrayList<PointMatch> matches = pair.get(0).inliers;
		nRemaining = matches.size();
		for (PointMatch match : matches) {
			// get local, immutable coordinates
			Point p1 = match.getP1();
			Point p2 = match.getP2();
			double[] p1loc = p1.getL();
			double[] p2loc = p2.getL();
			distanceList.add(Point.localDistance(p1, p2));
			x1List.add(p1loc[0]);
			y1List.add(p1loc[1]);
			z1List.add(p1loc[2]);
			x2List.add(p2loc[0]);
			y2List.add(p2loc[1]);
			z2List.add(p2loc[2]);
		}

		ix1 = Doubles.toArray(x1List);
		iy1 = Doubles.toArray(y1List);
		iz1 = Doubles.toArray(z1List);
		ix2 = Doubles.toArray(x2List);
		iy2 = Doubles.toArray(y2List);
		iz2 = Doubles.toArray(z2List);

		distances = Doubles.toArray(distanceList);

		ArrayList<InvertibleBoundable> modelList = Matching.globalOptimization(
				pair, 2, params); // this will modify the points inside pair!

		AbstractAffineModel3D<?> model = (AbstractAffineModel3D<?>) modelList
				.get(1);

		affine = model.getMatrix(affine);

		// TODO use static PointMatch.apply(Collection, model) to get new points
		// and distances?

		for (PointMatch match : matches) {
			double[] p1loc = match.getP1().getW(); // this should be the
													// transformed coordinates?
			cDistanceList.add(match.getDistance());
			cx1List.add(p1loc[0]);
			cy1List.add(p1loc[1]);
			cz1List.add(p1loc[2]);
		}

		cx1 = Doubles.toArray(cx1List);
		cy1 = Doubles.toArray(cy1List);
		cz1 = Doubles.toArray(cz1List);

		correctedDistances = Doubles.toArray(cDistanceList);
	}

	private ArrayList<DifferenceOfGaussianPeak<FloatType>> populateSpotList(
			double[] x, double[] y, double[] z) {
		ArrayList<DifferenceOfGaussianPeak<FloatType>> spotList = new ArrayList<>();
		FloatType f = new FloatType();
		for (int i = 0; i < x.length; i++) {
			int[] loc = new int[3];
			loc[0] = (int) x[i];
			loc[1] = (int) y[i];
			loc[2] = (int) z[i];
			DifferenceOfGaussianPeak<FloatType> spot = new DifferenceOfGaussianPeak<>(
					loc, f, SpecialPoint.MAX);
			spot.setSubPixelLocationOffset((float) (x[i] - loc[0]), 0);
			spot.setSubPixelLocationOffset((float) (y[i] - loc[1]), 1);
			spot.setSubPixelLocationOffset((float) (z[i] - loc[2]), 2);
			spotList.add(spot);
		}
		return spotList;
	}
}
