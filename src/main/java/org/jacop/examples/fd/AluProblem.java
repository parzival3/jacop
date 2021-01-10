package org.jacop.examples.fd;

import org.jacop.constraints.*;
import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.search.*;

import java.util.ArrayList;
import java.util.List;

public class AluProblem {

    public List<IntVar> vars;

    /**
     * It specifies the cost function, null if no cost function is used.
     */
    public IntVar cost;

    /**
     * It specifies the constraint store responsible for holding information
     * about constraints and variables.
     */
    public Store store;

    /**
     * It specifies the search procedure used by a given example.
     */
    public Search<IntVar> search;

    public void model() {
        store = new Store();
        vars = new ArrayList<IntVar>();

        IntVar a = new IntVar(store, "a", 0, 255);
        IntVar b = new IntVar(store, "b", 0, 255);
        IntVar fn = new IntVar(store, "fn", 0, 4);

        vars.add(a);
        vars.add(b);
        vars.add(fn);

        IntVar tmpSum = new IntVar(store, "tmpSum", -512, 512);
        store.impose(new XplusYeqZ(a, b, tmpSum));
        store.impose(new XltC(tmpSum, 255));

        IntVar tmpMul = new IntVar(store, "tmpMul", -512, 512);
        store.impose(new XmulYeqZ(a, b, tmpMul));
        store.impose(new XltC(tmpMul, 255));

        IntVar tmpDiff = new IntVar(store, "tmpDiff", -512, 512);
        IntVar tmpNeg = new IntVar(store, "tmpNeg", -a.max(), -a.min());
        store.impose(new XplusYeqC(a,tmpNeg, 0));
        store.impose(new XplusYeqZ(b, tmpNeg, tmpDiff));
        store.impose(new XgtC(tmpDiff, 0));

        store.impose(new XltC(fn, 3));
    }

    public boolean solve() {
        long T1, T2;
        T1 = System.currentTimeMillis();

        SelectChoicePoint<IntVar> select = new SimpleSelect<>(vars.toArray(new IntVar[1]), null, new IndomainMin<>());

        search = new DepthFirstSearch<>();

        boolean result = search.labeling(store, select);

        if (result)
            store.print();

        T2 = System.currentTimeMillis();

        System.out.println("\n\t*** Execution time = " + (T2 - T1) + " ms");

        System.out.println();
        System.out.print(search.getNodes() + "\t");
        System.out.print(search.getDecisions() + "\t");
        System.out.print(search.getWrongDecisions() + "\t");
        System.out.print(search.getBacktracks() + "\t");
        System.out.print(search.getMaximumDepth() + "\t");

        return result;

    }

    public static void main(String args[]) {

        AluProblem vector = new AluProblem();


        vector.model();

        if (vector.solve())
            System.out.println("Solution(s) found");

    }
}

