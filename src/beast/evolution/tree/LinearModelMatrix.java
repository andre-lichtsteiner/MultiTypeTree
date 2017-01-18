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

    public int getDimension(){
        return rateMatrix.getDimension();

    }

    public double getArrayValue(){
        return rateMatrix.getArrayValue();

    }

    @Override
    public double getArrayValue(int dim) {
        //Do some things here to change the

        //Temp
        return rateMatrix.getArrayValue(dim);


    }

    public void initAndValidate(){
        System.out.println("init and validate called on LinearModelMatrix");

        rateMatrix = rateMatrixInput.get();


    }
}
