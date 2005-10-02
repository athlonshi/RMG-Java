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
import jing.param.*;
import jing.chem.Species;
import jing.rxn.Reaction;
import jing.param.Pressure;
import jing.param.Temperature;
import jing.rxn.ReactionGenerator;

//## package jing::rxnSys 

//----------------------------------------------------------------------------
// jing\rxnSys\ReactionSystem.java                                                                  
//----------------------------------------------------------------------------

/**
Under specification of the natrual reaction conditions, (Temperature, Pressure, Reactants and their initial concentrations, Conversion), ReactionSystem represents the whole reaction process. 
*/
//## class ReactionSystem 
public class ReactionSystem {
    
    protected HashSet originalReactant;		//## attribute originalReactant 
    
    protected DynamicSimulator dynamicSimulator;
    protected FinishController finishController;
    protected InitialStatus initialStatus;
    protected PressureModel pressureModel;
    protected PrimaryReactionLibrary primaryReactionLibrary;
    protected ReactionGenerator reactionGenerator;
    protected ReactionModel reactionModel;
    protected ReactionModelEnlarger reactionModelEnlarger;
    protected LinkedList systemSnapshot;
    protected TemperatureModel temperatureModel;
    
    // Constructors
    
    //## operation ReactionSystem(TemperatureModel,PressureModel,ReactionModelEnlarger,FinishController,DynamicSimulator,PrimaryReactionLibrary,ReactionGenerator,HashSet,InitialStatus) 
    public  ReactionSystem(TemperatureModel p_temperatureModel, PressureModel p_pressureModel, ReactionModelEnlarger p_reactionModelEnlarger, FinishController p_finishController, DynamicSimulator p_dynamicSimulator, PrimaryReactionLibrary p_primaryReactionLibrary, ReactionGenerator p_reactionGenerator, HashSet p_speciesSeed, InitialStatus p_initialStatus) {
        {
            systemSnapshot=new LinkedList();
        }
        //#[ operation ReactionSystem(TemperatureModel,PressureModel,ReactionModelEnlarger,FinishController,DynamicSimulator,PrimaryReactionLibrary,ReactionGenerator,HashSet,InitialStatus) 
        temperatureModel = p_temperatureModel;
        pressureModel = p_pressureModel;
        setFinishController(p_finishController);
        reactionModelEnlarger = p_reactionModelEnlarger;
        initialStatus = p_initialStatus;
        dynamicSimulator = p_dynamicSimulator;
        primaryReactionLibrary = p_primaryReactionLibrary;
        reactionGenerator = p_reactionGenerator;
        originalReactant = p_speciesSeed;
        systemSnapshot.add(initialStatus);
        
        
        //#]
    }
    public  ReactionSystem() {
        {
            systemSnapshot=new LinkedList();
        }
    }
    
