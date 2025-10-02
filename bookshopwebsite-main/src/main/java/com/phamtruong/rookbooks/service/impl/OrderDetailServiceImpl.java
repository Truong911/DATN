package com.phamtruong.rookbooks.service.impl;

import com.phamtruong.rookbooks.entity.Order;
import com.phamtruong.rookbooks.entity.OrderDetail;
import com.phamtruong.rookbooks.service.OrderDetailService;
import com.phamtruong.rookbooks.repository.OrderDetailRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class OrderDetailServiceImpl implements OrderDetailService {

    private OrderDetailRepository orderDetailRepository;

    @Override
    public List<OrderDetail> getAllOrderDetailByOrder(Order order) {
        return orderDetailRepository.findByOrder(order);
    }
}
