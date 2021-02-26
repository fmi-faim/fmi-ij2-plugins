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
