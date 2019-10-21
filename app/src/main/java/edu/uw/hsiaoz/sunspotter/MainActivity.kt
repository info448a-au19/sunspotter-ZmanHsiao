package edu.uw.hsiaoz.sunspotter

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.adapter_layout.view.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val queue = Volley.newRequestQueue(this)
        // gets the url from edit text and makes query
        button.setOnClickListener{
            var zipcode = input.text
            var api = getString(R.string.OPEN_WEATHER_MAP_API_KEY)
            var url = "https://api.openweathermap.org/data/2.5/forecast?units=imperial&zip=$zipcode&APPID=$api"
            val jsonRequest = handleJson(url)
            queue.add(jsonRequest)
            middle.visibility = View.VISIBLE
        }
    }


    fun handleJson(url: String): JsonObjectRequest {
        var jsonRequest = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener<JSONObject> { response ->
                    var list = response.getJSONArray("list")
                    var arraylist = ArrayList<ForecastData>()
                    var ifsun = false
                    for (i in 0 until list.length()) {
                        var array = list.getJSONObject(i)
                        var dt = array.getString("dt")
                        var weather = array.getJSONArray("weather")
                        var main = weather.getJSONObject(0).getString("main")
                        var icon = weather.getJSONObject(0).getString("icon")
                        var temp = array.getJSONObject("main").getString("temp")
                        var forecast = ForecastData(main, dt, temp, icon)
                        arraylist.add(forecast)
                        if (!ifsun && main == "Clear") {
                            var sdf = SimpleDateFormat("EEE h:mm a")
                            val netdate = Date(dt.toLong() * 1000L)
                            var time = sdf.format(netdate)
                            displaySun.text = "There will be sun!"
                            whenSun.text = "At $time"
                            check.setImageResource(R.drawable.ic_check_circle_black_24dp)
                            ifsun = true
                        }
                    }
                    if (!ifsun) {
                        displaySun.text = "There will no be sun!"
                        whenSun.text = ""
                        check.setImageResource(R.drawable.ic_highlight_off_black_24dp)
                    }
                    var listView = displayWeather
                    var adapter = WeatherAdapter(this, arraylist)
                    listView.adapter = adapter
                    adapter.notifyDataSetChanged()
                },
                Response.ErrorListener {})
        return jsonRequest
    }
}

class ForecastData (var weather: String, var hour: String, var temp: String, var icon: String)

class WeatherAdapter (context: Context, list: ArrayList<ForecastData>): ArrayAdapter<ForecastData>(context, R.layout.adapter_layout, list) {

    var mcontext: Context = context
    var mlist = list

    // gets the layout views
    private class ViewHolder(row: View) {
        var weatherText = row.hourWeather
        var weatherIcon = row.hourIcon
    }

    // sets the content to the views
    override fun getView(i: Int, convertView: View?, viewGroup: ViewGroup): View {
        var view: View
        var viewHolder: ViewHolder
        if (convertView == null) {
            val inflater = mcontext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.adapter_layout, null)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }
        var iconString = "icon" + mlist[i].icon
        var drawableId: Int = mcontext.getResources().getIdentifier(iconString, "drawable", mcontext.getPackageName())
        viewHolder.weatherIcon.setImageResource(drawableId)
        var sdf = SimpleDateFormat("EEE h:mm a")
        val netdate = Date(mlist[i].hour.toLong() * 1000L)
        var time = sdf.format(netdate)
        var displaytext = mlist[i].weather + " @ $time" + " (" + mlist[i].temp + "F)"
        viewHolder.weatherText.text = displaytext

        return view
    }

}