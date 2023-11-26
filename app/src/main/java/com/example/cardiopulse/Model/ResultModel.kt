package com.example.cardiopulse.Model

import java.util.Date

data class ResultModel(
    val min : Int,
    val avg : Int,
    val max : Int,
    val date : Date,
    val status : String,
    val type : String
)
