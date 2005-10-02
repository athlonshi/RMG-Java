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


import jing.rxn.*;
import jing.chem.*;
import java.util.*;
import jing.chem.Species;
import jing.rxn.Reaction;
import jing.rxn.Structure;
import jing.param.Pressure;
import jing.param.Temperature;
import jing.param.ParameterInfor;

//## package jing::rxnSys 

//----------------------------------------------------------------------------
// jing\rxnSys\JDASPK.java                                                                  
//----------------------------------------------------------------------------

//## class JDASPK 
public class JDASPK implements SASolver, DAESolver {
    
    protected HashMap IDTranslator = new HashMap();		//## attribute IDTranslator 
    
    protected double atol;		//## attribute atol 
    
    protected int maxSpeciesNumber = 0;		//## attribute maxSpeciesNumber 
    
    protected ParameterInfor [] parameterInfor = null;		//## attribute parameterInfor 
    
    protected ODEReaction [] reactionList = null;		//## attribute reactionList 
    
    protected double rtol;		//## attribute rtol 
    
    protected ODEReaction [] thirdBodyReactionList;		//## attribute thirdBodyReactionList 
    
    //protected int temp =1;
    // Constructors
    
    //## operation JDASPK() 
    private  JDASPK() {
        //#[ operation JDASPK() 
        //#]
    }
    //## operation JDASPK(double,double,ParameterInfor []) 
    public  JDASPK(double p_rtol, double p_atol, ParameterInfor [] p_parameterInfor) {
        //#[ operation JDASPK(double,double,ParameterInfor []) 
        rtol = p_rtol;
        atol = p_atol;
        
        parameterInfor = p_parameterInfor;
        
        
        
        //#]
    }
    
    //## operation clean() 
    public void clean() {
        //#[ operation clean() 
        cleanDaspk();
        //#]
    }
    
    //## operation cleanDaspk() 
    private native void cleanDaspk();
    
    //## operation fixingPDepRate(Structure) 
    public double fixingPDepRate(Structure p_structure) {
        //#[ operation fixingPDepRate(Structure) 
        //check c4h9o. beta scission
        	if (p_structure.getReactantNumber() == 1 && p_structure.getProductNumber() == 2) {
        	    ChemGraph r = (ChemGraph)p_structure.getReactants().next();
        	    Iterator it = p_structure.getProducts(); 
        	    ChemGraph p1 = (ChemGraph)it.next();
        	    ChemGraph p2 = (ChemGraph)it.next();
        	    
        	    if (r.getChemicalFormula().equals("C4H9O.")) {
        	    	if (p1.getChemicalFormula().equals("C2H5.") || p2.getChemicalFormula().equals("C2H5.") ) {
               			return 1E3;
        	     	} 
        	    	else if (p1.getChemicalFormula().equals("C3H7.") || p2.getChemicalFormula().equals("C3H7.") ) {
               			return 1E3;
        	     	} 
        	    	else if (p1.getChemicalFormula().equals("CH3.") || p2.getChemicalFormula().equals("CH3.") ) {
               			return 1E3;
        	     	} 
        	    	else if (p1.getChemicalFormula().equals("H.") || p2.getChemicalFormula().equals("H.") ) {
               			return 1E3;
        	     	} 
        	    } 
        	}
        
        return 1;
        //#]
    }
    
    //## operation generateAllODEReactionList(ReactionModel,SystemSnapshot,Temperature,Pressure) 
    public void generateAllODEReactionList(ReactionModel p_reactionModel, SystemSnapshot p_beginStatus, Temperature p_temperature, Pressure p_pressure) {
        //#[ operation generateAllODEReactionList(ReactionModel,SystemSnapshot,Temperature,Pressure) 
        int size = p_reactionModel.getReactionSet().size();
        ODEReaction [] result = new ODEReaction[size];
        int id = 0;
        int thirdID = 0;
        Iterator iter = p_reactionModel.getReactionSet().iterator();
        while (iter.hasNext()) {
        	Reaction r = (Reaction)iter.next();
            if (r instanceof ThirdBodyReaction) {
            	thirdID++;
            	ODEReaction or = transferReaction(r, p_beginStatus, p_temperature, p_pressure);
        		result[size - thirdID] = or;
            }
            else {
        		id++;
            	ODEReaction or = transferReaction(r, p_beginStatus, p_temperature, p_pressure);
        		result[id-1] = or;
            }
        }
        
        reactionList = new ODEReaction[id];
        thirdBodyReactionList = new ODEReaction[thirdID];
        
        if (id+thirdID != size) throw new InvalidReactionSetException("Generating ODE reaction list for daspk");
        
        for (int i = 0; i < id; i++) {
        	reactionList[i] = result[i];
        
        }
        for (int i = id; i < size; i++) {
        	thirdBodyReactionList[i-id] = result[i];
        }
        
        return;
        //#]
    }
    
