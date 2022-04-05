package com.example.recipeapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.recipeapp.R
import com.example.recipeapp.model.Type
import kotlinx.android.synthetic.main.rv_sub_category.view.ivDish
import kotlinx.android.synthetic.main.rv_sub_category.view.tvDishName

class TypeAdapter: RecyclerView.Adapter<TypeAdapter.MyViewHolder>() {

    var listener: OnItemClickListener? = null
    private var types = emptyList<Type>()

    class  MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    fun setClickListener(listener1: OnItemClickListener){
        listener = listener1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_main_category, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var currentItem = types[position]
        holder.itemView.tvDishName.text = currentItem.name
        Glide.with(holder.itemView.context).load(currentItem.image).centerCrop().into(holder.itemView.ivDish)

        holder.itemView.rootView.setOnClickListener {
            listener!!.onClicked(currentItem.name)
        }
    }

    override fun getItemCount(): Int {
        return types.size
    }

    fun setData(types: List<Type>) {
        this.types = types
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onClicked(categoryName: String)
    }

}