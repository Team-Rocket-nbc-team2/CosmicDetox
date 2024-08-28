package com.rocket.cosmic_detox.presentation.component.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.ModalBottomsheetIconBinding
import com.rocket.cosmic_detox.databinding.ModalContentSetUseTimeBinding

// todo :: 앱 정보 데이터를 받아와야 함. 허용된 앱의 이름을 id와 함께 불러와야 함.
//  앱 정보를 불러오면 title을 변경하기.
class MyPageSetLimitUseTimeBottomSheet(): BottomSheetDialogFragment() {
    private val modalBottomSheetIconBinding by lazy { ModalBottomsheetIconBinding.inflate(layoutInflater) }
    private lateinit var modalContentSetUseTimeBinding: ModalContentSetUseTimeBinding

    private val hourArray by lazy { resources.getStringArray(R.array.number_picker_hour) }
    private val minuteArray by lazy { resources.getStringArray(R.array.number_picker_minute) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        modalBottomSheetIconBinding.bottomSheetBody.layoutResource = R.layout.modal_content_set_use_time
        val viewStub = modalBottomSheetIconBinding.bottomSheetBody.inflate()
        modalContentSetUseTimeBinding = ModalContentSetUseTimeBinding.bind(viewStub)

        modalBottomSheetIconBinding.ivBottomSheetClose.setOnClickListener {
            dismiss()
        }

        modalBottomSheetIconBinding.tvBottomSheetTitle.text = "app name ${getString(R.string.set_limit_use_time_bottom_sheet_title)}"
        setNumberPicker()
        modalContentSetUseTimeBinding.btnSetUseTimeComplete.setOnClickListener {
            val selectedHourIndex = modalContentSetUseTimeBinding.numberPickerHour.value
            val selectedMinIndex = modalContentSetUseTimeBinding.numberPickerMinute.value

            // numberPicker 에서 선택된 시간/분
            val selectedHourValue = hourArray[selectedHourIndex]
            val selectedMinuteValue = minuteArray[selectedMinIndex]

            // todo :: firebase 데이터 변경.

            dismiss()
        }

        return modalBottomSheetIconBinding.root
    }

    private fun setNumberPicker() {
        modalContentSetUseTimeBinding.numberPickerHour.apply {
            val hourList = hourArray.map { "${it}${getString(R.string.number_picker_unit_hour)}" }
                .toTypedArray()

            minValue = 0
            maxValue = hourList.lastIndex
            wrapSelectorWheel = true
            displayedValues = hourList
        }
        modalContentSetUseTimeBinding.numberPickerMinute.apply {
            val minList = minuteArray.map { "${it}${getString(R.string.number_picker_unit_minute)}" }
                .toTypedArray()

            minValue = 0
            maxValue = minList.lastIndex
            wrapSelectorWheel = true
            displayedValues = minList
        }
    }
}