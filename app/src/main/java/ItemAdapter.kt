package com.example.webrequestchecker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemAdapter(
    private val itemList: List<Item>,
    private val onEdit: (Int) -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item, position, onEdit, onDelete)
    }

    override fun getItemCount(): Int = itemList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val inputMintText: TextView = itemView.findViewById(R.id.inputMintText)
        private val outputMintText: TextView = itemView.findViewById(R.id.outputMintText)
        private val inAmountText: TextView = itemView.findViewById(R.id.inAmountText)
        private val alarmText: TextView = itemView.findViewById(R.id.alarmText)
        private val editButton: Button = itemView.findViewById(R.id.editButton)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        fun bind(item: Item, position: Int, onEdit: (Int) -> Unit, onDelete: (Int) -> Unit) {
            inputMintText.text = "Input Mint: ${item.inputMint}"
            outputMintText.text = "Output Mint: ${item.outputMint}"
            inAmountText.text = "In Amount: ${item.inAmount}"
            alarmText.text = if (item.alarmTime != null) "Alarm: ${item.alarmTime}" else ""

            editButton.setOnClickListener { onEdit(position) }
            deleteButton.setOnClickListener { onDelete(position) }
        }
    }
}