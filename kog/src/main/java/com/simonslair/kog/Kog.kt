package com.simonslair.kog

import android.util.Log

/**
 * @author Simon Yu
 */
typealias LogImplementation = (level: Int, tag: String, message: String) -> Unit

typealias MessageConvert<T> = (t: T) -> String


object AndroidLogcatImpl : LogImplementation {
    override fun invoke(level: Int, tag: String, message: String) {
        Log.println(level, tag, message)
    }
}

object KogLevel {
    const val verbose = Log.VERBOSE
    const val debug = Log.DEBUG
    const val info = Log.INFO
    const val warn = Log.WARN
    const val error = Log.ERROR
    const val assert = Log.ASSERT
}

class Configure {
    var allowlist = listOf<String>()
    var blocklist = listOf<String>()
    var shoulds = listOf<(Int, String, String) -> Boolean>()
    var implementation: LogImplementation = { _, _, _ ->
        // dump implementation
    }
    var converts = mapOf<Class<*>, MessageConvert<Any>>()

    class Builder {

        val configure = Configure()

        fun allowlist(vararg tags: String) {
            configure.allowlist += tags
        }

        fun blocklist(vararg tags: String) {
            configure.blocklist += tags
        }

        fun should(should: (level: Int, tag: String, msg: String) -> Boolean) {
            configure.shoulds += should
        }

        fun implementation(implementation: LogImplementation) {
            configure.implementation = implementation
        }

        fun convert(clz: Class<*>, convert: MessageConvert<Any>) {
            configure.converts += clz to convert
        }

        fun default() {
            configure.implementation = AndroidLogcatImpl
            configure.shoulds += { _, _, _ -> true }
        }

        fun build(): Configure {
            return configure
        }
    }
}

/**
 * A log with a tag, usually create by calling Kog.tag(tagName)
 *
 */
class Tag(private val tag: String, private val kog: Kog) {
    fun log(level: Int, msg: String) {
        kog.log(level, this.tag, msg)
    }

    fun log(level: Int, obj: Any) {
        kog.log(level, this.tag, obj)
    }

    fun d(msg: String) {
        kog.log(KogLevel.debug, tag, msg)
    }

    fun e(msg: String) {
        kog.log(KogLevel.error, tag, msg)
    }

    fun i(msg: String) {
        kog.log(KogLevel.info, tag, msg)
    }

    fun v(msg: String) {
        kog.log(KogLevel.verbose, tag, msg)
    }

    fun a(msg: String) {
        kog.log(KogLevel.assert, tag, msg)
    }

    fun w(msg: String) {
        kog.log(KogLevel.warn, tag, msg)
    }

}

/**
 * Kotlin Logger
 */
class Kog(private val configure: Configure) {

    private val tags = mutableMapOf<String, Tag>()

    fun tag(tag: String): Tag {
        return tags.getOrPut(tag, {
            Tag(tag, instance)
        })
    }

    /**
     * Generic log function
     * @param level Log level defined in KotLevel
     * @param tag Tag to be logged
     * @param msg Message to be logged
     */
    fun log(level: Int, tag: String, msg: String) {
        if (configure.shoulds.all {
                it(level, tag, msg)
            }) {
            if (tag in configure.allowlist) {
                configure.implementation(level, tag, msg)
            } else if (tag !in configure.blocklist) {
                configure.implementation(level, tag, msg)
            }
        }
    }

    fun log(level: Int, tag: String, obj: Any) {
        configure.converts.forEach { convert ->
            if (convert.key.isAssignableFrom(obj.javaClass)) {
                log(level, tag, convert.value(obj))
            }
        }
    }

    companion object {
        private lateinit var instance: Kog

        /**
         * Get a logger for logging all message using this tag
         */
        fun tag(tag: String): Tag {
            return instance.tags.getOrPut(tag, {
                Tag(tag, instance)
            })
        }

        fun new(init: Configure.Builder.() -> Unit): Kog {
            val builder = Configure.Builder()
            init(builder)
            return Kog(builder.build())
        }

        /**
         * Configure a global Kog instance for logging in your application
         */
        @Synchronized
        fun configure(init: Configure.Builder.() -> Unit) {
            val builder = Configure.Builder()
            init(builder)
            instance = Kog(builder.build())
        }

        /**
         * Configure a global Kog instance with default settings
         */
        fun default() {
            configure {
                implementation(AndroidLogcatImpl)
                should { _, _, _ -> true }
            }
        }

        /**
         * Log with debug level
         */
        fun d(tag: String, msg: String) {
            log(KogLevel.debug, tag, msg)
        }

        /**
         * Log with error level
         */
        fun e(tag: String, msg: String) {
            log(KogLevel.error, tag, msg)
        }

        /**
         * Log with info level
         */
        fun i(tag: String, msg: String) {
            log(KogLevel.info, tag, msg)
        }

        /**
         * Log with verbose level
         */
        fun v(tag: String, msg: String) {
            log(KogLevel.verbose, tag, msg)
        }

        /**
         * Log with assert level
         */
        fun a(tag: String, msg: String) {
            log(KogLevel.assert, tag, msg)
        }

        /**
         * Log with warn level
         */
        fun w(tag: String, msg: String) {
            log(KogLevel.warn, tag, msg)
        }

        fun log(level: Int, tag: String, msg: String) {
            instance.log(level, tag, msg);
        }

        fun log(level: Int, tag: String, obj: Any) {
            instance.log(level, tag, obj);
        }
    }
}


