package mega.privacy.android.app.meeting.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import mega.privacy.android.app.R
import mega.privacy.android.app.components.SimpleDividerItemDecoration
import mega.privacy.android.app.databinding.ActivityAssignModeratorBinding
import mega.privacy.android.app.meeting.adapter.AssignParticipantsAdapter
import mega.privacy.android.app.meeting.adapter.Participant
import mega.privacy.android.app.meeting.adapter.SelectedParticipantsAdapter
import mega.privacy.android.app.utils.StringResourcesUtils

/**
 * AssignModerator page allow moderator assign other users moderator when they are leaving the meeting
 */
class AssignModeratorBottomFragment(
    private val leaveMeeting: () -> Unit
) : BottomSheetDialogFragment() {
    private lateinit var binding: ActivityAssignModeratorBinding

    private var selectedParticipants: MutableList<Participant> = mutableListOf()
    private var participants: MutableList<Participant> = mutableListOf()

    private lateinit var participantsAdapter: AssignParticipantsAdapter
    private lateinit var selectedParticipantsAdapter: SelectedParticipantsAdapter
    @ExperimentalCoroutinesApi
    private val inMeetingViewModel by lazy { (parentFragment as InMeetingFragment).inMeetingViewModel }

    @ExperimentalCoroutinesApi
    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        binding = ActivityAssignModeratorBinding.inflate(layoutInflater)
        initRecyclerview()
        binding.btCancel.setOnClickListener { cancel() }
        binding.btOk.setOnClickListener { makeModerators() }
        binding.toolbar.setNavigationOnClickListener { dismiss() }

        inMeetingViewModel.participants.observe(this) { newData ->
            update(newData.filter { inMeetingViewModel.isStandardUser(it.peerId) && !it.isGuest }
                .map { it.copy() }
                .toMutableList())
        }

        dialog.setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog ?: return

        val bottomSheet: View = dialog.findViewById(R.id.design_bottom_sheet)
        bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

        BottomSheetBehavior.from(bottomSheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            isFitToContents = false
            skipCollapsed = true
            isHideable = true
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                        state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })

        }
    }

    @ExperimentalCoroutinesApi
    private fun initRecyclerview() {
        participantsAdapter = AssignParticipantsAdapter(inMeetingViewModel, selectCallback)
        selectedParticipantsAdapter =
            SelectedParticipantsAdapter(inMeetingViewModel, deleteCallback)

        binding.participantList.apply {
            layoutManager = LinearLayoutManager(context)
            itemAnimator = null
            adapter = participantsAdapter
            addItemDecoration(SimpleDividerItemDecoration(context))
        }

        binding.selectedParticipantList.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            itemAnimator = null
            clipToPadding = false
            adapter = selectedParticipantsAdapter
        }

        participantsAdapter.submitList(participants.toList())
    }

    /**
     * The call back function when users select participants, will update the selected list and participant list
     */
    private val selectCallback = fun(position: Int) {
        updateParticipantList(position)

        val participant = participants[position]

        if (participant.isChosenForAssign) {
            selectedParticipants.add(participant)
        } else {
            selectedParticipants.remove(participant)
        }

        updateSelectedParticipant()
    }

    /**
     * Update the selected participant list when user choose or delete the participant
     */
    private fun updateSelectedParticipant() {
        if (selectedParticipants.size > 0) {
            binding.toolbar.subtitle =
                StringResourcesUtils.getString(R.string.selected_items, selectedParticipants.size)
            binding.moderatorAddsContainer.isVisible = true
            binding.btOk.isEnabled = true
        } else {
            binding.moderatorAddsContainer.isVisible = false
            binding.toolbar.subtitle =
                StringResourcesUtils.getString(R.string.pick_new_moderator_message)
            binding.btOk.isEnabled = false
        }

        binding.moderatorAddsContainer.isVisible = selectedParticipants.size > 0

        selectedParticipantsAdapter.submitList(selectedParticipants.toMutableList())
    }

    /**
     * The call back function when users delete participants, will update the selected list and participant list
     */
    private val deleteCallback = fun(participant: Participant) {
        val position = participants.indexOf(participant)

        selectedParticipants.remove(participant)
        updateSelectedParticipant()
        updateParticipantList(position)
    }

    /**
     * Update the participant list when user choose or delete the participant
     */
    private fun updateParticipantList(position: Int) {
        val participant = participants[position]
        participant.isChosenForAssign = !participant.isChosenForAssign
        participants[position] = participant

        participantsAdapter.notifyItemChanged(position)
    }

    /**
     * Update the participant list and selected list when someone leave this meeting
     *
     * @param data new participant list
     */
    fun update(data: MutableList<Participant>) {
        // Get the current selected id
        participants = data
        val oldSelect = selectedParticipants.map { it.peerId }
        participants.forEach {
            if (oldSelect.contains(it.peerId)) {
                it.isChosenForAssign = true
            }
        }

        participantsAdapter.submitList(participants.toMutableList())

        val newSelect = participants.filter { it.isChosenForAssign }.map { it.peerId }
        selectedParticipants.run {
            removeIf {
                !newSelect.contains(it.peerId)
            }
            forEach {
                it.isChosenForAssign = true
            }
        }

        selectedParticipantsAdapter.submitList(selectedParticipants.toMutableList())
    }

    /**
     * Make selected participants to moderator
     */
    @ExperimentalCoroutinesApi
    private fun makeModerators() {
        // Get the list and assign the user in the list to moderator
        selectedParticipants.forEach {
            inMeetingViewModel.updateChatPermissions(it.peerId)
        }

        leaveMeeting.invoke()
        dismiss()
    }

    /**
     * Cancel this action and close this page
     */
    fun cancel() {
        dismiss()
    }

    companion object {
        fun newInstance(leaveMeeting: () -> Unit) = AssignModeratorBottomFragment(leaveMeeting)
    }

}