    //## operation generatePDepODEReactionList(ReactionModel,SystemSnapshot,Temperature,Pressure) 
    public LinkedList generatePDepODEReactionList(ReactionModel p_reactionModel, SystemSnapshot p_beginStatus, Temperature p_temperature, Pressure p_pressure) {
        //#[ operation generatePDepODEReactionList(ReactionModel,SystemSnapshot,Temperature,Pressure) 
        LinkedList nonPDepList = new LinkedList();
        LinkedList pDepList = new LinkedList();
        
        HashSet pDepStructureSet = new HashSet();
        for (Iterator iter = PDepNetwork.getDictionary().values().iterator(); iter.hasNext(); ) {
        	PDepNetwork pdn = (PDepNetwork)iter.next();
        	for (Iterator pdniter = pdn.getPDepNetReactionList(); pdniter.hasNext();) {
        		PDepNetReaction pdnr = (PDepNetReaction)pdniter.next();
        		if (!pdnr.reactantEqualsProduct()) {
        			if (p_reactionModel instanceof CoreEdgeReactionModel) {
        				if (((CoreEdgeReactionModel)p_reactionModel).isReactedReaction(pdnr)) {
        					pDepList.add(pdnr);
        					pDepStructureSet.add(pdnr.getStructure());
        				}
        			}
        			else {
        				pDepList.add(pdnr);
        				pDepStructureSet.add(pdnr.getStructure());
        			}
        		}	
        	}
        }
        //System.out.println("Total Number of pressure dependent reactions are "+ pDepList.size());
        for (Iterator iter = p_reactionModel.getReactionSet().iterator(); iter.hasNext(); ) {
        	Reaction r = (Reaction)iter.next();
        	if (!r.reactantEqualsProduct()) {
        		Structure s = r.getStructure();
        		if (!pDepStructureSet.contains(s)) { 
        			nonPDepList.add(r);
        		}                              
        	}
        }
        //System.out.println("Total Number of non pressure dependent reactions are "+ nonPDepList.size());
        
		int size = nonPDepList.size() + pDepList.size();
        reactionList = new ODEReaction[size];
        LinkedList all = new LinkedList();
        int id = 0;
        //System.out.println("non p_dep reactions: " + nonPDepList.size() );
        for (Iterator iter = nonPDepList.iterator(); iter.hasNext(); ) {
        	Reaction r = (Reaction)iter.next();
        	all.add(r);
        
        	ODEReaction or = transferReaction(r, p_beginStatus, p_temperature, p_pressure);
            reactionList[id] = or;                                                              
            double rate = r.calculateRate(p_temperature);
            if (r instanceof TemplateReaction) rate = ((TemplateReaction)r).calculatePDepRate(p_temperature);
            //System.out.println(r.getStructure().toString()+"\t rate = \t"+ String.valueOf(rate));
            //System.out.println(r.toChemkinString());
			id++;
        }
        
        //System.out.println("p_dep reactions: " + pDepList.size());
        for (Iterator iter = pDepList.iterator(); iter.hasNext(); ) {
        	Reaction r = (Reaction)iter.next();
        	all.add(r);
        
        	ODEReaction or = transferReaction(r, p_beginStatus, p_temperature, p_pressure);
            reactionList[id] = or;
            //System.out.println(r.getStructure().toString() + "\t rate = \t" + Double.toString(or.getRate()));
            //System.out.println(r.toChemkinString());
			id++;
        }
        
        return all;
        //#]
    }
    