    //## operation adjustTimeStep(ReactionTime) 
    public ReactionTime adjustTimeStep(ReactionTime p_presentTS) {
        //#[ operation adjustTimeStep(ReactionTime) 
        double time = p_presentTS.getTime();
        String unit = p_presentTS.getUnit();
        double maxCPS = -1;
        PresentStatus ps = getPresentStatus();
        for (Iterator iter = getOriginalReactant().iterator(); iter.hasNext(); ) {
          	Species reactant = (Species)iter.next();
            SpeciesStatus ss = ps.getSpeciesStatus(reactant);
            if (ss.getSpecies().getName().equals("C4H10")) {
        	    double convPerStep = -ss.getFlux()/ss.getConcentration();
            	maxCPS = convPerStep;
        	}
        }
        
        ReactionTime newStep = p_presentTS;
        if (maxCPS > 1e-2) {
         	newStep = new ReactionTime(10,unit);
        }
        else if (maxCPS > 1e-3) {
         	newStep = new ReactionTime(50,unit);
        }
        else {
         	newStep = new ReactionTime(100,unit);
        }
        
        return newStep;
          /*      
          if (maxCPS == -1 || (maxCPS < upLim && maxCPS > lowLim)) return p_presentTS;
                
          if (maxCPS >= upLim) {
          	int order = (int)(Math.log(maxCPS/upLim)/Math.log(10));
          	ReactionTime newStep = new ReactionTime(time/Math.pow(10, order),unit);
          	System.out.println("Present CPS: " + String.valueOf(maxCPS));
          	System.out.println("order = " + String.valueOf(order));
          	System.out.println("new step = " + newStep);
          	return newStep;
          }
          else {
          	int order = (int)(Math.log(lowLim/maxCPS)/Math.log(10));
          	ReactionTime newStep = new ReactionTime(time*Math.pow(10, order),unit);
         	return newStep;
          }
        */
        /*double time = p_presentTS.getTime();
        String unit = p_presentTS.getUnit();
        double ratio = calculatePresentRadicalConcentration()/calculatePresentMoleculeConcentration();
        double largestRatio = 1E-4;
        
        if (ratio < largestRatio) return p_presentTS;
        else {
        	System.out.println("Radical/Molecule = "+ String.valueOf(ratio));
        	int order = (int)(Math.log(ratio/largestRatio)/Math.log(10));
        	double level = ratio/largestRatio;
        	double newT = 100;
        	if (level > 1000) newT /= 1.0E4;
        	else if (level > 100) newT /= 1.0E3;
        	else if (level > 10) newT /= 1.0E2;
        	else if (level > 1) newT /= 1.0E1;
        	System.out.println("New time step " + String.valueOf(newT));
            ReactionTime newStep = new ReactionTime(newT,unit);
            return newStep;
        }
        */
        
        
        
        
        
        
        
        //#]
    }
    
    //## operation appendUnreactedSpeciesStatus(SystemSnapshot,Temperature) 
    public void appendUnreactedSpeciesStatus(SystemSnapshot p_systemSnapshot, Temperature p_temperature) {
        //#[ operation appendUnreactedSpeciesStatus(SystemSnapshot,Temperature) 
        if (!(reactionModel instanceof CoreEdgeReactionModel)) return;
        
        CoreEdgeReactionModel model = (CoreEdgeReactionModel)reactionModel;
        
        HashSet ur = model.getUnreactedReactionSet();
        double [] unreactedFlux = new double[SpeciesDictionary.getInstance().size()+1];
        for (Iterator iur = ur.iterator(); iur.hasNext();) {
        	Reaction r = (Reaction)iur.next();
        	double flux = 0;
        	if (r instanceof TemplateReaction) {
        		flux = ((TemplateReaction)r).calculatePDepRate(p_temperature);
        	}
        	else {
        	 	flux = r.calculateRate(p_temperature);
        	}
        	if (flux > 0) {
        		for (Iterator rIter=r.getReactants(); rIter.hasNext();) {
        		    ChemGraph cg = (ChemGraph)rIter.next();
        		    Species spe = cg.getSpecies();
        		    double conc = (p_systemSnapshot.getSpeciesStatus(spe)).getConcentration();
        			if (conc<0)
        				throw new NegativeConcentrationException(spe.getName() + ": " + String.valueOf(conc));
        		    flux *= conc;
        
        		}
        
        		for (Iterator rIter=r.getProducts(); rIter.hasNext();) {
        		    ChemGraph cg = (ChemGraph)rIter.next();
        		    Species spe = cg.getSpecies();
        			if (model.containsAsUnreactedSpecies(spe)) {
        				unreactedFlux[spe.getID()] += flux;
        			}
        		}
        
        	}
        	else {
        		throw new NegativeRateException(r.toChemkinString() + ": " + String.valueOf(flux));
        	}
        }
        
        HashSet us = model.getUnreactedSpeciesSet();
        for (Iterator ius = us.iterator(); ius.hasNext();) {
        	Species spe = (Species)ius.next();
        	double flux = unreactedFlux[spe.getID()];
        	if (flux<0)
        		throw new NegativeRateException();
        
        	SpeciesStatus speStatus = new SpeciesStatus(spe, 0, 0, flux);
        	p_systemSnapshot.putSpeciesStatus(speStatus);
        }
        
        return;
        
        
        
        
        
        
        //#]
    }
    
