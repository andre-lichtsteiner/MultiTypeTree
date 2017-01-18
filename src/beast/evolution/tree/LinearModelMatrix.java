package beast.evolution.tree;

import beast.core.Function;
import beast.core.Input;
import beast.core.parameter.RealParameter;

/**
 * Created by andre on 18/01/17.
 */
public class LinearModelMatrix implements Function {


    //Must provide the model which is to be compared with the flat model
    public Input<RealParameter> rateMatrixInput = new Input<>(
            "rateMatrix",
            "Migration rate matrix",
            Input.Validate.REQUIRED);
/*
    public Input<RealParameter> popSizesInput = new Input<>(
            "popSizes",
            "Deme population sizes.",
            Input.Validate.REQUIRED);
*/


    @Override
    public int getDimension() {
        return 0;
    }

    @Override
    public double getArrayValue() {
        return 0;
    }

    @Override
    public double getArrayValue(int dim) {
        //Do some things here to change the



        return 0;
    }

    @Override
    public double[] getDoubleValues() {
        return new double[0];
    }
}