    //## operation generateSpeciesStatus(ReactionModel,double [],double [],int) 
    private HashMap generateSpeciesStatus(ReactionModel p_reactionModel, double [] p_y, double [] p_yprime, int p_paraNum) {
        //#[ operation generateSpeciesStatus(ReactionModel,double [],double [],int) 
        int neq = p_reactionModel.getSpeciesNumber()*(p_paraNum+1);
        if (p_y.length != neq) throw new DynamicSimulatorException();
        if (p_yprime.length != neq) throw new DynamicSimulatorException();
        
        HashMap speStatus = new HashMap();
        
        for (Iterator iter = p_reactionModel.getSpecies(); iter.hasNext(); ) {
        	Species spe = (Species)iter.next();
        	int id = getRealID(spe);
        	if (id>p_y.length) throw new UnknownReactedSpeciesException(spe.getName());
        	double conc = p_y[id-1];
        	double flux = p_yprime[id-1];
        
        	System.out.println(String.valueOf(spe.getID()) + '\t' + spe.getName() + '\t' + String.valueOf(conc) + '\t' + String.valueOf(flux));
        
        	if (conc < 0) {
        		if (Math.abs(conc) < 1.0E-19) conc = 0;
        		else throw new NegativeConcentrationException("species " + spe.getName() + " has negative conc: " + String.valueOf(conc));
        	}
        	SpeciesStatus ss = new SpeciesStatus(spe, 1, conc, flux);
        	speStatus.put(spe,ss);
        }
        
        return speStatus;
        //#]
    }
    
    //## operation generateThirdBodyReactionList(ReactionModel,SystemSnapshot,Temperature,Pressure) 
    public void generateThirdBodyReactionList(ReactionModel p_reactionModel, SystemSnapshot p_beginStatus, Temperature p_temperature, Pressure p_pressure) {
        //#[ operation generateThirdBodyReactionList(ReactionModel,SystemSnapshot,Temperature,Pressure) 
        int size = p_reactionModel.getReactionSet().size();
        ODEReaction [] result = new ODEReaction[size];
        int thirdID = 0;
        Iterator iter = p_reactionModel.getReactionSet().iterator();
        while (iter.hasNext()) {
        	Reaction r = (Reaction)iter.next();
        
            if (r instanceof ThirdBodyReaction) {
            	thirdID++;
            	ODEReaction or = transferReaction(r, p_beginStatus, p_temperature, p_pressure);
        		result[thirdID-1] = or;
             }
        }
        
        thirdBodyReactionList = new ODEReaction[thirdID];
        
        for (int i = 0; i < thirdID; i++) {
        	thirdBodyReactionList[i] = result[i];
        }
        
        return;
        //#]
    }
    
    //## operation getRealID(Species) 
    public int getRealID(Species p_species) {
        //#[ operation getRealID(Species) 
        Integer id = (Integer)IDTranslator.get(p_species);
        if (id == null) {
        	maxSpeciesNumber++;
        	id = new Integer(maxSpeciesNumber);
        	IDTranslator.put(p_species, id);
        }
        
        return id.intValue();
        //#]
    }
    
