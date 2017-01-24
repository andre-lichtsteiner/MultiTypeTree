package beast.evolution.tree;

import beast.core.BEASTObject;
import beast.core.CalculationNode;
import beast.core.Function;
import beast.core.Input;
import beast.core.parameter.BooleanParameter;
import beast.core.parameter.RealParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andre on 18/01/17.
 */
public class LinearModelMatrix extends CalculationNode implements Function {


    //Must provide the model which is to be compared with the flat model
    public Input<List<RealParameter>> rateMatricesInput = new Input<>(
            "rateMatrix",
            "Migration rate matrices",
            new ArrayList<RealParameter>(), Input.Validate.REQUIRED);

    public Input<BooleanParameter> logTransformInput = new Input<>("logTransform", "Optional parameter, if set to 1, then the values in the rateMatrix will be log transformed before use. If this is not done, then the provided values need to already be log transformed.");


    /*
    public Input<List<RealParameter>> rateMatricesScaleFactorsInput = new Input<>("rateMatrixScaleFactor",
            "Optional number by which all items in the migration matrix will be evenly multiplied.", new ArrayList<RealParameter>());
    //List of rateMatrix scale factors
    */


    public Input<RealParameter> lambdaInput = new Input<>("lambdaParameter", "Specify a parameter with two starting values for lambda (the coefficient)", Input.Validate.REQUIRED);

    public Input<BooleanParameter> deltaInput = new Input<>("deltaParameter", "Specify a parameter with two starting values for delta (the indicator variables)", Input.Validate.REQUIRED);

    protected int numTimes;
    protected List<RealParameter> rateMatricesTemp;
    protected Double[] cachedTotalMatrix;
    protected ArrayList<ArrayList<Double>> rateMatricesKeep;
    protected boolean needsUpdate;
    //protected List<RealParameter> rateMatricesScaleFactors;
    protected RealParameter lambdaParameter;
    protected RealParameter oldLambdaParameter;
    protected BooleanParameter deltaParameter;
    //protected String calcTiming;
    //protected int calcTimeCount;

    public int getDimension(){
        return rateMatricesInput.get().size(); //Returns number of matrices provided

    }

    public double getArrayValue(){
        return rateMatricesInput.get().size(); //Need to determine what I should return here *TODO-----
    }


    @Override
    public double getArrayValue(int dim) {
        //New setup with caching
       // System.out.println("Get Array Value");
        if(needsUpdate) {
           // System.out.println(" -+-+-+-+--+-+-+-+-+-+-+-+-+-+-+-+-+   Had to do maths  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-");
            //Get the latest values for the delta and lambda parameters, relying on requiresRecalculation to manage when this is done (via needsUpdate)
            lambdaParameter = lambdaInput.get();
            deltaParameter = deltaInput.get();
            needsUpdate = false; //Because now updated

            ArrayList<Double> tempList = new ArrayList<>();

            for(int elementIndex = 0; elementIndex < rateMatricesKeep.get(0).size(); elementIndex++){

                double totalElementValue = 0;

                for(int matrixIndex = 0; matrixIndex < rateMatricesKeep.size(); matrixIndex++){

                    double delta = deltaParameter.getArrayValue(matrixIndex);

                    if(deltaParameter.getArrayValue(matrixIndex) != 0){
                        double term = lambdaParameter.getArrayValue(matrixIndex) * delta * rateMatricesKeep.get(matrixIndex).get(dim); //  * scaleFactor; //note that more generally we probably want the log transform to be done at a different stage
                        totalElementValue = totalElementValue + term;
                    }
                }
                tempList.add(Math.exp(totalElementValue));
            }
            cachedTotalMatrix = new Double[tempList.size()];
           tempList.toArray(cachedTotalMatrix);


        }
        return cachedTotalMatrix[dim].doubleValue();


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
    }

