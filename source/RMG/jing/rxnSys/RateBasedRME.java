////////////////////////////////////////////////////////////////////////////////
//
//	RMG - Reaction Mechanism Generator
//
//	Copyright (c) 2002-2009 Prof. William H. Green (whgreen@mit.edu) and the
//	RMG Team (rmg_dev@mit.edu)
//
//	Permission is hereby granted, free of charge, to any person obtaining a
//	copy of this software and associated documentation files (the "Software"),
//	to deal in the Software without restriction, including without limitation
//	the rights to use, copy, modify, merge, publish, distribute, sublicense,
//	and/or sell copies of the Software, and to permit persons to whom the
//	Software is furnished to do so, subject to the following conditions:
//
//	The above copyright notice and this permission notice shall be included in
//	all copies or substantial portions of the Software.
//
//	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
//	FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
//	DEALINGS IN THE SOFTWARE.
//
////////////////////////////////////////////////////////////////////////////////



package jing.rxnSys;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import jing.param.*;
import jing.rxn.NegativeRateException;
import jing.rxn.Reaction;
import jing.rxn.TemplateReaction;
import jing.chem.ChemGraph;
import jing.chem.Species;
import jing.rxnSys.SpeciesStatus;
import jing.chem.SpeciesDictionary;
//import RMG;
//## package jing::rxnSys 

//----------------------------------------------------------------------------
// jing\rxnSys\RateBasedRME.java                                                                  
//----------------------------------------------------------------------------

//## class RateBasedRME 
public class RateBasedRME implements ReactionModelEnlarger {
    
	protected HashSet includeSpecies = null; //these species are included into the core even if they have very 
	  										 //low flux.
    // Constructors
    
    public  RateBasedRME() {
    }
    
	public void addIncludeSpecies(HashSet p_includeSpecies){
		if (includeSpecies == null) {
			includeSpecies = p_includeSpecies;
		}
		else {
			System.out.println("IncludeSpecies have already been added!!");
			System.exit(0);
		}
			
	}
	
