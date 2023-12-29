package github.znzsofficial.myapplication

import android.app.Application

class LuaApplication : Application() {
    private val instance = this

    companion object {
        val instance: LuaApplication
            get() = instance
    }
}