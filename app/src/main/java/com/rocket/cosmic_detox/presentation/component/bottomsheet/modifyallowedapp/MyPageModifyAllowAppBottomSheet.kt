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
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rocket.cosmic_detox.R
import com.rocket.cosmic_detox.data.model.AllowedApp
import com.rocket.cosmic_detox.data.model.CheckedApp
import com.rocket.cosmic_detox.databinding.ModalBottomsheetBinding
import com.rocket.cosmic_detox.databinding.ModalContentModifyAllowAppBinding
import com.rocket.cosmic_detox.presentation.extensions.has
import com.rocket.cosmic_detox.presentation.extensions.toAllowedApp
import com.rocket.cosmic_detox.presentation.uistate.GetListUiState
import com.rocket.cosmic_detox.presentation.view.fragment.mypage.MyPageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyPageModifyAllowAppBottomSheet: BottomSheetDialogFragment() {
    private val modalBottomSheetBinding by lazy { ModalBottomsheetBinding.inflate(layoutInflater) }
    private lateinit var modalContentModifyAllowAppBinding: ModalContentModifyAllowAppBinding
    private val allowAppListAdapter by lazy {
        AllowAppListAdapter(
            context = requireContext(),
            onClick = { app ->
                updateCheckedApp(app)
            },
            onCheckboxClick = { app ->
                updateCheckedApp(app) // 체크박스, 아이템뷰 클릭 시 중복되는 문제 때문에 같은 로직을 독립적인 클릭 이벤트로 처리
            }
        )
    }
    private val allowAppViewModel by viewModels<AllowAppViewModel>()
    private val myPageViewModel by activityViewModels<MyPageViewModel>() // TODO: 나중에 두 뷰모델을 myPageViewModel로 통합해야하나 고민해보기
    private val args: MyPageModifyAllowAppBottomSheetArgs by navArgs()
    private lateinit var checkedApps: MutableList<AllowedApp>

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
        checkedApps = if (args.allowedApps.isNotEmpty()) {
            args.allowedApps.toMutableList()
        } else {
            mutableListOf()
        }
        Log.d("AllowAppBottomSheet", "args.allowedApps: ${args.allowedApps}")
        modalBottomSheetBinding.tvBottomSheetTitle.text =
            getString(R.string.allow_app_bottom_sheet_title)
        modalBottomSheetBinding.tvBottomSheetComplete.setOnClickListener {
            updateAllowApps()
        }
        ivClearSearchText.setOnClickListener {
            etSearchText.text.clear()
        }
        etSearchText.apply {
            doAfterTextChanged { text ->
                allowAppViewModel.searchApp(text.toString())
            }
            setOnEditorActionListener { _, actionId, _ ->
                clearFocus()
                var handled = false
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard(this)
                    handled = true
                }
                handled
            }
        }
    }

    private fun hideKeyboard(editText: EditText) {
        val inputManager: InputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(editText.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    private fun initViewModel() = with(allowAppViewModel) {
        viewLifecycleOwner.lifecycleScope.launch {
            installedApps
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collectLatest { uiState ->
                    modalContentModifyAllowAppBinding.apply {
                        progressBarModifyAllowApp.isVisible = uiState is GetListUiState.Loading
                        rvAllowAppsList.isVisible = uiState is GetListUiState.Success
                        tvSearchResultIsEmpty.isVisible = uiState is GetListUiState.Empty

                        when (uiState) {
                            is GetListUiState.Success -> {
                                // args.allowApps에 포함된 앱은 체크된 상태로 보여줌
                                // uiState.data에 args.allowedApps의 packageId가 포함되어 있으면 체크된 상태로 보여줌
                                val updatedApps = uiState.data.map { app ->
                                    if (args.allowedApps.any { app has it }) {
                                        app.copy(isChecked = true) // copy 메서드로 isChecked 값 수정
                                    } else {
                                        app
                                    }
                                }
                                Log.d("AllowAppBottomSheet", "updatedApps: $updatedApps")
                                allowAppListAdapter.submitList(updatedApps)
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
        allowAppViewModel.updateAllowApps(args.allowedApps.toList(), checkedApps)
        modalContentModifyAllowAppBinding.progressBarModifyAllowApp.isVisible = true
        viewLifecycleOwner.lifecycleScope.launch {
            allowAppViewModel.updateResult.collectLatest { success ->
                if (success) {
                    dismiss()
                    myPageViewModel.loadMyInfo()
                    allowAppViewModel.resetUpdateResult() // 상태 초기화
                } else {
                    Log.e("MyPageSetLimitUseTimeBottomSheet", "업데이트 실패")
                }
                modalContentModifyAllowAppBinding.progressBarModifyAllowApp.isVisible = false
            }
        }
    }

    private fun updateCheckedApp(updatedApp: CheckedApp) {
        // 만약 체크된 앱이면 체크를 해제하고, 체크되지 않은 앱이면 체크를 함
        val index = checkedApps.indexOfFirst { it.packageId == updatedApp.packageId }
        if (index != -1) {
            checkedApps.removeAt(index)
        } else {
            checkedApps.add(updatedApp.toAllowedApp())
        }
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
