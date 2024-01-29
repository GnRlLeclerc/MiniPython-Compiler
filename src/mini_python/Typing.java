package mini_python;

import java.util.LinkedList;

class Typing {

	static boolean debug = false;

	// use this method to signal typing errors
	static void error(Location loc, String msg) {
		throw new Error(loc + "\nerror: " + msg);
	}

	static TFile file(File f) {
		for (Def d: f.l)
			Typer.functions.put(d.f.id, d);
		f.s.accept(new Typer());
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
