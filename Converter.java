package pro_try;

 /******************************************************************************
	This class converts NFA to DFA using famous Subset Construction
	Algorihtm.

	
*******************************************************************************/
import java.util.*;

public class Converter {

	// Store complete NFA in a single TreeMap
	// Key 		= Name of the state
	// Value 	= NFA State itself
	private TreeMap treeMapNFA = new TreeMap();

	// Stores complete DFA in a single TreeMap
	// Key		= Name of the state
	// Value	= DFA State
	private TreeMap treeMapDFA = new TreeMap();

	// Array of input characters (except epsilon)
	public ArrayList charArray = new ArrayList();

	// Start State of the NFA
	private String strNFAStartStateName;

	// Start State name of DFA
	private String strDFAStartStateName;

	// Constructor
	public Converter(){
	}

	/**************************************************************************
		This method calculates Epsilon closure which as parameter takes a
		State object and returns set of states reachible from it based on
		one or more epsilon transitions.

		@param State		State to compute epsilon transitions
		@return ArrayList	Set of states which can be reached from this state
							whitout consuming any inputs.
	***************************************************************************/
	private ArrayList eClosure(State state){
		// Stack of states which are unprocessed
		Stack unprocessedStack 	= new Stack();
		unprocessedStack.push(state);

		// Set of states that are processed
		TreeSet processedSet 	= new TreeSet();

		// Set of states connected
		TreeSet setResult 		= new TreeSet();

		// add the first state to the result because all states
		// are connected to itself by epsilon transition
		setResult.add(state);

		// while there is a state unprocessed on the stack
		while(!unprocessedStack.empty()){
			// pop the state to process
			State processingState = (State)unprocessedStack.pop();

			// Debug Purposes
			// System.out.println("Processing State: "+processingState.strName);

			// mark this state as processed
			processedSet.add(processingState);

			// for each epsilon transition of this state add that state to the result
			for(int i=0; i<processingState.arrayTransitions.size(); ++i){
				Transition t = (Transition)processingState.arrayTransitions.get(i);

				// Debug Purposes
				// System.out.println("Transition Char: "+t.transChar);

				// check each transition is it epsilon
				if(t.transChar.compareToIgnoreCase("epsilon") == 0){
					for(int j=0; j<t.stateArray.size(); ++j){
						State s = (State)t.stateArray.get(j);
						setResult.add(s); // will eliminate duplicates automatically

						// Debug Purposes
						// System.out.println("Adding state to result: "+s.strName);

						// check did we already process this state or not
						if(!processedSet.contains(s))
							unprocessedStack.push(s);
					}
				}
			}
		}

		ArrayList arrayRes = new ArrayList();
		arrayRes.addAll(setResult);
		return(arrayRes);
	}

	/**************************************************************************
		This method returns set of all states that are reachible from the
		input state based on the spcific character input.

		@param State		State to compute character transitions
		@param String		Character input
		@return ArrayList	Set of states which can be reached from this state
							on this character input.
	***************************************************************************/
	private ArrayList Move(State state, String character){
		// Set of states connected
		ArrayList setResult = new ArrayList();

		// Just in case, get rid of spaces
		character.trim();
		if(character.length() == 0){
			System.out.println("Method Move called with empty character");
			return null;
		}

		// Go through all transitions and check is it this character
		for(int i=0; i<state.arrayTransitions.size(); ++i){
			Transition transition = (Transition)state.arrayTransitions.get(i);

			// compare is it character we are looking for
			if(transition.transChar.compareTo(character) == 0){
				// yes so add it to the result
				setResult.addAll(transition.stateArray);
				return setResult;
			}
		}

		return null;
	}

