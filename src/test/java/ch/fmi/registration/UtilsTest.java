package ch.fmi.registration;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;

import mpicbg.imglib.algorithm.scalespace.DifferenceOfGaussianPeak;
import mpicbg.imglib.type.numeric.real.FloatType;
import mpicbg.models.AbstractModel;
import mpicbg.models.AffineModel2D;
import mpicbg.models.AffineModel3D;
import mpicbg.models.InterpolatedAffineModel2D;
import mpicbg.models.InterpolatedAffineModel3D;
import mpicbg.models.InvertibleBoundable;
import mpicbg.models.RigidModel2D;
import mpicbg.models.RigidModel3D;
import mpicbg.models.TranslationModel2D;
import mpicbg.models.TranslationModel3D;
import net.imglib2.util.Pair;
import process.ComparePair;

public class UtilsTest {

	// Create Frame Array
	private static int[] frames = { //
			0, 0, 0, 0, 0, 0,//
			1, 1, 1, 1, 1, 1, //
			3, 3, 3, 3, 3, 3 //
		};

	// Create Frame Array
	private static int[] trackIDs = { //
			2, 42, 13, 157, 999, 1,//
			2, 42, 13, 157, 999, 5, //
			2, 42, 13, 157, 999, 7 //
		};

	// Create Coordinate Arrays
	private static double[] x = { //
		0, 0, 1, 1, 1, 2, //
		1, 2.1, 2, 0.9, 2, 3, //
		2, 3, 2, 3, 3, 4 //
	};
	private static double[] y = { //
		0, 1, 1, 0, 1, 2, //
		1, 2, 0.9, 2, 2.1, 3, //
		1, 2, 2, 1, 2, 3 //
	};
	private static double[] z = { //
		0, 1, 0, 1, 1, 2, //
		1, 1, 2, 2, 2, 3, //
		0, 0, 0, 1, 1, 2 //
	};

	@Test
	public void testCreatePeak() {
		double[] loc = { 1.5, 2.4, 3.6 };
		DifferenceOfGaussianPeak<FloatType> peak = Utils.createPeak(loc[0], loc[1], loc[2]);
		assertEquals(loc[0], peak.getSubPixelPosition(0), 0.00001);
		assertEquals(loc[1], peak.getSubPixelPosition(1), 0.00001);
		assertEquals(loc[2], peak.getSubPixelPosition(2), 0.00001);
	}

	@Test
	public void testGetSortedUniqueFrames() {
		Integer[] expected = { 0, 1, 3 };
		Integer[] unique = Utils.getSortedUniqueFrames(frames);
		assertArrayEquals(expected, unique);
	}

	@Test
	public void testGetPeaksFromArrays3D() {
		List<Integer> sortedUniqueFrames = Arrays.asList(Utils.getSortedUniqueFrames(frames));
		Map<Integer, Long> countMap = Arrays.stream(frames).boxed().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

		ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>> peakList = Utils.getPeaksFromArrays(sortedUniqueFrames, frames, x, y, z);

		assertEquals(sortedUniqueFrames.size(), peakList.size());
		assertEquals((long) countMap.get(sortedUniqueFrames.get(0)), peakList.get(0).size());
		assertEquals((long) countMap.get(sortedUniqueFrames.get(1)), peakList.get(1).size());
		assertEquals((long) countMap.get(sortedUniqueFrames.get(2)), peakList.get(2).size());
	}

	@Test
	public void testGetPeaksFromArrays2D() {
		List<Integer> sortedUniqueFrames = Arrays.asList(Utils.getSortedUniqueFrames(frames));
		Map<Integer, Long> countMap = Arrays.stream(frames).boxed().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

		ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>> peakList = Utils.getPeaksFromArrays(sortedUniqueFrames, frames, x, y, null);

		assertEquals(sortedUniqueFrames.size(), peakList.size());
		assertEquals((long) countMap.get(sortedUniqueFrames.get(0)), peakList.get(0).size());
		assertEquals((long) countMap.get(sortedUniqueFrames.get(1)), peakList.get(1).size());
		assertEquals((long) countMap.get(sortedUniqueFrames.get(2)), peakList.get(2).size());
	}

