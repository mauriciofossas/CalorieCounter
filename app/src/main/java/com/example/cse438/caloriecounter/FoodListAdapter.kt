package com.example.cse438.caloriecounter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView


class FoodListAdapter(private var activity: Activity, private var foods: ArrayList<Food>): BaseAdapter() {

    /**
     * Need to override the ViewHolder method
     */
    private class ViewHolder(row: View?){
        var foodName: TextView? = null
        var foodCalories: TextView? = null

        init {
            this.foodName = row?.findViewById(R.id.foodName) //R is short for resource.
            this.foodCalories = row?.findViewById(R.id.foodCalories)
        }
    }

    /**
     * Displays the information in the row format we want
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View?
        val viewHolder: ViewHolder
        if (convertView == null) {
            val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.food_row_layout, null)
            viewHolder = ViewHolder(view)
            view?.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        viewHolder.foodName?.text = foods[position].NameOfFood
        viewHolder.foodCalories?.text = foods[position].Calories

        return view as View
    }

    override fun getItem(position: Int): Any {
        return foods[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return foods.size
    }

}

