package net.noisetube.api.audio.calibration;

import org.xml.sax.SAXException;

import jlibs.xml.sax.XMLDocument;

public class CorrectionPair implements Comparable<CorrectionPair> {
    private double input;    //phone
    private double output;    //ref

    public CorrectionPair(double input, double output) {
        this.input = input;
        this.output = output;
    }

    public double getInput() {
        return input;
    }

    public void setInput(double input) {
        this.input = input;
    }

    public double getOutput() {
        return output;
    }

    public void setOutput(double output) {
        this.output = output;
    }

    public int compareTo(CorrectionPair o) {
        if (this.input == o.input)
            return 0;
        if (this.input > o.input)
            return 1;
        return -1;
    }

    public void parseToXML(XMLDocument xml) throws SAXException {
        xml.startElement("correction");
        xml.addAttribute("input", Double.toString(input));
        xml.addAttribute("output", Double.toString(output));
        xml.endElement("correction");
    }

}
