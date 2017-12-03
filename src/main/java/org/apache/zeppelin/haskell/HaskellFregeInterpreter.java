/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.zeppelin.haskell;

import java.io.PrintWriter;
import java.util.Properties;

import frege.prelude.Maybe;
import frege.prelude.PreludeBase;
import frege.runtime.Lambda;
import frege.runtime.Runtime;
import frege.runtime.WrappedCheckedException;
import frege.scriptengine.FregeScriptEngine.FregeCompiledScript;
import frege.scriptengine.FregeScriptEngine.FregeScriptEngineFactory;
import frege.scriptengine.FregeScriptEngine.JFregeScriptEngine;
import org.apache.zeppelin.interpreter.Interpreter;
import org.apache.zeppelin.interpreter.InterpreterContext;
import org.apache.zeppelin.interpreter.InterpreterResult;
import org.apache.zeppelin.interpreter.InterpreterResult.Code;
import org.apache.zeppelin.interpreter.InterpreterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HaskellFregeInterpreter extends Interpreter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HaskellFregeInterpreter.class);

    private JFregeScriptEngine engine;

    public HaskellFregeInterpreter(Properties property) {
        super(property);
    }

    @Override
    public void open() {
        engine = (JFregeScriptEngine) new FregeScriptEngineFactory().getScriptEngine();
    }

    @Override
    public void close() {
        engine = null;
    }

    @Override
    public InterpreterResult interpret(String script, InterpreterContext context) {

        try {

            PrintWriter output = new PrintWriter(context.out(), false);

            Runtime.stdout.set(output);
            Runtime.stderr.set(output);

            FregeCompiledScript compiledScript = (FregeCompiledScript) engine.compile(script);
            PreludeBase.TMaybe result = (PreludeBase.TMaybe) compiledScript.eval();

            if (Maybe.isNothing(result)) {
                if (Parser.hasDisplay(script)) {
                    result = (PreludeBase.TMaybe) engine.compile("display z_display").eval();
                } else if (Parser.hasMain(script)) {
                    result = (PreludeBase.TMaybe) engine.compile("z_main").eval();
                }
            }

            if (Maybe.isJust(result)) {
                Object val = Maybe.unJust(result);
                if (!(val instanceof Lambda)) {
                    return new InterpreterResult(Code.SUCCESS, String.valueOf(val));
                }
                ((Lambda) val).apply(1).result().forced();
                output.flush();
            }

            return new InterpreterResult(Code.SUCCESS);

        } catch (WrappedCheckedException e) {

            LOGGER.error("Exception in HaskellInterpreter while interpret ", e);

            return new InterpreterResult(Code.ERROR, e.getCause().getMessage());

        } catch (Exception e) {

            LOGGER.error("Exception in HaskellInterpreter while interpret ", e);

            return new InterpreterResult(Code.ERROR, InterpreterUtils.getMostRelevantMessage(e));

        }
    }

    @Override
    public void cancel(InterpreterContext context) {
    }

    @Override
    public FormType getFormType() {
        return FormType.SIMPLE;
    }

    @Override
    public int getProgress(InterpreterContext context) {
        return 0;
    }
}