    //## operation calculatePresentMoleculeConcentration() 
    public double calculatePresentMoleculeConcentration() {
        //#[ operation calculatePresentMoleculeConcentration() 
        double total = 0;
        
        PresentStatus ps = getPresentStatus();
        for (Iterator iter = ps.getSpeciesStatus(); iter.hasNext(); ) {
        	SpeciesStatus ss = (SpeciesStatus)iter.next();
        	Species spe = ss.getSpecies();
        	if (!spe.isRadical()) {
        		total += ss.getConcentration();
        	}
        }
        
        return total;
        
        //#]
    }
    
    //## operation calculatePresentRadicalConcentration() 
    public double calculatePresentRadicalConcentration() {
        //#[ operation calculatePresentRadicalConcentration() 
        double total = 0;
        
        PresentStatus ps = getPresentStatus();
        for (Iterator iter = ps.getSpeciesStatus(); iter.hasNext(); ) {
        	SpeciesStatus ss = (SpeciesStatus)iter.next();
        	Species spe = ss.getSpecies();
        	if (spe.isRadical()) {
        		total += ss.getConcentration();
        	}
        }
        
        return total;
        
        //#]
    }
    
    //## operation cleanDynamicSimulator() 
    public void cleanDynamicSimulator() {
        //#[ operation cleanDynamicSimulator() 
        getDynamicSimulator().clean();
        //#]
    }
    
    //## operation enlargeReactionModel() 
    public String enlargeReactionModel() {
        //#[ operation enlargeReactionModel() 
        if (reactionModelEnlarger == null) throw new NullPointerException("ReactionModelEnlarger");
        
        String species = reactionModelEnlarger.enlargeReactionModel(this);
        
        return species;
        //#]
    }
    
    //## operation findMainChannel(Species,SystemSnapshot) 
    public Reaction findMainChannel(Species p_species, SystemSnapshot p_systemSnapshot) {
        //#[ operation findMainChannel(Species,SystemSnapshot) 
        ReactionTime rt = p_systemSnapshot.getTime();
        Temperature temp = getTemperature(rt);
        double maxFlux = 0;
        Reaction maxReaction = null;
        
        HashSet rs = getReactionModel().getReactionSet();
        for (Iterator iter = rs.iterator(); iter.hasNext(); ) {
        	Reaction rxn = (Reaction)iter.next();
        	double rflux = 0;
        	for (Iterator pIter = rxn.getProducts(); pIter.hasNext(); ) {
        		Species spe = ((ChemGraph)pIter.next()).getSpecies();
        		if (spe.equals(p_species)) {
        			double flux = rxn.calculateRate(temp);
        			if (rxn instanceof TemplateReaction) {
        				flux = ((TemplateReaction)rxn).calculatePDepRate(temp);
        			}
        			else if (rxn instanceof ThirdBodyReaction) {
        				flux *= ((ThirdBodyReaction)rxn).calculateThirdBodyCoefficient(p_systemSnapshot);
        			}
        			for (Iterator rIter = rxn.getReactants(); rIter.hasNext(); ) {
        				Species reactant = ((ChemGraph)rIter.next()).getSpecies();
        				double concentration = p_systemSnapshot.getSpeciesStatus(reactant).getConcentration();
        				flux *= concentration;
        			}
        			rflux += flux;
        		}
        	}
        	if (rflux > maxFlux) {
        		maxFlux = rflux;
        		maxReaction = rxn;
        	}
        }
        
        System.out.println("The main pathway to generate " + p_species.getName() + " is ");
        System.out.println(maxReaction);
        System.out.println("The max flux is " + String.valueOf(maxFlux));
        
        return maxReaction;
        
        
        
        
        //#]
    }
    
    //## operation getColliders() 
    public void getColliders() {
        //#[ operation getColliders() 
        //#]
    }
    
    //## operation getInitialConcentration(Species) 
    public double getInitialConcentration(Species p_species) {
        //#[ operation getInitialConcentration(Species) 
        if (p_species == null) throw new NullPointerException();
        
        InitialStatus is = getInitialStatus();
        SpeciesStatus ss = is.getSpeciesStatus(p_species);
        
        return ss.getConcentration();
        //#]
    }
    
    //## operation getInitialReactionTime() 
    public ReactionTime getInitialReactionTime() {
        //#[ operation getInitialReactionTime() 
        return getInitialStatus().getTime();
        //#]
    }
    
