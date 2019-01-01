/** 
 * Colocalization Colormap
 * ImageJ plugin for quantifying spatial distribution of colocalization
 */

package Colocalization_Colormap_;
//Iports IJ packages
import java.io.File;
import java.io.IOException;
import ij.*;
import ij.gui.*;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
//Iports IJ packages

public class Colocalization_batch_processor_ implements PlugIn {
	
	// Initiates variables. They will be used to generate dialog window with image selection options, threshold settings, and to store nMDP values
	String outputname1;
	String outputname2;
		
	public void run(String arg) {
		
		
		boolean Thresholdstat = true; // 'Autothreshold' checkbox status
		double Thresholdvalue1; // Manual threshold for channel 1
		double Thresholdvalue2; // Manual threshold for channel 2
		boolean nMDPstat = false;
		boolean batchprocessor = true;
		ResultsTable resulttable = new ResultsTable(); // Creates empty result table. It will be used later on to store
		
		
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
		if (listOfFiles1.length != listOfFiles2.length) {
		IJ.showMessage("Different noumber of elements in the input folders");
		return;	}
		
		// Generates and opens the dialog window with the image selection options
		GenericDialog dialog = new GenericDialog("Colocalization Colormap", IJ.getInstance());
		dialog.addCheckbox("Autothreshold", Thresholdstat); // "Autothreshold" checkbox
		dialog.addNumericField("Threshold 1:", 0, 1); // Field with initial manual threshold value for channel 1
		dialog.addNumericField("Threshold 2:", 0, 1); // Field with initial manual threshold value for channel 1
		dialog.showDialog(); // Displays dialog window
		if (dialog.wasCanceled())
			return;
		// Gets values entered in the dialog window
		Thresholdstat = dialog.getNextBoolean(); // Autohreshold true\false
		Thresholdvalue1 = dialog.getNextNumber(); // Manual threshold value for channel 1
		Thresholdvalue2 = dialog.getNextNumber(); // Manual threshold value for channel 2	
		IJ.showStatus("Colocalization batch processing..."); // Updates process status.
		for (int i = 0; i < listOfFiles1.length; i++) {
		if (listOfFiles1[i].isDirectory() == true || listOfFiles2[i].isDirectory() == true) {
		IJ.showMessage("Subfolder within inputfolders are not allowed");	
		return;
		}
		if (listOfFiles1[i].isFile() == true & listOfFiles2[i].isFile() == true) {
			IJ.showProgress(i, listOfFiles1.length); // Updates process progress bar
			ImagePlus image1 = new ImagePlus(inputpath1+listOfFiles1[i].getName()); 
			ImagePlus image2 = new ImagePlus(inputpath2+listOfFiles2[i].getName()); 
			
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
			//Starts colocalization process	
			Colocalization_ batchprocess = new Colocalization_();
			try {
				batchprocess.colocalize(image1, image2, Thresholdstat, Thresholdvalue1, Thresholdvalue2, batchprocessor, outputpath, outputfile);
			} catch (IOException e) {
				e.printStackTrace();
			} 
}
		}
	}
}