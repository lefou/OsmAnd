package net.osmand.plus.card.color.palette.gradient;

import static net.osmand.gpx.GpxParameter.MAX_ELEVATION;
import static net.osmand.gpx.GpxParameter.MIN_ELEVATION;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.GradientChart;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import net.osmand.ColorPalette;
import net.osmand.gpx.GPXTrackAnalysis;
import net.osmand.plus.R;
import net.osmand.plus.card.color.palette.main.IColorsPalette;
import net.osmand.plus.card.color.palette.main.data.PaletteColor;
import net.osmand.plus.charts.ChartUtils;
import net.osmand.plus.plugins.srtm.TerrainMode.TerrainType;
import net.osmand.plus.routepreparationmenu.cards.BaseCard;
import net.osmand.plus.utils.AndroidUtils;
import net.osmand.plus.utils.OsmAndFormatter;
import net.osmand.plus.utils.OsmAndFormatter.FormattedValue;
import net.osmand.plus.utils.UiUtilities;
import net.osmand.plus.widgets.tools.HorizontalSpaceItemDecoration;
import net.osmand.router.RouteColorize.ColorizationType;

import java.text.DecimalFormat;

public class GradientColorsPaletteCard extends BaseCard implements IColorsPalette {
	public static final float MAX_ALTITUDE_ADDITION = 50f;

	private final GradientColorsPaletteController controller;
	private final GradientColorsPaletteAdapter paletteAdapter;
	private RecyclerView rvColors;


	public GradientColorsPaletteCard(@NonNull FragmentActivity activity,
	                                 @NonNull GradientColorsPaletteController controller) {
		this(activity, controller, true);
	}

	public GradientColorsPaletteCard(@NonNull FragmentActivity activity,
	                                 @NonNull GradientColorsPaletteController controller,
	                                 boolean usedOnMap) {
		super(activity, usedOnMap);
		this.controller = controller;

		controller.bindPalette(this);
		paletteAdapter = new GradientColorsPaletteAdapter(activity, controller, nightMode);
	}

	@Override
	public int getCardLayoutId() {
		return R.layout.card_gradient_colors_palette;
	}

	@Override
	protected void updateContent() {
		setupColorsPalette();
		updateCard();
	}

	private void updateCard() {
		setupAllColorsButton();
		askScrollToTargetColorPosition(controller.getSelectedColor(), false);
		updateChart();
	}

	private void updateChart() {
		if (!(controller.selectedPaletteColor instanceof PaletteGradientColor)) {
			return;
		}
		ColorPalette colorPalette = ((PaletteGradientColor) controller.selectedPaletteColor).getColorPalette();
		GradientChart chart = view.findViewById(R.id.chart);

		int labelsColor = ContextCompat.getColor(app, R.color.text_color_secondary_light);
		int xAxisGridColor = AndroidUtils.getColorFromAttr(app, R.attr.chart_x_grid_line_axis_color);

		ChartUtils.setupGradientChart(getMyApplication(), chart, 9, 24, false, xAxisGridColor, labelsColor);
		Object gradientType = controller.gradientCollection.getGradientType();

		IAxisValueFormatter formatter = null;
		if (gradientType instanceof ColorizationType) {
			formatter = getColorizationTypeFormatter((ColorizationType) gradientType, controller.analysis);
		} else if (gradientType instanceof TerrainType) {
			formatter = getTerrainTypeFormatter();
		}
		chart.setData(ChartUtils.buildGradientChart(app, chart, colorPalette, formatter, nightMode));
		chart.notifyDataSetChanged();
		chart.invalidate();
	}

