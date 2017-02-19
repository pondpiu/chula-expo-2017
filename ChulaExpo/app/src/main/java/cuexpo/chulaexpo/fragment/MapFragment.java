package cuexpo.chulaexpo.fragment;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cuexpo.chulaexpo.R;
import cuexpo.chulaexpo.utility.FacultyMapEntity;
import cuexpo.chulaexpo.utility.IMapEntity;
import cuexpo.chulaexpo.utility.NormalPinMapEntity;
import cuexpo.chulaexpo.utility.PopbusRouteMapEntity;


public class MapFragment extends Fragment implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private View rootView;
    private GoogleMap googleMap;
    private CardView pinList;
    private boolean isShowingPinList = false;
    private ImageView showFaculty, showLandmark, showInfo, showInterest, showCanteen, showToilet,
            showBusStop, showBusLine1, showBusLine2, showBusLine3;

//    private Application mainApp = getActivity().getApplication();
    HashMap<String, PopbusRouteMapEntity> popbusRoutes = new HashMap<>();
    HashMap<Integer, FacultyMapEntity> faculties = new HashMap<>();
    ArrayList<NormalPinMapEntity> infoPointsPins = new ArrayList<>();
    ArrayList<NormalPinMapEntity> landmarksPins = new ArrayList<>();
    ArrayList<NormalPinMapEntity> cuTourStationPins = new ArrayList<>();

    private void initializeFaculties() {
        try {
            JSONArray facultiesJSON = new JSONArray(
                    getContext().getResources().getString(R.string.jsonFacultyMap)
            );
            for (int i = 0; i < facultiesJSON.length(); i++) {
                JSONObject facultyData = facultiesJSON.getJSONObject(i);
                faculties.put(facultyData.getInt("id"), new FacultyMapEntity(facultyData));
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private void initializeMapData() {
        initializeFaculties();
//        initializePopbusRoutes();
//        initializeInfoPoints();
//        initializeLandmarks();
//        initializeCUTourStation();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            rootView = inflater.inflate(R.layout.fragment_map, container, false);
        } catch (InflateException e) {
            /* map is already there, just return view as it is */
        }

        pinList = (CardView) rootView.findViewById(R.id.pin_list);
        showFaculty = (ImageView) rootView.findViewById(R.id.show_faculty_city);
        showLandmark = (ImageView) rootView.findViewById(R.id.show_landmark);
        showInfo = (ImageView) rootView.findViewById(R.id.show_info);
        showInterest = (ImageView) rootView.findViewById(R.id.show_interest);
        showCanteen = (ImageView) rootView.findViewById(R.id.show_canteen);
        showToilet = (ImageView) rootView.findViewById(R.id.show_toilet);
        showBusStop = (ImageView) rootView.findViewById(R.id.show_bus_stop);
        showBusLine1 = (ImageView) rootView.findViewById(R.id.show_bus_line_1);
        showBusLine2 = (ImageView) rootView.findViewById(R.id.show_bus_line_2);
        showBusLine3 = (ImageView) rootView.findViewById(R.id.show_bus_line_3);
        // Set OnClickListener
        rootView.findViewById(R.id.show_hide_pin).setOnClickListener(showPinListOnClick);
        rootView.findViewById(R.id.show_current_location).setOnClickListener(showCurrentLocation);
        pinList.setOnClickListener(pinListOCL);
        rootView.findViewById(R.id.faculty_city).setOnClickListener(showFacultyOCL);
        rootView.findViewById(R.id.landmark).setOnClickListener(showLandmarkOCL);
        rootView.findViewById(R.id.info).setOnClickListener(showInfoOCL);
        rootView.findViewById(R.id.interest).setOnClickListener(showInterestOCL);
        rootView.findViewById(R.id.canteen).setOnClickListener(showCanteenOCL);
        rootView.findViewById(R.id.toilet).setOnClickListener(showToiletOCL);
        rootView.findViewById(R.id.bus_stop).setOnClickListener(showBusStopOCL);
        showBusLine1.setOnClickListener(showBusLine1OCL);
        showBusLine2.setOnClickListener(showBusLine2OCL);
        showBusLine3.setOnClickListener(showBusLine3OCL);
//        close.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                hideBottomBox();
//            }
//        });

        // Set visibility
        showFaculty.setSelected(true);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.main_map);
        mapFragment.getMapAsync(this);
        initializeMapData();
        return rootView;
    }

    private View.OnClickListener showPinListOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isShowingPinList) {
                isShowingPinList = false;
                ObjectAnimator.ofFloat(pinList, "x", dpToPx(12), dpToPx(-200)).start();
                Log.d("hide pin", ""+pinList.getX());
            } else {
                isShowingPinList = true;
                ObjectAnimator.ofFloat(pinList, "x", dpToPx(-200), dpToPx(12)).start();
                Log.d("show pin", ""+pinList.getX());
            }
        }
    };

    private View.OnClickListener showCurrentLocation = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hidePinList();
