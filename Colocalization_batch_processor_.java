/** 
 * Colocalization Colormap
 * ImageJ plugin for quantifying spatial distribution of colocalization
 */

package Colocalization_Colormap_;

import java.io.File;
import java.io.IOException;
import ij.*;
import ij.gui.*;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;

public class Colocalization_batch_processor_ implements PlugIn {

	// Initiates variables. They will be used to generate dialog window with image
	// selection options, threshold settings, and to store nMDP values
	String outputname1;
	String outputname2;

	public void run(String arg) {

		boolean thresholdstat = true; // 'Autothreshold' checkbox status
		double thresholdvalue1; // Manual threshold for channel 1
		double thresholdvalue2; // Manual threshold for channel 2
		boolean nMDPstat = false; // Determines if NMDPs are to be saved in a file specified by the user. By default this value is false
		boolean batchprocessor = true; // Determines if batchproceesor mode is on.
		ResultsTable resulttable = new ResultsTable(); // Creates empty result table. It will be used later on to store
		/**
		 * Asks user to determine the input folders and the output folder
		 */		
		String inputpath1 = IJ.getDirectory("Choose input directory 1");
		if (inputpath1 == null)
			return;
		File folder1 = new File(inputpath1);
		File[] listOfFiles1 = folder1.listFiles();
		String inputpath2 = IJ.getDirectory("Choose input directory 2");
		if (inputpath2 == null)
			return;
		File folder2 = new File(inputpath2);
		File[] listOfFiles2 = folder2.listFiles();
		String outputpath = IJ.getDirectory("Choose output directory");
		if (outputpath == null)
			return;
		// Clears pre-existing output folder data
		File folder3 = new File(outputpath);
		File[] list = folder3.listFiles();
        if (list != null) {
            for (int i = 0; i < list.length; i++) {
                 File tmpF = list[i];
                 tmpF.delete();
           }
       };
		// Checks whether input folders contain pairs of corresponding images
		if (listOfFiles1.length != listOfFiles2.length) {
			// Displays exception window if the number of files id different
			IJ.showMessage("Different number of elements in the input folders");
			return;
		}
		// Generates and opens the dialog window with the threshold options
		GenericDialog dialog = new GenericDialog("Colocalization Colormap", IJ.getInstance());
		dialog.addCheckbox("Autothreshold", thresholdstat); // "Autothreshold" checkbox
		dialog.addNumericField("Threshold 1:", 0, 1); // Field with initial manual threshold value for channel 1
		dialog.addNumericField("Threshold 2:", 0, 1); // Field with initial manual threshold value for channel 1
		dialog.showDialog(); // Displays dialog window
		if (dialog.wasCanceled())
			return;
		/**
		 * Updates log
		 */	
		IJ.log("Folders has been selected");
		IJ.log("Input1: " + inputpath1);
		IJ.log("Input2: " + inputpath2);
		IJ.log("Output: " + outputpath);
		// Gets values entered in the dialog window
		thresholdstat = dialog.getNextBoolean(); // Autohreshold true\false
		thresholdvalue1 = dialog.getNextNumber(); // Manual threshold value for channel 1
		thresholdvalue2 = dialog.getNextNumber(); // Manual threshold value for channel 2
		if (thresholdstat == false) { // Checks if autothreshold checkbox is unselected
			// Checks if manual threshold values for input images were specified in the
			// dialogbox
			if (Double.isNaN(thresholdvalue1) == true || Double.isNaN(thresholdvalue2) == true) {
				// Displays exception window if manual threshold values were not specified
				IJ.showMessage("The threshold value for at least one of the data sets has not been specified");
				return;
			}
		}
		IJ.log("Autothreshold: " + String.valueOf(thresholdstat)); // Updates log
		IJ.log("");
		for (int i = 0; i < listOfFiles1.length; i++) {
			// Checks the correct structure of input folders (single folders containing files only)
			if (listOfFiles1[i].isDirectory() == true || listOfFiles2[i].isDirectory() == true) {
				// Displays exception window if subfolders are detected
				IJ.showMessage("Subfolder within inputfolders are not allowed");
				return;
			}
			if (listOfFiles1[i].isFile() == true & listOfFiles2[i].isFile() == true) {
				IJ.showProgress(i, listOfFiles1.length); // Updates process progress bar
				ImagePlus image1 = new ImagePlus(inputpath1 + listOfFiles1[i].getName()); // Loads the first image based on input path
				ImagePlus image2 = new ImagePlus(inputpath2 + listOfFiles2[i].getName()); // Loads the second image based on input path

				int index1 = listOfFiles1[i].getName().lastIndexOf('.');
				if (index1 == -1) {
					outputname1 = listOfFiles1[i].getName();
				} else {
					outputname1 = listOfFiles1[i].getName().substring(0, index1);
				}

				int index2 = listOfFiles2[i].getName().lastIndexOf('.');

				if (index2 == -1) {
					outputname2 = listOfFiles2[i].getName();
				} else {
					outputname2 = listOfFiles2[i].getName().substring(0, index2);
				}
				String outputfile = outputname1 + " " + "and" + " " + outputname2;
				// Starts colocalization process
				IJ.log("Processing files " + outputname1 + " and " + outputname2 + ". Task " + String.valueOf(i + 1) 
						+ " of " + String.valueOf(listOfFiles1.length)); // Updates log
				 // Starts colocalization process
				Colocalization_ batchprocess = new Colocalization_();
				try {
					batchprocess.colocalize(image1, image2, thresholdstat, thresholdvalue1, thresholdvalue2,
							batchprocessor, outputpath, outputfile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		IJ.log("Batch processing has been completed"); // Updates log
	}
}