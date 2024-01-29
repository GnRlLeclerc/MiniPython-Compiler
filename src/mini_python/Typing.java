package mini_python;

import java.util.HashMap;
import java.util.LinkedList;

class Typing {

	static boolean debug = false;

	// use this method to signal typing errors
	static void error(Location loc, String msg) {
		throw new Error(loc + "\nerror: " + msg);
	}

	static TFile file(File f) {
		TFile tFile = new TFile();

		for (Def d : f.l)
			Typer.functions.put(d.f.id, d);

		// Visit the main function. We will keep the local variables of main() as global
		// variables
		Typer mainTyper = new Typer();
		f.s.accept(mainTyper);
		LinkedList<Variable> mainParams = new LinkedList<Variable>();
		Function mainFunc = new Function("main", mainParams);
		TDef mainTDef = new TDef(mainFunc, mainTyper.currStmt);

		// Add the main function to the function list
		tFile.l.add(mainTDef);

		// Visit all other functions
		for (Def def : f.l) {
			Typer typer = new Typer();
			// Create a copy instead of using the same
			typer.vars = new HashMap<>(mainTyper.vars);
			LinkedList<Variable> params = new LinkedList<Variable>();
			for (Ident i : def.l) {
				Variable v = Variable.mkVariable(i.id);
				params.push(v);
				typer.vars.put(i.id, v);
			}

			// Accept this function and add it to the function list
			def.s.accept(typer);
			Function func = new Function(def.f.id, params);
			TDef tdef = new TDef(func, typer.currStmt);
			tFile.l.add(tdef);
		}
		return tFile;
	}

}
