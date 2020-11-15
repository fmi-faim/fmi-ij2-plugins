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

public class AffineModelInverterTest {

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
	public void testInvertAffineMatrix() throws InterruptedException, ExecutionException
	{

		double[] input = { //
				1, 0, 0, 1, //
				0, 1, 0, 1, //
				0, 0, 1, 1 //
		};
		
		Map<String, Object> inputMap = new HashMap<>();
		inputMap.put("mIn", input);

		// Fit Model
		CommandService commandService = context.getService(CommandService.class);
		CommandModule module = commandService.run(AffineModelInverter.class, false, inputMap)
			.get();

		// Compare
		double[] expected = { //
				1, 0, 0, -1, //
				0, 1, 0, -1, //
				0, 0, 1, -1 //
		};
		double[] result = (double[]) module.getOutput("mOut");

		System.out.println(Arrays.toString(result));
		assertArrayEquals("Affine matrix", expected, result, 0.01);
	}
}
