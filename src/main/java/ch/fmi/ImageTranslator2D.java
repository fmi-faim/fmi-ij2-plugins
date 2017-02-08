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
