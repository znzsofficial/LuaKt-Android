package github.znzsofficial.luakt

import dalvik.system.DexClassLoader

class LuaDexClassLoader(
    val dexPath: String,
    optimizedDirectory: String?,
    libraryPath: String?,
    parent: ClassLoader?
) : DexClassLoader(
    dexPath, optimizedDirectory, libraryPath, parent
) {
    private val classCache = HashMap<String, Class<*>?>()

    @Throws(ClassNotFoundException::class)
    override fun findClass(name: String): Class<*>? {
        var cls = classCache[name]
        if (cls == null) {
            cls = super.findClass(name)
            classCache[name] = cls
        }
        return cls
    }
}
