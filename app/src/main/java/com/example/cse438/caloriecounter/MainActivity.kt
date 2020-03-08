package com.example.cse438.caloriecounter

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.IntegerRes
import com.example.cse438.caloriecounter.FoodListAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.enter_calories.*
import kotlinx.android.synthetic.main.enter_calories.view.*
import kotlinx.android.synthetic.main.enter_foods.*
import kotlinx.android.synthetic.main.food_row_layout.view.*
import java.lang.Double.parseDouble
import kotlinx.android.synthetic.main.enter_foods.*
import kotlinx.android.synthetic.main.enter_foods.view.*
import kotlinx.android.synthetic.main.suggested_intake.*
import kotlinx.android.synthetic.main.suggested_intake.view.*
import kotlinx.android.synthetic.main.bmr_and_calories.*
import kotlinx.android.synthetic.main.bmr_and_calories.view.*
import java.io.PrintStream
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.round

class MainActivity : AppCompatActivity() {

    // List of foods the user enters
    private var ourList = ArrayList<Food>()

    // Text and List views
    private var listView: ListView? = null

    private var remainingCals: Double = 0.0
    private var calsConsumed: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getFoods()
        allowance()
    }

    private fun getFoods(){
        listView = foods

        // Setting up the adapter using our custom built adapter
        val adapter = FoodListAdapter(this, ourList)
        listView?.adapter = adapter

        adapter.notifyDataSetChanged()
    }

    private fun allowance(){
        // Opens the dialog view asking the user for the caloric
        val dialogView = LayoutInflater.from(this).inflate(R.layout.enter_calories, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Enter caloric allowance or find suggested intake")
        val mAlertDialog = mBuilder.show()

        // Sets an onclick listener on the dialog box button
        mAlertDialog.submitCalories.setOnClickListener {
            val dailyAllowance = dialogView.allowance.text.toString()
            // If the string is empty or is not a number, we do not want to accept that as an input
            if(dailyAllowance != "" && is_positive(dailyAllowance)){
                remaining.text = dailyAllowance
                remainingCals = parseDouble(dailyAllowance)
                mAlertDialog.dismiss()
            } else{
                val myToast = Toast.makeText(this, "Please enter a valid value", Toast.LENGTH_SHORT)
                myToast.show()
            }
        }

        //If user wants to see their suggested caloric intake they are taken to this.
        /*
        Calculations for basal metabolic rate and suggested caloric intake were taken from:
        http://www.checkyourhealth.org/eat-healthy/cal_calculator.php
        After calculating basal metaboic rate, for simplicity calories are just bmr * 1.4625 which
        is the midpoint between lightly active and moderately active.
         */
        mAlertDialog.findCalories.setOnClickListener{
            mAlertDialog.dismiss()
            val suggestedView = LayoutInflater.from(this).inflate(R.layout.suggested_intake, null)
            val suggestedBuilder = AlertDialog.Builder(this).setView(suggestedView)
            val sugAlertDialog = suggestedBuilder.show()
            val myToast = Toast.makeText(this, "Please enter valid values", Toast.LENGTH_SHORT)
            /*
            Construction of alert dialogue for displaying the bmr and suggested caloric intake
             */
            val bmrCals = LayoutInflater.from(this).inflate(R.layout.bmr_and_calories, null)
            val bmrCalsBuilder = AlertDialog.Builder(this).setView(bmrCals)
            //If male
            sugAlertDialog.male.setOnClickListener{
                var bmr = find_bmr(suggestedView.YourHeight.text.toString(), suggestedView.YourWeight.text.toString(), suggestedView.YourAge.text.toString(), true)
                deal_with_bmr(bmr, sugAlertDialog)
            }
            //If female
            sugAlertDialog.female.setOnClickListener{
                var bmr = find_bmr(suggestedView.YourHeight.text.toString(), suggestedView.YourWeight.text.toString(), suggestedView.YourAge.text.toString(), false)
                deal_with_bmr(bmr, sugAlertDialog)
            }
        }
    }

    private fun deal_with_bmr(bmr: Double, sugAlertDialog : Dialog){
        val bmrCals = LayoutInflater.from(this).inflate(R.layout.bmr_and_calories, null)
        val bmrCalsBuilder = AlertDialog.Builder(this).setView(bmrCals)
        if(bmr > 0){
            var cals = bmr*1.4625
            sugAlertDialog.dismiss()
            val dialogBMR = bmrCalsBuilder.show()
            bmrCals.InfoBMR.text = "Your basal metabolic rate is: " + round(bmr) + " which yields a suggested caloric intake of " + round(cals) + " calories"
            bmrCals.UseBMRCals.setOnClickListener{
                remaining.text = cals.toString()
                remainingCals=cals
                dialogBMR.dismiss()
            }
            bmrCals.caloriesAfterBMR.setOnClickListener{
                val newCalories = bmrCals.postBMRAllowance.text.toString()
                if(is_positive(newCalories)){
                    remaining.text = newCalories
                    remainingCals= parseDouble(newCalories)
                    dialogBMR.dismiss()
                } else{
                    Toast.makeText(this, "Please enter valid values", Toast.LENGTH_SHORT).show()
                }
            }
        } else{
            Toast.makeText(this, "Please enter valid values", Toast.LENGTH_SHORT).show()
        }
    }

    private fun find_bmr(height : String, weight: String, age: String, male : Boolean) : Double{
        val toast = Toast.makeText(this, "Please enter valid values", Toast.LENGTH_SHORT)
        if(is_numeric(height) && is_numeric(weight) && is_numeric(age)){
            val inches = parseDouble(height)
            val pounds = parseDouble(weight)
            val years = parseDouble(age)
            if(inches > 3 && pounds > 0.5 && pounds < 3000 && inches <120 && years >= 0 && years < 200){
                if(male){
                    return 66.0 + (6.3 * pounds) + (12.9 * inches) - (6.8 * years)
                } else{
                    return  655.0 + (4.3 * pounds) + (4.7 * inches) - (4.7 * years)
                }
            } else{
                toast.show()
                return -1.0;
            }
        } else{
            toast.show()
            return -1.0;
        }
    }

    //Function to see if a string is numeric
    private fun is_numeric(input : String): Boolean {
        var numeric = true
        try {
            parseDouble(input)
        } catch (e: NumberFormatException) {
            numeric = false
        }
        return numeric
    }

    private fun is_positive(input : String): Boolean{
        if(!is_numeric(input)){
            return false
        } else {
            return parseDouble(input) >= 0
        }
    }


    /**
     * Handler for adding a new food
     */
    fun addFood(view: View?){
        val dialogView = LayoutInflater.from(this).inflate(R.layout.enter_foods, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Enter food")
        val mAlertDialog = mBuilder.show()

        mAlertDialog.button2.setOnClickListener{
            val foodToAdd = dialogView.newFood.text.toString()
            val caloriesToAdd = dialogView.newCalories.text.toString()
            if(foodToAdd != "" && is_positive(caloriesToAdd)){
                var aFood = Food(foodToAdd, caloriesToAdd)
                ourList.add(aFood)
                remainingCals -= parseDouble(caloriesToAdd)
                calsConsumed += parseDouble(caloriesToAdd)
                if(remainingCals < 0){
                    remaining.text = remainingCals.toString()
                    remaining.setTextColor(Color.rgb(200,0,0))
                } else {
                    remaining.text = remainingCals.toString()
                }
                consumed.text = calsConsumed.toString()
                (listView?.adapter as? FoodListAdapter)?.notifyDataSetChanged()
                mAlertDialog.dismiss()
            }
            else{
                val myToast = Toast.makeText(this, "Please enter valid values", Toast.LENGTH_SHORT)
                myToast.show()
            }
        }
    }
}
