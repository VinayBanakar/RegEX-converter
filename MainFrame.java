package pro_try;
/******************************************************************************
	Application main window.

	
*******************************************************************************/
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

public class MainFrame extends JFrame implements ActionListener {

	JTable automataTable = new JTable(15, 10);

	JButton convertButton = new JButton("Convert");

	JButton parseButton	= new JButton("Parse");

	JTextField regExText = new JTextField(60);

	private RegExParser regExParser = new RegExParser();

	// Converter object that converts the NFA to DFA
	private Converter converter = new Converter();

	// Default constructor
	public MainFrame(String strName){
		super(strName);

		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.add(parseButton);
		panel.add(regExText);
		getContentPane().add(panel, BorderLayout.NORTH);

		getContentPane().add(automataTable);
		getContentPane().add(convertButton, BorderLayout.SOUTH);

		convertButton.addActionListener(this);
		parseButton.addActionListener(this);

		automataTable.setValueAt(new String("Input/States"), 0, 0);

		addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

		pack();
	}

	public void actionPerformed(ActionEvent evt){
		JComponent cmp = (JComponent)evt.getSource();

		if(cmp == convertButton){
			converter.Clear();
			readNFATable();
			if(!converter.Convert())
				JOptionPane.showMessageDialog(null, "Could not convert from NFA to DFA", "Error 5", JOptionPane.ERROR_MESSAGE);
			displayDFA();
		}

		if(cmp == parseButton){
			ArrayList arrayNFA = regExParser.convertToNFA(regExText.getText());
			if(arrayNFA != null){
				displayNFA(arrayNFA);
			}
		}
	}

