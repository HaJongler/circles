package space.harbour.jonathan.circles;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

public class CircleChooserFragment extends Fragment {

    SendCircle SCN;
    private List<String> circles;
    private String service = "Contacts";
    private CircleAdapter circleAdapter;

    public static CircleChooserFragment newInstance() {
        // Default new chooser to go to contacts screen
        CircleChooserFragment ccf = new CircleChooserFragment().setService("Contacts");
        return ccf;
    }

    public CircleChooserFragment setService(String service) {
        this.service = service;
        return this;
    }

    public void refreshCircles() {
        circles.clear();
        circles.addAll(((MainActivity) getActivity()).getDataManager().getAllCircles());
        circleAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        circles = ((MainActivity) getActivity()).getDataManager().getAllCircles();

        // Show the array of friends on the screen
        ListView circlesListView = getView().findViewById(R.id.circles_list);

        // Create the custom adapter
        circleAdapter = new CircleAdapter(getActivity(), (ArrayList<String>) circles);
        circlesListView.setAdapter(circleAdapter);

        // Handle click on circle
        circlesListView.setClickable(true);
        circlesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Send the data to the new fragment
                System.out.println(circles.get(i));
                SCN.sendCircle(circles.get(i), service);
            }
        });

        // Handle add circle
        FloatingActionButton floatingActionButton = (FloatingActionButton) getView().findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                b.setTitle("New circle name:");
                final EditText input = new EditText(getActivity());
                b.setView(input);
                b.setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        // Get the new circle name
                        String result = input.getText().toString();
                        // Add to JSON
                        boolean success = ((MainActivity) getActivity()).getDataManager().addCircle(result);
                        if (!success) {
                            Toast.makeText(getActivity(), "Circle " + result + " already exists!", Toast.LENGTH_SHORT).show();
                        }
                        else { refreshCircles(); }
                    }
                });
                b.setNegativeButton("CANCEL", null);
                b.create().show();
            }
        });

        // Handle remove circle
        circlesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String removeCircle = circles.get(i);
                ((MainActivity) getActivity()).getDataManager().removeCircle(removeCircle);
                refreshCircles();
                return true;
            }
        });

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_circle_choose, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            SCN = (SendCircle) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException("Error in retrieving data. Please try again");
        }
    }

    interface SendCircle {
        void sendCircle(String circle, String service);
    }

    class CircleAdapter extends ArrayAdapter<String> {

        public CircleAdapter(Context context, ArrayList<String> circles) {
            super(context, 0, circles);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            String circle = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_single, parent, false);
            }
            // Lookup view for data population
            TextView circleName = (TextView) convertView.findViewById(R.id.contact_name);
            ImageView circlePhoto = (ImageView) convertView.findViewById(R.id.contact_img);
            // Populate the data into the template view using the data object
            circleName.setText(circle);
            circlePhoto.setImageDrawable(getContext().getDrawable(R.drawable.ic_priscilla));
            // Return the completed view to render on screen
            return convertView;
        }
    }
}
