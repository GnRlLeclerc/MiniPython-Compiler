package mini_python;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

class Typing {

	static boolean debug = false;

	// use this method to signal typing errors
	static void error(Location loc, String msg) {
		throw new Error(loc + "\nerror: " + msg);
	}

	static TFile file(File f) {
		TFile tFile = new TFile();
		Typer mainTyper = new Typer();

		for (Def d : f.l) {
			// Check if the function is already defined
			if (mainTyper.functions.containsKey(d.f.id)) {
				error(d.f.loc, "Function " + d.f.id + " is already defined");
			}
			LinkedList<Variable> params = new LinkedList<Variable>();
			HashSet<String> paramNames = new HashSet<String>();
			for (Ident i : d.l) {
				Variable v = Variable.mkVariable(i.id);
				if (paramNames.contains(v.name)) {
					error(i.loc, "Parameter " + v.name + " is already defined");
				}
				paramNames.add(v.name);
				params.push(v);
			}
			Function func = new Function(d.f.id, params);
			mainTyper.functions.put(d.f.id, func);
		}

		// Visit the main function. We will keep the local variables of main() as global
		// variables
		f.s.accept(mainTyper);
		LinkedList<Variable> mainParams = new LinkedList<Variable>();
		Function mainFunc = new Function("", mainParams);
		TDef mainTDef = new TDef(mainFunc, mainTyper.currStmt);

		// Add the main function to the function list
		tFile.l.add(mainTDef);

		HashSet<Function> currFunctions = new HashSet<>();
		// Visit all other functions
		for (Def def : f.l) {
			Typer typer = new Typer();

			// Create a copy instead of using the same
			typer.vars = new HashMap<>();
			Function func = mainTyper.functions.get(def.f.id);
			for (Variable v : func.params) {
				typer.vars.put(v.name, v);
			}
			currFunctions.add(func);
			for (Function prevFunc : currFunctions) {
				typer.functions.put(prevFunc.name, prevFunc);
			}

			// Accept this function and add it to the function list
			def.s.accept(typer);
			TDef tdef = new TDef(func, typer.currStmt);
			tFile.l.add(tdef);
		}
		return tFile;
	}

}
