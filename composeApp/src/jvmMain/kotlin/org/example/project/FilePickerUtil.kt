package org.example.project

import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.SwingUtilities

fun pickJsonFile(): File? {
    var result: File? = null
    // ensure dialog runs on AWT thread
    SwingUtilities.invokeAndWait {
        val chooser = JFileChooser()
        chooser.fileFilter = FileNameExtensionFilter("JSON files", "json")
        chooser.dialogTitle = "Select language JSON file"
        val res = chooser.showOpenDialog(null)
        if (res == JFileChooser.APPROVE_OPTION) result = chooser.selectedFile
    }
    return result
}