    //9/25/07 gmagoon: added ReactionModel parameter
    //10/30/07 gmagoon: updated parameters to match ReactionModelEnlarger
    //## operation enlargeReactionModel(ReactionSystem) 
    public void enlargeReactionModel(LinkedList p_reactionSystemList, ReactionModel rm, LinkedList p_validList){
    //public void enlargeReactionModel(ReactionSystem p_reactionSystem, ReactionModel rm) 
        //#[ operation enlargeReactionModel(ReactionSystem) 
        //ReactionModel rm = p_reactionSystem.getReactionModel();
        if (!(rm instanceof CoreEdgeReactionModel)) throw new InvalidReactionModelTypeException();
        CoreEdgeReactionModel cerm = (CoreEdgeReactionModel)rm;
        
        //10/30/07 gmagoon: iterate over reaction systems that are not valid 
		LinkedList nextList = new LinkedList();
		double startTime = System.currentTimeMillis();//note: moved before a couple of lines, but I expect it to have negligible effect on timing
		for (Integer i = 0; i < p_reactionSystemList.size(); i++) {
			if (!(Boolean) p_validList.get(i)) {
				PresentStatus ps = ((ReactionSystem) p_reactionSystemList.get(i)).getPresentStatus();
				String maxflux = "";

				Species next = getNextCandidateSpecies(cerm, ps, maxflux);
				nextList.add(next);
				} else {
				nextList.add(null);//****hopefully, null will contribute to length of list; otherwise, modifications will be needed
			}
		}

		// generate new reaction set
		/*startTime = System.currentTimeMillis();
		LinkedHashSet newReactionSet = p_reactionSystem.lrg.react(cerm.getReactedSpeciesSet(),next);
		newReactionSet.addAll(p_reactionSystem.getReactionGenerator().react(cerm.getReactedSpeciesSet(),next));

		double enlargeTime = (System.currentTimeMillis()-startTime)/1000/60;*/

		startTime = System.currentTimeMillis();

		//10/30/07 gmagoon: add species from nextList
		for (Integer i = 0; i < p_reactionSystemList.size(); i++) {
			if (!(Boolean) p_validList.get(i)) {
				if (cerm.containsAsReactedSpecies((Species) nextList.get(i))) //throw new InvalidNextCandidateSpeciesException();
				{
					System.out.println("Species is already present in reaction model");//10/30/07 gmagoon: preliminary message; should probably be refined in future
				} else {
					
					double findSpeciesTime = (System.currentTimeMillis() - startTime) / 1000 / 60;

					Species species = (Species) nextList.get(i);
					
					//Global.diagnosticInfo.append(next.getChemkinName() + "\t" + maxflux + "\t" + ((RateBasedVT) ((ReactionSystem) p_reactionSystemList.get(i)).finishController.validityTester).Rmin + "\t" + findSpeciesTime + "\t");
					System.out.print("\nAdd a new reacted Species:");
					System.out.println(species.getName());
					Temperature temp = new Temperature(298, "K");
					double H = species.calculateH(temp);
					double S = species.calculateS(temp);
					double G = species.calculateG(temp);
					double Cp = species.calculateCp(temp);
					System.out.println("Thermo of species at 298K (H, S, G, Cp, respectively)\t" + String.valueOf(H) + '\t' + String.valueOf(S) + '\t' + String.valueOf(G) + '\t' + String.valueOf(Cp));
			
					
					cerm.moveFromUnreactedToReactedSpecies(species);
					cerm.moveFromUnreactedToReactedReaction();
					//10/30/07 gmagoon: note subsequent lines were previously outside of else block (and it probably didn't matter then), but now I think they belong in else block
					Global.moveUnreactedToReacted = (System.currentTimeMillis() - startTime) / 1000 / 60;

					// add species status to reaction system
					SpeciesStatus speciesStatus = new SpeciesStatus(species, 1, 0.0, 0.0); // (species, type (reacted=1), concentration, flux)
					PresentStatus ps = ((ReactionSystem) p_reactionSystemList.get(i)).getPresentStatus();
					ps.putSpeciesStatus(speciesStatus);

					// generate new reaction set
					startTime = System.currentTimeMillis();
					LinkedHashSet newReactionSet = ((ReactionSystem) p_reactionSystemList.get(i)).getReactionGenerator().react(cerm.getReactedSpeciesSet(), (Species) nextList.get(i));
					newReactionSet.addAll(((ReactionSystem) p_reactionSystemList.get(i)).lrg.react(cerm.getReactedSpeciesSet(), (Species) nextList.get(i)));
					//LinkedHashSet newReactionSet = p_reactionSystem.getReactionGenerator().react(cerm.getReactedSpeciesSet(),next);
					//newReactionSet.addAll(p_reactionSystem.lrg.react(cerm.getReactedSpeciesSet(),next));

					double enlargeTime = (System.currentTimeMillis() - startTime) / 1000 / 60;



					startTime = System.currentTimeMillis();
					/* // we can't read in the restart files, so for now there's no point in writing them!
					StringBuilder restartFileContent= new StringBuilder();
					try{
					File allReactions = new File ("Restart/allReactions.txt");
					FileWriter fw = new FileWriter(allReactions, true);
					//Species species = (Species) iter.next();
					for(Iterator iter=newReactionSet.iterator();iter.hasNext();){

					Reaction reaction = (Reaction) iter.next();
					if (cerm.categorizeReaction(reaction)==-1)
					restartFileContent.append(reaction.toRestartString(((ReactionSystem)p_reactionSystemList.get(i)).getPresentTemperature()) + "\n");//10/30/07 gmagoon: changed to avoid use of Global.temperature
					//restartFileContent.append(reaction.toRestartString(Global.temperature) + "\n");

					}

					//restartFileContent += "\nEND";
					fw.write(restartFileContent.toString());
					fw.close();
					}
					catch (IOException e){
					System.out.println("Could not write the added Reactions to the allReactions file");
					System.exit(0);
					}
					 */

					double restartTime = (System.currentTimeMillis() - startTime) / 1000 / 60;

					Global.diagnosticInfo.append(Global.moveUnreactedToReacted + "\t" + enlargeTime + "\t" + restartTime + "\t");

					// partition the reaction set into reacted reaction set and unreacted reaction set
					// update the corresponding core and edge model of CoreEdgeReactionModel
					cerm.addReactionSet(newReactionSet);


				}
			}
		}
        

        //String return_string;
        return;
        
        
        //#]
    }
    
	public boolean presentInIncludedSpecies(Species p_species){
		Iterator iter = includeSpecies.iterator();
		while (iter.hasNext()){
			Species spe = (Species)iter.next();
			Iterator isomers = spe.getResonanceIsomers();
			while (isomers.hasNext()){
				ChemGraph cg = (ChemGraph)isomers.next();
				if (cg.equals(p_species.getChemGraph())) 
					return true;
			}
		}
		return false;
	}
	
