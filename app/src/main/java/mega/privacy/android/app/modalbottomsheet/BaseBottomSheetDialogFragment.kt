package mega.privacy.android.app.modalbottomsheet

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import mega.privacy.android.app.DatabaseHandler
import mega.privacy.android.app.R
import mega.privacy.android.app.di.MegaApi
import mega.privacy.android.app.interfaces.ActivityLauncher
import mega.privacy.android.app.utils.Constants.INVALID_VALUE
import mega.privacy.android.app.utils.Util
import mega.privacy.android.app.utils.Util.*
import nz.mega.sdk.MegaApiAndroid
import nz.mega.sdk.MegaChatApiAndroid
import javax.inject.Inject

/**
 * Mandatory base class for all BottomSheetDialogFragment which should behave as follows:
 *
 *  1. If sheet height is under or equal to 50% of screen height:
 *      - Show modal visibility at full height.
 *
 *  2. If sheet height is greater than 50 to 100% of screen height:
 *      - Show modal visibility at 50% of screen height.
 *      - On scroll, reveal full sheet.
 *      - Always left the bottom option of the 50% modal visibility partially visible.
 *
 *  3. If sheet height is greater than or equal to 100% of screen height:
 *      - Show modal partially visible at 50% of screen height.
 *      - On scroll, move to top of screen and scroll contents internally.
 *      - Always left the bottom option of the 50% . modal visibility partially visible.
 *
 * All classes which extends from here must implement onCreateView to set the mandatory views defined
 * as properties below. If not visible actions are:
 *  - Hidden here, then is not needed to implement onViewCreated.
 *  - Not hidden here, then is mandatory to implement onViewCreated too, hide the not visible actions
 *      and finally call super.onViewCreated at the end of the method to ensure the final view is
 *      already set.
 *
 * @property contentView        This view must be set in children onCreateView.
 *                              It must be the root view.
 * @property itemsLayout        This view must be set in children onCreateView.
 *                              It must be the view containing all available actions.
 * @property savedState         State of the sheet to restore it after screen's rotations.
 */
@AndroidEntryPoint
open class BaseBottomSheetDialogFragment : BottomSheetDialogFragment(), ActivityLauncher {

    companion object {
        private const val HEIGHT_SEPARATOR = 1F
        private const val STATE = "STATE"
    }

    @MegaApi
    @Inject
    lateinit var megaApi: MegaApiAndroid

    @Inject
    lateinit var megaChatApi: MegaChatApiAndroid

    @Inject
    lateinit var dbH: DatabaseHandler

    protected lateinit var contentView: View
    protected lateinit var itemsLayout: View

    private var savedState = INVALID_VALUE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedState = savedInstanceState?.getInt(STATE, INVALID_VALUE) ?: INVALID_VALUE
        view.post { setBottomSheetBehavior() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE, BottomSheetBehavior.from(contentView.parent as View).state)
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog ?: return
        BottomSheetBehavior.from(dialog.findViewById(R.id.design_bottom_sheet)).state =
            BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    override fun onResume() {
        super.onResume()

        val dialog = dialog ?: return
        val window = dialog.window ?: return

        // In landscape mode, we need limit the bottom sheet dialog width.
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val displayMetrics = resources.displayMetrics
            val maxSize = displayMetrics.heightPixels
            window.setLayout(maxSize, ViewGroup.LayoutParams.MATCH_PARENT)
        }

        // But `setLayout` causes navigation buttons almost invisible in light mode,
        // in this case we set navigation bar background with light grey to make
        // navigation buttons visible.
        if (!Util.isDarkMode(requireContext())) {
            // Only set navigation bar elements colour, View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR = 0x00000010
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = 0x00000010
        }
    }

    /**
     * Sets the initial state of a BottomSheet and its state.
     */
    private fun setBottomSheetBehavior() {
        val customPeekHeight = getCustomPeekHeight()

        BottomSheetBehavior.from(contentView.parent as View).apply {
            state = when {
                savedState != INVALID_VALUE -> {
                    peekHeight = customPeekHeight
                    savedState
                }
                customPeekHeight == contentView.height -> {
                    BottomSheetBehavior.STATE_EXPANDED
                }
                else -> {
                    peekHeight = customPeekHeight
                    BottomSheetBehavior.STATE_COLLAPSED
                }
            }

            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_HIDDEN -> dismissAllowingStateLoss()
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                return
                            }

                            val screenHeight =
                                resources.displayMetrics.heightPixels - getStatusBarHeight()

                            if (isDarkMode(requireContext()) && bottomSheet.height >= screenHeight) {
                                requireActivity().window.statusBarColor =
                                    getColor(requireContext(), R.color.grey_010_alpha_049)
                            }
                        }
                        else -> requireActivity().window.statusBarColor =
                            getColor(requireContext(), android.R.color.transparent)
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                }
            })
        }
    }

    /**
     * Hides the BottomSheet.
     */
    protected fun setStateBottomSheetBehaviorHidden() {
        BottomSheetBehavior.from(contentView.parent as View).state =
            BottomSheetBehavior.STATE_HIDDEN
    }

    /**
     * Gets the initial height of a BottomSheet.
     * It depends on the number of visible options on it, the display height
     * and current orientation of the used device.
     * The maximum height will be a bit more than half of the screen.
     *
     * @return The initial height of a BottomSheet.
     */
    private fun getCustomPeekHeight(): Int {
        val halfHeightDisplay = resources.displayMetrics.heightPixels / 2
        val contentViewHeight = contentView.height

        if (contentViewHeight <= halfHeightDisplay) {
            return contentViewHeight
        }

        var peekHeight = when (contentView) {
            is LinearLayout -> {
                (contentView as LinearLayout).getChildAt(0).height
            }
            is RelativeLayout -> {
                (contentView as RelativeLayout).getChildAt(0).height
            }
            is ConstraintLayout -> {
                (contentView as ConstraintLayout).getChildAt(0).height
            }
            else -> 0
        }

        val heightSeparator by lazy { Util.dp2px(HEIGHT_SEPARATOR) }

        val childCount = when (itemsLayout) {
            is LinearLayout -> (itemsLayout as LinearLayout).childCount
            is RelativeLayout -> (itemsLayout as RelativeLayout).childCount
            is ConstraintLayout -> (itemsLayout as ConstraintLayout).childCount
            is RadioGroup -> (itemsLayout as RadioGroup).childCount
            else -> 0
        }

        for (i in 0 until childCount) {
            val v: View = when (itemsLayout) {
                is LinearLayout -> (itemsLayout as LinearLayout).getChildAt(i)
                is RelativeLayout -> (itemsLayout as RelativeLayout).getChildAt(i)
                is ConstraintLayout -> (itemsLayout as ConstraintLayout).getChildAt(i)
                is RadioGroup -> (itemsLayout as RadioGroup).getChildAt(i)
                else -> continue
            }

            if (v.isVisible) {
                val height = v.height
                peekHeight += height

                if (itemsLayout is RadioGroup
                    && (itemsLayout as RadioGroup).showDividers == RadioGroup.SHOW_DIVIDER_MIDDLE
                ) {
                    peekHeight += dp2px(1F)
                }

                if (peekHeight > halfHeightDisplay && height > heightSeparator) {
                    peekHeight -= height / 2
                    return peekHeight
                }
            }
        }

        return peekHeight
    }

    override fun launchActivity(intent: Intent) {
        startActivity(intent)
    }

    override fun launchActivityForResult(intent: Intent, requestCode: Int) {
        startActivityForResult(intent, requestCode)
    }
}