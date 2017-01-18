package beast.evolution.tree;

import beast.core.BEASTObject;
import beast.core.Function;
import beast.core.Input;
import beast.core.parameter.RealParameter;

/**
 * Created by andre on 18/01/17.
 */
public class LinearModelMatrix extends BEASTObject implements Function {


    //Must provide the model which is to be compared with the flat model
    public Input<RealParameter> rateMatrixInput = new Input<>(
            "rateMatrix",
            "Migration rate matrix",
            Input.Validate.REQUIRED);

    public Input<RealParameter> lambdaInput = new Input<>("lambdaParameter", "Specify a parameter with two starting values for lambda (the coefficient)", Input.Validate.REQUIRED);

    public Input<RealParameter> deltaInput = new Input<>("deltaParameter", "Specify a parameter with two starting values for delta (the indicator variables)", Input.Validate.REQUIRED);
/*
    public Input<RealParameter> popSizesInput = new Input<>(
            "popSizes",
            "Deme population sizes.",
            Input.Validate.REQUIRED);
*/

    protected RealParameter rateMatrix;
    protected RealParameter alternativeMatrix;
    protected RealParameter lambdaParameter;
  //  protected RealParameter lambda;
    protected RealParameter deltaParameter;
  //  protected RealParameter delta;

    public int getDimension(){
        return rateMatrix.getDimension();

    }

    public double getArrayValue(){
        return rateMatrix.getArrayValue();

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



        System.out.println(deltaParameter.getArrayValue(0)*lambdaParameter.getArrayValue(0)*rateMatrix.getArrayValue(dim) + deltaParameter.getArrayValue(1)*lambdaParameter.getArrayValue(1)*alternativeMatrix.getArrayValue(dim));

        //return deltaParameter.getArrayValue(0)*lambdaParameter.getArrayValue(0)*alternativeMatrix.getArrayValue(dim) + deltaParameter.getArrayValue(1)*lambdaParameter.getArrayValue(1)*rateMatrix.getArrayValue(dim);
        //This uses the deltaParameter as an indicator variable, and the lambda parameter as a factor for both matrices to give the result as a linear combination of those two


        return rateMatrix.getArrayValue(dim);


    }

    public void initAndValidate(){
        //Setup initial state of this Function

        rateMatrix = rateMatrixInput.get();

        //Check that the XML file provides two values for both the lambda and delta parameters. If only one is given, this causes a NullPointerException
        if (lambdaInput.get().getDimension() != 2 || deltaInput.get().getDimension() != 2){
            System.out.println("You must provide the LinearModelMatrix with two values for both the lambdaParameter and the deltaParameter. The first will be used with the rate matrix you have provided and the second will be used with the alternative rateMatrix.");
        }

        //Create the alternative matrix for comparison

        Double[] alternativeArray = new Double[rateMatrix.getDimension()];
        for (int i = 0; i < alternativeArray.length; i++){
            alternativeArray[i] = new Double(1);
        }
        alternativeMatrix = new RealParameter(alternativeArray);

    }
}
