package mega.privacy.android.app.lollipop;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jeremyliao.liveeventbus.LiveEventBus;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.palette.graphics.Palette;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import mega.privacy.android.app.AuthenticityCredentialsActivity;
import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.activities.ManageChatHistoryActivity;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaContactDB;
import mega.privacy.android.app.R;
import mega.privacy.android.app.activities.PasscodeActivity;
import mega.privacy.android.app.components.AppBarStateChangeListener;
import mega.privacy.android.app.components.MarqueeTextView;
import mega.privacy.android.app.components.attacher.MegaAttacher;
import mega.privacy.android.app.components.saver.NodeSaver;
import mega.privacy.android.app.components.twemoji.EmojiEditText;
import mega.privacy.android.app.components.twemoji.EmojiTextView;
import mega.privacy.android.app.interfaces.SnackbarShower;
import mega.privacy.android.app.interfaces.ActionNodeCallback;
import mega.privacy.android.app.listeners.CreateChatListener;
import mega.privacy.android.app.listeners.SetAttrUserListener;
import mega.privacy.android.app.meeting.listeners.StartChatCallListener;
import mega.privacy.android.app.lollipop.controllers.ChatController;
import mega.privacy.android.app.lollipop.controllers.ContactController;
import mega.privacy.android.app.lollipop.controllers.NodeController;
import mega.privacy.android.app.lollipop.listeners.MultipleRequestListener;
import mega.privacy.android.app.lollipop.megachat.ChatActivityLollipop;
import mega.privacy.android.app.lollipop.megachat.NodeAttachmentHistoryActivity;
import mega.privacy.android.app.modalbottomsheet.ContactFileListBottomSheetDialogFragment;
import mega.privacy.android.app.modalbottomsheet.ContactNicknameBottomSheetDialogFragment;
import mega.privacy.android.app.utils.AlertsAndWarnings;
import mega.privacy.android.app.utils.AskForDisplayOverDialog;
import mega.privacy.android.app.utils.ColorUtils;
import mega.privacy.android.app.utils.StringResourcesUtils;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaChatApi;
import nz.mega.sdk.MegaChatApiJava;
import nz.mega.sdk.MegaChatCall;
import nz.mega.sdk.MegaChatError;
import nz.mega.sdk.MegaChatListItem;
import nz.mega.sdk.MegaChatListenerInterface;
import nz.mega.sdk.MegaChatPeerList;
import nz.mega.sdk.MegaChatPresenceConfig;
import nz.mega.sdk.MegaChatRequest;
import nz.mega.sdk.MegaChatRequestListenerInterface;
import nz.mega.sdk.MegaChatRoom;
import nz.mega.sdk.MegaContactRequest;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaEvent;
import nz.mega.sdk.MegaGlobalListenerInterface;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import nz.mega.sdk.MegaUser;
import nz.mega.sdk.MegaUserAlert;

import static mega.privacy.android.app.constants.BroadcastConstants.*;
import static mega.privacy.android.app.constants.EventConstants.EVENT_CALL_ON_HOLD_CHANGE;
import static mega.privacy.android.app.constants.EventConstants.EVENT_CALL_STATUS_CHANGE;
import static mega.privacy.android.app.constants.EventConstants.EVENT_SESSION_ON_HOLD_CHANGE;
import static mega.privacy.android.app.modalbottomsheet.ModalBottomSheetUtil.*;
import static mega.privacy.android.app.utils.AlertsAndWarnings.showForeignStorageOverQuotaWarningDialog;
import static mega.privacy.android.app.utils.AlertsAndWarnings.showOverDiskQuotaPaywallWarning;
import static mega.privacy.android.app.utils.CacheFolderManager.*;
import static mega.privacy.android.app.utils.CallUtil.*;
import static mega.privacy.android.app.utils.FileUtil.*;
import static mega.privacy.android.app.utils.ChatUtil.*;
import static mega.privacy.android.app.utils.LogUtil.*;
import static mega.privacy.android.app.utils.MegaProgressDialogUtil.createProgressDialog;
import static mega.privacy.android.app.utils.StringResourcesUtils.getQuantityString;
import static mega.privacy.android.app.utils.TimeUtils.*;
import static mega.privacy.android.app.utils.Util.*;
import static mega.privacy.android.app.utils.Constants.*;
import static mega.privacy.android.app.utils.ContactUtil.*;
import static mega.privacy.android.app.utils.AvatarUtil.*;
import static mega.privacy.android.app.utils.TextUtil.*;
import static nz.mega.sdk.MegaApiJava.INVALID_HANDLE;
import static nz.mega.sdk.MegaApiJava.STORAGE_STATE_PAYWALL;
import static nz.mega.sdk.MegaChatApiJava.MEGACHAT_INVALID_HANDLE;

import mega.privacy.android.app.components.AppBarStateChangeListener.State;

@SuppressLint("NewApi")

