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
import jing.param.Temperature;

//## package jing::rxnSys 

//----------------------------------------------------------------------------
// jing\rxnSys\CoreEdgeReactionModel.java                                                                  
//----------------------------------------------------------------------------

//## class CoreEdgeReactionModel 
public class CoreEdgeReactionModel implements ReactionModel {
    
    protected Core core;
    protected Edge edge;
    
    // Constructors
    
    //## operation CoreEdgeReactionModel() 
    public CoreEdgeReactionModel() {
        initRelations();
        //#[ operation CoreEdgeReactionModel() 
        //#]
    }
    // Argument HashSetp_reactedSpeciesSet : 
    /**
    reacted species set
    */
    // Argument HashSetp_reactionSet : 
    /**
    All the reaction generated from the reacted species set
    */
    //## operation CoreEdgeReactionModel(HashSet,HashSet) 
    public  CoreEdgeReactionModel(HashSet p_reactedSpeciesSet, HashSet p_reactionSet) {
        initRelations();
        //#[ operation CoreEdgeReactionModel(HashSet,HashSet) 
        core = newCore();
        core.setSpeciesSet(p_reactedSpeciesSet);
        addReactionSet(p_reactionSet);
        
        
        //#]
    }
    
    //## operation addPrimaryReactionSet(HashSet) 
    public void addPrimaryReactionSet(HashSet p_reactionSet) {
        //#[ operation addPrimaryReactionSet(HashSet) 
        for (Iterator iter = p_reactionSet.iterator(); iter.hasNext(); ) {
        	Reaction rxn = (Reaction)iter.next();
        	int rxnType = categorizeReaction(rxn);
        	// here the same reaction has been generated from template, we need to remove that one, and then add the one from PRL
        	if (rxnType == 1) {
        		if (containsAsReactedReaction(rxn)) {
        			// remove the present one
        			for (Iterator rIter = getReactedReactionSet().iterator(); rIter.hasNext(); ) {
        				Reaction r = (Reaction)rIter.next();
        				if (r.equals(rxn)) rIter.remove();
        			}
        		}
        		addReactedReaction(rxn);
        	}
        	else if (rxnType == -1) {
        		if (containsAsUnreactedReaction(rxn)) {
        			// remove the present one
        			for (Iterator rIter = getUnreactedReactionSet().iterator(); rIter.hasNext(); ) {
        				Reaction r = (Reaction)rIter.next();
        				if (r.equals(rxn)) rIter.remove();
        			}
        
        		}
        		addUnreactedReaction(rxn);
        	}
        
        }
        
        return;
        
        
        //#]
    }
    
    //## operation addReactedReaction(Reaction) 
    public void addReactedReaction(Reaction p_reaction) throws InvalidReactedReactionException {
        //#[ operation addReactedReaction(Reaction) 
        if (isReactedReaction(p_reaction)) {
			getReactedReactionSet().add(p_reaction);
			//if (p_reaction.isForward()) System.out.println(p_reaction.toRestartString());
        }
        else throw new InvalidReactedReactionException(p_reaction.toString());
        //#]
    }
    
    //## operation addReactedReactionSet(HashSet) 
    public void addReactedReactionSet(HashSet p_reactedReactionSet) throws InvalidReactedReactionException {
        //#[ operation addReactedReactionSet(HashSet) 
        try {
        	for (Iterator iter = p_reactedReactionSet.iterator(); iter.hasNext(); ) {
        		Reaction r = (Reaction)iter.next();
        		addReactedReaction(r);
        	}
        }
        catch (InvalidReactedReactionException e) {
        	throw new InvalidReactedReactionException(e.getMessage());
        }   
        
        
        //#]
    }
    
    //## operation addReactedSpecies(Species) 
    public void addReactedSpecies(Species p_species) {
        //#[ operation addReactedSpecies(Species) 
        if (containsAsUnreactedSpecies(p_species)) {
        	moveFromUnreactedToReactedSpecies(p_species);
        }
        else {
        	getReactedSpeciesSet().add(p_species);
        }
        
        moveFromUnreactedToReactedReaction();
        //#]
    }
    
