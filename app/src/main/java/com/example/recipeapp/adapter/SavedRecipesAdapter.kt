package com.example.recipeapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.recipeapp.DetailActivity
import com.example.recipeapp.R
import com.example.recipeapp.model.SavedRecipe
import kotlinx.android.synthetic.main.rv_recipe.view.*
import kotlinx.android.synthetic.main.rv_sub_category.view.ivDish
import kotlinx.android.synthetic.main.rv_sub_category.view.tvDishName

class SavedRecipesAdapter: RecyclerView.Adapter<SavedRecipesAdapter.MyViewHolder>() {

    private var recipeList = emptyList<SavedRecipe>()

    class  MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_recipe, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var currentItem = recipeList[position]
        holder.itemView.tvDishName.text = currentItem.title
        Glide.with(holder.itemView.context).load(currentItem.image).centerCrop().into(holder.itemView.ivDish)
        holder.itemView.tvTime.text = currentItem.totalTime.toString().plus("'")
        holder.itemView.tvServings.text = currentItem.servings.toString()
        holder.itemView.tvCalories.text = currentItem.nutrition?.nutrients?.get(0)!!.amount.toString().plus(" kcal")

        holder.itemView.rowLayout.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailActivity::class.java)
            intent.putExtra("id", currentItem.id)
            intent.putExtra("from", "saved") //znak da se u detail activity ide iz sacuvanih recepata
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }

    fun setData(recipe: List<SavedRecipe>) {
        this.recipeList = recipe
        notifyDataSetChanged()
    }

}