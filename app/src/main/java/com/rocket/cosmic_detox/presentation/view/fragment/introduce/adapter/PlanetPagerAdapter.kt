package com.rocket.cosmic_detox.presentation.view.fragment.introduce.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rocket.cosmic_detox.data.model.Planet
import com.rocket.cosmic_detox.databinding.ItemPlanetBinding

class PlanetPagerAdapter : RecyclerView.Adapter<PlanetPagerAdapter.PlanetViewHolder>() {

    private val planets = mutableListOf<Planet>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanetViewHolder {
        val binding = ItemPlanetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlanetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlanetViewHolder, position: Int) {
        holder.bind(planets[position])
    }

    override fun getItemCount(): Int = planets.size

    fun submitList(planetList: List<Planet>) {
        planets.clear()
        planets.addAll(planetList)
        notifyDataSetChanged()
    }

    class PlanetViewHolder(private val binding: ItemPlanetBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(planet: Planet) {
            binding.tvPlanetName.text = planet.name
            binding.tvPlanetDescription.text = planet.description
            binding.tvPlanetIntroduction.text=planet.introduction
            binding.ivPlanetImage.setImageResource(planet.imageResId)

        }
    }
}
