
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
