package com.oocl.parking.services;

import com.oocl.parking.dto.ParkinglotDto;
import com.oocl.parking.entities.Orders;
import com.oocl.parking.entities.Parkinglot;
import com.oocl.parking.repositories.OrderRepository;
import com.oocl.parking.repositories.ParkinglotRepository;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("parkinglotService")
public class ParkinglotService {


    private ParkinglotRepository parkinglotRepository;

    @Autowired
    private OrderRepository orderRepository;

    private static Logger logger = Logger.getLogger(ParkinglotService.class);

    @Autowired
    public ParkinglotService(ParkinglotRepository parkinglotRepository){
        this.parkinglotRepository = parkinglotRepository;
    }


    public List<ParkinglotDto> getAllParkinglots(Pageable page, String status) {
        if(status != null)
            return parkinglotRepository.findByStatus(page, status).stream()
                    .map(ParkinglotDto::new).collect(Collectors.toList());

        return parkinglotRepository.findAll(page)
                .stream().map(ParkinglotDto::new).collect(Collectors.toList());
    }


    public ParkinglotDto save(Parkinglot parkinglot) {
        if(parkinglot.getId() != null || parkinglotRepository.findByName(parkinglot.getName()).orElse(null) != null)
            return null;
        parkinglotRepository.save(parkinglot);
        return new ParkinglotDto(parkinglot);
    }


    public ParkinglotDto getById(long id) {
        Parkinglot parkinglot = parkinglotRepository.findById(id).orElse(null);
        if(parkinglot == null){
            return null;
        }
        return new ParkinglotDto(parkinglot);
    }


    public boolean changeStatusById(Long id) {
        Parkinglot parkinglot = parkinglotRepository.findById(id).orElse(null);
        if(parkinglot == null || !parkinglot.isEmpty() || parkinglot.getUser() != null){
            return false;
        }
        String status = "";
        if(parkinglot.getStatus().equals("open")) {
            status = "logout";
//            parkinglot.setStatus("logout");
        }else{
            status = "open";
//            parkinglot.setStatus("open");
        }
        parkinglotRepository.changeStatus(id, status);//save(parkinglot);
        return true;
    }

    public ParkinglotDto park(Long id) {
        logger.info("parkinglot id:"+id);
        Parkinglot parkinglot = parkinglotRepository.findById(id).orElse(null);
        logger.info("before parking car parkinglot id:"+parkinglot.getId()+"parkinglot name"+parkinglot.getName()+"parkinglot carCount"+parkinglot.getCountOfCars());
        logger.info("parkinglot size:"+parkinglot.getSize());
        if(parkinglot == null || parkinglot.isFull()){
            logger.info("parkinglot is full:");
           return null;
        }
        parkinglot.park();
//        orderRepository
        Parkinglot save = parkinglotRepository.save(parkinglot);
        logger.info("after parking car parkinglot id:"+save.getId()+"parkinglot name"+save.getName()+"parkinglot carCount"+save.getCountOfCars());
        logger.info("parkinglot size:"+save.getSize());
        return new ParkinglotDto(save);
    }

    public boolean unpark(Long id, Long parkingLotId) {
        Parkinglot parkinglot = parkinglotRepository.findById(parkingLotId).orElse(null);

        if(parkinglot == null || parkinglot.isEmpty()){
            logger.info("parkinglot is empty");
            return false;
        }
        logger.info("before order parkinglot countof car:"+parkinglot.getCountOfCars());
        parkinglot.unpark();
        logger.info("order id:"+id);
        Orders order = orderRepository.findById(id).get();
        order.setStatus("订单完成");
        logger.info(order.getStatus());
        Orders save = orderRepository.save(order);
        logger.info("finish order status:"+save.getStatus());
        Parkinglot save1 = parkinglotRepository.save(parkinglot);
        logger.info("finish order parkinglot countof car:"+save1.getCountOfCars());
        return true;
    }

    public List<ParkinglotDto> getDashboard(Pageable page, String status) {

        return parkinglotRepository.findByStatusAndUserNotNull(page, status).stream()
                .map(ParkinglotDto::new).collect(Collectors.toList());
    }

    public ParkinglotDto changeNameById(Long id, String name, int size) {
        Parkinglot parkinglot =parkinglotRepository.findById(id).orElse(null);
        if(parkinglot == null || (parkinglot.getSize() != size && !parkinglot.isEmpty())){
            return null;
        }
        parkinglotRepository.changeNameAndSizeById(id, size, name);
        parkinglot = parkinglotRepository.findById(id).orElse(null);
        return new ParkinglotDto(parkinglot);
    }

    public List<ParkinglotDto> getNoUserParkinglots(Pageable page, String status) {
        return parkinglotRepository.findAllByStatusAndUserNull(status, page)
                .stream().map(ParkinglotDto::new).collect(Collectors.toList());
    }

    public List<ParkinglotDto> getPakinglotsCombineSearch(Pageable page, String name, String tel, int bt, int st) {
        return parkinglotRepository.findAllBySizeGreaterThan(page, bt)
                .stream().filter(parkinglot ->
                    (matchName(parkinglot, name) && matchTel(parkinglot, tel)) && parkinglot.getSize()<st
                ).map(ParkinglotDto::new).collect(Collectors.toList());
    }

    private boolean matchName(Parkinglot parkinglot, String name){
        return name == null || parkinglot.getName().equals(name);
    }
    private boolean matchTel(Parkinglot parkinglot, String tel){
        return tel == null || parkinglot.getUser().getPhone().equals(tel);
    }
}
