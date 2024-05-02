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

package ch.fmi;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;

public class ModelFitterTest {

	private Context context;

	@Before
	public void initialize() {
		context = new Context();
	}

	@After
	public void disposeContext() {
		if (context != null) {
			context.dispose();
			context = null;
		}
	}

	@Test
	public void test3DSimilarityModel() throws InterruptedException,
		ExecutionException
	{
		// Create Point Arrays
		double[] x1 = { 0, 1, 1, 1 };
		double[] y1 = { 0, 0, 1, 1 };
		double[] z1 = { 0, 0, 0, 1 };
		double[] x2 = { 2, 2, 4, 4 };
		double[] y2 = { 0, -2, -2, -2 };
		double[] z2 = { 0, 0, 0, 2 };

		Map<String, Object> inputMap = new HashMap<>();
		inputMap.put("transformType", ModelFitter.SIMILARITY);
		inputMap.put("dim", ModelFitter.DIM3D);
		inputMap.put("x1", x1);
		inputMap.put("y1", y1);
		inputMap.put("z1", z1);
		inputMap.put("x2", x2);
		inputMap.put("y2", y2);
		inputMap.put("z2", z2);

		// Fit Model
		CommandService commandService = context.getService(CommandService.class);
		CommandModule module = commandService.run(ModelFitter.class, true, inputMap)
			.get();

		// Compare
		double[] expected = { //
			0, 2, 0, 2, //
			-2, 0, 0, 0, //
			0, 0, 2, 0 //
		};
		double[] result = (double[]) module.getOutput("affine");
		System.out.println(Arrays.toString(result));
		assertArrayEquals("Affine matrix", expected, result, 0.01);
	}

	@Test
	public void test3DAffineModel() throws InterruptedException,
		ExecutionException
	{
		// Create Point Arrays
		double[] x1 = { 0, 1, 1, 1 };
		double[] y1 = { 0, 0, 1, 1 };
		double[] z1 = { 0, 0, 0, 1 };
		double[] x2 = { 2, 2, 4, 4 };
		double[] y2 = { 0, -2, -3, -3 };
		double[] z2 = { 0, 0, 0, 2 };

		Map<String, Object> inputMap = new HashMap<>();
		inputMap.put("transformType", ModelFitter.AFFINE);
		inputMap.put("dim", ModelFitter.DIM3D);
		inputMap.put("x1", x1);
		inputMap.put("y1", y1);
		inputMap.put("z1", z1);
		inputMap.put("x2", x2);
		inputMap.put("y2", y2);
		inputMap.put("z2", z2);

		// Fit Model
		CommandService commandService = context.getService(CommandService.class);
		CommandModule module = commandService.run(ModelFitter.class, true, inputMap)
			.get();

		// Compare
		double[] expected = { //
			0, 2, 0, 2, //
			-2, -1, 0, 0, //
			0, 0, 2, 0 //
		};
		double[] result = (double[]) module.getOutput("affine");
		System.out.println(Arrays.toString(result));
		assertArrayEquals("Affine matrix", expected, result, 0.01);
	}

	@Test
	public void test2DAffineModel() throws InterruptedException,
		ExecutionException
	{
		// Create Point Arrays
		double[] x1 = { 0, 1, 1, 0 };
		double[] y1 = { 0, 0, 1, 1 };
		double[] x2 = { 2, 2, 4, 4 };
		double[] y2 = { 0, -2, -3, -1 };

		Map<String, Object> inputMap = new HashMap<>();
		inputMap.put("transformType", ModelFitter.AFFINE);
		inputMap.put("dim", ModelFitter.DIM2D);
		inputMap.put("x1", x1);
		inputMap.put("y1", y1);
		inputMap.put("x2", x2);
		inputMap.put("y2", y2);

		inputMap.put("z1", x1); // dummy input
		inputMap.put("z2", x1); // dummy input

		// Fit Model
		CommandService commandService = context.getService(CommandService.class);
		CommandModule module = commandService.run(ModelFitter.class, true, inputMap)
			.get();

		// Compare
		double[] expected = { //
			0, 2, 0, 2, //
			-2, -1, 0, 0, //
			0, 0, 1, 0 //
		};

		double[] result = (double[]) module.getOutput("affine");
		System.out.println(Arrays.toString(result));

		assertArrayEquals("Affine matrix", expected, result, 0.01);
	}
}
