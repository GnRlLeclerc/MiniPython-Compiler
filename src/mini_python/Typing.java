package mini_python;

import mini_python.typing.Type;

import java.util.LinkedList;

class Typing {

	static boolean debug = false;

	// use this method to signal typing errors
	static void error(Location loc, String msg) {
		throw new Error(loc + "\nerror: " + msg);
	}

	static TFile file(File f) {
		TFile tFile = new TFile();

		// TODO : parse the return statements for return type. Will determine how we compile it
		// NOTE: using not dynamic types is a first optimisation (no runtime checking)
		// il va falloir faire le boilerplate malloc, ça va être rigolo. On pourra passer un register en argument
		// pour l'instant, finir les return types de fonctions, puis faire le print avec les checks d'instance.
		// NOTE: si dynamique, il faudra appeler la fonction ? jsp. Les checks au runtime seront plus chiants, il faudra
		// tout implémenter

		for (Def d : f.l) {
			Typer typer = new Typer();

			// Check if the function is already defined
			if (Typer.functions.containsKey(d.f.id)) {
				error(d.f.loc, "Function " + d.f.id + " is already defined");
			}

			// Add all function arguments as local variables
			LinkedList<Variable> params = new LinkedList<Variable>();
			for (Ident i : d.l) {
				Variable v = Variable.mkVariable(i.id, Type.DYNAMIC); // Because we do not use type hinting, arguments are dynamic by default
				// TODO: we could add type hinting in order to have more efficient statically typed arguments

				if (typer.vars.containsKey(v.name)) {
					error(i.loc, "Parameter " + v.name + " is already defined");
				}
				typer.vars.put(v.name, v);
				params.push(v);
			}

			// Create the function element and add it to the type checking function list
			Function func = new Function(d.f.id, params);
			Typer.functions.put(d.f.id, func);

			// Accept this function and parse its return statements
			typer.returns.clear(); // Clear the return statement list
			d.s.accept(typer);
			func.returnType = typer.currentReturnType(); // Find out the current return type of the function
			typer.setReturnTypes(func.returnType); // Set the return type of all return statements

			// Create the typed function definition and add it to the final list of functions.
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