    //## operation getPresentConcentration(Species) 
    public double getPresentConcentration(Species p_species) {
        //#[ operation getPresentConcentration(Species) 
        if (p_species == null) throw new NullPointerException();
        
        PresentStatus ps = getPresentStatus();
        SpeciesStatus ss = ps.getSpeciesStatus(p_species);
        
        return ss.getConcentration();
        //#]
    }
    
    //## operation getPresentConversion(Species) 
    public double getPresentConversion(Species p_species) {
        //#[ operation getPresentConversion(Species) 
        if (p_species == null) throw new NullPointerException();
        
        double C0 = getInitialConcentration(p_species);
        double C = getPresentConcentration(p_species);
        
        if (C>C0) return -1;
        
        return (1-C/C0);
        //#]
    }
    
    //## operation getPresentPressure() 
    public Pressure getPresentPressure() {
        //#[ operation getPresentPressure() 
        return getPressure(getPresentStatus().getTime());
        //#]
    }
    
    //## operation getPresentStatus() 
    public PresentStatus getPresentStatus() {
        //#[ operation getPresentStatus() 
        SystemSnapshot ss = (SystemSnapshot)systemSnapshot.getLast();
        return new PresentStatus(ss);
        
        
        
        
        
        
        //#]
    }
    
    //## operation getPresentTemperature() 
    public Temperature getPresentTemperature() {
        //#[ operation getPresentTemperature() 
        return getTemperature(getPresentStatus().getTime());
        //#]
    }
    
    //## operation getPressure(ReactionTime) 
    public Pressure getPressure(ReactionTime p_time) {
        //#[ operation getPressure(ReactionTime) 
        return getPressureModel().getPressure(p_time);
        //#]
    }
    
    //## operation getRmin() 
    public double getRmin() {
        //#[ operation getRmin() 
        RateBasedVT vt = (RateBasedVT)(getFinishController().getValidityTester());
        double Rmin = vt.calculateRmin(getPresentStatus());
        return Rmin;
        //#]
    }
    
    //## operation getTemperature(ReactionTime) 
    public Temperature getTemperature(ReactionTime p_time) {
        //#[ operation getTemperature(ReactionTime) 
        return getTemperatureModel().getTemperature(p_time);
        //#]
    }
    
    //## operation hasPrimaryReactionLibrary() 
    public boolean hasPrimaryReactionLibrary() {
        //#[ operation hasPrimaryReactionLibrary() 
        if (primaryReactionLibrary == null) return false;
        return (primaryReactionLibrary.size() > 0);
        //#]
    }
    
    //## operation identifyColliders() 
    public HashMap identifyColliders() {
        //#[ operation identifyColliders() 
        return getInitialStatus().identifyColliders();
        //#]
    }
    
    //## operation initializeCoreEdgeModelWithPRL() 
    public void initializeCoreEdgeModelWithPRL() {
        //#[ operation initializeCoreEdgeModelWithPRL() 
        initializeCoreEdgeModelWithoutPRL();
        
        CoreEdgeReactionModel cerm = (CoreEdgeReactionModel)reactionModel;
        
        HashSet primarySpeciesSet = primaryReactionLibrary.getSpeciesSet();
        HashSet primaryReactionSet = primaryReactionLibrary.getReactionSet();
        cerm.addReactedSpeciesSet(primarySpeciesSet);
        cerm.addPrimaryReactionSet(primaryReactionSet);
        
        HashSet newReactions = getReactionGenerator().react(cerm.getReactedSpeciesSet());
        
        for (Iterator iter = newReactions.iterator(); iter.hasNext();) {
        	Reaction r = (Reaction)iter.next();
        	System.out.println(r.toString());
        }   
        
        cerm.addReactionSet(newReactions);
        /*
        HashSet allNewReactions = new HashSet();
        HashSet rs = cerm.getReactedSpeciesSet();
        for (Iterator iter = primarySpeciesSet.iterator(); iter.hasNext(); ) {
        	Species spe = (Species)iter.next();
        	HashSet newReactions = getReactionGenerator().react(rs,spe);
        	allNewReactions.addAll(newReactions);
        }
        
        
        //add in all the species and reactions from PRL as reacted species/reactions into model
        cerm.addReactedSpeciesSet(primarySpeciesSet);
        HashSet primaryReactionSet = primaryReactionLibrary.getReactionSet();
        try {
        	cerm.addReactedReactionSet(primaryReactionSet);
        }
        catch (InvalidReactedReactionException e) {
        	System.out.println("During adding primary reaction library: find a invalid reacted reaction in library:" );
        	System.out.println(e.getMessage());
        	System.exit(0);
        }
        
        // add the new reactions accordingly
        cerm.addReactionSet(allNewReactions);
        */
        return;
        
        
        
        
        
        
        //#]
    }
    
