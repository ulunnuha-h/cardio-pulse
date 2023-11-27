package com.example.cardiopulse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cardiopulse.Model.ResultModel
import java.text.SimpleDateFormat

class ResultAdapter(
    private var result: MutableList<ResultModel>,
    private val onDeleteResultListener: OnDeleteResultListener?
) : RecyclerView.Adapter<ResultAdapter.ResultViewHolder>() {

    interface OnDeleteResultListener {
        fun onDeleteResult(position: Int)
    }

    inner class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    var dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.result_item, parent, false)
        return ResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.itemView.apply {
            findViewById<TextView>(R.id.resultMin).text = result[position].min.toString()
            findViewById<TextView>(R.id.resultAvg).text = result[position].avg.toString()
            findViewById<TextView>(R.id.resultMax).text = result[position].max.toString()
            findViewById<TextView>(R.id.MeasureType).text = result[position].type
            findViewById<TextView>(R.id.HRStatus).text = result[position].status
            findViewById<TextView>(R.id.HRDate).text = dateFormat.format(result[position].date)

            findViewById<Button>(R.id.deleteButton).setOnClickListener {
                onDeleteButtonClick(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return result.size
    }

    private fun onDeleteButtonClick(position: Int) {
        onDeleteResultListener?.onDeleteResult(position)
    }

    fun updateResults(newResults: List<ResultModel>) {
        result.clear()
        result.addAll(newResults)
        notifyDataSetChanged()
    }
}