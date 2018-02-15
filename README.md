# fmi-ij2-plugins

[![GitHub Release](https://img.shields.io/github/release/fmi-faim/fmi-ij2-plugins.svg)](https://github.com/fmi-faim/fmi-ij2-plugins/releases)
[![Build Status](https://travis-ci.org/fmi-faim/fmi-ij2-plugins.svg?branch=master)](https://travis-ci.org/fmi-faim/fmi-ij2-plugins)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/6223c2d420574794be62f9f45a871903)](https://www.codacy.com/app/imagejan/fmi-ij2-plugins?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=fmi-faim/fmi-ij2-plugins&amp;utm_campaign=Badge_Grade)
[![DOI](https://zenodo.org/badge/72086675.svg)](https://zenodo.org/badge/latestdoi/72086675)

## Installation in KNIME

To use these plugins in KNIME, please activate the update site `https://community.knime.org/download/ch.fmi.knime.plugins.update/` as well as the [KNIME Image Processing Nightly Builds](https://www.knime.com/wiki/knime-image-processing-nightly-build).

## Manual installation in KNIME

If you want to install this plugin manually via the KNIME ImageJ2 integration, you'll have to install the `fmi-ij2-plugins` jar file as well as all the dependencies listed below.

1. Install [`fmi-ij2-plugins.jar`](https://github.com/fmi-faim/fmi-ij2-plugins/releases/download/v0.1.4/fmi-ij2-plugins-0.1.4.jar) via *File > Preferences* into *KNIME > Image Processing Plugin > ImageJ2 Plugin Installation*.

2. The following additional files (dependencies) are required:

| Project | Download `jar` from maven.imagej.net |
| --- | --- |
| [`AnalyzeSkeleton_`](https://github.com/fiji/AnalyzeSkeleton/) | [`AnalyzeSkeleton_-3.1.1.jar`](http://maven.imagej.net/service/local/repositories/releases/content/sc/fiji/AnalyzeSkeleton_/3.1.1/AnalyzeSkeleton_-3.1.1.jar) |
| [`Descriptor_based_registration`](https://github.com/fiji/Descriptor_based_registration) | [`Descriptor_based_registration-2.1.1.jar`](http://maven.imagej.net/service/local/repositories/releases/content/sc/fiji/Descriptor_based_registration/2.1.1/Descriptor_based_registration-2.1.1.jar) |
| [`Fiji_Plugins`](https://github.com/fiji/Fiji_Plugins) | [`Fiji_Plugins-3.1.0.jar`](http://maven.imagej.net/service/local/repositories/releases/content/sc/fiji/Fiji_Plugins/3.1.0/Fiji_Plugins-3.1.0.jar) |
| [`guava`](https://github.com/google/guava) | [`guava-21.0-rc2.jar`](http://maven.imagej.net/service/local/repositories/central/content/com/google/guava/guava/21.0-rc2/guava-21.0-rc2.jar) |
| `jdom2` | [`jdom2-2.0.6.jar`](http://maven.imagej.net/service/local/repositories/bedatadriven/content/org/jdom/jdom2/2.0.6/jdom2-2.0.6.jar) |
| [`jgrapht`](https://github.com/rcpoison/jgrapht) | [`jgrapht-0.8.3.jar`](http://maven.imagej.net/service/local/repositories/bedatadriven/content/net/sf/jgrapht/jgrapht/0.8.3/jgrapht-0.8.3.jar) |
| [`legacy-imglib1`](https://github.com/fiji/legacy-imglib1) | [`legacy-imglib1-1.1.6.jar`](http://maven.imagej.net/service/local/repositories/releases/content/sc/fiji/legacy-imglib1/1.1.6/legacy-imglib1-1.1.6.jar) |
| [`mpicbg`](https://github.com/axtimwalde/mpicbg/tree/master/mpicbg) | [`mpicbg-1.1.1.jar`](http://maven.imagej.net/service/local/repositories/releases/content/mpicbg/mpicbg/1.1.1/mpicbg-1.1.1.jar) |
| [`SPIM_Registration`](https://github.com/fiji/SPIM_Registration) | [`SPIM_Registration-5.0.8.jar`](http://maven.imagej.net/service/local/repositories/releases/content/sc/fiji/SPIM_Registration/5.0.8/SPIM_Registration-5.0.8.jar) |
| [`TrackMate_`](https://github.com/fiji/TrackMate) | [`TrackMate_-3.4.2.jar`](http://maven.imagej.net/service/local/repositories/releases/content/sc/fiji/TrackMate_/3.4.2/TrackMate_-3.4.2.jar) |
