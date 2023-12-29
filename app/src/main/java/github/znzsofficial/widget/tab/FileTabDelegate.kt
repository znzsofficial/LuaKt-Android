package github.znzsofficial.widget.tab

import com.google.android.material.tabs.TabLayout
import java.io.File

class FileTabDelegate {
    private var file: File = File("/")

    operator fun getValue(thisRef: Any?, property: Any?): File {
        return file
    }

    operator fun setValue(thisRef: Any?, property: Any?, value: File) {
        file = value
    }
}

var TabLayout.Tab.file by FileTabDelegate()