package net.osmand.plus.routepreparationmenu;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.SpannableString;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.osmand.AndroidUtils;
import net.osmand.Location;
import net.osmand.data.LatLon;
import net.osmand.data.PointDescription;
import net.osmand.plus.MapMarkersHelper;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.TargetPointsHelper;
import net.osmand.plus.TargetPointsHelper.TargetPoint;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.base.MenuBottomSheetDialogFragment;
import net.osmand.plus.base.bottomsheetmenu.BaseBottomSheetItem;
import net.osmand.plus.base.bottomsheetmenu.SimpleBottomSheetItem;
import net.osmand.plus.base.bottomsheetmenu.simpleitems.DividerHalfItem;
import net.osmand.plus.base.bottomsheetmenu.simpleitems.TitleItem;
import net.osmand.plus.helpers.FontCache;
import net.osmand.plus.helpers.MapMarkerDialogHelper;
import net.osmand.plus.helpers.WaypointDialogHelper;
import net.osmand.plus.mapcontextmenu.other.FavouritesBottomSheetMenuFragment;
import net.osmand.plus.routepreparationmenu.MapRouteInfoMenu.PointType;
import net.osmand.plus.search.QuickSearchDialogFragment;
import net.osmand.plus.widgets.style.CustomTypefaceSpan;

import java.util.List;

import static net.osmand.data.PointDescription.POINT_TYPE_MY_LOCATION;

public class AddPointBottomSheetDialog extends MenuBottomSheetDialogFragment {

	public static final String TAG = "AddPointBottomSheetDialog";
	public static final String POINT_TYPE_KEY = "point_type";

	public static final int ADD_FAVOURITE_TO_ROUTE_REQUEST_CODE = 1;

	private PointType pointType = PointType.START;

	@Override
	public void createMenuItems(Bundle savedInstanceState) {
		Bundle args = getArguments();
		if (args != null && args.containsKey(POINT_TYPE_KEY)) {
			pointType = PointType.valueOf(args.getString(POINT_TYPE_KEY));
		}
		String title;
		switch (pointType) {
			case START:
				title = getString(R.string.add_start_point);
				break;
			case TARGET:
				title = getString(R.string.add_destination_point);
				break;
			case INTERMEDIATE:
				title = getString(R.string.add_intermediate_point);
				break;
			case HOME:
				title = getString(R.string.add_home);
				break;
			case WORK:
				title = getString(R.string.add_work);
				break;
			default:
				title = "";
				break;
		}
		items.add(new TitleItem(title));

		createSearchItem();

		switch (pointType) {
			case START:
				createMyLocItem();
				createSelectOnTheMapItem();
				createFavouritesItem();
				createMarkersItem();
				items.add(new DividerHalfItem(getContext()));
				createSwitchStartAndEndItem();
				break;
			case TARGET:
				createSelectOnTheMapItem();
				createFavouritesItem();
				createMarkersItem();
				items.add(new DividerHalfItem(getContext()));
				createSwitchStartAndEndItem();
				break;
			case INTERMEDIATE:
				createSelectOnTheMapItem();
				createFavouritesItem();
				createMarkersItem();
				break;
			case HOME:
			case WORK:
				createSelectOnTheMapItem();
				createFavouritesItem();
				createMarkersItem();
				break;
			default:
				break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == ADD_FAVOURITE_TO_ROUTE_REQUEST_CODE) {
			dismiss();
		}
	}

	@Override
	protected int getDismissButtonTextId() {
		return R.string.shared_string_close;
	}

