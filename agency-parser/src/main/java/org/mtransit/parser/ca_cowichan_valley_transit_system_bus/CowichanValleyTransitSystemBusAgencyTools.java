package org.mtransit.parser.ca_cowichan_valley_transit_system_bus;

import static org.mtransit.commons.Constants.EMPTY;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CleanUtils;
import org.mtransit.commons.StringUtils;
import org.mtransit.parser.ColorUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.mt.data.MAgency;

import java.util.Locale;
import java.util.regex.Pattern;

// https://www.bctransit.com/open-data
public class CowichanValleyTransitSystemBusAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new CowichanValleyTransitSystemBusAgencyTools().start(args);
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "Cowichan Valley Regional TS";
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public boolean defaultRouteIdEnabled() {
		return true;
	}

	@Override
	public boolean useRouteShortNameForRouteId() {
		return false; // route ID used by GTFS RT
	}

	@Override
	public @Nullable String getRouteIdCleanupRegex() {
		return "\\-[A-Z]+$";
	}

	@Override
	public boolean defaultRouteLongNameEnabled() {
		return true;
	}

	@Override
	public boolean defaultAgencyColorEnabled() {
		return true;
	}

	private static final String AGENCY_COLOR_GREEN = "34B233";// GREEN (from PDF Corporate Graphic Standards)
	// private static final String AGENCY_COLOR_BLUE = "002C77"; // BLUE (from PDF Corporate Graphic Standards)

	private static final String AGENCY_COLOR = AGENCY_COLOR_GREEN;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Nullable
	@Override
	public String fixColor(@Nullable String color) {
		if (ColorUtils.BLACK.equals(color)) {
			color = null;
		}
		return super.fixColor(color);
	}

	@Nullable
	@Override
	public String provideMissingRouteColor(@NotNull GRoute gRoute) {
		switch (gRoute.getRouteShortName()) {
		// @formatter:off
		case "2": return "17468B";
		case "3": return "80CC28";
		case "4": return "F68712";
		case "5": return "C06EBE";
		case "6": return "ED0790";
		case "7": return "49690F";
		case "7x": return "ACA86E";
		case "8": return "49176D";
		case "9": return "B2BB1E";
		case "20": return "0073AD";
		case "21": return "A54499";
		case "31": return "FBBD09";
		case "34": return "0B6FAE";
		case "36": return "8A0C34";
		case "44": return "00AA4F";
		case "66": return "8CC63F";
		case "99": return "114D8A";
		// @formatter:on
		}
		throw new MTLog.Fatal("Unexpected route color for %s!", gRoute.toStringPlus());
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	@Override
	public boolean directionSplitterEnabled(long routeId) {
		return true;
	}

	@Override
	public boolean allowNonDescriptiveHeadSigns(long routeId) {
		if (routeId == 9L) {
			return true; // it's a mess
		} else if (routeId == 34L) {
			return true; // 2023-08-22 it's a mess
		}
		return super.allowNonDescriptiveHeadSigns(routeId);
	}

	private static final Pattern EXPRESS_ = CleanUtils.cleanWords("express");
	private static final String EXPRESS_REPLACEMENT = CleanUtils.cleanWordsReplacement(EMPTY);

	private static final Pattern DASH_TO_ = Pattern.compile("(-to )", Pattern.CASE_INSENSITIVE);
	private static final String DASH_TO_REPLACEMENT = " to ";

	private static final Pattern DASH_VIA_ = Pattern.compile("(- via )", Pattern.CASE_INSENSITIVE);
	private static final String DASH_VIA_REPLACEMENT = " via ";

	private static final String TRAIL = "Trail";
	private static final Pattern KEEP_TRAIL = Pattern.compile(String.format("((^|\\W){1}(%s)(\\W|$){1})", "trl"), Pattern.CASE_INSENSITIVE);
	private static final String KEEP_TRAIL_REPLACEMENT = String.format("$2%s$4", TRAIL);

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = DASH_TO_.matcher(tripHeadsign).replaceAll(DASH_TO_REPLACEMENT);
		tripHeadsign = DASH_VIA_.matcher(tripHeadsign).replaceAll(DASH_VIA_REPLACEMENT);
		tripHeadsign = CleanUtils.keepToAndRemoveVia(tripHeadsign);
		tripHeadsign = EXPRESS_.matcher(tripHeadsign).replaceAll(EXPRESS_REPLACEMENT);
		tripHeadsign = CleanUtils.CLEAN_AND.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign); // 1st
		tripHeadsign = KEEP_TRAIL.matcher(tripHeadsign).replaceAll(KEEP_TRAIL_REPLACEMENT); // 2nd
		tripHeadsign = CleanUtils.cleanSlashes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@NotNull
	private String[] getIgnoredUpperCaseWords() {
		return new String[]{"BC"};
	}

	private static final Pattern STARTS_WITH_DCOM = Pattern.compile("(^(\\(-DCOM-\\)))", Pattern.CASE_INSENSITIVE);
	private static final Pattern STARTS_WITH_IMPL = Pattern.compile("(^(\\(-IMPL-\\)))", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, gStopName, getIgnoredUpperCaseWords());
		gStopName = STARTS_WITH_DCOM.matcher(gStopName).replaceAll(EMPTY);
		gStopName = STARTS_WITH_IMPL.matcher(gStopName).replaceAll(EMPTY);
		gStopName = CleanUtils.cleanBounds(gStopName);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = CleanUtils.cleanStreetTypes(gStopName); // 1st
		gStopName = KEEP_TRAIL.matcher(gStopName).replaceAll(KEEP_TRAIL_REPLACEMENT); // 2nd
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	@NotNull
	@Override
	public String getStopCode(@NotNull GStop gStop) {
		if (StringUtils.isEmpty(gStop.getStopCode())) {
			//noinspection deprecation
			return gStop.getStopId(); // use stop ID as stop code (fall back = displayed on website)
		}
		return super.getStopCode(gStop);
	}

	@Override
	public int getStopId(@NotNull GStop gStop) {
		return super.getStopId(gStop); // required for GTFS-RT
	}
}
