package com.ctrl.supera.locationrecorder;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link gpsHeaderListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link gpsHeaderListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class gpsHeaderListFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String TAG = "gpsHeaderListFragment";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private TextView output;
    private TextView satelliteInfoTextView;
    private ListView gpsHeaderList;

    private boolean bRecordStatus;
    private Button btRecordCtrl;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment gpsHeaderListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static gpsHeaderListFragment newInstance(String param1, String param2) {
        gpsHeaderListFragment fragment = new gpsHeaderListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public gpsHeaderListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview = inflater.inflate(R.layout.fragment_gps_header_list, container, false);


        /* GPS Header information control */
        output = (TextView) rootview.findViewById(R.id.Location);
        satelliteInfoTextView = (TextView) rootview.findViewById(R.id.SatelliteInfo);
        gpsHeaderList = (ListView) rootview.findViewById(R.id.GpsDatabaseInformation);
        gpsHeaderList.setOnItemClickListener(this);

        btRecordCtrl = (Button) rootview.findViewById(R.id.LocationStart);
        btRecordCtrl.setOnClickListener(this);

        return rootview;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.LocationStart:
                if (bRecordStatus == false) {
                    bRecordStatus = true;
                    //mService.needRecordLocation = true;
                    btRecordCtrl.setText("Stop");
                } else {
                    bRecordStatus = false;
                    //mService.needRecordLocation = false;
                    btRecordCtrl.setText("Start");
                }
                break;
            case R.id.GpsDatabaseInformation:
                if (bRecordStatus == false) {

                }
                break;
            default:
                break;
        }
    }
}
