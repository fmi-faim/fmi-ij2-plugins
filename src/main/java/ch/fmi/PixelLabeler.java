package ch.fmi;

import net.imagej.ops.OpService;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.IntegerType;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, headless = true, menuPath = "FMI>Label Pixels")
public class PixelLabeler<T extends IntegerType<T>> implements Command {
	@Parameter(label="Input image")
	private Img<T> input;

	@Parameter(label="Background value")
	private int bg = 0;
	
	@Parameter
	private OpService ops;
	
	@Parameter(type=ItemIO.OUTPUT)
	private ImgLabeling<Integer, ?> output;
	
	@Override
	public void run() {
		Cursor<T> cursor = input.cursor();
		
		output = ops.create().imgLabeling(input);
		Cursor<LabelingType<Integer>> labType = output.cursor();
		
		int val = 0;
		
		while (cursor.hasNext()) {
			if (cursor.next().getInteger() != bg) {
				labType.next().add(++val);	
			} else {
				labType.fwd();
			}
		}
	}
}