    //## operation initializeCoreEdgeModelWithoutPRL() 
    protected void initializeCoreEdgeModelWithoutPRL() {
        //#[ operation initializeCoreEdgeModelWithoutPRL() 
        HashSet reactionSet = getReactionGenerator().react(originalReactant);
        reactionModel = new CoreEdgeReactionModel(new HashSet(originalReactant),reactionSet);
        
        if (reactionModel.isEmpty()) {
        	HashSet us = ((CoreEdgeReactionModel)reactionModel).getUnreactedSpeciesSet();
        	HashSet rs = ((CoreEdgeReactionModel)reactionModel).getReactedSpeciesSet();
        	HashSet newReactions = new HashSet();
        
        	for (Iterator iter = us.iterator(); iter.hasNext(); ) {
        		Species spe = (Species)iter.next();
        		rs.add(spe);
        		newReactions.addAll(getReactionGenerator().react(rs,spe));
        		iter.remove();
        	}
        
        	((CoreEdgeReactionModel)reactionModel).addReactionSet(newReactions);
        	((CoreEdgeReactionModel)reactionModel).moveFromUnreactedToReactedReaction();
        }
        
        return;
        
        
        
        
        
        
        
        
        //#]
    }
    
    //## operation initializeCoreEdgeReactionModel() 
    public void initializeCoreEdgeReactionModel() {
        //#[ operation initializeCoreEdgeReactionModel() 
        if (hasPrimaryReactionLibrary()) initializeCoreEdgeModelWithPRL();
        else initializeCoreEdgeModelWithoutPRL();
        
        
        
        //#]
    }
    
    //## operation initializePDepNetwork() 
    public void initializePDepNetwork() {
        //#[ operation initializePDepNetwork() 
        for (Iterator iter = PDepNetwork.getDictionary().values().iterator(); iter.hasNext(); ) {
        	PDepNetwork pdn = (PDepNetwork)iter.next();
        	pdn.runPDepCalculation(this);
        }
        //#]
    }
    
    //## operation isFinished() 
    public boolean isFinished() {
        //#[ operation isFinished() 
        return (isModelValid() && isReactionTerminated());
        //#]
    }
    
    //## operation isModelValid() 
    public boolean isModelValid() {
        //#[ operation isModelValid() 
        return finishController.isModelValid();
        //#]
    }
    
    //## operation isReactionTerminated() 
    public boolean isReactionTerminated() {
        //#[ operation isReactionTerminated() 
        return finishController.isReactionTerminated();
        //#]
    }
    
