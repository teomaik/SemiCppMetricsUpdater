/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parsers;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author Nikos
 */
public abstract class CodeFile implements Serializable {
    public File file;
    public int fanOut;
    public HashMap<String, Integer> methodsLOC;
    public HashMap<String, Integer> methodsCC;
    public int cohesion;
    
    public CodeFile(File file){
        this.file=file;
        methodsLOC= new HashMap<>();
        methodsCC= new HashMap<>();
    }
    
    public abstract void parse();
    public abstract void calculateCohesion();
    
}
