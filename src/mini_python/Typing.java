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

		for (Def d : f.l) {
			Typer typer = new Typer();

			// Check if the function is already defined
			if (Typer.functions.containsKey(d.f.id)) {
				error(d.f.loc, "Function " + d.f.id + " is already defined");
			}

			// Add all function arguments as local variables
			LinkedList<Variable> params = new LinkedList<>();
			// Create the function element. We link it with the list of parameters right now
			Function func = new Function(d.f.id, params);

			for (Ident i : d.l) {
				Variable v = Variable.mkVariable(i.id, Type.DYNAMIC, func.getStackFrameOffset()); // Because we do not use type hinting, arguments are dynamic by default

				if (typer.vars.containsKey(v.name)) {
					error(i.loc, "Parameter " + v.name + " is already defined");
				}
				typer.vars.put(v.name, v);
				params.push(v);
			}

			// Add the function to the list of functions that are valid to be called
			Typer.functions.put(d.f.id, func);

			// Accept this function and parse its return statements
			// Clear the return statement list & tell the typer that all new variable declarations are local to this function
			typer.returns.clear();
			typer.currentFunction = func;

			// Accept the function body
			d.s.accept(typer);

			// Set the return type of the function by parsing all return statements
			func.returnType = typer.currentReturnType(); // Find out the current return type of the function
			typer.setReturnTypes(func.returnType); // Set the return type of all return statements

			// Create the typed function definition and add it to the final list of functions.
			TDef tdef = new TDef(func, typer.currStmt);
			tFile.l.add(tdef);
		}

		Typer mainTyper = new Typer();
		// Visit the main function. We will keep the local variables of main() as global
		// variables
		LinkedList<Variable> mainParams = new LinkedList<>();
		Function mainFunc = new Function("", mainParams);
		mainTyper.currentFunction = mainFunc; // Same for the main function scope
		f.s.accept(mainTyper);
		TDef mainTDef = new TDef(mainFunc, mainTyper.currStmt);

		// Add the main function to the function list
		tFile.l.add(mainTDef);

		return tFile;
	}

}
