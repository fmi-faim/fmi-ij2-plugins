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

public class RegUtilsTest {

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
		DifferenceOfGaussianPeak<FloatType> peak = RegUtils.createPeak(loc[0], loc[1], loc[2]);
		assertEquals(loc[0], peak.getSubPixelPosition(0), 0.00001);
		assertEquals(loc[1], peak.getSubPixelPosition(1), 0.00001);
		assertEquals(loc[2], peak.getSubPixelPosition(2), 0.00001);
	}

	@Test
	public void testGetSortedUniqueFrames() {
		Integer[] expected = { 0, 1, 3 };
		Integer[] unique = RegUtils.getSortedUniqueFrames(frames);
		assertArrayEquals(expected, unique);
	}

	@Test
	public void testGetPeaksFromArrays3D() {
		List<Integer> sortedUniqueFrames = Arrays.asList(RegUtils.getSortedUniqueFrames(frames));
		Map<Integer, Long> countMap = Arrays.stream(frames).boxed().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

		ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>> peakList = RegUtils.getPeaksFromArrays(sortedUniqueFrames, frames, x, y, z);

		assertEquals(sortedUniqueFrames.size(), peakList.size());
		assertEquals((long) countMap.get(sortedUniqueFrames.get(0)), peakList.get(0).size());
		assertEquals((long) countMap.get(sortedUniqueFrames.get(1)), peakList.get(1).size());
		assertEquals((long) countMap.get(sortedUniqueFrames.get(2)), peakList.get(2).size());
	}

	@Test
	public void testGetPeaksFromArrays2D() {
		List<Integer> sortedUniqueFrames = Arrays.asList(RegUtils.getSortedUniqueFrames(frames));
		Map<Integer, Long> countMap = Arrays.stream(frames).boxed().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

		ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>> peakList = RegUtils.getPeaksFromArrays(sortedUniqueFrames, frames, x, y, null);

		assertEquals(sortedUniqueFrames.size(), peakList.size());
		assertEquals((long) countMap.get(sortedUniqueFrames.get(0)), peakList.get(0).size());
		assertEquals((long) countMap.get(sortedUniqueFrames.get(1)), peakList.get(1).size());
		assertEquals((long) countMap.get(sortedUniqueFrames.get(2)), peakList.get(2).size());
	}

	@Test
	public void testGetPeaksAndCorrespondencesFromArrays3D() {
		List<Integer> sortedUniqueFrames = Arrays.asList(RegUtils.getSortedUniqueFrames(frames));
		Map<Integer, Long> countMap = Arrays.stream(frames).boxed().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

		Pair<ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>>, List<List<Integer>>> values = RegUtils
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
		List<Integer> sortedUniqueFrames = Arrays.asList(RegUtils.getSortedUniqueFrames(frames));
		Map<Integer, Long> countMap = Arrays.stream(frames).boxed().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

		Pair<ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>>, List<List<Integer>>> values = RegUtils
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
		Integer[] frameLookup = RegUtils.getSortedUniqueFrames(frames);
		ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>> peaks = RegUtils
				.getPeaksFromArrays(Arrays.asList(frameLookup), frames, x, y, z);
		int range = 3;

		Vector<ComparePair> pairs = RegUtils.getComparePairs(frameLookup, peaks, range, new AffineModel3D());

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
		Integer[] frameLookup = RegUtils.getSortedUniqueFrames(frames);
		Pair<ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>>, List<List<Integer>>> values = RegUtils
				.getPeaksAndCorrespondencesFromArrays(Arrays.asList(frameLookup), frames, x, y, z, trackIDs);
		ArrayList<ArrayList<DifferenceOfGaussianPeak<FloatType>>> peakList = values.getA();
		List<List<Integer>> correspondences = values.getB();

		int range = 1;

		Vector<ComparePair> pairs = RegUtils.getComparePairs(frameLookup, peakList, range, new TranslationModel3D());
		RegUtils.populateComparePairs(pairs, peakList, correspondences);

		assertEquals(1, pairs.size());
		assertEquals(5, pairs.get(0).inliers.size());
	}

	@Test
	public void testSuitableModel() {
		assertTrue(RegUtils.suitableModel(RegUtils.DIM3D, RegUtils.AFFINE) instanceof AffineModel3D);
		assertTrue(RegUtils.suitableModel(RegUtils.DIM3D, RegUtils.RIGID) instanceof RigidModel3D);
		assertTrue(RegUtils.suitableModel(RegUtils.DIM2D, RegUtils.AFFINE) instanceof AffineModel2D);
		assertTrue(RegUtils.suitableModel(RegUtils.DIM2D, RegUtils.RIGID) instanceof RigidModel2D);
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testSuitableRegularizedModel() {
		AbstractModel<?> model1 = RegUtils.suitableRegularizedModel(RegUtils.DIM2D, RegUtils.AFFINE, RegUtils.TRANSLATION, 0.1);
		assertTrue(model1 instanceof InterpolatedAffineModel2D);
		assertTrue(((InterpolatedAffineModel2D) model1).getA() instanceof AffineModel2D);
		assertTrue(((InterpolatedAffineModel2D) model1).getB() instanceof TranslationModel2D);
		assertEquals(0.1, ((InterpolatedAffineModel2D) model1).getLambda(), 0.0);

		AbstractModel<?> model2 = RegUtils.suitableRegularizedModel(RegUtils.DIM3D, RegUtils.AFFINE, RegUtils.RIGID, 0.3);
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

		double[] flattened = RegUtils.flattenModels(models, RegUtils.DIM3D);
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

		double[] flattened = RegUtils.flattenModels(models, RegUtils.DIM2D);
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
		double[] flattened = RegUtils.flattenModels(Arrays.asList(model), RegUtils.DIM2D);
		double[] expected = { //
				1.0, 0.0, 0.0, 0.5, //
				0.0, 1.0, 0.0, 7.6, //
				0.0, 0.0, 1.0, 0.0 //
		};
		assertArrayEquals(expected, flattened, 0.0);
	}

	@Test
	public void testInterpolateModels2D() {
		Integer[] uniqueFrames = new Integer[] { 0, 1, 2, 3, 4, 5, 6 };
		double[] origin = new double[] { 0, 0 };

		TranslationModel2D a = new TranslationModel2D();
		a.set(0.0, -0.1);
		TranslationModel2D b = new TranslationModel2D();
		b.set(1.0, 0.3);
		TranslationModel2D c = new TranslationModel2D();
		c.set(2.0, -0.2);
		TranslationModel2D d = new TranslationModel2D();
		d.set(3.0, -0.5);
		TranslationModel2D e = new TranslationModel2D();
		e.set(4.0, 0.4);
		TranslationModel2D f = new TranslationModel2D();
		f.set(5.0, 0.3);
		TranslationModel2D g = new TranslationModel2D();
		g.set(6.0, -0.2);
		List<InvertibleBoundable> models = Arrays.asList(a, b, c, d, e, f, g);
		List<InvertibleBoundable> interpolated_0 = RegUtils.interpolateModels(uniqueFrames, models, 0, RegUtils.DIM2D);

		for (int i = 0; i < models.size(); i++) {
			assertEquals("Zero-radius interpolated models should be equal at position " + i, models.get(i),
					interpolated_0.get(i));
		}

		List<InvertibleBoundable> interpolated_1 = RegUtils.interpolateModels(uniqueFrames, models, 1, RegUtils.DIM2D);
		double[] d1 = interpolated_1.get(3).apply(origin);

		double[] expected_d1 = new double[] { 3.0, -0.1 };
		assertArrayEquals(expected_d1, d1, 0.0001);

		List<InvertibleBoundable> interpolated_2 = RegUtils.interpolateModels(uniqueFrames, models, 2, RegUtils.DIM2D);
		double[] d2 = interpolated_2.get(3).apply(origin);

		double[] expected_d2 = new double[] { 3.0, 0.06 };
		assertArrayEquals(expected_d2, d2, 0.0001);

		List<InvertibleBoundable> interpolated_3 = RegUtils.interpolateModels(uniqueFrames, models, 3, RegUtils.DIM2D);
		double[] d3 = interpolated_3.get(3).apply(origin);

		double[] expected_d3 = new double[] { 3.0, 0.0 };
		assertArrayEquals(expected_d3, d3, 0.0001);
	}
}
