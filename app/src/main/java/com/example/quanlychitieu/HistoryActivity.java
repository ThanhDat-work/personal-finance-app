package com.example.quanlychitieu;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.view.View;
import android.widget.AdapterView;
import java.util.Comparator;
import java.text.SimpleDateFormat;
import java.util.Date;
import androidx.appcompat.widget.SearchView;
import android.widget.EditText;
import android.widget.ImageView;
import android.graphics.Color;
import android.widget.Button; // Đạt nhớ thêm dòng này
import android.widget.LinearLayout; // Và dòng này
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class HistoryActivity extends AppCompatActivity {

    ListView lvHistory;
    Spinner spnMonth, spnType;
    androidx.appcompat.widget.SearchView searchView;
    String currentQuery = "";
    List<Transaction> allData = new ArrayList<>();
    List<Transaction> displayData = new ArrayList<>();
    TransactionAdapter adapter;
    LinearLayout layoutDeleteActions;
    Button btnCancel;
    Button btnSelectAll, btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        lvHistory = findViewById(R.id.lvHistory);
        spnMonth = findViewById(R.id.spnFilterMonth);
        spnType = findViewById(R.id.spnFilterType);
        searchView = findViewById(R.id.searchHistory);
        btnCancel = findViewById(R.id.btnCancel);
        btnSelectAll = findViewById(R.id.btnSelectAll);
        btnDelete = findViewById(R.id.btnDelete);
        layoutDeleteActions = findViewById(R.id.layoutDeleteActions);
        // --- SỬA LỖI MỜ TẠI ĐÂY ---
// 1. Tìm ô nhập chữ (Search Text)
        int searchEditTextId = searchView.getContext().getResources().getIdentifier("search_src_text", "id", getPackageName());
        EditText searchEditText = searchView.findViewById(searchEditTextId);
        if (searchEditText != null) {
            searchEditText.setTextColor(Color.BLACK);      // Chữ Đạt gõ vào sẽ đen đậm
            searchEditText.setHintTextColor(Color.BLACK);  // Chữ "Tìm kiếm..." cũng sẽ đen đậm cho rõ
        }

// 2. Tìm icon kính lúp (Search Icon)
        int searchIconId = searchView.getContext().getResources().getIdentifier("search_mag_icon", "id", getPackageName());
        ImageView searchIcon = searchView.findViewById(searchIconId);
        if (searchIcon != null) {
            searchIcon.setImageResource(R.drawable.ic_search);
            searchIcon.setColorFilter(Color.BLACK); // Ép kính lúp thành màu đen
        }

// 3. Tìm nút X xóa chữ (Close Icon)
        int closeIconId = searchView.getContext().getResources().getIdentifier("search_close_btn", "id", getPackageName());
        ImageView closeIcon = searchView.findViewById(closeIconId);
        if (closeIcon != null) {
            closeIcon.setColorFilter(Color.BLACK); // Ép nút X thành màu đen
        }
        setupSpinners();
        loadDataFromXML();

        adapter = new TransactionAdapter(this, displayData);
        lvHistory.setAdapter(adapter);
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText.toLowerCase(); // Lưu từ khóa tìm kiếm
                applyFilter(); // Chạy lại hàm lọc
                return true;
            }
        });
        // Lắng nghe sự kiện chọn Spinner để lọc
        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilter();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spnMonth.setOnItemSelectedListener(filterListener);
        spnType.setOnItemSelectedListener(filterListener);

        lvHistory.setOnItemLongClickListener((parent, view, position, id) -> {
            if (!adapter.isSelectionMode) {
                adapter.isSelectionMode = true; // Bật chế độ chọn
                // Lấy đúng item vừa nhấn giữ và tích chọn nó luôn
                Transaction selectedItem = displayData.get(position);
                selectedItem.isSelected = true;

                if (layoutDeleteActions != null) {
                    layoutDeleteActions.setVisibility(View.VISIBLE);
                }
                adapter.notifyDataSetChanged(); // Vẽ lại danh sách để hiện CheckBox
            }
            return true; // Trả về true để không bị dính sự kiện click thường
        });

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> {
                exitSelectionMode();
            });
        }
        btnSelectAll.setOnClickListener(v -> {
            for (Transaction t : displayData) {
                t.isSelected = true;
            }
            adapter.notifyDataSetChanged();
        });
        btnDelete.setOnClickListener(v -> {
            // 1. Đếm thử xem người dùng đã chọn bao nhiêu cái để báo ra màn hình
            int selectedCount = 0;
            for (Transaction t : allData) {
                if (t.isSelected) selectedCount++;
            }

            if (selectedCount == 0) {
                Toast.makeText(HistoryActivity.this, "Bạn chưa chọn giao dịch nào!", Toast.LENGTH_SHORT).show();
                return; // Dừng lại luôn, không làm gì tiếp
            }

            // 2. BẬT HỘP THOẠI XÁC NHẬN LÊN
            new AlertDialog.Builder(HistoryActivity.this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn xóa " + selectedCount + " giao dịch này?\nSố dư hiện tại sẽ được cập nhật lại.")
                    .setPositiveButton("Xác nhận", (dialog, which) -> {

                        // --- NẾU BẤM XÓA: BÊ NGUYÊN LOGIC CŨ VÀO ĐÂY ---
                        SharedPreferences pref = getSharedPreferences("UserDailyData", MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();

                        long totalBalance = pref.getLong("total_balance", 0);
                        long savingsBalance = pref.getLong("total_savings", 0);
                        long debtBalance = pref.getLong("total_debt", 0);

                        String allIds = pref.getString("all_record_ids", "");
                        List<String> idList = new ArrayList<>();
                        if (!allIds.isEmpty()) {
                            String[] idsArray = allIds.split(",");
                            for (String id : idsArray) {
                                if (!id.trim().isEmpty()) idList.add(id);
                            }
                        }

                        List<Transaction> itemsToRemove = new ArrayList<>();

                        for (Transaction t : allData) {
                            if (t.isSelected) {
                                itemsToRemove.add(t);
                                long amount = parseAmount(t.amount);

                                if (t.type.equals("+")) {
                                    totalBalance -= amount;
                                    if (t.service.equalsIgnoreCase("Mượn tiền")) {
                                        debtBalance -= amount;
                                    }
                                } else if (t.type.startsWith("-")) {
                                    if (t.source.equalsIgnoreCase("Tiền tiết kiệm") || t.source.equalsIgnoreCase("Tiết kiệm")) {
                                        savingsBalance += amount;
                                    } else {
                                        totalBalance += amount;
                                    }
                                    if (t.service.equalsIgnoreCase("Trả nợ")) {
                                        debtBalance += amount;
                                    }
                                } else if (t.type.equals("*") || t.type.equals("-*")) {
                                    savingsBalance -= amount;
                                }

                                String recordId = "REC_" + t.timestamp;
                                idList.remove(recordId);
                                editor.remove(recordId);
                            }
                        }

                        // Lưu lại file
                        StringBuilder sb = new StringBuilder();
                        for (String id : idList) {
                            if (!id.trim().isEmpty()) {
                                sb.append(id).append(",");
                            }
                        }
                        String newIdsString = sb.toString();

                        // Lưu lại: Bây giờ nó luôn là "ID1,ID2,ID3," y hệt ý Đạt lúc đầu
                        editor.putString("all_record_ids", newIdsString);
                        editor.putLong("total_balance", totalBalance);
                        editor.putLong("total_savings", savingsBalance);
                        editor.putLong("total_debt", debtBalance);
                        editor.apply();

                        // Cập nhật giao diện
                        allData.removeAll(itemsToRemove);
                        exitSelectionMode();
                        applyFilter();

                        Toast.makeText(HistoryActivity.this, "Đã xóa " + itemsToRemove.size() + " giao dịch!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> {
                        // --- NẾU BẤM HỦY: CHỈ TẮT HỘP THOẠI VÀ KHÔNG LÀM GÌ CẢ ---
                        dialog.dismiss();
                    })
                    .show(); // Hiển thị cái bảng thông báo lên
        });
    }

    private void setupSpinners() {
        // Lưu ý: Đạt nhớ đảm bảo Spinner chọn tháng phải khớp format (ví dụ 01 hay 1)
        String[] months = {"Tháng: Tất cả", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
        String[] types = {"Giao dịch: Tất cả", "Gửi tiền (+)", "Rút tiền (-)", "Tiết kiệm (*)"};

        spnMonth.setAdapter(new ArrayAdapter<>(this, R.layout.item_spinner, months));
        spnType.setAdapter(new ArrayAdapter<>(this, R.layout.item_spinner, types));
    }

    private void loadDataFromXML() {
        SharedPreferences pref = getSharedPreferences("UserDailyData", MODE_PRIVATE);

        // Đọc danh sách ID (đã được dọn dẹp sạch sẽ ở bước trước)
        String allIds = pref.getString("all_record_ids", "");

        if (allIds.isEmpty()) {
            allData.clear();
            displayData.clear();
            if (adapter != null) adapter.notifyDataSetChanged();
            return;
        }

        // Tách các ID bằng dấu phẩy
        String[] ids = allIds.split(",");
        allData.clear();

        for (String id : ids) {
            if (id.trim().isEmpty()) continue;

            String rawData = pref.getString(id, "");
            if (!rawData.isEmpty()) {
                String[] p = rawData.split("\\|");

                // Lấy timestamp từ ID
                long ts = 0;
                try {
                    ts = Long.parseLong(id.replace("REC_", ""));
                } catch (Exception e) { ts = 0; }

                // Xử lý nguồn tiền (giữ logic p.length > 5 của Đạt)
                String source = "Tiền tiêu dùng";
                if (p.length > 5) {
                    source = p[5];
                } else if (p.length > 4) {
                    // Suy luận từ type nếu là dữ liệu đời cũ
                    if (p[4].equals("*") || p[4].equals("-*")) {
                        source = "Tiền tiết kiệm";
                    }
                }

                // p[0]: date, p[1]: amount, p[2]: service, p[3]: content, p[4]: type
                if (p.length >= 5) {
                    allData.add(new Transaction(p[0], p[1], p[2], p[3], p[4], source, ts));
                }
            }
        }

        // SẮP XẾP: Ngày mới nhất lên đầu, cùng ngày thì ai nhập sau lên trước
        Collections.sort(allData, new Comparator<Transaction>() {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            @Override
            public int compare(Transaction t1, Transaction t2) {
                try {
                    Date d1 = sdf.parse(t1.date);
                    Date d2 = sdf.parse(t2.date);
                    int result = d2.compareTo(d1);
                    if (result == 0) {
                        return Long.compare(t2.timestamp, t1.timestamp);
                    }
                    return result;
                } catch (Exception e) {
                    return 0;
                }
            }
        });

        // Cập nhật lên giao diện
        displayData.clear();
        displayData.addAll(allData);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void applyFilter() {
        String selectedMonthStr = spnMonth.getSelectedItem().toString();
        String selectedType = spnType.getSelectedItem().toString();

        // Lấy số tháng (ví dụ: "Tháng: Tất cả" -> "All", "01" -> "01")
        String filterMonth = selectedMonthStr.equals("Tháng: Tất cả") ? "All" : selectedMonthStr;

        displayData.clear();
        for (Transaction t : allData) {
            // 1. Logic lọc tháng chuẩn hơn (tránh trường hợp tháng 1 dính tháng 11, 12)
            boolean matchMonth = false;
            if (filterMonth.equals("All")) {
                matchMonth = true;
            } else {
                // Kiểm tra xem chuỗi ngày có chứa "/01/" hoặc "/1/" không
                // Đạt nên nhập ngày chuẩn 01/01/2026 thì lọc sẽ chính xác nhất
                matchMonth = t.date.contains("/" + filterMonth + "/");
            }

            // 2. Logic lọc loại
            boolean matchType = false;
            if (selectedType.equals("Giao dịch: Tất cả")) {
                matchType = true;
            } else if (selectedType.contains("+") && t.type.equals("+")) {
                matchType = true;
            } else if (selectedType.contains("-") && t.type.startsWith("-")) {
                // Lấy cả "-+" (tiêu dùng) và "-*" (tiết kiệm)
                matchType = true;
            } else if (selectedType.contains("*") && t.type.equals("*")) {
                matchType = true;
            }

            // 3. Lọc tìm kiếm (MỚI)
            // Kiểm tra xem từ khóa có nằm trong Content hoặc Service không
            boolean matchSearch = true;
            if (!currentQuery.isEmpty()) {
                String content = t.content.toLowerCase();
                String service = t.service.toLowerCase();
                matchSearch = content.contains(currentQuery) || service.contains(currentQuery);
            }

            if (matchMonth && matchType && matchSearch) {
                displayData.add(t);
            }
        }

        // Vì allData đã được sắp xếp chuẩn ở trên, nên khi lọc xong displayData vẫn giữ đúng thứ tự đó
        adapter.notifyDataSetChanged();
    }
    public void exitSelectionMode() {
        adapter.isSelectionMode = false;
        for (Transaction t : displayData) {
            t.isSelected = false; // Reset toàn bộ trạng thái chọn
        }
        if (layoutDeleteActions != null) {
            layoutDeleteActions.setVisibility(View.GONE);
        } // Ẩn thanh công cụ
        adapter.notifyDataSetChanged(); // Vẽ lại danh sách
    }

    private long parseAmount(String amountStr) {
        try {
            // Xóa tất cả các ký tự không phải là số
            String cleanStr = amountStr.replaceAll("[^0-9]", "");
            return Long.parseLong(cleanStr);
        } catch (Exception e) {
            return 0;
        }
    }
}