	@Test
	public void testGetPeaksAndCorrespondencesFromArrays3D() {
		List<Integer> sortedUniqueFrames = Arrays.asList(Utils.getSortedUniqueFrames(frames));
		Map<Integer, Long> countMap = Arrays.stream(frames).boxed().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

		Pair<ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>>, List<List<Integer>>> values = Utils
				.getPeaksAndCorrespondencesFromArrays(sortedUniqueFrames, frames, x, y, z, trackIDs);

		ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>> peakList = values.getA();
		List<List<Integer>> correspondences = values.getB();

		assertEquals(sortedUniqueFrames.size(), peakList.size());
		assertEquals(sortedUniqueFrames.size(), correspondences.size());

		assertEquals((long) countMap.get(sortedUniqueFrames.get(0)), correspondences.get(0).size());
		assertEquals((long) countMap.get(sortedUniqueFrames.get(1)), correspondences.get(1).size());
		assertEquals((long) countMap.get(sortedUniqueFrames.get(2)), correspondences.get(2).size());

		Integer[] expected = { 2, 42, 13, 157, 999, 1 };
		assertArrayEquals(expected, correspondences.get(0).toArray());
	}

	@Test
	public void testGetPeaksAndCorrespondencesFromArrays2D() {
		List<Integer> sortedUniqueFrames = Arrays.asList(Utils.getSortedUniqueFrames(frames));
		Map<Integer, Long> countMap = Arrays.stream(frames).boxed().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

		Pair<ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>>, List<List<Integer>>> values = Utils
				.getPeaksAndCorrespondencesFromArrays(sortedUniqueFrames, frames, x, y, null, trackIDs);

		ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>> peakList = values.getA();
		List<List<Integer>> correspondences = values.getB();

		assertEquals(sortedUniqueFrames.size(), peakList.size());
		assertEquals(sortedUniqueFrames.size(), correspondences.size());

		assertEquals((long) countMap.get(sortedUniqueFrames.get(0)), correspondences.get(0).size());
		assertEquals((long) countMap.get(sortedUniqueFrames.get(1)), correspondences.get(1).size());
		assertEquals((long) countMap.get(sortedUniqueFrames.get(2)), correspondences.get(2).size());

		Integer[] expected = { 2, 42, 13, 157, 999, 1 };
		assertArrayEquals(expected, correspondences.get(0).toArray());
	}

	@Test
	public void testGetComparePairs() {
		Integer[] frameLookup = Utils.getSortedUniqueFrames(frames);
		ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>> peaks = Utils
				.getPeaksFromArrays(Arrays.asList(frameLookup), frames, x, y, z);
		int range = 3;

		Vector<ComparePair> pairs = Utils.getComparePairs(frameLookup, peaks, range, new AffineModel3D());

		assertEquals(3, pairs.size());

		assertEquals(0, pairs.get(0).indexA);
		assertEquals(1, pairs.get(0).indexB);

		assertEquals(0, pairs.get(1).indexA);
		assertEquals(2, pairs.get(1).indexB);

		assertEquals(1, pairs.get(2).indexA);
		assertEquals(2, pairs.get(2).indexB);
	}

	@Test
	public void testPopulateComparePairs() {
		Integer[] frameLookup = Utils.getSortedUniqueFrames(frames);
		Pair<ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>>, List<List<Integer>>> values = Utils
				.getPeaksAndCorrespondencesFromArrays(Arrays.asList(frameLookup), frames, x, y, z, trackIDs);
		ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>> peakList = values.getA();
		List<List<Integer>> correspondences = values.getB();

		int range = 1;

		Vector<ComparePair> pairs = Utils.getComparePairs(frameLookup, peakList, range, new TranslationModel3D());
		Utils.populateComparePairs(pairs, peakList, correspondences);

		assertEquals(1, pairs.size());
		assertEquals(5, pairs.get(0).inliers.size());
	}