    //## operation addReactedSpeciesSet(HashSet) 
    public void addReactedSpeciesSet(HashSet p_reactedSpeciesSet) {
        //#[ operation addReactedSpeciesSet(HashSet) 
        boolean added = false;
		HashSet rs = getReactedSpeciesSet();
        for (Iterator iter = p_reactedSpeciesSet.iterator(); iter.hasNext(); ) {
        	Species spe = (Species)iter.next();
        	if (containsAsUnreactedSpecies(spe)) {
        		moveFromUnreactedToReactedSpecies(spe);
        	}
        	else {
        		rs.add(spe);
				if (!added){
					Iterator originalSpeciesIterator = rs.iterator();
					while (originalSpeciesIterator.hasNext()){
						Species temp = (Species)originalSpeciesIterator.next();
						if(spe.equals(temp)){
							temp.setName(temp.getName());
						}
					}
				}
        	}
        }
        
        moveFromUnreactedToReactedReaction();
        
        
        //#]
    }
    
    //## operation addReactionSet(HashSet) 
    public void addReactionSet(HashSet p_reactionSet) {
        //#[ operation addReactionSet(HashSet) 
        for (Iterator iter = p_reactionSet.iterator(); iter.hasNext(); ) {
        	Reaction rxn = (Reaction)iter.next();
        	int rxnType = categorizeReaction(rxn);
        	if (rxnType == 1) {
        		addReactedReaction(rxn);
        	}
        	else if (rxnType == -1) {
        		addUnreactedReaction(rxn);
        	}
        }
        
        return;
        //#]
    }
    
    //## operation addUnreactedReaction(Reaction) 
    public void addUnreactedReaction(Reaction p_reaction) throws InvalidUnreactedReactionException {
        //#[ operation addUnreactedReaction(Reaction) 
        if (isUnreactedReaction(p_reaction)) getUnreactedReactionSet().add(p_reaction);
        else throw new InvalidUnreactedReactionException(p_reaction.toString());
        //#]
    }
    
    //## operation addUnreactedSpecies(Species) 
    public void addUnreactedSpecies(Species p_species) {
        //#[ operation addUnreactedSpecies(Species) 
        if (containsAsReactedSpecies(p_species)) {
        	// this is not a unreacted species
        	System.out.println("This is a reacted species " + p_species.getName());
        	System.out.println("Can't add it into unreacted species set!");
        }
        else {
        	getUnreactedSpeciesSet().add(p_species);
        }
        //#]
    }
	
//	## operation addUnreactedSpecies(Species) 
    public void addUnreactedSpeciesSet(HashSet p_species) {
        //#[ operation addUnreactedSpecies(Species)
		Iterator speciesIter = p_species.iterator();
		while (speciesIter.hasNext()){
			Species species = (Species)speciesIter.next();
			if (containsAsReactedSpecies(species)) {
	        	// this is not a unreacted species
	        	System.out.println("This is a reacted species " + species.getName());
	        	System.out.println("Can't add it into unreacted species set!");
	        }
	        else {
	        	getUnreactedSpeciesSet().add(species);
	        }
		}
        
        //#]
    }
    
    /**
    Requires: the reactionSpecies set and unreactedSpecies set has been defined properly.
    Effects: according to the reaction's reactants and products, categorize the pass-in reaction as reacted reaction (return 1), or unreacted reaction(return -1), or reaction not in the model (return 0).
    Modifies:
    */
    //## operation categorizeReaction(Reaction) 
    protected int categorizeReaction(Reaction p_reaction) {
        //#[ operation categorizeReaction(Reaction) 
        Iterator iter = p_reaction.getReactants();
        while (iter.hasNext()) {
        	ChemGraph cg = (ChemGraph)iter.next();
        	Species spe = cg.getSpecies();
        	if (!containsAsReactedSpecies(spe)) 
        		return 0;
        }
        
        int type = 1;
        iter = p_reaction.getProducts();
        while (iter.hasNext()) {
        	ChemGraph cg = (ChemGraph)iter.next();
        	Species spe = cg.getSpecies();
        	if (!contains(spe)) {
        		// new unreacted species 
        		type = -1;
        		addUnreactedSpecies(spe);
        	}
        	else if (containsAsUnreactedSpecies(spe)) {
        		type = -1;
        	}
        }	                                          
        
        return type;
        //#]
    }
	
