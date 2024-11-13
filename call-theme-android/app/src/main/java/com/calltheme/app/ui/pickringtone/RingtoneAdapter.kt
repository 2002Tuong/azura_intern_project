package com.calltheme.app.ui.pickringtone

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.screentheme.app.R
import com.screentheme.app.models.RingtoneModel
import com.screentheme.app.utils.helpers.RingtoneController

class RingtoneAdapter(private val ringtoneHelper: RingtoneController, private val ringtoneList: ArrayList<RingtoneModel> = ArrayList()) :
    RecyclerView.Adapter<RingtoneAdapter.RingtoneViewHolder>() {


    var mCallBack: ((ringtoneItem: RingtoneModel) -> Unit)? = null

    private var currentPlayingPosition = RecyclerView.NO_POSITION

    private var selectedItemPosition = RecyclerView.NO_POSITION


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RingtoneViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_layout_ringtone, parent, false)
        return RingtoneViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RingtoneViewHolder, position: Int) {
        val ringtoneItem = ringtoneList[position]
        holder.bind(ringtoneItem, position)

        val playPauseButton: ImageButton = holder.itemView.findViewById(R.id.playPauseButton)
        val selectionRadioButton: RadioButton = holder.itemView.findViewById(R.id.selectionRadioButton)
        val titleTextView: TextView = holder.itemView.findViewById(R.id.titleTextView)

        // Set click listener for play/pause button
        holder.itemView.setOnClickListener {

            selectRow(position)
        }

        playPauseButton.setOnClickListener {
            selectPlayButton(position)

            if (selectedItemPosition != position) {
                selectRow(position)
            }
        }

        if (selectedItemPosition == position) {
            holder.itemView.setBackgroundResource(R.drawable.select_ringtone_background)
            selectionRadioButton.isChecked = true
            playPauseButton.setImageResource(R.drawable.icon_play2)
        } else {
            holder.itemView.setBackgroundResource(R.drawable.unselect_ringtone_background)
            selectionRadioButton.isChecked = false
            playPauseButton.setImageResource(R.drawable.icon_play)
        }

        if (currentPlayingPosition == position) {
            playPauseButton.setImageResource(R.drawable.icon_pause)
        } else {
            playPauseButton.setImageResource(R.drawable.icon_play)
        }
    }

    private fun selectRow(position: Int) {
        // Update the selected item position
        val previousSelectedItem = selectedItemPosition

        if (selectedItemPosition == position) {
            // Clicked on the selected item, change background to white
            selectedItemPosition = RecyclerView.NO_POSITION
        } else {
            // Clicked on a different item, update selected item position
            selectedItemPosition = position

            mCallBack?.invoke(ringtoneList[position])
        }

        // Notify the adapter of the item changes
        notifyItemChanged(previousSelectedItem)
        notifyItemChanged(selectedItemPosition)
    }

    private fun selectPlayButton(position: Int) {

        val previousPlayingPosition = currentPlayingPosition

        if (currentPlayingPosition == position) {
            // Clicked on the selected item, change background to white
            currentPlayingPosition = RecyclerView.NO_POSITION
            pauseRingtone()
        } else {
            // Clicked on a different item, update selected item position
            currentPlayingPosition = position
            playRingtone(position)
        }

        notifyItemChanged(previousPlayingPosition)
        notifyItemChanged(currentPlayingPosition)
    }

    override fun getItemCount(): Int {
        return ringtoneList.size
    }

    fun onItemClicked(callback: (ringtoneItem: RingtoneModel) -> Unit) {
        this.mCallBack = callback
    }

    fun updateItems(newAvatars: ArrayList<RingtoneModel>) {
        ringtoneList.clear()
        ringtoneList.addAll(newAvatars)
        notifyDataSetChanged()
    }

    inner class RingtoneViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val context = itemView.context

        private val playPauseButton: ImageButton = itemView.findViewById(R.id.playPauseButton)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)

        fun bind(ringtoneItem: RingtoneModel, position: Int) {
            titleTextView.text = ringtoneItem.title


        }


    }

    private fun playRingtone(position: Int) {
        // Pause the previously playing ringtone
        pauseRingtone()

//        currentPlayingPosition = position
        ringtoneHelper.playRingtone(ringtoneList[position].uri)
    }

    private fun pauseRingtone() {
        ringtoneHelper.stopRingtone()
    }

}
