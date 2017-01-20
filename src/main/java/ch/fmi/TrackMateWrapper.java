package ch.fmi;

import com.google.common.primitives.Doubles;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.TrackModel;
import fiji.plugin.trackmate.detection.DetectorKeys;
import fiji.plugin.trackmate.detection.LogDetectorFactory;
import fiji.plugin.trackmate.tracking.LAPUtils;
import fiji.plugin.trackmate.tracking.TrackerKeys;
import fiji.plugin.trackmate.tracking.sparselap.SparseLAPTrackerFactory;
import ij.ImagePlus;

import java.util.ArrayList;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, headless = true, menuPath = "FMI>Track Spots (Subpixel localization)")
public class TrackMateWrapper implements Command {
	@Parameter(label = "Input image")
	private ImagePlus imp;

	@Parameter(label = "Spot radius")
	private double spotSize = DetectorKeys.DEFAULT_RADIUS;

	@Parameter(label = "Spot quality threshold")
	private double spotThreshold = DetectorKeys.DEFAULT_THRESHOLD;

	@Parameter(label = "Linking max distance")
	private double linkingMaxDistance = TrackerKeys.DEFAULT_LINKING_MAX_DISTANCE;

	@Parameter(label = "Gap closing max distance")
	private double closingMaxDistance = TrackerKeys.DEFAULT_GAP_CLOSING_MAX_DISTANCE;

	@Parameter(label = "Gap closing max frame gap")
	private int frameGap = TrackerKeys.DEFAULT_GAP_CLOSING_MAX_FRAME_GAP;

	@Parameter(type = ItemIO.OUTPUT)
	private int nSpotsFound;

	@Parameter(type = ItemIO.OUTPUT)
	private int nTracksFound;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] spotID;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] trackID;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] x;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] y;

	@Parameter(type = ItemIO.OUTPUT)
	private double[] z;

	@Override
	public void run() {
		// Create TrackMate instance with settings
		Model model = new Model();
		Settings settings = new Settings();

		settings.setFrom(imp);

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
				linkingMaxDistance);
		settings.trackerSettings.put(TrackerKeys.KEY_GAP_CLOSING_MAX_DISTANCE,
				closingMaxDistance);
		settings.trackerSettings.put(TrackerKeys.KEY_GAP_CLOSING_MAX_FRAME_GAP,
				frameGap);

		TrackMate trackmate = new TrackMate(model, settings);

		// Process (spot detection and tracking)
		if (!trackmate.checkInput()) {
			return; // TODO log errors
		}
		if (!trackmate.process()) {
			return; // TODO log errors
		}

		// Prepare lists to collect results
		ArrayList<Double> spotIDlist = new ArrayList<>();
		ArrayList<Double> trackIDlist = new ArrayList<>();
		ArrayList<Double> xList = new ArrayList<>();
		ArrayList<Double> yList = new ArrayList<>();
		ArrayList<Double> zList = new ArrayList<>();
		
		TrackModel trackModel = model.getTrackModel();
		for (Integer tID : trackModel.trackIDs(false)) {
			for (Spot spot : trackModel.trackSpots(tID)) {
				spotIDlist.add((double) spot.ID());
				trackIDlist.add((double) tID);
				xList.add(spot.getDoublePosition(0));
				yList.add(spot.getDoublePosition(1));
				zList.add(spot.getDoublePosition(2));
			}
		}

		/*
		// Get spot collection (all spots)
		SpotCollection spotCollection = model.getSpots();
		for (Spot spot : spotCollection.iterable(false)) {
			spotIDlist.add((double) spot.ID());
			trackIDlist.add((double) spot.ID()); // TODO trackID
			xList.add(spot.getDoublePosition(0));
			yList.add(spot.getDoublePosition(1));
			zList.add(spot.getDoublePosition(2));
		}
		*/

		// Get results (spot list with trackIDs)
		spotID = Doubles.toArray(spotIDlist);
		trackID = Doubles.toArray(trackIDlist);
		x = Doubles.toArray(xList);
		y = Doubles.toArray(yList);
		z = Doubles.toArray(zList);

		// Return summary values
		nSpotsFound = model.getSpots().getNSpots(false);
		nTracksFound = model.getTrackModel().nTracks(false);
	}

}
