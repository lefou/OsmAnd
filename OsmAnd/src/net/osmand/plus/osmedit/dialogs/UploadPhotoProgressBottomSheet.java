package net.osmand.plus.osmedit.dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import net.osmand.plus.R;
import net.osmand.plus.UiUtilities;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.base.MenuBottomSheetDialogFragment;
import net.osmand.plus.base.bottomsheetmenu.BaseBottomSheetItem;
import net.osmand.plus.base.bottomsheetmenu.BottomSheetItemWithDescription;
import net.osmand.plus.base.bottomsheetmenu.simpleitems.DividerSpaceItem;

public class UploadPhotoProgressBottomSheet extends MenuBottomSheetDialogFragment {

	public static final String TAG = DismissRouteBottomSheetFragment.class.getSimpleName();
	private ProgressBar planRouteProgressBar;

	@Override
	public void createMenuItems(Bundle savedInstanceState) {
		BaseBottomSheetItem descriptionItem = new BottomSheetItemWithDescription.Builder()
				.setTitle(getString(R.string.upload_photo))
				.setLayoutId(R.layout.bottom_sheet_with_progress_bar)
				.create();

		items.add(descriptionItem);
		View pbContainer = LayoutInflater.from(getMapActivity()).inflate(R.layout.bottom_sheet_with_progress_bar, null);
		planRouteProgressBar = (ProgressBar) pbContainer.findViewById(R.id.progress_bar);
		planRouteProgressBar.setVisibility(View.VISIBLE);

		int padding = getResources().getDimensionPixelSize(R.dimen.content_padding_small);
		items.add(new DividerSpaceItem(requireContext(), padding));
	}

	@Override
	protected boolean useVerticalButtons() {
		return true;
	}

	@Override
	protected int getDismissButtonTextId() {
		return R.string.shared_string_cancel;
	}

//	@Override
//	protected int getRightBottomButtonTextId() {
//		return R.string.shared_string_dismiss;
//	}

	@Override
	protected UiUtilities.DialogButtonType getRightBottomButtonType() {
		return UiUtilities.DialogButtonType.PRIMARY;
	}

	@Override
	public int getSecondDividerHeight() {
		return getResources().getDimensionPixelSize(R.dimen.bottom_sheet_icon_margin);
	}

	@Override
	protected void onRightBottomButtonClick() {
		dismiss();
	}

	@Override
	public void onDismiss(@NonNull DialogInterface dialog) {
		super.onDismiss(dialog);

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
			UploadPhotoProgressBottomSheet fragment = new UploadPhotoProgressBottomSheet();
			fragment.setRetainInstance(true);
			fragment.show(fragmentManager, TAG);
		}
	}
}
