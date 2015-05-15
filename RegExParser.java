package pro_try;
import java.util.*;

public class RegExParser {

	// Operator stack used to stack operators
	private Stack opStack		= new Stack();

	// Token stack used to stack expressions (input characters)
	private Stack inStack		= new Stack();

	// integer that keeps track of states naming convention
	private int nNextStateIndex	= 0;

	// Set containing all input characters
	public TreeSet treeSetInput	= new TreeSet();

	
		//Default construstor
	
	public RegExParser(){
	}

	/**************************************************************************
		Converts a regular expression to the NFA.
	***************************************************************************/
	public ArrayList convertToNFA(String strInput){
		// Used to record if the previous Token was an input
		// character or right paranthesis or "*" operator
		// In this way we can detect the concatenation
		// Concatenation will be denoted as "+" on the operator stack
		boolean bPrevTokenConcat = false;

		// clear both stacks
		opStack 		= new Stack();
		inStack 		= new Stack();
		// Reset char set
		treeSetInput	= new TreeSet();
		// Reset naming convention
		nNextStateIndex = 0;

		// A token here is single character, which could be
		// an input character or an operator
		for(int i=0; i<strInput.length(); ++i){
			// Get the current tocken
			char szToken = strInput.charAt(i);

			// Check is the character a input character or operator
			if(isTokenInput(szToken)){

				// Here we are detecting the concatenation
				// It is simple to detect if evaluating arithmetic expressions
				// but here a concatenation is 2 input characters together
				// or input char and left paranthesis ...
				if(bPrevTokenConcat){
					if(opStack.empty())
						opStack.push(new Character('+'));
					else{
						Character szOperator = (Character)opStack.peek();
						/* while(Precedence of concatenation <= Presedence of szOperator) */
						while(compareOperatorPrecedence('+', szOperator.charValue()) <= 0){

							// Evaluate szOperator or opStack.top()
							opEval();

							// If stack is empty then stop
							if(opStack.empty())
								break;

							// get the next operator on the stack without removing it
							// to check for precedence
							szOperator = (Character)opStack.peek();
						}

						// push concatenation operator on the stack
						opStack.push(new Character('+'));
					}
				}

				// Push this input character on the stack
				inStackPush(Character.toString(szToken));

				// Anything after this is concatenation (except "*")
				bPrevTokenConcat = true;
			}
			else if(opStack.empty()){
				if(bPrevTokenConcat){
					if(szToken == '(')
						opStack.push(new Character('+'));
				}

				if((szToken == '*') || (szToken == ')'))
					bPrevTokenConcat = true;
				else bPrevTokenConcat = false;
				opStack.push(new Character(szToken));
			}
			else if(isTokenLeftParanthesis(szToken)){
				if(bPrevTokenConcat){
					Character szOperator = (Character)opStack.peek();
					/* while(Precedence of concatenation <= Presedence of szOperator) */
					while(compareOperatorPrecedence('+', szOperator.charValue()) <= 0){

						// Evaluate szOperator or opStack.top()
						opEval();

						// If stack is empty then stop
						if(opStack.empty())
							break;
						// get the next operator on the stack without removing it
						// to check for precedence
						szOperator = (Character)opStack.peek();
					}

					// push concatenation operator on the stack
					opStack.push(new Character('+'));
				}

				// push left paranthesis on the stack
				opStack.push(new Character(szToken));

				// left paranthesis next to anything is not concatenation
				bPrevTokenConcat = false;
			}
			else if(isTokenRightParanthesis(szToken)){
				// get the top operator from the stack
				Character szOperator = (Character)opStack.peek();
				while(!isTokenLeftParanthesis(szOperator.charValue())){

					// Evaluate szOperator (or opStack.top())
					opEval();

					// If stack is empty then stop
					if(opStack.empty())
						break;

					// get the next operator on the stack without removing it
					szOperator = (Character)opStack.peek();
				}
				// get rid of left paranthesis from the stack
				opStack.pop();

				// left paranthesis next to anything is not concatenation
				bPrevTokenConcat = true;
			}
			else{
				Character szOperator = (Character)opStack.peek();
				/* while(Precedence of concatenation <= Presedence of szOperator) */
				while(compareOperatorPrecedence(szToken, szOperator.charValue()) <= 0){

					// Evaluate szOperator or opStack.top()
					opEval();

					// If stack is empty then stop
					if(opStack.empty())
						break;

					// get the next operator on the stack without removing it
					// to check for precedence
					szOperator = (Character)opStack.peek();
				}

				opStack.push(new Character(szToken));

				if(szToken == '*')
					bPrevTokenConcat = true;
				else bPrevTokenConcat = false;
			}
		}

		// Evaluate the rest of operators in the order they are popped
		while(!opStack.empty())
			opEval();

		return (ArrayList)inStack.pop();
	}

