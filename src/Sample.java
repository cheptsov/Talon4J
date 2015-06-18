import weka.classifiers.functions.LibSVM;
import weka.core.*;

import java.io.*;
import java.util.Arrays;

public class Sample implements Serializable {
    Instances instances = null;

    final Attribute WIDTH = new Attribute("width");
    final Attribute HEIGHT = new Attribute("height");

    final Attribute TARGET;
    final FastVector TARGET_ATTRIBUTES = new FastVector();

    {
        FastVector attrInfo = new FastVector();
        attrInfo.addElement(WIDTH);
        attrInfo.addElement(HEIGHT);

        TARGET_ATTRIBUTES.addElement("square");
        TARGET_ATTRIBUTES.addElement("rectangle");
        TARGET = new Attribute("target", TARGET_ATTRIBUTES);

        attrInfo.addElement(TARGET);

        instances = new Instances("MessageClassificationProblem", attrInfo, 100);
        instances.setClassIndex(instances.numAttributes() - 1);
    }

    public void train(double width, double height, String target) {
        Instance instance = getInstance(width, height);
        instance.setValue(TARGET, target);
        instances.add(instance);
    }

    private Instance getInstance(double width, double height) {
        Instance instance = new Instance(3);
        instance.setValue(WIDTH, width);
        instance.setValue(HEIGHT, height);
        instance.setDataset(instances);
        return instance;
    }

    public static void main(String[] options) throws Exception {
        Sample sample = new Sample();
        sample.train(2, 2, "square");
        sample.train(4, 4, "square");
        sample.train(9, 9, "square");
        sample.train(10, 10, "square");
        sample.train(5, 6, "rectangle");
        sample.train(7, 10, "rectangle");
        sample.train(3, 4, "rectangle");
        sample.train(4, 1, "rectangle");
        LibSVM libSVM = new LibSVM();
        libSVM.buildClassifier(sample.instances);
        double[] distribution = libSVM.distributionForInstance(sample.getInstance(8, 10));
        System.out.println(Arrays.toString(distribution));
    }
}