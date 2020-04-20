package com.qihoo.channel_processor

import javax.annotation.processing.Messager
import javax.tools.Diagnostic

class Logger(private val msg: Messager) {
    /**
     * Print info log.
     */
    fun info(info: CharSequence) {
        if (info.isNotEmpty()) {
            msg.printMessage(Diagnostic.Kind.NOTE, Consts.PREFIX_OF_LOGGER + info)
        }
    }

    fun error(error: CharSequence) {
        if (error.isNotEmpty()) {
            msg.printMessage(
                Diagnostic.Kind.ERROR,
                Consts.PREFIX_OF_LOGGER.toString() + "An exception is encountered, [" + error + "]"
            )
        }
    }

    fun error(error: Throwable?) {
        if (null != error) {
            msg.printMessage(
                Diagnostic.Kind.ERROR,
                Consts.PREFIX_OF_LOGGER.toString() + "An exception is encountered, [" + error.message + "]" + "\n" + formatStackTrace(
                    error.stackTrace
                )
            )
        }
    }

    fun warning(warning: CharSequence) {
        if (warning.isNotEmpty()) {
            msg.printMessage(Diagnostic.Kind.WARNING, Consts.PREFIX_OF_LOGGER + warning)
        }
    }

    private fun formatStackTrace(stackTrace: Array<StackTraceElement>): String {
        val sb = StringBuilder()
        for (element in stackTrace) {
            sb.append("    at ").append(element.toString())
            sb.append("\n")
        }
        return sb.toString()
    }

}