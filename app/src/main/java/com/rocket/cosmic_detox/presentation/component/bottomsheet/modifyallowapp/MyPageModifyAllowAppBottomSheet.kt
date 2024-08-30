package com.rocket.cosmic_detox.presentation.component.bottomsheet.modifyallowapp

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.UiState
import com.rocket.cosmic_detox.databinding.ModalBottomsheetBinding
import com.rocket.cosmic_detox.databinding.ModalContentModifyAllowAppBinding
import com.rocket.cosmic_detox.presentation.model.AppManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyPageModifyAllowAppBottomSheet: BottomSheetDialogFragment() {
    private val modalBottomSheetBinding by lazy { ModalBottomsheetBinding.inflate(layoutInflater) }
    private lateinit var modalContentModifyAllowAppBinding: ModalContentModifyAllowAppBinding
    private val allowAppListAdapter by lazy {
        AllowAppListAdapter(requireContext()) {
            Toast.makeText(context, it.appName, Toast.LENGTH_SHORT).show()
        }
    }
    private val allowAppViewModel by viewModels<AllowAppViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        modalBottomSheetBinding.bottomSheetBody.layoutResource = R.layout.modal_content_modify_allow_app
        val viewStub = modalBottomSheetBinding.bottomSheetBody.inflate()
        modalContentModifyAllowAppBinding = ModalContentModifyAllowAppBinding.bind(viewStub)

        return modalBottomSheetBinding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            setUpRatio(bottomSheetDialog)
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //modalBottomSheetBinding.lifecycleOwner = viewLifecycleOwner
        initView()

        modalBottomSheetBinding.tvBottomSheetTitle.text = getString(R.string.allow_app_bottom_sheet_title)
        modalBottomSheetBinding.tvBottomSheetComplete.setOnClickListener {
            dismiss()
        }

        //setDummyData()
    }

    private fun initView() = with(modalContentModifyAllowAppBinding) {
        rvAllowAppsList.adapter = allowAppListAdapter
        allowAppViewModel.loadInstalledApps()

        viewLifecycleOwner.lifecycleScope.launch {
            allowAppViewModel.installedApps.collect { uiState ->
                when (uiState) {
                    is UiState.Loading -> {
                        // 로딩 상태 처리 (예: ProgressBar 표시)
                        progressBar.visibility = View.VISIBLE
                        rvAllowAppsList.visibility = View.GONE
                        Log.d("MyPageModifyAllowAppBottomSheet", "UiState.Loading")
                    }
                    is UiState.Success -> {
                        // 성공 상태 처리 (예: 데이터 리스트 표시)
                        progressBar.visibility = View.GONE
                        rvAllowAppsList.visibility = View.VISIBLE
                        allowAppListAdapter.submitList(uiState.data)
                        Log.d("MyPageModifyAllowAppBottomSheet", "UiState.Success")
                    }
                    is UiState.Error -> {
                        // 에러 상태 처리 (예: 에러 메시지 표시)
                        progressBar.visibility = View.GONE
                        rvAllowAppsList.visibility = View.GONE
                        Toast.makeText(context, "Error loading apps: ${uiState.exception.message}", Toast.LENGTH_SHORT).show()
                        Log.d("MyPageModifyAllowAppBottomSheet", "UiState.Error: ${uiState.exception.message}")
                    }
                }
            }
        }
    }

//    private fun setDummyData() {
//        val list = AppManager.getAppList()
//        allowAppListAdapter.submitList(list)
//    }

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
