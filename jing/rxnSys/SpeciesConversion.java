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



package jing.rxnSys;


import java.util.*;
import jing.chem.Species;

//## package jing::rxnSys 

//----------------------------------------------------------------------------
// jing\rxnSys\SpeciesConversion.java                                                                  
//----------------------------------------------------------------------------

//## class SpeciesConversion 
public class SpeciesConversion {
    
    protected double conversion;		//## attribute conversion 
    
    protected Species species;		//## attribute species 
    
    
    // Constructors
    
    //## operation SpeciesConversion(Species,double) 
    public  SpeciesConversion(Species p_species, double p_conversion) {
        //#[ operation SpeciesConversion(Species,double) 
        species = p_species;
        conversion = p_conversion;
        //#]
    }
    public  SpeciesConversion() {
    }
    
    public double getConversion() {
        return conversion;
    }
    
    public void setConversion(double p_conversion) {
        conversion = p_conversion;
    }
    
    public Species getSpecies() {
        return species;
    }
    
    public void setSpecies(Species p_species) {
        species = p_species;
    }
    
}
/*********************************************************************
	File Path	: RMG\RMG\jing\rxnSys\SpeciesConversion.java
*********************************************************************/