	@NonNull
	private IAxisValueFormatter getColorizationTypeFormatter(@NonNull ColorizationType colorizationType, @Nullable GPXTrackAnalysis analysis) {
		return (value, axis) -> {
			String stringValue = formatValue(value, 100);
			String type = "%";
			FormattedValue formattedValue;
			switch (colorizationType) {
				case SPEED:
					if (analysis != null && analysis.getMaxSpeed() != 0) {
						type = app.getSettings().SPEED_SYSTEM.getModeValue(app.getSettings().getApplicationMode()).toShortString(app);
						stringValue = formatValue(value, analysis.getMaxSpeed());
					}
					break;
				case ELEVATION:
					if (analysis != null) {
						float calculatedValue;
						float minElevation = (float) analysis.getMinElevation();
						float maxElevation = (float) analysis.getMaxElevation() + MAX_ALTITUDE_ADDITION;
						if (minElevation != (double) MIN_ELEVATION.getDefaultValue() && maxElevation != (double) MAX_ELEVATION.getDefaultValue()) {
							if (value == 0) {
								calculatedValue = minElevation;
							} else {
								calculatedValue = minElevation + (value * ((maxElevation - minElevation)));
							}
						} else {
							break;
						}
						formattedValue = OsmAndFormatter.getFormattedDistanceValue(calculatedValue, app, null, app.getSettings().METRIC_SYSTEM.get());
						stringValue = formattedValue.value;
						type = formattedValue.unit;
					}
					break;
			}
			return app.getString(R.string.ltr_or_rtl_combine_via_space, stringValue, type);
		};
	}

	@NonNull
	private IAxisValueFormatter getTerrainTypeFormatter() {
		return (value, axis) -> GradientUiHelper.formatTerrainTypeValues(value);
	}

	@NonNull
	private String formatValue(float value, float multiplier) {
		DecimalFormat decimalFormat = new DecimalFormat("#");
		return decimalFormat.format(value * multiplier);
	}

	private void setupColorsPalette() {
		rvColors = view.findViewById(R.id.colors_list);
		rvColors.setLayoutManager(new LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false));
		rvColors.addItemDecoration(new HorizontalSpaceItemDecoration(getDimen(R.dimen.content_padding_small_half)));
		rvColors.setClipToPadding(false);
		rvColors.setAdapter(paletteAdapter);
	}

	private void setupAllColorsButton() {
		View buttonAllColors = view.findViewById(R.id.button_all_colors);
		buttonAllColors.setOnClickListener(v -> controller.onAllColorsButtonClicked(activity));
		updateAllColorsButton();
	}

	private void updateAllColorsButton() {
		View buttonAllColors = view.findViewById(R.id.button_all_colors);
		int controlsAccentColor = controller.getControlsAccentColor(nightMode);
		UiUtilities.setupListItemBackground(activity, buttonAllColors, controlsAccentColor);
	}

	private void askScrollToTargetColorPosition(@Nullable PaletteColor targetPaletteColor,
	                                            boolean useSmoothScroll) {
		if (targetPaletteColor == null) {
			return;
		}
		int targetPosition = paletteAdapter.indexOf(targetPaletteColor);
		LinearLayoutManager lm = (LinearLayoutManager) rvColors.getLayoutManager();
		int firstVisiblePosition = lm != null ? lm.findFirstCompletelyVisibleItemPosition() : 0;
		int lastVisiblePosition = lm != null ? lm.findLastCompletelyVisibleItemPosition() : paletteAdapter.getItemCount();
		if (targetPosition < firstVisiblePosition || targetPosition > lastVisiblePosition) {
			if (useSmoothScroll) {
				rvColors.smoothScrollToPosition(targetPosition);
			} else {
				rvColors.scrollToPosition(targetPosition);
			}
		}
	}

	@Override
	public void updatePaletteColors(@Nullable PaletteColor targetPaletteColor) {
		updateCard();
		paletteAdapter.updateColorsList();
	}

	@Override
	public void updatePaletteSelection(@Nullable PaletteColor oldColor, @NonNull PaletteColor newColor) {
		paletteAdapter.askNotifyItemChanged(oldColor);
		paletteAdapter.askNotifyItemChanged(newColor);
		askScrollToTargetColorPosition(newColor, true);
		if (controller.isAccentColorCanBeChanged()) {
			updateAllColorsButton();
		}
		updateChart();
	}
}
