package com.example.testble

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope



// This MutableLiveData mechanism is used for sharing centralized beacon data with the ViewControllers

//maybe it needs to changed , viewmodel class is not as good as expected  !!
class ScanViewModel() : ViewModel() {

//    val beacons: MutableLiveData<Collection<Beacon>> by lazy {
//        MutableLiveData<Collection<Beacon>>()
//    }

    var isScanning: MutableLiveData<Boolean> = MutableLiveData()




}
