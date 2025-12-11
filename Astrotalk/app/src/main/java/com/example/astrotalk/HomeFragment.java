package com.example.astrotalk;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.astrotalk.Adapters.AstrologerAdapter;
import com.example.astrotalk.adapter.BannerAdapter;
import com.example.astrotalk.models.Astrologer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    RecyclerView rvAstrologers;
    List<Astrologer> astrologerList = new ArrayList<>();
    AstrologerAdapter adapter;

    String url = "https://testing.trifrnd.net.in/ishwar/chat/chatUsers_fetch_api.php";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvAstrologers = view.findViewById(R.id.rvAstrologers);
        rvAstrologers.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        adapter = new AstrologerAdapter(astrologerList);
        rvAstrologers.setAdapter(adapter);

        loadAstrologers();  // 🔥 fetch data from API

        // banners
        ViewPager2 vpBanners = view.findViewById(R.id.vpBanners);
        vpBanners.setAdapter(new BannerAdapter(getDummyBanners()));

        return view;
    }

    private void loadAstrologers() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> parseResult(response),
                error -> Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(request);
    }

    private void parseResult(JSONArray response) {
        try {
            astrologerList.clear();
            for (int i = 0; i < response.length(); i++) {
                JSONObject object = response.getJSONObject(i);
                String userid = object.getString("userid");
                String name = object.getString("name");

                astrologerList.add(new Astrologer(userid, name));
            }

            adapter.notifyDataSetChanged(); // 🔥 update recyclerview
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int[] getDummyBanners() {
        return new int[]{
                R.drawable.ig_banner2,
                R.drawable.ig_banner
        };
    }
}
