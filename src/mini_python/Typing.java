package mini_python;

import java.util.LinkedList;

class Typing {

	static boolean debug = false;

	// use this method to signal typing errors
	static void error(Location loc, String msg) {
		throw new Error(loc + "\nerror: " + msg);
	}

	static TFile file(File f) {
		TFile tFile = new TFile();

		for (Def d : f.l) {
			Typer typer = new Typer();

			// Check if the function is already defined
			if (Typer.functions.containsKey(d.f.id)) {
				error(d.f.loc, "Function " + d.f.id + " is already defined");
			}
			LinkedList<Variable> params = new LinkedList<Variable>();
			for (Ident i : d.l) {
				Variable v = Variable.mkVariable(i.id);
				if (typer.vars.containsKey(v.name)) {
					error(i.loc, "Parameter " + v.name + " is already defined");
				}
				typer.vars.put(v.name, v);
				params.push(v);
			}
			Function func = new Function(d.f.id, params);
			Typer.functions.put(d.f.id, func);

			// Accept this function and add it to the function list
			d.s.accept(typer);
			TDef tdef = new TDef(func, typer.currStmt);
			tFile.l.add(tdef);
		}

		Typer mainTyper = new Typer();
		// Visit the main function. We will keep the local variables of main() as global
		// variables
		f.s.accept(mainTyper);
		LinkedList<Variable> mainParams = new LinkedList<Variable>();
		Function mainFunc = new Function("", mainParams);
		TDef mainTDef = new TDef(mainFunc, mainTyper.currStmt);

		// Add the main function to the function list
		tFile.l.add(mainTDef);

		return tFile;
	}

}
