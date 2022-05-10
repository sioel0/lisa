package it.unive.lisa.analysis.string;

public class Main {
	public static void main(String[] args) {
		Automaton A = new Automaton();
		State[] states = new State[11];
		State s;
	
		for(int i = 0; i < 11; ++i) {
			boolean init = false, fin = false;
			if(i == 0)
				init = true;
			if(i == 10)
				fin = true;
			s = new State(i, init, fin);
			states[i] = s;
			A.addState(s);
		}
		
		A.addTransition(new Transition(states[0],states[1],' '));
		A.addTransition(new Transition(states[0],states[7],' '));
		A.addTransition(new Transition(states[1],states[2],' '));
		A.addTransition(new Transition(states[1],states[4],' '));
		A.addTransition(new Transition(states[2],states[3],'a'));
		A.addTransition(new Transition(states[3],states[6],' '));
		A.addTransition(new Transition(states[4],states[5],'b'));
		A.addTransition(new Transition(states[5],states[6],' '));
		A.addTransition(new Transition(states[6],states[1],' '));
		A.addTransition(new Transition(states[6],states[7],' '));
		A.addTransition(new Transition(states[7],states[8],'a'));
		A.addTransition(new Transition(states[8],states[9],'b'));
		A.addTransition(new Transition(states[9],states[10],'b'));
//		states[0] = new State(0, true, false);
//		states[1] = new State(1, false, true);
//		
//		A.addState(states[0]);
//		A.addState(states[1]);
//		
//		A.addTransition(new Transition(states[0], states[0], 'a'));
//		A.addTransition(new Transition(states[0], states[0], 'b'));
//		A.addTransition(new Transition(states[0], states[1], 'b'));
		
		if(A.validateString("abb"))
			System.out.println("Valid");
		else
			System.out.println("Not valid");
		
	}
	
}