	private void createSearchItem() {
		View searchView = View.inflate(new ContextThemeWrapper(getContext(), themeRes), R.layout.bottom_sheet_double_item, null);
		TextView firstTitle = (TextView) searchView.findViewById(R.id.first_title);
		TextView secondTitle = (TextView) searchView.findViewById(R.id.second_title);
		ImageView firstIcon = (ImageView) searchView.findViewById(R.id.first_icon);
		ImageView secondIcon = (ImageView) searchView.findViewById(R.id.second_icon);

		firstTitle.setText(R.string.shared_string_search);
		secondTitle.setText(R.string.shared_string_address);
		firstIcon.setImageDrawable(getActiveIcon(R.drawable.ic_action_search_dark));
		secondIcon.setImageDrawable(getActiveIcon(R.drawable.ic_action_street_name));

		AndroidUtils.setBackground(getContext(), searchView.findViewById(R.id.first_divider),
				nightMode, R.color.dashboard_divider_light, R.color.dashboard_divider_dark);
		AndroidUtils.setBackground(getContext(), searchView.findViewById(R.id.second_divider),
				nightMode, R.color.dashboard_divider_light, R.color.dashboard_divider_dark);

		searchView.findViewById(R.id.first_item).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MapActivity mapActivity = (MapActivity) getActivity();
				if (mapActivity != null) {
					mapActivity.showQuickSearch(getSearchMode(), QuickSearchDialogFragment.QuickSearchTab.HISTORY);
				}
				dismiss();
			}
		});
		searchView.findViewById(R.id.second_item).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MapActivity mapActivity = (MapActivity) getActivity();
				if (mapActivity != null) {
					mapActivity.showQuickSearch(getSearchMode(), false);
				}
				dismiss();
			}
		});
		items.add(new BaseBottomSheetItem.Builder().setCustomView(searchView).create());
	}

	private MapActivity.ShowQuickSearchMode getSearchMode() {
		switch (pointType) {
			case START:
				return MapActivity.ShowQuickSearchMode.START_POINT_SELECTION;
			case TARGET:
				return MapActivity.ShowQuickSearchMode.DESTINATION_SELECTION;
			case INTERMEDIATE:
				return MapActivity.ShowQuickSearchMode.INTERMEDIATE_SELECTION;
			case HOME:
				return MapActivity.ShowQuickSearchMode.HOME_POINT_SELECTION;
			case WORK:
				return MapActivity.ShowQuickSearchMode.WORK_POINT_SELECTION;
			default:
				return MapActivity.ShowQuickSearchMode.START_POINT_SELECTION;
		}
	}

	private void createMyLocItem() {
		BaseBottomSheetItem myLocationItem = new SimpleBottomSheetItem.Builder()
				.setIcon(getIcon(R.drawable.ic_action_location_color, 0))
				.setTitle(getString(R.string.shared_string_my_location))
				.setLayoutId(R.layout.bottom_sheet_item_simple_56dp)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						OsmandApplication app = getMyApplication();
						if (app != null) {
							TargetPointsHelper targetPointsHelper = app.getTargetPointsHelper();
							Location myLocation = app.getLocationProvider().getLastKnownLocation();
							if (myLocation != null) {
								LatLon ll = new LatLon(myLocation.getLatitude(), myLocation.getLongitude());
								switch (pointType) {
									case START:
										if (targetPointsHelper.getPointToStart() != null) {
											targetPointsHelper.clearStartPoint(true);
											app.getSettings().backupPointToStart();
										}
										break;
									case TARGET:
										app.showShortToastMessage(R.string.add_destination_point);
										targetPointsHelper.navigateToPoint(ll, true, -1);
										break;
									case INTERMEDIATE:
										app.showShortToastMessage(R.string.add_intermediate_point);
										targetPointsHelper.navigateToPoint(ll, true, targetPointsHelper.getIntermediatePoints().size());
										break;
									case HOME:
										app.showShortToastMessage(R.string.add_intermediate_point);
										targetPointsHelper.setHomePoint(ll, null);
										break;
									case WORK:
										app.showShortToastMessage(R.string.add_intermediate_point);
										targetPointsHelper.setWorkPoint(ll, null);
										break;
								}
							}
						}
						dismiss();
					}
				}).create();
		items.add(myLocationItem);
	}

	private void createSelectOnTheMapItem() {
		BaseBottomSheetItem selectOnTheMapItem = new SimpleBottomSheetItem.Builder()
				.setIcon(getContentIcon(R.drawable.ic_show_on_map))
				.setTitle(getString(R.string.shared_string_select_on_map))
				.setLayoutId(R.layout.bottom_sheet_item_simple_56dp)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						MapActivity mapActivity = (MapActivity) getActivity();
						if (mapActivity != null) {
							MapRouteInfoMenu menu = mapActivity.getMapLayers().getMapControlsLayer().getMapRouteInfoMenu();
							menu.selectOnScreen(pointType);
						}
						dismiss();
					}
				})
				.create();
		items.add(selectOnTheMapItem);
	}

	private void createFavouritesItem() {
		BaseBottomSheetItem favouritesItem = new SimpleBottomSheetItem.Builder()
				.setIcon(getContentIcon(R.drawable.ic_action_fav_dark))
				.setTitle(getString(R.string.shared_string_favorites))
				.setLayoutId(R.layout.bottom_sheet_item_simple_56dp)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						MapActivity mapActivity = (MapActivity) getActivity();
						if (mapActivity != null) {
							FragmentManager fragmentManager = mapActivity.getSupportFragmentManager();
							FavouritesBottomSheetMenuFragment fragment = new FavouritesBottomSheetMenuFragment();
							Bundle args = new Bundle();
							args.putString(FavouritesBottomSheetMenuFragment.POINT_TYPE_KEY, pointType.name());
							fragment.setTargetFragment(AddPointBottomSheetDialog.this, ADD_FAVOURITE_TO_ROUTE_REQUEST_CODE);
							fragment.setArguments(args);
							fragment.show(fragmentManager, FavouritesBottomSheetMenuFragment.TAG);
						}
					}
				})
				.create();
		items.add(favouritesItem);
	}

	private void createMarkersItem() {
		final OsmandApplication app = getMyApplication();
		if (app == null) {
			return;
		}
		final View markersView = View.inflate(new ContextThemeWrapper(getContext(), themeRes), R.layout.bottom_sheet_double_item, null);

		MapMarkersHelper markersHelper = app.getMapMarkersHelper();
		List<MapMarkersHelper.MapMarker> markers = markersHelper.getMapMarkers();
		MapMarkersHelper.MapMarker marker = null;
		if (markers.size() > 0) {
			marker = markers.get(0);
		}
		TextView firstTitle = (TextView) markersView.findViewById(R.id.first_title);
		TextView secondTitle = (TextView) markersView.findViewById(R.id.second_title);
		ImageView firstIcon = (ImageView) markersView.findViewById(R.id.first_icon);
		ImageView secondIcon = (ImageView) markersView.findViewById(R.id.second_icon);

		firstTitle.setText(R.string.shared_string_markers);
		firstIcon.setImageDrawable(getActiveIcon(R.drawable.ic_action_flag_dark));

		if (marker != null) {
			secondTitle.setText(marker.getName(getContext()));
			secondIcon.setImageDrawable(MapMarkerDialogHelper.getMapMarkerIcon(app, marker.colorIndex));

		}
		markersView.findViewById(R.id.first_item).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MapActivity mapActivity = (MapActivity) getActivity();
				if (mapActivity != null) {
					MapRouteInfoMenu menu = mapActivity.getMapLayers().getMapControlsLayer().getMapRouteInfoMenu();
					menu.selectMapMarker(-1, pointType);
					dismiss();
				}
			}
		});
		markersView.findViewById(R.id.second_item).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MapActivity mapActivity = (MapActivity) getActivity();
				if (mapActivity != null) {
					MapRouteInfoMenu menu = mapActivity.getMapLayers().getMapControlsLayer().getMapRouteInfoMenu();
					menu.selectMapMarker(0, pointType);
					dismiss();
				}
			}
		});
		items.add(new BaseBottomSheetItem.Builder().setCustomView(markersView).create());
	}

	private void createSwitchStartAndEndItem() {
		final View switchStartAndEndView = View.inflate(new ContextThemeWrapper(getContext(), themeRes), R.layout.bottom_sheet_item_simple_56dp, null);
		TextView title = (TextView) switchStartAndEndView.findViewById(R.id.title);

		String titleS = getString(R.string.swap_start_and_destination);
		SpannableString titleSpan = new SpannableString(titleS);
		int firstIndex = titleS.indexOf(" ");
		if (firstIndex != -1) {
			Typeface typeface = FontCache.getRobotoMedium(getContext());
			titleSpan.setSpan(new CustomTypefaceSpan(typeface), firstIndex, titleS.indexOf(" ", firstIndex + 1), 0);
			titleSpan.setSpan(new CustomTypefaceSpan(typeface), titleS.lastIndexOf(" "), titleS.length(), 0);
		}
		title.setText(titleSpan);

		BaseBottomSheetItem switchStartAndEndItem = new SimpleBottomSheetItem.Builder()
				.setIcon(getContentIcon(R.drawable.ic_action_change_navigation_points))
				.setCustomView(switchStartAndEndView)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						MapActivity mapActivity = (MapActivity) getActivity();
						if (mapActivity != null) {
							TargetPointsHelper targetsHelper = mapActivity.getMyApplication().getTargetPointsHelper();
							WaypointDialogHelper.switchStartAndFinish(targetsHelper, targetsHelper.getPointToNavigate(),
									mapActivity, targetsHelper.getPointToStart(), mapActivity.getMyApplication(),
									mapActivity.getDashboard().getWaypointDialogHelper());
						}
						dismiss();
					}
				}).create();
		items.add(switchStartAndEndItem);
	}
}