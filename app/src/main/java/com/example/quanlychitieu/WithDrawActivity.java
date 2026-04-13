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
import android.widget.RadioGroup;

public class WithDrawActivity extends AppCompatActivity {
    // 1. Khai báo thêm ở đầu class
    RadioGroup rgSource;

    // 2. Ánh xạ trong onCreate

    EditText edtAmount, edtDate, edtContent;
    Spinner spnService;
    Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_draw);

        // 1. Ánh xạ
        rgSource = findViewById(R.id.rgSource);
        edtAmount = findViewById(R.id.edtAmount);
        edtDate = findViewById(R.id.edtDate);
        edtContent = findViewById(R.id.edtContent);
        spnService = findViewById(R.id.spnService);
        btnConfirm = findViewById(R.id.btnConfirm);

        // 2. Thiết lập danh sách dịch vụ (Spinner)
        // Lưu ý: Chữ "Trả nợ" phải viết đúng từng chữ để code nhận diện được
        String[] services = {"Ăn uống", "Xe cộ", "Sửa chữa", "Mua sắm", "Phí", "Trả nợ", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, services);
        adapter.setDropDownViewResource(R.layout.item_spinner);
        spnService.setAdapter(adapter);

        // 3. Chọn ngày gửi (Hiện Calendar)
        edtDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                // 1. Set ngày vào Calendar
                c.set(year, month, day);

                // 2. Dùng SimpleDateFormat để ép kiểu dd/MM/yyyy
                // "dd" là 2 số ngày, "MM" là 2 số tháng (M viết hoa)
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());

                // 3. Hiển thị lên EditText
                edtDate.setText(sdf.format(c.getTime()));

            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // 4. Nút xác nhận & Lưu XML
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
            saveToXML("-");
            // Toast báo thành công đã có trong hàm saveToXML rồi
            finish();
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    // 3. Sửa lại hàm saveToXML
    private void saveToXML(String ignoredType) { // Bỏ qua biến type cũ
        SharedPreferences pref = getSharedPreferences("UserDailyData", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        String sAmount = edtAmount.getText().toString().trim();
        String date = edtDate.getText().toString().trim();
        String service = spnService.getSelectedItem().toString();
        String content = edtContent.getText().toString().trim();
        long amountValue = Long.parseLong(sAmount);

        // Xử lý nguồn tiền và ký hiệu mới
        String finalType = "";
        String sourceName = "";

        if (rgSource.getCheckedRadioButtonId() == R.id.rbSpending) {
            // NGUỒN TIÊU DÙNG
            long currentBalance = pref.getLong("total_balance", 0);
            if (amountValue > currentBalance) {
                Toast.makeText(this, "Tiền tiêu dùng không đủ!", Toast.LENGTH_SHORT).show();
                return;
            }
            editor.putLong("total_balance", currentBalance - amountValue);
            finalType = "-+"; // Ký hiệu rút tiền tiêu dùng
            sourceName = "Tiền tiêu dùng";
        } else {
            // NGUỒN TIẾT KIỆM
            long currentSavings = pref.getLong("total_savings", 0);
            if (amountValue > currentSavings) {
                Toast.makeText(this, "Tiền tiết kiệm không đủ!", Toast.LENGTH_SHORT).show();
                return;
            }
            editor.putLong("total_savings", currentSavings - amountValue);
            finalType = "-*"; // Ký hiệu rút tiền tiết kiệm
            sourceName = "Tiền tiết kiệm";
        }

        // Xử lý nợ (giữ nguyên logic cũ của Đạt)
        if (service.equals("Trả nợ")) {
            long currentDebt = pref.getLong("total_debt", 0);
            editor.putLong("total_debt", Math.max(0, currentDebt - amountValue));
        }

        // 4. Lưu bản ghi lịch sử (Thêm sourceName vào cuối chuỗi | )
        String recordId = "REC_" + System.currentTimeMillis();
        String recordData = date + "|" + sAmount + "|" + service + "|" + content + "|" + finalType + "|" + sourceName;
        editor.putString(recordId, recordData);

        String oldIds = pref.getString("all_record_ids", "");
        editor.putString("all_record_ids", oldIds + recordId + ",");

        editor.apply();
        Toast.makeText(this, "Đã lưu giao dịch từ " + sourceName, Toast.LENGTH_SHORT).show();
    }
}