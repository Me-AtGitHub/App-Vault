package com.example.calculater.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.calculater.databinding.ItemCalculationHistoryBinding
import com.example.calculater.utils.CalculationHistory

class CalculationHistoryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val itemList = mutableListOf<CalculationHistory>()

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return HistoryViewHolder(
            ItemCalculationHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as HistoryViewHolder).binding
            .apply {
                itemList[position].let {
                    tvExpression.text = it.expression
                    tvResult.text = it.result
                }
            }
    }


    fun setList(items: List<CalculationHistory>) {
        itemList.clear()
        itemList.addAll(items)
        notifyItemRangeInserted(0, items.count())
    }

    fun addItem(item: CalculationHistory) {
        val count = itemList.count()
        itemList.add(item)
        notifyItemInserted(count)
    }

    inner class HistoryViewHolder(val binding: ItemCalculationHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {

        }
    }
}