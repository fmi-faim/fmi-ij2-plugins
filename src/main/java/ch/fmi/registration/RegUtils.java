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
package ch.fmi.registration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import com.google.common.primitives.Doubles;

import mpicbg.imglib.algorithm.scalespace.DifferenceOfGaussian.SpecialPoint;
import mpicbg.imglib.algorithm.scalespace.DifferenceOfGaussianPeak;
import mpicbg.imglib.type.numeric.real.FloatType;
import mpicbg.models.AbstractAffineModel2D;
import mpicbg.models.AbstractAffineModel3D;
import mpicbg.models.AbstractModel;
import mpicbg.models.Affine2D;
import mpicbg.models.Affine3D;
import mpicbg.models.AffineModel2D;
import mpicbg.models.AffineModel3D;
import mpicbg.models.IllDefinedDataPointsException;
import mpicbg.models.InterpolatedAffineModel2D;
import mpicbg.models.InterpolatedAffineModel3D;
import mpicbg.models.InvertibleBoundable;
import mpicbg.models.Model;
import mpicbg.models.NotEnoughDataPointsException;
import mpicbg.models.PointMatch;
import mpicbg.models.RigidModel2D;
import mpicbg.models.RigidModel3D;
import mpicbg.models.SimilarityModel2D;
import mpicbg.models.SimilarityModel3D;
import mpicbg.models.TranslationModel2D;
import mpicbg.models.TranslationModel3D;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import process.ComparePair;
import process.Particle;

public class RegUtils {
	private RegUtils() {
		// prevent instantiation of static utility class
	}

	final static public String TRANSLATION = "Translation";
	final static public String RIGID = "Rigid";
	final static public String SIMILARITY = "Similarity";
	final static public String AFFINE = "Affine";
	final static public String DIM2D = "2D";
	final static public String DIM3D = "3D";

	public static Vector<ComparePair> getComparePairs(Integer[] frameLookup,
			ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>> peakList, int range, Model<?> model) {

		final Vector<ComparePair> pairs = new Vector<>();

		int nFrames = peakList.size();

		for (int a = 0; a < nFrames; a++) {
			for (int b = a + 1; b < nFrames; b++) {
				if (Math.abs(frameLookup[a] - frameLookup[b]) <= range)
					pairs.add(new ComparePair(a, b, model));
			}
		}

		return pairs;
	}