    //## operation getNextCandidateSpecies(CoreEdgeReactionModel,PresentStatus) 
    public Species getNextCandidateSpecies(CoreEdgeReactionModel p_reactionModel, PresentStatus p_presentStatus, String maxflux) {
        //#[ operation getNextCandidateSpecies(CoreEdgeReactionModel,PresentStatus) 
    	LinkedHashSet unreactedSpecies = p_reactionModel.getUnreactedSpeciesSet();
         
        Species maxSpecies = null;
        double maxFlux = 0;
        Species maxIncludedSpecies = null;
		double maxIncludedFlux = 0;
		
        Iterator iter = unreactedSpecies.iterator();
        while (iter.hasNext()) {
        	Species us = (Species)iter.next();
        	//double thisFlux = Math.abs(p_presentStatus.getSpeciesStatus(us).getFlux());
			//System.out.println(p_presentStatus.unreactedSpeciesFlux[83]);
			//System.exit(0);
			double thisFlux = Math.abs(p_presentStatus.unreactedSpeciesFlux[us.getID()]);
			if (includeSpecies != null && includeSpecies.contains(us)) {
				if (thisFlux > maxIncludedFlux) {
	        		maxIncludedFlux = thisFlux;
	        		maxIncludedSpecies = us;
	        	}
			}
			else {
				if (thisFlux > maxFlux) {
	        		maxFlux = thisFlux;
	        		maxSpecies = us;
	        	}
			}
        	
        }
		
		if (maxIncludedSpecies != null){
			System.out.println("Instead of "+maxSpecies.toChemkinString()+" with flux "+ maxFlux + " "+ maxIncludedSpecies.toChemkinString() +" with flux " + maxIncludedFlux);
			maxFlux = maxIncludedFlux;
			maxSpecies = maxIncludedSpecies;
			includeSpecies.remove(maxIncludedSpecies);
		}
		
        maxflux = ""+maxFlux;
        if (maxSpecies == null) throw new NullPointerException();
        
		
		
        LinkedHashSet ur = p_reactionModel.getUnreactedReactionSet();
        
		HashMap significantReactions = new HashMap();
		int reactionWithSpecies = 0;
		
        for (Iterator iur = ur.iterator(); iur.hasNext();) {
        	Reaction r = (Reaction)iur.next();
        	double flux = 0;
			Temperature p_temperature = p_presentStatus.temperature;
            Pressure p_pressure = p_presentStatus.pressure;//10/30/07 gmagoon: added
			if (r.contains(maxSpecies)){
				reactionWithSpecies++;
				if (r instanceof TemplateReaction) {
                    flux = ((TemplateReaction)r).calculateTotalPDepRate(p_temperature, p_pressure);//10/30/07 gmagoon: changed to include pressure
	        		//flux = ((TemplateReaction)r).calculateTotalPDepRate(p_temperature);
	        	}
	        	else {
	        	 	flux = r.calculateTotalRate(p_temperature);
	        	}
	        	if (flux > 0) {
	        		for (Iterator rIter=r.getReactants(); rIter.hasNext();) {
						Species spe = (Species)rIter.next();
	        		    SpeciesStatus status = p_presentStatus.getSpeciesStatus(spe);
						if (status == null)
							flux = 0;
						else {
							double conc = status.getConcentration();
							if (conc<0) {
								double aTol = ReactionModelGenerator.getAtol();
								//if (Math.abs(conc) < aTol) conc = 0;
								//else throw new NegativeConcentrationException(spe.getName() + ": " + String.valueOf(conc));
								if (conc < -100.0 * aTol)
									throw new NegativeConcentrationException("Species " + spe.getName() + " has negative concentration: " + String.valueOf(conc));
							}
							flux *= conc;
						}

	        		}
					

	        	}
	        	else {
                                throw new NegativeRateException(r.toChemkinString(p_temperature) + ": " + String.valueOf(flux));//10/30/07 gmagoon: changed to avoid use of Global.temperature
	        		//throw new NegativeRateException(r.toChemkinString(Global.temperature) + ": " + String.valueOf(flux));
	        	}
				if (flux > 0.01 * maxFlux)
					significantReactions.put(r,flux);
			}
        	
        }
		
        System.out.print("Time: ");
        System.out.println(p_presentStatus.getTime());
        System.out.println("Unreacted species " + maxSpecies.getName() + " has highest flux: " + String.valueOf(maxFlux));
		System.out.println("The total number of unreacted reactions with this species is "+reactionWithSpecies+". Significant ones are:");
		Iterator reactionIter = significantReactions.keySet().iterator();
		while (reactionIter.hasNext()){
			Reaction r = (Reaction)reactionIter.next();
			System.out.println(" "+r.getStructure().toChemkinString(r.hasReverseReaction())+"\t"+significantReactions.get(r));
		}
        
        return maxSpecies;
        //#]
    }
    
}
/*********************************************************************
	File Path	: RMG\RMG\jing\rxnSys\RateBasedRME.java
*********************************************************************/

