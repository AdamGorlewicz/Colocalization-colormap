/** 
 * Colocalization Colormap
 * ImageJ plugin for quantifying spatial distribution of colocalization
 */

package Colocalization_Colormap_;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

// Iports IJ packages
import ij.*;
import ij.gui.*;
import ij.io.FileSaver;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.text.TextWindow;

public class Colocalization_ implements PlugIn {
	/**
	 * Initiates variables. They will be used to generate dialog window with image
	 * selection options, threshold settings, and to store dialog output
	 */
	String title1 = ""; // the name of the 1st input image or stack of images (image 1)
	String title2 = ""; // the name of the 2nd input image or stack of images (image 2)
	boolean nMDPstat = false; // 'Display Icorr' checkbox status
	boolean Thresholdstat = true; // 'Autothreshold' checkbox status
	double Thresholdvalue1; // default manual threshold for channel 1
	double Thresholdvalue2; // default manual threshold for channel 2
	boolean batchprocessor = false;
	String outputpath;
	String outputfile;

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
			if (imp != null)
				titles[i] = imp.getTitle();
			else
				titles[i] = "";
		}
		/**
		 * Generates and opens the dialog window with the image selection options
		 */
		GenericDialog dialog = new GenericDialog("Colocalization Colormap", IJ.getInstance());
		String defaultItem;
		if (title1.equals(""))
			defaultItem = titles[0];
		else
			defaultItem = title1;
		dialog.addChoice("Channel 1:", titles, defaultItem); // channel 1 selection
		if (title2.equals(""))
			defaultItem = titles[0];
		else
			defaultItem = title2;
		dialog.addChoice("Channel " + "2:", titles, defaultItem); // channel 1 selection
		dialog.addCheckbox("Save nMDPs", nMDPstat); // "Display or save nMDPs" checkbox
		dialog.addCheckbox("Autothreshold", Thresholdstat); // "Autothreshold" checkbox
		dialog.addNumericField("Threshold 1:", 0, 1); // field with initial manual threshold value for channel 1
		dialog.addNumericField("Threshold 2:", 0, 1); // field with initial manual threshold value for channel 1
		dialog.showDialog(); // Displays dialog window
		if (dialog.wasCanceled())
			return;
		/**
		 * Gets values entered in the dialog window
		 */
		nMDPstat = dialog.getNextBoolean(); // "Display or save nMDPs" true\false
		Thresholdstat = dialog.getNextBoolean(); // "Autohreshold" true\false
		Thresholdvalue1 = dialog.getNextNumber(); // manual threshold value for channel 1
		Thresholdvalue2 = dialog.getNextNumber(); // manual threshold value for channel 2
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
			colocalize(image1, image2, Thresholdstat, Thresholdvalue1, Thresholdvalue2, batchprocessor, outputpath, outputfile); // Applies process to the selected images
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initiates variables. They will be used to calculate and display colormap,
	 * threshold, maximum and average intensities, nMDP and Icorr values
	 */
	final public void colocalize(ImagePlus image1, ImagePlus image2, boolean Thresholdstat, double Thresholdvalue1, double Thresholdvalue2, boolean batchprocessor, String outputpath, String outputfile) throws IOException {
		IJ.showStatus("Initialization..."); // Updates process status
		// Generates 8-bit 'jet' map based on RGB standard ( red channel -colormapr,
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
		double threshold1 = 0; // the highest threshold calculated for individual planes in image 1
		double threshold2 = 0; // the highest threshold calculated for individual planes in image 2
		/**
		 * Gets information about the size of input images (the number of pixels in a
		 * row and column, the number of planes)
		 */
		ImageStack stack1 = image1.getImageStack(); // Gets input stack for image 1
		int width1 = stack1.getWidth(); // width of image 1 in pixels
		int height1 = stack1.getHeight(); // height of image 2 in pixels
		int endFrame1 = stack1.getSize(); // the number of planes image 1
		ImageStack stack2 = image2.getImageStack(); // Gets input stack for image 2
		int width2 = stack2.getWidth(); // width of image 2 in pixels
		int height2 = stack2.getHeight(); // height of image 2 in pixels
		int endFrame2 = stack2.getSize(); // the number of planes image 2
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
		if (Thresholdstat == false) { // Checks if autothreshold checkbox is unselected
			if (Double.isNaN(Thresholdvalue1) == true || Double.isNaN(Thresholdvalue2) == true) { // Checks if manual
																									// threshold values
																									// for input images
																									// were specified in
																									// the dialogbox
				IJ.showMessage("The threshold value for at least one of the images has not been specified"); // Displays
																												// exception
																												// window
																												// if
																												// manual
																												// threshold
																												// values
																												// were
																												// not
																												// specified
				return;
			}
			IJ.showStatus("Calculating thresholds..."); // Updates process status
			threshold1 = Thresholdvalue1; // Gets manual threshold value for image 1
			threshold2 = Thresholdvalue2; // Gets manual threshold value for image 2
		} else {
			/**
			 * Calculates autothreshold values.
			 */
			IJ.showStatus("Calculating thresholds..."); // Updates process status
			IJ.setAutoThreshold(image1, "Default dark stack");
			threshold1 = image1.getProcessor().getMinThreshold(); 
			IJ.resetThreshold(image1);
			IJ.setAutoThreshold(image2, "Default dark stack");
			threshold2 = image2.getProcessor().getMinThreshold(); 
			IJ.resetThreshold(image2);
			//threshold1 = image1.getProcessor().getAutoThreshold();
			//threshold2 = image2.getProcessor().getAutoThreshold();
		}
		/**
		 * Calculates max intensities.
		 */
		IJ.showStatus("Calculating max intensities..."); // Updates process status
		max1 = image1.getStatistics().max; // Calculates maximum intensity value for image 1
		max2 = image2.getStatistics().max; // Calculates maximum intensity value for image 2
		/**
		 * Calculates mean pixel intensities in the ROI (pixels equal to or above
		 * threshold) of image1 and image2
		 */
		IJ.showStatus("Calculating mean intensities..."); // Updates process status
		for (int i = 1; i < endFrame1 + 1; i++) {
			IJ.showProgress(i, endFrame1); // Updates process progress bar
			ImageProcessor grayprocessor1 = stack1.getProcessor(i); // Gets image 1 processor for calculation
			ImageProcessor grayprocessor2 = stack2.getProcessor(i); // Gets image 2 processor for calculation
			for (int y = 0; y < height1 + 1; y++)
				for (int x = 0; x < width1 + 1; x++) {
					int value1 = grayprocessor1.getPixel(x, y);
					int value2 = grayprocessor2.getPixel(x, y);
					if (value1 >= threshold1 || value2 >= threshold2) { // Only pixels in the ROI are taken into account
						meansum1 = meansum1 + value1; // sum of all intensities for image 1 ROI
						count1 = count1 + 1; // the number of pixels in the ROI of image 1 ROI
					}
					if (value1 > threshold1 || value2 > threshold2) { // Only pixels in the ROI are taken into account
						meansum2 = meansum2 + value2; // sum of all intensities for image 2 ROI
						count2 = count2 + 1; // the number of pixels in the ROI of image 2 ROI
					}
				}
		}
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
		double mean1 = meansum1 / count1; // mean pixel intensity for image1 ROI
		double mean2 = meansum2 / count2; // mean pixel intensity for image2 ROI
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
		ArrayList<Double> resulttable = new ArrayList<Double>();; // Creates empty result table. It will be used later on to store
														// calculated nMDP values.
		IJ.showStatus("Calculating Icor and nMDPs..."); // Updates process status.

		for (int i = 1; i < endFrame1 + 1; i++) {
			IJ.showProgress(i, endFrame1); // Updates process progress bar
			ImageProcessor grayprocessor1 = stack1.getProcessor(i); // Gets image 1 processor for calculation
			ImageProcessor grayprocessor2 = stack2.getProcessor(i); // Gets image 2 processor for calculation
			ImageProcessor colocalizationprocessor = stack1.getProcessor(i).convertToRGB(); // Creates result image
																							// processor for calculation
			// Gets intensity values of corresponding pixels from the ROIs of image1 and
			// image2
			for (int y = 0; y < height1; y++)
				for (int x = 0; x < width1; x++) {
					int value1 = grayprocessor1.getPixel(x, y); // Gets first pixel value
					int value2 = grayprocessor2.getPixel(x, y); // Gets second pixel value
					if (value1 >= threshold1 || value2 >= threshold2) { // Only pixels in the ROI are taken into account
						// Calculates nMDP for pairs of corresponding pixels
						nMDP = ((value1 - mean1) * (value2 - mean2)) / ((max1 - mean1) * (max2 - mean2));
						/**
						 * nMDPs are expected to be in a rage of -1 to 1. However, very few nMDPs
						 * calculated here might be slightly out of that range. This imprecision is due
						 * to the fact that irrational numbers are approximated. The code below corrects
						 * values that are out of limits.
						 */
						if (nMDP > 1) {
							nMDP = 1;
						}
						if (nMDP < -1) {
							nMDP = -1;
						}
						if (nMDP > 0) {
							nMDPpos = nMDPpos + 1; // the number of positive nMDPs
						}
						/**
						 * Creates result table and result array. both containing calculated nMDP values
						 */
						resulttable.add(nMDP); // Adds nMDPs to the resulttable (they will be displayed if
															// necessary)
						nMDPall = nMDPall + 1; // the number of nMDPs
						nMDP8bit = (int) Math.round(((nMDP + 1) / 2) * 255); // Converts calculated nMDP values to fit
																				// 8-bit RGB standard
						/**
						 * Applies RGB values of 'jet' colormap to the result image processor to
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

		/**
		 * Displays results
		 */
		ImagePlus colocalizationimage = new ImagePlus(title1 + " and " + title2 + " colocalization",
				colocalizationstack); // Generates colormap image
		double Icorr = nMDPpos / nMDPall;
		if (batchprocessor == false) {
		IJ.showStatus("Displaying results..."); // Updates process status.
		colocalizationimage.show(); // Displays colormap image
		TextWindow textwindow = new TextWindow("Index of correlation", Double.toString(Icorr),
				435, 180); // Calculates and displays Icorr
		if (nMDPstat == true) {
			String nMDPspath = IJ.getDirectory("Choose directory to save nMDPs");
		    PrintWriter printwritter = new PrintWriter(new FileOutputStream(nMDPspath + "nMDPs" + ".txt"));
		    for (Double element : resulttable)
		    printwritter.println(element);
		    printwritter.close();
		}
		}
		if (batchprocessor == true) {
		    PrintWriter printwritter = new PrintWriter(new FileOutputStream(outputpath + outputfile + ".txt"));
		    for (Double element : resulttable)
		    printwritter.println(element);
		    printwritter.close();
		FileSaver savecolormap = new FileSaver(colocalizationimage);
		if(endFrame1==1)
		savecolormap.saveAsTiff(outputpath + outputfile + ".tif");
		else
		savecolormap.saveAsTiffStack(outputpath + outputfile + ".tif");
		}
		IJ.showStatus("Colocalization colormap - the process has been finished"); // Updates process status
	}
}