    //## operation outputAllPathways(Species,LinkedList,SystemSnapshot,Temperature) 
    public static void outputAllPathways(Species p_species, LinkedList p_reactionList, SystemSnapshot p_systemSnapshot, Temperature p_temperature) {
        //#[ operation outputAllPathways(Species,LinkedList,SystemSnapshot,Temperature) 
        ReactionTime rt = p_systemSnapshot.getTime();
        Temperature temp = p_temperature;
        double maxFlux = 0;
        Reaction maxReaction = null;
        
        System.out.println("the consumption paths for " + p_species.getName());
        for (Iterator iter = p_reactionList.iterator(); iter.hasNext(); ) {
        	Reaction rxn = (Reaction)iter.next();
        	if (rxn.containsAsReactant(p_species)) { 
        		double flux;
        		if (rxn instanceof TemplateReaction) {
        			flux = ((TemplateReaction)rxn).calculatePDepRate(temp);
        		}
        		else if (rxn instanceof PDepNetReaction) {
        			flux = ((PDepNetReaction)rxn).getRate();
        		}
        		else {
        			flux = rxn.calculateRate(temp);
        		}
        		
        		if (rxn instanceof ThirdBodyReaction) {
        			flux *= ((ThirdBodyReaction)rxn).calculateThirdBodyCoefficient(p_systemSnapshot);
        		}
        
        		System.out.print(rxn.getStructure().toString() + '\t' + String.valueOf(flux));
        		for (Iterator rIter = rxn.getReactants(); rIter.hasNext(); ) {
        			Species reactant = ((ChemGraph)rIter.next()).getSpecies();
        			double concentration = p_systemSnapshot.getSpeciesStatus(reactant).getConcentration();
        			System.out.print('\t' + String.valueOf(concentration));
        			flux *= concentration;
        		}
        		System.out.println('\t' + String.valueOf(-flux));
        
        	}
        }
        System.out.println("the formationtion paths for " + p_species.getName());
        for (Iterator iter = p_reactionList.iterator(); iter.hasNext(); ) {
        	Reaction rxn = (Reaction)iter.next();
        	if (rxn.containsAsProduct(p_species)) { 
        		double flux;
        		if (rxn instanceof TemplateReaction) {
        			flux = ((TemplateReaction)rxn).calculatePDepRate(temp);
        		}
        		else if (rxn instanceof PDepNetReaction) {
        			flux = ((PDepNetReaction)rxn).getRate();
        		}
        		else {
        			flux = rxn.calculateRate(temp);
        		}
        		
        		if (rxn instanceof ThirdBodyReaction) {
        			flux *= ((ThirdBodyReaction)rxn).calculateThirdBodyCoefficient(p_systemSnapshot);
        		}
        		System.out.print(rxn.getStructure().toString() + '\t' + String.valueOf(flux));
        		for (Iterator rIter = rxn.getReactants(); rIter.hasNext(); ) {
        			Species reactant = ((ChemGraph)rIter.next()).getSpecies();
        			double concentration = p_systemSnapshot.getSpeciesStatus(reactant).getConcentration();
        			System.out.print('\t' + String.valueOf(concentration));
        			flux *= concentration;
        		}
        		System.out.println('\t' + String.valueOf(flux));
        
        	}
        }
        
        return;
        
        
        
        
        //#]
    }
    
    //## operation outputReactionFlux(SystemSnapshot) 
    public void outputReactionFlux(SystemSnapshot p_systemSnapshot) {
        //#[ operation outputReactionFlux(SystemSnapshot) 
        ReactionTime rt = p_systemSnapshot.getTime();
        Temperature t = getTemperature(rt);
        
        HashSet speSet = new HashSet();
        for (Iterator iter = getReactionModel().getReaction(); iter.hasNext(); ) {
        	Reaction rxn = (Reaction)iter.next();
        	double k = rxn.calculateRate(t);
        	if (rxn instanceof ThirdBodyReaction) {
        		k *= ((ThirdBodyReaction)rxn).calculateThirdBodyCoefficient(p_systemSnapshot);
        	}
        	double flux = k;
        	for (Iterator rIter = rxn.getReactants(); rIter.hasNext(); ) {
        		Species spe = ((ChemGraph)rIter.next()).getSpecies();
        		double concentration = p_systemSnapshot.getSpeciesStatus(spe).getConcentration();
        		flux *= concentration;
        	}
        	if (flux>1E-7) {
        		System.out.println(rxn.toString() + "\t" + String.valueOf(flux) + '\t' + String.valueOf(rxn.calculateHrxn(t)) + '\t' + String.valueOf(rxn.calculateKeq(t)));
        		for (double temp = 400; temp<1200; temp = temp + 50) {
        			double rate = rxn.calculateRate(new Temperature(temp,"K"));
        			System.out.println("Temp = " + String.valueOf(temp) + "\tRate = " + String.valueOf(rate));
        		}
        		for (Iterator rIter = rxn.getReactants(); rIter.hasNext(); ) {
        			Species spe = ((ChemGraph)rIter.next()).getSpecies();
        			speSet.add(spe);
        		}
                for (Iterator rIter = rxn.getProducts(); rIter.hasNext(); ) {
        			Species spe = ((ChemGraph)rIter.next()).getSpecies();
        			speSet.add(spe);
        		}
        	}
        }
        
        for (Iterator iter = speSet.iterator(); iter.hasNext(); ) {
        	Species spe = (Species)iter.next();
        	System.out.println(spe.getName()+"("+String.valueOf(spe.getID())+"): " + spe.getThermoData().toString());
        }
        
        
        
        
        
        
        //#]
    }
    
