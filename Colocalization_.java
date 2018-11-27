/** 
 * Colocalization Colormap
 * ImageJ plugin for quantifying spatial distribution of colocalization
 */

package Colocalization_Colormap_;

import java.text.DecimalFormat; //Imports a DecimalFormat
//Iports IJ packages
import ij.*;
import ij.gui.*;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.text.TextWindow;

public class Colocalization_ implements PlugIn {
	// Initiates variables. They will be used to generate dialog window with image selection options, store nMDP
	private static String title1 = ""; // the name of the 1st input image or stack of images (image1)
	private static String title2 = ""; // the name of the 2nd input image or stack of images (image2)
	private static boolean nMDPstat = true; // 'Display Icorr' checkbox status
	double nMDP; // nMDP value
	int nMDP2; // nMDP value mapped to 8-bit range of 256 indicies

	public void run(String arg) {
		// Creates the list of open image windows if any
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
		//Generates and opens the dialog window with the image selection options
		GenericDialog dialog = new GenericDialog("Colocalization Colormap", IJ.getInstance());
		String defaultItem;
		if (title1.equals(""))
			defaultItem = titles[0];
		else
			defaultItem = title1;
		dialog.addChoice("Channel1:", titles, defaultItem);
		if (title2.equals(""))
			defaultItem = titles[0];
		else
			defaultItem = title2;
		dialog.addChoice("Channel2:", titles, defaultItem);
		dialog.addCheckbox("Display Icorr", nMDPstat);
		dialog.showDialog();
		if (dialog.wasCanceled())
			return;
		nMDPstat = dialog.getNextBoolean();
		int index1 = dialog.getNextChoiceIndex();
		title1 = titles[index1];
		int index2 = dialog.getNextChoiceIndex();
		title2 = titles[index2];
		ImagePlus image1 = WindowManager.getImage(wList[index1]);
		ImagePlus image2 = WindowManager.getImage(wList[index2]);
		// Checks whether selected images are in 8-bit grayscale
		if (image1.getBitDepth() != 8) {
		// Displays exception messages if images at least one of the images is not in 8-bit grayscale
			IJ.showMessage("Convert " + title1 + " image to 8-bit grayscale.");
			return;
		}
		if (image2.getBitDepth() != 8) {
			IJ.showMessage("Convert " + title2 + " image to 8-bit grayscale.");
			return;
		}
		if (image1.getProcessor().isDefaultLut() == false) {
			IJ.showMessage("8-bit color images are not supported. \nConvert " + title1 + " image to 8-bit grayscale.");
			return;
		}
		if (image2.getProcessor().isDefaultLut() == false) {
			IJ.showMessage("8-bit color images are not supported. \nConvert " + title2 + " image to 8-bit grayscale.");
			return;
		}
		colocalize(image1, image2); //Starts colocalization process
	}
	//Generates 8-bit 'jet' map based on RGB standard ( red channel -colormapr, green channel -colormapg, blue channel -colormapb)
	public void colocalize(ImagePlus image1, ImagePlus image2) {
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
		//Initiates variables. They wil be used to calculate threshold, nMDP and Icorr values
		double nMDPpos = 0; // the number of positive nMDPs
		double nMDPall = 0; // the number of all nMDPs
		double nMDPsum = 0; // the sum of all nMDPs
		double max1 = 0; // max intensity in image1
		double max2 = 0; // max intensity in image2
		double meansum1 = 0; // sum of intensities in image1
		double meansum2 = 0; // sum of intensities in image2
		double count1 = 0; // the number of non zero pixels in image1
		double count2 = 0; // the number of non zero pixels in image2
		double maxthreshold1 = 0; // the highest threshol calculated for indvidual planes in image1
		double maxthreshold2 = 0; // the highest threshol calculated for indvidual planes in image2
		//Gets information about the size of input images (the number of pixels in a row and column, the number of planes)
		ImageStack stack1 = image1.getImageStack();
		int width1 = stack1.getWidth();
		int height1 = stack1.getHeight();
		int endFrame1 = stack1.getSize();
		ImageStack stack2 = image2.getImageStack();
		int width2 = stack2.getWidth();
		int height2 = stack2.getHeight();
		int endFrame2 = stack2.getSize();
		//Checks wheather input images are of the same size
		if (width1 != width2 || height1 != height2 || endFrame1 != endFrame2) {
                //Displays exception window if images ar not of the same size
			IJ.showMessage("The source images or image stacks must have the same size");
			return;
		}
		// Calculates thresholds and maximum pixel intensities in the image1 and image2
		for (int i = 1; i < endFrame1 + 1; i++) {
			IJ.showProgress(i, endFrame1 - 1);
			ImageProcessor grayprocessor1 = stack1.getProcessor(i);
			int threshold1 = grayprocessor1.getAutoThreshold();
			if (threshold1 > maxthreshold1)
				maxthreshold1 = threshold1;
			int maximum1 = (int) grayprocessor1.maxValue();
			if (maximum1 > max1)
				max1 = maximum1;
			ImageProcessor grayprocessor2 = stack2.getProcessor(i);
			int threshold2 = grayprocessor2.getAutoThreshold();
			if (threshold2 > maxthreshold2)
				maxthreshold2 = threshold2;
			int maximum2 = (int) grayprocessor2.maxValue();
			if (maximum2 > max2)
				max2 = maximum2;
		}

		// Calculates mean pixel intensities in the image1 and image2
		for (int i = 1; i < endFrame1 + 1; i++) {
			IJ.showProgress(i, endFrame1 - 1);
			ImageProcessor grayprocessor1 = stack1.getProcessor(i);
			ImageProcessor grayprocessor2 = stack2.getProcessor(i);
			for (int y = 0; y < height1 + 1; y++)
				for (int x = 0; x < width1 + 1; x++) {
					int value1 = grayprocessor1.getPixel(x, y);
					int value2 = grayprocessor2.getPixel(x, y);
					if (value1 >= maxthreshold1 || value2 >= maxthreshold2) {
						meansum1 = meansum1 + value1;
						count1 = count1 + 1;
					}
					if (value1 > maxthreshold1 || value2 > maxthreshold2) {
						meansum2 = meansum2 + value2;
						count2 = count2 + 1;
					}
				}
		}
		// To avoid division by zero checks whether input images contain any data
		if (count1 == 0 || count2 == 0) {
			// Displays exception message if images are empty
			IJ.showMessage(
					"Exception (impossible case):\n \nAt least one of your images or stacks planes can not be processed."
							+ "\nProbably there are only background pixels (black) in the image.");
			return;
		}
		double mean1 = meansum1 / count1;
		double mean2 = meansum2 / count2;
		nMDPall = count1;
		ImageStack colocalizationstack = new ImageStack(width1, height1);
		for (int i = 1; i < endFrame1 + 1; i++) {
			IJ.showProgress(i, endFrame1 - 1); // Updates progress bar
			//Creates an empty result image
			colocalizationstack.addSlice("", image1.getImageStack().getProcessor(i).convertToRGB());
			ImageProcessor grayprocessor1 = stack1.getProcessor(i);
			ImageProcessor grayprocessor2 = stack2.getProcessor(i);
			ImageProcessor colocalizationprocessor = colocalizationstack.getProcessor(i);
			// Gets intensity values of corresponding pixels from image1 and image2
			for (int y = 0; y < height1 + 1; y++)
				for (int x = 0; x < width1 + 1; x++) {
					int value1 = grayprocessor1.getPixel(x, y);
					int value2 = grayprocessor2.getPixel(x, y);
					if (value1 >= maxthreshold1 || value2 >= maxthreshold2) {
						// Calculates nMDP for pairs of corresponding pixels from image1 and image2
						nMDP = ((value1 - mean1) * (value2 - mean2)) / ((max1 - mean1) * (max2 - mean2));
						nMDPsum = nMDP + 1;
						if (nMDP > 1) {
							nMDP = 1;
						}
						if (nMDP < -1) {
							nMDP = -1;
						}
						if (nMDP > 0) {
							nMDPpos = nMDPpos + 1;
						}
						/** To avoid division by zero checks whether maximum and mean pixel intensities are different for each of given input images
						if mean=max in at least on of the images, nMDP value will not be represented by a number */
						if (Double.isNaN(nMDP)) {
						// Displays exception message if the above is true
							IJ.showMessage(
									"Exception (impossible case):\n \nAt least one of your images or stack planes can not be processed."
											+ "\nProbably all non-background pixels in the R contain the same intensity value.\n \n"
											+ "Note that mean intensity value and maximum intensity value\nmust be different "
											+ "to calculate the mean deviation product properly.");
							return;
						}
						nMDP2 = (int) Math.round(((nMDP + 1) / 2) * 255); // Converts calculated nMDP values to fit 8-bir RGB standard
						// Constructs the result colormap image based on converted nMDP values
						int[] rgb = { colormapr[nMDP2], colormapg[nMDP2], colormapb[nMDP2] };
						colocalizationprocessor.putPixel(x, y, rgb);
					} else {
						int[] zero = { 0, 0, 0 };
						colocalizationprocessor.putPixel(x, y, zero);
					}

				}
		}
		// Displays results
		ImagePlus colocalizationimage = new ImagePlus(title1 + " and " + title2 + " colocalization",
				colocalizationstack);
		colocalizationimage.show();
		if (nMDPstat == true) {
			TextWindow textwindow = new TextWindow("Index of correlation", "Icorr: " + Double.toString(nMDPpos / nMDPall), 435, 180);
		}
	}
}