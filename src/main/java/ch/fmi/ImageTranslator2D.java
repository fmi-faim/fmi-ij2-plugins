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

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, headless = true, menuPath = "FMI>Translate 2D Image")
public class ImageTranslator2D<T extends NumericType<T>> implements Command {
	@Parameter(label = "Image to be transformed")
	private Img<T> img;

	@Parameter(label = "X Shift")
	private int x;

	@Parameter(label = "Y Shift")
	private int y;

	@Parameter(type = ItemIO.OUTPUT)
	private Img<T> translated;

	@Override
	public void run() {
		long[] shift = new long[img.numDimensions()];
		shift[0] = x;
		shift[1] = y;
		IntervalView<T> extendedInterval = Views.interval(
				Views.extendBorder(img), img);
		IntervalView<T> translatedView = Views.translate(extendedInterval,
				shift);
		translated = img.factory().create(img, img.firstElement());
		Cursor<T> cursorIn = Views.interval(translatedView, img).cursor();
		Cursor<T> cursorOut = translated.cursor();

		while (cursorIn.hasNext()) {
			cursorIn.fwd();
			cursorOut.fwd();
			cursorOut.get().set(cursorIn.get());
		}
	}
}
