package com.example.targetscan


import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class DateRecordAdapter(private val dateList:List<DateRecord>) : RecyclerView.Adapter<DateRecordAdapter.ViewHolder>() {
    inner class ViewHolder(view:View):RecyclerView.ViewHolder(view){
        val dateImage: ImageView = view.findViewById(R.id.shootingImage)
        val dateName: TextView = view.findViewById(R.id.shootingName)
        val dateNumber : TextView = view.findViewById(R.id.processLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.shooting_onerecord,parent,false)

        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener{
            val position = viewHolder.adapterPosition
            val shootRecord = dateList[position]
//            Toast.makeText(parent.context, "You click num ${position}", Toast.LENGTH_SHORT).show()
            val intent = Intent(parent.context,DaySelect::class.java)
            intent.putExtra("date",dateList[position].name)
            parent.context.startActivity(intent)

        }
//        viewHolder.itemView.setOnLongClickListener {
//            val position = viewHolder.adapterPosition
//            val shootRecord = dateList[position]
//            Toast.makeText(parent.context, "You long click num ${position}", Toast.LENGTH_SHORT).show()
//            true
//        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val shoot = dateList[position]
        holder.dateName.text=shoot.name
        holder.dateImage.setImageResource(shoot.imageId)
        holder.dateNumber.text = shoot.number.toString()
    }
    override fun getItemCount(): Int {
        return dateList.size
    }
}