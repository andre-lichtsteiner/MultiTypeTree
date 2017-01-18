package beast.evolution.tree;

import beast.core.BEASTObject;
import beast.core.Function;
import beast.core.Input;
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

    public Input<RealParameter> lambdaInput = new Input<>("lambdaParameter", "Specify a parameter with two starting values for lambda (the coefficient)", Input.Validate.REQUIRED);

    public Input<RealParameter> deltaInput = new Input<>("deltaParameter", "Specify a parameter with two starting values for delta (the indicator variables)", Input.Validate.REQUIRED);
/*
    public Input<RealParameter> popSizesInput = new Input<>(
            "popSizes",
            "Deme population sizes.",
            Input.Validate.REQUIRED);
*/

    protected List<RealParameter> rateMatrices;
    protected RealParameter lambdaParameter;
    protected RealParameter deltaParameter;

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

        /*
        System.out.println("Alternative matrix value there: ");
        System.out.println(alternativeMatrix.getArrayValue(dim));
        System.out.println("Provided matrix value there: ");
        System.out.println(rateMatrix.getArrayValue(dim));
        */

        //System.out.printf("delta parameter%s%n", deltaInput.get().getArrayValue(0));


        //Return something like this



       // System.out.println(deltaParameter.getArrayValue(0)*lambdaParameter.getArrayValue(0)*rateMatrix.getArrayValue(dim) + deltaParameter.getArrayValue(1)*lambdaParameter.getArrayValue(1)*alternativeMatrix.getArrayValue(dim));

        //return deltaParameter.getArrayValue(0)*lambdaParameter.getArrayValue(0)*alternativeMatrix.getArrayValue(dim) + deltaParameter.getArrayValue(1)*lambdaParameter.getArrayValue(1)*rateMatrix.getArrayValue(dim);
        //This uses the deltaParameter as an indicator variable, and the lambda parameter as a factor for both matrices to give the result as a linear combination of those two


        //return rateMatrix.getArrayValue(dim);
        double totalValue = 0;


        for (int i = 0; i < rateMatrices.size(); i++){ //For each matrix provided
            double lambda = lambdaParameter.getArrayValue(i);
            double delta = deltaParameter.getArrayValue(i);
            double term = lambda * delta * rateMatrices.get(i).getArrayValue(dim);
            totalValue = totalValue + term;
        }
       // return rateMatrices.get(0).getArrayValue(0);
        return totalValue;
    }

    public void initAndValidate(){
        //Setup initial state of this Function

       rateMatrices = rateMatricesInput.get();

        //Check that the XML file provides one (and only one) value for both the lambda and delta parameters for each of the matrices provided. If not, cannot proceed.
        if (lambdaInput.get().getDimension() != rateMatricesInput.get().size() || deltaInput.get().getDimension() != rateMatricesInput.get().size()){
            System.out.println("You must provide the LinearModelMatrix with one value for both the lambdaParameter and the deltaParameter for each of the rate matrices you provide.");
            throw new IndexOutOfBoundsException("Wrong number of values provided to lambdaParameter or deltaParameter for LinearModelMatrix");
        }


    }
}