public class ContactInfoActivityLollipop extends PasscodeActivity
		implements MegaChatRequestListenerInterface, OnClickListener,
		MegaRequestListenerInterface, MegaChatListenerInterface, OnItemClickListener,
		MegaGlobalListenerInterface, ActionNodeCallback, SnackbarShower, StartChatCallListener.StartChatCallCallback {

	private ChatController chatC;
	private ContactController cC;

	RelativeLayout imageLayout;
	AlertDialog permissionsDialog;
	AlertDialog statusDialog;
	AlertDialog setNicknameDialog;
	ContactInfoActivityLollipop contactInfoActivityLollipop;
	CoordinatorLayout fragmentContainer;
	CollapsingToolbarLayout collapsingToolbar;

	ImageView contactPropertiesImage;
	LinearLayout optionsLayout;

	//Info of the user
	private EmojiTextView nameText;
	private TextView emailText;
	private TextView setNicknameText;

	LinearLayout chatOptionsLayout;
	View dividerChatOptionsLayout;
	RelativeLayout sendMessageLayout;
	RelativeLayout audioCallLayout;
	RelativeLayout videoCallLayout;

	LinearLayout notificationsLayout;
	private RelativeLayout notificationsSwitchLayout;
	SwitchCompat notificationsSwitch;
	TextView notificationsTitle;
	private TextView notificationsSubTitle;
	View dividerNotificationsLayout;

    boolean startVideo = false;
    private boolean isChatOpen;

	private RelativeLayout verifyCredentialsLayout;
	private TextView verifiedText;
	private ImageView verifiedImage;

	RelativeLayout sharedFoldersLayout;
	TextView sharedFoldersText;
	Button sharedFoldersButton;
	View dividerSharedFoldersLayout;

	RelativeLayout shareContactLayout;
	View dividerShareContactLayout;

	RelativeLayout sharedFilesLayout;
	View dividerSharedFilesLayout;

	//Toolbar elements
	private EmojiTextView firstLineTextToolbar;
	private int firstLineTextMaxWidthExpanded;
	private int firstLineTextMaxWidthCollapsed;
	private int contactStateIcon;
	private int contactStateIconPaddingLeft;

	private MarqueeTextView secondLineTextToolbar;
	private State stateToolbar = State.IDLE;

	private final MegaAttacher megaAttacher = new MegaAttacher(this);
	private final NodeSaver nodeSaver = new NodeSaver(this, this, this,
			AlertsAndWarnings.showSaveToDeviceConfirmDialog(this));

	private RelativeLayout manageChatLayout;
	private TextView retentionTimeText;
	private View dividerClearChatLayout;
	RelativeLayout removeContactChatLayout;

	Toolbar toolbar;
	ActionBar aB;
	AppBarLayout appBarLayout;

	MegaUser user;
	long chatHandle;
	String userEmailExtra;
	MegaChatRoom chat;

	boolean fromContacts = true;

	Display display;
	DisplayMetrics outMetrics;
	float density;
	float scaleW;
	float scaleH;

	DatabaseHandler dbH = null;

	Drawable drawableShare;
	Drawable drawableSend;
	Drawable drawableArrow;
	Drawable drawableDots;

	private MenuItem shareMenuItem;
	private MenuItem sendFileMenuItem;
	private MenuItem returnCallMenuItem;
	private Chronometer chronometerMenuItem;
	private LinearLayout layoutCallMenuItem;

	boolean isShareFolderExpanded;
    ContactSharedFolderFragment sharedFoldersFragment;
    MegaNode selectedNode;
    NodeController nC;
    boolean moveToRubbish;
    long parentHandle;

	private ContactFileListBottomSheetDialogFragment bottomSheetDialogFragment;
	private ContactNicknameBottomSheetDialogFragment contactNicknameBottomSheetDialogFragment;

	private AskForDisplayOverDialog askForDisplayOverDialog;

	private RelativeLayout callInProgressLayout;
	private Chronometer callInProgressChrono;
	private TextView callInProgressText;
	private LinearLayout microOffLayout;
	private LinearLayout videoOnLayout;

	private BroadcastReceiver manageShareReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null) return;

			if (sharedFoldersFragment != null) {
				sharedFoldersFragment.clearSelections();
				sharedFoldersFragment.hideMultipleSelect();
			}

			if (statusDialog != null) {
				statusDialog.dismiss();
			}
		}
	};

	private BroadcastReceiver userNameReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null
					|| intent.getAction() == null
					|| user == null
					|| intent.getLongExtra(EXTRA_USER_HANDLE, INVALID_HANDLE) != user.getHandle()) {
				return;
			}

			if (intent.getAction().equals(ACTION_UPDATE_NICKNAME)
					|| intent.getAction().equals(ACTION_UPDATE_FIRST_NAME)
					|| intent.getAction().equals(ACTION_UPDATE_LAST_NAME)) {
				checkNickname(user.getHandle());
				updateAvatar();
			}
		}
	};

	private final Observer<MegaChatCall> callStatusObserver = call -> {
		int callStatus = call.getStatus();
		switch (callStatus) {
			case MegaChatCall.CALL_STATUS_CONNECTING:
			case MegaChatCall.CALL_STATUS_IN_PROGRESS:
			case MegaChatCall.CALL_STATUS_DESTROYED:
			case MegaChatCall.CALL_STATUS_TERMINATING_USER_PARTICIPATION:
			case MegaChatCall.CALL_STATUS_USER_NO_PRESENT:
				checkScreenRotationToShowCall();
				if(call.getStatus() == MegaChatCall.CALL_STATUS_TERMINATING_USER_PARTICIPATION &&
						call.getTermCode() == MegaChatCall.TERM_CODE_TOO_MANY_PARTICIPANTS){
					showSnackbar(SNACKBAR_TYPE, StringResourcesUtils.getString(R.string.call_error_too_many_participants), MEGACHAT_INVALID_HANDLE);
				}
				break;
		}
	};

	private final Observer<MegaChatCall> callOnHoldObserver = call -> checkScreenRotationToShowCall();

	private final Observer<Pair> sessionOnHoldObserver = sessionAndCall -> checkScreenRotationToShowCall();

	private BroadcastReceiver chatRoomMuteUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null || intent.getAction() == null ||
					!intent.getAction().equals(ACTION_UPDATE_PUSH_NOTIFICATION_SETTING))
				return;

			checkSpecificChatNotifications(chatHandle, notificationsSwitch, notificationsSubTitle);
		}
	};

	private BroadcastReceiver retentionTimeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null || intent.getAction() == null ||
					!intent.getAction().equals(ACTION_UPDATE_RETENTION_TIME))
				return;

			long seconds = intent.getLongExtra(RETENTION_TIME, DISABLED_RETENTION_TIME);
			updateRetentionTimeLayout(retentionTimeText, seconds);
		}
	};

	private BroadcastReceiver destroyActionModeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null || intent.getAction() == null
					|| !intent.getAction().equals(BROADCAST_ACTION_DESTROY_ACTION_MODE))
				return;

			if (sharedFoldersFragment != null && sharedFoldersFragment.isVisible()) {
				sharedFoldersFragment.clearSelections();
				sharedFoldersFragment.hideMultipleSelect();
			}
		}
	};

	private void setAppBarOffset(int offsetPx){
		if (callInProgressLayout != null && callInProgressLayout.getVisibility() == View.VISIBLE) {
			changeToolbarLayoutElevation();
		} else {
			CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
			AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
			assert behavior != null;
			behavior.onNestedPreScroll(fragmentContainer, appBarLayout, null, 0, offsetPx, new int[]{0, 0});
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		contactInfoActivityLollipop = this;

		if(shouldRefreshSessionDueToSDK() || shouldRefreshSessionDueToKarere()){
			return;
		}

		if (savedInstanceState != null) {
			megaAttacher.restoreState(savedInstanceState);
			nodeSaver.restoreState(savedInstanceState);
		}

        // State icon resource id default value.
        contactStateIcon = Util.isDarkMode(this) ? R.drawable.ic_offline_dark_standard
                : R.drawable.ic_offline_light;

		megaChatApi.addChatListener(this);

		chatC = new ChatController(this);
		cC = new ContactController(this);
		nC = new NodeController(this);
        megaApi.addGlobalListener(this);
		display = getWindowManager().getDefaultDisplay();
		outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);
		density = getResources().getDisplayMetrics().density;

		scaleW = getScaleW(outMetrics, density);
		scaleH = getScaleH(outMetrics, density);

		askForDisplayOverDialog = new AskForDisplayOverDialog(this);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {

			setContentView(R.layout.activity_chat_contact_properties);
            fragmentContainer = findViewById(R.id.fragment_container);
			toolbar = findViewById(R.id.toolbar);
			appBarLayout = findViewById(R.id.app_bar);
			setSupportActionBar(toolbar);
			aB = getSupportActionBar();

			imageLayout = findViewById(R.id.image_layout);

			collapsingToolbar = findViewById(R.id.collapse_toolbar);

			/*TITLE*/
			firstLineTextToolbar = findViewById(R.id.first_line_toolbar);

			/*SUBTITLE*/
			secondLineTextToolbar = findViewById(R.id.second_line_toolbar);

			nameText = findViewById(R.id.chat_contact_properties_name_text);
			emailText = findViewById(R.id.chat_contact_properties_email_text);
			setNicknameText = findViewById(R.id.chat_contact_properties_nickname);
			setNicknameText.setOnClickListener(this);

			int width;
			if(isScreenInPortrait(this)){
				width = dp2px(MAX_WIDTH_APPBAR_PORT, outMetrics);
				secondLineTextToolbar.setPadding(0,0,0,11);
			}else{
				width = dp2px(MAX_WIDTH_APPBAR_LAND, outMetrics);
				secondLineTextToolbar.setPadding(0,0,0,5);
			}
			nameText.setMaxWidthEmojis(width);
			secondLineTextToolbar.setMaxWidth(width);

			// left margin 72dp + right margin 36dp
			firstLineTextMaxWidthExpanded = outMetrics.widthPixels - dp2px(108, outMetrics);
			firstLineTextMaxWidthCollapsed = width;
			firstLineTextToolbar.setMaxWidthEmojis(firstLineTextMaxWidthExpanded);
			contactStateIconPaddingLeft = dp2px(8, outMetrics);

			setTitle(null);
			aB.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
			aB.setHomeButtonEnabled(true);
			aB.setDisplayHomeAsUpEnabled(true);

			contactPropertiesImage = findViewById(R.id.toolbar_image);

			dbH = DatabaseHandler.getDbHandler(getApplicationContext());

			appBarLayout.post(new Runnable() {
				@Override
				public void run() {
					setAppBarOffset(50);
				}
			});

			callInProgressLayout = findViewById(R.id.call_in_progress_layout);
			callInProgressLayout.setOnClickListener(this);
			callInProgressChrono = findViewById(R.id.call_in_progress_chrono);
			callInProgressText = findViewById(R.id.call_in_progress_text);
			microOffLayout = findViewById(R.id.micro_off_layout);
			videoOnLayout = findViewById(R.id.video_on_layout);
			callInProgressLayout.setVisibility(View.GONE);

			//OPTIONS LAYOUT
			optionsLayout = findViewById(R.id.chat_contact_properties_options);

			//CHAT OPTIONS
			chatOptionsLayout = findViewById(R.id.chat_contact_properties_chat_options_layout);
			dividerChatOptionsLayout = findViewById(R.id.divider_chat_options_layout);
			sendMessageLayout = findViewById(R.id.chat_contact_properties_chat_send_message_layout);
			sendMessageLayout.setOnClickListener(this);
			audioCallLayout = findViewById(R.id.chat_contact_properties_chat_call_layout);
			audioCallLayout.setOnClickListener(this);
			videoCallLayout = findViewById(R.id.chat_contact_properties_chat_video_layout);
			videoCallLayout.setOnClickListener(this);

			//Notifications Layout
			notificationsLayout = findViewById(R.id.chat_contact_properties_notifications_layout);
			notificationsLayout.setVisibility(View.VISIBLE);
			notificationsTitle = findViewById(R.id.chat_contact_properties_notifications_text);
			notificationsSubTitle = findViewById(R.id.chat_contact_properties_notifications_muted_text);
			notificationsSubTitle.setVisibility(View.GONE);
			notificationsSwitchLayout = findViewById(R.id.chat_contact_properties_layout);
			notificationsSwitchLayout.setOnClickListener(this);
			notificationsSwitch = findViewById(R.id.chat_contact_properties_switch);
			notificationsSwitch.setClickable(false);

			dividerNotificationsLayout = findViewById(R.id.divider_notifications_layout);

			//Verify credentials layout
			verifyCredentialsLayout = findViewById(R.id.chat_contact_properties_verify_credentials_layout);
			verifyCredentialsLayout.setOnClickListener(this);
			verifiedText = findViewById(R.id.chat_contact_properties_verify_credentials_info);
			verifiedImage = findViewById(R.id.chat_contact_properties_verify_credentials_info_icon);

			//Shared folders layout
			sharedFoldersLayout = findViewById(R.id.chat_contact_properties_shared_folders_layout);
			sharedFoldersLayout.setOnClickListener(this);

			sharedFoldersText = findViewById(R.id.chat_contact_properties_shared_folders_label);

			sharedFoldersButton = findViewById(R.id.chat_contact_properties_shared_folders_button);
			sharedFoldersButton.setOnClickListener(this);

			dividerSharedFoldersLayout = findViewById(R.id.divider_shared_folder_layout);

			//Share Contact Layout

			shareContactLayout = findViewById(R.id.chat_contact_properties_share_contact_layout);
			shareContactLayout.setOnClickListener(this);

			dividerShareContactLayout = findViewById(R.id.divider_share_contact_layout);

			//Chat Shared Files Layout

			sharedFilesLayout = findViewById(R.id.chat_contact_properties_chat_files_shared_layout);
			sharedFilesLayout.setOnClickListener(this);

			dividerSharedFilesLayout = findViewById(R.id.divider_chat_files_shared_layout);

			//Clear chat Layout
			manageChatLayout = findViewById(R.id.manage_chat_history_contact_properties_layout);
			manageChatLayout.setOnClickListener(this);
			retentionTimeText = findViewById(R.id.manage_chat_history_contact_properties_subtitle);
			retentionTimeText.setVisibility(View.GONE);
			dividerClearChatLayout = findViewById(R.id.divider_clear_chat_layout);

			//Remove contact Layout
			removeContactChatLayout = findViewById(R.id.chat_contact_properties_remove_contact_layout);
			removeContactChatLayout.setOnClickListener(this);

			chatHandle = extras.getLong(HANDLE, MEGACHAT_INVALID_HANDLE);
			userEmailExtra = extras.getString(NAME);

			//isChatOpen is:
			//- True when the megaChatApi.openChatRoom() method has already been called from ChatActivityLollipop
			//  and the changes related to history clearing will be listened from there.
			//- False when it is necessary to call megaChatApi.openChatRoom() method to listen for changes related to history clearing.
			//  This will happen when ContactInfoActivityLollipop is opened from other parts of the app than the Chat room.
			isChatOpen = extras.getBoolean(ACTION_CHAT_OPEN, false);

			if (megaChatApi == null) {
				megaChatApi = MegaApplication.getInstance().getMegaChatApi();
			}
			if (chatHandle != -1) {
				logDebug("From chat!!");
				fromContacts = false;
				chat = megaChatApi.getChatRoom(chatHandle);

				long userHandle = chat.getPeerHandle(0);

				String userHandleEncoded = MegaApiAndroid.userHandleToBase64(userHandle);
				user = megaApi.getContact(userHandleEncoded);
				if (user != null) {
					checkNickname(user.getHandle());
				} else {
					String fullName = "";
					if (!isTextEmpty(getTitleChat(chat))) {
						fullName = getTitleChat(chat);
					} else if (userEmailExtra != null) {
						fullName = userEmailExtra;
					}
					withoutNickname(fullName);
				}
			} else {
				logDebug("From contacts!!");
				fromContacts = true;
				user = megaApi.getContact(userEmailExtra);
				if (user != null) {
					checkNickname(user.getHandle());
					chat = megaChatApi.getChatRoomByUser(user.getHandle());
				} else {
					withoutNickname(userEmailExtra);
					chat = null;
				}
			}

			updateUI();
			updateVerifyCredentialsLayout();
			checkScreenRotationToShowCall();

			if(isOnline(this)){
				logDebug("online -- network connection");
				setAvatar();

				if(user!=null){
					sharedFoldersLayout.setVisibility(View.VISIBLE);
					dividerSharedFoldersLayout.setVisibility(View.VISIBLE);

					ArrayList<MegaNode> nodes = megaApi.getInShares(user);
					setFoldersButtonText(nodes);
					emailText.setText(user.getEmail());

					shareContactLayout.setVisibility(View.VISIBLE);
					dividerShareContactLayout.setVisibility(View.VISIBLE);

					chatOptionsLayout.setVisibility(View.VISIBLE);
					dividerChatOptionsLayout.setVisibility(View.VISIBLE);
				}
				else{
					sharedFoldersLayout.setVisibility(View.GONE);
					dividerSharedFoldersLayout.setVisibility(View.GONE);
					chatOptionsLayout.setVisibility(View.GONE);
					dividerChatOptionsLayout.setVisibility(View.GONE);

					if (chat != null) {
						emailText.setText(user.getEmail());
					}

					shareContactLayout.setVisibility(View.VISIBLE);
					dividerShareContactLayout.setVisibility(View.VISIBLE);
				}
			}
			else{
				logDebug("OFFLINE -- NO network connection");
				if(chat!=null){
					String userEmail = chatC.getParticipantEmail(chat.getPeerHandle(0));
					setOfflineAvatar(userEmail);
					emailText.setText(user.getEmail());
				}
				sharedFoldersLayout.setVisibility(View.GONE);
				dividerSharedFoldersLayout.setVisibility(View.GONE);

				shareContactLayout.setVisibility(View.GONE);
				dividerShareContactLayout.setVisibility(View.GONE);

				chatOptionsLayout.setVisibility(View.VISIBLE);
				dividerChatOptionsLayout.setVisibility(View.VISIBLE);
			}

		} else {
			logWarning("Extras is NULL");
		}

        if(askForDisplayOverDialog != null) {
            askForDisplayOverDialog.showDialog();
        }

		registerReceiver(manageShareReceiver,
				new IntentFilter(BROADCAST_ACTION_INTENT_MANAGE_SHARE));

		registerReceiver(chatRoomMuteUpdateReceiver,
				new IntentFilter(ACTION_UPDATE_PUSH_NOTIFICATION_SETTING));

		registerReceiver(retentionTimeReceiver,
				new IntentFilter(ACTION_UPDATE_RETENTION_TIME));

		IntentFilter userNameUpdateFilter = new IntentFilter(BROADCAST_ACTION_INTENT_FILTER_CONTACT_UPDATE);
		userNameUpdateFilter.addAction(ACTION_UPDATE_NICKNAME);
		userNameUpdateFilter.addAction(ACTION_UPDATE_FIRST_NAME);
		userNameUpdateFilter.addAction(ACTION_UPDATE_LAST_NAME);
		registerReceiver(userNameReceiver, userNameUpdateFilter);

		LiveEventBus.get(EVENT_CALL_STATUS_CHANGE, MegaChatCall.class).observe(this, callStatusObserver);
		LiveEventBus.get(EVENT_CALL_ON_HOLD_CHANGE, MegaChatCall.class).observe(this, callOnHoldObserver);
		LiveEventBus.get(EVENT_SESSION_ON_HOLD_CHANGE, Pair.class).observe(this, sessionOnHoldObserver);

		registerReceiver(destroyActionModeReceiver,
				new IntentFilter(BROADCAST_ACTION_DESTROY_ACTION_MODE));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		megaAttacher.saveState(outState);
		nodeSaver.saveState(outState);
	}

	private void visibilityStateIcon() {
		if (megaChatApi == null) {
			firstLineTextToolbar.updateMaxWidthAndIconVisibility(firstLineTextMaxWidthCollapsed, false);
			return;
		}

		int userStatus = megaChatApi.getUserOnlineStatus(user.getHandle());
		if (stateToolbar == State.EXPANDED && (userStatus == MegaChatApi.STATUS_ONLINE
				|| userStatus == MegaChatApi.STATUS_AWAY
				|| userStatus == MegaChatApi.STATUS_BUSY
				|| userStatus == MegaChatApi.STATUS_OFFLINE)) {
			firstLineTextToolbar.setMaxLines(2);
			firstLineTextToolbar.setTrailingIcon(contactStateIcon, contactStateIconPaddingLeft);
			firstLineTextToolbar.updateMaxWidthAndIconVisibility(firstLineTextMaxWidthExpanded, true);
		} else {
			firstLineTextToolbar.setMaxLines(stateToolbar == State.EXPANDED ? 2 : 1);
			firstLineTextToolbar.updateMaxWidthAndIconVisibility(
					stateToolbar == State.EXPANDED ? firstLineTextMaxWidthExpanded
							: firstLineTextMaxWidthCollapsed, false);
		}
	}

	private void checkNickname(long contactHandle) {
		MegaContactDB contactDB = getContactDB(contactHandle);
		if (contactDB == null) return;

		String fullName = buildFullName(contactDB.getName(), contactDB.getLastName(), contactDB.getMail());
		String nicknameText = contactDB.getNickname();

		if (isTextEmpty(nicknameText)) {
			withoutNickname(fullName);
		} else {
			withNickname(fullName, nicknameText);
		}
	}

	private void withoutNickname(String name) {
		firstLineTextToolbar.setText(name);
		nameText.setVisibility(View.GONE);
		setNicknameText.setText(getString(R.string.add_nickname));
		setDefaultAvatar();
	}

	private void withNickname(String name, String nickname) {
		firstLineTextToolbar.setText(nickname);
		nameText.setText(name);
		nameText.setVisibility(View.VISIBLE);
		setNicknameText.setText(getString(R.string.edit_nickname));
		setDefaultAvatar();
	}


	private void setContactPresenceStatus(){
		logDebug("setContactPresenceStatus");
		if (megaChatApi != null){
			int userStatus = megaChatApi.getUserOnlineStatus(user.getHandle());
			contactStateIcon = getIconResourceIdByLocation(this,userStatus,StatusIconLocation.STANDARD);

			// Reset as default value.
			if(contactStateIcon == 0) {
                contactStateIcon = Util.isDarkMode(this) ? R.drawable.ic_offline_dark_standard
                        : R.drawable.ic_offline_light;
            }

			if(userStatus == MegaChatApi.STATUS_ONLINE){
				logDebug("This user is connected");
				secondLineTextToolbar.setVisibility(View.VISIBLE);
				secondLineTextToolbar.setText(getString(R.string.online_status));
			}else if(userStatus == MegaChatApi.STATUS_AWAY){
				logDebug("This user is away");
				secondLineTextToolbar.setVisibility(View.VISIBLE);
				secondLineTextToolbar.setText(getString(R.string.away_status));
			} else if(userStatus == MegaChatApi.STATUS_BUSY){
				logDebug("This user is busy");
				secondLineTextToolbar.setVisibility(View.VISIBLE);
				secondLineTextToolbar.setText(getString(R.string.busy_status));
			}
			else if(userStatus == MegaChatApi.STATUS_OFFLINE){
				logDebug("This user is offline");
				secondLineTextToolbar.setVisibility(View.VISIBLE);
				secondLineTextToolbar.setText(getString(R.string.offline_status));
			}
			else if(userStatus == MegaChatApi.STATUS_INVALID){
				logDebug("INVALID status: " + userStatus);
				secondLineTextToolbar.setVisibility(View.GONE);
			}
			else{
				logDebug("This user status is: " + userStatus);
				secondLineTextToolbar.setVisibility(View.GONE);
			}
		}
		visibilityStateIcon();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		logDebug("onCreateOptionsMenuLollipop");

		drawableDots = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_dots_vertical_white);
		drawableDots = drawableDots.mutate();
		drawableArrow = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_arrow_back_white);
		drawableArrow = drawableArrow.mutate();

		drawableShare = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_share);
		drawableShare = drawableShare.mutate();
		drawableSend = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_send_to_contact);
		drawableSend = drawableSend.mutate();

		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.contact_properties_action, menu);

		shareMenuItem = menu.findItem(R.id.cab_menu_share_folder);
		sendFileMenuItem = menu.findItem(R.id.cab_menu_send_file);

		returnCallMenuItem = menu.findItem(R.id.action_return_call);
		RelativeLayout rootView = (RelativeLayout) returnCallMenuItem.getActionView();
		layoutCallMenuItem = rootView.findViewById(R.id.layout_menu_call);
		chronometerMenuItem = rootView.findViewById(R.id.chrono_menu);
		chronometerMenuItem.setVisibility(View.GONE);

		rootView.setOnClickListener(v -> onOptionsItemSelected(returnCallMenuItem));
		setCallMenuItem(returnCallMenuItem, layoutCallMenuItem, chronometerMenuItem);

		sendFileMenuItem.setIcon(mutateIconSecondary(this, R.drawable.ic_send_to_contact, R.color.white));

		if(isOnline(this)){
			sendFileMenuItem.setVisible(fromContacts);
		} else {
			logDebug("Hide all - no network connection");
			shareMenuItem.setVisible(false);
			sendFileMenuItem.setVisible(false);
		}

		int statusBarColor = ColorUtils.getColorForElevation(this, getResources().getDimension(R.dimen.toolbar_elevation));
		if(isDarkMode(this)) {
			collapsingToolbar.setContentScrimColor(statusBarColor);
		}
        collapsingToolbar.setStatusBarScrimColor(statusBarColor);

		appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
			@Override
			public void onStateChanged(AppBarLayout appBarLayout, State state) {
				stateToolbar = state;
				if (stateToolbar == State.EXPANDED) {
					firstLineTextToolbar.setTextColor(ContextCompat.getColor(ContactInfoActivityLollipop.this, R.color.white_alpha_087));
					secondLineTextToolbar.setTextColor(ContextCompat.getColor(ContactInfoActivityLollipop.this, R.color.white_alpha_087));
					setColorFilterWhite();
					visibilityStateIcon();
				} else if (stateToolbar == State.COLLAPSED) {
					firstLineTextToolbar.setTextColor(ContextCompat.getColor(ContactInfoActivityLollipop.this, R.color.grey_087_white_087));
					secondLineTextToolbar.setTextColor(ColorUtils.getThemeColor(ContactInfoActivityLollipop.this, android.R.attr.textColorSecondary));
					setColorFilterBlack();
					visibilityStateIcon();
				}
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	void setColorFilterWhite () {
        int color = ContextCompat.getColor(this,R.color.white_alpha_087);
		drawableArrow.setColorFilter(color, PorterDuff.Mode.SRC_IN);
		getSupportActionBar().setHomeAsUpIndicator(drawableArrow);

		drawableDots.setColorFilter(color, PorterDuff.Mode.SRC_IN);
		toolbar.setOverflowIcon(drawableDots);

		if (shareMenuItem != null) {
			drawableShare.setColorFilter(color, PorterDuff.Mode.SRC_IN);
			shareMenuItem.setIcon(drawableShare);
		}
		if (sendFileMenuItem != null) {
			drawableSend.setColorFilter(color, PorterDuff.Mode.SRC_IN);
			sendFileMenuItem.setIcon(drawableSend);
		}
	}

	void setColorFilterBlack () {
        int color = ContextCompat.getColor(this,R.color.grey_087_white_087);
		drawableArrow.setColorFilter(color, PorterDuff.Mode.SRC_IN);
		getSupportActionBar().setHomeAsUpIndicator(drawableArrow);

		drawableDots.setColorFilter(color, PorterDuff.Mode.SRC_IN);
		toolbar.setOverflowIcon(drawableDots);

		if (shareMenuItem != null) {
			drawableShare.setColorFilter(color, PorterDuff.Mode.SRC_IN);
			shareMenuItem.setIcon(drawableShare);
		}
		if (sendFileMenuItem != null) {
			drawableSend.setColorFilter(color, PorterDuff.Mode.SRC_IN);
			sendFileMenuItem.setIcon(drawableSend);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		logDebug("onOptionsItemSelected");

		int id = item.getItemId();
		switch(id){
			case android.R.id.home:{
				finish();
				break;
			}
			case R.id.cab_menu_share_folder:{
				pickFolderToShare(user.getEmail());
				break;
			}
			case R.id.cab_menu_send_file:{

				if(!isOnline(this)){
					showSnackbar(SNACKBAR_TYPE, getString(R.string.error_server_connection_problem), -1);
					return true;
				}

				sendFileToChat();
				break;
			}
			case R.id.action_return_call:
				if (checkPermissionsCall(this, INVALID_TYPE_PERMISSIONS)) {
					returnActiveCall(this);
				}
				return true;
		}
		return true;
	}

	public void sendFileToChat(){
		logDebug("sendFileToChat");

		if (app.getStorageState() == STORAGE_STATE_PAYWALL) {
			showOverDiskQuotaPaywallWarning();
			return;
		}

		if(user==null){
			logWarning("Selected contact NULL");
			return;
		}
		List<MegaUser> userList = new ArrayList<MegaUser>();
		userList.add(user);

		Intent intent = ContactController.getPickFileToSendIntent(this, userList);
		startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
	}

	public void sendMessageToChat(){
		logDebug("sendMessageToChat");

		if (app.getStorageState() == STORAGE_STATE_PAYWALL) {
			showOverDiskQuotaPaywallWarning();
			return;
		}

		if(user!=null){
			MegaChatRoom chat = megaChatApi.getChatRoomByUser(user.getHandle());
			if(chat==null){
				logDebug("No chat, create it!");
				MegaChatPeerList peers = MegaChatPeerList.createInstance();
				peers.addPeer(user.getHandle(), MegaChatPeerList.PRIV_STANDARD);
				megaChatApi.createChat(false, peers, this);
			}
			else{
				logDebug("There is already a chat, open it!");
				if(fromContacts){
					Intent intentOpenChat = new Intent(this, ChatActivityLollipop.class);
					intentOpenChat.setAction(ACTION_CHAT_SHOW_MESSAGES);
					intentOpenChat.putExtra(CHAT_ID, chat.getChatId());
					this.startActivity(intentOpenChat);
				}
				else{
					Intent intentOpenChat = new Intent(this, ChatActivityLollipop.class);
					intentOpenChat.setAction(ACTION_CHAT_SHOW_MESSAGES);
					intentOpenChat.putExtra(CHAT_ID, chat.getChatId());
					intentOpenChat.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					this.startActivity(intentOpenChat);
				}

				finish();
			}
		}
	}

	public void startCall() {
		MegaChatRoom chatRoomTo = megaChatApi.getChatRoomByUser(user.getHandle());
		if (chatRoomTo != null) {
			logDebug("Chat exists");
			if (megaChatApi.getChatCall(chatRoomTo.getChatId()) != null) {
				logDebug("There is a call, open it");
				openMeetingInProgress(this, chatRoomTo.getChatId(), true);
			} else if (isStatusConnected(this, chatRoomTo.getChatId())) {
				logDebug("There is no call, start it");
				startCallWithChatOnline(chatRoomTo);
			}
		} else {
			logDebug("Chat doesn't exist");
			//Create first the chat
			ArrayList<MegaChatRoom> chats = new ArrayList<>();
			ArrayList<MegaUser> usersNoChat = new ArrayList<>();
			usersNoChat.add(user);
			CreateChatListener listener;

			if (startVideo) {
				listener = new CreateChatListener(CreateChatListener.START_VIDEO_CALL, chats,
						usersNoChat, this, this);
			} else {
				listener = new CreateChatListener(CreateChatListener.START_AUDIO_CALL, chats,
						usersNoChat, this, this);
			}

			MegaChatPeerList peers = MegaChatPeerList.createInstance();
			peers.addPeer(user.getHandle(), MegaChatPeerList.PRIV_STANDARD);
			megaChatApi.createChat(false, peers, listener);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case REQUEST_CAMERA:
			case REQUEST_RECORD_AUDIO:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
						checkPermissionsCall(this, INVALID_TYPE_PERMISSIONS)) {
					startCall();
				}
				break;
		}

		nodeSaver.handleRequestPermissionsResult(requestCode);
	}

	public void pickFolderToShare(String email){
		logDebug("pickFolderToShare");
		if (email != null){
			Intent intent = new Intent(this, FileExplorerActivityLollipop.class);
			intent.setAction(FileExplorerActivityLollipop.ACTION_SELECT_FOLDER_TO_SHARE);
			ArrayList<String> contacts = new ArrayList<String>();
			contacts.add(email);
			intent.putExtra(SELECTED_CONTACTS, contacts);
			startActivityForResult(intent, REQUEST_CODE_SELECT_FOLDER);
		}
		else{
			showSnackbar(SNACKBAR_TYPE, getString(R.string.error_sharing_folder), -1);
			logWarning("Error sharing folder");
		}
	}

	public void setAvatar() {
		logDebug("setAvatar");
		if(user == null)
			return;

		File avatar = buildAvatarFile(this,user.getEmail() + ".jpg");
		if (isFileAvailable(avatar)) {
			setProfileAvatar(avatar);
		}
	}

	public void setOfflineAvatar(String email) {
		logDebug("setOfflineAvatar");
		File avatar = buildAvatarFile(this, email + ".jpg");

        if (isFileAvailable(avatar)) {
            Bitmap imBitmap = null;
            if (avatar.length() > 0) {
                BitmapFactory.Options bOpts = new BitmapFactory.Options();
                imBitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(),bOpts);
                if (imBitmap != null) {
                    contactPropertiesImage.setImageBitmap(imBitmap);

                    if (imBitmap != null && !imBitmap.isRecycled()) {
                        int colorBackground = getDominantColor(imBitmap);
                        imageLayout.setBackgroundColor(colorBackground);
                    }
                }
            }
        }
	}

	public void setProfileAvatar(File avatar) {
		logDebug("setProfileAvatar");
		Bitmap imBitmap = null;
		if (avatar.exists()) {
			if (avatar.length() > 0) {
				BitmapFactory.Options bOpts = new BitmapFactory.Options();
				imBitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(), bOpts);
				if (imBitmap == null) {
					avatar.delete();
                    megaApi.getUserAvatar(user,buildAvatarFile(this, user.getEmail()).getAbsolutePath(), this);
                } else {
					contactPropertiesImage.setImageBitmap(imBitmap);

					if (imBitmap != null && !imBitmap.isRecycled()) {
						int colorBackground = getDominantColor(imBitmap);
						imageLayout.setBackgroundColor(colorBackground);
					}
				}
			}
		}
	}

	private void setDefaultAvatar() {
		logDebug("setDefaultAvatar");
		Bitmap defaultAvatar = Bitmap.createBitmap(outMetrics.widthPixels, outMetrics.widthPixels, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(defaultAvatar);
		Paint p = new Paint();
		p.setAntiAlias(true);
		p.setColor(Color.TRANSPARENT);
		c.drawPaint(p);

		imageLayout.setBackgroundColor(getColorAvatar(user));
		contactPropertiesImage.setImageBitmap(defaultAvatar);
	}

	private void startingACall(boolean withVideo) {

		if (app.getStorageState() == STORAGE_STATE_PAYWALL) {
			showOverDiskQuotaPaywallWarning();
			return;
		}

		startVideo = withVideo;
		if (checkPermissionsCall(this, INVALID_TYPE_PERMISSIONS)) {
			startCall();
		}
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
			case R.id.manage_chat_history_contact_properties_layout:
				Intent intentManageChat = new Intent(this, ManageChatHistoryActivity.class);
				intentManageChat.putExtra(EMAIL, user.getEmail());
				intentManageChat.putExtra(CHAT_ID, MEGACHAT_INVALID_HANDLE);
				intentManageChat.putExtra(IS_FROM_CONTACTS, fromContacts);
				startActivity(intentManageChat);
				break;

			case R.id.chat_contact_properties_remove_contact_layout: {
				logDebug("Remove contact chat option");

				if(user!=null){
					showConfirmationRemoveContact(user);
				}
				break;
			}
			case R.id.chat_contact_properties_chat_send_message_layout:{
				logDebug("Send message option");
				if(!checkConnection(this)) return;
				sendMessageToChat();
				break;
			}

			case R.id.chat_contact_properties_chat_video_layout:
			case R.id.chat_contact_properties_chat_call_layout:
				if (isCallOptionEnabled()) {
					startingACall(v.getId() == R.id.chat_contact_properties_chat_video_layout);
				} else {
					showSnackbar(SNACKBAR_TYPE, getString(R.string.not_allowed_to_start_call), MEGACHAT_INVALID_HANDLE);
				}
				break;

			case R.id.chat_contact_properties_share_contact_layout: {
				logDebug("Share contact option");

				if (app.getStorageState() == STORAGE_STATE_PAYWALL) {
					showOverDiskQuotaPaywallWarning();
					return;
				}

				if(user==null){
					logDebug("Selected contact NULL");
					return;
				}

				Intent intent = ChatController.getSelectChatsToAttachContactIntent(this, user);
				startActivityForResult(intent, REQUEST_CODE_SELECT_CHAT);
				break;
			}
			case R.id.chat_contact_properties_shared_folders_button:
			case R.id.chat_contact_properties_shared_folders_layout:{
				sharedFolderClicked();
				break;
			}
			case R.id.chat_contact_properties_layout:
				chatNotificationsClicked();
				break;

			case R.id.chat_contact_properties_chat_files_shared_layout:{
				Intent nodeHistoryIntent = new Intent(this, NodeAttachmentHistoryActivity.class);
				if(chat!=null){
					nodeHistoryIntent.putExtra("chatId", chat.getChatId());
				}
				startActivity(nodeHistoryIntent);
				break;
			}
			case R.id.chat_contact_properties_nickname: {
				if (setNicknameText.getText().toString().equals(getString(R.string.add_nickname))) {
					showConfirmationSetNickname(null);
				} else if (user != null && !isBottomSheetDialogShown(contactNicknameBottomSheetDialogFragment)) {
					contactNicknameBottomSheetDialogFragment = new ContactNicknameBottomSheetDialogFragment();
					contactNicknameBottomSheetDialogFragment.show(getSupportFragmentManager(), contactNicknameBottomSheetDialogFragment.getTag());
				}
				break;
			}
			case R.id.chat_contact_properties_verify_credentials_layout:
				Intent intent = new Intent(this, AuthenticityCredentialsActivity.class);
				intent.putExtra(EMAIL, user.getEmail());
				startActivity(intent);
				break;

			case R.id.call_in_progress_layout:
				if(checkPermissionsCall(this, INVALID_TYPE_PERMISSIONS)){
					returnActiveCall(this);
				}
				break;
		}
	}

	public void showConfirmationSetNickname(final String alias) {
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(scaleWidthPx(20, outMetrics), scaleHeightPx(16, outMetrics), scaleWidthPx(17, outMetrics), 0);
		final EmojiEditText input = new EmojiEditText(this);
		layout.addView(input, params);
		input.setSingleLine();
		input.setSelectAllOnFocus(true);
		input.requestFocus();
		input.setTextColor(ColorUtils.getThemeColor(this, android.R.attr.textColorSecondary));
		input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		input.setEmojiSize(dp2px(EMOJI_SIZE, outMetrics));
		input.setImeOptions(EditorInfo.IME_ACTION_DONE);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		showKeyboardDelayed(input);

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);

		input.setImeActionLabel(getString(R.string.add_nickname), EditorInfo.IME_ACTION_DONE);
		if (alias == null) {
			input.setHint(getString(R.string.nickname_title));
			builder.setTitle(getString(R.string.add_nickname));
		} else {
			input.setHint(alias);
			input.setText(alias);
			input.setSelection(input.length());
			builder.setTitle(getString(R.string.edit_nickname));
		}
		int colorDisableButton = ContextCompat.getColor(this, R.color.teal_300_038_teal_200_038);
		int colorEnableButton = ContextCompat.getColor(this, R.color.teal_300_teal_200);

		input.addTextChangedListener(new TextWatcher() {
			private void handleText() {
				if (setNicknameDialog != null) {
					final Button okButton = setNicknameDialog.getButton(AlertDialog.BUTTON_POSITIVE);
					if (input.getText().length() == 0) {
						okButton.setEnabled(false);
						okButton.setTextColor(colorDisableButton);
					} else {
						okButton.setEnabled(true);
						okButton.setTextColor(colorEnableButton);
					}
				}
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				handleText();
			}
		});
		builder.setPositiveButton(getString(R.string.button_set),
				(dialog, whichButton) -> onClickAlertDialog(input, alias));
		builder.setNegativeButton(getString(R.string.general_cancel),
				(dialog, whichButton) -> setNicknameDialog.dismiss());

		builder.setView(layout);
		setNicknameDialog = builder.create();
		setNicknameDialog.show();
		setNicknameDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
		setNicknameDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(colorDisableButton);
		setNicknameDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> onClickAlertDialog(input, alias));
		setNicknameDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> setNicknameDialog.dismiss());

	}

	private void onClickAlertDialog(EmojiEditText input, String alias) {
		String name = input.getText().toString();
		if (isTextEmpty(name)) {
			logWarning("Input is empty");
			input.setError(getString(R.string.invalid_string));
			input.requestFocus();
		} else {
			addNickname(alias, name);
			setNicknameDialog.dismiss();
		}
	}

	public void addNickname(String oldNickname, String newNickname) {
		if (oldNickname != null && oldNickname.equals(newNickname)) return;
		//Update the new nickname
		megaApi.setUserAlias(user.getHandle(), newNickname, new SetAttrUserListener(this));
	}

	private void updateAvatar(){
		if(isOnline(this)){
			setAvatar();
		}else if (chat != null){
			setOfflineAvatar(chatC.getParticipantEmail(chat.getPeerHandle(0)));
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

		logDebug("resultCode: " + resultCode);

		if (megaAttacher.handleActivityResult(requestCode, resultCode, intent, this)) {
			return;
		}

		if (nodeSaver.handleActivityResult(requestCode, resultCode, intent)) {
			return;
		}

		if (requestCode == REQUEST_CODE_SELECT_FOLDER && resultCode == RESULT_OK) {

			if (!isOnline(this)) {
				showSnackbar(SNACKBAR_TYPE, getString(R.string.error_server_connection_problem), -1);
				return;
			}

			final ArrayList<String> selectedContacts = intent.getStringArrayListExtra(SELECTED_CONTACTS);
			final long folderHandle = intent.getLongExtra("SELECT", 0);

			final MegaNode parent = megaApi.getNodeByHandle(folderHandle);

			if (parent.isFolder()){
				MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
				dialogBuilder.setTitle(getString(R.string.file_properties_shared_folder_permissions));
				final CharSequence[] items = {getString(R.string.file_properties_shared_folder_read_only), getString(R.string.file_properties_shared_folder_read_write), getString(R.string.file_properties_shared_folder_full_access)};
				dialogBuilder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						statusDialog = createProgressDialog(contactInfoActivityLollipop, StringResourcesUtils.getString(R.string.context_sharing_folder));
						permissionsDialog.dismiss();
						nC.shareFolder(parent, selectedContacts, item);
					}
				});
				permissionsDialog = dialogBuilder.create();
				permissionsDialog.show();
			}
        }
		else if (requestCode == REQUEST_CODE_SELECT_FILE && resultCode == RESULT_OK) {
			logDebug("requestCode == REQUEST_CODE_SELECT_FILE");
			if (intent == null) {
				logWarning("Return.....");
				return;
			}

			megaAttacher.handleSelectFileResult(intent, user, this);
		} else if (requestCode == REQUEST_CODE_SELECT_FOLDER_TO_COPY && resultCode == RESULT_OK) {
            if (!isOnline(this)) {
                showSnackbar(SNACKBAR_TYPE, getString(R.string.error_server_connection_problem), -1);
                return;
            }

            statusDialog = createProgressDialog(this, StringResourcesUtils.getString(R.string.context_copying));
            
            final long[] copyHandles = intent.getLongArrayExtra("COPY_HANDLES");
            final long toHandle = intent.getLongExtra("COPY_TO", 0);
            final int totalCopy = copyHandles.length;
            
            MegaNode parent = megaApi.getNodeByHandle(toHandle);
            for (int i = 0; i < copyHandles.length; i++) {
				logDebug("NODE TO COPY: " + megaApi.getNodeByHandle(copyHandles[i]).getName());
				logDebug("WHERE: " + parent.getName());
				logDebug("NODES: " + copyHandles[i] + "_" + parent.getHandle());
                MegaNode cN = megaApi.getNodeByHandle(copyHandles[i]);
                if (cN != null){
					logDebug("cN != null");
                    megaApi.copyNode(cN, parent, this);
                }
                else{
					logWarning("cN == null");
                    try {
                        statusDialog.dismiss();
                        if(sharedFoldersFragment!=null && sharedFoldersFragment.isVisible()){
                            showSnackbar(SNACKBAR_TYPE, getString(R.string.context_no_sent_node), -1);
                        }
                    } catch (Exception ex) {
                    }
                }
            }
        }

		super.onActivityResult(requestCode, resultCode, intent);
	}

	public void showConfirmationRemoveContact(final MegaUser c){
		logDebug("showConfirmationRemoveContact");
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						cC.removeContact(c);
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						break;
				}
			}
		};

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		String title = getResources().getQuantityString(R.plurals.title_confirmation_remove_contact, 1);
		builder.setTitle(title);
		String message= getResources().getQuantityString(R.plurals.confirmation_remove_contact, 1);
		builder.setMessage(message).setPositiveButton(R.string.general_remove, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();
	}

	@Override
	public void onRequestStart(MegaApiJava api, MegaRequest request) {
		logDebug("onRequestStart: " + request.getName());
	}

	@SuppressLint("NewApi")
	@Override
	public void onRequestFinish(MegaApiJava api, MegaRequest request, MegaError e) {
		logDebug("onRequestFinish: " + request.getType() + "__" + request.getRequestString());
		if (request.getType() == MegaRequest.TYPE_GET_ATTR_USER) {
			logDebug("MegaRequest.TYPE_GET_ATTR_USER");
			if (e.getErrorCode() == MegaError.API_OK) {
				File avatar = buildAvatarFile(this, request.getEmail() + ".jpg");
				Bitmap imBitmap = null;
				if (isFileAvailable(avatar)) {
					if (avatar.length() > 0) {
						BitmapFactory.Options bOpts = new BitmapFactory.Options();
						imBitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(), bOpts);
						if (imBitmap == null) {
							avatar.delete();
						} else {
							contactPropertiesImage.setImageBitmap(imBitmap);

							if (imBitmap != null && !imBitmap.isRecycled()) {
								Palette palette = Palette.from(imBitmap).generate();
								Palette.Swatch swatch = palette.getDarkVibrantSwatch();
								imageLayout.setBackgroundColor(swatch.getBodyTextColor());
							}
						}
					}
				}
			}
		} else if (request.getType() == MegaRequest.TYPE_CREATE_FOLDER){
            try {
                statusDialog.dismiss();
            }
            catch (Exception ex) {}
            
            if (e.getErrorCode() == MegaError.API_OK){
                if(sharedFoldersFragment!=null && sharedFoldersFragment.isVisible()){
                    showSnackbar(SNACKBAR_TYPE, getString(R.string.context_folder_created), -1);
                    sharedFoldersFragment.setNodes();
                }
            }
            else{
                if(sharedFoldersFragment!=null && sharedFoldersFragment.isVisible()){
                    showSnackbar(SNACKBAR_TYPE, getString(R.string.context_folder_no_created), -1);
                    sharedFoldersFragment.setNodes();
                }
            }
        } else if (request.getType() == MegaRequest.TYPE_COPY) {
            try {
                statusDialog.dismiss();
            } catch (Exception ex) {
            }
            
            if (e.getErrorCode() == MegaError.API_OK){
                if(sharedFoldersFragment!=null && sharedFoldersFragment.isVisible()){
                    sharedFoldersFragment.clearSelections();
                    sharedFoldersFragment.hideMultipleSelect();
                    showSnackbar(SNACKBAR_TYPE, getString(R.string.context_correctly_copied), -1);
                }
            }
            else{
                if(e.getErrorCode()==MegaError.API_EOVERQUOTA){
					if (api.isForeignNode(request.getParentHandle())) {
						showForeignStorageOverQuotaWarningDialog(this);
						return;
					}

					logWarning("OVERQUOTA ERROR: " + e.getErrorCode());
                    Intent intent = new Intent(this, ManagerActivityLollipop.class);
                    intent.setAction(ACTION_OVERQUOTA_STORAGE);
                    startActivity(intent);
                    finish();
                }
                else if(e.getErrorCode()==MegaError.API_EGOINGOVERQUOTA){
					logDebug("PRE OVERQUOTA ERROR: " + e.getErrorCode());
                    Intent intent = new Intent(this, ManagerActivityLollipop.class);
                    intent.setAction(ACTION_PRE_OVERQUOTA_STORAGE);
                    startActivity(intent);
                    finish();
                }
                else{
                    if(sharedFoldersFragment!=null && sharedFoldersFragment.isVisible()){
                        sharedFoldersFragment.clearSelections();
                        sharedFoldersFragment.hideMultipleSelect();
                        showSnackbar(SNACKBAR_TYPE, getString(R.string.context_no_copied), -1);
                    }
                }
            }

			logDebug("Copy nodes request finished");
        }
        else if (request.getType() == MegaRequest.TYPE_MOVE) {
			try {
				statusDialog.dismiss();
			} catch (Exception ex) {
			}

			if (sharedFoldersFragment != null && sharedFoldersFragment.isVisible()) {
				sharedFoldersFragment.clearSelections();
				sharedFoldersFragment.hideMultipleSelect();
			}

			if (e.getErrorCode() == MegaError.API_EOVERQUOTA && api.isForeignNode(request.getParentHandle())) {
				showForeignStorageOverQuotaWarningDialog(this);
			} else if (moveToRubbish) {
				logDebug("Finish move to Rubbish!");
				if (e.getErrorCode() == MegaError.API_OK) {
					showSnackbar(SNACKBAR_TYPE, getString(R.string.context_correctly_moved_to_rubbish), -1);
				} else {
					showSnackbar(SNACKBAR_TYPE, getString(R.string.context_no_moved), -1);
				}
			} else {
				if (e.getErrorCode() == MegaError.API_OK) {
					showSnackbar(SNACKBAR_TYPE, getString(R.string.context_correctly_moved), -1);
				} else {
					showSnackbar(SNACKBAR_TYPE, getString(R.string.context_no_moved), -1);
				}
			}
			moveToRubbish = false;
			logDebug("Move request finished");
		} else if(request.getType() == MegaRequest.TYPE_REMOVE_CONTACT){
			logDebug("Contact removed");
			finish();
		}
	}

	@Override
	public void onRequestTemporaryError(MegaApiJava api, MegaRequest request,
										MegaError e) {
		logWarning("onRequestTemporaryError: " + request.getName());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (drawableArrow != null) {
			drawableArrow.setColorFilter(null);
		}
		if (drawableDots != null) {
			drawableDots.setColorFilter(null);
		}
		if (drawableSend != null) {
			drawableSend.setColorFilter(null);
		}
		if (drawableShare != null) {
			drawableShare.setColorFilter(null);
		}
        if (askForDisplayOverDialog != null) {
            askForDisplayOverDialog.recycle();
        }

		unregisterReceiver(chatRoomMuteUpdateReceiver);
		unregisterReceiver(retentionTimeReceiver);
		unregisterReceiver(manageShareReceiver);
		unregisterReceiver(userNameReceiver);
		unregisterReceiver(destroyActionModeReceiver);

		nodeSaver.destroy();
	}

	@Override
	public void onRequestUpdate(MegaApiJava api, MegaRequest request) { }

	@Override
	protected void onResume() {
		super.onResume();

		updateVerifyCredentialsLayout();
		checkScreenRotationToShowCall();
		setContactPresenceStatus();
		requestLastGreen(-1);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

	}

	public void requestLastGreen(int state){
		logDebug("state: " + state);
		if(state == -1){
			state = megaChatApi.getUserOnlineStatus(user.getHandle());
		}

		if(state != MegaChatApi.STATUS_ONLINE && state != MegaChatApi.STATUS_BUSY && state != MegaChatApi.STATUS_INVALID){
			logDebug("Request last green for user");
			megaChatApi.requestLastGreen(user.getHandle(), this);
		}
	}

	@Override
	public void onRequestStart(MegaChatApiJava api, MegaChatRequest request) {

	}

	@Override
	public void onRequestUpdate(MegaChatApiJava api, MegaChatRequest request) {

	}

	@Override
	public void onRequestFinish(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {
		logDebug("onRequestFinish");
		if(request.getType() == MegaChatRequest.TYPE_CREATE_CHATROOM){
			if (e.getErrorCode() == MegaChatError.ERROR_OK) {
				logDebug("Chat created ---> open it!");

				Intent intent = new Intent(this, ChatActivityLollipop.class)
						.setAction(ACTION_CHAT_SHOW_MESSAGES)
						.putExtra(CHAT_ID, request.getChatHandle());

				if (!fromContacts) {
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				}

				this.startActivity(intent);
				finish();
			} else {
				logDebug("ERROR WHEN CREATING CHAT " + e.getErrorString());
				showSnackbar(SNACKBAR_TYPE, getString(R.string.create_chat_error), -1);
			}
		}
	}

	@Override
	public void onRequestTemporaryError(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {

	}

	private void sharedFolderClicked(){
        RelativeLayout sharedFolderLayout = (RelativeLayout)findViewById(R.id.shared_folder_list_container);
		if(isShareFolderExpanded){
			sharedFolderLayout.setVisibility(View.GONE);
			if (user != null) {
				setFoldersButtonText(megaApi.getInShares(user));
			}
		}
		else{
			sharedFolderLayout.setVisibility(View.VISIBLE);
			sharedFoldersButton.setText(R.string.general_close);
            if (sharedFoldersFragment == null){
                sharedFoldersFragment = new ContactSharedFolderFragment();
                sharedFoldersFragment.setUserEmail(user.getEmail());
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_shared_folders, sharedFoldersFragment, "sharedFoldersFragment").commitNow();
            }
		}
		isShareFolderExpanded = !isShareFolderExpanded;
	}

	/**
	 * Update UI elements if chat exists.
	 */
	private void updateUI() {
		if (chat == null || chat.getChatId() == MEGACHAT_INVALID_HANDLE) {
			sharedFilesLayout.setVisibility(View.GONE);
			dividerSharedFilesLayout.setVisibility(View.GONE);
			manageChatLayout.setVisibility(View.GONE);
			dividerClearChatLayout.setVisibility(View.GONE);
			retentionTimeText.setVisibility(View.GONE);
		} else {
			chatHandle = chat.getChatId();

			if (!isChatOpen) {
				MegaApplication.getChatManagement().openChatRoom(chat.getChatId());
			}

			updateRetentionTimeLayout(retentionTimeText, getUpdatedRetentionTimeFromAChat(chatHandle));

			if(isOnline(this)){
				manageChatLayout.setVisibility(View.VISIBLE);
				dividerClearChatLayout.setVisibility(View.VISIBLE);
			}else{
				manageChatLayout.setVisibility(View.GONE);
				dividerClearChatLayout.setVisibility(View.GONE);
			}

			sharedFilesLayout.setVisibility(View.VISIBLE);
			dividerSharedFilesLayout.setVisibility(View.VISIBLE);
		}

		checkSpecificChatNotifications(chatHandle, notificationsSwitch, notificationsSubTitle);
		notificationsLayout.setVisibility(View.VISIBLE);
		dividerNotificationsLayout.setVisibility(View.VISIBLE);
	}

	/**
	 * Method that makes the necessary updates when the chat has been created.
	 *
	 * @param newChats The created chats.
	 */
	private Unit chatsCreated(List<? extends MegaChatRoom> newChats) {
		if (newChats.isEmpty()) {
			return Unit.INSTANCE;
		}

		MegaChatRoom newChat = newChats.get(0);

		if (newChat != null && newChat.getChatId() != MEGACHAT_INVALID_HANDLE) {
			chat = newChat;
			updateUI();
			chatNotificationsClicked();
		}

		return Unit.INSTANCE;
	}

	/**
	 * Make the necessary actions when clicking on the Chat Notifications layout.
	 */
	private void chatNotificationsClicked() {
		if (chatHandle == MEGACHAT_INVALID_HANDLE) {
			logDebug("The chat doesn't exist, create it");
			ArrayList<MegaChatRoom> chats = new ArrayList<>();
			ArrayList<MegaUser> usersNoChat = new ArrayList<>();
			usersNoChat.add(user);
			CreateChatListener listener = new CreateChatListener(
					CreateChatListener.CONFIGURE_DND, chats, usersNoChat, this, this,
					this::chatsCreated);
			MegaChatPeerList peers = MegaChatPeerList.createInstance();
			peers.addPeer(user.getHandle(), MegaChatPeerList.PRIV_STANDARD);
			megaChatApi.createChat(false, peers, listener);
		} else {
			logDebug("The chat exists");
			if (notificationsSwitch.isChecked()) {
				createMuteNotificationsAlertDialogOfAChat(this, chatHandle);
			} else {
				MegaApplication.getPushNotificationSettingManagement().controlMuteNotificationsOfAChat(this, NOTIFICATIONS_ENABLED, chatHandle);
			}
		}
	}
    
    public void showOptionsPanel(MegaNode node){
		logDebug("showOptionsPanel");

        if (node == null || isBottomSheetDialogShown(bottomSheetDialogFragment)) return;

		selectedNode = node;
		bottomSheetDialogFragment = new ContactFileListBottomSheetDialogFragment();
		bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
    }
    
    public MegaNode getSelectedNode() {
        return selectedNode;
    }
    
    public void setSelectedNode(MegaNode selectedNode) {
        this.selectedNode = selectedNode;
    }

	public String getNickname() {
		return getNicknameContact(user.getHandle());
	}

	public void downloadFile(List<MegaNode> nodes) {
		nodeSaver.saveNodes(nodes, true, false, false, false);
	}
    
    public void leaveMultipleShares (ArrayList<Long> handleList){
        
        for (int i=0; i<handleList.size(); i++){
            MegaNode node = megaApi.getNodeByHandle(handleList.get(i));
            megaApi.remove(node);
        }
    }
    
    public void showMoveLollipop(ArrayList<Long> handleList){
        moveToRubbish=false;
        Intent intent = new Intent(this, FileExplorerActivityLollipop.class);
        intent.setAction(FileExplorerActivityLollipop.ACTION_PICK_MOVE_FOLDER);
        long[] longArray = new long[handleList.size()];
        for (int i=0; i<handleList.size(); i++){
            longArray[i] = handleList.get(i);
        }
        intent.putExtra("MOVE_FROM", longArray);
        startActivityForResult(intent, REQUEST_CODE_SELECT_FOLDER_TO_MOVE);
    }
    
    public void showCopyLollipop(ArrayList<Long> handleList) {
        
        Intent intent = new Intent(this, FileExplorerActivityLollipop.class);
        intent.setAction(FileExplorerActivityLollipop.ACTION_PICK_COPY_FOLDER);
        long[] longArray = new long[handleList.size()];
        for (int i = 0; i < handleList.size(); i++) {
            longArray[i] = handleList.get(i);
        }
        intent.putExtra("COPY_FROM", longArray);
        startActivityForResult(intent, REQUEST_CODE_SELECT_FOLDER_TO_COPY);
    }
    
    public void askConfirmationMoveToRubbish(final ArrayList<Long> handleList){
		logDebug("askConfirmationMoveToRubbish");
        
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        moveToTrash(handleList);
                        break;
                    
                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
        
        if(handleList!=null){
            
            if (handleList.size() > 0){
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
//				builder.setTitle(getResources().getString(R.string.section_rubbish_bin));
                if (handleList.size() > 1){
                    builder.setMessage(getResources().getString(R.string.confirmation_move_to_rubbish_plural));
                }
                else{
                    builder.setMessage(getResources().getString(R.string.confirmation_move_to_rubbish));
                }
                builder.setPositiveButton(R.string.general_move, dialogClickListener);
                builder.setNegativeButton(R.string.general_cancel, dialogClickListener);
                builder.show();
            }
        }
        else{
			logWarning("handleList NULL");
            return;
        }
    }
    
    public boolean isEmptyParentHandleStack() {
        if (sharedFoldersFragment != null) {
            return sharedFoldersFragment.isEmptyParentHandleStack();
        }
		logWarning("Fragment NULL");
        return true;
    }
    
    public void moveToTrash(final ArrayList<Long> handleList){
		logDebug("moveToTrash: ");
        moveToRubbish=true;
        if (!isOnline(this)) {
            showSnackbar(SNACKBAR_TYPE, getString(R.string.error_server_connection_problem), -1);
            return;
        }
        
        MultipleRequestListener moveMultipleListener = null;
        MegaNode parent;
        //Check if the node is not yet in the rubbish bin (if so, remove it)
        if(handleList!=null){
            if(handleList.size()>1){
				logDebug("MOVE multiple: " + handleList.size());
                moveMultipleListener = new MultipleRequestListener(MULTIPLE_SEND_RUBBISH, this);
                for (int i=0;i<handleList.size();i++){
                    megaApi.moveNode(megaApi.getNodeByHandle(handleList.get(i)), megaApi.getRubbishNode(), moveMultipleListener);
                }
            }
            else{
				logDebug("MOVE single");
                megaApi.moveNode(megaApi.getNodeByHandle(handleList.get(0)), megaApi.getRubbishNode(), this);
                
            }
        }
        else{
			logWarning("handleList NULL");
            return;
        }
    }

    public void setParentHandle(long parentHandle) {
        this.parentHandle = parentHandle;
    }
    
    private void setFoldersButtonText(ArrayList<MegaNode> nodes){
		if (nodes != null) {
			sharedFoldersButton.setText(getQuantityString(R.plurals.num_folders_with_parameter, nodes.size(), nodes.size()));
			if (nodes.size() == 0) {
				sharedFoldersButton.setClickable(false);
				sharedFoldersLayout.setClickable(false);
			}
		}
    }

	@Override
	public void onUsersUpdate(MegaApiJava api, ArrayList<MegaUser> users) {
		if (users != null && !users.isEmpty()) {
			for (MegaUser updatedUser : users) {
				if (updatedUser.getHandle() == user.getHandle()) {
					user = updatedUser;
					emailText.setText(user.getEmail());
					break;
				}
			}
		}
	}

	@Override
	public void onUserAlertsUpdate(MegaApiJava api, ArrayList<MegaUserAlert> userAlerts) {
		logDebug("onUserAlertsUpdate");
	}
    
    @Override
    public void onNodesUpdate(MegaApiJava api,ArrayList<MegaNode> nodeList) {
        if (sharedFoldersFragment != null){
            if (sharedFoldersFragment.isVisible()){
                sharedFoldersFragment.setNodes(parentHandle);
            }
        }
        ArrayList<MegaNode> nodes = megaApi.getInShares(user);
        setFoldersButtonText(nodes);
    }
    
    @Override
    public void onReloadNeeded(MegaApiJava api) {
    
    }
    
    @Override
    public void onAccountUpdate(MegaApiJava api) {
    
    }
    
    @Override
    public void onContactRequestsUpdate(MegaApiJava api,ArrayList<MegaContactRequest> requests) {
    
    }
    
    @Override
    public void onEvent(MegaApiJava api,MegaEvent event) {
    
    }

	@Override
	public void onChatListItemUpdate(MegaChatApiJava api, MegaChatListItem item) {

	}

	@Override
	public void onChatInitStateUpdate(MegaChatApiJava api, int newState) {

	}

	@Override
	public void onChatOnlineStatusUpdate(MegaChatApiJava api, long userhandle, int status, boolean inProgress) {
		logDebug("userhandle: " + userhandle + ", status: " + status + ", inProgress: " + inProgress);
		setContactPresenceStatus();
		requestLastGreen(status);
	}

	@Override
	public void onChatPresenceConfigUpdate(MegaChatApiJava api, MegaChatPresenceConfig config) {

	}

	@Override
	public void onChatConnectionStateUpdate(MegaChatApiJava api, long chatid, int newState) {
		MegaChatRoom chatRoom = api.getChatRoom(chatid);
		if (isChatConnectedInOrderToInitiateACall(newState, chatRoom)) {
			startCallWithChatOnline(api.getChatRoom(chatid));
		}
	}

	@Override
	public void onChatPresenceLastGreen(MegaChatApiJava api, long userhandle, int lastGreen) {
		if(userhandle == user.getHandle()){
			logDebug("Update last green");

			int state = megaChatApi.getUserOnlineStatus(user.getHandle());

			if(state != MegaChatApi.STATUS_ONLINE && state != MegaChatApi.STATUS_BUSY && state != MegaChatApi.STATUS_INVALID){
				String formattedDate = lastGreenDate(this, lastGreen);
				secondLineTextToolbar.setVisibility(View.VISIBLE);
				secondLineTextToolbar.setText(formattedDate);
				secondLineTextToolbar.isMarqueeIsNecessary(this);
				logDebug("Date last green: " + formattedDate);
			}
		}
	}

	private void startCallWithChatOnline(MegaChatRoom chatRoom) {
		addChecksForACall(chatRoom.getChatId(), startVideo);
		enableCallLayouts(false);
		megaChatApi.startChatCall(chatRoom.getChatId(), startVideo, true, new StartChatCallListener(this, this, this));
		MegaApplication.setIsWaitingForCall(false);
	}

	private void enableCallLayouts(Boolean enable) {
		videoCallLayout.setEnabled(enable);
		audioCallLayout.setEnabled(enable);
	}

	/**
	 * Updates the "Verify credentials" view.
	 */
	public void updateVerifyCredentialsLayout() {
		if (user != null) {
			verifyCredentialsLayout.setVisibility(View.VISIBLE);

			if (megaApi.areCredentialsVerified(user)) {
				verifiedText.setText(R.string.label_verified);
				verifiedImage.setVisibility(View.VISIBLE);
			} else {
				verifiedText.setText(R.string.label_not_verified);
				verifiedImage.setVisibility(View.GONE);
			}
		} else {
			verifyCredentialsLayout.setVisibility(View.GONE);
		}
	}

	/**
	 * This method is used to change the elevation of the toolbar.
	 */
	public void changeToolbarLayoutElevation() {
		appBarLayout.setElevation(callInProgressLayout.getVisibility() == View.VISIBLE ?
				dp2px(16, outMetrics) : 0);

		if (callInProgressLayout.getVisibility() == View.VISIBLE) {
			appBarLayout.setExpanded(false);
		}
	}

	/**
	 * Method to check the rotation of the screen to display the call properly.
	 */
	private void checkScreenRotationToShowCall() {
		if (isScreenInPortrait(ContactInfoActivityLollipop.this)) {
			setCallWidget();
		} else {
			supportInvalidateOptionsMenu();
		}
	}

	/**
	 * This method sets "Tap to return to call" banner when there is a call in progress.
	 */
	private void setCallWidget() {
		if (!isScreenInPortrait(this)) {
			hideCallWidget(this, callInProgressChrono, callInProgressLayout);
			return;
		}

		showCallLayout(this, callInProgressLayout, callInProgressChrono, callInProgressText);
	}

	@Override
	public void showSnackbar(int type, String content, long chatId) {
		showSnackbar(type, fragmentContainer, content, chatId);
	}

	@Override
	public void finishRenameActionWithSuccess(@NonNull String newName) {
		//No update needed
	}

	@Override
	public void actionConfirmed() {
		if (sharedFoldersFragment != null && sharedFoldersFragment.isVisible()) {
			sharedFoldersFragment.clearSelections();
			sharedFoldersFragment.hideMultipleSelect();
		}
	}

	@Override
	public void createFolder(@NotNull String folderName) {
		//No action needed
	}

	@Override
	public void onCallStarted(long chatId, boolean enableVideo, int enableAudio) {
		MegaChatRoom chatRoomTo = megaChatApi.getChatRoomByUser(user.getHandle());
		if (chatRoomTo != null && chatRoomTo.getChatId() == chatId) {
			openMeetingWithAudioOrVideo(this, chatId, enableAudio == START_CALL_AUDIO_ENABLE, enableVideo);
		}

		enableCallLayouts(true);
	}

	@Override
	public void onCallFailed(long chatId) {
		enableCallLayouts(true);
	}
}