	public static void populateComparePairs(Vector<ComparePair> pairs,
			ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>> peaks, List<List<Integer>> correspondences) {
		for (ComparePair pair : pairs) {
			ArrayList<DifferenceOfGaussianPeak<FloatType>> peaksA = peaks.get(pair.indexA);
			ArrayList<DifferenceOfGaussianPeak<FloatType>> peaksB = peaks.get(pair.indexB);
			List<Integer> idsA = correspondences.get(pair.indexA);
			List<Integer> idsB = correspondences.get(pair.indexB);

			int id = 0;
			for (int a = 0; a < peaksA.size(); a++) {
				for (int b = 0; b < peaksB.size(); b++) {
					if (idsA.get(a).equals(idsB.get(b))) {
						pair.inliers.add(new PointMatch(new Particle(id++, peaksA.get(a), 1.0f),
								new Particle(id++, peaksB.get(b), 1.0f)));
					}
				}
			}

			try {
				pair.model.fit(pair.inliers);
			} catch (NotEnoughDataPointsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllDefinedDataPointsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static Integer[] getSortedUniqueFrames(int[] frames) {
		return Arrays.stream(frames).distinct().sorted().boxed().toArray(Integer[]::new);
	}

	public static Pair<ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>>, List<List<Integer>>> getPeaksAndCorrespondencesFromArrays(
			List<Integer> sortedUniqueFrames, int[] frames, double[] x, double[] y, double[] z, int[] correspondences) {
		if (z == null)
			return getPeaksAndCorrespondencesFromArrays(sortedUniqueFrames, frames, x, y, correspondences);

		ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>> peaks = new ArrayList<>(sortedUniqueFrames.size());
		List<List<Integer>> ids = new ArrayList<>(sortedUniqueFrames.size());

		for (int i = 0; i < sortedUniqueFrames.size(); i++) {
			peaks.add(new ArrayList<>());
			ids.add(new ArrayList<>());
		}

		for (int i = 0; i < frames.length; i++) {
			peaks.get(sortedUniqueFrames.indexOf(frames[i])).add(createPeak(x[i], y[i], z[i]));
			ids.get((sortedUniqueFrames.indexOf(frames[i]))).add(correspondences[i]);
		}
		return new ValuePair<ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>>, List<List<Integer>>>(peaks, ids);
	}

	public static Pair<ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>>, List<List<Integer>>> getPeaksAndCorrespondencesFromArrays(
			List<Integer> sortedUniqueFrames, int[] frames, double[] x, double[] y, int[] correspondences) {

		ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>> peaks = new ArrayList<>(sortedUniqueFrames.size());
		List<List<Integer>> ids = new ArrayList<>(sortedUniqueFrames.size());

		for (int i = 0; i < sortedUniqueFrames.size(); i++) {
			peaks.add(new ArrayList<>());
			ids.add(new ArrayList<>());
		}

		for (int i = 0; i < frames.length; i++) {
			peaks.get(sortedUniqueFrames.indexOf(frames[i])).add(createPeak(x[i], y[i]));
			ids.get((sortedUniqueFrames.indexOf(frames[i]))).add(correspondences[i]);
		}
		return new ValuePair<ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>>, List<List<Integer>>>(peaks, ids);
	}

	public static ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>> getPeaksFromArrays(
			List<Integer> sortedUniqueFrames, int[] frames, double[] x, double[] y, double[] z) {
		if (z == null)
			return getPeaksFromArrays(sortedUniqueFrames, frames, x, y);

		ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>> peaks = new ArrayList<>(sortedUniqueFrames.size());

		for (int i = 0; i < sortedUniqueFrames.size(); i++) {
			peaks.add(new ArrayList<>());
		}

		for (int i = 0; i < frames.length; i++) {
			peaks.get(sortedUniqueFrames.indexOf((int) frames[i])).add(createPeak(x[i], y[i], z[i]));
		}
		return peaks;
	}

	public static ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>> getPeaksFromArrays(
			List<Integer> sortedUniqueFrames, int[] frames, double[] x, double[] y) {

		ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>> peaks = new ArrayList<>(sortedUniqueFrames.size());

		for (int i = 0; i < sortedUniqueFrames.size(); i++) {
			peaks.add(new ArrayList<>());
		}

		for (int i = 0; i < frames.length; i++) {
			peaks.get(sortedUniqueFrames.indexOf((int) frames[i])).add(createPeak(x[i], y[i]));
		}
		return peaks;
	}

	public static DifferenceOfGaussianPeak<FloatType> createPeak(double x, double y, double z) {
		int[] loc = new int[] { (int) x, (int) y, (int) z };
		DifferenceOfGaussianPeak<FloatType> p = new DifferenceOfGaussianPeak<>(loc, new FloatType(), SpecialPoint.MAX);
		p.setSubPixelLocationOffset((float) (x - loc[0]), 0);
		p.setSubPixelLocationOffset((float) (y - loc[1]), 1);
		p.setSubPixelLocationOffset((float) (z - loc[2]), 2);
		return p;
	}

	public static DifferenceOfGaussianPeak<FloatType> createPeak(double x, double y) {
		int[] loc = new int[] { (int) x, (int) y };
		DifferenceOfGaussianPeak<FloatType> p = new DifferenceOfGaussianPeak<>(loc, new FloatType(), SpecialPoint.MAX);
		p.setSubPixelLocationOffset((float) (x - loc[0]), 0);
		p.setSubPixelLocationOffset((float) (y - loc[1]), 1);
		return p;
	}

	public static AbstractModel<?> suitableModel(String d, String transform) {
		switch (transform) {
		case TRANSLATION:
			return d.equals(DIM2D) ? new TranslationModel2D() : new TranslationModel3D();
		case RIGID:
			return d.equals(DIM2D) ? new RigidModel2D() : new RigidModel3D();
		case SIMILARITY:
			return d.equals(DIM2D) ? new SimilarityModel2D() : new SimilarityModel3D();
		case AFFINE:
		default:
			return d.equals(DIM2D) ? new AffineModel2D() : new AffineModel3D();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static AbstractModel<?> suitableRegularizedModel(String d, String transform, String regularizer, double lambda) {
		switch (d) {
		case DIM2D:
			return new InterpolatedAffineModel2D(suitableModel(d, transform), suitableModel(d, regularizer), lambda);
		case DIM3D:
		default:
			return new InterpolatedAffineModel3D(suitableModel(d, transform), suitableModel(d, regularizer), lambda);
		}
	}

	public static double[] flattenModels(List<InvertibleBoundable> models, String dim) {
		List<Double> list = new ArrayList<>();
		double[] m;

		switch (dim) {
		case DIM2D:
			m = new double[6];
			for (int i = 0; i < models.size(); i++) {
				((Affine2D<?>) models.get(i)).toArray(m);
				list.add(m[0]);
				list.add(m[2]);
				list.add(0d);
				list.add(m[4]);
				list.add(m[1]);
				list.add(m[3]);
				list.add(0d);
				list.add(m[5]);
				list.add(0d);
				list.add(0d);
				list.add(1d);
				list.add(0d);
			}
			break;
		case DIM3D:
		default:
			m = new double[12];
			for (int i = 0; i < models.size(); i++) {
				((Affine3D<?>) models.get(i)).toArray(m);
				list.add(m[0]);
				list.add(m[3]);
				list.add(m[6]);
				list.add(m[9]);
				list.add(m[1]);
				list.add(m[4]);
				list.add(m[7]);
				list.add(m[10]);
				list.add(m[2]);
				list.add(m[5]);
				list.add(m[8]);
				list.add(m[11]);
			}
			break;
		}

		return Doubles.toArray(list);
	}

	public static List<InvertibleBoundable> interpolateModels(Integer[] frames, List<InvertibleBoundable> models, int radius, String dim) {
		if (frames.length != models.size()) {
			throw new IllegalArgumentException("'frames' and 'models' have different size: " + frames.length + " vs. " + models.size());
		}
		List<Integer> frameList = Arrays.asList(frames);
		List<InvertibleBoundable> interpolated = new ArrayList<>(models.size());

		for (int i = 0; i < models.size(); i++) {
			int current = frames[i];
			InvertibleBoundable previous = null;
			InvertibleBoundable cur = null;
			InvertibleBoundable next = null;
			int count = 0;
			int previousCount = 0;
			for (int r = radius; r > 0; r--) {
				// lookup current-r and current+r in models
				int indexA = frameList.indexOf(current - r);
				int indexB = frameList.indexOf(current + r);
				cur = interpolate(get(models, indexA), get(models, indexB), 0.5, dim);
				count += (indexA < 0 ? 0 : 1) + (indexB < 0 ? 0 : 1);
				next = interpolate(cur, previous, (double) previousCount / count, dim);
				previous = next;
				// increase counter by 0,1 or 2
				previousCount = count;
			}
			double lambda = (double) count / (count + 1);
			interpolated.add(interpolate(models.get(i), previous, lambda, dim));
		}
		return interpolated;
	}

	private static InvertibleBoundable interpolate(InvertibleBoundable modelA, InvertibleBoundable modelB,
			double lambda, String dim) {
		if (modelA == null) {
			return modelB;
		}
		if (modelB == null) {
			return modelA;
		}
		switch (dim) {
		case DIM2D:
			return interpolate2D(modelA, modelB, lambda);
		case DIM3D:
			return interpolate3D(modelA, modelB, lambda);
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static InvertibleBoundable interpolate2D(InvertibleBoundable modelA, InvertibleBoundable modelB, double lambda) {
		if (modelA instanceof AbstractAffineModel2D) {
			if (modelB instanceof AbstractAffineModel2D) {
				return new InterpolatedAffineModel2D((AbstractAffineModel2D) modelA, (AbstractAffineModel2D) modelB, lambda);
			}
			return new InterpolatedAffineModel2D((AbstractAffineModel2D) modelA, (InterpolatedAffineModel2D) modelB, lambda);
		}
		if (modelB instanceof AbstractAffineModel2D) {
			return new InterpolatedAffineModel2D((InterpolatedAffineModel2D) modelA, (AbstractAffineModel2D) modelB, lambda);
		}
		return new InterpolatedAffineModel2D((InterpolatedAffineModel2D) modelA, (InterpolatedAffineModel2D) modelB, lambda);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static InvertibleBoundable interpolate3D(InvertibleBoundable modelA, InvertibleBoundable modelB, double lambda) {
		if (modelA instanceof AbstractAffineModel3D) {
			if (modelB instanceof AbstractAffineModel3D) {
				return new InterpolatedAffineModel2D((AbstractAffineModel3D) modelA, (AbstractAffineModel3D) modelB, lambda);
			}
			return new InterpolatedAffineModel3D((AbstractAffineModel3D) modelA, (InterpolatedAffineModel3D) modelB, lambda);
		}
		if (modelB instanceof AbstractAffineModel3D) {
			return new InterpolatedAffineModel3D((InterpolatedAffineModel3D) modelA, (AbstractAffineModel3D) modelB, lambda);
		}
		return new InterpolatedAffineModel3D((InterpolatedAffineModel3D) modelA, (InterpolatedAffineModel3D) modelB, lambda);
	}

	private static InvertibleBoundable get(List<InvertibleBoundable> models, int index) {
		return (index < 0 ? null : models.get(index));
	}
}
