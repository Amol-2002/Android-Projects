package com.example.classteacherportal;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddhomeworkFragment extends Fragment {

    // Add homework API
    private static final String HOMEWORK_ADD_URL =
            "https://testing.trifrnd.net.in/ishwar/school/api/homework_add_api.php";

    // Teacher profile API (to get class_id & section_id)
    private static final String TEACHER_PROFILE_URL =
            "https://testing.trifrnd.net.in/ishwar/school/api/teacher_profile_api.php";

    private TextView tvClassName, tvSectionName, tvSelectedFileName, tvUploadedBy;
    private Spinner spSubject;
    private EditText etHwDate;
    private Button btnChooseFile, btnAddHomework, btnBack;

    private Uri selectedFileUri;
    private byte[] fileBytes;
    private String fileName;

    private ProgressDialog progressDialog;
    private RequestQueue requestQueue;

    private ActivityResultLauncher<android.content.Intent> filePickerLauncher;

    // From login SharedPrefs (userid == staff_id)
    private String staffId = "";

    // From teacher_profile_api.php
    private String classId = "";
    private String sectionId = "";
    private String teacherName = "";

    public AddhomeworkFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1️⃣ Get userid from StudentProfile prefs -> this is our staff_id
        SharedPreferences studentPrefs =
                requireContext().getSharedPreferences("StudentProfile", Context.MODE_PRIVATE);
        staffId = studentPrefs.getString("userid", "");  // staff_id == userid

        requestQueue = Volley.newRequestQueue(requireContext());

        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait...");

        // File picker launcher
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK
                            && result.getData() != null) {
                        selectedFileUri = result.getData().getData();
                        if (selectedFileUri != null) {
                            fileName = getFileNameFromUri(selectedFileUri);
                            if (tvSelectedFileName != null) {
                                tvSelectedFileName.setText(fileName);
                            }

                            try {
                                fileBytes = readBytesFromUri(selectedFileUri);
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(requireContext(),
                                        "Failed to read file", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_addhomework, container, false);

        // Find the ImageView
        ImageView attarrow = view.findViewById(R.id.attarrow);

        // Click → Intent to MainActivity
        attarrow.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        });

        tvClassName = view.findViewById(R.id.tvClassName);
        tvSectionName = view.findViewById(R.id.tvSectionName);
        spSubject = view.findViewById(R.id.spSubject);
        etHwDate = view.findViewById(R.id.etHwDate);
        btnChooseFile = view.findViewById(R.id.btnChooseFile);
        tvSelectedFileName = view.findViewById(R.id.tvSelectedFileName);
        tvUploadedBy = view.findViewById(R.id.tvUploadedBy);
        btnAddHomework = view.findViewById(R.id.btnAddHomework);
        btnBack = view.findViewById(R.id.btnBack);

        // Show staffId for info (will replace with teacher_name after API call)
        tvUploadedBy.setText("Staff ID: " + staffId);

        setupSubjectSpinner();
        setupDatePicker();
        setupClickListeners();

        if (!TextUtils.isEmpty(staffId)) {
            fetchTeacherProfile();
        } else {
            Toast.makeText(requireContext(),
                    "Staff ID missing. Please login again.",
                    Toast.LENGTH_LONG).show();
        }

        return view;
    }

    // 🔹 Call teacher_profile_api.php to get class_id, section_id, teacher_name
    private void fetchTeacherProfile() {
        progressDialog.setMessage("Loading teacher profile...");
        progressDialog.show();

        StringRequest request = new StringRequest(
                Request.Method.POST,
                TEACHER_PROFILE_URL,
                response -> {
                    progressDialog.dismiss();
                    parseTeacherProfileResponse(response);
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(),
                            "Failed to load teacher profile: " + error.getMessage(),
                            Toast.LENGTH_LONG).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // API requires "staff_id"
                params.put("staff_id", staffId);
                return params;
            }
        };

        requestQueue.add(request);
    }

    // 🔹 Example response:
    // [
    //   {
    //     "user_id": "108",
    //     "teacher_name": "Atul Sing  Rajputn",
    //     "departments": "Sanskrit",
    //     "designation": "Teacher",
    //     "joining_date": "2024-09-02",
    //     "phone": "9887887678",
    //     "gender": "female",
    //     "state": "Maharashtra",
    //     "city": "Pune",
    //     "present_address": "pune",
    //     "permanent_address": "pune",
    //     "class_id": "7",
    //     "section_id": "A"
    //   }
    // ]
    private void parseTeacherProfileResponse(String response) {
        try {
            JSONArray arr = new JSONArray(response);
            if (arr.length() > 0) {
                JSONObject obj = arr.getJSONObject(0);

                teacherName = obj.optString("teacher_name", "");
                classId = obj.optString("class_id", "");
                sectionId = obj.optString("section_id", "");

                // Update UI labels
                if (!TextUtils.isEmpty(teacherName)) {
                    tvUploadedBy.setText(teacherName);
                } else {
                    tvUploadedBy.setText("Staff ID: " + staffId);
                }

                tvClassName.setText("" + classId);
                tvSectionName.setText("" + sectionId);
            } else {
                Toast.makeText(requireContext(),
                        "No teacher profile found for this staff ID",
                        Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(),
                    "Invalid teacher profile response",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void setupSubjectSpinner() {
        String[] subjects = new String[]{
                "Select Subject",
                "Marathi",
                "Hindi",
                "English",
                "Maths",
                "Science",
                "History",
                "Geography",
                "Civics",
                "Chemistry",
                "Physics",
                "Sanskrit"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                subjects
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSubject.setAdapter(adapter);
    }

    private void setupDatePicker() {
        etHwDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    requireContext(),
                    (DatePicker view, int y, int m, int d) -> {
                        // Format yyyy-MM-dd (change if API wants dd-MM-yyyy)
                        String formatted = String.format("%04d-%02d-%02d",
                                y, (m + 1), d);
                        etHwDate.setText(formatted);
                    },
                    year, month, day
            );
            dialog.show();
        });
    }

    private void setupClickListeners() {
        btnChooseFile.setOnClickListener(v -> openFilePicker());

        btnAddHomework.setOnClickListener(v -> {
            if (validateForm()) {
                uploadHomework();
            }
        });

        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void openFilePicker() {
        android.content.Intent intent = new android.content.Intent(
                android.content.Intent.ACTION_GET_CONTENT);
        intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
        intent.setType("*/*"); // or "application/pdf"
        filePickerLauncher.launch(intent);
    }

    private boolean validateForm() {
        String hwDate = etHwDate.getText().toString().trim();

        if (TextUtils.isEmpty(staffId)) {
            Toast.makeText(requireContext(),
                    "Staff ID missing. Please login again.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(classId) || TextUtils.isEmpty(sectionId)) {
            Toast.makeText(requireContext(),
                    "Class / Section not loaded yet.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (spSubject.getSelectedItemPosition() == 0) {
            Toast.makeText(requireContext(),
                    "Please select subject", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(hwDate)) {
            Toast.makeText(requireContext(),
                    "Please select homework date", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (fileBytes == null) {
            Toast.makeText(requireContext(),
                    "Please choose file", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void uploadHomework() {
        progressDialog.setMessage("Adding homework...");
        progressDialog.show();

        VolleyMultipartRequest request = new VolleyMultipartRequest(
                Request.Method.POST,
                HOMEWORK_ADD_URL,
                response -> {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(),
                            "Homework added successfully", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(),
                            "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                // From prefs & teacher profile API
                params.put("staff_id", staffId);
                params.put("class_id", classId);
                params.put("section_id", sectionId);

                params.put("subject", spSubject.getSelectedItem().toString());
                params.put("hw_date", etHwDate.getText().toString().trim());

                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                if (fileBytes != null && fileName != null) {
                    params.put("file_name", new DataPart(fileName, fileBytes));
                }
                return params;
            }
        };

        requestQueue.add(request);
    }

    // ---------- Helper methods ----------

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            ContentResolver resolver = requireContext().getContentResolver();
            Cursor cursor = resolver.query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (idx >= 0) {
                        result = cursor.getString(idx);
                    }
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private byte[] readBytesFromUri(Uri uri) throws IOException {
        ContentResolver resolver = requireContext().getContentResolver();
        InputStream inputStream = resolver.openInputStream(uri);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        if (inputStream == null) return null;

        int nRead;
        byte[] data = new byte[4096];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        inputStream.close();
        return buffer.toByteArray();
    }
}
