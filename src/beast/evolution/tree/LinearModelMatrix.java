package beast.evolution.tree;

import beast.core.*;
import beast.core.parameter.BooleanParameter;
import beast.core.parameter.RealParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleBinaryOperator;

/**
 * Created by andre on 18/01/17.
 *
 * TODO - occasionally on startup there is a StackOverflowError:
 *
         * Exception in thread "main" java.lang.StackOverflowError
         at beast.evolution.tree.Node.getLeft(Node.java:730)
         at beast.evolution.tree.Node.toSortedNewick(Node.java:377)
         at beast.evolution.tree.Node.toSortedNewick(Node.java:379)
         at beast.evolution.tree.Node.toSortedNewick(Node.java:379)
 *
 */
public class LinearModelMatrix extends CalculationNode implements Function {


    //Must provide the model which is to be compared with the flat model
    public Input<RealParameter> rateMatrixInput = new Input<>(
            "rateMatrix",
            "Migration rate matrices");

    public Input<RealParameter> switchInput = new Input<>("switch", "A boolean parameter to determine whether or not the provided rateMatrix is included in the calculation at a particular point.");

    public Input<RealParameter> scaleFactorInput = new Input<>("scaleFactor", "Scale factor parameter which will be applied to values in the rateMatrix.");

    protected double[] rateMatrix;

    protected double[] cachedMatrix;
    protected double[] oldCachedMatrix;

    protected double switchValue;
    protected double scaleFactorValue;

    protected double oldSwitchValue;
    protected double oldScaleFactorValue;



    // protected ArrayList<Double> cachedTotalMatrix;
    // protected ArrayList<Double> oldCachedTotalMatrix;

    //protected ArrayList<ArrayList<Double>> rateMatricesKeep;
    protected boolean needsUpdate;
    //protected RealParameter lambdaParameter;
    //protected BooleanParameter deltaParameter;

    public int getDimension(){
        return rateMatrix.length; //Returns number of elements in provided matrix

    }

    public Input<RealParameter> getSwitchInput(){

        return switchInput;
    }

    public double getArrayValue(){
        return rateMatrix.length; //Need to determine what I should return here *TODO-----
    }

    /*
    private ArrayList<Double> recalculateMatrix(){

        if (cachedTotalMatrix != null){
            oldCachedTotalMatrix = (ArrayList<Double>) cachedTotalMatrix.clone(); //Save the matrix state before changing
        }


        ArrayList<Double> tempList = new ArrayList<>(); //This will hold the calculated values for each matrix temporarily

        for(int elementIndex = 0; elementIndex < rateMatricesKeep.get(0).size(); elementIndex++){
            //For each element in the matrices

            double totalElementValue = 0;

            for(int matrixIndex = 0; matrixIndex < rateMatricesKeep.size(); matrixIndex++){
                //For each component matrix

                double delta = deltaParameter.getArrayValue(matrixIndex);

                if(deltaParameter.getArrayValue(matrixIndex) != 0){
                    double term = lambdaParameter.getArrayValue(matrixIndex) * delta * rateMatricesKeep.get(matrixIndex).get(elementIndex);
                    totalElementValue = totalElementValue + term;
                }
            }
            tempList.add(Math.exp(totalElementValue));
        }


        //cachedTotalMatrix = new Double[tempList.size()];
        //cachedTotalMatrix = tempList;
        return tempList;
    }
    */

    @Override
    public double getArrayValue(int dim) {

        if (needsUpdate){
            switchValue = switchInput.get().getArrayValue(0);
            scaleFactorValue = scaleFactorInput.get().getValue();

            if (cachedMatrix != null){
                oldCachedMatrix = cachedMatrix.clone();
            }

            cachedMatrix = new double[rateMatrix.length];

            for (int i = 0; i < rateMatrix.length; i++){
                cachedMatrix[i] = scaleFactorValue * Math.pow(rateMatrix[i], switchValue);
            }

            needsUpdate = false;
        }

       // double switchValue = switchInput.get().getArrayValue(0);
       // double scaleFactorValue = scaleFactorInput.get().getValue();

        return cachedMatrix[dim];
        //return scaleFactorValue * Math.pow(rateMatrix[dim], switchValue);
        /*
        if (switchValue == 0){
            return scaleFactorValue;
        }
        else{
            return rateMatrix[dim] * scaleFactorValue;
        }
        */


        /*
        //Disable the caching
        boolean disableCaching = false;

        if(needsUpdate && ! disableCaching) {

            cachedTotalMatrix = recalculateMatrix();
            needsUpdate = false; //Because now recalculated

        }

        if(!needsUpdate && !disableCaching) {



           return cachedTotalMatrix.get(dim);// .doubleValue();
        }

        if (disableCaching){
            //Just calculate the one combined element once
            double totalValue = 0;
            for (int i = 0; i < rateMatricesKeep.size(); i++) { //For each matrix provided
                double lambda = lambdaInput.get().getArrayValue(i);
                double delta = deltaParameter.getArrayValue(i); //getArrayValue of boolean parameter returns a double

                double term = lambda * delta * rateMatricesKeep.get(i).get(dim);


                totalValue = totalValue + term;
            }

            return Math.exp(totalValue);

        }
        */


    }

    public RealParameter getMatrix(int index){
        System.out.println("NOT SURE IF THIS IS WORKING?"); //Probably fine (mostly) because currently only used for working out if the matrix is square

        Double[] tempArray = new Double[rateMatrix.length];
        for (int i = 0; i < rateMatrix.length; i++){
            tempArray[i] = rateMatrix[i];
        }

        return new RealParameter(tempArray);

    }

    public void initAndValidate(){
        //Setup initial state of this Function

        RealParameter rateMatrixStart = rateMatrixInput.get();

        //Now transform the values in the matrix to be centred around 1
        //First get the mean of the values
        double startTotal = 0;
        for (int i = 0; i < rateMatrixStart.getDimension(); i++){
            startTotal = startTotal + rateMatrixStart.getArrayValue(i);
        }
        double startMean = startTotal / rateMatrixStart.getDimension();

        double multiplier = 1 / startMean;

        //Store the re-centred rateMatrix and print it out
        System.out.println("Re-centred rate matrix:");
        rateMatrix = new double[rateMatrixStart.getDimension()]; //Make new array
        for (int i = 0; i < rateMatrixStart.getDimension(); i++){
            rateMatrix[i] = Math.max(0.0, rateMatrixStart.getArrayValue(i) * multiplier); //Multiply each rateMatrix value by the multiplier (and make sure that the value is never negative because that doesn't work)
            System.out.print(rateMatrix[i] + " ");
        }
        System.out.println("");

        needsUpdate = true;
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

        scaleFactorValue = oldScaleFactorValue;
        switchValue = oldSwitchValue;

        if (oldCachedMatrix != null) {
            double[] swapMat = cachedMatrix;
            cachedMatrix = oldCachedMatrix;
            oldCachedMatrix = swapMat;
        }
        else{
            needsUpdate = true; //Because otherwise wrong
        }


       // ArrayList<Double> tempSwapCachedTotalMatrix = oldCachedTotalMatrix;
       // oldCachedTotalMatrix = cachedTotalMatrix;
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
