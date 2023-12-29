package github.znzsofficial.luakt

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.content.res.Resources.Theme
import android.util.Log
import dalvik.system.DexClassLoader
import github.znzsofficial.myapplication.LuaApplication
import org.luaj.vm2.LuaError
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.math.BigInteger
import java.security.MessageDigest

class LuaDexLoader(private val mContext: Context, private val luaDir: String) {

    val classLoaders = ArrayList<ClassLoader>()
    val librarys = HashMap<String, String>()
    private var mResources: LuaResources? = null
    var theme: Theme? = null
        private set
    private val odexDir: String

    init {
        odexDir = mContext.getDir("odex", Context.MODE_PRIVATE).absolutePath
    }

    fun loadApp(pkg: String?): LuaDexClassLoader? {
        try {
            var dex = dexCache[pkg]
            if (dex == null) {
                val manager = mContext.packageManager
                val info = manager.getPackageInfo(pkg!!, 0).applicationInfo
                dex = LuaDexClassLoader(
                    info.publicSourceDir,
                    odexDir,
                    info.nativeLibraryDir,
                    mContext.classLoader
                )
                dexCache[pkg] = dex
            }
            if (!classLoaders.contains(dex)) {
                classLoaders.add(dex)
            }
            return dex
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(LuaError::class)
    fun loadLibs() {
        val libs = File("$luaDir/libs").listFiles() ?: return
        for (f in libs) {
            if (f.isDirectory()) continue
            loadDex(f.absolutePath)
        }
    }

    @Throws(LuaError::class)
    fun loadLib(name: String) {
        var fn = name
        val i = name.indexOf(".")
        if (i > 0) fn = name.substring(0, i)
        if (fn.startsWith("lib")) fn = fn.substring(3)
        val libDir = mContext.getDir(fn, Context.MODE_PRIVATE).absolutePath
        val libPath = "$libDir/lib$fn.so"
        var f = File(libPath)
        if (!f.exists()) {
            f = File("$luaDir/libs/lib$fn.so")
            if (!f.exists()) throw LuaError("can not find lib $name")
            copyFile("$luaDir/libs/lib$fn.so", libPath)
        }
        librarys[fn] = libPath
    }

    @Throws(LuaError::class)
    fun loadDex(path: String): DexClassLoader {
        var path = path
        var dex = dexCache[path]
        if (dex == null) dex = loadApp(path)
        if (dex == null) {
            val name = path
            if (path[0] != '/') path = "$luaDir/$path"
            if (!File(path).exists()) path += if (File("$path.dex").exists()) ".dex" else if (File(
                    "$path.jar"
                )
                    .exists()
            ) ".jar" else throw LuaError("$path not found")
            var id = getFileMD5(path)
            if (id != null && id == "0") id = name
            dex = dexCache[id]
            if (dex == null) {
                dex = LuaDexClassLoader(
                    path,
                    odexDir,
                    LuaApplication.instance.applicationInfo.nativeLibraryDir,
                    mContext.classLoader
                )
                dexCache[id] = dex
            }
        }
        if (!classLoaders.contains(dex)) {
            classLoaders.add(dex)
            path = dex.dexPath
            if (path.endsWith(".jar")) loadResources(path)
        }
        return dex
    }

    fun loadResources(path: String?) {
        try {
            val assets = mContext.createPackageContext(mContext.packageName, 0).assets
            val superRes = mContext.resources
            mResources = LuaResources(
                assets, superRes.displayMetrics,
                superRes.configuration
            )
            mResources!!.setSuperResources(superRes)
            theme = mResources!!.newTheme()
            theme!!.setTo(mContext.theme)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val resources: Resources?
        get() = mResources

    companion object {
        private val dexCache = HashMap<String?, LuaDexClassLoader>()
        fun copyFile(from: String?, to: String?) {
            try {
                copyFile(FileInputStream(from), FileOutputStream(to))
            } catch (e: IOException) {
                Log.i("lua", e.message!!)
            }
        }

        fun copyFile(`in`: InputStream, out: OutputStream): Boolean {
            try {
                var byteread = 0
                val buffer = ByteArray(1024 * 1024)
                while (`in`.read(buffer).also { byteread = it } != -1) {
                    out.write(buffer, 0, byteread)
                }
                //in.close();
                //out.close();
            } catch (e: Exception) {
                Log.i("lua", e.message!!)
                return false
            }
            return true
        }

        fun getFileMD5(file: String?): String? {
            return getFileMD5(File(file))
        }

        fun getFileMD5(file: File?): String? {
            return try {
                getFileMD5(FileInputStream(file))
            } catch (e: FileNotFoundException) {
                null
            }
        }

        fun getFileMD5(`in`: InputStream): String? {
            val buffer = ByteArray(1024 * 1024)
            var len: Int
            try {
                `in`.use {
                    val digest = MessageDigest.getInstance("MD5")
                    while (`in`.read(buffer).also { len = it } != -1) {
                        digest.update(buffer, 0, len)
                    }
                    val bigInt = BigInteger(1, digest.digest())
                    return bigInt.toString(16)
                }
            } catch (ignored: Exception) {
            }
            return null
        }
    }
}
