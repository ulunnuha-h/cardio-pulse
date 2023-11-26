package com.example.cardiopulse.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cardiopulse.Model.ResultModel
import java.util.Date

class ResultViewModel : ViewModel() {
    private val list = mutableListOf<ResultModel>()
    val listLiveData = MutableLiveData<List<ResultModel>>()

    fun addResult(min: String?, avg: String?, max: String?, status: String, type: String){
        list.add(ResultModel(Integer.parseInt(min),Integer.parseInt(avg), Integer.parseInt(max), Date(), status, type))
        Log.d("Current Data", list.toString())
        listLiveData.value = list
    }
    fun getResults() = list

    fun getResultLiveData() : LiveData<List<ResultModel>> {
        return listLiveData
    }

    fun deleteResult(index : Int){
        list.removeAt(index);
        listLiveData.value = list;
    }
}