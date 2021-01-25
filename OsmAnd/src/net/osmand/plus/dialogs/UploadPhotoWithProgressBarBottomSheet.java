package net.osmand.plus.dialogs;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import net.osmand.plus.R;
import net.osmand.plus.UiUtilities;
import net.osmand.plus.UiUtilities.DialogButtonType;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.base.MenuBottomSheetDialogFragment;
import net.osmand.plus.base.bottomsheetmenu.BaseBottomSheetItem;
import net.osmand.plus.base.bottomsheetmenu.BottomSheetItemWithDescription;
import net.osmand.plus.widgets.TextViewEx;

public class UploadPhotoWithProgressBarBottomSheet extends MenuBottomSheetDialogFragment {

	public static final String TAG = UploadPhotoWithProgressBarBottomSheet.class.getSimpleName();

	private ProgressBar progressBar;

	@Override
	public void createMenuItems(Bundle savedInstanceState) {
		Context context = requireContext();
		LayoutInflater inflater = UiUtilities.getInflater(context, nightMode);
		View view = inflater.inflate(R.layout.bottom_sheet_with_progress_bar, null);
		progressBar = view.findViewById(R.id.progress_bar);

		BaseBottomSheetItem descriptionItem = new BottomSheetItemWithDescription.Builder()
				.setTitle(getString(R.string.upload_photo))
				.setCustomView(view)
				.create();
		items.add(descriptionItem);
	}

	protected void setTextId() {
		View progressBarContainer = LayoutInflater.from(getMapActivity()).inflate(R.layout.bottom_sheet_with_progress_bar, null);

		TextViewEx progressTitle = progressBarContainer.findViewById(R.id.title);
		progressTitle.setText(R.string.select_color);
	}

	public void setupProgress(int max) {
		progressBar.setMax(max);
	}

	public void onProgressUpdate(Integer... values) {
		progressBar.setProgress(values[0]);
	}

	public void onUploadingFinished() {
		progressBar.setProgress(1);
		setDismissButtonTextId(R.string.shared_string_close);
		setTextId();
	}

	@Override
	protected boolean useVerticalButtons() {
		return true;
	}

	@Override
	protected int getDismissButtonTextId() {
		return R.string.shared_string_cancel;
	}

	@Override
	protected DialogButtonType getRightBottomButtonType() {
		return DialogButtonType.PRIMARY;
	}

	@Override
	public int getSecondDividerHeight() {
		return getResources().getDimensionPixelSize(R.dimen.bottom_sheet_icon_margin);
	}

	@Override
	protected void onRightBottomButtonClick() {
		dismiss();
	}

	@Nullable
	public MapActivity getMapActivity() {
		Activity activity = getActivity();
		if (activity instanceof MapActivity) {
			return (MapActivity) activity;
		}
		return null;
	}

	public static void showInstance(@NonNull FragmentManager fragmentManager) {
		if (!fragmentManager.isStateSaved()) {
			UploadPhotoWithProgressBarBottomSheet fragment = new UploadPhotoWithProgressBarBottomSheet();
			fragment.setRetainInstance(true);
			fragment.show(fragmentManager, TAG);
		}
	}
}