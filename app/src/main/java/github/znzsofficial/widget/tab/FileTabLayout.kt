package github.znzsofficial.widget.tab

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.tabs.TabLayout
import java.io.File

class FileTabLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : TabLayout(context, attrs) {
    private var onRemovedListener: OnTabRemovedListener? = null

    init {
        tabGravity = GRAVITY_START
    }

    fun addOnTabRemovedListener(listener: OnTabRemovedListener) {
        onRemovedListener = listener
    }

    private fun newTab(file: File): Tab {
        val tab = newTab()
        tab.text = file.name
        tab.tag = file
        return tab
    }

    // 添加文件到 TabLayout.Tab，并添加到 TabLayout 中
    fun addFile(file: File): Tab? {
        val possibleTab = getTabByFile(file)
        if (possibleTab != null) {
            selectTab(possibleTab)
            return null
        }
        val tab = newTab(file)
        addTab(tab)
        return tab
    }

    // 使用路径添加文件到 TabLayout.Tab，并添加到 TabLayout 中
    fun addFile(path: String): Tab? {
        return addFile(File(path))
    }

    fun getPositionByTab(tab: Tab): Int {
        for (i in 0..<tabCount) {
            val currentTab = getTabAt(i)
            if (currentTab == tab) {
                return i
            }
        }
        return 0
    }

    fun getTabByFile(file: File?): Tab? {
        for (i in 0..<tabCount) {
            val currentTab = getTabAt(i)
            if (currentTab?.tag == file) {
                return currentTab
            }
        }
        return null
    }

    fun selectTabAt(index: Int, animate: Boolean = false) {
        selectTab(getTabAt(index), animate)
    }

    override fun removeTab(tab: Tab) {
        onRemovedListener
            ?.onTabRemoved(tab, getPositionByTab(tab))
        super.removeTab(tab)
    }

    override fun removeTabAt(position: Int) {
        getTabAt(position)?.let {
            onRemovedListener
                ?.onTabRemoved(it, position)
        }
        super.removeTabAt(position)
    }

    fun removeTabByPath(path: String) {
        val position = 0
        for (i in 0 until tabCount) {
            val currentTab = getTabAt(i)
            if ((currentTab?.tag as File).path == path) {
                onRemovedListener
                    ?.onTabRemoved(currentTab, position)
                removeTabAt(i)
                break
            }
        }
    }

    interface OnTabRemovedListener {
        fun onTabRemoved(tab: Tab, position: Int)
    }
}
