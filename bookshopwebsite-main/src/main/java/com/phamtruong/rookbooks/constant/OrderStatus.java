package com.phamtruong.rookbooks.constant;

public interface OrderStatus {
    String PENDING = "ĐANG CHỜ XỬ LÝ";         // Đang chờ xử lý
    String PROCESSING = "ĐANG XỬ LÝ";     // Đang xử lý
    String DELIVERING = "ĐANG GIAO HÀNG";        // Đang giao hàng
    String DELIVERED = "ĐÃ GIAO THÀNH CÔNG";      // Đã giao thành công
    String CANCELLED = "ĐÃ HỦY";       // Đã hủy
    String UNPAID = "CHƯA THANH TOÁN"; // chưa hoàn tất việc thanh toán
}

