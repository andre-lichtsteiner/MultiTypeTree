package beast.evolution.tree;

import beast.core.BEASTObject;
import beast.core.CalculationNode;
import beast.core.Function;
import beast.core.Input;
import beast.core.parameter.BooleanParameter;
import beast.core.parameter.RealParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleBinaryOperator;

/**
 * Created by andre on 18/01/17.
 */
public class MatrixSwitch extends CalculationNode implements Function {


    //Must provide the model which is to be compared with the flat model
    public Input<List<RealParameter>> rateMatricesInput = new Input<>(
            "rateMatrix",
            "Migration rate matrices",
            new ArrayList<RealParameter>(), Input.Validate.REQUIRED);

    public Input<BooleanParameter> logTransformInput = new Input<>("logTransform", "Optional parameter, if set to 1, then the values in the rateMatrix will be log transformed before use. If this is not done, then the provided values need to already be log transformed.");

    public Input<List<RealParameter>> rateMatrixScaleFactorsInput = new Input<>("rateMatrixScaleFactor", "Scalers which are applied to the matrices in this switcher.",
            new ArrayList<RealParameter>());

    public Input<BooleanParameter> deltaInput = new Input<>("deltaParameter", "Specify a parameter with two starting values for delta (the indicator variables)", Input.Validate.REQUIRED);

    protected List<RealParameter> rateMatricesTemp;
    protected List<RealParameter> rateMatrixScaleFactors;

    protected ArrayList<ArrayList<Double>> rateMatricesKeep;
    protected boolean needsUpdate;
    protected BooleanParameter deltaParameter;

    public int getDimension(){
        return rateMatricesKeep.get(0).size(); //Returns size of first matrix provided

    }

    public double getArrayValue(){
        return rateMatricesInput.get().size(); //Need to determine what I should return here *TODO-----
    }


    @Override
    public double getArrayValue(int dim) {


        deltaParameter = deltaInput.get();
        int delta = (int) deltaParameter.getArrayValue(0);

        double scaleFactor = 1; //default
        if (rateMatrixScaleFactorsInput.get().size() != 0){
            scaleFactor = rateMatrixScaleFactorsInput.get().get(delta).getValue();
        }

        return (rateMatricesKeep.get(delta).get(dim) * scaleFactor);


    }

    public RealParameter getMatrix(int index){
        System.out.println("GETTING THE WRONG MATRIX HERE FOR NOW"); //Probably fine (mostly) because currently only used for working out if the matrix is square
        return rateMatricesTemp.get(index);
    }

    public void initAndValidate(){
        //Setup initial state of this Function

        rateMatricesTemp = rateMatricesInput.get();
        rateMatricesKeep = new ArrayList<ArrayList<Double>>();
       // lambdaParameter = lambdaInput.get();
        deltaParameter = deltaInput.get();




        ArrayList<Double> matrixElementMeans = new ArrayList<>();
        double sumOfMeans = 0;

        for(int i = 0; i < rateMatricesTemp.size(); i++){
            double subtotal = 0;
            double elementCount = 0;
            //matrixElementMeans.add(0)

            for (int j = 0; j < rateMatricesTemp.get(i).getDimension(); j++){
                subtotal = subtotal + rateMatricesTemp.get(i).getArrayValue(j);
                elementCount = elementCount + 1;
            }

            matrixElementMeans.add(subtotal/elementCount);
            sumOfMeans = sumOfMeans + subtotal/elementCount;
        }

        double meanOfMeans = sumOfMeans / matrixElementMeans.size();



        for (int i = 0; i < rateMatricesTemp.size(); i++) {
            System.out.println("\nTransformed Matrix " + i);
            //For each matrix provided
            ArrayList<Double> current_matrix = new ArrayList<>();

            for (int j = 0; j < rateMatricesTemp.get(i).getDimension(); j++) {
                //For each element in that matrix
                double elementValue = rateMatricesTemp.get(i).getArrayValue(j);
                if (i == 0){
                    elementValue = (elementValue * matrixElementMeans.get(1))/meanOfMeans;

                }
                else if(i == 1){
                    elementValue = (elementValue * matrixElementMeans.get(0))/meanOfMeans;
                }
                else{
                    throw new IllegalArgumentException("Wrong");

                }
                System.out.print(elementValue + " ");
                current_matrix.add(elementValue);
            }
            rateMatricesKeep.add(current_matrix);
        }


        /*

        //Pass through values from rateMatricesTemp to rateMatricesKeep
        for (int i = 0; i < rateMatricesTemp.size(); i++){
            rateMatricesKeep.add(new ArrayList<>());

            for (int j = 0; j < rateMatricesTemp.get(i).getDimension(); j++){
                rateMatricesKeep.get(i).add(rateMatricesTemp.get(i).getArrayValue(j));
            }

        }
        */

        needsUpdate = true;
        if (rateMatrixScaleFactorsInput.get() != null) {
            rateMatrixScaleFactors = rateMatrixScaleFactorsInput.get();
        }

        System.out.println("Successfully initialised LinearModelMatrix.");
    }

