package com.example.coroutines

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.NonCancellable.start
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //task1()
        //task2()
        //task3()
        //task4()
        //task5()
        //task6()
        //task7()

    }

    private fun task1() {
        // 1. A coroutine scope where the UI is being updated before and after a coroutine is executed on a different thread than the UI thread.
        Log.d(TAG, "From thread ${Thread.currentThread().name}")
        GlobalScope.launch(Dispatchers.IO) {
            Log.d(TAG, "From thread ${Thread.currentThread().name}")
        }
        Thread.sleep(1000)
        Log.d(TAG, "From thread ${Thread.currentThread().name}")
    }

    private fun task2() {
        // 2.A main-safe suspend function being called from UI thread
        GlobalScope.launch(Dispatchers.IO) {
            Log.d(TAG, "From thread ${Thread.currentThread().name}")
            doWork()
        }
    }

    private fun task3() {
        // 3. A coroutine that is cancelled on user action and a clean-up function that is executed if a coroutine gets cancelled
        val job = lifecycleScope.launch {
            while (true) {
                doWork()
            }
        }
        button.setOnClickListener {
            try {
                job.cancel()
            } catch (e: CancellationException) {
                textView.text = e.message
                throw e
            }
        }

    }

    private fun task4() {
        // 4. A coroutine that is launched when a lifecycle object is started and cancelled when it's stopped
        lifecycleScope.launch(Dispatchers.Default) {
            whenStarted {
                while (true) {
                    doWork()
                }
            }
        }
    }

    private fun task5() {
        // 5. A coroutine scope that launches multiple coroutines and returns the combined result of all them
        lifecycleScope.launch() {
            val time = measureTimeMillis {
                val answer1 = async { work() }
                val answer2 = async { work() }
                listOf(answer1, answer2).awaitAll()
                withContext(Dispatchers.Main) {
                    textView.text = "$answer1 $answer2"
                }
            }
            Log.d(TAG, "Requests took $time ms")
        }
    }


    private fun task6() {
        // 6. Multiple coroutines that perform operations on the same object without affecting its final result
        val mutex = Mutex()
        var counter = 0
        lifecycleScope.launch {
            withContext(Dispatchers.Default) {
                repeat(100) {
                    launch {
                        repeat(1000) {
                            mutex.withLock {
                                counter++
                                Log.d(TAG, "Count is: $counter")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun task7() {
        // 7. Coroutine usage in high order functions
        highOrder {
            doWork()
        }
    }

    suspend fun doWork() {
        withContext(Dispatchers.Main) {
            Log.d(TAG, "From thread ${Thread.currentThread().name}")
            delay(1000)
        }
    }

    suspend fun work(): String {
        delay(1000)
        Log.d(TAG, "Thread ${Thread.currentThread().name}")
        return "Thread ${Thread.currentThread().name}"
    }

    fun highOrder(work: suspend () -> Unit): Job {
        return lifecycleScope.launch {
            textView.text = getString(R.string.start)
            work()
            textView.text = getString(R.string.finish)
        }
    }

}
