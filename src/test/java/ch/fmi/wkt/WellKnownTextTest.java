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
package ch.fmi.wkt;

import static org.junit.Assert.assertEquals;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;

import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;

public class WellKnownTextTest {

	private Context context;
	private CommandService commandService;

	@Before
	public void setUp() throws Exception {
		context = new Context();
		commandService = context.service(CommandService.class);
	}

	@After
	public void tearDown() throws Exception {
		if (context != null) {
			context.dispose();
			context = null;
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testWKTRoundTrip() throws InterruptedException, ExecutionException {
		String polygon = "POLYGON ((1 2, 2 2, 3 2, 3 3, 2 3, 1 2, 1 2))";

		Map<String, Object> inputMap1 = new HashMap<>();
		inputMap1.put("text", polygon);

		CommandModule module1 = commandService.run(WellKnownTextToBitmask.class, true, inputMap1).get();

		Img<BitType> img = (Img<BitType>) module1.getOutput("output");

		Map<String, Object> inputMap2 = new HashMap<>();
		inputMap2.put("input", img);
		
		CommandModule module2 = commandService.run(BitmaskToWellKnownText.class, true, inputMap2).get();

		assertEquals(polygon, module2.getOutput("output"));
	}

}