    public RealParameter getMatrix(int index){
        System.out.println("GETTING THE WRONG MATRIX HERE FOR NOW"); //Probably fine (mostly) because currently only used for working out if the matrix is square
        return rateMatricesTemp.get(index);
    }

    public void initAndValidate(){
        //Setup initial state of this Function

        rateMatricesTemp = rateMatricesInput.get();
        rateMatricesKeep = new ArrayList<ArrayList<Double>>();
        lambdaParameter = lambdaInput.get();
        deltaParameter = deltaInput.get();
        //rateMatricesScaleFactors = rateMatricesScaleFactorsInput.get();

        //Check that the XML file provides one (and only one) value for both the lambda and delta parameters for each of the matrices provided. If not, cannot proceed.
        if (lambdaInput.get().getDimension() != rateMatricesInput.get().size() || deltaInput.get().getDimension() != rateMatricesInput.get().size()){
            System.out.println("You must provide the LinearModelMatrix with one value for both the lambdaParameter and the deltaParameter for each of the rate matrices you provide.");
            throw new IndexOutOfBoundsException("Wrong number of values provided to lambdaParameter or deltaParameter for LinearModelMatrix");
        }

        if(logTransformInput.get() != null){
            //This has been set in the XML by the user
            if(logTransformInput.get().getDimension() != rateMatricesInput.get().size()){
                System.out.println("You must provide the LinearModelMatrix with one value for logTransform each of the rate matrices you provide.");
                throw new IndexOutOfBoundsException("Wrong number of values provided to logTransform for LinearModelMatrix");
            }
            else{
                for (int i = 0; i < logTransformInput.get().getDimension(); i++){
                    //For each matrix
                    ArrayList<Double> current_matrix = new ArrayList<Double>();

                    if(logTransformInput.get().getArrayValue(i) == 1){
                        //If told to do an initial log transform
                        for (int j = 0; j < rateMatricesTemp.get(i).getDimension(); j++){
                            current_matrix.add(Math.log(rateMatricesTemp.get(i).getArrayValue(j)));
                        }
                    }
                    else{
                        //If NOT told to do an initial log transform
                        for (int j = 0; j < rateMatricesTemp.get(i).getDimension(); j++){
                            current_matrix.add(rateMatricesTemp.get(i).getArrayValue(j));
                        }
                    }


                    rateMatricesKeep.add(current_matrix);


                }
            }
        }
        else{

            for (int i = 0; i < rateMatricesTemp.size(); i++) {
                ArrayList<Double> current_matrix = new ArrayList<Double>();
                for (int j = 0; j < rateMatricesTemp.get(i).getDimension(); j++) {
                    current_matrix.add(rateMatricesTemp.get(i).getArrayValue(j));
                }
            }
        }

        /*
        //Check that if there are any rateMatricesScaleFactors provided, that there are the right number of them
        if(rateMatricesScaleFactorsInput.get() != null && rateMatricesScaleFactorsInput.get().size() != 0 && rateMatricesScaleFactorsInput.get().size() != rateMatricesInput.get().size()){
            System.out.println("If you are using scaleFactors for a rateMatrix, you must provide the LinearModelMatrix with a rateMatrixScaleFactor for each of the rate matrices you provide.");
            throw new IndexOutOfBoundsException("Wrong number of rateMatrixScaleFactor values provided for LinearModelMatrix");
        }
        */

        //Do the things which are usually done for rateMatrix in SCMigrationModel

        /*
        for (int i = 0; i < rateMatrices.size(); i++){
            rateMatrices.get(i).setLower(Math.max(rateMatrices.get(i).getLower(), 0.0));
        }
        */
        needsUpdate = true;
        System.out.println("Successfully initialised LinearModelMatrix.");
    }

    @Override
    public boolean requiresRecalculation(){
        if (lambdaInput.get().somethingIsDirty()){
            needsUpdate = true;
            return true;
        }
        if(deltaInput.get().somethingIsDirty()){
            needsUpdate = true;
            return true;
        }

        return false;
    }



}