	@Test
	public void testSuitableModel() {
		assertTrue(Utils.suitableModel(Utils.DIM3D, Utils.AFFINE) instanceof AffineModel3D);
		assertTrue(Utils.suitableModel(Utils.DIM3D, Utils.RIGID) instanceof RigidModel3D);
		assertTrue(Utils.suitableModel(Utils.DIM2D, Utils.AFFINE) instanceof AffineModel2D);
		assertTrue(Utils.suitableModel(Utils.DIM2D, Utils.RIGID) instanceof RigidModel2D);
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testSuitableRegularizedModel() {
		AbstractModel<?> model1 = Utils.suitableRegularizedModel(Utils.DIM2D, Utils.AFFINE, Utils.TRANSLATION, 0.1);
		assertTrue(model1 instanceof InterpolatedAffineModel2D);
		assertTrue(((InterpolatedAffineModel2D) model1).getA() instanceof AffineModel2D);
		assertTrue(((InterpolatedAffineModel2D) model1).getB() instanceof TranslationModel2D);
		assertEquals(0.1, ((InterpolatedAffineModel2D) model1).getLambda(), 0.0);

		AbstractModel<?> model2 = Utils.suitableRegularizedModel(Utils.DIM3D, Utils.AFFINE, Utils.RIGID, 0.3);
		assertTrue(model2 instanceof InterpolatedAffineModel3D);
		assertTrue(((InterpolatedAffineModel3D) model2).getA() instanceof AffineModel3D);
		assertTrue(((InterpolatedAffineModel3D) model2).getB() instanceof RigidModel3D);
		assertEquals(0.3, ((InterpolatedAffineModel3D) model2).getLambda(), 0.0);
	}

	@Test
	public void testFlattenModels3D() {
		List<InvertibleBoundable> models = new ArrayList<>();
		AffineModel3D model1 = new AffineModel3D();
		model1.set(//
				1.0, 0.0, 0.0, 0.0,//
				0.0, 1.0, 0.0, 0.0,//
				0.0, 0.0, 1.0, 0.0);
		AffineModel3D model2 = new AffineModel3D();
		model2.set(//
				1.0, 0.0, 0.0, 2.0,//
				0.0, 1.0, 0.0, 3.5,//
				0.0, 0.0, 1.0, 4.7);
		models.add(model1);
		models.add(model2);

		double[] flattened = Utils.flattenModels(models, Utils.DIM3D);
		double[] expected = {
				1.0, 0.0, 0.0, 0.0,//
				0.0, 1.0, 0.0, 0.0,//
				0.0, 0.0, 1.0, 0.0, //
				1.0, 0.0, 0.0, 2.0,//
				0.0, 1.0, 0.0, 3.5,//
				0.0, 0.0, 1.0, 4.7
		};
		assertArrayEquals(expected, flattened, 0.0);
	}

	@Test
	public void testFlattenModels2D() {
		List<InvertibleBoundable> models = new ArrayList<>();
		AffineModel2D model1 = new AffineModel2D();
		model1.set(//
				1.0, 0.0,//
				0.0, 1.0,//
				0.0, 0.0
				);
		AffineModel2D model2 = new AffineModel2D();
		model2.set(//
				-1.0, 0.0,//
				0.0, 1.0,//
				3.7, 4.2//
				);
		models.add(model1);
		models.add(model2);

		double[] flattened = Utils.flattenModels(models, Utils.DIM2D);
		double[] expected = {
				1.0, 0.0, 0.0, 0.0,//
				0.0, 1.0, 0.0, 0.0,//
				0.0, 0.0, 1.0, 0.0, //
				-1.0, 0.0, 0.0, 3.7,//
				0.0, 1.0, 0.0, 4.2,//
				0.0, 0.0, 1.0, 0.0
		};
		assertArrayEquals(expected, flattened, 0.0);
	}

	@Test
	public void testFlattenTranslation2D() {
		TranslationModel2D model = new TranslationModel2D();
		model.set(0.5, 7.6);
		double[] flattened = Utils.flattenModels(Arrays.asList(model), Utils.DIM2D);
		double[] expected = { //
				1.0, 0.0, 0.0, 0.5, //
				0.0, 1.0, 0.0, 7.6, //
				0.0, 0.0, 1.0, 0.0 //
		};
		assertArrayEquals(expected, flattened, 0.0);
	}
}
