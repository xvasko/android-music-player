package com.matejvasko.player.fragments.account;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matejvasko.player.R;
import com.matejvasko.player.adapters.RequestListAdapter;
import com.matejvasko.player.authentication.Authentication;
import com.matejvasko.player.firebase.FirebaseDatabaseManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private RecyclerView recyclerView;
    private RequestListAdapter requestListAdapter;

    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_request, container, false);

        String userId = Authentication.getCurrentUserUid();
        if (userId != null) {
            FirebaseDatabaseManager.rootDatabase.child("friend_requests").child(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<String> requests = new ArrayList<>();
                    for (DataSnapshot request : dataSnapshot.getChildren()) {
                        if (request.child("request_type").getValue().equals("received")) {
                            requests.add(request.getKey());
                        }
                    }
                    requestListAdapter = new RequestListAdapter(getActivity(), requests);

                    recyclerView = view.findViewById(R.id.requests_recycler_view);
                    recyclerView.setAdapter(requestListAdapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        return view;
    }

}
