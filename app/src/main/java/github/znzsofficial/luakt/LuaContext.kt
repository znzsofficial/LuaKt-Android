package github.znzsofficial.luakt

import android.content.Context
import org.luaj.vm2.Globals

interface LuaContext  {
    val context: Context?
    val luaState: Globals?

    fun sendMsg(msg: String?)
    fun sendError(title: String?, msg: Exception?)
    fun regGc(obj: LuaGcable?)
}



