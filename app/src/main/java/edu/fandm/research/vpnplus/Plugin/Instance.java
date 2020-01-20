package edu.fandm.research.vpnplus.Plugin;

public class Instance {
    // The class Instance is used to represent an instance in the data set
    
    public String actual;
    public String predicted;
    public String[] attributes;

    /**
     * Create a new Instance given attribute list and known category/class
     * @param att
     * @param cat
     */
    public Instance(String[] att, String cat){
        actual = cat;
        attributes = att;
    }

    /**
     * Create a new Instance given attribute list (category unknown)
     * @param att
     */
    public Instance(String[] att){
        attributes = att;
    }

    /**
     * Return a String representation of Instance object which contains attributes and category/class
     * @return
     */
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for (String s: attributes){
            sb.append(s+" ");
        }
        sb.append(actual);
        return sb.toString();
    }
}
