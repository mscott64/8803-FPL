import java.util.HashSet;
import java.util.List;
import java.util.Set;

import chord.project.Chord;
import chord.util.tuple.object.Pair;
import joeq.Compiler.Quad.Quad;
import joeq.Compiler.Quad.RegisterFactory.Register;

@Chord(name="reachdef")
public class ReachDefAnalysis extends DataflowAnalysis<Pair<Quad, Register>> {
	@Override
	public void doAnalysis() {
		// Implement your reaching definitions dataflow analysis here. 
		//
		// File DataflowAnalysis.java defines instance fields main, inMap, and
		// outMap, which will serve as the inputs and outputs of your analysis:
		//
		// INPUT: Method main.
		//
		// OUTPUT: Populate maps inMap and outMap with the results of your
		// reaching definitions analysis of method main.
		//
		// Specifically, for each Quad q in the control-flow graph of main,
		// inMap(q) and outMap(q) must contain the sets of all <Quad, Register>
		// definitions that may reach the entry and the exit, respectively, of
		// q.  You can leave a set either null or empty if it does not contain
		// any reaching definitions.
		//
		// Your analysis will be graded for the following aspects in decreasing
		// order of importance:
		//
		// 1. Correctness of the results produced by the analysis.
		// 2. Efficiency of the analysis, in particular, the number of times you
		// revisit each quad.
		// 3. Clarity of your source code.  While we will run automatic tests
		// to evaluate correctness, we might need to read your source code to
		// evaluate the efficiency of your analysis.  So, it is important that
		// your code be concise and readable.  Adding comments is not required
		// but it won't hurt if you comment your code.
		//
		// Add helper instance methods to this class if necessary.  All your
		// code must be in this class itself, and it must be written in Java
		// (for instance, you cannot use Datalog).
		//
		// Note: This is a single procedure analysis; you do not need to
		// consult any pointer analysis, call graph, or any method of the given
		// program besides the provided main method.
		
		main = Program.g().getMainMethod();
		if (main.isAbstract())
			throw new RuntimeException("Method " + main + " is abstract");
		ControlFlowGraph cfg = main.getCFG();
		
		int count = 0;
		boolean changed = true;
		List<Quad> quads;
		
		while(changed) {
			count++;
			changed = false;
			System.out.println("Iteration " + count + "...");
			
			for(BasicBlock bb : cfg.reversePostOrder()) {
				
				for(Quad q : bb.getQuads()) {
					Set<Register> out = new HashSet<Register>();
					Set<Register> in = new HashSet<Register>();
					List<RegisterOperand> def = q.getDefinedRegisters();
					List<RegisterOperand> used = q.getUsedRegisters();
					
					for(RegisterOperand ro : def) 
						in.add(ro.getRegister());
					
					for(RegisterOperand ro : used)
						out.add(ro.getRegister());
					
					Set<Register> prev_in = inMap.put(q, in);
					Set<Register> prev_out = outMap.put(q, out);
					
					if(!setEquals(prev_in, in) || !setEquals(prev_out, out)) 
						changed = true;
				}
			}
		}
		System.out.println("Finished after " + count + " iterations.");
	}
	
	public boolean setEquals(Set<Register> set1, Set<Register> set2) {
		
		if(set1 == null)
			return set2 == null;
		
		if(set2 == null)
			return set1 == null;
			
		if(set1.size() != set2.size()) 
			return false;
		
		for(Register r : set1) {
			if(!setContains(r, set2))
				return false;
		}
		
		return true;
	}
	
	public boolean setContains(Register r, Set<Register> s) {
		
		if(s.isEmpty())
			return false;
		
		for(Register curr : s)
			if(r.toString().equals(curr.toString()))
				return true;
		
		return false;
	}
}



