package ch.fmi.registration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import com.google.common.primitives.Doubles;

import mpicbg.imglib.algorithm.scalespace.DifferenceOfGaussian.SpecialPoint;
import mpicbg.imglib.algorithm.scalespace.DifferenceOfGaussianPeak;
import mpicbg.imglib.type.numeric.real.FloatType;
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

public class Utils {
	private Utils() {
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

}