    //## operation printConcentrationProfile(LinkedList) 
    public String printConcentrationProfile(LinkedList p_speciesList) {
        //#[ operation printConcentrationProfile(LinkedList) 
        if (p_speciesList == null) throw new NullPointerException();
        
        
        if (p_speciesList.isEmpty()) return "EMPTY species list";
        
        // check the validity of p_speciesList and print the title line
        System.out.print("Time");
        int size = p_speciesList.size();
        for (int i=0; i<size; i++) {
        	Species spe = (Species)p_speciesList.get(i);
        	if (!spe.repOk()) throw new InvalidSpeciesException();
         	String name = spe.getName();
         	System.out.print('\t' + name);
        }
        System.out.println();
        
        Iterator iter = getSystemSnapshot();
        while (iter.hasNext()) {
        	SystemSnapshot ss = (SystemSnapshot)iter.next();
        	System.out.print(String.valueOf(ss.getTime().getTime()));
        	for (int i=0; i<size; i++) {
        		Species spe = (Species)p_speciesList.get(i);
        
         		if (spe != null) {
         			SpeciesStatus speSta = ss.getSpeciesStatus(spe);
         			double conc = 0;
         			if (speSta != null) conc = speSta.getConcentration();
         			System.out.print('\t' + String.valueOf(conc));
         		}
         	}
         	System.out.println();
        }
        
        return "END";
        //#]
    }
    
    //## operation printMoleFractionProfile(LinkedList) 
    public String printMoleFractionProfile(LinkedList p_speciesList) {
        //#[ operation printMoleFractionProfile(LinkedList) 
        if (p_speciesList == null) throw new NullPointerException();
        
        if (p_speciesList.isEmpty()) return "EMPTY species list";
        
        // check the validity of p_speciesList and print the title line
        System.out.print("Time");
        int size = p_speciesList.size();
        for (int i=0; i<size; i++) {
        	Species spe = (Species)p_speciesList.get(i);
        	if (spe != null) {
        		if (!spe.repOk()) throw new InvalidSpeciesException();
        	 	String name = spe.getName();
        	 	System.out.print('\t' + name);
        	 }
        }
        System.out.println();
        
        Iterator iter = getSystemSnapshot();
        while (iter.hasNext()) {
        	SystemSnapshot ss = (SystemSnapshot)iter.next();
        	double totalMole = ss.getTotalMole();
        	System.out.print(String.valueOf(ss.getTime().getTime()));
        	for (int i=0; i<size; i++) {
        		Species spe = (Species)p_speciesList.get(i);
        		if (spe != null) {
        	 		SpeciesStatus speSta = ss.getSpeciesStatus(spe);
        	 		double mf;
        	 		if (speSta==null) mf = 0;
        	 		else mf = speSta.getConcentration()/totalMole;
                                System.out.print('\t' + String.valueOf(mf));
        	 	}
         	}
         	System.out.println();
        }
        
        return "END";
        //#]
    }
    
    //## operation resetSystemSnapshot() 
    public void resetSystemSnapshot() {
        //#[ operation resetSystemSnapshot() 
        systemSnapshot.clear();
        systemSnapshot.add(initialStatus);
        //#]
    }
    
    //## operation solveReactionSystem(ReactionTime,ReactionTime,boolean,boolean,boolean) 
    public void solveReactionSystem(ReactionTime p_beginTime, ReactionTime p_endTime, boolean p_initialization, boolean p_reactionChanged, boolean p_conditionChanged) {
        //#[ operation solveReactionSystem(ReactionTime,ReactionTime,boolean,boolean,boolean) 
        Temperature t = getTemperatureModel().getTemperature(p_beginTime);
        Pressure p = getPressureModel().getPressure(p_beginTime); 
        
        if (p_reactionChanged || p_initialization || p_conditionChanged) {
        	initializePDepNetwork();
        	p_reactionChanged = true;
        }
        
        SystemSnapshot beginStatus = (SystemSnapshot)(getSystemSnapshotEnd().next());
        
        if (!beginStatus.getTime().equals(p_beginTime)) throw new InvalidBeginStatusException();
        
        SystemSnapshot present = getDynamicSimulator().solve(p_initialization, getReactionModel(), p_reactionChanged, beginStatus, p_beginTime, p_endTime, t, p, p_conditionChanged);
        
        appendUnreactedSpeciesStatus(present, t);
        
        systemSnapshot.add(present);
        
        
        return;
        //#]
    }
    
