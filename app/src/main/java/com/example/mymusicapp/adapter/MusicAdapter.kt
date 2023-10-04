package com.example.mymusicapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.example.mymusicapp.databinding.ItemMusicBinding
import com.example.mymusicapp.db.entity.MusicEntity

class MusicAdapter(var list: ArrayList<MusicEntity>, var onItemClick: (MusicEntity, Int) -> Unit) :
    RecyclerView.Adapter<MusicAdapter.VH>() {

    inner class VH(var binding: ItemMusicBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(musicEntity: MusicEntity, position: Int) {
            binding.nameArtist.text = musicEntity.aArtist
            binding.nameMusic.text = musicEntity.aName

            itemView.setOnClickListener {
                onItemClick(musicEntity, position)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(list[position], position)
    }
}