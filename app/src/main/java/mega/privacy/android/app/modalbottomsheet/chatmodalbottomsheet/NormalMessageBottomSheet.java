package mega.privacy.android.app.modalbottomsheet.chatmodalbottomsheet;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.twemoji.EmojiImageView;
import mega.privacy.android.app.components.twemoji.EmojiKeyboard;
import mega.privacy.android.app.components.twemoji.emoji.Emoji;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.controllers.ChatController;
import mega.privacy.android.app.lollipop.controllers.NodeController;
import mega.privacy.android.app.lollipop.megachat.AndroidMegaChatMessage;
import mega.privacy.android.app.lollipop.megachat.ChatActivityLollipop;
import mega.privacy.android.app.modalbottomsheet.UtilsModalBottomSheet;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatMessage;
import nz.mega.sdk.MegaChatRoom;
import nz.mega.sdk.MegaHandleList;

import static mega.privacy.android.app.utils.Constants.*;
import static mega.privacy.android.app.utils.LogUtil.*;
import static mega.privacy.android.app.utils.Util.*;

public class NormalMessageBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener {

    Context context;
    MegaHandleList handleList;
    AndroidMegaChatMessage message = null;
    long chatId;
    long messageId;
    String email=null;

    int position;

    private BottomSheetBehavior mBehavior;
    private RelativeLayout mainLayout;
    private LinearLayout optionsLayout;

    private LinearLayout reactionsLayout;
    private RelativeLayout firstReaction;
    private EmojiImageView firstEmoji;
    private RelativeLayout secondReaction;
    private EmojiImageView secondEmoji;
    private RelativeLayout thirdReaction;
    private EmojiImageView thirdEmoji;
    private RelativeLayout fourthReaction;
    private EmojiImageView fourthEmoji;
    private RelativeLayout fifthReaction;
    private EmojiImageView fifthEmoji;
    private EmojiKeyboard emojiKeyboard;

    private RelativeLayout addReaction;

    private LinearLayout itemsLayout;
    private LinearLayout optionForward;
    private LinearLayout optionEdit;
    private LinearLayout optionCopy;
    private LinearLayout optionDelete;

    private DisplayMetrics outMetrics;
    static ManagerActivityLollipop.DrawerItem drawerItem = null;
    private int heightDisplay;
    private MegaApiAndroid megaApi;
    private MegaChatApiAndroid megaChatApi;
    private DatabaseHandler dbH;
    private NodeController nC;
    private ChatController chatC;
    private MegaChatRoom chatRoom;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logDebug("onCreate");

        if (megaApi == null) {
            megaApi = MegaApplication.getInstance().getMegaApi();
        }
        if (megaChatApi == null) {
            megaChatApi = MegaApplication.getInstance().getMegaChatApi();
        }

        if (savedInstanceState != null) {
            logDebug("Bundle is NOT NULL");
            chatId = savedInstanceState.getLong(CHAT_ID, -1);
            messageId = savedInstanceState.getLong(MESSAGE_ID, -1);

            MegaChatMessage messageMega = megaChatApi.getMessage(chatId, messageId);
            if (messageMega != null) {
                message = new AndroidMegaChatMessage(messageMega);
            }
        } else {

            if (context instanceof ChatActivityLollipop) {
                chatId = ((ChatActivityLollipop) context).idChat;
                messageId = ((ChatActivityLollipop) context).selectedMessageId;
            }

            MegaChatMessage messageMega = megaChatApi.getMessage(chatId, messageId);
            if (messageMega != null) {
                message = new AndroidMegaChatMessage(messageMega);
            }
        }