    //## operation toString() 
    public String toString() {
        //#[ operation toString() 
        return reactionModel.toString();
        //#]
    }
    
    public HashSet getOriginalReactant() {
        return originalReactant;
    }
    
    public DynamicSimulator getDynamicSimulator() {
        return dynamicSimulator;
    }
    
    public void setDynamicSimulator(DynamicSimulator p_DynamicSimulator) {
        dynamicSimulator = p_DynamicSimulator;
    }
    
    public FinishController getFinishController() {
        return finishController;
    }
    
    public void __setFinishController(FinishController p_FinishController) {
        finishController = p_FinishController;
    }
    
    public void _setFinishController(FinishController p_FinishController) {
        if(finishController != null)
            finishController.__setReactionSystem(null);
        __setFinishController(p_FinishController);
    }
    
    public void setFinishController(FinishController p_FinishController) {
        if(p_FinishController != null)
            p_FinishController._setReactionSystem(this);
        _setFinishController(p_FinishController);
    }
    
    public void _clearFinishController() {
        finishController = null;
    }
    
    public InitialStatus getInitialStatus() {
        return initialStatus;
    }
    
    public void setInitialStatus(InitialStatus p_InitialStatus) {
        initialStatus = p_InitialStatus;
    }
    
    public PressureModel getPressureModel() {
        return pressureModel;
    }
    
    public void setPressureModel(PressureModel p_PressureModel) {
        pressureModel = p_PressureModel;
    }
    
    public PrimaryReactionLibrary getPrimaryReactionLibrary() {
        return primaryReactionLibrary;
    }
    
    public void setPrimaryReactionLibrary(PrimaryReactionLibrary p_PrimaryReactionLibrary) {
        primaryReactionLibrary = p_PrimaryReactionLibrary;
    }
    
    public ReactionGenerator getReactionGenerator() {
        return reactionGenerator;
    }
    
    public void setReactionGenerator(ReactionGenerator p_ReactionGenerator) {
        reactionGenerator = p_ReactionGenerator;
    }
    
    public ReactionModel getReactionModel() {
        return reactionModel;
    }
    
    public void setReactionModel(ReactionModel p_ReactionModel) {
        reactionModel = p_ReactionModel;
    }
    
    public ReactionModelEnlarger getReactionModelEnlarger() {
        return reactionModelEnlarger;
    }
    
    public void setReactionModelEnlarger(ReactionModelEnlarger p_ReactionModelEnlarger) {
        reactionModelEnlarger = p_ReactionModelEnlarger;
    }
    
    public ListIterator getSystemSnapshot() {
        ListIterator iter=systemSnapshot.listIterator(0);
        return iter;
    }
    
    public ListIterator getSystemSnapshotEnd() {
        return systemSnapshot.listIterator(systemSnapshot.lastIndexOf(systemSnapshot.getLast()));
    }
    
    public SystemSnapshot newSystemSnapshot() {
        SystemSnapshot newSystemSnapshot = new SystemSnapshot();
        systemSnapshot.add(newSystemSnapshot);
        return newSystemSnapshot;
    }
    
    public void deleteSystemSnapshot(SystemSnapshot p_SystemSnapshot) {
        systemSnapshot.remove(p_SystemSnapshot);
        p_SystemSnapshot=null;
    }
    
    public TemperatureModel getTemperatureModel() {
        return temperatureModel;
    }
    
    public void setTemperatureModel(TemperatureModel p_TemperatureModel) {
        temperatureModel = p_TemperatureModel;
    }
    
}
/*********************************************************************
	File Path	: RMG\RMG\jing\rxnSys\ReactionSystem.java
*********************************************************************/
