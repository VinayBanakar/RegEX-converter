package pro_try;
import java.util.*;
public class State implements Comparable{

	public String strName;

	public ArrayList arrayTransitions = new ArrayList();

	public ArrayList NFAStates = new ArrayList();


	public State(){
	}

	 
	public State(String name){
		strName = name;
	}

	
	public State(ArrayList sArray){
		NFAStates.addAll(sArray);

		if(NFAStates.size()>0){
			strName = "{" + ((State)(NFAStates.get(0))).strName;
			for(int i=1; i<NFAStates.size(); ++i)
				strName += "," + ((State)(NFAStates.get(i))).strName;
			strName += "}";
		}
	}

	public int compareTo(Object s){
		return(strName.compareTo(((State)s).strName));
	}
}