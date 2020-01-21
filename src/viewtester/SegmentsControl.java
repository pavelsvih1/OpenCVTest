/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package viewtester;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author pavel
 */
public class SegmentsControl {
    
    /**
     * Vymaskuje obrazek danou maskou
     * 
     * @param inputPicture
     * @param maskPicture
     * @return 
     */
    public static Mat pictureMask(Mat inputPicture, Mat maskPicture) {
        Mat result = new Mat();

        Core.bitwise_and(inputPicture, maskPicture, result);
        
        return(result);
    }
    
    /**
     * Vypocita perspektivni matici dle vstupnich bodu a vystupnich bodu a 
     * orizne matici podle vystupnich bodu
     * @param inputMat
     * @param inputPoints
     * @param outputPoints
     * @return 
     */
    public static Mat perspectiveMatAndCut(Mat inputMat, Point[] inputPoints, Point[] outputPoints){
        Mat transformace = new Mat();
        Mat outputMat = new Mat();
        Mat resultMat = new Mat();

        // nejprve si spocitame transformacni matici z vypozorovanych hodnot

        MatOfPoint2f orig = new MatOfPoint2f(inputPoints);
        // vypocitame matici transformace
        transformace = Imgproc.getPerspectiveTransform(orig, new MatOfPoint2f(outputPoints));
        System.out.println("Transformace>\n" + transformace.dump());
        Imgproc.warpPerspective(inputMat, outputMat, transformace, inputMat.size());
        resultMat = outputMat.submat(new Rect(outputPoints[0], outputPoints[3]));
        return(resultMat);
    }
    
    
    /**
     * transformuje vstupni matici na velikost vzorove
     * @param inputMat
     * @param templateMat
     * @return 
     */
    public static Mat changeMatSize(Mat inputMat, Mat templateMat) {
        Mat resultMat = new Mat();
        Mat transformaceMat;
            Point[] skutecna = new Point[3];
            Point[] cilova = new Point[3];
            skutecna[0] = new Point(0,0);
            skutecna[1] = new Point(0,inputMat.height());
            skutecna[2] = new Point(inputMat.width(), inputMat.height());
            cilova[0] = skutecna[0];
            cilova[1] = new Point(0,templateMat.height());
            cilova[2] = new Point(templateMat.width(), templateMat.height());

        MatOfPoint2f orig = new MatOfPoint2f(skutecna);
        // vypocitame matici transformace
        transformaceMat = Imgproc.getAffineTransform(orig, new MatOfPoint2f(cilova));
        System.out.println("Transformace>\n" + transformaceMat.dump());
        Imgproc.warpAffine(inputMat, resultMat, transformaceMat, inputMat.size());

        return(resultMat.submat(new Rect(cilova[0], cilova[2])));
    }
    
    public static Mat displayToBinaryView(Mat inputMat, Mat maskMat) {
        Mat resultMat1 = new Mat();
        Mat resultMat2 = new Mat();
        
        Core.bitwise_not(inputMat, resultMat1);
        
        Mat matElement = new Mat(3, 3, CvType.CV_8U, Scalar.all(1));
        Imgproc.dilate(resultMat1, resultMat2, matElement);
        
        Imgproc.erode(resultMat2, resultMat1, matElement);
        
        resultMat2 = new Mat(inputMat.width(), inputMat.height(), CvType.CV_8UC1);
        Imgproc.cvtColor(resultMat1, resultMat2, Imgproc.COLOR_RGB2GRAY);

        Imgproc.adaptiveThreshold(resultMat2, resultMat1, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 0);

        resultMat2 = new Mat(inputMat.width(), inputMat.height(), CvType.CV_8U);
        Imgproc.cvtColor(resultMat1, resultMat2, Imgproc.COLOR_GRAY2RGB);

        resultMat1 = pictureMask(resultMat2, maskMat);
        
        matElement = new Mat(2, 2, CvType.CV_8U, Scalar.all(1));
        Imgproc.morphologyEx(resultMat1, resultMat2, Imgproc.MORPH_OPEN, matElement);

        resultMat1 = pictureMask(resultMat2, maskMat);

        return resultMat1;
    }
}
