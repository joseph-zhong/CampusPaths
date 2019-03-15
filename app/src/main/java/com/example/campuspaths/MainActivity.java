package com.example.campuspaths;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import hw5.Edge;
import hw8.CampusBuilding;
import hw8.CampusPath;
import hw8.Coordinate;
import hw8.model.Campus;

/**
 * Primary interface for the user.
 *
 * Workflow:
 *  - Initialize App
 *      - Map is loaded
 *      - List of campus buildings and paths are loaded
 *      - Dropdown menu for src/dst buildings are loaded
 *      - Find Route and Reset buttons are loaded
 *
 * Actions:
 *  - User can select campus buildings to navigate between through the dropdown menu
 *      - Selecting each campus building adds a marker to the map
 *          - This cleans drawn paths if already drawn
 *      - Once the two buildings are selected, tapping the FindRouteButton draws the Path
 *      - Tapping the ResetButton clears the markers and paths and menu */
public class MainActivity extends AppCompatActivity {
    private static float zoom = 2.5f;

    private Campus model;

    DrawView mapDrawView;
    Button findRouteBtn;
    Button resetBtn;
    ListView buildingsSrcListView;
    ListView buildingsDstListView;

    private CampusBuilding source;
    private CampusBuilding destination;
    private boolean routeDrawn;

