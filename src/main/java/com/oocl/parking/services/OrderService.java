package com.oocl.parking.services;

import com.oocl.parking.dto.ParkinglotDto;
import com.oocl.parking.entities.Orders;
import com.oocl.parking.exceptions.BadRequestException;
import com.oocl.parking.repositories.OrderRepository;
import com.oocl.parking.util.DateUtil;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ParkinglotService parkinglotService;


    private Logger logger = Logger.getLogger(OrderService.class);

    /**
     * cutsom park car generate order before parkingBoy to get the order to park
     * @param order
     * @return
     */
    public Orders parkOrder(Orders order) {
        order.setStatus("无人处理");
        order.setOperation("指派");
        order.setCreatedTime(DateUtil.parseDateToString(new Date()));
        Orders save = orderRepository.save(order);
        logger.info("order id:" + save.getId());
        return order;
    }

    /**
     * let order become getCarStatus
     * @param id
     * @return
     */
    public Orders unparkOrder(Long id) {
        logger.info("unpark order id"+id);
        Orders existOrder = orderRepository.findById(id).orElse(null);

        if (existOrder == null) {
//            logger.info("order is not exist");
            throw new BadRequestException("无效的订单号");
        }
//        existOrder.setStatus("停取中");
        existOrder.setType("取车");
        Orders save = orderRepository.save(existOrder);
        return save;
    }

    /**
     * get all uncompleted order for web end
     * @return
     */
    public List<Orders> getOrders() {
        return orderRepository.findAll()
                .stream()
                .filter(orders -> !(orders.getStatus().equals("订单完成")))
                .collect(Collectors.toList());
    }

    /**
     * distribute Order To ParkingBoy by order and parkingboy id
     * @param id
     * @param boyId
     * @return
     */
    public Orders distributeOrderToParkingBoy(Long id, Long boyId) {
        Orders order = orderRepository.findById(id).get();
        if (order.getBoyId() != null)
            throw new BadRequestException("order is already distribute");
        order.setBoyId(boyId);
        order.setStatus("停取中");
        order.setOperation(null);
        Orders save = orderRepository.save(order);
        logger.info("order id:"+save.getId()+"boy id:"+save.getBoyId()+"parkinglot id:"+save.getParkinglotId());
        return order;
    }

    /**
     * distribute Order To ParkingLot by order and parkinglot id
     * @param id
     * @param parkingLotId
     * @return
     */
    public Orders distributeOrderToParkingLot(Long id, Long parkingLotId) {
        Orders order = orderRepository.findById(id).get();
        logger.info("before distributeOrderToParkingLot database order id:"+order.getId()+"car number:"+order.getCarId()+ (order.getType().equals("存车")?"parkingboy need to park car":order.getType().equals("取车")?"customer need to unpark":"order type is unknown"));
        logger.info("order status:"+order.getStatus()+"order boyId:"+order.getBoyId());
        order.setParkinglotId(parkingLotId);
        ParkinglotDto parkinglotDto = parkinglotService.park(parkingLotId);
        if( parkinglotDto == null){
            throw new BadRequestException("parked failed");
        }
        else {
            return orderRepository.save(order);
        }
    }

    /**
     * get all undeal order
     * @return
     */
    public List<Orders> getNoHandledOrders() {
        return orderRepository.findAll()
                .stream()
                .filter(order -> order.getStatus().equals("无人处理"))
                .collect(Collectors.toList());
    }

    /**
     * get all unfinish order by parkingboyId
     * @param parkingBoyId
     * @return
     */
    public List<Orders> getUncompletedOrdersByParkingBoyId(Long parkingBoyId) {
        return orderRepository.findByBoyId(parkingBoyId)
                .stream()
                .filter(orders -> !(orders.getStatus().equals("订单完成")))
                .collect(Collectors.toList());
    }

    /**
     * find order by carId and soon...
     * @param id
     * @param carId
     * @param type
     * @param status
     * @return
     */
    public List<Orders> selectByParam(Long id, String carId, String type, String status) {
//        return orderRepository.findByIdOrCarIdOrTypeOrStatus(id, carId, type, status);
//        "%"+name+"%","%"+email+"%","%"+phone+"%",id
        return orderRepository.findByIdOrCarIdLikeOrTypeLikeOrStatusLike(id,"%"+carId+"%","%"+type+"%","%"+status+"%");
    }

    public List<Orders> getCompletedOrdersByBoyId(Long parkingBoyId) {
        return orderRepository.findAll().stream()
                .filter(orders -> orders.getStatus().equals("订单完成"))
                .collect(Collectors.toList());
    }
}
