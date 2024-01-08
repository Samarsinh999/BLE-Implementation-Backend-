package com.example.blenoui

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class ScanResultAdapter(private val scanResults: List<ScanResult>) :
    RecyclerView.Adapter<ScanResultAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_scan_result_adapter, parent, false)
        return ViewHolder(view)
    }

    interface OnItemClickListener {
        fun onItemClick(scanResult: ScanResult)
    }

    // Create a variable to hold the listener
    private var listener: OnItemClickListener? = null

    // Setter method to set the listener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = scanResults[position]

        // Set the device name and MAC address
        holder.textViewDeviceName.text = result.device.name
        holder.textViewMacAddress.text = result.device.address
        holder.textViewSignalStrength.text = result.rssi.toString()
        holder.itemView.setOnClickListener {
            listener?.onItemClick(result)
        }
    }

    override fun getItemCount(): Int {
        return scanResults.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewDeviceName: TextView
        var textViewMacAddress: TextView
        var textViewSignalStrength : TextView

        init {
            textViewDeviceName = itemView.findViewById(R.id.textViewDeviceName)
            textViewMacAddress = itemView.findViewById(R.id.textViewMacAddress)
            textViewSignalStrength = itemView.findViewById(R.id.textViewSignalStrength)
        }
    }
}
