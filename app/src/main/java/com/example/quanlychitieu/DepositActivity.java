package com.example.quanlychitieu;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class DepositActivity extends AppCompatActivity {

    EditText edtAmount, edtDate, edtContent;
    Spinner spnService;
    Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit);

        // 1. Ánh xạ
        edtAmount = findViewById(R.id.edtAmount);
        edtDate = findViewById(R.id.edtDate);
        edtContent = findViewById(R.id.edtContent);
        spnService = findViewById(R.id.spnService);
        btnConfirm = findViewById(R.id.btnConfirm);

        // 2. Thiết lập danh sách dịch vụ (Spinner)
        String[] services = {"Lương", "Tiền trả nợ", "Mượn tiền", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, services);
        adapter.setDropDownViewResource(R.layout.item_spinner);
        spnService.setAdapter(adapter);

        // 3. Chọn ngày gửi
        edtDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                // 1. Set ngày vào Calendar
                c.set(year, month, day);

                // 2. Dùng SimpleDateFormat để ép kiểu dd/MM/yyyy
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());

                // 3. Hiển thị lên EditText
                edtDate.setText(sdf.format(c.getTime()));

            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // 4. Nút xác nhận
        btnConfirm.setOnClickListener(v -> {
            String amount = edtAmount.getText().toString().trim();
            String date = edtDate.getText().toString().trim();
            String content = edtContent.getText().toString().trim();

            if (amount.isEmpty() || date.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            } else {
                showConfirmDialog();
            }
        });
    }

    private void showConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận thông tin");
        builder.setMessage("Bạn chắc chắn với những thông tin trên chưa?");

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            saveToXML("+");
            finish();
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void saveToXML(String type) {
        SharedPreferences pref = getSharedPreferences("UserDailyData", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        String sAmount = edtAmount.getText().toString().trim();
        String date = edtDate.getText().toString().trim();
        String service = spnService.getSelectedItem().toString();
        String content = edtContent.getText().toString().trim();

        long amountValue = Long.parseLong(sAmount);

        // 1. Cập nhật Số Dư Tổng (Balance)
        long currentBalance = pref.getLong("total_balance", 0);
        long newBalance = currentBalance + amountValue; // Gửi tiền luôn là cộng vào balance
        editor.putLong("total_balance", newBalance);

        // 2. Xử lí tiền nợ
        if (service.equals("Mượn tiền")) {
            long currentDebt = pref.getLong("total_debt", 0);
            long newDebt = currentDebt + amountValue; // Mượn thêm -> Tăng nợ
            editor.putLong("total_debt", newDebt);
            Toast.makeText(this, "Đã ghi nhận nợ mới! Tổng nợ: " + newDebt + " VND", Toast.LENGTH_SHORT).show();
        }

        // 3. Lưu bản ghi lịch sử
        String recordId = "REC_" + System.currentTimeMillis();
        String recordData = date + "|" + sAmount + "|" + service + "|" + content + "|" + type;
        editor.putString(recordId, recordData);

        String oldIds = pref.getString("all_record_ids", "");
        editor.putString("all_record_ids", oldIds + recordId + ",");

        editor.apply();

        Toast.makeText(this, "Đã lưu! Số dư hiện tại: " + newBalance + " VND", Toast.LENGTH_SHORT).show();
    }
}
