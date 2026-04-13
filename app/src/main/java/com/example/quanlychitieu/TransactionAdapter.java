package com.example.quanlychitieu;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

public class TransactionAdapter extends ArrayAdapter<Transaction> {
    private Context context;
    private List<Transaction> transactions;
    public boolean isSelectionMode = false;
    public TransactionAdapter(Context context, List<Transaction> objects) {
        super(context, 0, objects);
        this.context = context;
        this.transactions = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        }

        Transaction t = transactions.get(position);

        // 1. Ánh xạ (Thêm cái Service mới)
        TextView txtHeaderDate = convertView.findViewById(R.id.txtHeaderDate);
        LinearLayout layoutItem = convertView.findViewById(R.id.layoutItem);
        TextView txtService = convertView.findViewById(R.id.txtHistoryService); // MỚI
        TextView txtAmount = convertView.findViewById(R.id.txtHistoryAmount);
        TextView txtContent = convertView.findViewById(R.id.txtHistoryContent);
        TextView txtSource = convertView.findViewById(R.id.txtHistorySource);

        // 2. XỬ LÝ LỖI NGÀY THÁNG (FIXED)
        boolean showHeader = false;
        if (position == 0) {
            showHeader = true;
        } else {
            Transaction prevT = transactions.get(position - 1);
            if (!t.date.equals(prevT.date)) {
                showHeader = true;
            }
        }

        if (showHeader) {
            txtHeaderDate.setVisibility(View.VISIBLE);

            // --- LOGIC SỬA LỖI ---
            try {
                // Tách chuỗi bằng dấu gạch chéo
                String[] parts = t.date.split("/");
                // parts[0] là ngày, parts[1] là tháng. Ví dụ: 5/1/2026 -> lấy 5 và 1
                if (parts.length >= 2) {
                    txtHeaderDate.setText("Ngày " + parts[0] + "/" + parts[1]);
                } else {
                    txtHeaderDate.setText("Ngày " + t.date); // Fallback nếu lỗi
                }
            } catch (Exception e) {
                txtHeaderDate.setText("Ngày " + t.date);
            }

            // Màu header theo theme
            txtHeaderDate.setBackgroundColor(Color.parseColor("#8DA895"));
            txtHeaderDate.setTextColor(Color.BLACK);
        } else {
            txtHeaderDate.setVisibility(View.GONE);
        }

        // 3. ĐỔ DỮ LIỆU MỚI (Tách Service và Content)
        txtService.setText(t.service); // Dòng trên đậm
        txtContent.setText(t.content); // Dòng dưới thường

        // 4. Xử lý Nguồn tiền (Ẩn/Hiện)
        if (t.type.equals("*")) {
            txtSource.setVisibility(View.GONE);
        } else {
            txtSource.setVisibility(View.VISIBLE);
            txtSource.setText(t.source);
            if ("Tiền tiết kiệm".equals(t.source)) {
                txtSource.setTextColor(Color.parseColor("#FFEB3B"));
            } else {
                txtSource.setTextColor(Color.parseColor("#2196F3"));
            }
        }

        // 5. Định dạng tiền
        try {
            long value = Long.parseLong(t.amount);
            txtAmount.setText(String.format("%,d VND", value));
        } catch (Exception e) {
            txtAmount.setText(t.amount + " VND");
        }

        // 6. Đổ màu nền
        if (t.type.equals("+")) {
            layoutItem.setBackgroundColor(Color.parseColor("#3498DB"));
        }
        else if (t.type.startsWith("-")) {
            layoutItem.setBackgroundColor(Color.parseColor("#FF4C4C"));
        }
        else if (t.type.equals("*")) {
            layoutItem.setBackgroundColor(Color.parseColor("#FFD700"));
        }
        CheckBox cbSelect = convertView.findViewById(R.id.cbSelect);
        Transaction lst1 = getItem(position);

        // Xử lý hiển thị CheckBox
        if (isSelectionMode) {
            cbSelect.setVisibility(View.VISIBLE);
            cbSelect.setChecked(lst1.isSelected);
        } else {
            cbSelect.setVisibility(View.GONE);
        }

        // Khi nhấn vào CheckBox thì cập nhật trạng thái vào object
        cbSelect.setOnClickListener(v -> {
            lst1.isSelected = cbSelect.isChecked();

            checkIfAnyItemSelected();
        });

        return convertView;
    }
    private void checkIfAnyItemSelected() {
        boolean hasSelection = false;
        for (int i = 0; i < getCount(); i++) {
            Transaction item = getItem(i);
            if (item != null && item.isSelected) {
                hasSelection = true;
                break;
            }
        }

        // Nếu không còn checkbox nào được chọn, gọi hàm thoát ở Activity
        if (!hasSelection) {
            // Ép kiểu context về HistoryActivity để gọi hàm exitSelectionMode
            if (getContext() instanceof HistoryActivity) {
                ((HistoryActivity) getContext()).exitSelectionMode();
            }
        }
    }
}