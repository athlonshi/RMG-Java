//!********************************************************************************
//!
//!    RMG: Reaction Mechanism Generator                                            
//!
//!    Copyright: Jing Song, MIT, 2002, all rights reserved
//!     
//!    Author's Contact: jingsong@mit.edu
//!
//!    Restrictions:
//!    (1) RMG is only for non-commercial distribution; commercial usage
//!        must require other written permission.
//!    (2) Redistributions of RMG must retain the above copyright
//!        notice, this list of conditions and the following disclaimer.
//!    (3) The end-user documentation included with the redistribution,
//!        if any, must include the following acknowledgment:
//!        "This product includes software RMG developed by Jing Song, MIT."
//!        Alternately, this acknowledgment may appear in the software itself,
//!        if and wherever such third-party acknowledgments normally appear.
//!  
//!    RMG IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED 
//!    WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES 
//!    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
//!    DISCLAIMED.  IN NO EVENT SHALL JING SONG BE LIABLE FOR  
//!    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
//!    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT 
//!    OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;  
//!    OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  
//!    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT  
//!    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF 
//!    THE USE OF RMG, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//! 
//!******************************************************************************



package jing.chem;


import java.util.*;

//## package jing::chem 

//----------------------------------------------------------------------------
// jing\chem\ThermoGAValue.java                                                                  
//----------------------------------------------------------------------------

/**
Immutable data holds all the benson's group value.
*/
//## class ThermoGAValue 
public class ThermoGAValue {
    
    protected double Cp1000 = 0;		//## attribute Cp1000 
    
    protected double Cp1500 = 0;		//## attribute Cp1500 
    
    protected double Cp300 = 0;		//## attribute Cp300 
    
    protected double Cp400 = 0;		//## attribute Cp400 
    
    protected double Cp500 = 0;		//## attribute Cp500 
    
    protected double Cp600 = 0;		//## attribute Cp600 
    
    protected double Cp800 = 0;		//## attribute Cp800 
    
    protected double H298 = 0;		//## attribute H298 
    
    protected double S298 = 0;		//## attribute S298 
    
    protected static double T_HIGH = 1500;		//## attribute T_HIGH 
    
    protected static double T_LOW = 300;		//## attribute T_LOW 
    
    protected String comments = null;		//## attribute comments 
    
    protected String name = null;		//## attribute name 
    
    
    // Constructors
    
    //## operation ThermoGAValue() 
    public  ThermoGAValue() {
        //#[ operation ThermoGAValue() 
        H298 = 0;
        S298 = 0;
        Cp300 = 0;
        Cp400 = 0;
        Cp500 = 0;
        Cp600 = 0;
        Cp800 = 0;
        Cp1000 = 0;
        Cp1500 = 0;
        
        
        
        //#]
    }
    //## operation ThermoGAValue(double,double,double,double,double,double,double,double,double,String) 
    public  ThermoGAValue(double p_H298, double p_S298, double p_Cp300, double p_Cp400, double p_Cp500, double p_Cp600, double p_Cp800, double p_Cp1000, double p_Cp1500, String p_comments) {
        //#[ operation ThermoGAValue(double,double,double,double,double,double,double,double,double,String) 
        H298 = p_H298;
        S298 = p_S298;
        Cp300 = p_Cp300;
        Cp400 = p_Cp400;
        Cp500 = p_Cp500;
        Cp600 = p_Cp600;
        Cp800 = p_Cp800;
        Cp1000 = p_Cp1000;
        Cp1500 = p_Cp1500;
        comments = p_comments;
        
        
        
        //#]
    }
    //## operation ThermoGAValue(String,ThermoGAValue,String) 
    public  ThermoGAValue(String p_name, ThermoGAValue p_ga, String p_comments) {
        //#[ operation ThermoGAValue(String,ThermoGAValue,String) 
        H298 = p_ga.H298;
        S298 = p_ga.S298;
        Cp300 = p_ga.Cp300;
        Cp400 = p_ga.Cp400;
        Cp500 = p_ga.Cp500;
        Cp600 = p_ga.Cp600;
        Cp800 = p_ga.Cp800;
        Cp1000 = p_ga.Cp1000;
        Cp1500 = p_ga.Cp1500;
        comments = p_comments;
        name = p_name;
        
        
        
        //#]
    }
    //## operation ThermoGAValue(ThermoGAValue) 
    public  ThermoGAValue(ThermoGAValue p_ga) {
        //#[ operation ThermoGAValue(ThermoGAValue) 
        H298 = p_ga.H298;
        S298 = p_ga.S298;
        Cp300 = p_ga.Cp300;
        Cp400 = p_ga.Cp400;
        Cp500 = p_ga.Cp500;
        Cp600 = p_ga.Cp600;
        Cp800 = p_ga.Cp800;
        Cp1000 = p_ga.Cp1000;
        Cp1500 = p_ga.Cp1500;
        comments = p_ga.comments;
        name = p_ga.name;
        
        
        //#]
    }
    
    //## operation toString() 
    public String toString() {
        //#[ operation toString() 
        String s = "";
        s = s + String.valueOf(H298) + '\t';
        s = s + String.valueOf(S298) + '\t';
        s = s + String.valueOf(Cp300) + '\t';
        s = s + String.valueOf(Cp400) + '\t';
        s = s + String.valueOf(Cp500) + '\t';
        s = s + String.valueOf(Cp600) + '\t';
        s = s + String.valueOf(Cp800) + '\t';
        s = s + String.valueOf(Cp1000) + '\t';
        s = s + String.valueOf(Cp1500);
        
        return s;
        //#]
    }
    
    protected double getCp1000() {
        return Cp1000;
    }
    
    protected double getCp1500() {
        return Cp1500;
    }
    
    protected double getCp300() {
        return Cp300;
    }
    
    protected double getCp400() {
        return Cp400;
    }
    
    protected double getCp500() {
        return Cp500;
    }
    
    protected double getCp600() {
        return Cp600;
    }
    
    protected double getCp800() {
        return Cp800;
    }
    
    protected double getH298() {
        return H298;
    }
    
    protected double getS298() {
        return S298;
    }
    
    private static double getT_HIGH() {
        return T_HIGH;
    }
    
    private static void setT_HIGH(double p_T_HIGH) {
        T_HIGH = p_T_HIGH;
    }
    
    private static double getT_LOW() {
        return T_LOW;
    }
    
    private static void setT_LOW(double p_T_LOW) {
        T_LOW = p_T_LOW;
    }
    
    public String getComments() {
        return comments;
    }
    
    public String getName() {
        return name;
    }
    
}
/*********************************************************************
	File Path	: RMG\RMG\jing\chem\ThermoGAValue.java
*********************************************************************/
