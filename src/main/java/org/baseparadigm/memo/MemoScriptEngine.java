package org.baseparadigm.memo;

import java.io.Reader;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

public class MemoScriptEngine implements ScriptEngine, Compilable {

    @Override
    public Object eval(String script, ScriptContext context)
            throws ScriptException {
        // TODO Auto-generated method stub
        throw new Error("unimplemented");
    }

    @Override
    public Object eval(Reader reader, ScriptContext context)
            throws ScriptException {
        // TODO Auto-generated method stub
        throw new Error("unimplemented");
    }

    @Override
    public Object eval(String script) throws ScriptException {
        // TODO Auto-generated method stub
        throw new Error("unimplemented");
    }

    @Override
    public Object eval(Reader reader) throws ScriptException {
        // TODO Auto-generated method stub
        throw new Error("unimplemented");
    }

    @Override
    public Object eval(String script, Bindings n) throws ScriptException {
        // TODO Auto-generated method stub
        throw new Error("unimplemented");
    }

    @Override
    public Object eval(Reader reader, Bindings n) throws ScriptException {
        // TODO Auto-generated method stub
        throw new Error("unimplemented");
    }

    @Override
    public void put(String key, Object value) {
        // TODO Auto-generated method stub
        throw new Error("unimplemented");
    }

    @Override
    public Object get(String key) {
        // TODO Auto-generated method stub
        throw new Error("unimplemented");
    }

    @Override
    public MemoBindings getBindings(int scope) {
        // TODO Auto-generated method stub
        throw new Error("unimplemented");
    }

    @Override
    public void setBindings(Bindings bindings, int scope) {
        // TODO Auto-generated method stub
        if (! (bindings instanceof MemoBindings))
            throw new IllegalArgumentException("needs a MemoBindings instance");
        throw new Error("unimplemented");
    }

    @Override
    public MemoBindings createBindings() {
        return new MemoBindings();
    }

    @Override
    public ScriptContext getContext() {
        // TODO Auto-generated method stub
        throw new Error("unimplemented");
    }

    @Override
    public void setContext(ScriptContext context) {
        // TODO Auto-generated method stub
        throw new Error("unimplemented");
    }

    @Override
    public ScriptEngineFactory getFactory() {
        // TODO Auto-generated method stub
        throw new Error("unimplemented");
    }

    @Override
    public CompiledScript compile(String script) throws ScriptException {
        // TODO Auto-generated method stub
        throw new Error("unimplemented");
    }

    @Override
    public CompiledScript compile(Reader script) throws ScriptException {
        // TODO Auto-generated method stub
        throw new Error("unimplemented");
    }

}
