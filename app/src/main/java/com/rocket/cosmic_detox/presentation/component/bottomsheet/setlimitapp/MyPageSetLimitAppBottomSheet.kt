package com.rocket.cosmic_detox.presentation.component.bottomsheet.setlimitapp

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.databinding.ModalBottomsheetBinding
import com.rocket.cosmic_detox.databinding.ModalContentSetLimitAppBinding
import com.rocket.cosmic_detox.presentation.uistate.MyPageUiState
import com.rocket.cosmic_detox.presentation.view.fragment.mypage.MyPageViewModel
import com.rocket.cosmic_detox.presentation.view.fragment.timer.BottomSheetState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyPageSetLimitAppBottomSheet: BottomSheetDialogFragment() {
    private val modalBottomSheetBinding by lazy { ModalBottomsheetBinding.inflate(layoutInflater) }
    private lateinit var modalContentSetLimitAppBinding: ModalContentSetLimitAppBinding
    private val limitedAppAdapter by lazy {
        LimitedAppAdapter(requireContext()) {
            navigateToSetLimitUseTimeBottomSheet(it)
        }
    }
    private val args: MyPageSetLimitAppBottomSheetArgs by navArgs()
    private val myPageViewModel by activityViewModels<MyPageViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        modalBottomSheetBinding.bottomSheetBody.layoutResource = R.layout.modal_content_set_limit_app
        val viewStub = modalBottomSheetBinding.bottomSheetBody.inflate()
        modalContentSetLimitAppBinding = ModalContentSetLimitAppBinding.bind(viewStub)

        // todo :: number picker bottom sheet 여는 코드 작성
        //  recyclerView item 클릭 시 bottom Sheet를 열면 됨.
        //  아래 코드 복붙.
        // val bottomSheet = MyPageSetLimitUseTimeBottomSheet()
        // bottomSheet.isCancelable = false
        // bottomSheet.show(parentFragmentManager, bottomSheet.tag)
        return modalBottomSheetBinding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =  super.onCreateDialog(savedInstanceState)

        dialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) { // 뒤로 가기 버튼(KEYCODE_BACK)이 눌린 순간(ACTION_DOWN), ACTION_UP은 떼어졌을 때 둘다 작동은 하는 듯?
                myPageViewModel.loadMyInfo()
                dismiss()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            setUpRatio(bottomSheetDialog)
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()

        modalBottomSheetBinding.tvBottomSheetTitle.text = getString(R.string.limit_app_bottom_sheet_title)
        modalBottomSheetBinding.tvBottomSheetComplete.setOnClickListener {
            dismiss()
            myPageViewModel.loadMyInfo()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            myPageViewModel.myInfo.collectLatest { uiState ->
                when (uiState) {
                    is MyPageUiState.Success -> {
                        limitedAppAdapter.submitList(uiState.data.apps)
                    }
                    else -> {
                        Log.e("MyPageSetLimitAppBottomSheet", "업데이트 실패")
                    }
                }
            }
        }
    }

    private fun initView() = with(modalContentSetLimitAppBinding) {
        rvSetLimitAppList.apply {
            adapter = limitedAppAdapter
            itemAnimator = null
            isVisible = args.allowedApps.isNotEmpty()
        }
        Log.d("jade", "initView: ${args.allowedApps.toList()}")
        tvLimitAppIsEmpty.isVisible = args.allowedApps.isEmpty()
    }

    private fun navigateToSetLimitUseTimeBottomSheet(allowedApp: AllowedApp) {
        val action = MyPageSetLimitAppBottomSheetDirections.actionSetLimitAppToSetLimitUseTime(allowedApp)
        findNavController().navigate(action)
    }

    private fun setUpRatio(bottomSheetDialog: BottomSheetDialog) {
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as View
        val behavior = BottomSheetBehavior.from(bottomSheet)
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = getWindowHeight()

        bottomSheet.layoutParams = layoutParams
        behavior.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            isDraggable = false
        }
    }

    private fun getWindowHeight(): Int {
        val wm = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = wm.currentWindowMetrics
            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            return windowMetrics.bounds.height() - insets.bottom - insets.top
        } else {
            val displayMetrics = DisplayMetrics()
            wm.defaultDisplay.getMetrics(displayMetrics)
            return displayMetrics.heightPixels
        }
    }
}