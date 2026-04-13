package com.example.quanlychitieu;

public class Transaction {
    // Các cột thuộc tính bạn đã yêu cầu
    public String date;      // Ngày
    public String amount;    // Số tiền (để String để tí nữa parse sau)
    public String service;   // Dịch vụ/Loại hình
    public String content;   // Nội dung ghi chú
    public String type;      // Ký hiệu: + (Gửi), - (Rút), * (Tiết kiệm)

    // Thuộc tính bổ sung để sắp xếp (thời gian tính bằng miligiây)
    public long timestamp;
    public String source;
    public boolean isSelected = false;
    // Hàm khởi tạo (Constructor) - Giúp tạo nhanh 1 giao dịch từ dữ liệu file XML
    public Transaction(String date, String amount, String service, String content, String type, String source, long timestamp) {
        this.date = date;
        this.amount = amount;
        this.service = service;
        this.content = content;
        this.type = type;
        this.timestamp = timestamp;
        this.source=source;
    }
}