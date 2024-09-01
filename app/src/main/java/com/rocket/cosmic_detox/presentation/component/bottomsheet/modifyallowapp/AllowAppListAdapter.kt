package com.rocket.cosmic_detox.presentation.component.bottomsheet.modifyallowapp

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.databinding.ItemAppCheckboxListBinding
import com.rocket.cosmic_detox.data.model.App
import com.rocket.cosmic_detox.presentation.view.common.ViewHolder

class AllowAppListAdapter(
    private val context: Context,
    private val onClick: (AllowedApp) -> Unit
) : ListAdapter<AllowedApp, ViewHolder<AllowedApp>>(AppDiffCallback()) {

    private val checkedItems = mutableSetOf<AllowedApp>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<AllowedApp> {
        return AllowAppViewHolder.from(parent, onClick, context, checkedItems)
    }

    override fun onBindViewHolder(holder: ViewHolder<AllowedApp>, position: Int) {
        holder.onBind(currentList[position])
    }

    fun getCheckedItems(): List<AllowedApp> {
        return checkedItems.toList()
    }

    class AllowAppViewHolder(
        private val binding: ItemAppCheckboxListBinding,
        private val onClick: (AllowedApp) -> Unit,
        private val context: Context,
        private val checkedItems: MutableSet<AllowedApp>
    ) : ViewHolder<AllowedApp>(binding.root) {

        override fun onBind(item: AllowedApp) {
            with(binding) {
                // 체크박스 클릭 시 발생할 이벤트 처리
                //checkboxAllowApp.setOnCheckedChangeListener(null) // 리스너 해제

                // 체크박스 상태 초기화
                checkboxAllowApp.isChecked = checkedItems.contains(item) // TODO: 나중에 contains로 or isAllowed로 변경해야 하는지 확인

                // 아이템 뷰 클릭 처리
                itemView.setOnClickListener {
                    // 체크박스 상태 반전
                    checkboxAllowApp.isChecked = !checkboxAllowApp.isChecked
                }

                // 체크박스 클릭 처리
                checkboxAllowApp.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        checkedItems.add(item)
                    } else {
                        checkedItems.remove(item)
                    }
                    onClick(item)
                }

                // 나머지 바인딩 작업
                Glide.with(ivAllowAppIcon)
                    .load(context.packageManager.getApplicationIcon(item.packageId))
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(12)))
                    .into(ivAllowAppIcon)
                tvAllowAppName.text = item.appName
                tvAllowAppLimitedTime.text = item.limitedTime.toString()

                //checkboxAllowApp.isChecked = item.isAllowed // TODO: 나중에 contains로 변경해야 하는지 확인
            }
        }

        companion object {
            fun from(parent: ViewGroup, onClick: (AllowedApp) -> Unit, context: Context, checkedItems: MutableSet<AllowedApp>): AllowAppViewHolder {
                val binding = ItemAppCheckboxListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return AllowAppViewHolder(binding, onClick, context, checkedItems)
            }
        }
    }
}

class AppDiffCallback : DiffUtil.ItemCallback<AllowedApp>() {
    override fun areItemsTheSame(oldItem: AllowedApp, newItem: AllowedApp): Boolean {
        return oldItem.packageId == newItem.packageId
    }

    override fun areContentsTheSame(oldItem: AllowedApp, newItem: AllowedApp): Boolean {
        return oldItem == newItem
    }
}