package beast.evolution.tree;

import beast.core.BEASTObject;
import beast.core.Function;
import beast.core.Input;
import beast.core.parameter.BooleanParameter;
import beast.core.parameter.RealParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andre on 18/01/17.
 */
public class LinearModelMatrix extends BEASTObject implements Function {


    //Must provide the model which is to be compared with the flat model
    public Input<List<RealParameter>> rateMatricesInput = new Input<>(
            "rateMatrix",
            "Migration rate matrices",
            new ArrayList<RealParameter>(), Input.Validate.REQUIRED);

    public Input<List<RealParameter>> rateMatricesScaleFactorsInput = new Input<>("rateMatrixScaleFactor",
            "Optional number by which all items in the migration matrix will be evenly multiplied.", new ArrayList<RealParameter>());
    //List of rateMatrix scale factors


    public Input<RealParameter> lambdaInput = new Input<>("lambdaParameter", "Specify a parameter with two starting values for lambda (the coefficient)", Input.Validate.REQUIRED);

    public Input<BooleanParameter> deltaInput = new Input<>("deltaParameter", "Specify a parameter with two starting values for delta (the indicator variables)", Input.Validate.REQUIRED);

    protected List<RealParameter> rateMatrices;
    protected List<RealParameter> rateMatricesScaleFactors;
    protected RealParameter lambdaParameter;
    protected BooleanParameter deltaParameter;

    public int getDimension(){
        return rateMatricesInput.get().size(); //Returns number of matrices provided

    }

    public double getArrayValue(){
        return rateMatricesInput.get().size(); //Need to determine what I should return here *TODO-----
    }

    @Override
    public double getArrayValue(int dim) {
        //Do some things here to change the
        //Get the latest values for the delta and lambda parameters
        lambdaParameter = lambdaInput.get();
        deltaParameter = deltaInput.get();



        double totalValue = 0;

        for (int i = 0; i < rateMatrices.size(); i++){ //For each matrix provided
            double lambda = lambdaParameter.getArrayValue(i);
            double delta = deltaParameter.getArrayValue(i);
           // double delta = 0;
            //if (deltaParameter.getArrayValue(i)){
            //    delta = 1;
           // }

            //Each matrix may have its own scaleFactor
            double scaleFactor = 1; //Leave this as 1 if no scaleFactor provided
            if (rateMatricesScaleFactorsInput.get() != null && rateMatricesScaleFactorsInput.get().size() != 0){
                scaleFactor = rateMatricesScaleFactorsInput.get().get(i).getValue();
            }


            double term = lambda * delta * rateMatrices.get(i).getArrayValue(dim);
            totalValue = totalValue + term;
        }
       // return Math.exp(totalValue);
        return totalValue;
    }

    public void initAndValidate(){
        //Setup initial state of this Function

       rateMatrices = rateMatricesInput.get();
       rateMatricesScaleFactors = rateMatricesScaleFactorsInput.get();


        //Check that the XML file provides one (and only one) value for both the lambda and delta parameters for each of the matrices provided. If not, cannot proceed.
        if (lambdaInput.get().getDimension() != rateMatricesInput.get().size() || deltaInput.get().getDimension() != rateMatricesInput.get().size()){
            System.out.println("You must provide the LinearModelMatrix with one value for both the lambdaParameter and the deltaParameter for each of the rate matrices you provide.");
            throw new IndexOutOfBoundsException("Wrong number of values provided to lambdaParameter or deltaParameter for LinearModelMatrix");
        }

        //Check that if there are any rateMatricesScaleFactors provided, that there are the right number of them
        if(rateMatricesScaleFactorsInput.get() != null && rateMatricesScaleFactorsInput.get().size() != 0 && rateMatricesScaleFactorsInput.get().size() != rateMatricesInput.get().size()){
            System.out.println("If you are using scaleFactors for a rateMatrix, you must provide the LinearModelMatrix with a rateMatrixScaleFactor for each of the rate matrices you provide.");
            throw new IndexOutOfBoundsException("Wrong number of rateMatrixScaleFactor values provided for LinearModelMatrix");
        }


    }
}
