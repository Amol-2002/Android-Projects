package com.example.studentdairy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    // Link to the APK (or GitHub/release). You already used a Drive link — keep it here.
    private static final String GITHUB_LINK = "https://drive.google.com/file/d/1ovVU8AE4cswcdyqrHVS6ki0ElQmQBnY0/view?usp=drivesdk";

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Back arrow -> MainActivity
        ImageView attarrow = view.findViewById(R.id.attarrow);
        attarrow.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        });

   



        // ================= privacy policy  Card =================//
        LinearLayout privacypolicyCard= view.findViewById(R.id.card_privacy_policy);
        privacypolicyCard.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new PrivacyPolicyFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // ================= support  Card =================//
        LinearLayout supportCard= view.findViewById(R.id.card_support);
        supportCard.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new SupportFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // ================= change Password  Card =================//
        LinearLayout changepasswordCard= view.findViewById(R.id.card_change_password);
        changepasswordCard.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new UpdatePasswordFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // ================= Share App Card =================
        LinearLayout shareAppCard = view.findViewById(R.id.card_share_app);
        shareAppCard.setOnClickListener(v -> {
            // Use Drive link (or change to Play Store link if published)
            String urlToShare = GITHUB_LINK;
            // Example Play Store link:
            // urlToShare = "https://play.google.com/store/apps/details?id=" + requireContext().getPackageName();

            openShareChooser(urlToShare);
        });

        // ================= Log Out Card =================
        LinearLayout logoutCard = view.findViewById(R.id.card_logout);
        logoutCard.setOnClickListener(v -> performLogout());

        return view;
    }

    /**
     * Opens Android share chooser with the provided link and a short message.
     */
    private void openShareChooser(String url) {
        if (getContext() == null) return;

        String message = "Hi — check Student Dairy app by Trifrnd PVT.LTD : " + url +
                "\n\nYou can download this file to open the app.";

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Student Dairy - App Link");
        sendIntent.setType("text/plain");

        Intent chooser = Intent.createChooser(sendIntent, "Share via");
        try {
            startActivity(chooser);
        } catch (Exception e) {
            // If something goes wrong, show a friendly message
            Toast.makeText(getContext(), "No app available to share the link", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Perform logout: clear SessionManager + app SharedPreferences and go to LoginActivity.
     */
    private void performLogout() {
        if (getContext() == null) return;

        // Clear SessionManager (if you're using it)
        try {
            SessionManager session = new SessionManager(requireContext());
            session.clearSession();
        } catch (Exception ignored) {}

        // Clear Student login session
        SharedPreferences studentPrefs = requireActivity()
                .getSharedPreferences("StudentProfile", requireActivity().MODE_PRIVATE);
        studentPrefs.edit().clear().apply();

        // Clear Parent profile session
        SharedPreferences parentPrefs = requireActivity()
                .getSharedPreferences("ParentProfile", requireActivity().MODE_PRIVATE);
        parentPrefs.edit().clear().apply();

        // Clear timetable/homework session
        SharedPreferences timePrefs = requireActivity()
                .getSharedPreferences("Homework", requireActivity().MODE_PRIVATE);
        timePrefs.edit().clear().apply();

        // Clear Timetable prefs too (you used this name earlier)
        SharedPreferences ttPrefs = requireActivity()
                .getSharedPreferences("Timetable", requireActivity().MODE_PRIVATE);
        ttPrefs.edit().clear().apply();

        // Go to LoginActivity
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Close current activity
        requireActivity().finish();
    }

    /**
     * Optional helper: Share directly to WhatsApp (if installed).
     */
    @SuppressWarnings("unused")
    private void shareToWhatsApp(String url) {
        if (getContext() == null) return;
        String message = "Check my app: " + url;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.setPackage("com.whatsapp");
        try {
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getContext(), "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Optional helper: Try to share to Instagram (text-only may not be accepted by some IG versions).
     */
    @SuppressWarnings("unused")
    private void shareToInstagram(String url) {
        if (getContext() == null) return;
        String message = "Check my app repo: " + url;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.setPackage("com.instagram.android");
        try {
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getContext(), "Instagram not installed or doesn't accept text sharing", Toast.LENGTH_SHORT).show();
        }
    }
}