	/**
    Requires: the reactionSpecies set and unreactedSpecies set has been defined properly.
    Effects: according to the reaction's reactants and products, categorize the pass-in reaction as reacted reaction (return 1), or unreacted reaction(return -1), or reaction not in the model (return 0).
    Modifies:
    */
    //## operation categorizeReaction(Reaction) 
    public int categorizeReaction(Structure p_structure) {
        //#[ operation categorizeReaction(Reaction) 
        Iterator iter = p_structure.getReactants();
        while (iter.hasNext()) {
        	ChemGraph cg = (ChemGraph)iter.next();
        	Species spe = cg.getSpecies();
        	if (!containsAsReactedSpecies(spe)) 
        		return 0;
        }
        
        int type = 1;
        iter = p_structure.getProducts();
        while (iter.hasNext()) {
        	ChemGraph cg = (ChemGraph)iter.next();
        	Species spe = cg.getSpecies();
        	if (!contains(spe)) {
        		// new unreacted species 
        		type = -1;
        		addUnreactedSpecies(spe);
        	}
        	else if (containsAsUnreactedSpecies(spe)) {
        		type = -1;
        	}
        }	                                          
        
        return type;
        //#]
    }
    
    //## operation contains(Reaction) 
    public boolean contains(Reaction p_reaction) {
        //#[ operation contains(Reaction) 
        return (containsAsReactedReaction(p_reaction) || containsAsUnreactedReaction(p_reaction));
        //#]
    }
    
    //## operation contains(Species) 
    public boolean contains(Species p_species) {
        //#[ operation contains(Species) 
        return (containsAsReactedSpecies(p_species) || containsAsUnreactedSpecies(p_species));
        //#]
    }
    
    //## operation containsAsReactedReaction(Reaction) 
    public boolean containsAsReactedReaction(Reaction p_reaction) {
        //#[ operation containsAsReactedReaction(Reaction) 
        return getReactedReactionSet().contains(p_reaction);
        //#]
    }
    
    //## operation containsAsReactedSpecies(Species) 
    public boolean containsAsReactedSpecies(Species p_species) {
        //#[ operation containsAsReactedSpecies(Species) 
        return getReactedSpeciesSet().contains(p_species);
        //#]
    }
    
    //## operation containsAsUnreactedReaction(Reaction) 
    public boolean containsAsUnreactedReaction(Reaction p_reaction) {
        //#[ operation containsAsUnreactedReaction(Reaction) 
        return getUnreactedReactionSet().contains(p_reaction);
        //#]
    }
    
    //## operation containsAsUnreactedSpecies(Species) 
    public boolean containsAsUnreactedSpecies(Species p_species) {
        //#[ operation containsAsUnreactedSpecies(Species) 
        return getUnreactedSpeciesSet().contains(p_species);
        //#]
    }
    
    //## operation getReactedReactionSet() 
    public HashSet getReactedReactionSet() {
        //#[ operation getReactedReactionSet() 
        return getCore().getReactionSet();
        //#]
    }
    
    //## operation getReactedSpeciesSet() 
    public HashSet getReactedSpeciesSet() {
        //#[ operation getReactedSpeciesSet() 
        return getCore().getSpeciesSet();
        //#]
    }
    
    //## operation getReaction() 
    public Iterator getReaction() {
        //#[ operation getReaction() 
        return core.getReaction();
        //#]
    }
  