	/**************************************************************************
		Reads in the NFA table from the user input
	***************************************************************************/
	private void readNFATable(){
		// Go through each row starting at index 1 until the column
		// with index 0 in that row is null (Read in all posible states)
		for(int i=1; i<automataTable.getRowCount() && automataTable.getValueAt(i, 0) != null; ++i){
			// This is new state so add it to the
			String strStateName	= (String)automataTable.getValueAt(i, 0);
			strStateName.trim();

			// Ignore blank state names
			if(strStateName.length() == 0)
				break;

			State state 		= new State(strStateName);

			// set the start state name
			if(i==1)
				converter.setNFAStartState(strStateName);

			// add state to the NFA structure
			if(!converter.addNFAState(state)){
				JOptionPane.showMessageDialog(null, "Could not add state: "+strStateName, "Error 1", JOptionPane.ERROR_MESSAGE);
			}
		}

		// Now read transitions for each state
		for(int i=1; i<=converter.getNFAStateCount(); ++i){
			for(int j=1; j<automataTable.getColumnCount() && automataTable.getValueAt(0, j) != null; ++j){
				// add all input characters to the converter
				if(i==1){
					// Check is the character blank cell
					// If cell is blank the object is either null or empty string
					// This caused that we considered empty string as character
					// BUT empty string is not a character (throwed me off a little)
					String chrInputChar = (String)automataTable.getValueAt(0, j);
					if(chrInputChar.length() == 0)
						break;
					if(!converter.addInputChar(chrInputChar))
							JOptionPane.showMessageDialog(null, "Could not add character: "+(String)automataTable.getValueAt(0, j),
								"Error 4", JOptionPane.ERROR_MESSAGE);
				}

				// Check is there anything in the transition and if yes
				// then add that transition to the state
				String strTransitionEntry = (String)automataTable.getValueAt(i, j);
				if(strTransitionEntry == null)
					continue;
				strTransitionEntry.trim();
				if(strTransitionEntry.length()==0)
					continue;

				// split the state names. in NFA there can be many transitions at single char entry
				// for example {s1, s2, s3, s4 ... }
				String[] strStates = strTransitionEntry.split(",");

				// create transition object
				Transition transition = new Transition((String)automataTable.getValueAt(0, j));

				// now create separate transition object for each transition
				for(int k=0; k<strStates.length; ++k){
					String strStateName = strStates[k];
					strStateName.trim();
					if(strStateName.length()==0)
						continue;

					// Debuging purposes
					// System.out.println(strStateName);

					// add transition state
					State state = converter.getNFAState(strStateName);
					if(state != null){
						transition.stateArray.add(state);
					}else{
						JOptionPane.showMessageDialog(null, "Could not find state with the name: "+strStateName,
													"Error 2", JOptionPane.ERROR_MESSAGE);
					}

					// add transition to the state
					strStateName = (String)automataTable.getValueAt(i, 0);
					strStateName.trim();
					state = converter.getNFAState(strStateName);
					if(state != null){
						state.arrayTransitions.add(transition);
						State tmpState = (State)transition.stateArray.get(transition.stateArray.size()-1);

						// Debuging purposes
						// System.out.println(state.strName+" (on character: "+transition.transChar+") "+tmpState.strName);
					}else{
						JOptionPane.showMessageDialog(null, "Could not find state with the name: "+strStateName,
										"Error 3", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
	}

	/**************************************************************************
		Displays the DFA table
	***************************************************************************/
	private void displayDFA(){

		// Get the name of the start state
		String strDFAStartState = converter.getDFAStartState();
		TreeMap treeMapDFA 		= converter.getDFA();

		// Create table of the needed size
		getContentPane().remove(automataTable);
		automataTable = new JTable(treeMapDFA.size()+1, converter.charArray.size()+1);
		getContentPane().add(automataTable);
		automataTable.validate();

		// display States\Input string
		automataTable.setValueAt("States/Input", 0, 0);

		// Display input characters and record the position of each character
		// in a tree map
		int nColumn 		= 1;
		TreeMap charTreeMap	= new TreeMap();
		for(int i=0; i<converter.charArray.size(); ++i){
			String strInChar = (String)converter.charArray.get(i);
			automataTable.setValueAt(strInChar, 0, nColumn);
			charTreeMap.put(strInChar, new Integer(nColumn));
			++nColumn;
		}

		// remove the start state from the tree
		// This is done because I want start state
		// to be at the top of the table, but by
		// traversing the tree this might not happen
		State DFAStartState 	= (State)treeMapDFA.get(strDFAStartState);
		treeMapDFA.remove(strDFAStartState);

		// Display DFA start state first
		automataTable.setValueAt(DFAStartState.strName, 1, 0);
		for(int i=0; i<DFAStartState.arrayTransitions.size(); ++i){
			Transition t = (Transition)DFAStartState.arrayTransitions.get(i);

			// find out where we need to write this transition into the table
			Integer nCol = (Integer)charTreeMap.get(t.transChar);
			automataTable.setValueAt(((State)t.stateArray.get(0)).strName, 1, nCol.intValue());
		}

		// Display the entries for all other states
		Iterator iter = treeMapDFA.values().iterator();
		int nRow = 2;
		while(iter.hasNext()){
			State tmpState = (State)iter.next();

			// display the name of this state in first column of the current row
			automataTable.setValueAt(tmpState.strName, nRow, 0);

			// Debuging purposes
			// System.out.println("---------------------------------");
			// System.out.println("Display State: "+tmpState.strName);

			for(int j=0; j<tmpState.arrayTransitions.size(); ++j){
				Transition t = (Transition)tmpState.arrayTransitions.get(j);

				// Debuging purposes
				// System.out.println("Transition: from " + tmpState.strName + " to " + ((State)t.stateArray.get(0)).strName);

				// find out where we need to write this transition into the table
				Integer nCol = (Integer)charTreeMap.get(t.transChar);
				automataTable.setValueAt(((State)t.stateArray.get(0)).strName, nRow, nCol.intValue());
			}

			++nRow;
		}
	}

	/**************************************************************************
		Displays the NFA table
	***************************************************************************/
	private void displayNFA(ArrayList arrayNFA){

		// Create table of the needed size
		getContentPane().remove(automataTable);
		automataTable = new JTable(arrayNFA.size()+1, regExParser.treeSetInput.size()+2);
		getContentPane().add(automataTable);
		automataTable.validate();

		// display States\Input string
		automataTable.setValueAt("States/Input", 0, 0);

		// Display input characters and record the position of each character
		// in a tree map
		int nColumn 		= 1;
		TreeMap inCharPos 	= new TreeMap();
		Iterator iter 		= regExParser.treeSetInput.iterator();
		while(iter.hasNext()){
			String strInChar = (String)iter.next();
			automataTable.setValueAt(strInChar, 0, nColumn);
			inCharPos.put(strInChar, new Integer(nColumn));
			++nColumn;
		}
		// last one is epsilon
		automataTable.setValueAt("epsilon", 0, nColumn);
		inCharPos.put("epsilon", new Integer(nColumn));

		// now traverse through the states array and display all transitions for it
		for(int i=0; i<arrayNFA.size(); ++i){
			State s = (State)arrayNFA.get(i);

			// display state name
			automataTable.setValueAt(s.strName, i+1, 0);

			// go thrugh all transitions and display them
			for(int j=0; j<s.arrayTransitions.size(); ++j){
				Transition t = (Transition)s.arrayTransitions.get(j);

				// get the position where the transition will be recorded
				Integer nCol = (Integer)inCharPos.get(t.transChar);

				// Put all states for this transition in displayable format - String
				String strStates = ((State)t.stateArray.get(0)).strName;
				for(int k=1; k<t.stateArray.size(); ++k)
					strStates += "," + ((State)t.stateArray.get(k)).strName;

				// first get value at that position and if there is something
				// already then we must separate it with ","
				String strEntry = (String)automataTable.getValueAt(i+1, nCol.intValue());
				if(strEntry != null)
					if(strEntry.length()>0)
						strEntry += "," + strStates;
					else strEntry = strStates;
				else strEntry = strStates;

				// Display the value in the table cell
				if(strEntry != null)
					automataTable.setValueAt(strEntry, i+1, nCol.intValue());
			}
		}
	}

}