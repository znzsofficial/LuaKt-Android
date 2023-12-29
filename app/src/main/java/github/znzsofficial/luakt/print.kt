package github.znzsofficial.luakt

import org.luaj.vm2.Globals
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.VarArgFunction

class print(context: LuaContext) : VarArgFunction() {
    private val globals: Globals
    private val mCotext: LuaContext

    init {
        mCotext = context
        globals = context.luaState!!
    }

    override fun invoke(args: Varargs): Varargs {
        val buf = StringBuilder()
        var i = 1
        val n = args.narg()
        while (i <= n) {
            buf.append(args.arg(i).tojstring())
            if (i < n) buf.append("    ")
            i++
        }
        mCotext.sendMsg(buf.toString())
        return NONE
    }
}


