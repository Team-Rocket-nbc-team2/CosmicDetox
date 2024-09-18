package com.rocket.cosmic_detox.presentation.component.bottomsheet.modifyallowedapp

import android.content.Context
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rocket.cosmic_detox.data.model.CheckedApp
import com.rocket.cosmic_detox.databinding.ItemAppCheckboxListBinding
import com.rocket.cosmic_detox.presentation.extensions.loadAllowedAppIcon
import com.rocket.cosmic_detox.presentation.extensions.loadInstalledAppIcon

class AllowAppListAdapter(
    private val context: Context,
    private val onClick: (CheckedApp) -> Unit,
    private val onCheckboxClick: (CheckedApp) -> Unit
) : ListAdapter<CheckedApp, AllowAppListAdapter.AllowAppViewHolder>(CheckedAppDiffCallback()) {

    // SparseBooleanArray를 사용하여 체크 상태 관리
    private val checkedStates = SparseBooleanArray()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllowAppViewHolder {
        return AllowAppViewHolder.from(parent, onClick, onCheckboxClick, context)
    }

    override fun onBindViewHolder(holder: AllowAppViewHolder, position: Int) {
        holder.onBind(currentList[position], position, checkedStates)
    }

    class AllowAppViewHolder(
        private val binding: ItemAppCheckboxListBinding,
        private val onClick: (CheckedApp) -> Unit,
        private val onCheckboxClick: (CheckedApp) -> Unit, // 체크박스 클릭 이벤트
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: CheckedApp, position: Int, checkedStates: SparseBooleanArray) {
            with(binding) {
                // 체크 상태를 SparseBooleanArray에서 복원
                checkboxAllowApp.isChecked = checkedStates.get(position, item.isChecked)

                // 체크박스 클릭 이벤트를 따로 처리
                checkboxAllowApp.setOnClickListener {
                    val isChecked = !checkedStates.get(position, item.isChecked)
                    checkboxAllowApp.isChecked = isChecked
                    checkedStates.put(position, isChecked)
                    onCheckboxClick(item) // 체크박스 클릭 이벤트 처리
                }

                // 아이템 뷰 클릭 이벤트 처리
                itemView.setOnClickListener {
                    onClick(item) // 아이템 클릭 시 처리
                }

                itemView.setOnClickListener {
                    val isChecked = !checkboxAllowApp.isChecked
                    checkboxAllowApp.isChecked = isChecked
                    checkedStates.put(position, isChecked)
                    onClick(item)
                }
                ivAllowAppIcon.loadInstalledAppIcon(item.appIcon)
                tvAllowAppName.text = item.appName
                tvAllowAppLimitedTime.text = item.limitedTime.toString()
            }
        }

        companion object {
            fun from(parent: ViewGroup, onClick: (CheckedApp) -> Unit, onCheckboxClick: (CheckedApp) -> Unit, context: Context): AllowAppViewHolder {
                val binding = ItemAppCheckboxListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return AllowAppViewHolder(binding, onClick, onCheckboxClick, context)
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