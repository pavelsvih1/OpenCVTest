/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package viewtester;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
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

        return (result);
    }

    /**
     * Vypocita perspektivni matici dle vstupnich bodu a vystupnich bodu a
     * orizne matici podle vystupnich bodu
     *
     * @param inputMat vstupni obraz
     * @param resultOutputMat vystupni matice - vysledek
     * @param inputPoints
     * @param outputPoints
     * @return transformacni matice perspektivy
     */
    public static Mat perspectiveMatAndCut(Mat inputMat, Mat resultOutputMat, Point[] inputPoints, Point[] outputPoints) {
        Mat transformace;
        Mat outputMat = new Mat();

        // nejprve si spocitame transformacni matici z vypozorovanych hodnot
        MatOfPoint2f orig = new MatOfPoint2f(inputPoints);
        // vypocitame matici transformace
        transformace = Imgproc.getPerspectiveTransform(orig, new MatOfPoint2f(outputPoints));
        System.out.println("Transformace>\n" + transformace.dump());
        Imgproc.warpPerspective(inputMat, outputMat, transformace, inputMat.size());
        (outputMat.submat(new Rect(outputPoints[0], outputPoints[3]))).assignTo(resultOutputMat);
        return (transformace);
    }

    /**
     * Najde oreze displej o stinovy ramecek.
     * @param inputMat obrazek displeje orezany a pravouhly - prvni orez 
     * @return 4 rohy displeje ve vstupnim obraze
     */
    public static Point[] findDisplayCorners(Mat inputMat) {
        Mat resultMat = new Mat();
        Point [] quatrop;
        
        // --> CB
        Mat preMat = new Mat(inputMat.width(), inputMat.height(), CvType.CV_8UC1);
        Imgproc.cvtColor(inputMat, preMat, Imgproc.COLOR_RGB2GRAY);

        // --> Invertovat barvy
        Core.bitwise_not(preMat, resultMat);

        // --> Prahovat - adaptivni MeanC/11
        Imgproc.adaptiveThreshold(resultMat, preMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 11, 0);

        // --> Invertovat barvy
        Core.bitwise_not(preMat, resultMat);
        
        // --> Otevrit 5x5
        Mat matElement = new Mat(5, 5, CvType.CV_8U, Scalar.all(1));
        Imgproc.morphologyEx(resultMat, preMat, Imgproc.MORPH_OPEN, matElement);

        // --> Zavrit 
        Imgproc.morphologyEx(preMat, resultMat, Imgproc.MORPH_CLOSE, matElement);

        // --> Extrahuj obdelnikove obrysy
        quatrop = getQuatroHullPoints(resultMat, true);

        return quatrop;
    }

    /**
     * Vrati obrysy nejvetsiho prvku v obrazku dane 4-mi body
     * @param inputMat
     * @param orthogonal zda maji byt obrysy pravouhle 
     * @return 
     */
    public static Point[] getQuatroHullPoints(Mat inputMat, boolean orthogonal) {
        Mat hierarchy = new Mat();
        Point[] quatrop;
//            Mat maska;
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(inputMat, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);
//            outputMat = Mat.zeros(preMat.size(), CvType.CV_8UC3);
//            for (int i = 0; i < contours.size(); i++) {
//                Scalar color = new Scalar(50, 0, 255);
//                Imgproc.drawContours(outputMat, contours, i, color, 2, Imgproc.LINE_8, hierarchy, 0, new Point());
//            }

//            maska = Mat.zeros(preMat.size(), CvType.CV_8UC3);
//            Imgproc.drawContours(maska, contours,-1, new Scalar(255), Imgproc.FILLED);
//            // vypln vseho spojeneho
//            for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
//                Imgproc.drawContours(maska, contours, contourIdx, new Scalar(0, 0, 255), Imgproc.FILLED);
//            }
        double maxArea = 0;
        int iMaxContour = 0;
        // nalezneme nejvetsi obklicenou plochu
        for (int ci = 0; ci < contours.size(); ci++) {
            MatOfPoint contour = contours.get(ci);
            double contourArea = Imgproc.contourArea(contour);
//                Rect boundingBox = Imgproc.boundingRect(contour);
//                double aspectRatio = (double) boundingBox.width / boundingBox.height;
//                double extent = contourArea / (boundingBox.width * boundingBox.height);
//                if (contourArea > maxArea && contourArea > 200 && aspectRatio > this.aspectMin && aspectRatio < this.aspectMax && extent > this.extentMin && extent < this.extentMax) {
            if (contourArea > maxArea) {
                maxArea = contourArea;
                iMaxContour = ci;
            }
        }
//            Imgproc.drawContours(maska, contours, iMaxContour, new Scalar(0, 0, 255), Imgproc.FILLED);
//            System.out.println("nalezeno>\n"+contours.get(iMaxContour).dump());

        MatOfPoint contour = contours.get(iMaxContour);
        if (!orthogonal) {
            List<MatOfPoint> hullList = new ArrayList<>();
            MatOfInt hull = new MatOfInt();
            Imgproc.convexHull(contour, hull);
            Point[] contourArray = contour.toArray();
            Point[] hullPoints = new Point[hull.rows()];
            List<Integer> hullContourIdxList = hull.toList();
            double dx, dy;
            for (int i = 0; i < hullContourIdxList.size(); i++) {
                hullPoints[i] = contourArray[hullContourIdxList.get(i)];
                System.out.print("Bod " + i + " : " + hullPoints[i].toString());
                if (i > 0) {
                    dx = hullPoints[i].x - hullPoints[i - 1].x;
                    dy = hullPoints[i].y - hullPoints[i - 1].y;
                    System.out.print("  Rozdil: " + dx + "," + dy);
                }
                System.out.println();
            }
            hullList.add(new MatOfPoint(hullPoints));
            quatrop = quadrilateralHull(hullPoints, 5);
            hullList.add(new MatOfPoint(quatrop));
            quatrop = sortPoints(quatrop);
        } else {
            quatrop = new Point[4];
            RotatedRect minRect = Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray()));
            minRect.points(quatrop);