	/**************************************************************************
		Converts NFA to DFA using Subset Construction algorithm

		@return Boolean		TRUE if conversion successfull
	***************************************************************************/
	public boolean Convert(){
		// The start state of DFA is constructed from the e-closure
		// of the starting state of NFA
		State startState = (State)treeMapNFA.get(strNFAStartStateName);
		if(startState == null)
			return false;

		// Here I need Queue (FIFO) but I must simulate it with ArrayList
		ArrayList queueUnprocessed = new ArrayList();

		// This collection keeps track of aready processed states
		TreeSet treeSetProcessed = new TreeSet();

		// construct starting state of DFA from the array
		ArrayList setState = eClosure(startState);
		startState = new State(setState);

		// save the name of this state
		strDFAStartStateName = startState.strName;

		// add it to the DFA tree map
		treeMapDFA.put(strDFAStartStateName, startState);

		// add this starting state to unprocessed states
		queueUnprocessed.add(startState);

		// process all unprocessed DFA states
		while(!queueUnprocessed.isEmpty()){

			// get the DFA state to be processed
			State DFAState = (State)queueUnprocessed.get(0);
			treeMapDFA.put(DFAState.strName, DFAState);
			queueUnprocessed.remove(0);

			// we do not want to process single state many times
			if(treeSetProcessed.contains(DFAState))
				continue;

			// Mark this state as processed
			treeSetProcessed.add(DFAState);

			// this is for debuging purposes only
			// System.out.println("------------------------------------------------");
			// System.out.println("State Processed: " + DFAState.strName);

			// for each possible character
			for(int i=0; i<charArray.size(); ++i){

				// 1. apply move to newly created state
				// This action will result in a set of states that
				// can be reached from this set of states
				TreeSet moveSet = new TreeSet();
				for(int j=0; j<DFAState.NFAStates.size(); ++j){
					ArrayList moveArray = Move((State)DFAState.NFAStates.get(j), (String)charArray.get(i));
					if(moveArray != null)
						moveSet.addAll(moveArray); // trick to eliminate duplicates
				}

				// 2. apply e-closure to newly set of states from step 1
				// if this yelds empty set then we are done, otherwise
				// this is new state and it must be processed. Well
				// This is where this algorithm is different from
				// subset construction, beacuse I structured my data
				// differently than in subset construction algorithm
				TreeSet eClosureSet = new TreeSet();
				Iterator iter = moveSet.iterator();
				while(iter.hasNext()){
					State tmpState = (State)iter.next();
					ArrayList closureArray = eClosure(tmpState);
					eClosureSet.addAll(closureArray);
				}

				// check did the last operation yield any new states
				if(eClosureSet.size()>0){
					// Create new DFA state from the set of the returned states
					State newState = new State(new ArrayList(eClosureSet));

					// This is new state of the DFA so add it to the DFA collection
					// If this state already exists in the collection of newly created
					// DFA states then we need to use the already created and not newly
					// created state
					if(!treeMapDFA.containsKey(newState.strName))
						treeMapDFA.put(newState.strName, newState);
					else newState = (State)treeMapDFA.get(newState.strName);

					// add this state to unprocessed list
					queueUnprocessed.add(newState);

					// now create transition object on this character between
					// DFAState and newState
					Transition t = new Transition((String)charArray.get(i));
					t.stateArray.add(newState);
					DFAState.arrayTransitions.add(t);

					// For debuging purposes only
					// System.out.println("Transition from " + DFAState.strName + " to " + newState.strName);
				}
			}
		}

		return true;
	}

	/**************************************************************************
		Adds a Input Character

		@param String		Character to be added
		@return Boolean		TRUE if successfully added otherwise false
	***************************************************************************/
	public boolean addInputChar(String inChar){

		// if it says epsilon (or something different then ignore)
		if(inChar.compareToIgnoreCase("epsilon") == 0)
			return true;

		// in character cannot be empty
		if(inChar.length() != 1)
			return false;

		// add character
		charArray.add(inChar);
		return true;
	}

	/**************************************************************************
		Adds a State to the NFA TreeMap

		@param State		State to be added
		@return Boolean		TRUE if successfully added otherwise false
							(2 States have same name or state name empty string)
	***************************************************************************/
	public boolean addNFAState(State s){
		// check is the name empty string
		if(s.strName.length() == 0)
			return false;

		// check if the state name exists already
		if(treeMapNFA.containsKey(s.strName))
			return false;

		// ok, add the state to the map
		try{
			treeMapNFA.put(s.strName, s);
		}catch(ClassCastException e){
			return false;
		}catch(NullPointerException e){
			return false;
		}

		return true;
	}

	/**************************************************************************
		Adds a Transition to NFA state

		@param String		Name of the state
		@param Transition	Transition to be added
		@return Boolean		TRUE if transition could be added
	***************************************************************************/
	public boolean addNFAStateTransition(String strStateName, Transition t){
		// get the state with such name
		State state = getNFAState(strStateName);
		if(state == null)
			return false;

		// add the transition object to the state
		state.arrayTransitions.add(t);
		return true;
	}

	/**************************************************************************
		Returns Transaction object with the name specified

		@param String		Name of the state
		@return State		State object or null if non-existent
	***************************************************************************/
	public State getNFAState(String strStateName){
		// Retrieve the state object
		State state = null;
		try{
			state = (State)treeMapNFA.get(strStateName);
		}catch(ClassCastException e){
			return null;
		}catch(NullPointerException e){
			return null;
		}
		return state;
	}

	/**************************************************************************
		Returns number of NFA states

		@return int		NFA states count
	***************************************************************************/
	public int getNFAStateCount(){
		return treeMapNFA.size();
	}

	/**************************************************************************
		Sets the name of the NFA start state

		@return void
	***************************************************************************/
	public void setNFAStartState(String strStateName){
		strNFAStartStateName = strStateName;
	}

	/**************************************************************************
		Returns the name of the DFA start state

		@return void
	***************************************************************************/
	public String getDFAStartState(){
		return strDFAStartStateName;
	}

	/**************************************************************************
		Clears all Tables

		@return void
	***************************************************************************/
	public void Clear(){
		// To clear just dereference the main collection
		// gc is supposed to take care of the rest
		// I feel awkward as C++ progrmmer
		treeMapNFA 	= new TreeMap();
		treeMapDFA 	= new TreeMap();
		charArray	= new ArrayList();
	}

	/**************************************************************************
		Returns the DFA tree map

		@return void
	***************************************************************************/
	public TreeMap getDFA(){
		return treeMapDFA;
	}
}
