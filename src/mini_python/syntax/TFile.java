package mini_python.syntax;

import java.util.LinkedList;

/*
 * visitor for the typed trees
 * (feel free to modify it for your needs)
 */

public class TFile {
    public final LinkedList<TDef> l;
    // the block of global statements is now a `main` function

    public TFile() {
        super();
        this.l = new LinkedList<>();
    }
}
