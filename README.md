# Kog - Kotlin + Log

Another Android log library written by Kotlin

## Requirements

* Android SDK Lolipop

## Install

Add the dependency in your app's build.gradle

    dependencies {
        implementation 'com.simonslair.kog:kog-android:1.0'
    }

## Usage


    ```kotlin
    // Before starting your first log, you must initialize a global Kog instance
    
    // init global Kog instance with default settings
    Kog.default()
    
    // init global Kog instance with your own settings
    Kog.configure {
        implementation(AndroidLogcatImpl) // Use Android Logcat for logging implementation
        should { _, _, _ -> true } // Any logs will be logged
        should { level, _, _ -> level > KotLevel.debug } // Logs which level greater than debug will be logged
        blocklist("HTTP", "DEBUG") // Logs with these tags will not be logged
        allowlist("HTTP") // Logs with HTTP tag will still be logged even it's in the blocklist
        
        // Register a class you can now log object that is type of Throwable (or any subclass type)
        // using Kog.log(level, tag, throwable)
        convert(Throwable::java.class) {
            it.toString()
        }
    }
    
    // init a local Kog instance
    val kog = Kog.new {
        default() // Apply the default settings which will log all message using Android Logcat
    }
    ```
    
    // Write your own implementation
    Kog.configure {
        implementation({ level, tag, msg ->
            // log to files, etc.
            // handle the async logic yourself
        })
    }
    
    // Logging using global instance
    Kog.d("TAG", "Somethings happened")
    val httpLogger = Kog.tag("HTTP")
    httpLogger.d("Request something")
    httpLogger.d("Response data")
    
    // Logging using a local instance, which use its own settings
    val kog = Kog.new { default() }
    kog.log(KogLevel.debug, "DEBUG", "Debug message!")
    
    // Write an extension function to log
    fun Any.kog(): Tag {
        return Kog.tag(this::class.java.simpleName)
    }
    
    class Test {
        init {
            kog.d("Init") // will log using "Test" tag
        }
    }
