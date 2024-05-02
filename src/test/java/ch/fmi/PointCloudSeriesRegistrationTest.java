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
import static org.junit.Assert.assertThrows;

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

public class PointCloudSeriesRegistrationTest {

	private Context context;
	private CommandService commandService;

	@Before
	public void initialize() {
		context = new Context();
		commandService = context.service(CommandService.class);
	}

	@After
	public void disposeContext() {
		if (context != null) {
			context.dispose();
			context = null;
		}
	}

	@Test
	public void testSeriesRegistration() throws InterruptedException, ExecutionException
	{
		// Create Point Arrays
		double[] x = { //
			0, 0, 1, 1, 1, 2, //
			1, 2.1, 2, 0.9, 2, 3, //
			2, 3, 2, 3, 3, 4 //
		};
		double[] y = { //
			0, 1, 1, 0, 1, 2, //
			1, 2, 0.9, 2, 2.1, 3, //
			1, 2, 2, 1, 2, 3 //
		};
		double[] z = { //
			0, 1, 0, 1, 1, 2, //
			1, 1, 2, 2, 2, 3, //
			0, 0, 0, 1, 1, 2 //
		};
		double[] frame = { //
			0, 0, 0, 0, 0, 0,//
			1, 1, 1, 1, 1, 1, //
			3, 3, 3, 3, 3, 3 //
		};

		Map<String, Object> inputMap = new HashMap<>();
		inputMap.put("transformType", PointCloudSeriesRegistration.TRANSLATION);
		inputMap.put("dim", PointCloudSeriesRegistration.DIM3D);
		inputMap.put("xCoords", x);
		inputMap.put("yCoords", y);
		inputMap.put("zCoords", z);
		inputMap.put("frame", frame);
		inputMap.put("range", 3);

		// Fit Model
		CommandModule module = commandService.run(PointCloudSeriesRegistration.class, true, inputMap).get();

		// Compare
		double[] expectedModels = { //
			1, 0, 0,  0,   0, 1, 0,  0,   0, 0, 1,  0, //
			1, 0, 0, -1,   0, 1, 0, -1,   0, 0, 1, -1, //
			1, 0, 0, -2,   0, 1, 0, -1,   0, 0, 1,  0  //
		};
		double[] flatModels = (double[]) module.getOutput("flatModels");
		System.out.println(Arrays.toString(flatModels));
		assertArrayEquals("Models", expectedModels, flatModels, 0.01);

		int[] frames = { 0, 1, 3 };
		int[] frameList = (int[]) module.getOutput("frameList");
		System.out.println(Arrays.toString(flatModels));
		assertArrayEquals("Frame list", frames, frameList);

		double[] expectedCosts = { 0.0036778835300844024, 0.00622333538791261, 0.0028498240371903037 };
		double[] costs = (double[]) module.getOutput("modelCosts");
		System.out.println(Arrays.toString(costs));
		assertArrayEquals("Costs", expectedCosts , costs , 0.000001);
	}

	@Test
	public void testWrongInputs() {
		double[] x = { 0, 0 };
		double[] y = { 0, 0, 0 };
		double[] z = { 0, 0, 0 };
		double[] frame = { 0, 0 };

		Map<String, Object> inputMap = new HashMap<>();
		inputMap.put("transformType", PointCloudSeriesRegistration.TRANSLATION);
		inputMap.put("dim", PointCloudSeriesRegistration.DIM3D);
		inputMap.put("xCoords", x);
		inputMap.put("yCoords", y);
		inputMap.put("zCoords", z);
		inputMap.put("frame", frame);
		inputMap.put("range", 3);

		// TODO use @ExpectedException with expectCause(instanceOf(IllegalArgumentException.class))
		assertThrows(ExecutionException.class, () -> commandService.run(
			PointCloudSeriesRegistration.class, true, inputMap).get());
	}

	
}