	/**************************************************************************
		Checks if the specified character is operator

		@param char			Character to be tested for *, |
		@return boolean 	True if specified character is operator
	***************************************************************************/
	private boolean isTokenOperator(char ch){
		// Check is the specified character "*" or "|"
		return((ch == '*') || (ch == '|'));
	}

	/**************************************************************************
		Checks if the tocken is right paranthesis

		@param char			Character to be tested
		@return boolean 	True if specified character is Right parantesis
	***************************************************************************/
	private boolean isTokenRightParanthesis(char ch){
		return(ch == ')');
	}

	/**************************************************************************
		Checks if the tocken is left paranthesis

		@param char			Character to be tested
		@return boolean 	True if specified character is left parantesis
	***************************************************************************/
	private boolean isTokenLeftParanthesis(char ch){
		return(ch == '(');
	}

	/**************************************************************************
		Checks if the specified an input character. Everything that is not
		an operator is an input character. If an operator must be considered
		as an input charcter then special considerations must be takean into
		an account, but for simplicity I will ignore them.

		@param char			Character to be tested
		@return boolean 	True if specified character is an input character
	***************************************************************************/
	private boolean isTokenInput(char ch){
		return(!isTokenOperator(ch) && !isTokenRightParanthesis(ch) && !isTokenLeftParanthesis(ch));
	}

	/**************************************************************************
		Compares 2 operators for precedence

		@param char			First operator
		@param char			Second operator
		@return in 			 0 if equal presedence
							-1 if First < Second
							 1 if First > Second
	***************************************************************************/
	private int compareOperatorPrecedence(char opFirst, char opSecond){
		// Check for equality
		if(opFirst == opSecond)
			return 0;

		// ")" operator has also highest precedence
		// This comparison will only happen with "+" - concatenation
		if(opFirst == ')')
			return 1;
		if(opSecond == ')')
			return -1;

		// "*" operator is after paranthesis
		if(opFirst == '*')
			return 1;
		if(opSecond == '*')
			return -1;

		// Ok ... it is not equal and not "*"
		if(opFirst == '+')
			return 1;
		if(opSecond == '+')
			return -1;

		// "(" operator has lowest precedence
		if(opFirst == '|')
			return 1;
		else return -1;
	}

	/**************************************************************************
		Evaluates the operator from the top of the operator stack

		@return void
	***************************************************************************/
	private void opEval(){
		// pop the operator from the operator stack
		Character szOperator = (Character)opStack.pop();

		// Debuging Purposes
		System.out.println("Eval "+ szOperator.toString());

		// There is difference in evaluating operators
		// because "*" is aplied only on the single
		// element while "|" operator is applied on
		// 2 expressions. Aditionally concatenation is applied
		// to 2 operator in specific order. So we need to check
		// what operator it is and then evaluate the expression
		// appropriatelly
		switch(szOperator.charValue()){
			case '*':
				evalStar();
				break;
			case '|':
				evalUnion();
				break;
			case '+':
				evalConcat();
				break;
		}
	}

	/**************************************************************************
		Evaluates the star operator on the top of the input symbol stack.
		Star evaluation is basically creating the epsilon transition between
		or from last state of the operand to the first state, and creating
		the 2 new states with epsilon tranition from first to the second new
		state and from first new state to the first state in the operand. Also
		there is epsilon transition from last state in the operand to the
		second newly created state. Sounds complicated but see THOMPSON'S
		algorithm for details!

		@return void
	***************************************************************************/
	private void evalStar(){
		// get operands
		ArrayList expOperand = (ArrayList)inStack.pop();

		// Create 2 new states
		State stateFirst 	= new State("s"+Integer.toString(nNextStateIndex));
		// keep track of state naming convention
		++nNextStateIndex;
		State stateSecond 	= new State("s"+Integer.toString(nNextStateIndex));
		// keep track of state naming convention
		++nNextStateIndex;

		// Create transitions from stateFirst to the first state of the operand
		// and from stateFirst to the stateSecond
		Transition eT1 = new Transition("epsilon");
		eT1.stateArray.add(expOperand.get(0));
		eT1.stateArray.add(stateSecond);
		stateFirst.arrayTransitions.add(eT1);

		// now add transition from last element in operand and
		// stateSecond
		Transition eT2 = new Transition("epsilon");
		eT2.stateArray.add(stateSecond);
		((State)expOperand.get(expOperand.size()-1)).arrayTransitions.add(eT2);

		// now add epilon transition between last and first state of the operand
		Transition eT3 = new Transition("epsilon");
		eT3.stateArray.add(expOperand.get(0));
		((State)expOperand.get(expOperand.size()-1)).arrayTransitions.add(eT3);

		// This is whole new expression so put it onto the stack
		ArrayList newExp = new ArrayList();
		newExp.add(stateFirst);
		newExp.addAll(expOperand);
		newExp.add(stateSecond);

		// add it to the stack
		inStack.push(newExp);

	}

