import chord.project.Chord;
import chord.project.analyses.JavaAnalysis;
import chord.program.Program;

import joeq.Class.jq_Method;
import joeq.Compiler.Quad.*;
import joeq.Compiler.Quad.RegisterFactory.Register;
import joeq.Compiler.Quad.Operand.RegisterOperand;

import java.util.*;

@Chord(name="liveness")
public class LivenessAnalysis extends DataflowAnalysis<Register> {
	@Override
	public void doAnalysis() {
		// Implement your liveness dataflow analysis here. 
		//
		// File DataflowAnalysis.java defines instance fields main, inMap, and
		// outMap, which will serve as the inputs and outputs of your analysis:
		//
		// INPUT: Method main.
		//
		// OUTPUT: Populate maps inMap and outMap with the results of your
		// liveness analysis of method main.
		//
		// Specifically, for each Quad q in the control-flow graph of main,
		// inMap(q) and outMap(q) must contain the sets of all Registers that
		// may be live on entry and on exit, respectively, of q.  You can leave
		// a set either null or empty if it does not contain any registers.
		//
		// Your analysis will be graded for the following aspects in decreasing
		// order of importance:
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
		boolean last = true;
		List<Quad> quads;
		Set<Register> out, in;
		
		while(changed) {
			count++;
			changed = false;
			System.out.println("Iteration " + count + "...");
			
			for(BasicBlock bb : cfg.reversePostOrderOnReverseGraph()) {
				
				quads = bb.getQuads();
				last = true;
				for(int i = quads.size() - 1; i >= 0; i--) {
					Quad q = quads.get(i);
					Set<Quad> succs = getSuccessors(q, bb, last, i); 
					last = false;
					
					out = new HashSet<Register>();
					in = new HashSet<Register>();
					
					// Union of entry of successors
					for(Quad succ_q : succs) {
						if(inMap.get(succ_q) != null)
							out.addAll(inMap.get(succ_q));
					}
					
					List<RegisterOperand> def = q.getDefinedRegisters();
					List<RegisterOperand> used = q.getUsedRegisters();
					
					in.addAll(out);
					// Remove kill set
					for(RegisterOperand ro : def) 
						in = setRemove(ro.getRegister(), in);
					
					// Add gen set
					for(RegisterOperand ro : used)
						in.add(ro.getRegister());
					
					Set<Register> prev_in = inMap.put(q, in);
					Set<Register> prev_out = outMap.put(q, out);
					
					//if(!setEquals(prev_in, in) || !setEquals(prev_out, out))
					if(prev_in == null || (prev_in != null && !prev_in.equals(in)) || prev_out == null || (prev_out != null && !prev_out.equals(out)))
						changed = true;
				}
			}
		}
		System.out.println("Finished after " + count + " iterations.");
	}
	
	private boolean setEquals(Set<Register> set1, Set<Register> set2) {
		
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
	
	private boolean setContains(Register r, Set<Register> s) {
		
		if(s.isEmpty())
			return false;
		
		String r_str = r.toString();
		for(Register curr : s)
			if(r_str.equals(curr.toString()))
				return true;
		
		return false;
	}
	
	private Set<Quad> getSuccessors(Quad q, BasicBlock bb, boolean last, int index) {
		
		Set<Quad> succs = new HashSet<Quad>();
		
		if(last) {
			for(BasicBlock succ_bb : bb.getSuccessors()) 
				if(succ_bb.size() > 0)
					succs.add(succ_bb.getQuad(0));
			
			return succs;
		}
		
		succs.add(bb.getQuad(index + 1));
		return succs;
	}
	
	private Set<Register> setRemove(Register r, Set<Register> in) {
		
		String r_str = r.toString();
		for(Register curr : in) {
			if(r_str.equals(curr.toString())) {
				in.remove(curr);
				return in;
			}
		}
		
		return in;
	}
}