	 public LinkedList generatePDepReactionSet() {
	        //#[ operation generatePDepReactionSet() 
	        LinkedList nonPDepList = new LinkedList();
	        LinkedList pDepList = new LinkedList();
	        
	        HashSet pDepStructureSet = new HashSet();
	        
	        HashSet rSet = getReactionSet();
	        for (Iterator iter = rSet.iterator(); iter.hasNext(); ) {
	        	Reaction r = (Reaction)iter.next();
	        	if (r instanceof ThirdBodyReaction || r instanceof TROEReaction) {
	        		pDepList.add(r);
	        		pDepStructureSet.add(r.getStructure().generateSpeciesStructure());
	        		pDepStructureSet.add(r.getStructure().generateReverseStructure().generateSpeciesStructure());
	        	}
	        }
	        
	        for (Iterator iter = PDepNetwork.getDictionary().values().iterator(); iter.hasNext(); ) {
	        	PDepNetwork pdn = (PDepNetwork)iter.next();
	        	for (Iterator pdniter = pdn.getPDepNetReactionList(); pdniter.hasNext();) {
	        		PDepNetReaction pdnr = (PDepNetReaction)pdniter.next();
	        		Structure s = pdnr.getStructure().generateSpeciesStructure();
	        		if (!pdnr.reactantEqualsProduct() && !pDepStructureSet.contains(s) && isReactedReaction(pdnr)) {
	        			pDepList.add(pdnr);
	        			pDepStructureSet.add(s);
	        		}	
	        	}
	        }
	        
	        for (Iterator iter = getReactionSet().iterator(); iter.hasNext(); ) {
	        	Reaction r = (Reaction)iter.next();
	        	if (!r.reactantEqualsProduct() && !(r instanceof ThirdBodyReaction) && !(r instanceof TROEReaction)) {
	        		Structure s = r.getStructure();
	        		Structure ss = s.generateSpeciesStructure();
	        		Structure rs = s.generateReverseStructure();
	        		Structure rss = rs.generateSpeciesStructure();
	        		if (!pDepStructureSet.contains(ss) && !pDepStructureSet.contains(rss)) {
	        			nonPDepList.add(r);
	        		}                                   
	        	}
	        }
	        
	        LinkedList all = new LinkedList();
	        all.add(0,nonPDepList);
	        all.add(1,pDepList);
	        return all;
	        //#]
	    }
	 
    //## operation getReactionNumber() 
    public int getReactionNumber() {
        //#[ operation getReactionNumber() 
        return core.getReactionNumber();
        //#]
    }
    
    //## operation getReactionSet() 
    public HashSet getReactionSet() {
        //#[ operation getReactionSet() 
        return getReactedReactionSet();
        //#]
    }
    
    //## operation getSpecies() 
    public Iterator getSpecies() {
        //#[ operation getSpecies() 
        return core.getSpecies();
        //#]
    }
    
    //## operation getSpeciesNumber() 
    public int getSpeciesNumber() {
        //#[ operation getSpeciesNumber() 
        return core.getSpeciesNumber();
        //#]
    }
    
    //## operation getSpeciesSet() 
    public HashSet getSpeciesSet() {
        //#[ operation getSpeciesSet() 
        return getReactedSpeciesSet();
        //#]
    }
    
    //## operation getUnreactedReactionSet() 
    public HashSet getUnreactedReactionSet() {
        //#[ operation getUnreactedReactionSet() 
        return getEdge().getReactionSet();
        
        
        //#]
    }
    
    //## operation getUnreactedSpeciesSet() 
    public HashSet getUnreactedSpeciesSet() {
        //#[ operation getUnreactedSpeciesSet() 
        return getEdge().getSpeciesSet();
        
        
        //#]
    }
    
    //## operation isCoreEdgeConsistent() 
    public boolean isCoreEdgeConsistent() {
        //#[ operation isCoreEdgeConsistent() 
        return AbstractReactionModel.isDisjoint(core,edge);
        
        
        
        //#]
    }
    
    //## operation isEmpty() 
    public boolean isEmpty() {
        //#[ operation isEmpty() 
        return core.isEmpty();
        //#]
    }
    
    //## operation isReactedReaction(Reaction) 
    public boolean isReactedReaction(Reaction p_reaction) {
        //#[ operation isReactedReaction(Reaction) 
        return (categorizeReaction(p_reaction) == 1) ;
        //#]
    }
    
    //## operation isUnreactedReaction(Reaction) 
    public boolean isUnreactedReaction(Reaction p_reaction) {
        //#[ operation isUnreactedReaction(Reaction) 
        return (categorizeReaction(p_reaction) == -1) ;
        //#]
    }
    
