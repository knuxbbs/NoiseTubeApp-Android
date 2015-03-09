package net.noisetube.api.audio.calibration;

public class Corrector {

    //STATICS------------------------------------------------------------------
    public final static int INPUT_IDX = 0;  //phone
    public final static int OUTPUT_IDX = 1; //ref


    //DYNAMICS-----------------------------------------------------------------
    private double[][] dBACalibrationArray;
    private double[][] dBCalibrationArray; //needed?


    public Corrector(double[][] dBACalibrationArray /*, double[][] dBCalibrationArray*/) //TODO use separate values for DB correction?
    {
        this.dBACalibrationArray = dBACalibrationArray;
        this.dBCalibrationArray = dBACalibrationArray; //use same values for now
    }

    public double correctDB(double spl) {
        return correctSPL(spl, dBACalibrationArray);
    }

    public double correctDBA(double spl) {
        return correctSPL(spl, dBACalibrationArray);
    }

    /**
     * Corrects the given SPL value by means of linear interpolation
     * on the line through the two most fitting calibration points
     * <p/>
     * Math reminder:
     * The linear interpolation of a point (x,y), with known value x, on a straight line
     * through points (x0, y0) and (x1, y1), is calculated as:
     * y = ((x - x0) * ((y1 - y0) / (x1 - x0))) + y0;
     * In which (y1 - y0) / (x1 - x0) is the slope of the line.
     * Full explanation: http://en.wikipedia.org/wiki/Linear_interpolation
     *
     * @param calibrationArray
     * @param spl
     * @return corrected spl
     */
    private double correctSPL(double spl, double[][] calibrationArray) {
        int i = 0;
        while (i < calibrationArray.length && spl > calibrationArray[i][INPUT_IDX])
            i++;
        if (i == calibrationArray.length)
            i--; //use last two calibration points
        double x0, y0, x1, y1; //INPUT -> x; OUTPUT -> y
        if (i == 0) {    //interpolate between the origin (0.0; 0.0) and the first (0th) calibration point
            x0 = 0.0d;
            y0 = 0.0d;
        } else {    //interpolate between the (i-1)th and (i)th calibration point
            x0 = calibrationArray[i - 1][INPUT_IDX];
            y0 = calibrationArray[i - 1][OUTPUT_IDX];
        }
        x1 = calibrationArray[i][INPUT_IDX];
        y1 = calibrationArray[i][OUTPUT_IDX];
        //x = spl; y = return value (i.e. the corrected spl)
        return ((spl - x0) * ((y1 - y0) / (x1 - x0))) + y0;
    }

    /**
     * @return the dBACalibrationArray
     */
    public double[][] getdBACalibrationArray() {
        return dBACalibrationArray;
    }

    /**
     * @return the dBCalibrationArray
     */
    public double[][] getdBCalibrationArray() {
        return dBCalibrationArray;
    }

    public String toString() {
        StringBuffer bff = new StringBuffer();
        bff.append("dB(A) correction values: [");
        for (int i = 0; i < dBACalibrationArray.length; i++)
            bff.append((i == 0 ? "" : "; ") + "(" + dBACalibrationArray[i][INPUT_IDX] + ", " + dBACalibrationArray[i][OUTPUT_IDX] + ")");
        return bff.toString();
    }

}

