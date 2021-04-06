package mega.privacy.android.app.meeting.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.meeting_on_boarding_fragment.*
import mega.privacy.android.app.meeting.activity.MeetingActivity
import mega.privacy.android.app.utils.LogUtil
import mega.privacy.android.app.utils.LogUtil.logDebug
import mega.privacy.android.app.utils.Util
import mega.privacy.android.app.utils.Util.showKeyboardDelayed
import nz.mega.sdk.*

@AndroidEntryPoint
class CreateMeetingFragment : AbstractMeetingOnBoardingFragment(), MegaRequestListenerInterface,
    MegaChatRequestListenerInterface {
    private val viewModel: CreateMeetingViewModel by viewModels()

    override fun meetingButtonClick() {
        logDebug("Meeting Name: $meetingName")
        // will replaced
        val peers = MegaChatPeerList.createInstance()
        viewModel.createMeeting(false, peers, this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showKeyboardDelayed(type_meeting_edit_text)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViewModel()
        setProfileAvatar()
    }

    /**
     * Initialize viewmodel
     */
    private fun initViewModel(){
        abstractMeetingOnBoardingViewModel.result.observe(viewLifecycleOwner) {
            (activity as MeetingActivity).setBottomFloatingPanelViewHolder(true)
        }
        abstractMeetingOnBoardingViewModel.tips.observe(viewLifecycleOwner) {
            showToast(fab_tip_location, it, Toast.LENGTH_SHORT)
        }
    }

    override fun onRequestStart(api: MegaApiJava?, request: MegaRequest?) {
        Toast.makeText(requireContext(), "onRequestStart", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestUpdate(api: MegaApiJava?, request: MegaRequest?) {
        Toast.makeText(requireContext(), "onRequestUpdate", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestFinish(api: MegaApiJava?, request: MegaRequest?, e: MegaError?) {
        Toast.makeText(requireContext(), "onRequestFinish", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestTemporaryError(api: MegaApiJava?, request: MegaRequest?, e: MegaError?) {
        Toast.makeText(requireContext(), "onRequestTemporaryError", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestStart(api: MegaChatApiJava?, request: MegaChatRequest?) {

    }

    override fun onRequestUpdate(api: MegaChatApiJava?, request: MegaChatRequest?) {

    }

    override fun onRequestFinish(
        api: MegaChatApiJava?,
        request: MegaChatRequest?,
        e: MegaChatError?
    ) {
        logDebug("onRequestFinish: " + request!!.requestString + " " + request!!.type)
    }

    override fun onRequestTemporaryError(
        api: MegaChatApiJava?,
        request: MegaChatRequest?,
        e: MegaChatError?
    ) {

    }
}