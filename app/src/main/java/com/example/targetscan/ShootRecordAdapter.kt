package com.example.targetscan


import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class ShootRecordAdapter(val shootList:List<ShootRecord>) : RecyclerView.Adapter<ShootRecordAdapter.ViewHolder>() {
    inner class ViewHolder(view:View):RecyclerView.ViewHolder(view){
        val shootImage: ImageView = view.findViewById(R.id.shootingImage)
        val shootName: TextView = view.findViewById(R.id.shootingName)
        val shootProcessLabel : TextView = view.findViewById(R.id.processLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.shooting_onerecord,parent,false)

        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener{
            val position = viewHolder.adapterPosition
            val shootRecord = shootList[position]
            Toast.makeText(parent.context, "You click num ${position}", Toast.LENGTH_SHORT).show()
            val intent = Intent(parent.context,RecordDetail::class.java)
            intent.putExtra("position",position)
            intent.putExtra("name",shootRecord.name)
            parent.context.startActivity(intent)

        }
        viewHolder.itemView.setOnLongClickListener {
            val position = viewHolder.adapterPosition
            val shootRecord = shootList[position]
            Toast.makeText(parent.context, "You long click num ${position}", Toast.LENGTH_SHORT).show()
            true
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val shoot = shootList[position]
        holder.shootName.text=shoot.name
        holder.shootImage.setImageResource(shoot.imageId)
        holder.shootProcessLabel.text = shoot.processLabel
    }
    override fun getItemCount(): Int {
        return shootList.size
    }
}