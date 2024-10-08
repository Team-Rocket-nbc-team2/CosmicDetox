package com.rocket.cosmic_detox.presentation.component.bottomsheet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.databinding.ModalBottomsheetIconBinding
import com.rocket.cosmic_detox.databinding.ModalContentSetUseTimeBinding
import com.rocket.cosmic_detox.presentation.extensions.fromSecondsToHours
import com.rocket.cosmic_detox.presentation.extensions.fromSecondsToMinutes
import com.rocket.cosmic_detox.presentation.extensions.toHours
import com.rocket.cosmic_detox.presentation.extensions.toMinutes
import com.rocket.cosmic_detox.presentation.view.fragment.mypage.MyPageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// todo :: 앱 정보 데이터를 받아와야 함. 허용된 앱의 이름을 id와 함께 불러와야 함.
//  앱 정보를 불러오면 title을 변경하기.
@AndroidEntryPoint
class MyPageSetLimitUseTimeBottomSheet : BottomSheetDialogFragment() {
    private val modalBottomSheetIconBinding by lazy { ModalBottomsheetIconBinding.inflate(layoutInflater) }
    private lateinit var modalContentSetUseTimeBinding: ModalContentSetUseTimeBinding
    private val myPageViewModel by activityViewModels<MyPageViewModel>()

    private val hourArray by lazy { resources.getStringArray(R.array.number_picker_hour) }
    private val minuteArray by lazy { resources.getStringArray(R.array.number_picker_minute) }

    private val args: MyPageSetLimitUseTimeBottomSheetArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        modalBottomSheetIconBinding.bottomSheetBody.layoutResource = R.layout.modal_content_set_use_time
        val viewStub = modalBottomSheetIconBinding.bottomSheetBody.inflate()
        modalContentSetUseTimeBinding = ModalContentSetUseTimeBinding.bind(viewStub)

        return modalBottomSheetIconBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        modalBottomSheetIconBinding.ivBottomSheetClose.setOnClickListener {
            dismiss()
        }

        modalBottomSheetIconBinding.tvBottomSheetTitle.text = "${args.allowedApp.appName} ${getString(R.string.set_limit_use_time_bottom_sheet_title)}"
        setNumberPicker()
        modalContentSetUseTimeBinding.btnSetUseTimeComplete.setOnClickListener {
            val selectedHourIndex = modalContentSetUseTimeBinding.numberPickerHour.value
            val selectedMinIndex = modalContentSetUseTimeBinding.numberPickerMinute.value

            val selectedHourValue = hourArray[selectedHourIndex]
            val selectedMinuteValue = minuteArray[selectedMinIndex]

            // ViewModel을 통해 시간 제한을 설정하고 Firestore에 저장
            myPageViewModel.setAppUsageLimit(args.allowedApp, selectedHourValue, selectedMinuteValue)

            // 성공 여부 확인 후 dismiss
            viewLifecycleOwner.lifecycleScope.launch {
                myPageViewModel.updateResult.collectLatest { success ->
                    if (success) {
                        dismiss()
                        myPageViewModel.resetUpdateResult() // 상태 초기화
                    } else {
                        //Toast.makeText(requireContext(), "업데이트 실패", Toast.LENGTH_SHORT).show()
                        Log.e("MyPageSetLimitUseTimeBottomSheet", "업데이트 실패") // 버튼 누르자마자는 실패, 성공 후 리셋 후 또 실패 -> 걍 else 문 없앨까
                    }
                }
            }
        }
    }

    private fun setNumberPicker() {
        val limitedTime = args.allowedApp.limitedTime
        val initialHour = limitedTime.toLong().fromSecondsToHours().toInt() // TODO: 나중에 타입은 어떻게 할지 생각해보기, 차피 초니까 Int로 해도 되지 않을까
        val remainingSeconds = limitedTime % 3600
        val initialMinute = remainingSeconds.toLong().fromSecondsToMinutes().toInt()

        Log.d("MyPageSetLimitUseTimeBottomSheet", "initialHour: $initialHour, initialMinute: $initialMinute")
        val initialMinuteIndex = minuteArray.indexOf(initialMinute.toString().padStart(2, '0')) // 0 -> 00으로 변환

        modalContentSetUseTimeBinding.numberPickerHour.apply {
            val hourList = hourArray.map { "${it}${getString(R.string.number_picker_unit_hour)}" }
                .toTypedArray()

            minValue = 0
            maxValue = hourList.lastIndex
            wrapSelectorWheel = true
            displayedValues = hourList
            value = initialHour
        }
        modalContentSetUseTimeBinding.numberPickerMinute.apply {
            val minList = minuteArray.map { "${it}${getString(R.string.number_picker_unit_minute)}" }
                .toTypedArray()

            minValue = 0
            maxValue = minList.lastIndex
            wrapSelectorWheel = true
            displayedValues = minList
            value = initialMinuteIndex
        }
    }
}