package opencvtest;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

class CalcCHist {
    public void run(String[] args) {
        int sirka = 240;
        int vyska = 320;

        String filename = args.length > 0 ? args[0] : ("disp3.jpg");
        Mat src = Imgcodecs.imread(filename);
        if (src.empty()) {
            System.err.println("Cannot read image: " + filename);
            System.exit(0);
        }


    Mat horizontal = new Mat(src.rows()+1, src.cols(), CvType.CV_8UC1);//horizontal histogram
	horizontal.setTo(Scalar.all(0));
    Mat vertical = new Mat(1, src.rows(),CvType.CV_8UC1);//horizontal histogram
	vertical.setTo(Scalar.all(0));
    Mat converted = new Mat(src.width(), src.height(), CvType.CV_8UC1);
     Imgproc.cvtColor(src, converted, Imgproc.COLOR_RGB2GRAY);
    System.out.println("Typ "+src.type()+" : "+src.toString());
    int sum;
    Imgproc.threshold(converted, converted, 120, 255, Imgproc.THRESH_BINARY);
//    Imgproc.threshold(converted, converted, 120, 255, Imgproc.THRESH_TOZERO);
	for(int i=0;i<(converted.cols());i++)
	{
        sum = Core.countNonZero(converted.submat(new Rect(i, 0, 1, converted.rows())));
        
        for(int j=0;j<sum;j++) {
            horizontal.put(j, i, 255);
        }
//        System.out.println("hodnota "+i+" : " +sum);
	}

    //! [Display]
        HighGui.imshow( "Origin�l obr�zku", src );
        HighGui.imshow( "konvertovnany obr�zku", converted );
        HighGui.imshow( "Horizontalni histogram", horizontal);
//        HighGui.imshow( "Vertikalni histogram", vertical );
        HighGui.waitKey(0);
        //! [Display]

        System.exit(0);
    }
}

public class CalcColumnHist {
    public static void main(String[] args) {
        // Load the native OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        new CalcCHist().run(args);
    }
}