//            for (int j = 0; j < 4; j++) {
//                Imgproc.line(inputMat, rectPoints[j], rectPoints[(j + 1) % 4], new Scalar(255, 0, 0));
//            }
            quatrop = sortPoints(quatrop);
        }
        return (quatrop);
    }

    /**
     * transformuje vstupni matici na velikost vzorove
     *
     * @param inputMat
     * @param templateMat
     * @return
     */
    public static Mat changeMatSize(Mat inputMat, Mat templateMat) {
        Mat resultMat = new Mat();
        Mat transformaceMat;
        Point[] skutecna = new Point[3];
        Point[] cilova = new Point[3];
        skutecna[0] = new Point(0, 0);
        skutecna[1] = new Point(0, inputMat.height());
        skutecna[2] = new Point(inputMat.width(), inputMat.height());
        cilova[0] = skutecna[0];
        cilova[1] = new Point(0, templateMat.height());
        cilova[2] = new Point(templateMat.width(), templateMat.height());

        MatOfPoint2f orig = new MatOfPoint2f(skutecna);
        // vypocitame matici transformace
        transformaceMat = Imgproc.getAffineTransform(orig, new MatOfPoint2f(cilova));
        System.out.println("Transformace>\n" + transformaceMat.dump());
        Imgproc.warpAffine(inputMat, resultMat, transformaceMat, inputMat.size());

        return (resultMat.submat(new Rect(cilova[0], cilova[2])));
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

//        resultMat2 = new Mat(inputMat.width(), inputMat.height(), CvType.CV_8U);
//        Imgproc.cvtColor(resultMat1, resultMat2, Imgproc.COLOR_GRAY2RGB);
        resultMat2 = resultMat1;

        resultMat1 = pictureMask(resultMat2, maskMat);

        matElement = new Mat(2, 2, CvType.CV_8U, Scalar.all(1));
        Imgproc.morphologyEx(resultMat1, resultMat2, Imgproc.MORPH_OPEN, matElement);

        resultMat1 = pictureMask(resultMat2, maskMat);

        return resultMat1;
    }

    /**
     * Vrati obrys dany 4-mi rohovymi body. Primarne urceno k odstraneni napr.
     * zaoblenych rohu
     *
     * @param hullPoints obrys, zadany minimalne 8-mi body (4 usecky)
     * @param minLen nejmensi delka dvou usecek na sebe navazujicich tvorenych
     * tremi body urcena k jejich spojeni
     * @return
     */
    public static Point[] quadrilateralHull(Point[] hullPoints, int minLen) {

        if (hullPoints.length <= 8) {
            return (hullPoints);
        }

        List<Point> pointList = Arrays.asList(hullPoints);
        List<Point> computedList = computeDeltaPoints(pointList);
        List<Point> retPointList = new ArrayList<>();
        Point pi, pj, pk;
        boolean dx, dj;

        for (int i = 1; i < computedList.size(); i++) {
            pi = computedList.get(i);
            pj = computedList.get(i - 1);
            dx = (Math.abs(pi.x) > Math.abs(pi.y));
            dj = (Math.abs(pj.x) > Math.abs(pj.y));

            if (sameSign(pi.x, pj.x) && sameSign(pi.y, pj.y) && (dx == dj)) {
                if (dx && (Math.abs(pi.x) > minLen) && (Math.abs(pj.x) > minLen)) {
                    // nezapisujem i-1
                } else if ((!dx) && (Math.abs(pi.y) > minLen) && (Math.abs(pj.y) > minLen)) {
                    // nezapisujem i-1
                } else {
                    retPointList.add(hullPoints[i - 1]);
                }
            } else {
                retPointList.add(hullPoints[i - 1]);
            }
        }
        pi = computedList.get(computedList.size() - 1);
        pj = computedList.get(0);
        dx = (Math.abs(pi.x) > Math.abs(pi.y));
        dj = (Math.abs(pj.x) > Math.abs(pj.y));

        if (sameSign(pi.x, pj.x) && sameSign(pi.y, pj.y) && (dx == dj)) {
            if (dx && (Math.abs(pi.x) > minLen) && (Math.abs(pj.x) > minLen)) {
                // nezapisujem i-1
            } else if ((!dx) && (Math.abs(pi.y) > minLen) && (Math.abs(pj.y) > minLen)) {
                // nezapisujem i-1
            } else {
                retPointList.add(hullPoints[computedList.size() - 1]);
            }
        }

        // ted ponechame jen 4 nejvetsi dvojice bodu - 2 v X, 2 v Y
        pointList = retPointList;
        int[] nej = {0, 1, 2, 3};    // 0-nejX, 1-2.nejX, 2-nejY, 3-2.nejY ; zacit musim s ruznymi hodnotami

        computedList = computeDeltaPoints(pointList);   // prepocitame
        printListPoints(computedList, pointList);
        // vybereme dvojice
        for (int i = 0; i < computedList.size(); i++) {
            // nejprve porovname delta X
            pi = computedList.get(nej[1]);
            pk = computedList.get(nej[0]);
            pj = computedList.get(i);
            if (Math.abs(pj.x) > Math.abs(pi.x)) {
                if (Math.abs(pj.x) > Math.abs(pk.x)) {
                    nej[1] = nej[0];
                    nej[0] = i;
                } else {
                    nej[1] = i;
                }
            }
            // ted porovname delta Y
            pi = computedList.get(nej[3]);
            pk = computedList.get(nej[2]);
            if (Math.abs(pj.y) > Math.abs(pi.y)) {
                if (Math.abs(pj.y) > Math.abs(pk.y)) {
                    nej[3] = nej[2];
                    nej[2] = i;
                } else {
                    nej[3] = i;
                }
            }
        }
        // seradime indexy dle poradi v puvodnim listu
        Arrays.sort(nej);
        // a vlozime prvky pro navrat
        retPointList = new ArrayList<>();
        int index;
        for (int i = 0; i < 4; i++) {
            index = (nej[i] - 1);
            if (index < 0) {
                index += pointList.size();
            }
            retPointList.add(pointList.get(index));
            retPointList.add(pointList.get(nej[i]));
        }

        // jeste debug vystup
        computedList = computeDeltaPoints(retPointList);   // prepocitame
        printListPoints(computedList, retPointList);

        pointList = new ArrayList<>();
        pointList.add(getCrossPoint(retPointList.get(0), retPointList.get(1), retPointList.get(3), retPointList.get(2)));
        pointList.add(getCrossPoint(retPointList.get(2), retPointList.get(3), retPointList.get(5), retPointList.get(4)));
        pointList.add(getCrossPoint(retPointList.get(4), retPointList.get(5), retPointList.get(7), retPointList.get(6)));
        pointList.add(getCrossPoint(retPointList.get(6), retPointList.get(7), retPointList.get(1), retPointList.get(0)));

        // jeste debug vystup
        computedList = computeDeltaPoints(pointList);   // prepocitame
        printListPoints(computedList, pointList);
        return (pointList.toArray(new Point[pointList.size()]));
    }

    /**
     * Vrati prusecik primek danych body AB a CD
     *
     * @param pa
     * @param pb
     * @param pc
     * @param pd
     * @return
     */
    private static Point getCrossPoint(Point pa, Point pb, Point pc, Point pd) {
        double bax, bay, bal = 0;
        double dcx, dcy, dck = 0;
        double sx, sy = 0;
        boolean mameY = false;
        boolean nemameK = false;
        boolean nemameL = false;

        bax = pb.x - pa.x;
        bay = pb.y - pa.y;  // osetrit na nulu!

        if (bay == 0) {
            // rovnice primky je tedy Y=pa.y
            sy = pa.y;
            mameY = true;
            nemameL = true;
        } else {
            bal = (-1) * bax / bay;
        }

        dcx = pd.x - pc.x;
        dcy = pd.y - pc.y;

        if (dcy == 0) {
            sy = pd.y;
            mameY = true;
            nemameK = true;
        } else {
            dck = (-1) * dcx / dcy; //!
        }

        if (!mameY) {
            sy = pa.x + pa.y * bal - pc.x - pc.y * dck;
            sx = bal - dck;     // nesmi byt 0
            sy = sy / sx;   // souradnice Y
        }

        if (!nemameK) {
            sx = (pc.x + pc.y * dck) - dck * sy;
        } else if (!nemameL) {
            sx = (pa.x + pa.y * bal) - bal * sy;
        } else {
            // rovnobezky
            return (null);
        }
        // nakonec jeste zaokrouhlime na cela cisla
        sx = Math.round(sx);
        sy = Math.round(sy);

        return (new Point(sx, sy));
    }

    /**
     * Srovna rohy pro poradi vhodne k orezani
     *
     * @param qpoints
     * @return
     */
    public static Point[] sortPoints(Point[] qpoints) {
        int prvni = 0;
        int i, testovanyL, testovanyH, hodnotaL, hodnotaH;
        if (qpoints.length != 4) {
            return (null);
        }
        Point[] retPoints = new Point[4];

        //nejprve si nalezneme nejvyssi vrchol
        for (i = 0; i < 4; i++) {
            if (qpoints[i].y < qpoints[prvni].y) {
                prvni = i;
            }
        }

        //podle vetsi delky sousednich bodu se rozhodneme, kde je zacatek
        testovanyL = prvni - 1;
        if (testovanyL < 0) {
            testovanyL = 3;
        }
        testovanyH = prvni + 1;
        if (testovanyH > 3) {
            testovanyH = 0;
        }
        hodnotaH = (int) (qpoints[testovanyH].x - qpoints[prvni].x);
        hodnotaL = (int) (qpoints[prvni].x - qpoints[testovanyL].x);
        if (hodnotaL > hodnotaH) {
            prvni = testovanyL; // zamenime pocatek
        }

        //a vlastni srovnani
        retPoints[0] = qpoints[prvni];
        retPoints[1] = qpoints[(prvni + 1) % 4];
        retPoints[2] = qpoints[(prvni + 3) % 4];
        retPoints[3] = qpoints[(prvni + 2) % 4];

        return (retPoints);
    }

    /**
     * Vytiskne debugovaci vystup listu
     *
     * @param computedList
     * @param pointList
     */
    private static void printListPoints(List<Point> computedList, List<Point> pointList) {
        Point pi;
        Point pj;
        for (int i = 0; i < computedList.size(); i++) { // debug tisk
            pi = pointList.get(i);
            pj = computedList.get(i);
            System.out.print("Bod " + i + " : [" + pi.x + "," + pi.y);
            System.out.print("]  Rozdil: " + pj.x + "," + pj.y);

            System.out.println();
        }
    }

    /**
     * Pokud ma cislo stejne znamenko, vrati true. 0 je povazovana take jako
     * stejne znamenko pro druhou hodnotu kladnou, nulovou i zapornou.
     *
     * @param a
     * @param b
     * @return
     */
    private static boolean sameSign(int a, int b) {
        int sigA = Integer.signum(a);
        int sigB = Integer.signum(b);
        if ((sigA == 0) || (sigB == 0)) {
            return (true);
        }
        if (sigA == sigB) {
            return (true);
        }
        return (false);
    }

    /**
     * Pokud ma cislo stejne znamenko, vrati true. 0 je povazovana take jako
     * stejne znamenko pro druhou hodnotu kladnou, nulovou i zapornou.
     *
     * @param y
     * @param y0
     * @return
     */
    private static boolean sameSign(double y, double y0) {
        return (sameSign((int) y, (int) y0));
    }

    /**
     * Spocita hodnoty rozdilu mezi body
     *
     * @param pointList
     * @return
     */
    private static List<Point> computeDeltaPoints(List<Point> pointList) {
        double dx, dy;
        List<Point> computedList = new ArrayList<>();

        dx = pointList.get(0).x - pointList.get(pointList.size() - 1).x;
        dy = pointList.get(0).y - pointList.get(pointList.size() - 1).y;
        Point delta = new Point(dx, dy);
        computedList.add(0, delta);
        for (int i = 1; i < pointList.size(); i++) {
            dx = pointList.get(i).x - pointList.get(i - 1).x;
            dy = pointList.get(i).y - pointList.get(i - 1).y;
            delta = new Point(dx, dy);
            computedList.add(i, delta);
        }

        return (computedList);
    }

}
