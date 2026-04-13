package com.example.quanlychitieu;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

// --- CÁC IMPORT CẦN THIẾT CHO BIỂU ĐỒ ---
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend; // DÒNG QUAN TRỌNG NHẤT ĐỂ SỬA LỖI
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity {
    Spinner spnStatType, spnStatMonth;
    BarChart barChart;
    PieChart pieChart;
    TextView txtError;
    List<Transaction> allTransactions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        spnStatType = findViewById(R.id.spnStatType);
        spnStatMonth = findViewById(R.id.spnStatMonth);
        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);
        txtError = findViewById(R.id.txtError);

        setupSpinners();
        loadDataFromPrefs();
        autoFixAllHistoryDates();

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateUI();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spnStatType.setOnItemSelectedListener(listener);
        spnStatMonth.setOnItemSelectedListener(listener);
    }

    private String normalizeDate(String date) {
        try {
            String[] p = date.split("/");
            if (p.length == 3) {
                int d = Integer.parseInt(p[0]);
                int m = Integer.parseInt(p[1]);
                int y = Integer.parseInt(p[2]);
                // Ép về định dạng 02d (luôn có 2 chữ số)
                return String.format("%02d/%02d/%04d", d, m, y);
            }
        } catch (Exception e) {
            return date; // Nếu lỗi thì giữ nguyên để tránh mất dữ liệu
        }
        return date;
    }
    private void setupSpinners() {
        String[] types = {"Thu/Chi/Tiết kiệm", "Thống kê Thu", "Thống kê Chi"};
        spnStatType.setAdapter(new ArrayAdapter<>(this, R.layout.item_spinner, types));

        String[] months = {"Tất cả", "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"};
        spnStatMonth.setAdapter(new ArrayAdapter<>(this, R.layout.item_spinner, months));
    }

    private void loadDataFromPrefs() {
        allTransactions.clear();
        SharedPreferences pref = getSharedPreferences("UserDailyData", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String allIds = pref.getString("all_record_ids", "");
        if (allIds.isEmpty()) return;

        String[] ids = allIds.split(",");
        boolean isDataChanged = false;
        for (String id : ids) {
            if (id.trim().isEmpty()) continue;
            String rawData = pref.getString(id, "");
            if (!rawData.isEmpty()) {
                String[] p = rawData.split("\\|");
                String originalDate = p[0];
                String fixedDate = normalizeDate(originalDate);

                if (!originalDate.equals(fixedDate)) {
                    p[0] = fixedDate;
                    // Ghép lại chuỗi: date|amount|service|content|type|source
                    String newRawData = String.join("|", p);
                    editor.putString(id, newRawData);
                    isDataChanged = true;
                }

                try {
                    long ts = Long.parseLong(id.replace("REC_", ""));
                    allTransactions.add(new Transaction(
                            p[0], p[1], p[2], p[3], p[4],
                            (p.length > 5 ? p[5] : "Tiền tiêu dùng"),
                            ts
                    ));
                } catch (Exception e) {}
            }
        }
        if (isDataChanged) {
            editor.apply(); // Lưu vĩnh viễn những ngày đã sửa vào file
        }
    }

    private void updateUI() {
        int typePos = spnStatType.getSelectedItemPosition();
        int monthPos = spnStatMonth.getSelectedItemPosition();

        if (typePos == 0) {
            showBarChart(monthPos);
        } else {
            showPieChart(typePos == 1 ? "+" : "-", monthPos);
        }
    }

    private void showBarChart(int month) {
        // ẨN NGAY biểu đồ tròn để không bị đè
        pieChart.setVisibility(View.GONE);

        float thu = 0, chi = 0, tk = 0;
        boolean hasData = false;

        for (Transaction t : allTransactions) {
            try {
                int tMonth = Integer.parseInt(t.date.split("/")[1]);
                if (month == 0 || tMonth == month) {
                    hasData = true;
                    float val = Float.parseFloat(t.amount) / 1000f; // Đơn vị nghìn đồng
                    if (t.type.equals("+")) thu += val;
                    else if (t.type.startsWith("-")) chi += val;
                    else if (t.type.equals("*")) tk += val;
                }
            } catch (Exception e) {}
        }

        if (!hasData) {
            txtError.setText("Không có dữ liệu!");
            txtError.setVisibility(View.VISIBLE);
            barChart.setVisibility(View.GONE);
            return;
        }

        txtError.setVisibility(View.GONE);
        barChart.setVisibility(View.VISIBLE); // HIỆN biểu đồ cột

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(1, thu));
        entries.add(new BarEntry(2, chi));
        entries.add(new BarEntry(3, tk));

        BarDataSet dataSet = new BarDataSet(entries, "Thu (+) | Chi (-) | Tiết kiệm (*)");

        // --- SET MÀU CHUẨN THEO ĐÚNG THỨ TỰ THU - CHI - TK ---
        dataSet.setColors(new int[]{
                Color.parseColor("#2196F3"), // Xanh dương (Gửi +)
                Color.parseColor("#F44336"), // Đỏ (Rút -)
                Color.parseColor("#FFEB3B")  // Vàng (Tiết kiệm *)
        });

        dataSet.setValueTextSize(12f);
        BarData data = new BarData(dataSet);
        barChart.setData(data);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    private void showPieChart(String type, int month) {
        // ẨN NGAY biểu đồ cột để không bị đè
        barChart.setVisibility(View.GONE);

        // Mảng màu cho các dịch vụ (Ăn uống, xe cộ...)
        int[] myColors = {
                Color.parseColor("#2ECC71"), Color.parseColor("#F1C40F"), Color.parseColor("#E74C3C"),
                Color.parseColor("#3498DB"), Color.parseColor("#9B59B6"), Color.parseColor("#E67E22"),
                Color.parseColor("#1ABC9C"), Color.parseColor("#34495E"), Color.parseColor("#FF80AB")
        };
        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : myColors) colors.add(c);

        Map<String, Float> map = new HashMap<>();
        for (Transaction t : allTransactions) {
            try {
                int tMonth = Integer.parseInt(t.date.split("/")[1]);
                if (month == 0 || tMonth == month) {
                    if ((type.equals("+") && t.type.equals("+")) || (type.equals("-") && t.type.startsWith("-"))) {
                        map.put(t.service, map.getOrDefault(t.service, 0f) + Float.parseFloat(t.amount));
                    }
                }
            } catch (Exception e) {}
        }

        if (map.isEmpty()) {
            txtError.setText("Không có dữ liệu!");
            txtError.setVisibility(View.VISIBLE);
            pieChart.setVisibility(View.GONE);
            return;
        }

        txtError.setVisibility(View.GONE);
        pieChart.setVisibility(View.VISIBLE); // HIỆN biểu đồ tròn

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> e : map.entrySet()) {
            entries.add(new PieEntry(e.getValue(), e.getKey()));
        }

        PieDataSet set = new PieDataSet(entries, "");
        set.setColors(colors);
        set.setSliceSpace(3f);
        set.setValueTextSize(14f);
        set.setValueTextColor(Color.BLACK);

        pieChart.setData(new PieData(set));
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText(type.equals("+") ? "Tổng Thu" : "Tổng Chi");

        // Legend (Chú thích)
        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setWordWrapEnabled(true);

        pieChart.animateXY(800, 800);
        pieChart.invalidate();
    }
    // Hàm 1: Công cụ nắn lại định dạng ngày
    private String fixDateFormat(String date) {
        try {
            String[] parts = date.split("/");
            if (parts.length == 3) {
                int day = Integer.parseInt(parts[0].trim());
                int month = Integer.parseInt(parts[1].trim());
                int year = Integer.parseInt(parts[2].trim());
                return String.format("%02d/%02d/%04d", day, month, year);
            }
        } catch (Exception e) { return date; }
        return date;
    }

    // Hàm 2: Quét toàn bộ kho dữ liệu để sửa
    private void autoFixAllHistoryDates() {
        SharedPreferences pref = getSharedPreferences("UserDailyData", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String allIds = pref.getString("all_record_ids", "");
        if (allIds.isEmpty()) return;

        String[] ids = allIds.split(",");
        for (String id : ids) {
            String recordId = id.trim();
            if (recordId.isEmpty()) continue;
            String rawData = pref.getString(recordId, "");
            if (!rawData.isEmpty()) {
                String[] p = rawData.split("\\|");
                String oldDate = p[0];
                String newDate = fixDateFormat(oldDate);
                if (!oldDate.equals(newDate)) {
                    p[0] = newDate;
                    String newRawData = String.join("|", p);
                    editor.putString(recordId, newRawData);
                }
            }
        }
        editor.apply(); // Chốt hạ lưu vào máy
    }
}