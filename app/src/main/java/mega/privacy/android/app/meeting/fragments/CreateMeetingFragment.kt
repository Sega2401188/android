package mega.privacy.android.app.meeting.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.MotionEvent.ACTION_DOWN
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_meeting.*
import kotlinx.android.synthetic.main.meeting_on_boarding_fragment.*
import mega.privacy.android.app.R
import mega.privacy.android.app.meeting.activity.MeetingActivity.Companion.MEETING_ACTION_CREATE
import mega.privacy.android.app.utils.ChatUtil
import mega.privacy.android.app.utils.ChatUtil.isAllowedTitle
import mega.privacy.android.app.utils.Constants
import mega.privacy.android.app.utils.LogUtil.logDebug
import mega.privacy.android.app.utils.Util
import mega.privacy.android.app.utils.Util.hideKeyboardView
import mega.privacy.android.app.utils.Util.showKeyboardDelayed
import nz.mega.sdk.MegaChatRoom
import java.util.*


@AndroidEntryPoint
class CreateMeetingFragment : AbstractMeetingOnBoardingFragment() {

    private val viewModel: CreateMeetingViewModel by viewModels()

    //Create first the chat
    var chats = ArrayList<MegaChatRoom>()

    override fun onMeetingButtonClick() {
        if (!isAllowedTitle(meetingName)) {
            type_meeting_edit_text.error = getString(R.string.title_long)
            type_meeting_edit_text.requestFocus()
            return
        }

        // if the name is empty, get the default name for the meeting
        if (meetingName.isEmpty()) {
            type_meeting_edit_text.setText(viewModel.initHintMeetingName())
        }

        logDebug("Meeting Name: $meetingName")
        releaseVideoAndHideKeyboard()

        val action = InMeetingFragmentDirections.actionGlobalInMeeting(
            action = MEETING_ACTION_CREATE,
            meetingName = meetingName
        )
        findNavController().navigate(action)
    }

    fun releaseVideoAndHideKeyboard() {
        hideKeyboardView(type_meeting_edit_text.context, type_meeting_edit_text, 0)
        releaseVideoDeviceAndRemoveChatVideoListener()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Toolbar should be set to TRANSPARENT in "Create Meeting"
        activity?.toolbar?.background = ColorDrawable(Color.TRANSPARENT)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Reset the toolbar background to the default value
        activity?.toolbar?.background = ContextCompat.getDrawable(requireContext(), R.drawable.gradient_shape_callschat)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initComponent()
    }

    /**
     * Initialize components of UI
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun initComponent() {
        binding.root.setOnTouchListener { v, event ->
            when (event.action) {
                ACTION_DOWN -> {
                    if (v != null) {
                        if (v != type_meeting_edit_text)
                            hideKeyboardView(
                                type_meeting_edit_text.context,
                                type_meeting_edit_text,
                                0
                            )
                    }
                }
            }
            true
        }
        binding.typeMeetingEditText.let {
            it.visibility = View.VISIBLE
            it.setSelectAllOnFocus(true)
            it.setEmojiSize(Util.dp2px(Constants.EMOJI_SIZE.toFloat(), outMetrics))
            val defaultName = viewModel.initHintMeetingName()
            it.hint = defaultName
            val maxAllowed = ChatUtil.getMaxAllowed(defaultName)
            it.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxAllowed))
        }
    }

    /**
     * Initialize ViewModel
     */
    private fun initViewModel() {
        binding.createviewmodel = viewModel
        // Set default meeting name
        viewModel.initMeetingName()
        initRTCAudioManager()

        viewModel.meetingName.observe(viewLifecycleOwner) {
            meetingName = it
        }
    }

    /**
     * Callback when permissions are all granted
     * @param permissions the granted permissions
     */
    override fun onRequiresPermission(permissions: ArrayList<String>) {
        super.onRequiresPermission(permissions)
        showKeyboardDelayed(type_meeting_edit_text)
    }

    /**
     * Callback when permissions are denied
     * @param permissions the granted permissions
     */
    override fun onPermissionDenied(permissions: ArrayList<String>) {
        super.onPermissionDenied(permissions)
        showKeyboardDelayed(type_meeting_edit_text)
    }
}