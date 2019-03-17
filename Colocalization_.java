/** 
 * Colocalization Colormap
 * ImageJ plugin for quantifying spatial distribution of colocalization
 */

package Colocalization_Colormap_;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import ij.*;
import ij.gui.*;
import ij.io.FileSaver;
import ij.io.SaveDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.text.TextWindow;

public class Colocalization_ implements PlugIn {
	/**
	 * Initiates variables. They will be used to generate dialog window with image
	 * selection options and threshold settings, to store dialog output, and to save
	 * results.
	 */
	String title1 = ""; // the name of the 1st input image or stack of images (image 1)
	String title2 = ""; // the name of the 2nd input image or stack of images (image 2)
	boolean nMDPstat = false; // 'Display Icorr' checkbox status
	boolean thresholdstat = true; // 'Autothreshold' checkbox status
	double thresholdvalue1; // default manual threshold for channel 1
	double thresholdvalue2; // default manual threshold for channel 2
	boolean batchprocessor = false; // Determines if batchproceesor mode is on. By default this value is false
	String outputpath; // the directory to save results in
	String outputfile; // the name of the file that stores results

	public void run(String arg) {
		/**
		 * Creates the list of open images if any
		 */
		int[] wList = WindowManager.getIDList();
		if (wList == null) {
			IJ.noImage();
			return;
		}
		IJ.register(Colocalization_.class);
		String[] titles = new String[wList.length];
		for (int i = 0; i < wList.length; i++) {
			ImagePlus imp = WindowManager.getImage(wList[i]);
			titles[i] = imp.getTitle(); // list of the names of open images
		}
		/**
		 * Generates and opens the dialog window with the image selection options
		 */
		GenericDialog dialog = new GenericDialog("Colocalization Colormap", IJ.getInstance());
		String defaultItem1;
		String defaultItem2;
		if (wList.length > 1) {
			defaultItem1 = titles[0];
			defaultItem2 = titles[1];
		} else {
			defaultItem1 = titles[0];
			defaultItem2 = titles[0];
		}
		dialog.addChoice("Channel 1:", titles, defaultItem1); // channel 1 default selection
		dialog.addChoice("Channel 2:", titles, defaultItem2); // channel 2 default selection

		dialog.addCheckbox("Save nMDPs", nMDPstat); // "Display or save nMDPs" checkbox
		dialog.addCheckbox("Autothreshold", thresholdstat); // "Autothreshold" checkbox
		dialog.addNumericField("Threshold 1:", 0, 1); // field with initial manual threshold value for channel 1
		dialog.addNumericField("Threshold 2:", 0, 1); // field with initial manual threshold value for channel 1
		dialog.showDialog(); // Displays dialog window
		if (dialog.wasCanceled())
			return;
		/**
		 * Gets values entered in the dialog window
		 */
		nMDPstat = dialog.getNextBoolean(); // "Display or save nMDPs" true\false
		thresholdstat = dialog.getNextBoolean(); // "Autohreshold" true\false
		thresholdvalue1 = dialog.getNextNumber(); // manual threshold value for channel 1
		thresholdvalue2 = dialog.getNextNumber(); // manual threshold value for channel 2
		/**
		 * Gets images selected in the dialog window
		 */
		int index1 = dialog.getNextChoiceIndex();
		title1 = titles[index1];
		int index2 = dialog.getNextChoiceIndex();
		title2 = titles[index2];
		ImagePlus image1 = WindowManager.getImage(wList[index1]); // image 1
		ImagePlus image2 = WindowManager.getImage(wList[index2]); // image 2
		/**
		 * Starts logging
		 */
		IJ.log("Channel1: " + title1);
		IJ.log("Channel2: " + title2);
		/**
		 * Checks whether selected images are in 8-bit or in 16-bit grayscale
		 */
		if (image1.getBitDepth() > 16) {
			// Displays exception messages if at least one of the images is not in 8-bit or
			// in 16-bit grayscale
			IJ.showMessage("Convert " + title1 + " image to 8-bit or 16-bit grayscale.");
			return;
		}
		if (image2.getBitDepth() > 16) {
			IJ.showMessage("Convert " + title2 + " image to 8-bit or 16-bit grayscale.");
			return;
		}
		if (image1.getBitDepth() != image2.getBitDepth()) {
			// Displays exception message if input images have different bit depths
			IJ.showMessage("Images must have the same bit depth.");
			return;
		}
		/**
		 * Starts colocalization process
		 */
		try {
			colocalize(image1, image2, thresholdstat, thresholdvalue1, thresholdvalue2, batchprocessor, outputpath,
					outputfile); // Applies colocalization process to the selected images
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initiates variables. They will be used to calculate and display colormap,
	 * threshold, maximum and average intensities, nMDP and Icorr values
	 */
	final public void colocalize(ImagePlus image1, ImagePlus image2, boolean thresholdstat, double thresholdvalue1,
			double thresholdvalue2, boolean batchprocessor, String outputpath, String outputfile) throws IOException {
		IJ.showStatus("Initialization..."); // Updates process status
		IJ.log("Initialization..."); // Updates log
		// Generates 'jet' map based on RGB standard ( red channel -colormapr,
		// green channel -colormapg, blue channel -colormapb)
		int[] colormapr = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 8, 12, 16,
				20, 24, 28, 32, 36, 40, 44, 48, 52, 56, 60, 64, 68, 72, 76, 80, 84, 88, 92, 96, 100, 104, 108, 112, 116,
				120, 124, 128, 131, 135, 139, 143, 147, 151, 155, 159, 163, 167, 171, 175, 179, 183, 187, 191, 195, 199,
				203, 207, 211, 215, 219, 223, 227, 231, 235, 239, 243, 247, 251, 255, 255, 255, 255, 255, 255, 255, 255,
				255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
				255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
				255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 251, 247, 243, 239, 235, 231,
				227, 223, 219, 215, 211, 207, 203, 199, 195, 191, 187, 183, 179, 175, 171, 167, 163, 159, 155, 151, 147,
				143, 139, 135, 131, 128 };
		int[] colormapg = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 4, 8, 12, 16, 20, 24, 28, 32, 36, 40, 44, 48, 52, 56, 60, 64, 68, 72, 76, 80, 84, 88, 92, 96, 100,
				104, 108, 112, 116, 120, 124, 128, 131, 135, 139, 143, 147, 151, 155, 159, 163, 167, 171, 175, 179, 183,
				187, 191, 195, 199, 203, 207, 211, 215, 219, 223, 227, 231, 235, 239, 243, 247, 251, 255, 255, 255, 255,
				255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
				255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
				255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 251, 247,
				243, 239, 235, 231, 227, 223, 219, 215, 211, 207, 203, 199, 195, 191, 187, 183, 179, 175, 171, 167, 163,
				159, 155, 151, 147, 143, 139, 135, 131, 128, 124, 120, 116, 112, 108, 104, 100, 96, 92, 88, 84, 80, 76,
				72, 68, 64, 60, 56, 52, 48, 44, 40, 36, 32, 28, 24, 20, 16, 12, 8, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		int[] colormapb = { 131, 135, 139, 143, 147, 151, 155, 159, 163, 167, 171, 175, 179, 183, 187, 191, 195, 199,
				203, 207, 211, 215, 219, 223, 227, 231, 235, 239, 243, 247, 251, 255, 255, 255, 255, 255, 255, 255, 255,
				255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
				255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
				255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 251, 247, 243, 239, 235, 231,
				227, 223, 219, 215, 211, 207, 203, 199, 195, 191, 187, 183, 179, 175, 171, 167, 163, 159, 155, 151, 147,
				143, 139, 135, 131, 128, 124, 120, 116, 112, 108, 104, 100, 96, 92, 88, 84, 80, 76, 72, 68, 64, 60, 56,
				52, 48, 44, 40, 36, 32, 28, 24, 20, 16, 12, 8, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0 };
		double nMDP; // nMDP value
		int nMDP8bit; // nMDP value mapped to 8-bit range of 256 indices
		double nMDPpos = 0; // the number of positive nMDPs
		double nMDPall = 0; // the number of all nMDPs
		double max1 = 0; // max intensity in image 1
		double max2 = 0; // max intensity in image 2
		double meansum1 = 0; // sum of intensities in image 1
		double meansum2 = 0; // sum of intensities in image 2
		double count1 = 0; // the number of above-threshold pixels in image 1
		double count2 = 0; // the number of above-threshold pixels in image 2
		double threshold1 = 0; // the threshold for image 1
		double threshold2 = 0; // the threshold for image 2
		/**
		 * Gets information about the size of input images (the number of pixels in a
		 * row and column, the number of planes)
		 */
		ImageStack stack1 = image1.getImageStack(); // Gets input stack for image 1
		int width1 = stack1.getWidth(); // width of image 1 in pixels
		int height1 = stack1.getHeight(); // height of image 2 in pixels
		int endFrame1 = stack1.getSize(); // the number of planes in image 1
		ImageStack stack2 = image2.getImageStack(); // Gets input stack for image 2
		int width2 = stack2.getWidth(); // width of image 2 in pixels
		int height2 = stack2.getHeight(); // height of image 2 in pixels
		int endFrame2 = stack2.getSize(); // the number of planes in image 2
		/**
		 * Checks whether input images are of the same size
		 */
		if (width1 != width2 || height1 != height2 || endFrame1 != endFrame2) {
			// Displays exception window if images are not of the same size
			IJ.showMessage("The source images or image stacks must have the same size");
			return;
		}
		/**
		 * Gets manual threshold values entered in the dialog window.
		 */
		if (thresholdstat == false) { // Checks if autothreshold checkbox is unselected
			// Checks if manual threshold values for input images were specified in the
			// dialogbox
			if (Double.isNaN(thresholdvalue1) == true || Double.isNaN(thresholdvalue2) == true) {
				// Displays exception window if manual threshold values were not specified
				IJ.showMessage("The threshold value for at least one of the images has not been specified");
				return;
			}
			IJ.showStatus("Calculating thresholds..."); // Updates process status
			threshold1 = thresholdvalue1; // Gets manual threshold value for image 1
			threshold2 = thresholdvalue2; // Gets manual threshold value for image 2
			IJ.log("Threshold1: " + String.valueOf(threshold1)); // Updates log
			IJ.log("Threshold2: " + String.valueOf(threshold2)); // Updates log
		} else {
			/**
			 * Calculates autothreshold values.
			 */
			IJ.showStatus("Applying thresholds..."); // Updates process status
			IJ.log("Applying thresholds..."); // Updates log
			// Calculates autothreshold for image1 using default ImgeJ method (variation of
			// IsoData algorithm)
			IJ.setAutoThreshold(image1, "Default dark stack");
			threshold1 = image1.getProcessor().getMinThreshold();
			IJ.resetThreshold(image1);
			// Calculates autothreshold for image1 using default ImgeJ method (variation of
			// IsoData algorithm)
			IJ.setAutoThreshold(image2, "Default dark stack");
			threshold2 = image2.getProcessor().getMinThreshold();
			IJ.resetThreshold(image2);
			IJ.log("Threshold 1: " + String.valueOf(threshold1)); // Updates log
			IJ.log("Threshold 2: " + String.valueOf(threshold2)); // Updates log
		}
		/**
		 * Calculates mean pixel intensities in the ROI (pixels equal to or above
		 * threshold) of image1 and image2
		 */
		IJ.showStatus("Calculating mean and max intensities..."); // Updates process status
		IJ.log("Calculating mean and max intensities..."); // Updates process status
		for (int i = 1; i < endFrame1 + 1; i++) {
			IJ.showProgress(i, endFrame1); // Updates process progress bar
			ImageProcessor grayprocessor1 = stack1.getProcessor(i); // Gets image 1 processor
			ImageProcessor grayprocessor2 = stack2.getProcessor(i); // Gets image 2 processor
			for (int y = 0; y < height1; y++)
				for (int x = 0; x < width1; x++) {
					int value1 = grayprocessor1.getPixel(x, y); // Gets the first pixel value
					int value2 = grayprocessor2.getPixel(x, y); // Gets the second pixel value
					if (value1 >= threshold1 || value2 >= threshold2) { // Only pixels in the ROI are taken into account
						meansum1 = meansum1 + value1; // sum of all intensities for image 1 ROI
						count1 = count1 + 1; // the number of pixels in the ROI of image 1 ROI
						if (value1 > max1)
							max1 = value1; // maximum intensity in the image 1 ROI
					}
					if (value1 >= threshold1 || value2 >= threshold2) { // Only pixels in the ROI are taken into account
						meansum2 = meansum2 + value2; // sum of all intensities for image 2 ROI
						count2 = count2 + 1; // the number of pixels in the ROI of image 2 ROI
						if (value2 > max2)
							max2 = value2; // maximum intensity in the image 2 ROI
					}
				}
		}
		IJ.log("Maximum 1: " + String.valueOf(max1)); // Updates log
		IJ.log("Maximum 2: " + String.valueOf(max2)); // Updates log
		/**
		 * To avoid division by zero checks whether ROIs of input images contain any
		 * data
		 */
		if (count1 == 0 || count2 == 0) {
			// Displays exception message if images are empty
			IJ.showMessage("Exception (impossible case):\n \nAt least one of your images can not be processed."
					+ "\nProbably there are only black pixels above the threshold."
					+ "\nCheck image and threshold parameters.");
			return;
		}
		double mean1 = meansum1 / count1; // mean pixel intensity for the image1 ROI
		double mean2 = meansum2 / count2; // mean pixel intensity for the image2 ROI
		IJ.log("Mean 1: " + String.valueOf(mean1));
		IJ.log("Mean 2: " + String.valueOf(mean2));
		/**
		 * To avoid division by zero checks whether maximum and mean pixel intensities
		 * are different for given input images if mean=max in at least one of the
		 * images, nMDP value will not be represented by a number
		 */
		if (max1 == mean1 || max2 == mean2) {
			// Displays exception message if the above is true
			IJ.showMessage("Exception (impossible case):\n \nAt least one of your images can not be processed."
					+ "\nProbably all above-threshold pixels contain the same intensity value.\n \n"
					+ "Note that mean intensity value and maximum intensity value\nmust be different "
					+ "to calculate the mean deviation product properly.");
			return;
		}
		/**
		 * Calculates nMDPs, the number of nMDPs, the number of positive nMDPs, Icorr,
		 * and displays results
		 */
		ImageStack colocalizationstack = new ImageStack(width1, height1); // Creates an empty result stack. It will be
																			// used later on to store colormap.
		ArrayList<Double> resulttable = new ArrayList<Double>();
		; // Creates empty result table. It will be used later on to store
		// calculated nMDP values.
		IJ.showStatus("Calculating Icor and nMDPs..."); // Updates process status.
		IJ.log("Calculating Icor and nMDPs..."); // Updates log.
		for (int i = 1; i < endFrame1 + 1; i++) {
			IJ.showProgress(i, endFrame1); // Updates process progress bar.
			ImageProcessor grayprocessor1 = stack1.getProcessor(i); // Gets image 1 processor
			ImageProcessor grayprocessor2 = stack2.getProcessor(i); // Gets image 2 processor
			ImageProcessor colocalizationprocessor = stack1.getProcessor(i).convertToRGB(); // Creates result image
																							// processor
			// Gets intensity values of corresponding pixels from the ROIs of image1 and
			// image2
			for (int y = 0; y < height1; y++)
				for (int x = 0; x < width1; x++) {
					int value1 = grayprocessor1.getPixel(x, y); // Gets the first pixel value
					int value2 = grayprocessor2.getPixel(x, y); // Gets the second pixel value
					if (value1 >= threshold1 || value2 >= threshold2) { // Only pixels in the ROI are taken into account
						// Calculates nMDP for pairs of corresponding pixels
						nMDP = ((value1 - mean1) * (value2 - mean2)) / ((max1 - mean1) * (max2 - mean2));
						if (nMDP > 1)
							nMDP = 1;
						if (nMDP < -1)
							nMDP = -1;
						if (nMDP > 0) {
							nMDPpos = nMDPpos + 1; // the number of positive nMDPs
						}
						/**
						 * Creates result table and result array. Both containing calculated nMDP values
						 */
						// Adds nMDPs to the result table (they will be displayed if requested by the user)
						resulttable.add(nMDP);
						nMDPall = nMDPall + 1; // the number of nMDPs
						// Maps nMDP values onto 8-bit RGB standard
						nMDP8bit = (int) Math.round(((nMDP + 1) / 2) * 255);
						/**
						 * Applies RGB values of 'jet' colormap to the result image processor in order to
						 * generate colormap (for pixels equal to or above the threshold)
						 */
						int[] rgb24bit = { colormapr[nMDP8bit], colormapg[nMDP8bit], colormapb[nMDP8bit] };
						colocalizationprocessor.putPixel(x, y, rgb24bit);
					} else {
						int[] zero = { 0, 0, 0 };
						colocalizationprocessor.putPixel(x, y, zero); // Applies zero values to the empty result image
																		// (for pixels below the threshold);
					}
				}
			colocalizationstack.addSlice("", colocalizationprocessor); // Assigns calculated colormap to the empty stack
																		// slice by slice
		}
		double Icorr = nMDPpos / nMDPall; // Calculates Icorr
		if (batchprocessor == false) {
			/**
			 * Displays results
			 */
			// Gets names of input files (names only, without extensions)
			String outputname1 = "";
			int index1 = title1.lastIndexOf('.');
			if (index1 == -1) {
				outputname1 = title1;
			} else {
				outputname1 = title1.substring(0, index1);
			}

			String outputname2 = "";
			int index2 = title2.lastIndexOf('.');
			if (index1 == -1) {
				outputname2 = title2;
			} else {
				outputname2 = title2.substring(0, index2);
			}

			outputfile = outputname1 + " " + outputname2; // Assembles names of input files. This will be used
															// to create result file name
			ImagePlus colocalizationimage = new ImagePlus(outputfile + " colocalization", colocalizationstack); // Generates
																												// colormap
																												// image
			IJ.showStatus("Displaying results..."); // Updates process status.
			IJ.log("Displaying results..."); // Updates log
			colocalizationimage.show(); // Displays colormap image
			TextWindow textwindow = new TextWindow("Index of correlation", Double.toString(Icorr), 435, 180);
			/**
			 * Saving nMDPs
			 */
			if (nMDPstat == true) {
				IJ.log("Saving nMDPs..."); // Updates log
				SaveDialog savewindow = new SaveDialog("Save nMDPs", outputname1 + " and " + outputname2 + " nMDPs",
						".txt"); // Opens "Save as" dialog window
				String savefile = savewindow.getFileName(); // Gets file name specified by the user
				String savedirectory = savewindow.getDirectory(); // Gets directory specified by the user
				// Saves nMDPs in the specified directory and file
				PrintWriter printwritter = new PrintWriter(new FileOutputStream(savedirectory + savefile));
				for (Double element : resulttable)
					printwritter.println(element);
				printwritter.close();
			}
		}
		/**
		 * Saves results in batchprocessor mode
		 */
		if (batchprocessor == true) {
			IJ.log("Saving results..."); // Updates log
			// Saves nMDPs in txt file
			PrintWriter printwritter = new PrintWriter(
					new FileOutputStream(outputpath + "nMDPs " + outputfile + ".txt"));
			for (Double element : resulttable)
				printwritter.println(element);
			printwritter.close();
			// Saves Icorr in txt file
			PrintWriter printwritter2 = new PrintWriter(
					new FileOutputStream(outputpath + "Icorr " + outputfile + ".txt"));
			printwritter2.println(Icorr);
			printwritter2.close();
			ImagePlus colocalizationimage = new ImagePlus(outputfile + " colocalization", colocalizationstack); // Generates
																												// colormap
																												// image
			// Saves colormap in a tiff file
			FileSaver savecolormap = new FileSaver(colocalizationimage);
			if (endFrame1 == 1)
				savecolormap.saveAsTiff(outputpath + "Colormap " + outputfile + ".tif");
			else
				savecolormap.saveAsTiffStack(outputpath + "Colormap " + outputfile + ".tif");
		}
		IJ.showStatus("Colocalization colormap - the process has been finished"); // Updates process status
		IJ.log("The process has been finished"); // Updates log
		IJ.log("");
	}
}