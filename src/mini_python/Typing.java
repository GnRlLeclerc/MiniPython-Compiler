package mini_python;

import java.util.LinkedList;

import mini_python.exception_handling.CompilationException;
import mini_python.syntax.Def;
import mini_python.syntax.File;
import mini_python.syntax.Function;
import mini_python.syntax.Ident;
import mini_python.syntax.Location;
import mini_python.syntax.TDef;
import mini_python.syntax.TFile;
import mini_python.syntax.Variable;
import mini_python.syntax.constants.Cnone;
import mini_python.syntax.exprs_typed.TEcst;
import mini_python.syntax.stmts_typed.TSblock;
import mini_python.syntax.stmts_typed.TSreturn;
import mini_python.typing.Type;

class Typing {

	static boolean debug = false;

	// use this method to signal typing errors
	static void error(Location loc, String msg) {
		throw new Error(loc + "\nerror: " + msg);
	}

	static TFile file(File f) throws CompilationException {
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

			int retroStackOffset = 16;
			for (Ident i : d.l) {
				// Because Ecall statements prepare the arguments on the stack, we do not need
				// to copy them.
				// The arguments offset go up the previous stack frame instead of down the
				// current one
				Variable v = Variable.mkVariable(i.id, Type.DYNAMIC, retroStackOffset); // Because we do not use type
																						// hinting, arguments are
																						// dynamic by default
				retroStackOffset += 8;

				if (typer.vars.containsKey(v.name)) {
					error(i.loc, "Parameter " + v.name + " is already defined");
				}
				typer.vars.put(v.name, v);
				params.push(v);
			}

			// Add the function to the list of functions that are valid to be called
			Typer.defs.put(d.f.id, d);
			Typer.functions.put(d.f.id, func);

			// Accept this function and parse its return statements
			// Clear the return statement list & tell the typer that all new variable
			// declarations are local to this function
			typer.returns.clear();
			typer.currentFunction = func;

			// Accept the function body
			d.s.accept(typer);

			// Set the return type of the function by parsing all return statements
			func.returnType = typer.currentReturnType(); // Find out the current return type of the function
			typer.setReturnTypes(func.returnType); // Set the return type of all return statements

			// If there is no return statement, add a default return None statement
			TSblock block = (TSblock) typer.currStmt;
			if (typer.returns.isEmpty()) {
				block.l.add(new TSreturn(new TEcst(new Cnone(null))));
			}

			TDef tdef = new TDef(func, block);
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