	/**************************************************************************
		Evaluates the union operator on the two top elements of the input stack
		The union is created as following: Create 2 states. First state has
		epsilon transitions to first state of both objects. Last 2 states of
		both objects have epsilon transition to second newly created state.
		Then all states are added in following order. First State then both
		expressions (no specific order needed) and then last state.

		@return void
	***************************************************************************/
	private void evalUnion(){
		// get operands
		ArrayList expSecond = (ArrayList)inStack.pop();
		ArrayList expFirst  = (ArrayList)inStack.pop();

		// Create 2 new states
		State stateFirst 	= new State("s"+Integer.toString(nNextStateIndex));
		// keep track of state naming convention
		++nNextStateIndex;
		State stateSecond 	= new State("s"+Integer.toString(nNextStateIndex));
		// keep track of state naming convention
		++nNextStateIndex;

		// Create 2 epsilon transitions from first state to the
		// first states of the operands
		Transition eT1 = new Transition("epsilon");
		eT1.stateArray.add(expFirst.get(0));
		Transition eT2 = new Transition("epsilon");
		eT2.stateArray.add(expSecond.get(0));

		// add both tranisitons to the stateFirst
		stateFirst.arrayTransitions.add(eT1);
		stateFirst.arrayTransitions.add(eT2);

		// now add transitions from last state in each operand to the stateSecond
		Transition eT3 = new Transition("epsilon");
		eT3.stateArray.add(stateSecond);
		Transition eT4 = new Transition("epsilon");
		eT4.stateArray.add(stateSecond);
		((State)expFirst.get(expFirst.size()-1)).arrayTransitions.add(eT3);
		((State)expSecond.get(expSecond.size()-1)).arrayTransitions.add(eT4);

		// This is whole new expression so put it onto the stack
		ArrayList newExp = new ArrayList();
		newExp.add(stateFirst);
		newExp.addAll(expFirst);
		newExp.addAll(expSecond);
		newExp.add(stateSecond);

		// add it to the stack
		inStack.push(newExp);
	}

	/**************************************************************************
		Concatenates the two elements from the top of the input stack.
		Concatenation is order sensitive or not comutative so the
		RS != SR. When concatenating the element that is higher on the stack
		is the operator on the left of the concatenation (or second one)

		@return void
	***************************************************************************/
	private void evalConcat(){
		// get operands
		ArrayList expSecond = (ArrayList)inStack.pop();
		ArrayList expFirst  = (ArrayList)inStack.pop();

		// now just create epsilon transition between the last
		// state in the expFirst and first state in the expSecond
		// Create epsilon transition
		Transition t = new Transition("epsilon");
		// transition to the first state in the second operand
		t.stateArray.add((State)expSecond.get(0));
		// from the last state in the first operand
		State s = (State)expFirst.get(expFirst.size()-1);
		s.arrayTransitions.add(t);

		// This is whole new expression so put it onto the stack
		ArrayList newExp = new ArrayList();
		newExp.addAll(expFirst);
		newExp.addAll(expSecond);

		// add it to the stack
		inStack.push(newExp);
	}

	/**************************************************************************
		Pushes a basic element onto the stack which is 2 States and a transition
		between state 1 to state 2 on input character given as parameter

		@param String	Input character for transition
		@return void
	***************************************************************************/
	private void inStackPush(String strInChar){

		// Debuging purposes
		System.out.println("Push " + strInChar);

		// Array of states represent the object
		ArrayList arrayExpression = new ArrayList();

		// construct state 1
		String strState1Name = "s"+Integer.toString(nNextStateIndex);
		State s1 = new State(strState1Name);

		// increase next state index
		++nNextStateIndex;

		// construct state 2
		String strState2Name = "s"+Integer.toString(nNextStateIndex);
		State s2 = new State(strState2Name);

		// Now construct the Transition between s1 and s2
		Transition t = new Transition(strInChar);
		t.stateArray.add(s2);
		s1.arrayTransitions.add(t);

		// add states to the array
		arrayExpression.add(s1);
		arrayExpression.add(s2);

		// add this array object to input stack
		inStack.push(arrayExpression);

		// increase next state index
		++nNextStateIndex;

		// Add it to the character list
		treeSetInput.add(strInChar);
	}
}