    //## operation solve(boolean,ReactionModel,boolean,SystemSnapshot,ReactionTime,ReactionTime,Temperature,Pressure,boolean) 
    public SystemSnapshot solve(boolean p_initialization, ReactionModel p_reactionModel, boolean p_reactionChanged, SystemSnapshot p_beginStatus, ReactionTime p_beginTime, ReactionTime p_endTime, Temperature p_temperature, Pressure p_pressure, boolean p_conditionChanged) {
        //#[ operation solve(boolean,ReactionModel,boolean,SystemSnapshot,ReactionTime,ReactionTime,Temperature,Pressure,boolean) 
        ReactionTime rt = p_beginStatus.getTime();
        if (!rt.equals(p_beginTime)) throw new InvalidBeginStatusException();
        
        // set time
        double tBegin = p_beginTime.getStandardTime();
        double tEnd = p_endTime.getStandardTime();
        
        // set reaction set
        //if (p_initialization || p_reactionChanged || p_conditionChanged) {
        generateThirdBodyReactionList(p_reactionModel, p_beginStatus, p_temperature, p_pressure);
        LinkedList rList = generatePDepODEReactionList(p_reactionModel, p_beginStatus, p_temperature, p_pressure);
        
        	//generateAllODEReactionList(p_reactionModel, p_beginStatus, p_temperature, p_pressure);
        	//p_reactionChanged = true;
        //}
        
        // set numbers
		System.out.println("Total number of reactions to Daspk is "+rList.size());
        int nState = p_reactionModel.getSpeciesNumber();
        int nParameter = 0;
        if (parameterInfor!=null) nParameter = parameterInfor.length;
        int neq = nState*(nParameter+1);
        
        // set temperature and pressure
        double T = p_temperature.getK();
        double P = p_pressure.getAtm();
        
        // set initial value of y and yprime;
        double [] y = new double[neq];
        double [] yprime = new double[neq];
        
		//double numberOfReactedSpecies=0;
        // get the present status at t_begin, and set y and y' accordingly
        System.out.println("Before ODE: " + String.valueOf(tBegin) + "SEC");
        System.out.println("End at : " + String.valueOf(tEnd) + "SEC");
        for (Iterator iter = p_beginStatus.getSpeciesStatus(); iter.hasNext(); ) {
        	SpeciesStatus ss = (SpeciesStatus)iter.next();
        	double conc = ss.getConcentration();
        	double flux = ss.getFlux();
        	if (ss.isReactedSpecies()) {
				
        		Species spe = ss.getSpecies();
        		int id = getRealID(spe);
        		System.out.println(String.valueOf(spe.getID()) + '\t' + spe.getName() + '\t' + String.valueOf(conc) + '\t' + String.valueOf(flux));
         		y[id-1] = conc;
        		yprime[id-1] = flux;
        	}
        }
        
		//System.out.println("Number of Reacted Species is " + numberOfReactedSpecies);
		
        int idid;
        HashMap speStatus = new HashMap();
        double[] tPresent = {tBegin};
		int temp = 1;
        if (nParameter==0) {
        	idid = solveDAE(p_initialization, reactionList, true, thirdBodyReactionList, nState, y, yprime, tBegin, tEnd, this.rtol, this.atol, T, P, temp, tPresent);
        	if (idid !=1 && idid != 2 && idid != 3)	throw new DynamicSimulatorException("DASPK: SA off.");
                System.out.println("After ODE: from " + String.valueOf(tBegin) + " SEC to " + String.valueOf(tEnd) + "SEC");
				
        	speStatus = generateSpeciesStatus(p_reactionModel, y, yprime, 0);
        }
        else {
        	idid = solveSEN(p_initialization, reactionList, p_reactionChanged, thirdBodyReactionList, nState, nParameter, this.parameterInfor, y, yprime, tBegin, tEnd, this.rtol, this.atol, T, P);
        	if (idid != 2 && idid != 3) throw new DynamicSimulatorException("DASPK: SA on.");
        	speStatus = generateSpeciesStatus(p_reactionModel, y, yprime, nParameter);
        }
        
		SystemSnapshot sss = new SystemSnapshot(p_endTime, speStatus, p_beginStatus.getTemperature(), p_beginStatus.getPressure()); 
		//ReactionSystem.outputAllPathways(SpeciesDictionary.getSpeciesFromName("CH4"), rList, sss, p_temperature);              
        //ReactionSystem.outputAllPathways(SpeciesDictionary.getSpeciesFromName("CO"), rList, sss, p_temperature);              
        //ReactionSystem.outputAllPathways(SpeciesDictionary.getSpeciesFromName("CO2"), rList, sss, p_temperature);              
        
        return sss;
        //#]
    }
    
    //## operation solveDAE(boolean,ODEReaction [],boolean,ODEReaction [],int,double [],double [],double,double,double,double,double,double) 
    private native int solveDAE(boolean p_initialization, ODEReaction [] p_reactionSet, boolean p_reactionChanged, ODEReaction [] p_thirdBodyReactionList, int p_nState, double [] p_y, double [] p_yprime, double p_tBegin, double p_tEnd, double p_rtol, double p_atol, double p_temperature, double p_pressure, int temp, double [] tpresent);
    
