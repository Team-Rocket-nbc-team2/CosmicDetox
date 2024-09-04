package com.rocket.cosmic_detox.presentation.component.bottomsheet.modifyallowedapp

import android.content.Context
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.data.model.CheckedApp
import com.rocket.cosmic_detox.databinding.ItemAppCheckboxListBinding
import com.rocket.cosmic_detox.presentation.extensions.loadAppIcon
import com.rocket.cosmic_detox.presentation.view.common.ViewHolder

class AllowAppListAdapter(
    private val context: Context,
    private val onClick: (CheckedApp) -> Unit
) : ListAdapter<CheckedApp, AllowAppListAdapter.AllowAppViewHolder>(CheckedAppDiffCallback()) {

    // SparseBooleanArray를 사용하여 체크 상태 관리
    private val checkedStates = SparseBooleanArray()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllowAppViewHolder {
        return AllowAppViewHolder.from(parent, onClick, context)
    }

    override fun onBindViewHolder(holder: AllowAppViewHolder, position: Int) {
        holder.onBind(currentList[position], position, checkedStates)
    }

    class AllowAppViewHolder(
        private val binding: ItemAppCheckboxListBinding,
        private val onClick: (CheckedApp) -> Unit,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: CheckedApp, position: Int, checkedStates: SparseBooleanArray) {
            with(binding) {
                // 체크 상태를 SparseBooleanArray에서 복원
                checkboxAllowApp.isChecked = checkedStates.get(position, item.isChecked)

                // 체크박스 클릭 시 상태를 SparseBooleanArray에 저장
//                checkboxAllowApp.setOnCheckedChangeListener { _, isChecked ->
//                    checkedStates.put(position, isChecked)
//                    //onClick(item)
//                }
                checkboxAllowApp.setOnClickListener {
                    val isChecked = !checkboxAllowApp.isChecked
                    checkedStates.put(position, isChecked)
                    onClick(item)
                }

                itemView.setOnClickListener {
                    val isChecked = !checkboxAllowApp.isChecked
                    checkboxAllowApp.isChecked = isChecked
                    checkedStates.put(position, isChecked)
                    onClick(item)
                }

                ivAllowAppIcon.loadAppIcon(context, item.packageId)
                tvAllowAppName.text = item.appName
                tvAllowAppLimitedTime.text = item.limitedTime.toString()
            }
        }

        companion object {
            fun from(parent: ViewGroup, onClick: (CheckedApp) -> Unit, context: Context): AllowAppViewHolder {
                val binding = ItemAppCheckboxListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return AllowAppViewHolder(binding, onClick, context)
            }
        }
    }
}

class CheckedAppDiffCallback : DiffUtil.ItemCallback<CheckedApp>() {
    override fun areItemsTheSame(oldItem: CheckedApp, newItem: CheckedApp): Boolean {
        return oldItem.packageId == newItem.packageId
    }

    override fun areContentsTheSame(oldItem: CheckedApp, newItem: CheckedApp): Boolean {
        return oldItem == newItem
    }
}