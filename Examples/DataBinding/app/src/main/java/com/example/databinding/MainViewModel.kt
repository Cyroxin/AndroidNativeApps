package com.example.databinding

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class MainViewModel : ViewModel() {
    private var counter: MutableLiveData<String> = MutableLiveData("0")
    private var increment: MutableLiveData<Int> = MutableLiveData(0)

    fun getCounter(): MutableLiveData<String>? {
        return counter
    }

    fun setCounter(counter : String)
    {
        this.counter.value = counter
    }

    fun getIncrement(): MutableLiveData<Int>?
    {
        return increment
    }

    fun setIncrement(increment : Int)
    {
        this.increment?.postValue(increment)
    }

    fun incCounter() {
        counter?.postValue((counter!!.value!!.toInt() + increment!!.value!!).toString()) // this can be called from another coroutine
    }
}