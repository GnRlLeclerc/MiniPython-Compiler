package mini_python;

import java.util.HashMap;

class Typing {

	static boolean debug = false;

	// use this method to signal typing errors
	static void error(Location loc, String msg) {
		throw new Error(loc + "\nerror: " + msg);
	}

	static TFile file(File f) {
		for (Def d : f.l)
			Typer.functions.put(d.f.id, d);

		// Visit the main function. We will keep the local variables of main() as global variables
		Typer mainTyper = new Typer();
		f.s.accept(mainTyper);

		// Visit all other functions
		for (Def d : f.l) {
			Typer typer = new Typer();
			// Create a copy instead of using the same
			typer.vars = new HashMap<>(mainTyper.vars);

			// Accept this function and add it to the function list
			d.s.accept(typer);
		}


		// for (Def def : f.l) {
		// 	LinkedList<Variable> params = new LinkedList<Variable>();
		// 	for (Ident i : def.l) {
		// 		Variable v = Variable.mkVariable(i.id);
		// 		params.push(v);
		// 	}
		// 	Function func = new Function(def.f.id, params);
		// 	TDef tdef = new TDef(func, null);
		// }
		return Typer.tFile;
	}

}