//            if (isShowingPinList) {
//                isShowingPinList = false;
//                pinList.animate().translationY(dpToPx(0));
//            } else {
//                isShowingPinList = true;
//                pinList.animate().translationY(dpToPx(212));
//            }
        }
    };

    private View.OnClickListener showFacultyOCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (showFaculty.isSelected()){
                showFaculty.setSelected(false);
                setAllFacultiesVisibility(false);
            } else {
                showFaculty.setSelected(true);
                setAllFacultiesVisibility(true);
            }
        }
    };

    private void setAllFacultiesVisibility(boolean isVisible) {
        for (IMapEntity faculty : faculties.values()) {
            faculty.setVisible(isVisible);
        }
    }

    private View.OnClickListener showLandmarkOCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (showLandmark.isSelected()){
                showLandmark.setSelected(false);
            } else {
                showLandmark.setSelected(true);
            }
        }
    };

    private View.OnClickListener showInfoOCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (showInfo.isSelected()){
                showInfo.setSelected(false);
            } else {
                showInfo.setSelected(true);
            }
        }
    };

    private View.OnClickListener showInterestOCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (showInterest.isSelected()){
                showInterest.setSelected(false);
            } else {
                showInterest.setSelected(true);
            }
        }
    };

    private View.OnClickListener showCanteenOCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (showCanteen.isSelected()){
                showCanteen.setSelected(false);
            } else {
                showCanteen.setSelected(true);
            }
        }
    };

    private View.OnClickListener showToiletOCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (showToilet.isSelected()){
                showToilet.setSelected(false);
            } else {
                showToilet.setSelected(true);
            }
        }
    };

    private View.OnClickListener showBusStopOCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (showBusStop.isSelected()){
                showBusStop.setSelected(false);
            } else {
                showBusStop.setSelected(true);
            }
        }
    };

    private View.OnClickListener showBusLine1OCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (showBusLine1.isSelected()){
                showBusLine1.setSelected(false);
            } else {
                showBusLine1.setSelected(true);
            }
        }
    };

    private View.OnClickListener showBusLine2OCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (showBusLine2.isSelected()){
                showBusLine2.setSelected(false);
            } else {
                showBusLine2.setSelected(true);
            }
        }
    };

    private View.OnClickListener showBusLine3OCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (showBusLine3.isSelected()){
                showBusLine3.setSelected(false);
            } else {
                showBusLine3.setSelected(true);
            }
        }
    };

//    private void enableMyLocation() {
//        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            // Permission to access the location is missing.
//            PermissionUtils.requestPermission((AppCompatActivity) this.getActivity(), LOCATION_PERMISSION_REQUEST_CODE,
//                    Manifest.permission.ACCESS_FINE_LOCATION, true);
//        } else if (googleMap != null) {
//            try {
//                googleMap.setMyLocationEnabled(true);
//
//                if (locationSource == null) {
//                    locationSource = new MyLocationSource(this.getContext());
//                    googleMap.setLocationSource(locationSource);
//                }
//
//                Location location = locationSource.getLastKnownLocation();
//                if (location != null) {
//                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                            new LatLng(location.getLatitude(), location.getLongitude()),
//                            17
//                    ), 1000, null);
//                    Snackbar.make(rootView, "Showing your current location...", Snackbar.LENGTH_SHORT).show();
//                }
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//    }

    private View.OnClickListener myLocationListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                enableMyLocation();
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
//        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
//            return;
//        }
//
//        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
//                Manifest.permission.ACCESS_FINE_LOCATION)) {
//            // Enable the my location layer if the permission has been granted.
//            enableMyLocation();
//        } else {
//            // Display the missing permission error dialog when the fragments resume.
//            PermissionUtils.PermissionDeniedDialog
//                    .newInstance(true).show(this.getChildFragmentManager(), "dialog");
//        }
    }

    private void hidePinList() {
        if (isShowingPinList) {
            isShowingPinList = false;
            pinList.animate().translationX(dpToPx(0));
        }
    }

    private View.OnClickListener closeOCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            hideBottomBox();
        }
    };

    private View.OnClickListener pinListOCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        // Add faculties
        for (IMapEntity facultyEntry : faculties.values()) {
            facultyEntry.setMap(googleMap);
        }
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        return Math.round(dp * ((float)displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
