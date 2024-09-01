package com.rocket.cosmic_detox.presentation.component.bottomsheet.modifyallowedapp

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
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.databinding.ModalBottomsheetBinding
import com.rocket.cosmic_detox.databinding.ModalContentModifyAllowAppBinding
import com.rocket.cosmic_detox.presentation.uistate.GetListUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyPageModifyAllowAppBottomSheet: BottomSheetDialogFragment() {
    private val modalBottomSheetBinding by lazy { ModalBottomsheetBinding.inflate(layoutInflater) }
    private lateinit var modalContentModifyAllowAppBinding: ModalContentModifyAllowAppBinding
    private val allowAppListAdapter by lazy {
        AllowAppListAdapter(requireContext()) { app ->
            updateCheckedApp(app)
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
        initView()
        initViewModel()
    }

    private fun initView() = with(modalContentModifyAllowAppBinding) {
        rvAllowAppsList.adapter = allowAppListAdapter
        allowAppViewModel.loadInstalledApps()
        modalBottomSheetBinding.tvBottomSheetTitle.text = getString(R.string.allow_app_bottom_sheet_title)
        modalBottomSheetBinding.tvBottomSheetComplete.setOnClickListener {
            updateAllowApps()
            dismiss()
        }
    }

    private fun initViewModel() = with(allowAppViewModel) {
        viewLifecycleOwner.lifecycleScope.launch {
            installedApps
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collectLatest { uiState ->
                    modalContentModifyAllowAppBinding.apply {
                        progressBar.isVisible = uiState is GetListUiState.Loading
                        rvAllowAppsList.isVisible = uiState is GetListUiState.Success
                        tvSearchResultIsEmpty.isVisible = uiState is GetListUiState.Empty

                        when (uiState) {
                            is GetListUiState.Success -> {
                                allowAppListAdapter.submitList(uiState.data)
                            }
                            is GetListUiState.Error -> {
                                Toast.makeText(requireContext(), uiState.message, Toast.LENGTH_SHORT).show()
                            }
                            else -> Unit // Loading, Empty는 위에서 처리됨
                        }
                    }
                }
        }
    }

    private fun updateAllowApps() {
        val checkedApps = allowAppListAdapter.getCheckedItems()
        Log.d("AllowAppBottomSheet", "checkedApps: $checkedApps")
        if (checkedApps.isNotEmpty()) {
            allowAppViewModel.updateAllowApps("test2", checkedApps) // TODO: uid current user로 수정해야함.
        } else {
            Toast.makeText(requireContext(), "선택된 앱이 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCheckedApp(updatedApp: AllowedApp) {
        Toast.makeText(requireContext(), updatedApp.packageId, Toast.LENGTH_SHORT).show()
    }


//    private fun updateAllowApp(updatedApp: AllowedApp) {
//        val updatedList = allowAppListAdapter.currentList.toMutableList()
//        val originalApp = updatedList.find { it.packageId == updatedApp.packageId }
//        originalApp?.let {
//            updatedList.remove(it)
//        }
//
//        if (updatedApp.isAllowed) {
//            updatedList.add(0, updatedApp) // 체크되면 상단에 추가
//        } else {
//            updatedList.add(updatedApp) // 체크 해제 시 원래 리스트에 다시 추가
//        }
//
//        allowAppListAdapter.submitList(updatedList.sortedBy { it.appName })
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
