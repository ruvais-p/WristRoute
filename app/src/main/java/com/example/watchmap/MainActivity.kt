package com.example.watchmap

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.viewport
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.common.location.DeviceLocationProvider
import com.mapbox.common.location.LocationProviderRequest
import com.mapbox.common.location.LocationService
import com.mapbox.common.location.LocationServiceFactory
import com.mapbox.maps.extension.style.utils.transition
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity(), PermissionsListener {
    private lateinit var mapView: MapView
    private lateinit var permissionsManager: PermissionsManager
    private lateinit var locationButton: FloatingActionButton
    private lateinit var markButton: FloatingActionButton
    private lateinit var profileButton: FloatingActionButton
    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource
    private lateinit var navigationCamera: NavigationCamera
    private var destinationPoint: Point? = null
    private var originPoint: Point? = null
    private var currentRoute: DirectionsRoute? = null
    private lateinit var routeLineApi: MapboxRouteLineApi
    private lateinit var routeLineView: MapboxRouteLineView
    private lateinit var routeArrowApi: MapboxRouteArrowApi
    private lateinit var routeArrowView: MapboxRouteArrowView

    private var isMarkingEnabled = false // Flag to track if marking is enabled

    // Add this property using requireMapboxNavigation
    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onResumedObserver = object : MapboxNavigationObserver {
            @SuppressLint("MissingPermission")
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                // Register observers when navigation is attached
                mapboxNavigation.startTripSession()
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                // Clean up when detached
            }
        },
        onInitialize = this::initNavigation
    )

    // Add this initialization function
    private fun initNavigation() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .build()
        )

        // Initialize route line components
        val routeLineApiOptions = MapboxRouteLineApiOptions.Builder().build()
        routeLineApi = MapboxRouteLineApi(routeLineApiOptions)

        val routeLineViewOptions = MapboxRouteLineViewOptions.Builder(this).build()
        routeLineView = MapboxRouteLineView(routeLineViewOptions)

        // Initialize route arrow components
        val routeArrowOptions = RouteArrowOptions.Builder(this).build()
        routeArrowApi = MapboxRouteArrowApi()
        routeArrowView = MapboxRouteArrowView(routeArrowOptions)

        // Initialize viewport data source and navigation camera
        viewportDataSource = MapboxNavigationViewportDataSource(mapView.getMapboxMap())
        navigationCamera = NavigationCamera(
            mapView.getMapboxMap(),
            mapView.camera,
            viewportDataSource
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        mapView = findViewById(R.id.mapView)
        locationButton = findViewById(R.id.locationButton)
        markButton = findViewById(R.id.markButton)
        profileButton = findViewById(R.id.profileButton)

        // Set up button click listeners
        locationButton.setOnClickListener {
            // Check if location component is enabled first
            if (mapView.location.enabled) {
                // Get the current device location using the correct method
                val locationProvider = LocationServiceFactory.getOrCreate()
                    .getDeviceLocationProvider(LocationProviderRequest.Builder().build())

                if (locationProvider.isValue) {
                    locationProvider.value?.getLastLocation { location ->
                        if (location != null) {
                            // Store the current location in originPoint
                            originPoint = Point.fromLngLat(location.longitude, location.latitude)
                            Log.d("MainActivity", "Origin point set to: ${originPoint?.latitude()}, ${originPoint?.longitude()}")
                            Toast.makeText(this, "Origin point set!", Toast.LENGTH_SHORT).show()
                            // Center map on current location with zoom
                            mapView.viewport.transitionTo(
                                targetState = mapView.viewport.makeFollowPuckViewportState(
                                    options = FollowPuckViewportStateOptions.Builder()
                                        .zoom(15.0) // You can adjust zoom level as needed
                                        .pitch(0.0)  // Looking straight down
                                        .build()
                                ),
                                transition = mapView.viewport.makeImmediateViewportTransition()
                            )
                        } else {
                            Toast.makeText(this, "Unable to retrieve current location", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Location provider not available", Toast.LENGTH_SHORT).show()
                }
            } else {
                // If location isn't enabled yet, enable it first
                enableLocationComponent()
                // Show a toast indicating location is being enabled
                Toast.makeText(this, "Enabling location...", Toast.LENGTH_SHORT).show()
            }
        }

        markButton.setOnClickListener {
            isMarkingEnabled = true // Enable marking mode
            Toast.makeText(this, "Tap on the map to mark a location", Toast.LENGTH_SHORT).show()
            markLocationPoint()

            // Only request routes if both origin and destination are set
            if (originPoint != null && destinationPoint != null) {
                requestRoute()
            } else {
                Toast.makeText(this, "Please set both origin and destination points", Toast.LENGTH_SHORT).show()
            }
        }

        profileButton.setOnClickListener {

        }


    }
    private fun requestRoute() {
        if (originPoint == null || destinationPoint == null) {
            Toast.makeText(this, "Origin and destination points are required", Toast.LENGTH_SHORT).show()
            return
        }

        val routeOptions = RouteOptions.builder()
            .applyDefaultNavigationOptions()
            .profile(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
            .alternatives(true)
            .annotationsList(
                listOf(
                    DirectionsCriteria.ANNOTATION_CONGESTION_NUMERIC,
                    DirectionsCriteria.ANNOTATION_DISTANCE,
                    DirectionsCriteria.ANNOTATION_DURATION
                )
            )
            .coordinatesList(listOf(originPoint, destinationPoint))
            .build()

        mapboxNavigation.requestRoutes(routeOptions,
            object : NavigationRouterCallback {
                override fun onCanceled(
                    routeOptions: RouteOptions,
                    routerOrigin: String
                ) {
                    Log.d("MainActivity", "Route request canceled")
                    Toast.makeText(
                        this@MainActivity,
                        "Route request canceled",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onFailure(
                    reasons: List<RouterFailure>,
                    routeOptions: RouteOptions
                ) {
                    Log.e("MainActivity", "Route request failed: ${reasons.firstOrNull()?.message}")
                    Toast.makeText(
                        this@MainActivity,
                        "Route request failed: ${reasons.firstOrNull()?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onRoutesReady(
                    routes: List<NavigationRoute>,
                    routerOrigin: String
                ) {
                    mapboxNavigation.setNavigationRoutes(routes)
                    routeLineApi.setNavigationRoutes(routes) { value ->
                        mapView.getMapboxMap().getStyle()?.let { style ->
                            routeLineView.renderRouteDrawData(style, value)
                        }
                    }

                    // Update viewport to show the entire route
                    viewportDataSource.onRouteChanged(routes.first())
                    viewportDataSource.evaluate()
                    navigationCamera.requestNavigationCameraToOverview()
                }
            }
        )
    }

    private fun markLocationPoint(){
        // Set up touch listener for the map
        mapView.getMapboxMap().addOnMapClickListener { point ->
            if (isMarkingEnabled) {
                addMarkerAtPoint(point)
                destinationPoint = point
                isMarkingEnabled = false // Disable marking mode after marking a point
                Toast.makeText(
                    this,
                    "Marked location: Lat: ${point.latitude()}, Lng: ${point.longitude()}",
                    Toast.LENGTH_SHORT
                ).show()

                // Request route after setting the destination
                if (originPoint != null) {
                    requestRoute()
                } else {
                    Toast.makeText(this, "Please set origin point first", Toast.LENGTH_SHORT).show()
                }
            }
            true // Return true to indicate the event is consumed
        }

        // Check for location permissions
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            enableLocationComponent()
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }

    private fun enableLocationComponent() {
        with(mapView) {
            location.locationPuck = createDefault2DPuck(withBearing = true)
            location.enabled = true
            location.puckBearing = PuckBearing.COURSE
            location.puckBearingEnabled = true
            location.pulsingEnabled = true

            try {
                location.pulsingColor = R.color.black
            } catch (e: Exception) {
                location.pulsingColor = android.R.color.black
            }

            // Center map on user's location initially
            viewport.transitionTo(
                targetState = viewport.makeFollowPuckViewportState(),
                transition = viewport.makeImmediateViewportTransition()
            )
        }
    }

    private fun addMarkerAtPoint(point: Point) {
        try {
            // Create a simple circle marker without an image
            val circleManager = mapView.annotations.createCircleAnnotationManager()
            val circleOptions = CircleAnnotationOptions()
                .withPoint(point)
                .withCircleRadius(8.0)
                .withCircleColor("#FF0000") // Red circle
                .withCircleStrokeWidth(2.0)
                .withCircleStrokeColor("#FFFFFF") // White stroke

            circleManager.create(circleOptions)

            // Set this as destination
            destinationPoint = point
        } catch (e: Exception) {
            Log.e("MainActivity", "Error adding marker: ${e.message}", e)
            Toast.makeText(this, "Error marking location: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // PermissionsListener implementation
    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        // You could show a dialog explaining why you need location permissions
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent()
        } else {
            // Handle permission denial
            // You might want to show a toast or dialog explaining limitations
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}