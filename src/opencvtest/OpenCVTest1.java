/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package opencvtest;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author pavel.svihalek
 * ZPA Smart Energy a.s.
 * Trutnov
 * Czech republic
 */
public class OpenCVTest1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
                System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
                Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
                System.out.println("mat = " + mat.dump());

                Mat mt = new Mat(3, 1, CvType.CV_8U, new Scalar(1));
                Mat mt2 = new Mat(6, 6, CvType.CV_8U, new Scalar(1));
                
                System.out.println("mt = " + mt.dump());
                mt2.put(3, 3, 0);
                Imgproc.erode(mt2, mt2, mt);
                System.out.println("mt2 = " + mt2.dump());
    }

}
