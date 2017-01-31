package ch.fmi;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.detection.DetectorKeys;
import fiji.plugin.trackmate.detection.LogDetectorFactory;
import fiji.plugin.trackmate.tracking.LAPUtils;
import fiji.plugin.trackmate.tracking.TrackerKeys;
import fiji.plugin.trackmate.tracking.sparselap.SparseLAPTrackerFactory;
import ij.ImagePlus;

import java.util.ArrayList;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, headless = true, menuPath = "FMI>Spot Detection (Subpixel localization)")
public class TrackMateSpotDetector implements Command {
	@Parameter
	private LogService log;

	@Parameter(label = "Input image")
	private ImagePlus imp;

	@Parameter(label = "Spot radius")
	private double spotSize = DetectorKeys.DEFAULT_RADIUS;

	@Parameter(label = "Spot quality threshold")
	private double spotThreshold = DetectorKeys.DEFAULT_THRESHOLD;

	@Parameter(label = "Remove calibration?")
	private boolean removeCalibration;

	@Parameter(type = ItemIO.OUTPUT)
	private int nSpotsFound;

	@Parameter(type = ItemIO.OUTPUT)
	private int[] spotID;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] x;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] y;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] z;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] quality;
	
	// TODO optionally output intensity and radius (x,y,z?) for each spot

	@Override
	public void run() {
		// Create TrackMate instance with settings
		Model model = new Model();

		// optionally remove calibration from imp
		if (removeCalibration) {
			imp.setCalibration(null);
		}

		Settings settings = new Settings();

		settings.setFrom(imp);

		// TODO make detector choice optional
		settings.detectorFactory = new LogDetectorFactory<>();

		settings.detectorSettings = settings.detectorFactory
				.getDefaultSettings();
		settings.detectorSettings.put(
				DetectorKeys.KEY_DO_SUBPIXEL_LOCALIZATION, true);
		settings.detectorSettings.put(DetectorKeys.KEY_RADIUS, spotSize);
		settings.detectorSettings
				.put(DetectorKeys.KEY_THRESHOLD, spotThreshold);

		settings.trackerFactory = new SparseLAPTrackerFactory();
		settings.trackerSettings = LAPUtils.getDefaultLAPSettingsMap();
		settings.trackerSettings.put(TrackerKeys.KEY_LINKING_MAX_DISTANCE,
				TrackerKeys.DEFAULT_LINKING_MAX_DISTANCE);
		settings.trackerSettings.put(TrackerKeys.KEY_GAP_CLOSING_MAX_DISTANCE,
				TrackerKeys.DEFAULT_GAP_CLOSING_MAX_DISTANCE);
		settings.trackerSettings.put(TrackerKeys.KEY_GAP_CLOSING_MAX_FRAME_GAP,
				TrackerKeys.DEFAULT_GAP_CLOSING_MAX_FRAME_GAP);

		// settings.detectorFactory.

		TrackMate trackmate = new TrackMate(model, settings);

		// Process (spot detection and tracking)
		if (!trackmate.checkInput()) {
			log.error("Configuration error: " + trackmate.getErrorMessage());
			return;
		}
		if (!trackmate.process()) {
			log.error("Processing error: " + trackmate.getErrorMessage());
			return;
		}

		// Prepare lists to collect results
		ArrayList<Integer> spotIDlist = new ArrayList<>();
		ArrayList<Double> xList = new ArrayList<>();
		ArrayList<Double> yList = new ArrayList<>();
		ArrayList<Double> zList = new ArrayList<>();
		ArrayList<Double> qList = new ArrayList<>();

		// Get spot collection (all spots)
		SpotCollection spotCollection = model.getSpots();
		for (Spot spot : spotCollection.iterable(false)) {
			spotIDlist.add(spot.ID());
			xList.add(spot.getDoublePosition(0));
			yList.add(spot.getDoublePosition(1));
			zList.add(spot.getDoublePosition(2));
			qList.add(spot.getFeature(Spot.QUALITY));
		}

		// Get results (spot list with trackIDs)
		spotID = Ints.toArray(spotIDlist);
		x = Doubles.toArray(xList);
		y = Doubles.toArray(yList);
		z = Doubles.toArray(zList);
		quality = Doubles.toArray(qList);

		// Return summary values
		nSpotsFound = model.getSpots().getNSpots(false);
	}

}