    /**
     * onCreate.
     * This is the primary interfacing controller class for the user to interact with, which tightly interacts with the
     * representative view and underlying model class state representation.
     * This initializes the internal model state, and controller class.
     * @param savedInstanceState Bundle Android-specific Instance State during creation time.
     * @spec.effects Sets the primary view to the activity_main, and initializes the underlying model internal state.
     *               Afterwards, this initializes the controller state for which the user will interact from through the
     *               view interface, controlled by the initialized controller states. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Model: Load data.
        List<CampusBuilding> buildings = null;
        List<CampusPath> paths = null;
        InputStream pathsInputStream = this.getResources().openRawResource(R.raw.campus_paths);
        InputStream buildingsInputStream = this.getResources().openRawResource(R.raw.campus_buildings_new);
        try {
            buildings = AndroidParser.parseBuildingData(buildingsInputStream);
            paths = AndroidParser.parsePathData(pathsInputStream);
            this.model = new Campus(buildings, paths);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // Controller: Track UI elements.
        mapDrawView = findViewById(R.id.map_draw_view);
        findRouteBtn = findViewById(R.id.find_route_btn);
        resetBtn = findViewById(R.id.reset_btn);
        buildingsSrcListView = findViewById(R.id.src_buildings_listview);
        buildingsDstListView = findViewById(R.id.dst_buildings_listview);

        // Populate menu list elements with building short names.
        List<String> buildingShortNames = new ArrayList<>();
        for (CampusBuilding campusBuilding: buildings) {
            buildingShortNames.add(campusBuilding.getShortName());
        }
        ArrayAdapter<String> adapterSrc = new ArrayAdapter<String>(
                getApplicationContext(),
                R.layout.support_simple_spinner_dropdown_item,
                new ArrayList<String>());
        ArrayAdapter<String> adapterDst = new ArrayAdapter<String>(
                getApplicationContext(),
                R.layout.support_simple_spinner_dropdown_item,
                new ArrayList<String>());
        adapterSrc.addAll(buildingShortNames);
        adapterDst.addAll(buildingShortNames);

        buildingsSrcListView.setAdapter(adapterSrc);
        buildingsDstListView.setAdapter(adapterDst);

        // Set listeners.
        resetBtn.setOnClickListener(resetBtnClick);
        findRouteBtn.setOnClickListener(findRouteBtnClick);
        buildingsSrcListView.setOnItemClickListener(listViewItemClick);
        buildingsDstListView.setOnItemClickListener(listViewItemClick);
    }

    /**
     * OnItemClickListener.
     * This is the primary ListView item click listener for when the user clicks on an item in the
     * particular list view.
     *
     * Here this will handle adding an indicator to the map that a source or destination campus
     * building has been selected. */
    private ListView.OnItemClickListener listViewItemClick = new ListView.OnItemClickListener() {
        /**
         * onItemClick.
         * This is the primary ItemClick listener for when the user clicks on a button to select a building.
         * @param parent AdapterView&lt;?&gt; for the container of the item clicked.
         * @param view View for which the item was clicked.
         * @param position int representing the position of the item clicked in the list.
         * @param id int representing the id of the item clicked in the list.
         * @spec.effects This cleans up a previously drawn route if a new building is chosen.
         *               If the user selected an item from the source list, then a new source building is drawn,
         *               otherwise if an item from the destination list is selected then a new destination building is
         *               drawn. */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            System.out.printf("ListView: onItemClick: " +
                    "\n\tparent: '%s'" +
                    "\n\tview: '%s'" +
                    "\n\tposition: '%s'" +
                    "\n\tid: '%s'\n",
                    parent.getId(), view.getId(), position, id);

            // Query building from shortName.
            int parentId = parent.getId();
            String shortName = (String) parent.getItemAtPosition(position);
            CampusBuilding building = model.getBuildingByShortName(shortName);
            Coordinate buildingLoc = building.getLocation();
            String longName = building.getLongName();

            // Clean up previously drawn route if a different building is chosen.
            if (routeDrawn
                    && (!building.equals(source) || !building.equals(destination))) {
                System.out.printf("\t\tonItemClick: clean up pts!" +
                        "\n\tsource: '%s'" +
                        "\n\tdestination: '%s'" +
                        "\n\tbuilding: '%s'\n",
                        source, destination, building);
                resetPath();
                resetZoom();
            }

            System.out.printf("\tsrcBuilding: " +
                    "\n\t\tname: '%s' -- '%s'" +
                    "\n\t\tcoord: '%s'\n",
                    shortName, longName, buildingLoc);
            System.out.printf("\tListView: onItemClick: \n\t\tDebug: srcBuildingId: '%s'\n", R.id.src_buildings_listview);
            System.out.printf("ListView: onItemClick: \n\t\tDebug: dstBuildingId: '%s'\n", R.id.dst_buildings_listview);

            // Draw building location.
            assert(parentId == R.id.src_buildings_listview || parentId == R.id.dst_buildings_listview);
            if (parentId == R.id.src_buildings_listview) {
                // Add the src marker.
                mapDrawView.setSrc(buildingLoc);
                source = building;
            }
            else {
                // Add the dst marker.
                mapDrawView.setDst(buildingLoc);
                destination = building;
            }
        }
    };

    /**
     * OnClickListener.
     * This is the primary Button click listener for when the user clicks on a button to find a route.
     *
     * Here this will draw the route between a pre-selected source and destination campus building.
     * If the source and destination campus buildings are not already selected, then no action is taken. */
    private View.OnClickListener findRouteBtnClick = new View.OnClickListener() {
        /**
         * onClick.
         * This finds and draws the route between the source and destination CampusBuildings if they are pre-selected.
         * @param view View for which the onClick was called from.
         * @spec.requires There must exist a path between the two CampusBuilding locations.
         * @spec.effects Sets the pts for the mapDrawView using the computed shortestPath between the source and
         *               destination buildings.  */
        @Override
        public void onClick(View view) {
            System.out.printf("Button onClick: " +
                    "\n\tview: '%s'\n",
                    view.getId());

            if (mapDrawView.getSrc() != null && mapDrawView.getDst() != null) {
                // Both sources are available: find the path and draw it.
                ArrayList<Edge<Double, Coordinate>> path = model.getShortestPath(source.getLocation(), destination.getLocation());
                assert (path != null);
                mapDrawView.setPts(path);
                routeDrawn = true;

                // Zoom.
                float midX = (float) (Math.abs(source.getLocation().getX() + destination.getLocation().getX()) / 2);
                float midY = (float) (Math.abs(source.getLocation().getY() + destination.getLocation().getY()) / 2);
                mapDrawView.setPivotX(midX * DrawView.SCALING);
                mapDrawView.setPivotY(midY * DrawView.SCALING);
                mapDrawView.setScaleX(zoom);
                mapDrawView.setScaleY(zoom);
            }
        }
    };

    /**
     * OnClickListener.
     * This is the primary Button click listener for when the user clicks on a button to reset the state and view. */
    private View.OnClickListener resetBtnClick = new View.OnClickListener() {
        /**
         * onClick.
         * This sets all of the relevant state to null and resets the view the initial state.
         * @param view View for which the onClick was called from.
         * @spec.requires mapDrawView is not null.
         * @spec.effects Sets source, destination, and the image view points, src and dst to null. */
        @Override
        public void onClick(View view) {
            System.out.printf("Button onClick:" +
                    "\n\tview: '%s'", view.getId());
            source = null;
            destination = null;

            mapDrawView.setSrc(null);
            mapDrawView.setDst(null);

            resetPath();
            resetZoom();
        }
    };

    /**
     * Setter.
     * This resets the path of the map image view.
     * @spec.requires mapDrawView is not null.
     * @spec.effects This resets the path of the map image view. */
    private void resetPath() {
        mapDrawView.setPts(null);
        routeDrawn = false;
    }

    /**
     * Setter.
     * This resets the zoom of the map image view.
     * @spec.requires mapDrawView is not null.
     * @spec.effects This resets the zoom of the map image view. */
    private void resetZoom() {
        mapDrawView.setPivotX(0.0f);
        mapDrawView.setPivotY(0.0f);
        mapDrawView.setScaleX(1.0f);
        mapDrawView.setScaleY(1.0f);
    }
}
