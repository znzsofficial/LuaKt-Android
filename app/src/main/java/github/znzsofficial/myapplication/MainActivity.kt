package github.znzsofficial.myapplication

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import com.amrdeveloper.codeview.CodeView
import com.github.only52607.luakt.lib.LuaKotlinExLib
import com.github.only52607.luakt.lib.LuaKotlinLib
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import github.znzsofficial.luakt.LuaContext
import github.znzsofficial.luakt.LuaGcable
import github.znzsofficial.luakt.R
import github.znzsofficial.luakt.databinding.ActivityMainBinding
import github.znzsofficial.luakt.databinding.DialogAddFileBinding
import okio.buffer
import okio.source
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaError
import org.luaj.vm2.lib.jse.JsePlatform
import java.io.File


var AppCompatActivity.contentView: View
    get() = this.window.decorView
    set(value) {
        this.setContentView(value)
    }

fun CodeView.readFile(file: File) {
    this.setText(try {
        file.source().buffer().use { it.readUtf8() }
    } catch (e: Exception) {
        e.toString()
    })
}

class MainActivity : AppCompatActivity(), LuaContext {
    private lateinit var mBinding: ActivityMainBinding
    private val toastbuilder = StringBuilder()
    private var toast: Toast? = null
    private var lastShow: Long = 0
    override val luaState: Globals = JsePlatform.standardGlobals()
    private val mGc = ArrayList<LuaGcable>()
    override val context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.setData(Uri.parse("package:$packageName"))
                startActivity(intent)
            }
        }
        ActivityCompat.requestPermissions(
            this,
            this.packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_PERMISSIONS
            ).requestedPermissions,
            1001
        )

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        this.contentView = mBinding.root
        setSupportActionBar(mBinding.toolbar)

        initView()
        luaState.load(LuaKotlinLib())
        luaState.load(LuaKotlinExLib())
        luaState.jset("activity", this)
        luaState.jset("this", this)
        luaState.set("print", github.znzsofficial.luakt.print(this))
    }

    private fun getTabInfo(tab: TabLayout.Tab?): String {
        val file = tab?.tag as File
        return "路径：" + file.path.toString() + "\n" +
                "position：" + tab.position.toString() + "\n" +
                "文件名：" + tab.text.toString() + "\n" +
                "存在：" + file.exists().toString()
    }

    private fun isDarkMode() =
        resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    private fun initView() {
        mBinding.mTab.addOnTabSelectedListener(tabListener)
        mBinding.toolbar.overflowIcon?.colorFilter = PorterDuffColorFilter(
            com.google.android.material.R.attr.colorTertiary,
            PorterDuff.Mode.SRC_ATOP
        )
        val jetBrainsMono = ResourcesCompat.getFont(this, R.font.jetbrains_mono_medium)

        val pairCompleteMap: MutableMap<Char, Char> = HashMap()
        pairCompleteMap['{'] = '}'
        pairCompleteMap['['] = ']'
        pairCompleteMap['('] = ')'
        pairCompleteMap['<'] = '>'
        pairCompleteMap['"'] = '"'
        pairCompleteMap['\''] = '\''

        val startSet: MutableSet<Char> = HashSet()
        startSet.add('{')
        val endSet: MutableSet<Char> = HashSet()
        endSet.add('}')

        mBinding.editor.apply {
            setPairCompleteMap(pairCompleteMap)
            enablePairComplete(true)
            enablePairCompleteCenterCursor(true)
            setTypeface(jetBrainsMono)
            setEnableAutoIndentation(true)
            setEnableLineNumber(true)
            setLineNumberTextColor(Color.GRAY)
            setLineNumberTextSize(25f)
            setEnableHighlightCurrentLine(true)
            setHighlightCurrentLineColor(0x22000000)
            setTabLength(4)
            setIndentationStarts(startSet)
            setIndentationEnds(endSet)

            customSelectionActionModeCallback =
                object : ActionMode.Callback {
                    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                        menu?.add(0, 0, 0, "Custom Action")
                        return true
                    }

                    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                        return true
                    }

                    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                        when (item?.itemId) {
                            0 -> {}
                        }
                        return false
                    }

                    override fun onDestroyActionMode(mode: ActionMode?) {
                        mBinding.editor.setSelected(false)
                    }
                }
        }

    }

    fun showToast(text: String?) {
        val now = System.currentTimeMillis()
        if (toast == null || now - lastShow > 1000) {
            toastbuilder.setLength(0)
            toast = Toast.makeText(this, text, Toast.LENGTH_LONG)
            toastbuilder.append(text)
            toast!!.show()
        } else {
            toastbuilder.append("\n")
            toastbuilder.append(text)
            toast!!.setText(toastbuilder.toString())
            toast!!.setDuration(Toast.LENGTH_LONG)
        }
        lastShow = now
    }

    override fun sendMsg(msg: String?) {
        runOnUiThread {
            showToast(msg)
        }
    }

    override fun regGc(obj: LuaGcable?) {
        mGc.add(obj!!)
    }

    override fun sendError(title: String?, msg: java.lang.Exception?) {
        sendMsg(title + ": " + msg?.message)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        mBinding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_add -> {
                    try {
                        val dialogView = DialogAddFileBinding.inflate(layoutInflater)
                        MaterialAlertDialogBuilder(this)
                            .setTitle("添加文件")
                            .setView(dialogView.root)
                            .setPositiveButton("确定") { _, _ ->
                                mBinding.mTab.addFile(
                                    dialogView.edit.text.toString()
                                )
                            }
                            .show()
                    } catch (e: Exception) {
                        Toast.makeText(this, "error：" + e.message, Toast.LENGTH_SHORT).show()
                    }
                    true
                }

                R.id.menu_exit -> {
                    finish()
                    true
                }

                R.id.menu_redo -> {
                    mBinding.editor.redo()
                    true
                }

                R.id.menu_undo -> {
                    mBinding.editor.undo()
                    true
                }

                R.id.menu_play -> {
                    try {
                        luaState.load(mBinding.editor.getText().toString()).call()
                    } catch (e: LuaError) {
                        MaterialAlertDialogBuilder(this)
                            .setTitle("LuaError")
                            .setMessage(e.message)
                            .setPositiveButton("确定", null)
                            .show()
                    }
                    true
                }

                else -> true
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    private val tabListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            val file = tab.tag as File
            mBinding.editor.readFile(file)
            setTitle(file.name)
        }

        override fun onTabReselected(tab: TabLayout.Tab) {
            val popup = PopupMenu(this@MainActivity, tab.view)
            popup.menuInflater.inflate(R.menu.menu_popup, popup.menu)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_delete -> {
                        mBinding.mTab.removeTab(tab)
                        mBinding.editor.setText("")
                        true
                    }

                    R.id.menu_info -> {
                        MaterialAlertDialogBuilder(this@MainActivity)
                            .setMessage(getTabInfo(tab))
                            .show()
                        true
                    }

                    else -> false
                }
            }
            popup.show()
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {
        }
    }
}
