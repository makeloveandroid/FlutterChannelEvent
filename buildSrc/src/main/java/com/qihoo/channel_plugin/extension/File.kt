package com.qihoo.channel_plugin.extension

import java.io.File
import java.io.FileNotFoundException

fun File.eachFileRecurse(block: (File) -> Unit) {
    if (!exists()) {
        throw FileNotFoundException(absolutePath)
    } else require(isDirectory) { "The provided File object is not a directory: $absolutePath" }

    val files: Array<File> = this.listFiles()
    if (files != null) {
        val var5 = files.size
        for (var6 in 0 until var5) {
            val file = files[var6]
            if (file.isDirectory) {
                file.eachFileRecurse(block)
            } else  {
                block(file)
            }
        }
    }
}