        chatRoom = megaChatApi.getChatRoom(chatId);
        nC = new NodeController(context);
        chatC = new ChatController(context);
        dbH = MegaApplication.getInstance().getDbH();
    }

    @Override
    public void setupDialog(final Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        heightDisplay = outMetrics.heightPixels;

        View contentView = View.inflate(getContext(), R.layout.bottom_sheet_text_msg, null);
        mainLayout = contentView.findViewById(R.id.bottom_sheet);

        emojiKeyboard = contentView.findViewById(R.id.emoji_keyboard);
        emojiKeyboard.init(((ChatActivityLollipop) context), px2dp(239, outMetrics));

        optionsLayout = contentView.findViewById(R.id.bottom_sheet_options_layout);
        reactionsLayout = contentView.findViewById(R.id.reaction_layout);
        firstReaction = reactionsLayout.findViewById(R.id.first_emoji_layout);
        firstEmoji = reactionsLayout.findViewById(R.id.first_emoji_image);
        secondReaction = reactionsLayout.findViewById(R.id.second_emoji_layout);
        secondEmoji = reactionsLayout.findViewById(R.id.second_emoji_image);
        thirdReaction = reactionsLayout.findViewById(R.id.third_emoji_layout);
        thirdEmoji = reactionsLayout.findViewById(R.id.third_emoji_image);
        fourthReaction = reactionsLayout.findViewById(R.id.fourth_emoji_layout);
        fourthEmoji = reactionsLayout.findViewById(R.id.fourth_emoji_image);
        fifthReaction = reactionsLayout.findViewById(R.id.fifth_emoji_layout);
        fifthEmoji = reactionsLayout.findViewById(R.id.fifth_emoji_image);
        addReaction = reactionsLayout.findViewById(R.id.icon_more_reactions);
        itemsLayout = contentView.findViewById(R.id.items_layout);
        optionForward = contentView.findViewById(R.id.forward_layout);
        optionEdit = contentView.findViewById(R.id.edit_layout);
        optionCopy = contentView.findViewById(R.id.copy_layout);
        optionDelete = contentView.findViewById(R.id.delete_layout);

        firstEmoji.setEmoji(new Emoji(0x1f642, R.drawable.emoji_twitter_1f642));
        secondEmoji.setEmoji(new Emoji(0x2639, R.drawable.emoji_twitter_2639));
        thirdEmoji.setEmoji(new Emoji(0x1f923, R.drawable.emoji_twitter_1f923));
        fourthEmoji.setEmoji(new Emoji(0x1f44d, R.drawable.emoji_twitter_1f44d));
        fifthEmoji.setEmoji(new Emoji(0x1f44f, R.drawable.emoji_twitter_1f44f));

        optionForward.setOnClickListener(this);
        optionEdit.setOnClickListener(this);
        optionCopy.setOnClickListener(this);
        optionDelete.setOnClickListener(this);
        firstReaction.setOnClickListener(this);
        secondReaction.setOnClickListener(this);
        thirdReaction.setOnClickListener(this);
        fourthReaction.setOnClickListener(this);
        fifthReaction.setOnClickListener(this);
        addReaction.setOnClickListener(this);

        if (message == null || chatRoom == null || !(context instanceof ChatActivityLollipop) || ((ChatActivityLollipop) context).hasMessagesRemoved(message.getMessage()) || message.isUploading()) {
            optionForward.setVisibility(View.GONE);
            optionEdit.setVisibility(View.GONE);
            optionCopy.setVisibility(View.GONE);
            optionDelete.setVisibility(View.GONE);
        } else {
            optionCopy.setVisibility(View.VISIBLE);
            if (((chatRoom.getOwnPrivilege() == MegaChatRoom.PRIV_RM || chatRoom.getOwnPrivilege() == MegaChatRoom.PRIV_RO) && !chatRoom.isPreview())) {
                optionForward.setVisibility(View.GONE);
                optionEdit.setVisibility(View.GONE);
                optionDelete.setVisibility(View.GONE);

            } else {
                if (!isOnline(context) || chatC.isInAnonymousMode()) {
                    optionForward.setVisibility(View.GONE);
                } else {
                    optionForward.setVisibility(View.VISIBLE);
                }
                if (message.getMessage().getUserHandle() != megaChatApi.getMyUserHandle() || !message.getMessage().isEditable()) {
                    optionEdit.setVisibility(View.GONE);
                    optionDelete.setVisibility(View.GONE);
                } else {
                    optionEdit.setVisibility(View.VISIBLE);
                    optionDelete.setVisibility(View.VISIBLE);
                }
            }
        }

        dialog.setContentView(contentView);
        mBehavior = BottomSheetBehavior.from((View) mainLayout.getParent());
        mBehavior.setPeekHeight(UtilsModalBottomSheet.getPeekHeight(itemsLayout, heightDisplay, context, 48));
        mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

    }

    @Override
    public void onClick(View v) {
        if (message == null) {
            logWarning("The message is NULL");
            return;
        }
        ArrayList<AndroidMegaChatMessage> messagesSelected = new ArrayList<>();
        messagesSelected.add(message);
        switch(v.getId()){

            case R.id.forward_layout:
                ((ChatActivityLollipop) context).forwardMessages(messagesSelected);
                dismissAllowingStateLoss();
                break;

            case R.id.edit_layout:
                ((ChatActivityLollipop) context).editMessage(messagesSelected);
                dismissAllowingStateLoss();
                break;

            case R.id.copy_layout:
                ((ChatActivityLollipop) context).copyMessage(message);
                dismissAllowingStateLoss();
                break;

            case R.id.delete_layout:
                ((ChatActivityLollipop) context).showConfirmationDeleteMessages(messagesSelected, chatRoom);
                dismissAllowingStateLoss();
                break;

            case R.id.first_emoji_layout:
            case R.id.second_emoji_layout:
            case R.id.third_emoji_layout:
            case R.id.fourth_emoji_layout:
            case R.id.fifth_emoji_layout:
                dismissAllowingStateLoss();
                break;

            case R.id.icon_more_reactions:
                if(emojiKeyboard != null){
                    optionsLayout.setVisibility(View.GONE);
                    emojiKeyboard.setVisibility(View.VISIBLE);
                }
                break;
        }

        mBehavior = BottomSheetBehavior.from((View) mainLayout.getParent());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(CHAT_ID, chatId);
        outState.putLong(MESSAGE_ID, messageId);
    }
}
