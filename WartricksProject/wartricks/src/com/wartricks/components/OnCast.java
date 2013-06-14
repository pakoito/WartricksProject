
package com.wartricks.components;

import com.artemis.Entity;
import com.artemis.World;
import com.wartricks.logic.GameMap;
import com.wartricks.utils.LoadScript;

public class OnCast extends AbstractScriptAction {
    public OnCast(String scriptName, String methodName) {
        super(scriptName, methodName);
    }

    public void execute(GameMap gameMap, World gameWorld, Entity caster) {
        final LoadScript script = new LoadScript(path);
        script.runUnboundScriptFunction(method, gameMap, gameWorld, caster);
    }
}
