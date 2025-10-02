package com.phamtruong.rookbooks.service;

import com.phamtruong.rookbooks.entity.Order;
import com.phamtruong.rookbooks.entity.OrderDetail;

import java.util.List;

public interface OrderDetailService {
    List<OrderDetail> getAllOrderDetailByOrder(Order order);
}