    @Override
    public boolean requiresRecalculation(){
       needsUpdate = true;
       return true;
    }

    @Override
    protected void restore() {
        //System.out.println("Called restore"); //Just to see how often this happens, to see how much efficiency this gives us it did happen quite commonly!
        //Double[] tempSwapCachedTotalMatrix = oldCachedTotalMatrix;
        //ArrayList<Double> tempSwapCachedTotalMatrix = oldCachedTotalMatrix;
        //oldCachedTotalMatrix = cachedTotalMatrix;
        //cachedTotalMatrix = tempSwapCachedTotalMatrix;

        //needsUpdate = true; // Can make this more efficient by not throwing away the old version of the calculated array
        super.restore();
    }


    ///Might need to override store() as well, although less clear
    @Override
    protected void store(){
        super.store();
    }


}


/*

    private ArrayList<Double> recalculateMatrix(){

        if (cachedTotalMatrix != null){
            oldCachedTotalMatrix = (ArrayList<Double>) cachedTotalMatrix.clone(); //Save the matrix state before changing
        }

*/
        /*
        //Get the latest values for the delta and lambda parameters, relying on requiresRecalculation to manage when this is done (via needsUpdate)
        //lambdaParameter = lambdaInput.get();
        deltaParameter = deltaInput.get();

        ArrayList<Double> tempList = new ArrayList<>(); //This will hold the calculated values for each matrix temporarily

        for(int elementIndex = 0; elementIndex < rateMatricesKeep.get(0).size(); elementIndex++){
            //For each element in the matrices

            double totalElementValue = 0;

            for(int matrixIndex = 0; matrixIndex < rateMatricesKeep.size(); matrixIndex++){
                //For each component matrix

                double delta = deltaParameter.getArrayValue(matrixIndex);

                if(deltaParameter.getArrayValue(matrixIndex) != 0){
                    double term = delta * rateMatricesKeep.get(matrixIndex).get(elementIndex); //  * lambdaParameter.getArrayValue(matrixIndex)
                            totalElementValue = totalElementValue + term;
                }
            }
            tempList.add(Math.exp(totalElementValue));
        }


        //cachedTotalMatrix = new Double[tempList.size()];
        //cachedTotalMatrix = tempList;
        return tempList;

        */
        /*
        ArrayList<Double> tempList = new ArrayList<>();

        //Get delta to determine which matrix value to return
        deltaParameter = deltaInput.get();
        int delta = (int) deltaParameter.getArrayValue(0);

        for(int elementIndex = 0; elementIndex < rateMatricesKeep.get(0).size(); elementIndex++){
            //For each element in the matrices

            double element = rateMatricesKeep.get(delta).get(elementIndex);
            tempList.add(element);

        }

        return tempList;

    } */




//Need to normalise the rateMatrices provided
        /*
        //Get maximum and min in first matrix
        double mat_0_maximum = 0;
        double mat_0_minimum = 0;
        for (int i = 0; i < rateMatricesTemp.get(0).getDimension(); i++) {
            double curValue = rateMatricesTemp.get(0).getArrayValue(i);
            if (curValue > mat_0_maximum) {
                mat_0_maximum = curValue;
            } else if (curValue < mat_0_minimum) {
                mat_0_minimum = curValue;
            }
        }

        //want all to be positive (I think)
        double addAmount_0;
        if (mat_0_minimum <= 0){ //Also don't want any zeroes?
            addAmount_0 = 0 - mat_0_minimum;
        }
        else{
            addAmount_0 = 0;
        }
        mat_0_maximum = mat_0_maximum + addAmount_0; //Because this amount will be added to the all elements, incl the maximum and min ones, resulting in a higher maximum
        mat_0_minimum = mat_0_minimum + addAmount_0;

        double mat_1_maximum = 0;
        double mat_1_minimum = 0;
        for (int i = 0; i < rateMatricesTemp.get(1).getDimension(); i++) {
            double curValue = rateMatricesTemp.get(1).getArrayValue(i);
            if (curValue > mat_1_maximum) {
                mat_1_maximum = curValue;
            } else if (curValue < mat_1_minimum) {
                mat_1_minimum = curValue;
            }
        }

        double addAmount_1;
        if (mat_1_minimum <= 0){ //Also don't want any zeroes?
            addAmount_1 = 0 - mat_1_minimum ;
        }
        else{
            addAmount_1 = 0;
        }
        mat_1_maximum = mat_1_maximum + addAmount_1; //Because this amount will be added to the all elements, incl the maximum and min ones, resulting in a higher maximum
        mat_1_minimum = mat_1_minimum + addAmount_1;


        double new_scale_max = mat_0_maximum * mat_1_maximum;
        double new_scale_min = new_scale_max / 1000;
        */



           /*
        for (int i = 0; i < rateMatricesTemp.size(); i++) {
            System.out.println("\nTransformed Matrix " + i);
            //For each matrix provided
            ArrayList<Double> current_matrix = new ArrayList<>();

            for (int j = 0; j < rateMatricesTemp.get(i).getDimension(); j++) {
                //For each element in that matrix
                double elementValue = rateMatricesTemp.get(i).getArrayValue(j);
                if (i == 0){
                    elementValue = (addAmount_0 + elementValue) * (mat_1_maximum/mat_0_maximum);
                }
                else if(i == 1){
                    elementValue = (addAmount_1 + elementValue); //We scaled the other matrix to match this one
                }

                System.out.print(elementValue + " ");
                current_matrix.add(elementValue);
            }
            rateMatricesKeep.add(current_matrix);
        }
        */