    //## operation mergeBasedOnReactedSpecies(CoreEdgeReactionModel,CoreEdgeReactionModel) 
    public static CoreEdgeReactionModel mergeBasedOnReactedSpecies(CoreEdgeReactionModel p_cerm1, CoreEdgeReactionModel p_cerm2) {
        //#[ operation mergeBasedOnReactedSpecies(CoreEdgeReactionModel,CoreEdgeReactionModel) 
        // notice!!! 
        // here p_cerm1 is the primary one, which means every reacted thing in it will be guaranteed showing in the merge result
        // while p_cerm2 is the secondary one, which means if the reacted thing in it is already in p_cerm1, it wont be in the merged one
        HashSet rs1 = p_cerm1.getReactedSpeciesSet();
        HashSet us1 = p_cerm1.getUnreactedSpeciesSet();    
        HashSet rr1 = p_cerm1.getReactedReactionSet();
        HashSet ur1 = p_cerm1.getUnreactedReactionSet(); 
        
        HashSet rs2 = p_cerm2.getReactedSpeciesSet();
        HashSet us2 = p_cerm2.getUnreactedSpeciesSet();    
        HashSet rr2 = p_cerm2.getReactedReactionSet();
        HashSet ur2 = p_cerm2.getUnreactedReactionSet(); 
        
        HashSet rs = new HashSet(rs1);
        rs.addAll(rs2);
        HashSet us = new HashSet(us1);
        us.addAll(us2);
        
        // remove the one possibly in both reacted and unreacted species set from unreacted set
        for (Iterator iter = us.iterator(); iter.hasNext(); ) {
        	Species spe = (Species)iter.next();
        	if (rs.contains(spe)) {
        		iter.remove();
        	}
        }
        
        CoreEdgeReactionModel result = new CoreEdgeReactionModel();
        result.getCore().setSpeciesSet(rs);
        result.getEdge().setSpeciesSet(us);
        
        result.addReactionSet(rr1);
        result.addReactionSet(ur1);
        result.addReactionSet(rr2);
        result.addReactionSet(ur2);
        
        return result;
        
        
        //#]
    }
    
    //## operation moveFromUnreactedToReactedReaction() 
    public void moveFromUnreactedToReactedReaction() {
        //#[ operation moveFromUnreactedToReactedReaction() 
        HashSet ur = getUnreactedReactionSet();
        Iterator iter = ur.iterator();
        while (iter.hasNext()) {
        	Reaction r = (Reaction)iter.next();
        	if (categorizeReaction(r) == 1) {
        		addReactedReaction(r);
        		iter.remove();
        	}
        }
        
        return;
        //#]
    }
    
    //## operation moveFromUnreactedToReactedSpecies(Species) 
    public void moveFromUnreactedToReactedSpecies(Species p_species) {
        //#[ operation moveFromUnreactedToReactedSpecies(Species) 
        boolean rs = containsAsReactedSpecies(p_species);
        boolean us = containsAsUnreactedSpecies(p_species);
        
        if (rs && !us) return;
        else if (!rs && us) {
        	getUnreactedSpeciesSet().remove(p_species);
        	getReactedSpeciesSet().add(p_species);
        	return;
        }
        else throw new InvalidCoreEdgeRelationException(p_species.getName());
        //#]
    }
    
