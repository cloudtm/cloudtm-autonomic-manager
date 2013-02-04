
public class CubistJNI {

  public native void initiateCubist(String filename);

  public native double getPrediction(String filename);

  public native double[] getPredictionAndError(String filename);

  public static void main(String[] args) {
    System.loadLibrary("cubistJNI");

    CubistJNI cubistJNI = new CubistJNI();

    cubistJNI.initiateCubist("tasRtt");
    String blah = "5343.0,15.0,2.0,3.0,284.29537210374576,598.1268435758845,3095.0,1604348.5,0.3893095563468767,5.990125069846153E9";
    double pred = cubistJNI.getPrediction(blah);
    System.out.println("Prediction: " + pred);
    double pred2 = cubistJNI.getPrediction(blah);
    System.out.println("Prediction2:" + pred2);
    double[] predError = cubistJNI.getPredictionAndError(blah);

    System.out.println("Prediction: " +predError[0] + " Error: " + predError[1]);


  }

}
