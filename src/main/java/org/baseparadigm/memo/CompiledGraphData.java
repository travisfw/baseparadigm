package org.baseparadigm.memo;

import java.util.Iterator;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.baseparadigm.GraphData;
import org.baseparadigm.GraphDatum;

/**
 * A CompiledScript is a collection of graph edges, the
 *  same as GraphData, which also implements Iterable<GraphDatum>.
 * 
 * @author travis@traviswellman.com
 */
public class CompiledGraphData extends CompiledScript implements Iterable<GraphDatum>{
    
    private MemoScriptEngine mse;
    public GraphData graphData;

    protected CompiledGraphData (MemoScriptEngine mse, GraphData gd) {
        this.mse = mse;
        this.graphData = gd;
    }

    @Override
    public Object eval(ScriptContext context) throws ScriptException {
        // TODO Auto-generated method stub
        throw new Error("unimplemented");
    }

    @Override
    public ScriptEngine getEngine() {
        return mse;
    }

    @Override
    public Iterator<GraphDatum> iterator() { return graphData.iterator(); }
    
}
