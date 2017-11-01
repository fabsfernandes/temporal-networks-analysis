
package br.ufu.lsi.event.methods;

/**
 * Adapted from moa.classifiers.rules.driftdetection.PageHinkleyTest
 * 
 * @author fabiola
 *
 */
public class PageHinckleyTest {
    
    protected double cumulativeSum;

    public double getCumulativeSum() {
        return cumulativeSum;
    }

    public double getMinimumValue() {
        return minimumValue;
    }

    protected double minimumValue;

    protected double sumAbsolutError;

    protected long phinstancesSeen;

    protected double threshold;

    protected double alpha;

    protected long minInstancesSeen;

    public PageHinckleyTest( double threshold, double alpha, long minInstancesSeen ) {
        this.threshold = threshold;
        this.alpha = alpha;
        this.minInstancesSeen = minInstancesSeen;
        this.reset();
    }

    public void reset() {
        this.cumulativeSum = 0.0;
        this.minimumValue = Double.MAX_VALUE;
        this.sumAbsolutError = 0.0;
        this.phinstancesSeen = 0;
    }

    //Compute Page-Hinckley test
    public boolean update( double error ) {
        
        this.phinstancesSeen++ ;
        double absolutError = Math.abs( error );
        this.sumAbsolutError = this.sumAbsolutError + absolutError;
        if ( this.phinstancesSeen > minInstancesSeen ) {
            
            // improving the alpha concept
            double meanValue = this.sumAbsolutError / this.phinstancesSeen;
            double percentualValue = this.alpha * meanValue;
            //double percentualValue = this.alpha;
            
            double mT = Math.abs(absolutError - ( this.sumAbsolutError / this.phinstancesSeen )) - percentualValue;
            
            //mT = Math.abs(mT);
            
            this.cumulativeSum = this.cumulativeSum + mT; // Update the cumulative mT sum
            if ( this.cumulativeSum < this.minimumValue ) { // Update the minimum mT value if the new mT is smaller than the current minimum
                this.minimumValue = this.cumulativeSum;
            }
            
            //System.out.println( "\n" + cumulativeSum + " - " + minimumValue + " = " + (this.cumulativeSum - this.minimumValue) );
            return this.cumulativeSum - this.minimumValue > Math.abs(this.minimumValue*this.threshold);
            
            //return ( ( ( this.cumulativeSum - this.minimumValue ) > this.threshold ) );
        }
        return false;
    }
    
    public void incrementPHInstancesSeen(){
        this.phinstancesSeen++ ;
    }

}