//Do the things which are usually done for rateMatrix in SCMigrationModel

        /*
        Looks like the purpose of this is to not have any negative rates in the matrix

        for (int i = 0; i < rateMatrices.size(); i++){
            rateMatrices.get(i).setLower(Math.max(rateMatrices.get(i).getLower(), 0.0));
        }
        */

//avgDenom = 0;
// avgNumer = 0;

//have to have some initial value for cachedTotalMatrix..
//cachedTotalMatrix = recalculateMatrix();





        /*
        //Disable the caching
        boolean disableCaching = false;

        if(needsUpdate && ! disableCaching) {

            cachedTotalMatrix = recalculateMatrix();
            needsUpdate = false; //Because now recalculated

        }

        if(!needsUpdate && !disableCaching) {

            /*
            arrayValueCallCount++;
            if (arrayValueCallCount == 100000){
                double curValue = cachedTotalMatrix.get(dim);
                avgNumer = avgNumer + curValue;
                avgDenom++;

               // System.out.println("Average array value: " + (avgNumer/avgDenom));

                arrayValueCallCount = 0;

            }
            */

//  return cachedTotalMatrix.get(dim);// .doubleValue();
// }

    /*
        if (disableCaching){
            //Just calculate the one combined element once
            double totalValue = 0;
            for (int i = 0; i < rateMatricesKeep.size(); i++) { //For each matrix provided
                //double lambda = lambdaInput.get().getArrayValue(i);
                double delta = deltaParameter.getArrayValue(i); //getArrayValue of boolean parameter returns a double

                double term = delta * rateMatricesKeep.get(i).get(dim); // * lambda


                totalValue = totalValue + term;
            }

            return Math.exp(totalValue);

        }
        */


        /*
        numTimes++;
        System.out.println(numTimes);

        if(needsUpdate) {
            //Get the latest values for the delta and lambda parameters, relying on requiresRecalculation to manage when this is done (via needsUpdate)
            System.out.println("Needs update");
            lambdaParameter = lambdaInput.get();
            deltaParameter = deltaInput.get();
            needsUpdate = false; //Because now sorted

        }
        double totalValue = 0;

        for (int i = 0; i < rateMatricesKeep.size(); i++) { //For each matrix provided
            double lambda = lambdaParameter.getArrayValue(i);
            double delta = deltaParameter.getArrayValue(i); //getArrayValue of boolean parameter returns a double

            //Each matrix may have its own scaleFactor
            //double scaleFactor = 1; //Leave this as 1 if no scaleFactor provided
            //if (rateMatricesScaleFactorsInput.get() != null && rateMatricesScaleFactorsInput.get().size() != 0){
            //   scaleFactor = rateMatricesScaleFactorsInput.get().get(i).getValue();
            //}

            double term = lambda * delta * rateMatricesKeep.get(i).get(dim); //  * scaleFactor; //note that more generally we probably want the log transform to be done at a different stage


            totalValue = totalValue + term;
        }

        return Math.exp(totalValue);
        */
//return 101003030; //Just cause we need to return something regardless




/*

        ArrayList<Double> matrixElementMeans = new ArrayList<>();
        double sumOfMeans = 0;

        for(int i = 0; i < rateMatricesTemp.size(); i++){
            double subtotal = 0;
            double elementCount = 0;
            //matrixElementMeans.add(0)

            for (int j = 0; j < rateMatricesTemp.get(i).getDimension(); j++){
                subtotal = subtotal + rateMatricesTemp.get(i).getArrayValue(j);
                elementCount = elementCount + 1;
            }

            matrixElementMeans.add(subtotal/elementCount);
            sumOfMeans = sumOfMeans + subtotal/elementCount;
        }

        double meanOfMeans = sumOfMeans / matrixElementMeans.size();



        for (int i = 0; i < rateMatricesTemp.size(); i++) {
            System.out.println("\nTransformed Matrix " + i);
            //For each matrix provided
            ArrayList<Double> current_matrix = new ArrayList<>();

            for (int j = 0; j < rateMatricesTemp.get(i).getDimension(); j++) {
                //For each element in that matrix
                double elementValue = rateMatricesTemp.get(i).getArrayValue(j);
                if (i == 0){
                    elementValue = (elementValue * matrixElementMeans.get(1))/meanOfMeans;

                }
                else if(i == 1){
                    elementValue = (elementValue * matrixElementMeans.get(0))/meanOfMeans;
                }
                else{
                    throw new IllegalArgumentException("Wrong");

                }
                System.out.print(elementValue + " ");
                current_matrix.add(elementValue);
            }
            rateMatricesKeep.add(current_matrix);
        }

        */