    //## operation solveSEN(boolean,ODEReaction [],boolean,ODEReaction [],int,int,ParameterInfor [],double [],double [],double,double,double,double,double,double) 
    private native int solveSEN(boolean p_initialization, ODEReaction [] p_reactionSet, boolean p_reactionChanged, ODEReaction [] p_thirdBodyReactionList, int p_nState, int p_nParameter, ParameterInfor [] p_parameterInfor, double [] p_y, double [] p_yprime, double p_tBegin, double p_tEnd, double p_rtol, double p_atol, double p_temperature, double p_pressure);
    static {System.loadLibrary("daspk");}
    
    
    //## operation transferReaction(Reaction,SystemSnapshot,Temperature,Pressure) 
    public ODEReaction transferReaction(Reaction p_reaction, SystemSnapshot p_beginStatus, Temperature p_temperature, Pressure p_pressure) {
        //#[ operation transferReaction(Reaction,SystemSnapshot,Temperature,Pressure) 
        double dT = 1;
        Temperature Tup = new Temperature(p_temperature.getStandard()+dT, Temperature.getStandardUnit());
        Temperature Tlow = new Temperature(p_temperature.getStandard()-dT, Temperature.getStandardUnit());
        
        int rnum = p_reaction.getReactantNumber();
        int pnum = p_reaction.getProductNumber();
        
        int [] rid = new int[rnum];
        int index = 0;
        for (Iterator r_iter = p_reaction.getReactants(); r_iter.hasNext(); ) {
        	Species s = ((ChemGraph)r_iter.next()).getSpecies();
        	rid[index] = getRealID(s);
        	index++;
        }
        
        int [] pid = new int[pnum];
        index = 0;
        for (Iterator p_iter = p_reaction.getProducts(); p_iter.hasNext(); ) {
        	Species s = ((ChemGraph)p_iter.next()).getSpecies();
        	pid[index] = getRealID(s);
        	index++;
        }
        
        if(p_reaction instanceof PDepNetReaction) {
        	double rate = ((PDepNetReaction)p_reaction).getRate();
        	//rate /= fixingPDepRate(p_reaction.getStructure());
        	/*if (p_reaction.getReactantNumber()==1 && p_reaction.getProductNumber()==2) {
        		if (rate>1.0E7) {
        			System.out.println("reaction " + p_reaction.getStructure().toString() + "\t rate is too high: " + String.valueOf(rate));	
        			rate = 1.0E7;
        		}
        	}*/	
        	ODEReaction or = new ODEReaction(rnum, pnum, rid, pid, rate);
        	return or;
        }
        else {
        	double rate = 0;
        	if (p_reaction instanceof TemplateReaction) {
        		rate = ((TemplateReaction)p_reaction).calculatePDepRate(p_temperature);
        	}
        	else {
        		rate = p_reaction.calculateRate(p_temperature);
        	}	
        	/* the old way to output reaction to Daspk, now only out put rate
        	int direction = p_reaction.getDirection();
        	
        	ArrheniusKinetics ak = (ArrheniusKinetics)p_reaction.getKinetics();
        	double A = ak.getAValue();
        	double n = ak.getNValue();
        	double E = ak.getEValue();
        	double alpha = 0;
        	if (ak instanceof ArrheniusEPKinetics) alpha = ((ArrheniusEPKinetics)ak).getAlphaValue();
        	
        	double H = p_reaction.calculateHrxn(p_temperature);
        	double dHdT = (p_reaction.calculateHrxn(Tup)-p_reaction.calculateHrxn(Tlow))/2/dT;
        	
        	double Keq = p_reaction.calculateKeq(p_temperature);
        	double dKeqdT = (p_reaction.calculateKeq(Tup)-p_reaction.calculateKeq(Tlow))/2/dT;
        	
        	// add thirdbody correction
        	if (p_reaction instanceof ThirdBodyReaction) {
        		double correction = ((ThirdBodyReaction)p_reaction).calculateThirdBodyCoefficient(p_beginStatus);
        		A *= correction;
        	}
        	//String output = p_reaction.getStructure().toString() + '\t' + String.valueOf(A) + '\t' +  String.valueOf(n) + '\t' + String.valueOf(E) + '\t' + String.valueOf(alpha) + '\t' + String.valueOf(H) + '\n';
        	//System.out.println(output);
        	//System.out.println(" Keq = " + String.valueOf(Keq) + '\t' + "k = " + String.valueOf(p_reaction.calculateRate(p_temperature)));
        	*/
        	
        	ODEReaction or = new ODEReaction(rnum, pnum, rid, pid, rate);
        	return or;
        }
        //#]
    }
    
    public double getAtol() {
        return atol;
    }
    
    public int getMaxSpeciesNumber() {
        return maxSpeciesNumber;
    }
    
    public double getRtol() {
        return rtol;
    }
    
}
/*********************************************************************
	File Path	: RMG\RMG\jing\rxnSys\JDASPK.java
*********************************************************************/