    //## operation printPDepModel(Temperature) 
    public void printPDepModel(Temperature p_temperature) {
        //#[ operation printPDepModel(Temperature) 
        System.out.print("This model include totally " + String.valueOf(getSpeciesNumber()) + " Species and ");
        System.out.println(String.valueOf(getReactionNumber()) + " Reactions.");
        //System.out.println("Species Set:");
        //System.out.println("Totally " + String.valueOf(getSpeciesNumber()) + " Species:");
        /*LinkedList sortedSpeList = new LinkedList();
        for (Iterator iter = getSpecies(); iter.hasNext(); ) {
        	Species spe = (Species)iter.next();
        	int id = spe.getID();
        	boolean added = false;
        	if (sortedSpeList.isEmpty()) sortedSpeList.add(spe);
        	else {
        		for (int i = 0; i<sortedSpeList.size(); i++) {
        			Species thisSpe = (Species)sortedSpeList.get(i);
        			if (thisSpe.getID()>id) {
        				sortedSpeList.add(i, spe);
        				added = true;
        				break;
        			}
        		}
        		if (!added) sortedSpeList.add(spe);
        	}
        }
        
        for (int i=0; i<sortedSpeList.size(); i++) {
        	Species spe = (Species)sortedSpeList.get(i);
        	System.out.println(spe.toStringWithoutH());
        }
        
        System.out.println("Thermo Properties:");
        System.out.println("SpeciesID\tNamw\tH298\tS298\tCp300\tCp400\tCp500\tCp600\tCp800\tCp1000\tCp1500");
        for (int i=0; i<sortedSpeList.size(); i++) {
        	Species spe = (Species)sortedSpeList.get(i);
        	System.out.println(String.valueOf(spe.getID()) + '\t' + spe.getName() + '\t' + spe.getThermoData().toString());
        }*/
        
        LinkedList nonPDepList = new LinkedList();
        LinkedList pDepList = new LinkedList();
        
        HashSet pDepStructureSet = new HashSet();
        for (Iterator iter = PDepNetwork.getDictionary().values().iterator(); iter.hasNext(); ) {
        	PDepNetwork pdn = (PDepNetwork)iter.next();
        	for (Iterator pdniter = pdn.getPDepNetReactionList(); pdniter.hasNext();) {
        		PDepNetReaction pdnr = (PDepNetReaction)pdniter.next();
        		if (isReactedReaction(pdnr)) {
        			pDepList.add(pdnr);
        			pDepStructureSet.add(pdnr.getStructure());
        		}
        	}
        }
        
        for (Iterator iter = getReactionSet().iterator(); iter.hasNext(); ) {
        	Reaction r = (Reaction)iter.next();
        	Structure s = r.getStructure();
        	if (!pDepStructureSet.contains(s)) {
        		nonPDepList.add(r);
        	} 
        }
        
        System.out.println("//non p_dep reactions:");
        for (Iterator iter = nonPDepList.iterator(); iter.hasNext(); ) {
        	Reaction r = (Reaction)iter.next();
        	double rate = r.calculateRate(p_temperature);
            if (r instanceof TemplateReaction) rate = ((TemplateReaction)r).calculatePDepRate(p_temperature);
            //System.out.println(r.toString()+"\t rate = \t"+ String.valueOf(rate));
			System.out.println(r.toChemkinString());
        }
        
        System.out.println("//p_dep reactions:");
        for (Iterator iter = pDepList.iterator(); iter.hasNext(); ) {
        	PDepNetReaction r = (PDepNetReaction)iter.next();
            //System.out.println(r.getStructure().toString() + "\t rate = \t" + Double.toString(r.getRate()));
			System.out.println(r.toChemkinString());
        }
        System.out.println("/////////////////////////////");
        return;
        //#]
    }
    
    //## operation repOk() 
    public boolean repOk() {
        //#[ operation repOk() 
        return true;
        //#]
    }
    
    //## operation toString() 
    public String toString() {
        //#[ operation toString() 
        String s = "Model Core:\n";
        s = s + core.toString();
        s = s + "Model Edge:\n";
        s = s + edge.toString();
        
        return s;
        
        
        //#]
    }
    
    public Core getCore() {
        return core;
    }
    
    public Core newCore() {
        core = new Core();
        return core;
    }
    
    public void deleteCore() {
        core=null;
    }
    
    public Edge getEdge() {
        return edge;
    }
    
    public Edge newEdge() {
        edge = new Edge();
        return edge;
    }
    
    public void deleteEdge() {
        edge=null;
    }
    
    protected void initRelations() {
        core = newCore();
        edge = newEdge();
    }
    
}
/*********************************************************************
	File Path	: RMG\RMG\jing\rxnSys\CoreEdgeReactionModel.java
*********************************************************************/
